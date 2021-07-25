package mdp2021.backend.services.RMI;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import mdp2021.backend.model.User;
import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.FileHolder;
import mdp2021.backend.utilities.REDIS_UserSessions;
import mdp2021.backend.utilities.UserSessions;


public class RMI_services implements RMI_services_interface
{
	
	//private final UserSessions sessions;
	private final IReportPersistence reportPersistence;
	private final int sessionDurationSeconds;
	private final FileQueueWorker queueWorker;
	 
	
	public RMI_services(IReportPersistence reportPersistence, int sessionDurationSeconds)
	{
		this.reportPersistence = reportPersistence;
		this.sessionDurationSeconds = sessionDurationSeconds;
		
		queueWorker = new FileQueueWorker(reportPersistence);
		queueWorker.start();
	}
	
	public List<FileHolder> listReports(String userCookie) throws RemoteException
	{
		UserSessions sessions = new REDIS_UserSessions(sessionDurationSeconds);
		Optional<User> user = sessions.getUser(userCookie);
		
		if(user.isEmpty())
			return null;
		
		List<FileHolder> reports = reportPersistence.listReports();
		System.out.println("\nRMI has found files: ");
		for(FileHolder f : reports)
			System.out.println(f);
		
		return reports;
	}
	
	public FileHolder getReport(String userCookie, String fileName)  throws RemoteException
	{
		UserSessions sessions = new REDIS_UserSessions(sessionDurationSeconds);
		// authenticate the user by his cookie
		Optional<User> user = sessions.getUser(userCookie);
		if(user.isEmpty())
			return null;
		
		Optional<FileHolder> file = reportPersistence.getReport(fileName);
		return file.get();
	}
	
	public Code_response sendReport(String userCookie, String fileName, byte[] file) throws RemoteException
	{
		if(fileName.endsWith(".pdf") == false)
			return new Code_response(406, "Only PDF files are allowed");
		
		
		UserSessions sessions = new REDIS_UserSessions(sessionDurationSeconds);
		// authenticate the user by his cookie
		Optional<User> user = sessions.getUser(userCookie);
		if(user.isEmpty())
			return new Code_response(404, "Not logged in.");
		
		
		// write the file (what about concurrent writes?)
		User reportSender = user.get();
		
		FileHolder fileWrapper = new FileHolder(fileName, file, reportSender.getUsername(),LocalDateTime.now());
		
		queueWorker.add(fileWrapper);
		
		return new Code_response(202, "Report received");
	
		
		/*boolean status = reportPersistence.saveReport(fileWrapper);
		if(status == false)
			return new Code_response(500, "Error.");
		
		
		return new Code_response(200, "Report sent.");*/
	}
}
