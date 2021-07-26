package mdp2021.backend.services.RMI;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.shared.FileHolder;

public class FileQueueWorker extends Thread
{
	private static final Logger log = Logger.getLogger(FileQueueWorker.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/FileQueueWorker.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private BlockingQueue<Object> fileQueue = new LinkedBlockingQueue<>();
	private IReportPersistence reportPersistence;
	private boolean runLoop = true;
	
	private class Stopper
	{
		
	}
	
	public FileQueueWorker(IReportPersistence reportPersistence)
	{
		this.reportPersistence = reportPersistence;
	}
	
	public void stopRunning()
	{
		runLoop = false;
		try
		{
			fileQueue.put(new Stopper());
		}
		catch(Exception e) 
		{
			log.info(e.getMessage());
		}
	}
	
	@Override
	public void run()
	{
		while(runLoop)
		{
			try
			{
				Object newFile = fileQueue.take();
				
				if(newFile instanceof Stopper)
					return;
				
				reportPersistence.saveReport((FileHolder)newFile);
			}
			catch (InterruptedException e)
			{
				log.info(e.getMessage());
			}
		}
	}
	
	public void add(FileHolder file)
	{
		try
		{
			fileQueue.put(file);
		} catch (InterruptedException e)
		{
			log.info(e.getMessage());
		}
	}
}
