package mdp2021.backend.model;

import java.util.HashSet;
import java.util.Set;

public class TrainstationUsers
{
	public final TrainStation trainStation;
	public final Set<User> users = new HashSet<>();
	
	public TrainstationUsers(TrainStation trainStation)
	{
		this.trainStation = trainStation;
	}
	
	public void addUser(User user)
	{
		users.add(user);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
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
		TrainstationUsers other = (TrainstationUsers) obj;
		if (trainStation == null)
		{
			if (other.trainStation != null)
				return false;
		} else if (!trainStation.equals(other.trainStation))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "TrainstationUsers [trainStation=" + trainStation + ", users=" + users + "]";
	}
	
	
}
