package mdp2021.backend.GUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.Filesystem_ReportPersistence;
import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.persistence.ITrainstationPersistence;
import mdp2021.backend.persistence.IUserDAO;
import mdp2021.backend.persistence.REDIS_TrainstationPersistence;
import mdp2021.backend.persistence.XML_UserDAO;
import mdp2021.backend.services.RMI.RMI_services_interface;
import mdp2021.backend.services.socket.MulticastSocketService;
import mdp2021.backend.shared.FileHolder;
import mdp2021.backend.utilities.BCrypt_hasher;
import mdp2021.backend.utilities.PasswordHasher;


public class Backend_GUI_Initializer extends Application
{
	private static final Logger log = Logger.getLogger(Backend_GUI_Initializer.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/Backend_GUI_Initializer.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static final String propertiesPath = "Resources/backend constants.properties";
	private static final String MULTICAST_GROUP_property = "MULTICAST_GROUP";
	private static final String MULTICAST_PORT_property = "MULTICAST_PORT";
	private static final String MULTICAST_MAX_BUFFER_SIZE_property = "MULTICAST_MAX_BUFFER_SIZE";
	private static final String fxmlPath = "MDP2021 GUI.fxml";
	
	private String MULTICAST_GROUP;
	private int MULTICAST_PORT;
	private int MULTICAST_MAX_BUFFER_SIZE;
	
	
	private final ITrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();
	private final IUserDAO userPersistence = new XML_UserDAO(".\\Application data\\Users\\");
	
	
	private static final String downloadFolder = "Application data\\Downloaded reports\\";
	private static final String RMI_service_nameProperty = "RMI_service_name";
	private static final String RMI_service_portProperty = "RMI_service_port";
	
	private String adminCookie;

	private String RMI_service_name;
	private int RMI_port;
	
	private RMI_services_interface rmiService;
	private MulticastSocketService multicastService;
	private Registry rmiRegistry;
	
	private GUI_JavaFX_Controller javaFXController;
	
	@Override
	public void stop()
	{
		javaFXController.shutdown();
	}
	
	private boolean initializeServices()
	{
		System.setProperty("java.security.policy", "./Resources/client_policyfile.txt");
		
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try
		{
			rmiRegistry = LocateRegistry.getRegistry(RMI_port);
			rmiService = (RMI_services_interface)rmiRegistry.lookup(RMI_service_name);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			//e.printStackTrace();
		}
		
		try
		{
			multicastService = new MulticastSocketService(MULTICAST_PORT, MULTICAST_GROUP, MULTICAST_MAX_BUFFER_SIZE);
		}
		catch (IOException e)
		{
			log.severe(e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void initData()
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
		
		RMI_service_name = backendProperties.getProperty(RMI_service_nameProperty);
		RMI_port = Integer.parseInt(backendProperties.getProperty(RMI_service_portProperty));
		
		
		MULTICAST_GROUP = backendProperties.getProperty(MULTICAST_GROUP_property);
		MULTICAST_PORT = Integer.parseInt(backendProperties.getProperty(MULTICAST_PORT_property));
		MULTICAST_MAX_BUFFER_SIZE = Integer.parseInt(backendProperties.getProperty(MULTICAST_MAX_BUFFER_SIZE_property));
	}
	

	@Override
	public void start(Stage primaryStage)
	{
		Parameters params = getParameters();
		List<String> parameters = params.getRaw();
		adminCookie = parameters.get(0);
		
		
		initData();
		if(initializeServices() == false)
		{
			System.out.println("Could not start multicast service!");
			try
			{
				Thread.sleep(1500);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(fxmlPath));
		
		Parent root = null;
		try
		{
			root = fxmlLoader.load();
		}
		catch (IOException e)
		{
			log.warning(e.getMessage());
			e.printStackTrace();
			return;
		}
		//FXMLLoader.load(Paths.get(fxmlPath).toUri().toURL());
		javaFXController = fxmlLoader.<GUI_JavaFX_Controller>getController();
		javaFXController.setApplicationObject(this);
		
		primaryStage.setTitle("MDP2021 (Balta Stefan)");
		primaryStage.setScene(new Scene(root));
		
		primaryStage.show();
	}
	
	
	
	
	
	public List<User> getUsers()
	{
		return userPersistence.getUsers();
	}
	
	public Optional<List<TrainStation>> getTrainStations()
	{
		return trainstationPersistence.getTrainStations();
	}
	
	public Optional<LinesOfTrainstation> getTrainstationLines(TrainStation station)
	{
		return trainstationPersistence.getTrainstationLines(station);
	}
	
	public PasswordHasher getHasher()
	{
		return new BCrypt_hasher();
	}
	
	public boolean addUser(User user)
	{
		return userPersistence.addUser(user);
	}
	
	public boolean removeUser(User user)
	{
		return userPersistence.removeUser(user);
	}
	
	public boolean addTrainStation(TrainStation station)
	{
		return trainstationPersistence.addTrainStation(station);
	}
	
	public boolean removeStation(TrainStation station)
	{
		return trainstationPersistence.removeStation(station);
	}
	
	public TrainLine addLine(TrainLine line)
	{
		return trainstationPersistence.addLine(line);
	}
	
	public boolean removeLine(TrainLine line)
	{
		return trainstationPersistence.removeLine(line);
	}
	
	public List<FileHolder> listReports() throws RemoteException
	{
		return rmiService.listReports(adminCookie);
	}
	
	public FileHolder getReport(String filename) throws RemoteException
	{
		return rmiService.getReport(adminCookie, filename);
	}
	
	public void saveReport(FileHolder file)
	{
		IReportPersistence reportPersistence = new Filesystem_ReportPersistence(downloadFolder);
    	reportPersistence.saveReport(file);
	}
	
	public MulticastSocketService getMulticastService()
	{
		return multicastService;
	}
	
	public Boolean sendMulticastData(byte[] data)
	{
		return multicastService.sendData(data);
	}
}
