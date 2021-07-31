package mdp2021.backend.GUI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.REDIS_TrainstationPersistence;
import mdp2021.backend.services.socket.MulticastSocketService;
import mdp2021.backend.shared.Announcement;
import mdp2021.backend.shared.FileHolder;
import mdp2021.backend.utilities.PasswordHasher;

// USE CLASS Event OVER ActionEvent!!!!

public class GUI_JavaFX_Controller
{
	private Backend_GUI_Initializer applicationObject;
	
	private static final Logger log = Logger.getLogger(GUI_JavaFX_Controller.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/GUI_JavaFX_Controller.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void setApplicationObject(Backend_GUI_Initializer applicationObject)
	{
		this.applicationObject = applicationObject;
		multicastEventListener = new MulticastServiceGUIUpdater(applicationObject.getMulticastService(), this);
		multicastEventListener.start();
		
		// get users
    	usersListviewItems.addAll(applicationObject.getUsers());
    	
    	Optional<List<TrainStation>> trainStations = applicationObject.getTrainStations();
    	if(trainStations.isEmpty() == false)
    		trainstationListviewItems.addAll(trainStations.get());
    	
    	//Optional<Set<TrainLine>> trainLinesInfo = Optional.empty();//TrainstationsController.getTrainLines();
    	
    	// get train lines
    	//Optional<List<TrainStation>> trainStationsInfo = trainstationPersistence.getTrainStations();
		if(trainStations.isEmpty() == false)
		{
			Set<TrainLine> trainLines = new HashSet<>();
			
			for(TrainStation t : trainStations.get())
			{
				Optional<LinesOfTrainstation> tempLineData = applicationObject.getTrainstationLines(t);
				if(tempLineData.isEmpty())
					continue;
				
				List<TrainLine> tempLines = tempLineData.get().linesThroughStation;
				trainLines.addAll(tempLines);
			}
			
			trainLinesListViewItems.addAll(trainLines);
		}
	}
	
	private static class MulticastServiceGUIUpdater extends Thread
	{
		private static final Logger log = Logger.getLogger(MulticastServiceGUIUpdater.class.getName());
		static
		{
			log.setLevel(Level.FINEST);
			FileHandler txtHandler;
			try
			{
				txtHandler = new FileHandler("Logs/MulticastServiceGUIUpdater.txt", true);
				SimpleFormatter txtFormatter = new SimpleFormatter();
				txtHandler.setFormatter(txtFormatter);
				log.addHandler(txtHandler);
			} catch (SecurityException | IOException e)
			{
				e.printStackTrace();
			}
		}
	
		private static final int sleepPeriod = 5000;
		
		private final MulticastSocketService multicastService;
		private final GUI_JavaFX_Controller interfaceToUpdate;
	
		private boolean run = true;
		
		public MulticastServiceGUIUpdater(MulticastSocketService multicastService, GUI_JavaFX_Controller interfaceToUpdate)
		{
			this.multicastService = multicastService;
			this.interfaceToUpdate = interfaceToUpdate;
			
			multicastService.start();
		}
		
		public void stopService()
		{
			run = false;
			multicastService.stopService();
		}
		
		@Override
		public void run()
		{
			Gson gson = new Gson();
			
			while(run)
			{
				try
				{
					Thread.sleep(sleepPeriod);
					if(run == false)
						return;
					
					List<Byte[]> data = multicastService.getData();
					if(data.isEmpty())
						continue;
					
					List<Announcement> announcements = new ArrayList<>();
					
					for(Byte[] byteArray : data)
					{
						byte[] primitiveArray = new byte[byteArray.length];
						for(int i = 0; i < byteArray.length; i++)
							primitiveArray[i] = byteArray[i];
						
						String announcementJSON = new String(primitiveArray, StandardCharsets.UTF_8);
						announcementJSON = announcementJSON.trim();
						try
						{
							Announcement announcement = gson.fromJson(announcementJSON, Announcement.class);
							announcements.add(announcement);
						}
						catch(Exception e)
						{
							log.warning(e.getMessage());
						}
					}
					
					interfaceToUpdate.updateAnnouncementsList(announcements);
				} 
				catch (InterruptedException e)
				{
					log.warning(e.getMessage());
				}
			}
		}
	}
	
	
	private static final ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(1);
	
	//public static MulticastSocketService multicastService;
	private static MulticastServiceGUIUpdater multicastEventListener;
	
	
	
	public void shutdown()
	{
		multicastEventListener.stopService();
	}
	
	
	public void initialize()
	{
		usersListviewItems = usersListView.getItems();
    	usersListviewItems.add(addNewUserPlaceholder);
    	
    	trainLinesListViewItems = trainLinesListView.getItems();
    	
    	trainstationListviewItems = trainstationListview.getItems();
    	
    	lineStopsListviewItems = lineStopsListView.getItems();
    	
    	reportsListViewItems = reportsListView.getItems();
    	
    	tabPane.setTabMinWidth(200);
	}
	

    private User addNewUserPlaceholder = new User(null, "Add new user", null, null);
	private User selectedUser = null;
	
	
	private ObservableList<User> usersListviewItems;
	private ObservableList<TrainStation> trainstationListviewItems;
	private ObservableList<StationArrival> lineStopsListviewItems;
	private ObservableList<TrainLine> trainLinesListViewItems;
	private ObservableList<FileHolder> reportsListViewItems;
	
	@FXML
	private Button sendAnnouncementButton;
	
	@FXML
	private TextArea announcementContent;
	
	@FXML
	private TextArea newAnnouncementTextArea;
	
	@FXML
	private ListView<Announcement> announcementsListView;
	
	@FXML
	private Label announcementStatusLabel;
	
	public void updateAnnouncementsList(List<Announcement> announcements)
	{
		Platform.runLater(new Runnable() 
		{
			@Override
			public void run()
			{
				announcementsListView.getItems().addAll(announcements);
			}
		});
	}
	
	@FXML
	void sendAnnouncement(Event event)
	{
		Gson gson = new Gson();
		
		String announcementMessage = newAnnouncementTextArea.getText();
		if(announcementMessage.equals(""))
			return;
		
		Announcement announcement = new Announcement(announcementMessage, new User(new TrainStation(0), "Administrator", null, null));
		String announcementJSON = gson.toJson(announcement);
		
		Task<Boolean> tempTask = new Task<Boolean>() 
		{
			@Override
			protected Boolean call() throws Exception
			{
				return applicationObject.sendMulticastData(announcementJSON.getBytes(StandardCharsets.UTF_8));
				//return multicastService.sendData(announcementJSON.getBytes(StandardCharsets.UTF_8));
			}
		};
		
		tempTask.setOnRunning((runningEvent)->
		{
			sendAnnouncementButton.setDisable(true);
		});
		
		tempTask.setOnFailed((failedEvent)->
		{
			sendAnnouncementButton.setDisable(false);
			announcementStatusLabel.setText("Announcement not sent");
			announcementStatusLabel.setTextFill(Color.RED);
		});
		
		tempTask.setOnSucceeded((successEvent)->
		{
			sendAnnouncementButton.setDisable(false);
			
			boolean success = tempTask.getValue();
			
			if(success == true)
			{
				announcementStatusLabel.setText("Announcement sent");
				announcementStatusLabel.setTextFill(Color.GREEN);
				newAnnouncementTextArea.clear();
			}
			else
			{
				announcementStatusLabel.setText("Announcement not sent");
				announcementStatusLabel.setTextFill(Color.RED);
			}
		});
		
		executor.submit(tempTask);
	}
	
	@FXML
	void announcementSelect(Event event)
	{
		Announcement announcement = announcementsListView.getSelectionModel().getSelectedItem();
		if(announcement == null)
			return;
		
		announcementContent.setText("Sender: " + announcement.sender.toString() + "\n");
		announcementContent.appendText("Station: " + announcement.sender.getTrainStation().toString() + "\n");
		announcementContent.appendText("\n\n");
		announcementContent.appendText(announcement.message);
	}
	
	@FXML
    private TabPane tabPane;

    @FXML
    private ListView<User> usersListView;
    
    @FXML
    private ListView<TrainStation> trainstationListview;
    
    @FXML
    private ListView<StationArrival> lineStopsListView;
    
    @FXML
    private ListView<TrainLine> trainLinesListView;
    
    @FXML
    private ListView<FileHolder> reportsListView;

    @FXML
    private TextField reportNameInput;
    
    @FXML
    private TextField reportUploaderInput;
    
    @FXML
    private TextField reportFilesizeInput;
    
    @FXML
    private TextField reportUploadTimestamp;
    
    @FXML
    private TextField usernameInput;

    @FXML
    private TextField passwordInput;
    
    @FXML
    private TextField addTrainstationID_input;

    @FXML
    private TextField userTrainstationID_input;

    @FXML
    private Button addUserButton;

    @FXML
    private Button removeUserButton;

    @FXML
    private Label userInteractionStatusMessage;
    
    @FXML
    private Label lineOperationStatusLabel;

    @FXML
    private Label reportsStatusMessageLabel;

    @FXML
    private Button addStationButton;

    @FXML
    private Button removeStationButton;

    @FXML
    private Label trainstationInteractionStatusLabel;

    @FXML
    private TextField timeInput;

    @FXML
    private DatePicker dateInput;

    @FXML
    private Tab reportsTab;
    
	
	// Users tab
    @FXML
    void addUser(Event event)
    {
    	String username = usernameInput.getText();
    	String trainstationID_string = userTrainstationID_input.getText();
    	String password = passwordInput.getText();
    	
    	if(username.equals("") || trainstationID_string.equals("") || password.equals(""))
    		return;
    	
    	int trainStationID = Integer.parseInt(trainstationID_string);
    	
    	
    	User newUser = new User(new TrainStation(trainStationID), username, password, null);
    	//boolean registrationStatus = UsersController.register(newUser);
    	
    	// from controller
    	REDIS_TrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();
		boolean registrationStatus = trainstationPersistence.addUserToTrainstation((User)newUser.clone());
		
		PasswordHasher hasher = applicationObject.getHasher();
		if(registrationStatus == true)
		{
			byte[] salt = hasher.getSalt();
			newUser.setSalt(salt);
			try
			{
				String hash = hasher.hash(salt, newUser.getPassword());
				newUser.setPassword(hash);
				
				registrationStatus = applicationObject.addUser(newUser);
			}
			catch (Exception e)
			{
				log.info(e.getMessage());
				registrationStatus =  false;
			}
		}
    	
    	// end of controller
    	
    	if(registrationStatus == true)
    	{
    		userInteractionStatusMessage.setText("Registration successful");
    		userInteractionStatusMessage.setTextFill(Color.GREEN);
    		
    		usernameInput.setText("");
    		passwordInput.setText("");
    		userTrainstationID_input.setText("");
    		
    		usersListviewItems.add(newUser);
    	}
    	else
    	{
    		userInteractionStatusMessage.setText("Registration unsuccessful");
    		userInteractionStatusMessage.setTextFill(Color.RED);
    	}
    }

    @FXML
    void removeUser(Event event)
    {
    	if(selectedUser == null)
    		return;
    	
    	boolean status = applicationObject.removeUser(selectedUser);
    	
    	if(status == true)
    	{
    		userInteractionStatusMessage.setText("User removed");
    		userInteractionStatusMessage.setTextFill(Color.GREEN);
    		
    		usersListviewItems.remove(selectedUser);
    		selectedUser = null;
    	}
    	else
    	{
    		userInteractionStatusMessage.setText("User not removed");
    		userInteractionStatusMessage.setTextFill(Color.RED);
    	}
    }

    @FXML
    void selectUser(Event event)
    {
    	int selectedIndex = usersListView.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	userInteractionStatusMessage.setText("");
    	
    	
    	selectedUser = usersListviewItems.get(selectedIndex);
    	
    	if(selectedUser.equals(addNewUserPlaceholder))
    	{
    		selectedUser = null;
    		
        	usernameInput.setDisable(false);
        	passwordInput.setDisable(false);
        	userTrainstationID_input.setDisable(false);
        	
        	addUserButton.setDisable(false);
        	removeUserButton.setDisable(true);
        	
        	usernameInput.setText("");
        	userTrainstationID_input.setText("");
    	}
    	else
    	{
        	usernameInput.setDisable(true);
        	passwordInput.setDisable(true);
        	userTrainstationID_input.setDisable(true);
        	
        	addUserButton.setDisable(true);
        	removeUserButton.setDisable(false);

        	usernameInput.setText(selectedUser.getUsername());
        	userTrainstationID_input.setText(Integer.toString(selectedUser.getTrainStation().getID()));
    	}
    }

    // Lines tab
    @FXML
    void addStation(Event event)
    {
    	String trainstationIDInput = addTrainstationID_input.getText();
    	int trainstationID = 0;
    	
    	try
    	{
    		trainstationID = Integer.parseInt(trainstationIDInput);
    	}
    	catch(NumberFormatException e)
    	{
    		log.info(e.getMessage());
    		trainstationInteractionStatusLabel.setText("Enter ID of the trainstation");
    		trainstationInteractionStatusLabel.setTextFill(Color.RED);
    		return;
    	}
    	
    	TrainStation selectedStation = new TrainStation(trainstationID);
    	boolean status = applicationObject.addTrainStation(selectedStation);
    	
    	if(status == true)
    	{
    		trainstationInteractionStatusLabel.setText("Train station created.");
    		trainstationInteractionStatusLabel.setTextFill(Color.GREEN);
    		
    		addTrainstationID_input.setText("");
    		
    		trainstationListviewItems.add(selectedStation);
    	}
    	else
    	{
    		trainstationInteractionStatusLabel.setText("Trainstation not created.");
    		trainstationInteractionStatusLabel.setTextFill(Color.RED);
    	}
    }
    
    @FXML
    void removeStation(Event event) 
    {
    	TrainStation selectedTrainstation = getSelectedTrainstation_linesTab();
    	if(selectedTrainstation == null)
    		return;
    	
    	boolean status = applicationObject.removeStation(selectedTrainstation);
    	
    	if(status == true)
    	{
    		trainstationInteractionStatusLabel.setText("Station removed");
    		trainstationInteractionStatusLabel.setTextFill(Color.GREEN);
    		
    		trainstationListviewItems.remove(selectedTrainstation);
    		
    		addTrainstationID_input.setText("");
    	}
    	else
    	{
    		trainstationInteractionStatusLabel.setText("Error");
    		trainstationInteractionStatusLabel.setTextFill(Color.RED);
    	}
    }
    
    @FXML
    void addPoint(Event event)
    {
    	TrainStation selectedTrainstation = getSelectedTrainstation_linesTab();
    	if(selectedTrainstation == null)
    		return;
    	
    	LocalDate date = dateInput.getValue(); // will be null when nothing is selected
    	if(date == null)
    		return;
    	
    	String timeValue = timeInput.getText();
    	if(timeValue.equals(""))
    		return;
    	
    	LocalTime time = null;
    	try
    	{
    		time = LocalTime.parse(timeValue);
    	}
    	catch(DateTimeParseException e) 
    	{
    		log.info(e.getMessage());
    		return;
    	}
    	
    	LocalDateTime dateTime = LocalDateTime.of(date, time);

    	StationArrival arrival = new StationArrival(selectedTrainstation, dateTime, false);
    	
    	if(lineStopsListviewItems.contains(arrival))
    		return;
    	
    	lineStopsListviewItems.add(arrival);
    }
    
    @FXML
    void resetLineInputs(Event event)
    {
    	lineStopsListviewItems.clear();
    	
    	timeInput.setText("");
    	dateInput.setValue(null);
    }
    
    @FXML
    void addLine(Event event)
    {
    	Optional<TrainLine> newTrainLine = Optional.empty();
    	List<StationArrival> lines = new ArrayList<>(lineStopsListView.getItems());
    	
    	try
		{
			StringBuilder builder = new StringBuilder();
			int i = 0;
			for(; i < lines.size()-1; i++)
				builder.append(lines.get(i).trainStation.getID() + "-");
			builder.append(lines.get(i).trainStation.getID());
			
			TrainLine line = new TrainLine(0, builder.toString(), lines);
			TrainLine newLine = applicationObject.addLine(line);
			
			newTrainLine = Optional.of(newLine);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
		}
    	
    	if(newTrainLine.isEmpty() == false)
    	{
    		lineOperationStatusLabel.setText("Line added");
    		lineOperationStatusLabel.setTextFill(Color.GREEN);
    		
    		resetLineInputs(null);
    		
    		trainLinesListViewItems.add(newTrainLine.get());
    	}
    	else
    	{
    		lineOperationStatusLabel.setText("Line not added");
    		lineOperationStatusLabel.setTextFill(Color.RED);
    	}
    }
    
    @FXML
    void selectStation(Event event) 
    {
    	TrainStation selectedTrainstation = getSelectedTrainstation_linesTab();
    	if(selectedTrainstation == null)
    		return;
    	
    	addTrainstationID_input.setText(selectedTrainstation.toString());
    }

    @FXML
    void removeSelectedLine(Event event)
    {
    	int selectedIndex = trainLinesListView.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	TrainLine lineToRemove = trainLinesListViewItems.get(selectedIndex);
    	boolean status = applicationObject.removeLine(lineToRemove);
    	
    	if(status == true)
    	{
    		lineOperationStatusLabel.setText("Line removed");
    		lineOperationStatusLabel.setTextFill(Color.GREEN);
    		
    		trainLinesListViewItems.remove(selectedIndex);
    	}
    	else
    	{
    		lineOperationStatusLabel.setText("Line not removed");
    		lineOperationStatusLabel.setTextFill(Color.RED);
    	}
    }
    
    @FXML
    void showTrainLineData(Event event)
    {
    	int selectedIndex = trainLinesListView.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	lineStopsListviewItems.clear();
    	
    	TrainLine selectedTrainLine = trainLinesListViewItems.get(selectedIndex);
    	lineStopsListviewItems.addAll(selectedTrainLine.stationArrivals);
    }
    
    private TrainStation getSelectedTrainstation_linesTab()
    {
    	int selectedIndex = trainstationListview.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return null;
    	
    	trainstationInteractionStatusLabel.setText("");
    	
    	return trainstationListviewItems.get(selectedIndex);
    }

    // Reports tab
    @FXML
    void getReportsList(Event event) 
    {
    	if(reportsTab.isSelected() == false)
    		return;
    	
    	List<FileHolder> filesMetadata = null;
		try
		{
			filesMetadata = applicationObject.listReports();
		} 
		catch (RemoteException e)
		{
			log.info(e.getMessage());
			reportsStatusMessageLabel.setText("Error has occured while fetching files list");
    		reportsStatusMessageLabel.setTextFill(Color.RED);
    		return;
		}

    	reportsStatusMessageLabel.setText("");
    	
    	reportsListViewItems.clear();
    	reportsListViewItems.addAll(filesMetadata);
    }
    
    @FXML
    void downloadReport(Event event)
    {
    	int selectedIndex = reportsListView.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	FileHolder selectedFile = reportsListViewItems.get(selectedIndex);
    	FileHolder file = null;
    	
    	try
		{
			file = applicationObject.getReport(selectedFile.metadata.fileName);
			if(file == null)
				throw new Exception();
		}
    	catch (Exception e)
		{
    		log.info(e.getMessage());
			reportsStatusMessageLabel.setText("Error has occured while obtaining file data.");
    		reportsStatusMessageLabel.setTextFill(Color.RED);
    		return;
		}
    	
    	reportsStatusMessageLabel.setText("");
    	
    	applicationObject.saveReport(file);
    }
    
    @FXML
    void getReportMetadata(Event event)
    {
    	int selectedIndex = reportsListView.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	FileHolder file = reportsListViewItems.get(selectedIndex);
    	
    	try
		{
			applicationObject.getReport(file.metadata.fileName);
		} 
    	catch (RemoteException e)
		{
    		log.info(e.getMessage());
    		reportsStatusMessageLabel.setText("Error has occured while obtaining file data.");
    		reportsStatusMessageLabel.setTextFill(Color.RED);
    		return;
		}
    	
    	reportsStatusMessageLabel.setText("");
    	
    	reportNameInput.setText(file.metadata.fileName);
    	reportUploaderInput.setText(file.metadata.username);
    	
    	int fileSize = file.metadata.fileSize; //Integer.toString(file.metadata.fileSize);
    	int newSize = 0;
    	
    	
    	String sizeSuffix = " MiB";
    	newSize = fileSize / (1024 * 1024);
    	if(newSize == 0)
    	{
    		newSize = fileSize / 1024;
    		sizeSuffix = " KiB";
    		
    		if(newSize == 0)
    		{
    			newSize = fileSize;
    			sizeSuffix = " B";
    		}
    	}
    	
    	reportFilesizeInput.setText(Integer.toString(newSize) + sizeSuffix);
    	reportUploadTimestamp.setText(file.metadata.dateTime);
    }


}

