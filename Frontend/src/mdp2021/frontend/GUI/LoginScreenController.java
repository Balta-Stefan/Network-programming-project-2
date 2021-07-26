package mdp2021.frontend.GUI;

import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mdp2021.backend.shared.LoginReply;

public class LoginScreenController extends BaseFXController
{
	
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
			loginButton.setDisable(false);
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
}
