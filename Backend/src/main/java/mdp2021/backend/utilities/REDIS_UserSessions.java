package mdp2021.backend.utilities;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import redis.clients.jedis.Jedis;

public class REDIS_UserSessions implements UserSessions
{
	
	public static final int cookieByteLength = 16;
	
	
	private final int sessionDurationSeconds;
	
	private final SecureRandom randomGenerator = new SecureRandom();
	
	public REDIS_UserSessions(int sessionDurationSeconds)
	{
		this.sessionDurationSeconds = sessionDurationSeconds;
		
	}
	
	public String login(User user)
	{
		byte[] cookieBytes = new byte[cookieByteLength];
		randomGenerator.nextBytes(cookieBytes);
		
		Encoder base64Encoder = Base64.getEncoder();
		String stringCookie = base64Encoder.encodeToString(cookieBytes);
		
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
		
		return stringCookie;
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
			System.out.println(e);
			return Optional.empty();
		}
	}
	
	public boolean logout(String userCookie)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			long result = jedis.del("user:" + userCookie);
			
			if(result == 1)
				return true;
		}
		return false;
	}

}
