package mdp2021.backend.model;

import java.time.LocalDateTime;

public class TrainPassReport
{
	public TrainStation trainstation;
	public TrainLine trainLine;
	//public final int userID; // the user that made this report
	public LocalDateTime datetime;
	
	public TrainPassReport() {}
	
	public TrainPassReport(TrainStation trainstation, TrainLine trainLine, LocalDateTime datetime)
	{
		this.trainstation = trainstation;
		this.trainLine = trainLine;
		//this.userID = userID;
		this.datetime = datetime;
	}	
}
