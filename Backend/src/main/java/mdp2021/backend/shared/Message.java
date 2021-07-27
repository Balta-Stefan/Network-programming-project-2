package mdp2021.backend.shared;

import java.io.Serializable;

public abstract class Message implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String sender;
	
	public Message(String sender)
	{
		this.sender = sender;
	}
}
