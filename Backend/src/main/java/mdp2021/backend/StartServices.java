package mdp2021.backend;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.GUI.Main;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.Filesystem_ReportPersistence;
import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.services.RMI.RMI_services;
import mdp2021.backend.services.RMI.RMI_services_interface;
import mdp2021.backend.services.socket.MessageProcessor;
import mdp2021.backend.services.socket.MulticastSocketService;
import mdp2021.backend.services.socket.Socket_service;
import mdp2021.backend.utilities.REDIS_UserSessions;
import mdp2021.backend.utilities.UserSessions;
import redis.clients.jedis.JedisPool;

public class StartServices
{
	private static final Logger log = Logger.getLogger(StartServices.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/StartServices.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private static final String propertiesPath = "Resources/backend constants.properties";
	private static final String userSessionDurationProperty = "userSessionDuration";
	private static final String RMI_service_nameProperty = "RMI_service_name";
	private static final String RMI_service_portProperty = "RMI_service_port";
	private static final String JedisPool_URIProperty = "JedisPool_URI";
	private static final String KEY_STORE_PATH = "./Resources/keystore.p12";
	private static final String KEY_STORE_PASSWORDProperty = "KEY_STORE_PASSWORD";
	private static final String socketServicePortProperty = "socketServicePort";
	private static final String MULTICAST_GROUP_property = "MULTICAST_GROUP";
	private static final String MULTICAST_PORT_property = "MULTICAST_PORT";
	private static final String MULTICAST_MAX_BUFFER_SIZE_property = "MULTICAST_MAX_BUFFER_SIZE";
	
	
	public static Socket_service socketService;
	public static MulticastSocketService multicastService;
	
	//public static final String host = "127.0.0.1";
	//public static final int port = 5201;
	public static String KEY_STORE_PASSWORD;
	
	public static int sessionDurationSeconds;
	public static String RMI_service_name;
	public static int RMI_port;
	public static int socketServicePort;
	
	public static String MULTICAST_GROUP;
	public static int MULTICAST_PORT;
	public static int MULTICAST_MAX_BUFFER_SIZE;
	
	public static final JedisPool pool;
	
	private static Registry registry;
	
	static
	{
		Properties backendProperties = new Properties();
		
		try(FileInputStream fis = new FileInputStream(new File(propertiesPath)))
		{
			backendProperties.load(fis);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String jedisPoolURI = backendProperties.getProperty(JedisPool_URIProperty);
		pool = new JedisPool(jedisPoolURI);
		
		sessionDurationSeconds = Integer.parseInt(backendProperties.getProperty(userSessionDurationProperty));
		RMI_service_name = backendProperties.getProperty(RMI_service_nameProperty);
		RMI_port = Integer.parseInt(backendProperties.getProperty(RMI_service_portProperty));
		KEY_STORE_PASSWORD = backendProperties.getProperty(KEY_STORE_PASSWORDProperty);
		socketServicePort = Integer.parseInt(backendProperties.getProperty(socketServicePortProperty));
		
		MULTICAST_GROUP = backendProperties.getProperty(MULTICAST_GROUP_property);
		MULTICAST_PORT = Integer.parseInt(backendProperties.getProperty(MULTICAST_PORT_property));
		MULTICAST_MAX_BUFFER_SIZE = Integer.parseInt(backendProperties.getProperty(MULTICAST_MAX_BUFFER_SIZE_property));
	}
	
	
	public static void stopServices()
	{
		socketService.stopSocketService();
		
		try
		{
			UnicastRemoteObject.unexportObject(registry, true);
		} 
		catch (NoSuchObjectException e)
		{
			log.warning(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		System.setProperty("java.security.policy", "./Resources/server_policyfile.txt");
		
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		
		try
		{
			IReportPersistence reportPersistence = new Filesystem_ReportPersistence("./Application data/Reports/");
			RMI_services rmiService = new RMI_services(reportPersistence, sessionDurationSeconds);
			RMI_services_interface stub = (RMI_services_interface)UnicastRemoteObject.exportObject(rmiService, RMI_port);
			registry = LocateRegistry.createRegistry(RMI_port);
			registry.rebind(RMI_service_name, stub);
			System.out.println("RMI started");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		// create administrator session and pass the cookie to the GUI
		UserSessions session = new REDIS_UserSessions(sessionDurationSeconds);
		User admin = new User(new TrainStation(-1), "admin", "", null);
		
		String adminCookie = session.login(admin).get();
		
		MessageProcessor.initialize(sessionDurationSeconds);
				
		// start socket service
		try
		{
			socketService = new Socket_service(socketServicePort, KEY_STORE_PATH, KEY_STORE_PASSWORD);
			socketService.start();
			System.out.println("Socket service started.");
		}
		catch(IOException e)
		{
			log.severe(e.getMessage());
			System.out.println("Couldn't start socket service.");
			return;
		}
	

		String[] applicationArguments = {adminCookie};
		Main.main(applicationArguments);
	}
}
