package mdp2021.backend.GUI;

import javafx.application.Application;

public class Main
{
	public static void main(String[] args)
	{
		Application.launch(GUI_Initializer.class, args[0]); // this line blocks as long as the GUI is open
	}
}
