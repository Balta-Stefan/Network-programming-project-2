package mdp2021.backend.utilities;

import mdp2021.backend.GUI.GUI_JavaFX_Controller;
import mdp2021.backend.services.socket.MulticastSocketService;

public class MulticastServiceGUIUpdater extends Thread
{
	private final MulticastSocketService multicastService;
	private final GUI_JavaFX_Controller interfaceToUpdate;
	
	private boolean run = true;
	
	public MulticastServiceGUIUpdater(MulticastSocketService multicastService, GUI_JavaFX_Controller interfaceToUpdate)
	{
		this.multicastService = multicastService;
		this.interfaceToUpdate = interfaceToUpdate;
	}
	
	public void stopService()
	{
		run = false;
		multicastService.stopService();
	}
	
	
	@Override
	public void run()
	{
		while(run)
		{
			
		}
	}
}
