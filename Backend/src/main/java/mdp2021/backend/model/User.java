package mdp2021.backend.model;

import java.io.Serializable;

public class User implements Serializable
{
	//public final int ID;
	private TrainStation trainStation;
	private String username;
	private String password;
	private byte[] salt;
	
	public User() {}


	public User(TrainStation trainStation, String username, String password, byte[] salt)
	{
		//this.ID = ID;
		this.trainStation = trainStation;
		this.username = username;
		this.password = password;
		this.salt = salt;
	}
	
	public void setTrainStation(TrainStation trainStation)
	{
		this.trainStation = trainStation;
	}


	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public TrainStation getTrainStation()
	{
		return trainStation;
	}
	
	public byte[] getSalt()
	{
		return salt;
	}

	public void setSalt(byte[] salt)
	{
		this.salt = salt;
	}
	
	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((trainStation == null) ? 0 : trainStation.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
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
		User other = (User) obj;
	
		if (trainStation == null)
		{
			if (other.trainStation != null)
				return false;
		} else if (!trainStation.equals(other.trainStation))
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return username;
	}
	
}
