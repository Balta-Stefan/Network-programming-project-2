package mdp2021.backend.persistence;

import java.util.List;
import java.util.Optional;

import mdp2021.backend.shared.FileHolder;

public interface IReportPersistence
{
	public boolean saveReport(FileHolder fileWrapper);
	public List<FileHolder> listReports();
	public Optional<FileHolder> getReport(String filename);
}
