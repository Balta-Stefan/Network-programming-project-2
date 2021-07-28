package mdp2021.frontend.GUI;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mdp2021.frontend.controller.Controller;

public class BaseFXController
{
	private static final Logger log = Logger.getLogger(BaseFXController.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/GUI_Controller.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	protected static Controller frontendController = new Controller();
	
	protected static final String loginPanelFXMLPath = "Resources/login screen.fxml";
	protected static final String employeePanelFXMLPath = "Resources/employee panel.fxml";
	
	protected static final ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(1);
	
	protected void activateWindow(String FXML_path, String title, Event event)
	{
		try
		{
			Parent newRoot = FXMLLoader.load(Paths.get(FXML_path).toUri().toURL());
			Stage newStage = new Stage();
			newStage.setTitle(title);
			newStage.setScene(new Scene(newRoot));
			newStage.show();
			
			 ((Node)(event.getSource())).getScene().getWindow().hide();
		} catch (IOException e)
		{
			log.warning(e.getMessage());
			e.printStackTrace();
		}
	}
}
