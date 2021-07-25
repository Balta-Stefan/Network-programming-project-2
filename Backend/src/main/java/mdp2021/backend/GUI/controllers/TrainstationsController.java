package mdp2021.backend.GUI.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.persistence.ITrainstationPersistence;
import mdp2021.backend.persistence.REDIS_TrainstationPersistence;

public class TrainstationsController
{
	private static final Logger log = Logger.getLogger(TrainstationsController.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/TrainstationsController.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static HashMap<TrainStation, TrainStation> trainStations = new HashMap<>();

	
	private static AtomicInteger availableLineID = new AtomicInteger(1);
	
	
	public static boolean addTrainstation(TrainStation station)
	{
		if(trainStations.containsKey(station))
			return false;

		trainStations.put(station, station);
		
		return true;
	}
	
	public static boolean removeTrainstation(TrainStation station)
	{
		if(trainStations.remove(station) == null)
			return false;
		
		return true;
	}
	
	public static Optional<TrainLine> addLinesToTrainstation(List<StationArrival> lines)
	{
		try
		{
			ITrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();
			
			int lineID = availableLineID.getAndIncrement();
			
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for(; i < lines.size()-1; i++)
				builder.append(lines.get(i).trainStation.getID() + "-");
			builder.append(lines.get(i).trainStation.getID());
			
			TrainLine line = new TrainLine(lineID, builder.toString(), lines);
			boolean status = trainstationPersistence.addLine(line);
			
			if(status == true)
				return  Optional.of(line);
			return Optional.empty();
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			return Optional.empty();
		}
	
		/*try(Jedis jedis = pool.getResource())
		{
			StringBuilder builder = new StringBuilder();
			
			int i = 0;
			for(; i < lines.size() - 1; i++)
			{
				StationArrival arrival = lines.get(i);
				builder.append(arrival.trainStation.stationID + "-");
				
				jedis.hset("line:"+lineID, "station:"+arrival.trainStation.stationID, arrival.timeOfArrival.toString());
				jedis.sadd("station-lines:"+arrival.trainStation.stationID, "line:"+lineID);
			}
			StationArrival arrival = lines.get(i);
			builder.append(arrival.trainStation.stationID);
			
			jedis.hset("line:"+lineID, "station:"+arrival.trainStation.stationID, arrival.timeOfArrival.toString());
			jedis.sadd("station-lines:"+arrival.trainStation.stationID, "line:"+lineID);
			
			jedis.hset("line:"+lineID, "representation", arrival.toString());
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;*/
	}

	public static boolean removeTrainLine(TrainLine line)
	{
		ITrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();
		
		return trainstationPersistence.removeLine(line);
	
	}
}
