package mdp2021.frontend.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.GUI.GUI_JavaFX_Controller;
import mdp2021.backend.services.RMI.RMI_services_interface;

public class Controller
{
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
	
	private void RMI_Init()
	{
		System.setProperty("java.security.policy", "./Resources/client_policyfile.txt");
		
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
	
	private void loadGUI()
	{
	
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
		
		loadGUI();
	}
}