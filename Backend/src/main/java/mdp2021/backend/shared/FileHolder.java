package mdp2021.backend.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHolder implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public class FileMetadata implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		//public final int userID;
		public final String username;
		
		public final String fileName;
		public final int fileSize;
		
		public final String dateTime; // String is user over LocalDateTime because GSON doesn't support LocalDateTime
		
		public FileMetadata(String fileName, LocalDateTime dateTime, String username, int fileSize)
		{
			//this.userID = userID;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			this.dateTime = dateTime.format(formatter); 
			this.username = username;
			this.fileName = fileName;
			this.fileSize = fileSize;
		}

		@Override
		public String toString()
		{
			return fileName;
		}
	}

	//public final String name;
	public final byte[] data;
	public final FileMetadata metadata;
	
	public FileHolder(String name, byte[] data, String uploaded_by_name, LocalDateTime upload_datetime)
	{
		//this(name, data);
		this.data = data;
		
		metadata = new FileMetadata(name, upload_datetime, uploaded_by_name, data.length);
	}
	
	public FileHolder(byte[] data, FileMetadata metadata)
	{
		this.data = data;
		this.metadata = metadata;
	}
	
	@Override
	public String toString()
	{
		return metadata.toString();
	}
	
	/*public FileHolder(String name, byte[] data)
	{
		this.name = name;
		this.data = data;
	}*/
}
