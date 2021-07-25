package mdp2021.backend.services.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.FileHolder;

public interface RMI_services_interface extends Remote
{
	public Code_response sendReport(String userCookie, String fileName, byte[] file) throws RemoteException;
	public List<FileHolder> listReports(String userCookie) throws RemoteException;
	public FileHolder getReport(String userCookie, String fileName) throws RemoteException;
}
