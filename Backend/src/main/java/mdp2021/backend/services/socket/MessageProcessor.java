package mdp2021.backend.services.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

import mdp2021.backend.model.User;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.FileOrTextMessage;
import mdp2021.backend.shared.Message;
import mdp2021.backend.shared.SubscribeRequest;
import mdp2021.backend.utilities.REDIS_UserSessions;
import mdp2021.backend.utilities.SubscribersContainer;
import mdp2021.backend.utilities.UserSessions;

public class MessageProcessor extends Thread
{
	private Socket socket;
	private static int sessionDurationSeconds;
	//public static SubscribersContainer subscribers;
	//public static UserSessions userSessions;
	
	public MessageProcessor(Socket socket)
	{
		this.socket = socket;
	}
	
	private void handleFileOrTextMessage(User user, CustomSocket senderCustomSocket, FileOrTextMessage message)
	{
		// send the message and status of the sending
		Optional<Socket> receiverSocket = SubscribersContainer.getReceiver(message.receiver_username);
		if(receiverSocket.isEmpty())
		{
			Code_response response = new Code_response(406, "Receiver is not online.");
			senderCustomSocket.send(response);
		}
		else
		{
			Socket receiver = receiverSocket.get();
			synchronized(receiver)
			{
				// synchronization on the socket is necessary to avoid problems with sending multiple messages at the same time
				
				CustomSocket receiverCS = new CustomSocket(receiver);
				
				// message.sender = user.getUsername();
				
				receiverCS.send(message);
			}
		}
		
		try
		{
			socket.close();
		} catch (IOException e){}
	}
	
	private void handleSubscriptionRequest(User subscriber, CustomSocket cs, SubscribeRequest request)
	{
		// subscribe the user and send him the status message
		String message = "";
		
		if(request.type.equals(SubscribeRequest.Type.SUBSCRIBE))
		{
			SubscribersContainer.subscribe(subscriber.getUsername(), socket);
			message = "Subscribed";
		}
		else
		{
			SubscribersContainer.unsubscribe(subscriber.getUsername());
			message = "Unsubscribed";
		}
		
		Code_response response = new Code_response(200, message);
		cs.send(response);
		
		return;
	}
	
	public static void initialize(int sessionDurationSeconds)
	{
		MessageProcessor.sessionDurationSeconds = sessionDurationSeconds;
	}
	
	@Override
	public void run()
	{
		CustomSocket cs = new CustomSocket(socket);
		
		try
		{
			UserSessions userSessions = new REDIS_UserSessions(sessionDurationSeconds);
			
			Optional<Object> receivedObject = cs.receive();
			
			if(receivedObject.isEmpty())
				throw new Exception("Error");
			
			Object received = receivedObject.get();
			if(received instanceof Message == false)
				throw new Exception("Sent data is not of proper format.");
			
			Message message = (Message)received;
			Optional<User> user = userSessions.getUser(message.sender);
			if(user.isEmpty())
			{
				Code_response response = new Code_response(403, "You are not logged in.");
				cs.send(response);
				try
				{
					socket.close();
				} catch (IOException e){}
				return;
			}
			
			if(received instanceof FileOrTextMessage)
			{
				handleFileOrTextMessage(user.get(), cs, (FileOrTextMessage)received);
			}
			else if(received instanceof SubscribeRequest)
			{
				handleSubscriptionRequest(user.get(), cs, (SubscribeRequest)message);
			}
			
			throw new Exception("Sent data is not of proper format.");
		}
		catch(Exception e)
		{
			Code_response response = new Code_response(500, e.getMessage());
			cs.send(response);
		}
		
	}
}
