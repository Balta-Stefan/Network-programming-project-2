package mdp2021.backend.services.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bouncycastle.util.Arrays;

public class MulticastSocketService extends Thread
{
	private static final Logger log = Logger.getLogger(MulticastSocketService.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/MulticastSocketService.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private final int port;
	private final MulticastSocket mcSocket;
	private boolean run = true;
	private final int bufferSize;
	private final InetAddress address;
	
	private List<Byte[]> data = new ArrayList<>();
	
	public void stopService()
	{
		run = false;
	}
	
	public List<Byte[]> getData()
	{
		synchronized(data)
		{
			List<Byte[]> newList = new ArrayList<>(data);
			data.clear();
			return newList;
		}
	}
	
	public boolean sendData(byte[] data)
	{
		try
		{
			DatagramPacket dgramPacket = new DatagramPacket(data, data.length, address, port);
			mcSocket.send(dgramPacket);
		}
		catch(Exception e)
		{
			log.warning(e.getMessage());
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public MulticastSocketService(int port, String multicastAddress, int bufferSize) throws IOException
	{
		this.port = port;
		this.bufferSize = bufferSize;
		
		mcSocket = new MulticastSocket(port);
		address = InetAddress.getByName(multicastAddress);
		mcSocket.joinGroup(address);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run()
	{
		byte[] buffer = new byte[bufferSize];
		
		while(run)
		{
			Arrays.clear(buffer);
			try
			{
				DatagramPacket dgramPacket = new DatagramPacket(buffer, bufferSize);
				mcSocket.receive(dgramPacket);
				
				Byte[] copy = new Byte[bufferSize];
				for(int i = 0; i < buffer.length; i++)
					copy[i] = buffer[i];
				synchronized(data)
				{
					data.add(copy);
				}
			}
			catch(Exception e)
			{
				log.warning(e.getMessage());
				try
				{
					mcSocket.leaveGroup(address);
				} 
				catch (IOException e1)
				{
					log.warning(e1.getMessage());
				}
				mcSocket.close();
				return;
			}
		}
	}
}
