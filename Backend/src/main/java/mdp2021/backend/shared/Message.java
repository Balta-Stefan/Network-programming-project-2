package mdp2021.backend.shared;

import java.io.Serializable;

public abstract class Message implements Serializable
{
	private static final long serialVersionUID = 1L;
	public String sender;
	
	public Message(String sender)
	{
		this.sender = sender;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
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
		Message other = (Message) obj;
		if (sender == null)
		{
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		return true;
	}
}
