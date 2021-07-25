package mdp2021.backend.GUI.controllers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.model.User;
import mdp2021.backend.persistence.IUserDAO;
import mdp2021.backend.persistence.XML_UserDAO;
import mdp2021.backend.utilities.BCrypt_hasher;
import mdp2021.backend.utilities.PasswordHasher;

public class UsersController
{
	private static final Logger log = Logger.getLogger(UsersController.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/UsersController.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static final IUserDAO userPersistence = new XML_UserDAO(".\\Application data\\Users\\");
	private static final PasswordHasher hasher = new BCrypt_hasher();
	
	public static boolean register(User user)
	{
		byte[] salt = hasher.getSalt();
		user.setSalt(salt);
		try
		{
			String hash = hasher.hash(salt, user.getPassword());
			user.setPassword(hash);
			
			boolean registrationStatus = userPersistence.addUser(user);
			return registrationStatus;
		}
		catch (Exception e)
		{
			log.info(e.getMessage());
			return false;
		}
	}
	
	public static boolean removeUser(User user)
	{
		return userPersistence.removeUser(user);
	}
}
