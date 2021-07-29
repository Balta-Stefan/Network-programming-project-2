package mdp2021.backend.services.SOAP;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

import mdp2021.backend.model.TrainstationUsers;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.ITrainstationPersistence;
import mdp2021.backend.persistence.IUserDAO;
import mdp2021.backend.persistence.REDIS_TrainstationPersistence;
import mdp2021.backend.persistence.XML_UserDAO;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.LoginReply;
import mdp2021.backend.utilities.BCrypt_hasher;
import mdp2021.backend.utilities.REDIS_UserSessions;
import mdp2021.backend.utilities.UserSessions;


public final class SOAP_service
{
	//private static final String propertiesPath = ".\\Resources\\backend constants.properties";
	//private static final String userSessionDurationProperty = "userSessionDuration";
	private static final Logger log = Logger.getLogger(SOAP_service.class.getName());
	private static final String pathPrefix = "D:\\Knjige za fakultet\\3. godina\\6. semestar\\Mrezno i distribuirano programiranje\\Projekat\\Source 2\\Backend";
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/SOAP_service.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static final int sessionDurationSeconds = 7200;
	private static final IUserDAO userDatabase = new XML_UserDAO(pathPrefix + "\\Application data\\Users\\");
	

	/*static
	{
		// ne radi jer je User.dir u System32 pa relativne putanje ne rade
		System.out.println("Staticki blok");
		Properties backendProperties = new Properties();
		
		System.out.println(System.getProperty("user.dir"));
		try(FileInputStream fis = new FileInputStream(new File(propertiesPath)))
		{
			backendProperties.load(fis);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			System.out.println("Izuzetak je: " + e.getMessage());
			//e.printStackTrace();
		}
		
		String durationProperty = backendProperties.getProperty(userSessionDurationProperty);
		System.out.println(durationProperty);
		sessionDurationSeconds = Integer.parseInt(durationProperty);
	}*/
	
	public LoginReply login(String username, String password)
	{
		Optional<User> user = userDatabase.getUser(username);
		if(user.isEmpty())
			return new LoginReply(new Code_response(403, "Username not found."), null, null);
			//return new Code_response(403, "Username not found");
		
		User userInfo = user.get();
		BCrypt_hasher hasher = new BCrypt_hasher();
	
		try
		{
			String hashedPass = hasher.hash(userInfo.getSalt(), password);
			if(hashedPass.equals(userInfo.getPassword()) == false)
				return new LoginReply(new Code_response(401, "Incorrect password"), null, null);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			return new LoginReply(new Code_response(500, "Error."), null, null);
		}
		
		
		UserSessions sessions = new REDIS_UserSessions(sessionDurationSeconds);
		Optional<String> cookieOpt = sessions.login(userInfo);
		if(cookieOpt.isEmpty())
			return new LoginReply(new Code_response(400, "You are already logged in."), null, null);
		
		return new LoginReply(new Code_response(200, "Success"), userInfo.getTrainStation(), cookieOpt.get());
	}
	
	public Code_response logout(String cookie)
	{
		boolean status = false;

		UserSessions sessions = new REDIS_UserSessions(sessionDurationSeconds);
		status = sessions.logout(cookie);
		
		
		if(status == false)
			return new Code_response(500, "Error");
		
		return new Code_response(200, "Logout successful");
	}
	
	public String getTrainstationUsers(String cookie)
	{
		// the result may be null
		Optional<User> user = Optional.empty();
	
		UserSessions sessions = new REDIS_UserSessions(sessionDurationSeconds);
		user = sessions.getUser(cookie);
		
		if(user.isEmpty())
			return "";
		
		ITrainstationPersistence trainstationData = new REDIS_TrainstationPersistence();
		
		Gson gson = new Gson();
		List<TrainstationUsers> trainstationUsers = trainstationData.getTrainstationUsers();
		
		return gson.toJson(trainstationUsers);
		
	}
}
