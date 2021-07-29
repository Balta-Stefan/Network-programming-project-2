import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.GUI.GUI_JavaFX_Controller;
import mdp2021.backend.GUI.GUI_Starter;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.Filesystem_ReportPersistence;
import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.persistence.ITrainstationPersistence;
import mdp2021.backend.persistence.IUserDAO;
import mdp2021.backend.persistence.REDIS_TrainstationPersistence;
import mdp2021.backend.persistence.XML_UserDAO;
import mdp2021.backend.services.RMI.RMI_services;
import mdp2021.backend.services.RMI.RMI_services_interface;
import mdp2021.backend.services.socket.MessageProcessor;
import mdp2021.backend.services.socket.MulticastSocketService;
import mdp2021.backend.services.socket.Socket_service;
import mdp2021.backend.utilities.BCrypt_hasher;
import mdp2021.backend.utilities.PasswordHasher;
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
	
	public static void addTestData()
	{
		IReportPersistence reportPersistence = new Filesystem_ReportPersistence("./Application data/Reports/");
		IUserDAO userDatabase = new XML_UserDAO(".\\Application data\\Users\\");
		PasswordHasher hasher = new BCrypt_hasher();
		
		// add train stations
		TrainStation station1 = new TrainStation(1);
		TrainStation station2 = new TrainStation(2);
		TrainStation station3 = new TrainStation(3);
		
		// add users
		User user1 = new User(station1, "Marko", "1234", hasher.getSalt());
		User user2 = new User(station1, "Slavko", "1234", hasher.getSalt());
		User user3 = new User(station2, "Safet", "1234", hasher.getSalt());
		User user4 = new User(station3, "Pufet", "1234", hasher.getSalt());
		
		try
		{
			user1.setPassword(hasher.hash(user1.getSalt(), user1.getPassword()));
			user2.setPassword(hasher.hash(user2.getSalt(), user2.getPassword()));
			user3.setPassword(hasher.hash(user3.getSalt(), user3.getPassword()));
			user4.setPassword(hasher.hash(user4.getSalt(), user4.getPassword()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		System.out.println(userDatabase.addUser(user1));
		System.out.println(userDatabase.addUser(user2));
		System.out.println(userDatabase.addUser(user3));
		System.out.println(userDatabase.addUser(user4));
		
	
		
		// add train station lines
		StationArrival arrival1 = new StationArrival(station1, null, false);
		StationArrival arrival2 = new StationArrival(station2, null, false);
		StationArrival arrival3 = new StationArrival(station3, LocalDateTime.now(), false);
		
		List<StationArrival> stationArrivals = Arrays.asList(arrival1, arrival2, arrival3);
		
		TrainLine line1 = new TrainLine(1, "A-B-C-D", stationArrivals);
		
		LinesOfTrainstation lot = new LinesOfTrainstation(station1, Arrays.asList(line1));
		

	
		ITrainstationPersistence tp = new REDIS_TrainstationPersistence();
		tp.addTrainStation(station1);
		tp.addTrainStation(station2);
		tp.addTrainStation(station3);
		
		tp.addUserToTrainstation(user1);
		tp.addUserToTrainstation(user2);
		tp.addUserToTrainstation(user3);
		tp.addUserToTrainstation(user4);
		
	}
			
	public static void main(String[] args)
	{
		
		
		/*
		// set up REST service
		REST_service.trainstationPersistence = trainstationPersistence;
		REST_service.userSessions = userSessions;

		// set up RMI service - to do
		RMI_services rmiService = new RMI_services(userSessions, reportPersistence);
		
		
		// set up socket service - to do (multicast service left)
		Socket_service socketService = null;
		try
		{
			socketService = new Socket_service(host, port, KEY_STORE_PATH, KEY_STORE_PASSWORD);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}*/
		

		//addTestData();
		
	
		
		IReportPersistence reportPersistence = new Filesystem_ReportPersistence("./Application data/Reports/");
		//reportPersistence.listReports();
		
	
		System.setProperty("java.security.policy", "./Resources/server_policyfile.txt");
		
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		
		try
		{
			RMI_services rmiService = new RMI_services(reportPersistence, sessionDurationSeconds);
			RMI_services_interface stub = (RMI_services_interface)UnicastRemoteObject.exportObject(rmiService, RMI_port);
			Registry registry = LocateRegistry.createRegistry(RMI_port);
			registry.rebind(RMI_service_name, stub);
			System.out.println("RMI started");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("RMI started 2");
		
		// start the socket service - to do
		

		
		// create administrator session and pass the cookie to the GUI
		
	
		UserSessions session = new REDIS_UserSessions(sessionDurationSeconds);
		User admin = new User(new TrainStation(-1), "admin", "", null);
		
		GUI_JavaFX_Controller.adminCookie = session.login(admin).get();
		
		MessageProcessor.initialize(sessionDurationSeconds);
				
		// start socket service
		// Socket_service(int port, String KEY_STORE_PATH, String KEY_STORE_PASSWORD)
		try
		{
			socketService = new Socket_service(socketServicePort, KEY_STORE_PATH, KEY_STORE_PASSWORD);
			socketService.start();
		}
		catch(IOException e)
		{
			log.severe(e.getMessage());
			System.out.println("Couldn't start socket service.");
			return;
		}
		
		try
		{
			multicastService = new MulticastSocketService(MULTICAST_PORT, MULTICAST_GROUP, MULTICAST_MAX_BUFFER_SIZE);
		}
		catch (IOException e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return;
		}

		GUI_Starter.main(null);
	}
}
