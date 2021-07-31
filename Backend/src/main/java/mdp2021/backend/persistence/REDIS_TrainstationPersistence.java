package mdp2021.backend.persistence;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainPassReport;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.TrainstationUsers;
import mdp2021.backend.model.User;
import mdp2021.backend.utilities.REDIS_CustomPool;
import redis.clients.jedis.Jedis;

public class REDIS_TrainstationPersistence implements ITrainstationPersistence
{
	private static final Logger log = Logger.getLogger(REDIS_TrainstationPersistence.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/REDIS_TrainstationPersistence.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static List<TrainstationUsers> trainstationUsers;
	
	static
	{
		// make a list of all trainstation users
		Gson gson = new Gson();
		String usersJSON = null;
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			usersJSON = jedis.get("users_of_stations");
			
			// initialize train line the ID counter if it doesn't exist
			//available_train_line_ID
			
			if(jedis.get("available_train_line_ID") == null)
				jedis.set("available_train_line_ID", "0");
		}
		
		Type trainstationUsersJSON = new TypeToken<List<TrainstationUsers>>(){}.getType();
		
		trainstationUsers = gson.fromJson(usersJSON, trainstationUsersJSON);
		if(trainstationUsers == null)
			trainstationUsers = new ArrayList<TrainstationUsers>();
	}
	
	// getters
	
	public List<TrainstationUsers> getTrainstationUsers()
	{
		synchronized(trainstationUsers)
		{
			return new ArrayList<TrainstationUsers>(trainstationUsers);
		}
	}
	
	public Optional<StationArrival> getArrivalOfLine(TrainStation station, TrainLine line)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			Map<String, String> data = jedis.hgetAll("line:" + line.lineID);
			
			Set<String> keys = data.keySet();
			keys.remove("representation");
			for(String k : keys)
			{
				// key format: station:someID
				try
				{
					String strID = k.substring(8);
					int ID = Integer.parseInt(strID);
					
					if(ID == station.getID())
					{
						String dateTimeOfArrival = data.get(k);
						boolean passed = (dateTimeOfArrival.charAt(0) == 'P') ? true : false;
						
						LocalDateTime dateTime = LocalDateTime.parse(dateTimeOfArrival.substring(2));
						StationArrival arrival = new StationArrival(station, dateTime, passed);
						
						return Optional.of(arrival);
					}
				}
				catch(Exception e)
				{
					log.info(e.getMessage());
				}
			}
			
