package mdp2021.backend.model;

import java.time.LocalDateTime;

public class StationArrival
{
	public TrainStation trainStation;
	public LocalDateTime timeOfArrival;
	public boolean passed;
	
	public StationArrival() {}
	
	public StationArrival(TrainStation trainStation, LocalDateTime timeOfArrival, boolean passed)
	{
		this.trainStation = trainStation;
		this.timeOfArrival = timeOfArrival;
		this.passed = passed;
	}

	@Override
	public String toString()
	{
		String suffix = (passed == true) ? "Passed" : "Hasn't passed";
		return "[train station=" + trainStation + ", timeOfArrival=" + timeOfArrival + ", " + suffix + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((timeOfArrival == null) ? 0 : timeOfArrival.hashCode());
		result = prime * result + ((trainStation == null) ? 0 : trainStation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StationArrival other = (StationArrival) obj;
		if (timeOfArrival == null)
		{
			if (other.timeOfArrival != null)
				return false;
		} else if (!timeOfArrival.equals(other.timeOfArrival))
			return false;
		if (trainStation == null)
		{
			if (other.trainStation != null)
				return false;
		} else if (!trainStation.equals(other.trainStation))
			return false;
		return true;
	}
	
	
}
