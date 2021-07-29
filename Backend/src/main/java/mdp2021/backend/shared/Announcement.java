package mdp2021.backend.shared;


import mdp2021.backend.model.User;

public class Announcement 
{
	public final String message;
	public final User sender;
	
	
	public Announcement(String message, User sender)
	{
		this.message = message;
		this.sender = sender;
	}


	@Override
	public String toString()
	{
		return "Announcement: " + "sender=" + sender;
	}
}
