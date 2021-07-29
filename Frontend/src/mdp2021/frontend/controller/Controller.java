package mdp2021.frontend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.rpc.ServiceException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import mdp2021.backend.services.socket.CustomSocket;
import mdp2021.backend.services.socket.MulticastSocketService;
import mdp2021.backend.GUI.GUI_JavaFX_Controller;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainPassReport;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.TrainstationUsers;
import mdp2021.backend.model.User;
import mdp2021.backend.services.RMI.RMI_services_interface;
import mdp2021.backend.services.SOAP.SOAP_service;
import mdp2021.backend.services.SOAP.SOAP_serviceServiceLocator;
import mdp2021.backend.shared.Announcement;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.FileHolder;
import mdp2021.backend.shared.FileOrTextMessage;
import mdp2021.backend.shared.LoginReply;
import mdp2021.backend.shared.SubscribeRequest;
import mdp2021.frontend.GUI.EmployeePanelController;
import mdp2021.frontend.utilities.MessageDaemon;

public class Controller
{
	private static class AnnouncementUpdaterDaemon extends Thread
	{
		private static final Logger log = Logger.getLogger(AnnouncementUpdaterDaemon.class.getName());
		static
		{
			log.setLevel(Level.FINEST);
			FileHandler txtHandler;
			try
			{
				txtHandler = new FileHandler("Logs/AnnouncementUpdaterDaemon.txt", true);
				SimpleFormatter txtFormatter = new SimpleFormatter();
				txtHandler.setFormatter(txtFormatter);
				log.addHandler(txtHandler);
			} catch (SecurityException | IOException e)
			{
				e.printStackTrace();
			}
		}
	
		private static final int sleepPeriod = 5000;
		
		private final MulticastSocketService multicastService;
		private final EmployeePanelController interfaceToUpdate;
	
		private boolean run = true;
		
		public AnnouncementUpdaterDaemon(MulticastSocketService multicastService, EmployeePanelController interfaceToUpdate)
		{
			this.multicastService = multicastService;
			this.interfaceToUpdate = interfaceToUpdate;
			
			multicastService.start();
		}
		
		public void stopService()
		{
			run = false;
			multicastService.stopService();
		}
		
