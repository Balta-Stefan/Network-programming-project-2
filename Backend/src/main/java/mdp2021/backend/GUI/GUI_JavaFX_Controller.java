package mdp2021.backend.GUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mdp2021.backend.GUI.controllers.TrainstationsController;
import mdp2021.backend.GUI.controllers.UsersController;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.Filesystem_ReportPersistence;
import mdp2021.backend.persistence.IReportPersistence;
import mdp2021.backend.services.RMI.RMI_services_interface;
import mdp2021.backend.shared.FileHolder;

// USE CLASS Event OVER ActionEvent!!!!

public class GUI_JavaFX_Controller
{
	private static final Logger log = Logger.getLogger(GUI_JavaFX_Controller.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{
			txtHandler = new FileHandler("Logs/GUI_Controller.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static final String downloadFolder = "Application data\\Downloaded reports\\";
	private static final String RMI_service_nameProperty = "RMI_service_name";
	private static final String RMI_service_portProperty = "RMI_service_port";
	
	private static final String propertiesPath = "Resources\\backend constants.properties";
	private final String RMI_service_name;
	private final int RMI_port;
	
	private RMI_services_interface rmiService;
	
	public GUI_JavaFX_Controller()
	{
		Properties backendProperties = new Properties();
		
		try(FileInputStream fis = new FileInputStream(new File(propertiesPath)))
		{
			backendProperties.load(fis);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			log.info(e.getMessage());
		}
		
		RMI_service_name = backendProperties.getProperty(RMI_service_nameProperty);
		RMI_port = Integer.parseInt(backendProperties.getProperty(RMI_service_portProperty));
		
		
		System.setProperty("java.security.policy", "./Resources/client_policyfile.txt");
		
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try
		{
			Registry registry = LocateRegistry.getRegistry(RMI_port);
			rmiService = (RMI_services_interface)registry.lookup(RMI_service_name);
		}
		catch(Exception e)
		{
			log.info(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	
	public static String adminCookie;

	
    private User addNewUserPlaceholder = new User(null, "Add new user", null, null);
   
   
	
	
	
	private User selectedUser = null;
	
	private ObservableList<User> usersListviewItems;
	private ObservableList<TrainStation> trainstationListviewItems;
	private ObservableList<StationArrival> lineStopsListviewItems;
	private ObservableList<TrainLine> trainLinesListViewItems;
	private ObservableList<FileHolder> reportsListViewItems;
	
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

    
	public void initialize()
	{
		usersListviewItems = usersListView.getItems();
    	usersListviewItems.add(addNewUserPlaceholder);
    	
    	trainLinesListViewItems = trainLinesListView.getItems();
    	
    	trainstationListviewItems = trainstationListview.getItems();
    	
    	lineStopsListviewItems = lineStopsListView.getItems();
    	
    	reportsListViewItems = reportsListView.getItems();
    	
    	tabPane.setTabMinWidth(200);
    	
    	// get users
    	usersListviewItems.addAll(UsersController.getUsers());
    	
    	Optional<List<TrainStation>> trainStations = TrainstationsController.getTrainStations();
    	if(trainStations.isEmpty() == false)
    		trainstationListviewItems.addAll(trainStations.get());
    	
    	Optional<Set<TrainLine>> trainLines = TrainstationsController.getTrainLines();
    	if(trainLines.isPresent())
    		trainLinesListViewItems.addAll(trainLines.get());
	}
	
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
    	boolean registrationStatus = UsersController.register(newUser);
    	
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
    	
    	boolean status = UsersController.removeUser(selectedUser);
    	
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
    	boolean status = TrainstationsController.addTrainstation(selectedStation);
    	
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
    	
    	boolean status = TrainstationsController.removeTrainstation(selectedTrainstation);
    	
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
    	Optional<TrainLine> newTrainLine = TrainstationsController.addLinesToTrainstation(lineStopsListviewItems);
    	
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
    	boolean status = TrainstationsController.removeTrainLine(lineToRemove);
    	
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
    	List<FileHolder> filesMetadata = null;
		try
		{
			filesMetadata = rmiService.listReports(adminCookie);
		} 
		catch (RemoteException e)
		{
			log.info(e.getMessage());
			reportsStatusMessageLabel.setText("Error has occured while fetching files list");
    		reportsStatusMessageLabel.setTextFill(Color.RED);
    		return;
		}

    	reportsStatusMessageLabel.setText("");
    	
    	for(FileHolder f : filesMetadata)
    	{
    		reportsListViewItems.add(f);
    	}
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
			file = rmiService.getReport(adminCookie, selectedFile.metadata.fileName);
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
    	
    	IReportPersistence reportPersistence = new Filesystem_ReportPersistence(downloadFolder);
    	reportPersistence.saveReport(file);
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
			rmiService.getReport(adminCookie, file.metadata.fileName);
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
    	reportFilesizeInput.setText(Integer.toString(file.metadata.fileSize));
    	reportUploadTimestamp.setText(file.metadata.dateTime);
    }


}

