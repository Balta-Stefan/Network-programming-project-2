package mdp2021.backend.persistence;

import java.io.IOException;
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
	
	private static final List<TrainstationUsers> trainstationUsers = new ArrayList<>();
	
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
						LocalDateTime dateTime = LocalDateTime.parse(dateTimeOfArrival);
						StationArrival arrival = new StationArrival(station, dateTime);
						
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
	
	public LinesOfTrainstation getTrainstationLines(TrainStation trainstation)
	{
		// this needs to be tested, most likely doesn't work
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			// kljuc - station-lines:ID_stanice
			Set<String> setValues = jedis.smembers("station-lines:" + Integer.toString(trainstation.getID()));
			
			// what happens if given key doesn't exist?Will setValues be null or empty?
			if(setValues == null || setValues.isEmpty())
				return null;
			
			List<TrainLine> linesThroughStation = new ArrayList<>();
			
			for(String s : setValues)
			{
				// vrijednosti - line:ID_linije
				Map<String, String> lineData = jedis.hgetAll(s);
				
				String lineRepresentation = lineData.get("representation");
				int lineID = Integer.parseInt(s.substring(s.indexOf(":") + 1));
				List<StationArrival> stationArrivals = new ArrayList<>();
				
				lineData.remove(lineRepresentation);
				Set<String> lineDataKeys = lineData.keySet();
				
				for(String station : lineDataKeys)
				{
					// vrijednosti: station:stationID
					// kljucevi: vrijeme prolaska
					
					int semicolumnIndex = station.indexOf(":");
					
					// the assumption is that all keys are valid
					int stationID = Integer.parseInt(station.substring(semicolumnIndex+1));
					LocalDateTime dateTime = LocalDateTime.parse(lineData.get(station));
					
					StationArrival arrivalData = new StationArrival(new TrainStation(stationID), dateTime);
					stationArrivals.add(arrivalData);
				}
				
				linesThroughStation.add(new TrainLine(lineID, lineRepresentation, stationArrivals));
			}
			
			return new LinesOfTrainstation(trainstation, linesThroughStation);
		}
	}
	
	// setters
	
	public boolean reportTrainPass(TrainPassReport report)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			jedis.hset("line:" + report.trainLine.lineID, "station:" + report.trainstation.getID(), report.datetime.toString());
		}
		return true;
	}
	
	public boolean addTrainStation(TrainStation station)
	{
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			long status = jedis.sadd("stations", Integer.toString(station.getID()));
			if(status == 1)
				return true;
		}
		return false;
	}
	
	public boolean addUserToTrainstation(TrainStation station, User user)
	{
		synchronized(trainstationUsers)
		{
			for(TrainstationUsers t : trainstationUsers)
			{
				if(station.equals(t.trainStation) == false)
					continue;
				t.addUser(user);
				return true;
			}
			
			TrainstationUsers newStation = new TrainstationUsers(station);
			newStation.addUser(user);
			trainstationUsers.add(newStation);
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
				String key = hmap.get(s);
				try
				{
					String stationID = key.substring(8); // format is: station:someNumber
					
					
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
	}
	
	public boolean addLine(TrainLine line)
	{
		int i = 0;
		
		try(Jedis jedis = REDIS_CustomPool.getConnection())
		{
			jedis.hset("line:"+line.lineID, "representation", line.line);
			
			List<StationArrival> arrivals = line.stationArrivals;
			for(; i < arrivals.size() - 1; i++)
			{
				StationArrival arrival = arrivals.get(i);
				
				jedis.hset("line:"+line.lineID, "station:"+arrival.trainStation.getID(), arrival.timeOfArrival.toString());
				jedis.sadd("station-lines:"+arrival.trainStation.getID(), "line:"+line.lineID);
			}
			StationArrival arrival = arrivals.get(i);
			
			jedis.hset("line:"+line.lineID, "station:"+arrival.trainStation.getID(), arrival.timeOfArrival.toString());
			jedis.sadd("station-lines:"+arrival.trainStation.getID(), "line:"+line.lineID);
		}
		return true;
	}
}