			return Optional.empty();
		}
	}
	
	public Optional<LinesOfTrainstation> getTrainstationLines(TrainStation trainstation)
	{
		// this needs to be tested, most likely doesn't work
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			// kljuc - station-lines:ID_stanice
			Set<String> setValues = jedis.smembers("station-lines:" + Integer.toString(trainstation.getID()));
			
			// what happens if given key doesn't exist?Will setValues be null or empty?
			if(setValues == null || setValues.isEmpty())
				return Optional.empty();
			
			List<TrainLine> linesThroughStation = new ArrayList<>();
			
			for(String s : setValues)
			{
				// vrijednosti - line:ID_linije
				Map<String, String> lineData = jedis.hgetAll(s);
				
				String lineRepresentation = lineData.get("representation");
				int lineID = Integer.parseInt(s.substring(5)); // extract ID from string line:someID
				List<StationArrival> stationArrivals = new ArrayList<>();
				
				lineData.remove("representation");
				Set<String> lineDataKeys = lineData.keySet();
				
				for(String station : lineDataKeys)
				{
					// vrijednosti: station:stationID
					// kljucevi: vrijeme prolaska
					
					// the assumption is that all keys are valid (are of the form station:stationID)
					int stationID = Integer.parseInt(station.substring(8));
					
					String arrivalInfo = lineData.get(station);
					boolean passed = (arrivalInfo.charAt(0) == 'P') ? true : false;
					
					LocalDateTime dateTime = LocalDateTime.parse(arrivalInfo.substring(2));
					
					StationArrival arrivalData = new StationArrival(new TrainStation(stationID), dateTime, passed);
					stationArrivals.add(arrivalData);
				}
				
				linesThroughStation.add(new TrainLine(lineID, lineRepresentation, stationArrivals));
			}
			
			return Optional.of(new LinesOfTrainstation(trainstation, linesThroughStation));
		}
	}
	
	public Optional<List<TrainStation>> getTrainStations()
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			Gson gson = new Gson();
			
			String stationsJSON = jedis.get("stations_info");
			Type listOfTestObject = new TypeToken<List<TrainStation>>(){}.getType();
			
			List<TrainStation> trainStationsInfo = gson.fromJson(stationsJSON, listOfTestObject);
			
			return Optional.ofNullable(trainStationsInfo);
		}
	}
	
	
	// setters
	
	public boolean reportTrainPass(TrainPassReport report)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			jedis.hset("line:" + report.trainLine.lineID, "station:" + report.trainstation.getID(), "P-" + report.datetime.toString());
		}
		return true;
	}
	
	public boolean addTrainStation(TrainStation station)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			long status = jedis.sadd("stations", Integer.toString(station.getID()));
			if(status != 1)
				return false;
			
			Gson gson = new Gson();
			
			String stationsJSON = jedis.get("stations_info");
			Type listOfTestObject = new TypeToken<List<TrainStation>>(){}.getType();
			
			List<TrainStation> trainStationsInfo = gson.fromJson(stationsJSON, listOfTestObject);
			if(trainStationsInfo == null)
				trainStationsInfo = new ArrayList<TrainStation>();
			
			if(trainStationsInfo.contains(station))
				return false;
			
			trainStationsInfo.add(station);
			
			String serializedList = gson.toJson(trainStationsInfo);
			
			jedis.set("stations_info", serializedList);
		}
		return true;
	}
	
	private boolean checkExists(TrainStation station)
	{
		Set<String> stations = null;
		
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			stations = jedis.smembers("stations");
		}
		String stationID = Integer.toString(station.getID());
		
		return stations.contains(stationID);
	}
	
	public boolean addUserToTrainstation(User user)
	{
		// check whether the station exists
		if(checkExists(user.getTrainStation()) == false)
			return false;
		
		String JSON_list = null;
		boolean status = false;
		
		synchronized(trainstationUsers)
		{
			for(TrainstationUsers t : trainstationUsers)
			{
				if(user.getTrainStation().equals(t.trainStation) == false)
					continue;
				t.addUser(user);
				status = true;
				break;
			}
			
			if(status == false)
			{
				TrainstationUsers newStation = new TrainstationUsers(user.getTrainStation());
				newStation.addUser(user);
				trainstationUsers.add(newStation);	
			}
			Gson gson = new Gson();
			JSON_list = gson.toJson(trainstationUsers);
		}
		
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			jedis.set("users_of_stations", JSON_list);
		}
		
		return true;
	}

	public boolean removeLine(TrainLine line)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			Map<String, String> hmap = jedis.hgetAll("line:" + line.lineID);
			hmap.remove("representation");
			
			Set<String> keys = hmap.keySet();
			for(String s : keys)
			{
				// remove the information that this line passes through stations
				try
				{
					String stationID = s.substring(8); // format is: station:someNumber
					
					jedis.srem("station-lines:" + stationID, "line:" + line.lineID);
				}
				catch(Exception e)
				{
					log.info(e.getMessage());
				}
			}
			
			// remove all the data about this line
			long status = jedis.del("line:" + line.lineID);
			if(status == 1)
				return true;
			
			return false;
		}
	}
	/*
	// unused
	public boolean addTrainstationLines(LinesOfTrainstation lines)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			for(TrainLine line : lines.linesThroughStation)
			{
				jedis.sadd("station-lines:" + lines.trainStation.getID(), Integer.toString(line.lineID));
				
				jedis.hset("line:" + line.lineID, "representation", line.line);
				
				for(StationArrival arrival : line.stationArrivals)
				{
					
					jedis.hset("line:" + line.lineID, "station:" + arrival.trainStation.getID(), arrival.timeOfArrival.toString());
				}
			}
		}
	
		return true;
	}*/
	
	public TrainLine addLine(TrainLine line)
	{
		int i = 0;
		
		// atomically get an ID for the line
		
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			long lineID = jedis.incr("available_train_line_ID");
			line.lineID = (int) lineID;
			
			jedis.hset("line:"+line.lineID, "representation", line.line);
			
			List<StationArrival> arrivals = line.stationArrivals;
			for(; i < arrivals.size() - 1; i++)
			{
				StationArrival arrival = arrivals.get(i);
				
				String toAppend = (arrival.passed == true) ? "P-" : "E-";
				jedis.hset("line:"+lineID, "station:"+arrival.trainStation.getID(), toAppend + arrival.timeOfArrival.toString());
				jedis.sadd("station-lines:" + arrival.trainStation.getID(), "line:"+lineID);
			}
			StationArrival arrival = arrivals.get(i);
			
			String toAppend = (arrival.passed == true) ? "P-" : "E-";
			jedis.hset("line:"+lineID, "station:"+arrival.trainStation.getID(), toAppend + arrival.timeOfArrival.toString());
			jedis.sadd("station-lines:" + arrival.trainStation.getID(), "line:"+lineID);
		}
		return line;
	}

	public boolean removeStation(TrainStation station)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			Gson gson = new Gson();
			
			String stationsJSON = jedis.get("stations_info");
			Type listOfTestObject = new TypeToken<List<TrainStation>>(){}.getType();
			List<TrainStation> trainStationsInfo = gson.fromJson(stationsJSON, listOfTestObject);
			
			if(trainStationsInfo == null)
				return false;
			
			trainStationsInfo.remove(station);
			
			String serializedJSON = gson.toJson(trainStationsInfo);
			jedis.set("stations_info", serializedJSON);
			
			
			// delete the data in all other places for consistency
			Optional<LinesOfTrainstation> lines = getTrainstationLines(station);
			if(lines.isEmpty())
			{
				jedis.del("station-lines:" + station.getID());
				return true;
			}
			
			LinesOfTrainstation linesData = lines.get();
			
			for(TrainLine l : linesData.linesThroughStation)
			{
				jedis.hdel("line:" + l.lineID, "station:" + station.getID());
			}
			
			jedis.del("station-lines:" + station.getID());
		}
		
		return true;
	}
}
