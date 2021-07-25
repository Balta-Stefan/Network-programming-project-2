package mdp2021.backend.model;

import java.util.List;

public class LinesOfTrainstation
{
	public final TrainStation trainStation;
	public final List<TrainLine> linesThroughStation;
	
	
	public LinesOfTrainstation(TrainStation trainStation, List<TrainLine> linesThroughStation)
	{
		this.trainStation = trainStation;
		this.linesThroughStation = linesThroughStation;
	}

	@Override
	public String toString()
	{
		return "LinesOfTrainstation [trainStation=" + trainStation + ", linesThroughStation=" + linesThroughStation
				+ "]";
	}
}
