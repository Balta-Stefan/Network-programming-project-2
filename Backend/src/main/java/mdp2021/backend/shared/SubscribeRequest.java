package mdp2021.backend.shared;

import java.io.Serializable;

public class SubscribeRequest extends Message implements Serializable
{
	public static enum Type{SUBSCRIBE, UNSUBSCRIBE}
	public final Type type;

	public SubscribeRequest(String userCookie, Type type)
	{
		super(userCookie);
		this.type = type;
	}
}
