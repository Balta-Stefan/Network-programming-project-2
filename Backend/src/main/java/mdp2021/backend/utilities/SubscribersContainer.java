package mdp2021.backend.utilities;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



// used to map usernames to their sockets that are used for message notifications

public final class SubscribersContainer
{
	private static final Logger log = Logger.getLogger(SubscribersContainer.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/SubscribersContainer.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static HashMap<String, Socket> users = new HashMap<>();
	
	public static void subscribe(String username, Socket socket)
	{
		synchronized(users)
		{
			users.putIfAbsent(username, socket);
		}
	}
	
	public static void unsubscribe(String username)
	{
		synchronized(users)
		{
			Socket socket = users.remove(username);
			try
			{
				socket.close();
			} catch (Exception e) 
			{
				log.info(e.getMessage());
			}
		}
	}
	
	public static Optional<Socket> getReceiver(String receiver)
	{
		synchronized(users)
		{
			Socket socket = users.get(receiver);
			return Optional.ofNullable(socket);
		}
	}
}
