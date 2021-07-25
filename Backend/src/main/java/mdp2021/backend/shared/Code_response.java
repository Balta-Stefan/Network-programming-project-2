package mdp2021.backend.shared;

import java.io.Serializable;

public class Code_response implements Serializable
{
	public int code;
	public String message;
	
	public Code_response() {}
	
	public Code_response(int code, String message)
	{
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString()
	{
		return "Code_response [code=" + code + ", message=" + message + "]";
	}
	
}
