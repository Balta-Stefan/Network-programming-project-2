package mdp2021.backend.utilities;

import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;



// used to map usernames to their sockets that are used for message notifications

public final class SubscribersContainer
{
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
			} catch (Exception e) {}
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
