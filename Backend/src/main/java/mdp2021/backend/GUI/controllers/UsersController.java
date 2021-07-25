package mdp2021.backend.GUI.controllers;

import mdp2021.backend.model.User;
import mdp2021.backend.persistence.IUserDAO;
import mdp2021.backend.persistence.XML_UserDAO;
import mdp2021.backend.utilities.BCrypt_hasher;
import mdp2021.backend.utilities.PasswordHasher;

public class UsersController
{
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
			return false;
		}
	}
	
	public static boolean removeUser(User user)
	{
		return userPersistence.removeUser(user);
	}
}
