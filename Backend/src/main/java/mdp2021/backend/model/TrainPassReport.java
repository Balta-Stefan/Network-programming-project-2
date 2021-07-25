package mdp2021.backend.model;

import java.time.LocalDateTime;

public class TrainPassReport
{
	public final TrainStation trainstation;
	public final TrainLine trainLine;
	public final int userID; // the user that made this report
	public final LocalDateTime datetime;
	
	
	public TrainPassReport(TrainStation trainstation, TrainLine trainLine, int userID, LocalDateTime datetime)
	{
		this.trainstation = trainstation;
		this.trainLine = trainLine;
		this.userID = userID;
		this.datetime = datetime;
	}	
}
