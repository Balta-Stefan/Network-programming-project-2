package mdp2021.frontend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.rpc.ServiceException;

import mdp2021.backend.GUI.GUI_JavaFX_Controller;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.services.RMI.RMI_services_interface;
import mdp2021.backend.services.SOAP.SOAP_service;
import mdp2021.backend.services.SOAP.SOAP_serviceServiceLocator;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.LoginReply;

public class Controller
{
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
	
	private static final String propertiesPath = "Resources\\application properties.properties";
	private final String RMI_service_name;
	private final int RMI_port;
	
	private RMI_services_interface rmiService;
	private SOAP_service soapService;
	
	// REST related
	private static final Client client = ClientBuilder.newClient();
	private static final WebTarget webTarget = client.target(apiURL);
	
	private String cookie;
	private TrainStation trainstationInfo;
	
	private void RMI_Init()
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
	
	private void SOAP_init()
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
	
	public Controller()
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
		
		
		// perform intialization for RMI
		RMI_Init();
		SOAP_init();
	}
	
	private void postLoginActions()
	{
		// to do
		
	}
	
	public LoginReply login(String username, String password) throws RemoteException
	{
		LoginReply reply = soapService.login(username, password);
		
		cookie = reply.getCookie();
    	trainstationInfo = reply.getTrainstationInfo();
    	
    	if(reply.getCodeResponse().getCode() == 200)
    		postLoginActions();
    	
    	return reply;
	}

	public Code_response logout() throws RemoteException
	{
		Code_response response = soapService.logout(cookie);
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
		
		Cookie cookieObject = new Cookie("cookie", cookie);
		invocationBuilder.cookie(cookieObject);
		
		Response response = invocationBuilder.get();
		
		return response.readEntity(LinesOfTrainstation.class);
	}
}
