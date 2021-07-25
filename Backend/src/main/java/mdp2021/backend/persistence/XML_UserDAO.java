package mdp2021.backend.persistence;


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.model.User;

public class XML_UserDAO implements IUserDAO
{
	private static final Logger log = Logger.getLogger(XML_UserDAO.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/XML_UserDAO.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private final String pathPrefix;
	
	public XML_UserDAO(String pathPrefix)
	{
		this.pathPrefix = pathPrefix;
	}
	
	public Optional<User> getUser(String username)
	{
		String filename = pathPrefix + username + ".xml";
		File file = new File(filename);
		
		if(file.exists() == false)
			return Optional.empty();
		
		try(FileInputStream fis = new FileInputStream(file);
			XMLDecoder decoder = new XMLDecoder(fis))
		{
			User user = (User)(decoder.readObject());
			return Optional.of(user);
		}
		catch (Exception e)
		{
			return Optional.empty();
		}
	}
	
	public boolean addUser(User user)
	{
		String filename = pathPrefix + user.getUsername() + ".xml";
		File file = new File(filename);
		
		try
		{
			if(file.createNewFile() == false) // if file already exists, stop
				return false;
			
			try(XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file)))
			{
				encoder.writeObject(user);
			}
		}
		catch(Exception e) {return false;}
		
		return true;
	}
	
	public boolean removeUser(User user)
	{
		String filename = pathPrefix + user.getUsername() + ".xml";
		File file = new File(filename);
		
		if(file.exists() == false)
			return false;
		
		return file.delete();
	}
	
	/*public static void main(String[] args)
	{
		User user = new User(1, "Marko markovic", "abcd", 5);
		System.out.println("Real user is: ");
		System.out.println(user);
		System.out.println("\n\n\n");
		
		XML_UserDAO tmp = new XML_UserDAO(".");
		tmp.addUser(user);
		
		Optional<User> deserialized = tmp.getUser("Marko Markovic");
		if(deserialized.isEmpty())
		{
			System.out.println("is empty");
			return;
		}
		User usr = deserialized.get();
		
		System.out.println("Deserialized: ");
		System.out.println(deserialized.get());
	}*/
}
