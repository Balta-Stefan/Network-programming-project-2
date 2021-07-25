package mdp2021.backend.services.RMI;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.shared.FileHolder;

public class FileQueueWorker extends Thread
{
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
		catch(Exception e) {}
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
				e.printStackTrace();
				// log with Logger
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
			e.printStackTrace();
			// log with Logger
		}
	}
}
