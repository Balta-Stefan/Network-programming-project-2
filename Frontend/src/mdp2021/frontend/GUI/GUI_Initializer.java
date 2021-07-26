package mdp2021.frontend.GUI;

import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI_Initializer extends Application
{

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		String fxmlPath = ".\\Resources\\login screen.fxml";
		
		Parent root = FXMLLoader.load(Paths.get(fxmlPath).toUri().toURL());
		primaryStage.setTitle("Login");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

}
