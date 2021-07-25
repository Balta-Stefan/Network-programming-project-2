package mdp2021.backend.GUI;

import java.nio.file.Paths;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI_Initializer extends Application
{
	/*public static void main(String[] args)
	{
		launch(args);
	}*/

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		// TODO Auto-generated method stub
		
		String fxmlPath = ".\\Resources\\MDP2021 GUI.fxml";
		
		Parent root = FXMLLoader.load(Paths.get(fxmlPath).toUri().toURL());
		primaryStage.setTitle("MDP2021 (Balta Stefan)");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
}
