package mdp2021.backend.services.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;



public class Socket_service extends Thread
{
	public boolean run = true;
	
	private ServerSocket serverSocket;
	
	
	public Socket_service(String host, int port, String KEY_STORE_PATH, String KEY_STORE_PASSWORD) throws IOException
	{
		System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
		System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);
		
		SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		serverSocket = ssf.createServerSocket(port);
	}

	@Override
	public void run()
	{
		System.out.println("Socket service started.");
		
		while(true)
		{
			try
			{
				Socket clientSocket = serverSocket.accept();
				new MessageProcessor(clientSocket).start();
			}
			catch (IOException e) {}
		}
	}
}
