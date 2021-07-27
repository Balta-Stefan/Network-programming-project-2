package mdp2021.backend.persistence;

import java.util.List;
import java.util.Optional;

import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainPassReport;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.TrainstationUsers;
import mdp2021.backend.model.User;

public interface ITrainstationPersistence
{
	// getters
	public List<TrainstationUsers> getTrainstationUsers();
	public Optional<LinesOfTrainstation> getTrainstationLines(TrainStation trainstation);
	public Optional<StationArrival> getArrivalOfLine(TrainStation station, TrainLine line);
	public Optional<List<TrainStation>> getTrainStations();
	
	// setters
	public boolean reportTrainPass(TrainPassReport report);
	public boolean addTrainStation(TrainStation station);
	public boolean addUserToTrainstation(TrainStation station, User user);
	//public boolean addTrainstationLines(LinesOfTrainstation lines); // unused
	public boolean addLine(TrainLine line);
	public boolean removeLine(TrainLine line);
	public boolean removeStation(TrainStation station);
}
