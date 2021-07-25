package mdp2021.backend.persistence;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import mdp2021.backend.shared.FileHolder;

public class Filesystem_ReportPersistence implements IReportPersistence
{
	private Gson gson = new Gson();
	
	
	private final String filePathPrefix;
	private static final String metadataFileSuffix = "-Metadata.json";
	private static final Charset charset = StandardCharsets.UTF_8;
	
	public Filesystem_ReportPersistence(String filePathPrefix)
	{
		this.filePathPrefix = filePathPrefix;
	}
	
	public List<FileHolder> listReports()
	{
		List<FileHolder> reportsMetadata = new ArrayList<>();
		
		File parentDir = new File(filePathPrefix);
		
		File[] reports = parentDir.listFiles();
		System.out.println("Report persistence has found: " + reports.length + " metadata files");
		
		for(File f : reports)
		{
			System.out.println(f);
			if((f.getName().endsWith(metadataFileSuffix)) == false)
				continue;
			String fileData = null;
			try
			{
				fileData = new String(Files.readAllBytes(f.toPath()), charset);
			}
			catch (IOException e)
			{
				continue;
			}
			
			System.out.println(fileData);
			System.out.println("Now deserializing");
			FileHolder.FileMetadata metadata = gson.fromJson(fileData, FileHolder.FileMetadata.class);
			System.out.println("The metadata is: " + metadata);
			reportsMetadata.add(new FileHolder(null, metadata));
		}
		
		return reportsMetadata;
	}
	
	public Optional<FileHolder> getReport(String filename)
	{
		File file = new File(filePathPrefix + filename);
		File fileInformation = new File(filePathPrefix + filename + metadataFileSuffix);
		
		if(file.exists() == false)
			return Optional.empty();
		
		if(fileInformation.exists() == false)
			return Optional.empty();
		
		try
		{
			byte[] fileData = Files.readAllBytes(file.toPath());
			String fileMetadataString = new String(Files.readAllBytes(fileInformation.toPath()), charset);
			
			FileHolder.FileMetadata metadata = gson.fromJson(fileMetadataString, FileHolder.FileMetadata.class);
			
			FileHolder report = new FileHolder(fileData, metadata);
			return Optional.of(report);
		}
		catch (IOException e)
		{
			return Optional.empty();
		}
	}
	
	public boolean saveReport(FileHolder fileWrapper)
	{
		String filename = fileWrapper.metadata.fileName;
		
		File file = new File(filePathPrefix + filename);
		File fileInformation = new File(filePathPrefix + filename + metadataFileSuffix);
		
		if(file.exists() == true)
			return false;
		
		if(fileInformation.exists() == true)
			return false;
		
		try
		{
			if(file.createNewFile() == false)
				return false;
			if(fileInformation.createNewFile() == false)
			{
				file.delete();
				return false;
			}
			
			String metadata_JSON = gson.toJson(fileWrapper.metadata);
			Files.write(file.toPath(), fileWrapper.data);
			Files.write(fileInformation.toPath(), metadata_JSON.getBytes(charset));
		}
		catch (IOException e)
		{
			return false;
		}
		
		
		return true;
	}

}
