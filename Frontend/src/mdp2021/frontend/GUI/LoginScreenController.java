package mdp2021.frontend.GUI;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mdp2021.backend.shared.Code_response;

public class LoginScreenController extends BaseFXController
{
	private static final Logger log = Logger.getLogger(LoginScreenController.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/LoginScreenController.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private Frontend_GUI_Initializer applicationObject;
	
	public void setApplicationObject(Frontend_GUI_Initializer applicationObject)
	{
		this.applicationObject = applicationObject;
	}
	
	@FXML
    private Label loginStatusLabel;

	
    @FXML
    private TextField usernameInput;

    @FXML
    private PasswordField passwordInput;
	
	@FXML
	private Button loginButton;
	

    @FXML
    void login(Event event)
    {
    	String username = usernameInput.getText();
    	String password = passwordInput.getText();
    	
    	
    	Task<Optional<Code_response>> tempTask = new Task<Optional<Code_response>>()
		{
			@Override
			protected Optional<Code_response> call() throws Exception
			{
				return applicationObject.login(username, password);
				//return frontendController.login(username, password);
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
			loginButton.setDisable(false);
			Optional<Code_response> replyObj = tempTask.getValue();
			
			if(replyObj.isEmpty())
			{
				loginStatusLabel.setText("An error has occured");
				loginStatusLabel.setTextFill(Color.RED);
				return;
			}
			
			Code_response reply = replyObj.get();
			
	    	if(reply.getCode() != 200)
	    	{
	    		loginStatusLabel.setText(reply.getMessage());
				loginStatusLabel.setTextFill(Color.RED);
				return;
	    	}
	    	
	    	applicationObject.activateEmployeePanel();
	    	//activateWindow(employeePanelFXMLPath, "Station: " + trainstationID, event);
	    	
	    	/*try
			{
				FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Resources/employee panel.fxml"));     
				Parent newRoot = fxmlLoader.load(); 
				
				EmployeePanelController employeePanelController = fxmlLoader.<EmployeePanelController>getController();
				employeePanelController.setController(frontendController);
				
				Stage newStage = new Stage();
				newStage.setTitle("Station: " + trainstationID);
				newStage.setScene(new Scene(newRoot));
				newStage.show();
				
				((Node)(event.getSource())).getScene().getWindow().hide();
			}
	    	catch (IOException e)
			{
				log.warning(e.getMessage());
				e.printStackTrace();
			}*/
		});
		
		
		executor.submit(tempTask);
    }

}
