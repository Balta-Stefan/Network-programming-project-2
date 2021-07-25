package mdp2021.backend.model;

import java.util.List;

public class TrainLine
{
	public final int lineID;
	public final String line;
	public final List<StationArrival> stationArrivals;
	
	
	public TrainLine(int lineID, String line, List<StationArrival> stationArrivals)
	{
		this.lineID = lineID;
		this.line = line;
		this.stationArrivals = stationArrivals;
	}

	@Override
	public String toString()
	{
		return "TrainLine [lineID=" + lineID + ", line=" + line + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((line == null) ? 0 : line.hashCode());
		result = prime * result + lineID;
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
		TrainLine other = (TrainLine) obj;
		if (line == null)
		{
			if (other.line != null)
				return false;
		} else if (!line.equals(other.line))
			return false;
		if (lineID != other.lineID)
			return false;
		return true;
	}
	
}
