package mdp2021.backend.shared;

import java.io.Serializable;
import java.util.List;

public class FileOrTextMessage extends Message implements Serializable
{
	private static final long serialVersionUID = 1L;
	public final String message;
	public final List<FileHolder> files;
	public final String receiver_username;
	
	public FileOrTextMessage(String message, List<FileHolder> files, String from, String receiver_username)
	{
		super(from);
		this.message = message;
		this.files = files;
		this.receiver_username = receiver_username;
	}
	
	@Override
	public String toString()
	{
		return "Message from: " + sender;
	}
}
