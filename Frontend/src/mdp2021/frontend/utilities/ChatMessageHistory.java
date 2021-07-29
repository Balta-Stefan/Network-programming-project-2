package mdp2021.frontend.utilities;

import mdp2021.backend.model.TrainstationUsers;
import mdp2021.backend.model.User;

public class ChatMessageHistory
{
	private TrainstationUsers station;
	private User user;
	private String myMessageHistory;
	private String receiverMessageHistory;
	
	
	public ChatMessageHistory(TrainstationUsers station, User user, String myMessageHistory, String receiverMessageHistory)
	{
		this.station = station;
		this.user = user;
		this.myMessageHistory = myMessageHistory;
		this.receiverMessageHistory = receiverMessageHistory;
	}
	
	public String getMyMessageHistory()
	{
		return myMessageHistory;
	}


	public void appendMyMessage(String myMessageHistory)
	{
		if(this.myMessageHistory == null)
			this.myMessageHistory = myMessageHistory;
		else
			this.myMessageHistory += "\n" + myMessageHistory;
	}


	public String getReceiverMessageHistory()
	{
		return receiverMessageHistory;
	}


	public void appendReceiverMessage(String receiverMessageHistory)
	{
		if(this.receiverMessageHistory == null)
			this.receiverMessageHistory = receiverMessageHistory;
		else
			this.receiverMessageHistory += "\n" + receiverMessageHistory;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((station == null) ? 0 : station.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		ChatMessageHistory other = (ChatMessageHistory) obj;
		if (station == null)
		{
			if (other.station != null)
				return false;
		} else if (!station.equals(other.station))
			return false;
		if (user == null)
		{
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
	
	
}
