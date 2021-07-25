package mdp2021.backend.model;

public class TrainStation
{
	private int stationID;
	//public final String stationName;
	
	public TrainStation() {}
	
	public TrainStation(int stationID)
	{
		this.stationID = stationID;
	}
	
	public int getID()
	{
		return stationID;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + stationID;
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
		TrainStation other = (TrainStation) obj;
		if (stationID != other.stationID)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "TrainStation [stationID=" + stationID + "]";
	}
	
	
}
