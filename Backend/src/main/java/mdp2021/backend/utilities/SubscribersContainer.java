package mdp2021.backend.utilities;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
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
		System.out.println("SubscribersContainer static block");
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
	
	private static final HashMap<String, Socket> users = new HashMap<>();
	
	public static void subscribe(String username, Socket socket)
	{
		synchronized(users)
		{
			Set<String> keys = users.keySet();
			Socket tmp = users.putIfAbsent(username, socket);
			Socket temp2 = users.get(username);
			keys = users.keySet();
			int a = 3;
		}
		Set<String> keys = users.keySet();
		int a = 3;
	}
	
	public static void unsubscribe(String username)
	{
		Set<String> keys0 = users.keySet();
		int a = 3;
		synchronized(users)
		{
			Set<String> keys = users.keySet();
			Socket tempCheck = users.get(username);
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
