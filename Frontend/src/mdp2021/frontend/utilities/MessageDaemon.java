package mdp2021.frontend.utilities;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.services.socket.CustomSocket;
import mdp2021.backend.shared.FileOrTextMessage;
import mdp2021.frontend.GUI.EmployeePanelController;

public class MessageDaemon extends Thread
{
	private CustomSocket socket;
	private EmployeePanelController employeePanel;
	private static final Logger log = Logger.getLogger(MessageDaemon.class.getName());
	
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/MessageDaemon.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void stopDaemon() throws IOException
	{
		socket.close();
	}
	
	public void setPanel(EmployeePanelController employeePanel)
	{
		this.employeePanel = employeePanel;
	}
	
	public MessageDaemon(CustomSocket socket)
	{
		this.socket = socket;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				Optional<Object> object = socket.receive();

				if(socket.isClosed())
					return;
				
				if(object.isEmpty() || object.get() instanceof FileOrTextMessage == false)
					continue;
				
				FileOrTextMessage message = (FileOrTextMessage)object.get();
				employeePanel.receiveMessage(message);
			}
			catch(Exception e)
			{
				log.warning(e.getMessage());
				return;
			}
		}
	}
}
