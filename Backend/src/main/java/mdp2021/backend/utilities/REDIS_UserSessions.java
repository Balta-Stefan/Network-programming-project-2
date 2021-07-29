package mdp2021.backend.utilities;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import redis.clients.jedis.Jedis;

public class REDIS_UserSessions implements UserSessions
{
	private static final Logger log = Logger.getLogger(REDIS_UserSessions.class.getName());
	
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/REDIS_UserSessions.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private final int sessionDurationSeconds;
	
	public REDIS_UserSessions(int sessionDurationSeconds)
	{
		this.sessionDurationSeconds = sessionDurationSeconds;
	}
	
	public Optional<String> login(User user)
	{		
		Optional<Socket> userSocket = SubscribersContainer.getReceiver(user.getUsername());
		if(userSocket.isPresent())
			return Optional.empty(); // only one session is allowed
		
		String stringCookie = CookieGenerator.generateCookie();
		
		// insert into Redis database
		HashMap<String, String> hmap = new HashMap<>();
		//hmap.put("ID", Integer.toString(user.ID));
		hmap.put("train_station_ID", Integer.toString(user.getTrainStation().getID()));
		//hmap.put("train_station_name", user.trainStation.stationName);
		hmap.put("username", user.getUsername());
		
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			jedis.hmset("user:" + stringCookie, hmap);
			jedis.expire("user:" + stringCookie, sessionDurationSeconds);
		}
		
		return Optional.of(stringCookie);
	}
	
	public Optional<User> getUser(String userCookie)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			Map<String, String> hashmap = jedis.hgetAll("user:" + userCookie);
			/*Set<String> keys = hashmap.keySet();
			System.out.println("Keys:");
			for(String k : keys)
				System.out.println(k);*/
			
			//int userID = Integer.parseInt(hashmap.get("ID"));
			int trainStationID = Integer.parseInt(hashmap.get("train_station_ID"));
			//String trainStationName = hashmap.get("train_station_name");
			String username = hashmap.get("username");
			
			User user = new User(new TrainStation(trainStationID), username, null, null);
			
			return Optional.of(user);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			return Optional.empty();
		}
	}
	
	public boolean logout(String userCookie)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			/*String username = jedis.hget("user:" + userCookie, "username");
			SubscribersContainer.unsubscribe(username);*/
			
			long result = jedis.del("user:" + userCookie);
			
			if(result == 1)
				return true;
		}
		return false;
	}
}
