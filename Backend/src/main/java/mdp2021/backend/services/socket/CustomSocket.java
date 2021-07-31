package mdp2021.backend.services.socket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


// closing socket streams also closes the socket
public class CustomSocket
{
	private static final Logger log = Logger.getLogger(CustomSocket.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/CustomSocket.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private final Socket socket;
	public static final int max_receive_size = 32 * 1024 * 1024;
	
	public CustomSocket(Socket socket)
	{
		this.socket = socket;
	}
	
	public void close() throws IOException
	{
		socket.close();
	}
	
	public Optional<Object> receive()
	{
		byte[] objectStream = null;
		
		try
		{
			InputStream is = socket.getInputStream();
			int dataLength = 0x0;
			byte[] dataLengthBytes = is.readNBytes(4);
			
			dataLength |= ((int)(dataLengthBytes[0])) & 0xFF;
			dataLength |= (((int)dataLengthBytes[1]) << 8) & 0xFF00;
			dataLength |= (((int)dataLengthBytes[2]) << 16) & 0xFF0000;
			dataLength |= (((int)dataLengthBytes[3]) << 24) & 0xFF000000;
			
			if(dataLength > max_receive_size)
				return Optional.empty();
			
			objectStream = is.readNBytes(dataLength);
		}
		catch (IOException e1)
		{
			log.info(e1.getMessage());
			return Optional.empty();
		}
		
		try
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(objectStream);
		    ObjectInputStream in = new ObjectInputStream(bis);
			return Optional.of(in.readObject());
		}
		catch (Exception e)
		{
			log.info(e.getMessage());
			return Optional.empty();
		}
	}
	
	public boolean isClosed()
	{
		return socket.isClosed() || socket.isOutputShutdown() || socket.isInputShutdown();
	}
	
	public boolean send(Object object)
	{
		// send data length
		byte[] data = null;
		
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.flush();
			data = bos.toByteArray();
		}
		catch (IOException e1)
		{
			log.info(e1.getMessage());
			return false;
		}
		
		try
		{
			
			OutputStream os = socket.getOutputStream();
			byte[] lengthStream = new byte[4];
			
			int dataLength = data.length;
			lengthStream[0] = (byte)dataLength;
			lengthStream[1] = (byte)(dataLength >> 8);
			lengthStream[2] = (byte)(dataLength >> 16);
			lengthStream[3] = (byte)(dataLength >> 24);
			
			System.out.println("Sending data over socket with length: " + dataLength + ", and bytes: ");
			for(byte b : lengthStream)
				System.out.println(b);
			
			os.write(lengthStream);
			os.write(data);
			
		}
		catch (IOException e)
		{
			log.info(e.getMessage());
			return false;
		}
		
		return true;
	}
}
