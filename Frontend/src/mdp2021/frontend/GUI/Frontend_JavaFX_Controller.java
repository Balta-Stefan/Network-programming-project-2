package mdp2021.frontend.GUI;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.LoginReply;
import mdp2021.frontend.controller.Controller;

public class Frontend_JavaFX_Controller
{
	private static final Logger log = Logger.getLogger(Frontend_JavaFX_Controller.class.getName());
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
	
	private static final ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(1);
			
	
	private static final String employeePanelFXMLPath = "Resources/employee panel.fxml";
	private static final String loginPanelFXMLPath = "Resources/login screen.fxml";
	
	private static final Controller frontendController = new Controller();
	
	@FXML
    private Label loginStatusLabel;
	
    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;
	
	@FXML
	private Label logoutSuccessLabel;
	
	@FXML
	private Button loginButton;
	
	private void activateWindow(String FXML_path, String title, Event event)
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
		}
	}
	
    @FXML
    void login(Event event)
    {
    	String username = usernameInput.getText();
    	String password = passwordInput.getText();
    	
    	
    	Task<LoginReply> tempTask = new Task<LoginReply>()
		{
			@Override
			protected LoginReply call() throws Exception
			{
				return frontendController.login(username, password);
			}
		};
		
		tempTask.setOnRunning((runEvent)->
		{
			loginButton.setDisable(true);
		});
		
		tempTask.setOnFailed((failEvent)->
		{
			loginStatusLabel.setText("An error has occured.");
			loginStatusLabel.setTextFill(Color.RED);
			loginButton.setDisable(false);
		});
		
		tempTask.setOnSucceeded((successEvent)->
		{
			LoginReply reply = tempTask.getValue();
			//executor.submit(tempTask);
			
			
	    	//LoginReply reply = frontendController.login(username, password);
			
	    	
	    	/*if(reply == null)
	    	{
	    		loginStatusLabel.setText("An error has occured.");
				loginStatusLabel.setTextFill(Color.RED);
				return;
	    	}*/
			
	    	if(reply.getCodeResponse().getCode() != 200)
	    	{
	    		loginStatusLabel.setText(reply.getCodeResponse().getMessage());
				loginStatusLabel.setTextFill(Color.RED);
				return;
	    	}
	    	
	    	loginStatusLabel.setText("Login successful");
	    	loginStatusLabel.setTextFill(Color.GREEN);
	    	
	    	// add pause for the message
	    	try
			{
				Thread.sleep(500);
			} catch (InterruptedException e){}
	    	
	    	// show the new window
	    	int trainstationID = frontendController.getTrainstationID();
	    	
	    	 //activateWindow(String FXML_path, String title, Event event)
	    	activateWindow(employeePanelFXMLPath, "Station: " + trainstationID, event);
	    	/*try
			{
				Parent newRoot = FXMLLoader.load(Paths.get(employeePanelFXMLPath).toUri().toURL());
				Stage newStage = new Stage();
				newStage.setTitle("Station: " + trainstationID);
				newStage.setScene(new Scene(newRoot));
				newStage.show();
				
				 ((Node)(event.getSource())).getScene().getWindow().hide();
			} catch (IOException e)
			{
				log.warning(e.getMessage());
			}*/
		});
		
		executor.submit(tempTask);
    }

    @FXML
    void logout(Event event)
    {
    	Task<Code_response> tempTask = new Task<Code_response>()
    	{
			@Override
			protected Code_response call() throws Exception
			{
				return frontendController.logout();
			}
		};
		
		tempTask.setOnFailed((failedEvent)->
		{
			Code_response response = tempTask.getValue();
			logoutSuccessLabel.setText(response.getMessage());
			logoutSuccessLabel.setTextFill(Color.RED);
		});
		
		tempTask.setOnSucceeded((successEvent)->
		{
			Code_response response = tempTask.getValue();
			
			if(response.getCode() != 200)
			{
				logoutSuccessLabel.setText(response.getMessage());
				logoutSuccessLabel.setTextFill(Color.RED);
				return;
			}
			
			try
			{
				Thread.sleep(500);
			} 
			catch (InterruptedException e){}
			
			logoutSuccessLabel.setText(response.getMessage());
			logoutSuccessLabel.setTextFill(Color.GREEN);
			
			activateWindow(loginPanelFXMLPath, "Login", event);
		});
		
		executor.submit(tempTask);
    }
}
