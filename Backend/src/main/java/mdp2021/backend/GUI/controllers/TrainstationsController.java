package mdp2021.backend.GUI.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.model.LinesOfTrainstation;
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
	
	private static final ITrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();

	
	public static Optional<List<TrainStation>> getTrainStations()
	{
		return trainstationPersistence.getTrainStations();
	}
	
	public static boolean addTrainstation(TrainStation station)
	{
		return trainstationPersistence.addTrainStation(station);
	}
	
	public static Optional<Set<TrainLine>> getTrainLines()
	{
		Optional<List<TrainStation>> trainStations = trainstationPersistence.getTrainStations();
		if(trainStations.isEmpty())
			return Optional.empty();
		
		Set<TrainLine> trainLines = new HashSet<>();
		
		for(TrainStation t : trainStations.get())
		{
			Optional<LinesOfTrainstation> tempLineData = trainstationPersistence.getTrainstationLines(t);
			if(tempLineData.isEmpty())
				continue;
			
			List<TrainLine> tempLines = tempLineData.get().linesThroughStation;
			trainLines.addAll(tempLines);
		}
		
		return Optional.of(trainLines);
	}
	
	public static boolean removeTrainstation(TrainStation station)
	{
		return trainstationPersistence.removeStation(station);
	}
	
	public static Optional<TrainLine> addLinesToTrainstation(List<StationArrival> lines)
	{
		try
		{
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for(; i < lines.size()-1; i++)
				builder.append(lines.get(i).trainStation.getID() + "-");
			builder.append(lines.get(i).trainStation.getID());
			
			TrainLine line = new TrainLine(0, builder.toString(), lines);
			TrainLine newLine = trainstationPersistence.addLine(line);
			
			return  Optional.of(newLine);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			return Optional.empty();
		}
	}

	public static boolean removeTrainLine(TrainLine line)
	{
		return trainstationPersistence.removeLine(line);
	}
}