		@Override
		public void run()
		{
			Gson gson = new Gson();
			
			while(run)
			{
				try
				{
					Thread.sleep(sleepPeriod);
					if(run == false)
						return;
					
					List<Byte[]> data = multicastService.getData();
					if(data.isEmpty())
						continue;
					
					List<Announcement> announcements = new ArrayList<>();
					
					for(Byte[] byteArray : data)
					{
						byte[] primitiveArray = new byte[byteArray.length];
						for(int i = 0; i < byteArray.length; i++)
							primitiveArray[i] = byteArray[i];
						
						String announcementJSON = new String(primitiveArray, StandardCharsets.UTF_8);
						announcementJSON = announcementJSON.trim();
						try
						{
							Announcement announcement = gson.fromJson(announcementJSON, Announcement.class);
							announcements.add(announcement);
						}
						catch(Exception e)
						{
							log.warning(e.getMessage());
						}
					}
					
					interfaceToUpdate.updateAnnouncementsList(announcements);
				} 
				catch (InterruptedException e)
				{
					log.warning(e.getMessage());
				}
			}
		}
	
		
	}
	
	
	private static final String apiURL = "http://localhost:8080/MDP2021_backend/api/v1";
	private static final Logger log = Logger.getLogger(GUI_JavaFX_Controller.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/Controller.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private static final String downloadFolder = "Application data\\Downloaded reports\\";
	private static final String RMI_service_nameProperty = "RMI_service_name";
	private static final String RMI_service_portProperty = "RMI_service_port";
	private static final String socketServiceAddressProperty = "socketServiceAddress";
	private static final String socketServicePortProperty = "socketServicePort";
	private static final String TRUST_STORE_PATH = "./Resources/clientTruststore.p12";
	private static final String TRUST_STORE_PASSWORD_property = "TRUST_STORE_PASSWORD";
	private static final String MULTICAST_GROUP_property = "MULTICAST_GROUP";
	private static final String MULTICAST_PORT_property = "MULTICAST_PORT";
	
	private static final String propertiesPath = "Resources\\application properties.properties";
	private static final String RMI_service_name;
	private static final int RMI_port;
	private static final String socketServiceAddress;
	private static final int socketServicePort;
	private static final String KEY_STORE_PASSWORD;
	public static String MULTICAST_GROUP;
	public static int MULTICAST_PORT;
	public static final int MULTICAST_MAX_BUFFER_SIZE = 2048;
	
	private static RMI_services_interface rmiService;
	private static SOAP_service soapService;
	private static MessageDaemon messageDaemon;
	private static MulticastSocketService multicastService;
	private AnnouncementUpdaterDaemon announcementsDaemon;
	
	// REST related
	private static final Client client = ClientBuilder.newClient();
	private static final WebTarget webTarget = client.target(apiURL);
	
	private static String cookie;
	private static TrainStation trainstationInfo;
	private static Cookie cookieObject;
	private static String username;
	
	private static void RMI_Init()
	{
		System.setProperty("java.security.policy", "Resources/client_policyfile.txt");
		
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try
		{
			Registry registry = LocateRegistry.getRegistry(RMI_port);
			rmiService = (RMI_services_interface)registry.lookup(RMI_service_name);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private static void SOAP_init()
	{
		SOAP_serviceServiceLocator locator = new SOAP_serviceServiceLocator();
		
		try
		{
			soapService = locator.getSOAP_service();
		}
		catch (ServiceException e)
		{
			log.severe(e.getMessage());
		}
	}
	
	
	private EmployeePanelController employeePanel;
	
	public boolean sendMulticastData(String announcementMessage)
	{
		Announcement announcement = new Announcement(announcementMessage, new User(trainstationInfo, username, null, null));
		Gson gson = new Gson();
		
		String announcementJSON = gson.toJson(announcement);
		
		return multicastService.sendData(announcementJSON.getBytes(StandardCharsets.UTF_8));
	}
	
	static
	{
		// load properties
		Properties backendProperties = new Properties();
		
		try(FileInputStream fis = new FileInputStream(new File(propertiesPath)))
		{
			backendProperties.load(fis);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			log.info(e.getMessage());
		}
		
		RMI_service_name = backendProperties.getProperty(RMI_service_nameProperty);
		RMI_port = Integer.parseInt(backendProperties.getProperty(RMI_service_portProperty));
		socketServiceAddress = backendProperties.getProperty(socketServiceAddressProperty);
		socketServicePort = Integer.parseInt(backendProperties.getProperty(socketServicePortProperty));
		KEY_STORE_PASSWORD = backendProperties.getProperty(TRUST_STORE_PASSWORD_property);
		MULTICAST_GROUP = backendProperties.getProperty(MULTICAST_GROUP_property);
		MULTICAST_PORT = Integer.parseInt(backendProperties.getProperty(MULTICAST_PORT_property));
		
		// perform intialization for RMI
		RMI_Init();
		SOAP_init();
		
		// secure socket init
		System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
		System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);
	}
	
	
	public void setEmployeePanelController(EmployeePanelController employeePanel)
	{
		this.employeePanel = employeePanel;
		messageDaemon.setPanel(employeePanel);
		messageDaemon.start();
		
		try
		{
			multicastService = new MulticastSocketService(MULTICAST_PORT, MULTICAST_GROUP, MULTICAST_MAX_BUFFER_SIZE);
		} 
		catch (IOException e)
		{
			log.severe(e.getMessage());
		}
		
		announcementsDaemon = new AnnouncementUpdaterDaemon(multicastService, employeePanel);
		announcementsDaemon.start();
	}
	

	
	/*public Controller()
	{
		// load properties
		Properties backendProperties = new Properties();
		
		try(FileInputStream fis = new FileInputStream(new File(propertiesPath)))
		{
			backendProperties.load(fis);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			log.info(e.getMessage());
		}
		
		RMI_service_name = backendProperties.getProperty(RMI_service_nameProperty);
		RMI_port = Integer.parseInt(backendProperties.getProperty(RMI_service_portProperty));
		socketServiceAddress = backendProperties.getProperty(socketServiceAddressProperty);
		socketServicePort = Integer.parseInt(backendProperties.getProperty(socketServicePortProperty));
		KEY_STORE_PASSWORD = backendProperties.getProperty(TRUST_STORE_PASSWORD_property);
		
		// perform intialization for RMI
		RMI_Init();
		SOAP_init();
		
		// secure socket init
		System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
		System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);
	}*/
	
	private CustomSocket sendSecureMessage(Object message)
	{
		// create a socket
		SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket secureSocket = null;
		try
		{
			secureSocket = (SSLSocket)sf.createSocket(socketServiceAddress, socketServicePort);
			secureSocket.setKeepAlive(true);
					
			CustomSocket cs = new CustomSocket(secureSocket);
			cs.send(message);
			
			return cs;
		}
		catch (UnknownHostException e)
		{
			log.severe(e.getMessage());
			return null;
		}
		catch (IOException e)
		{
			log.severe(e.getMessage());
			return null;
		}
	}
	
	public Optional<LoginReply> login(String username, String password) throws RemoteException
	{
		LoginReply reply = soapService.login(username, password);
		Controller.username = username;
		
		cookie = reply.getCookie();
    	trainstationInfo = reply.getTrainstationInfo();
    	
    	cookieObject = new Cookie("cookie", cookie);
    	
    	if(reply.getCodeResponse().getCode() == 200)
    	{
    		// subscribe for chat notifications
    		SubscribeRequest request = new SubscribeRequest(cookie, SubscribeRequest.Type.SUBSCRIBE);
    		
    		CustomSocket socket = sendSecureMessage(request);
    		// check the response
    		Optional<Object> responseObj = socket.receive();
    		if(responseObj.isEmpty() || (responseObj.get() instanceof Code_response == false))
    			return Optional.empty();
    		
    		Code_response loginInfo = (Code_response)responseObj.get();
    		
    		messageDaemon = new MessageDaemon(socket);
    		//messageDaemon.start();
    		
    		System.out.println("Subscription: " + loginInfo.getCode() + ", " + loginInfo.getMessage());
    		return Optional.of(reply);
    	}
    	
    	return Optional.empty();
	}

	public Code_response logout() throws RemoteException
	{
		// unsubscribe from the push notification service
		SubscribeRequest unsubscribeNotification = new SubscribeRequest(cookie, SubscribeRequest.Type.UNSUBSCRIBE);
		CustomSocket socket = sendSecureMessage(unsubscribeNotification);
		
		Optional<Object> unsubscribeResponseObj = socket.receive();
		if(unsubscribeResponseObj.isPresent() && unsubscribeResponseObj.get() instanceof Code_response)
		{
			Code_response unsubscribeRequest = (Code_response)unsubscribeResponseObj.get();
			int a = 3;
		}
		
		try
		{
			socket.close();
		}
		catch (IOException e1)
		{
			log.warning(e1.getMessage());
		}
		
		try
		{
			messageDaemon.stopDaemon();
		}
		catch (IOException e)
		{
			log.warning(e.getMessage());
		}
		
		Code_response response = soapService.logout(cookie);
		
		cookie = null;
		trainstationInfo = null;
		cookieObject = null;
		username = null;
		
		announcementsDaemon.stopService();
		
		return response;
	}
	
	public int getTrainstationID()
	{
		return trainstationInfo.getID();
	}

	public LinesOfTrainstation getLines()
	{
		WebTarget lineWebTarget = webTarget.path("train-schedule");
		
		Invocation.Builder invocationBuilder = lineWebTarget.request(MediaType.APPLICATION_JSON);
		
		invocationBuilder.cookie(cookieObject);
		
		Response response = invocationBuilder.get();
		
		return response.readEntity(LinesOfTrainstation.class);
	}

	public String reportTrainPass(LocalDateTime dateTime, TrainLine line)
	{
		TrainPassReport report = new TrainPassReport(trainstationInfo, line, dateTime);
		
		WebTarget lineWebTarget = webTarget.path("train-schedule");
		
		Invocation.Builder invocationBuilder = lineWebTarget.request(MediaType.APPLICATION_JSON);
		invocationBuilder.cookie(cookieObject);
		
		Response response = invocationBuilder.put(Entity.entity(report, MediaType.APPLICATION_JSON));
		
		return response.readEntity(String.class);
	}

	public Code_response uploadReport(File report)
	{
		try
		{
			byte[] fileData = Files.readAllBytes(report.toPath());
			return rmiService.sendReport(cookie, report.getName(), fileData);
		}
		catch (RemoteException e)
		{
			log.warning(e.getMessage());
			return new Code_response(0, "Error - cannot retrieve data from the server.");
		} catch (IOException e)
		{
			log.severe(e.getMessage());
			return new Code_response(0, "Error - cannot read the file.");
		}
	}

	public Optional<List<TrainstationUsers>> getTrainstationUsers()
	{
		try
		{
			Gson gson = new Gson();
			
			String users = soapService.getTrainstationUsers(cookie);
			System.out.println(users);
			Type listOfTestObject = new TypeToken<List<TrainstationUsers>>(){}.getType();
			
			List<TrainstationUsers> trainStationsInfo = gson.fromJson(users, listOfTestObject);
			
			for(TrainstationUsers t: trainStationsInfo)
				System.out.println(t);
			
			return Optional.of(trainStationsInfo);
		}
		catch (RemoteException e)
		{
			log.severe(e.getMessage());
			return Optional.empty();
		}
		catch(JsonSyntaxException je)
		{
			log.severe(je.getMessage());
			return Optional.empty();
		}
	}

	public Code_response sendMessage(String message, List<File> files, String receiver_username)
	{
		List<FileHolder> fileWrappers = new ArrayList<>();
		
		for(File f : files)
		{
			byte[] fileData;
			try
			{
				fileData = Files.readAllBytes(f.toPath());
				FileHolder tempHolder = new FileHolder(f.getName(), fileData, username, LocalDateTime.now());
				fileWrappers.add(tempHolder);
			} 
			catch (IOException e)
			{
				log.warning(e.getMessage());
				return new Code_response(0, "Couldn't read the file " + f.getName());
			}
		}
		
		FileOrTextMessage messageWrapper = new FileOrTextMessage(message, fileWrappers, cookie, receiver_username);
		
		CustomSocket socket = sendSecureMessage(messageWrapper);
		
		Optional<Object> responseObj = socket.receive();
		if(responseObj.isEmpty())
			return new Code_response(0, "Error.");
		if(responseObj.get() instanceof Code_response == false)
			return new Code_response(0, "Response cannot be parsed.");
		
		return (Code_response)responseObj.get();
	}
}
