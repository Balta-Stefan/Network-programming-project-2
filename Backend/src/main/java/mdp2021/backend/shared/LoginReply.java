package mdp2021.backend.shared;

import java.io.Serializable;

import mdp2021.backend.model.TrainStation;

public class LoginReply implements Serializable
{
	public Code_response codeResponse;
	public TrainStation trainstationInfo;
	public String cookie;
	
	public LoginReply() {}
	
	public LoginReply(Code_response codeResponse, TrainStation trainstationInfo, String cookie)
	{
		this.codeResponse = codeResponse;
		this.trainstationInfo = trainstationInfo;
		this.cookie = cookie;
	}

	@Override
	public String toString()
	{
		return "LoginReply [codeResponse=" + codeResponse + ", trainstationInfo=" + trainstationInfo + ", cookie="
				+ cookie + "]";
	}
	
	
}
