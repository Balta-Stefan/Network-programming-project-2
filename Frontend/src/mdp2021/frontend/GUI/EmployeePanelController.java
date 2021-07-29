package mdp2021.frontend.GUI;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.model.TrainStation;
import mdp2021.backend.model.TrainstationUsers;
import mdp2021.backend.model.User;
import mdp2021.backend.shared.Announcement;
import mdp2021.backend.shared.Code_response;
import mdp2021.backend.shared.FileOrTextMessage;
import mdp2021.frontend.utilities.ChatMessageHistory;


public class EmployeePanelController extends BaseFXController
{
	private HashMap<ChatMessageHistory, ChatMessageHistory> chatHistory = new HashMap<>();
	
	public EmployeePanelController()
	{
		frontendController.setEmployeePanelController(this);
	}
	
	private File chosenReport;

    @FXML
    private Tab lineSchedulesTab;

    @FXML
    private Button refreshLineSchedulesTabButton;

    @FXML
    private Label getLinesStatusLabel;

    @FXML
    private ListView<TrainLine> trainLinesListView_lineSchedulesTab;
    private ObservableList<TrainLine> trainLinesListViewItems = FXCollections.observableArrayList();
    
    @FXML
    private ListView<String> lineStations_lineSchedulesTab;
    private ObservableList<String> lineStations_lineSchedulesTabItems =  FXCollections.observableArrayList();
    
    @FXML
    private Tab recordTrainPassTab;

    @FXML
    private Button refreshLinesList_recordTab;

    @FXML
    private ListView<TrainLine> trainLinesList_recordTrainPassTab;

    @FXML
    private TextField recordTrainPass_timeInput;

    @FXML
    private Button recordTrainPass_sendInfoButton;

    @FXML
    private Label recordTrainPassStatusLabel;

    @FXML
    private Tab announcementsTab;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private Button sendReportButton;

    @FXML
    private Label reportUploadStatusLabel;

    @FXML
    private Tab chatTab;

    @FXML
    private ComboBox<TrainstationUsers> chatTrainstationsComboBox;

    @FXML
    private ComboBox<User> chatUsersComboBox;

    @FXML
    private TextArea sentMessagesTextArea;

    @FXML
    private TextField chatMessageInput;

    @FXML
    private ListView<File> chatFilesListView;

    @FXML
    private Button sendMessageButton;

    @FXML
    private Label chatMessageStatusLabel;

    @FXML
    private ListView<FileOrTextMessage> messageNotifications;
    private Set<FileOrTextMessage> nonDuplicateMessageNotifications = new HashSet<>();
    
    @FXML
    private Label logoutSuccessLabel;
    
    @FXML
    private ListView<Announcement> announcementsListView;
    
    @FXML
    private TextArea announcementContent;
    
    @FXML
    private TextArea newAnnouncementTextArea;
    
    @FXML
    private Button sendAnnouncementButton;
    
    @FXML
    private TextArea receiverTextArea;
    
    @FXML
    private Label announcementStatusLabel;
    
    public void initialize()
    {
    	//trainLinesListViewItems = trainLinesListView_lineSchedulesTab.getItems();
    	//lineStations_lineSchedulesTabItems = lineStations_lineSchedulesTab.getItems();
    	trainLinesListView_lineSchedulesTab.setItems(trainLinesListViewItems);
    	trainLinesList_recordTrainPassTab.setItems(trainLinesListViewItems);
    	
    	lineStations_lineSchedulesTab.setItems(lineStations_lineSchedulesTabItems);
    	
    	Optional<List<TrainstationUsers>> trainstationUsers = frontendController.getTrainstationUsers();
    	if(trainstationUsers.isPresent())
    	{
    		List<TrainstationUsers> usersData = trainstationUsers.get();
    		for(TrainstationUsers c : usersData)
    			System.out.println(c.trainStation.getID());
    		chatTrainstationsComboBox.getItems().addAll(usersData);
    	}
    }
    
    @FXML
    void sendAnnouncement(Event event)
    {
		String announcementMessage = newAnnouncementTextArea.getText();
		if(announcementMessage.equals(""))
			return;
		
		Task<Boolean> tempTask = new Task<Boolean>() 
		{
			@Override
			protected Boolean call() throws Exception
			{
				return frontendController.sendMulticastData(announcementMessage);
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
    void removeChatFile(Event event)
    {
    	File selectedFile = chatFilesListView.getSelectionModel().getSelectedItem();
    	if(selectedFile == null)
    		return;
    	
    	chatFilesListView.getItems().remove(selectedFile);
    }
    
    @FXML
    void addChatFiles(Event event)
    {
    	FileChooser fileChooser = new FileChooser();
    	
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("File type:" , "*"));
    	File fileToSend = fileChooser.showOpenDialog(((Node)(event.getSource())).getScene().getWindow());
    	
    	chatFilesListView.getItems().add(fileToSend);
    }
    
    private void addMessageToHistory(TrainstationUsers tu, User user, String myMessage, String receiverMessage)
    {
    	ChatMessageHistory temp = new ChatMessageHistory(tu, user, myMessage, receiverMessage);
		ChatMessageHistory obj = chatHistory.get(temp);
		
		if(obj == null)
		{
			chatHistory.put(temp, temp);
		}
		else
		{
			if(myMessage != null) // sending
			{
				obj.appendMyMessage(myMessage);
			}
			else // receiving
			{
				obj.appendReceiverMessage(receiverMessage);
			}
		}
    }
    
    public void receiveMessage(FileOrTextMessage message)
    {
    	Platform.runLater(new Runnable()
    	{
			@Override
			public void run()
			{
				chatTab.setStyle("-fx-text-base-color: red;");
				
				nonDuplicateMessageNotifications.add(message);
				messageNotifications.getItems().clear();
				messageNotifications.getItems().addAll(nonDuplicateMessageNotifications);
				
				
				for(TrainstationUsers tu : chatTrainstationsComboBox.getItems())
		    	{
		    		for(User user : tu.users)
		    		{
		    			if(user.getUsername().equals(message.sender))
		    			{
		    				chatTrainstationsComboBox.getSelectionModel().select(tu);
		    				chatUsersComboBox.getSelectionModel().select(user);
		    				
		    				addMessageToHistory(tu, user, null, message.message);
		    				
		    				return;
		    			}
		    		}
		    	}
			}
		});
    }
    
    @FXML
    void sendMessage(Event event)
    {
    	String message = chatMessageInput.getText();
    	User receiver = chatUsersComboBox.getSelectionModel().getSelectedItem();
    	TrainstationUsers selectedStation = chatTrainstationsComboBox.getSelectionModel().getSelectedItem();
    			
    	
    	List<File> files = chatFilesListView.getItems();
    	
    	if(receiver == null)
    	{
    		chatMessageStatusLabel.setText("Choose a receiver first.");
    		chatMessageStatusLabel.setTextFill(Color.RED);
    		return;
    	}
    	
    	if(message.equals("") && files.isEmpty())
    	{
    		chatMessageStatusLabel.setText("There are no files or message to send.");
    		chatMessageStatusLabel.setTextFill(Color.RED);
    		return;
    	}
    	
    	// sendMessage(String message, List<File> files, String receiver_username
    	
    	Task<Code_response> tempTask = new Task<Code_response>()
    	{
			@Override
			protected Code_response call() throws Exception
			{
				return frontendController.sendMessage(message, files, receiver.getUsername());
			}
		};
    	
		tempTask.setOnRunning((runningEvent)->
		{
			sendMessageButton.setDisable(true);
		});
		
		tempTask.setOnFailed((failedEvent)->
		{
			sendMessageButton.setDisable(false);
			
			chatMessageStatusLabel.setText("Error.");
			chatMessageStatusLabel.setTextFill(Color.RED);
		});
		
		tempTask.setOnSucceeded((successEvent)->
		{
			sendMessageButton.setDisable(false);
			
			Code_response response = tempTask.getValue();
			
			if(response.getCode() != 200)
			{
				chatMessageStatusLabel.setText(response.getMessage());
				chatMessageStatusLabel.setTextFill(Color.RED);
			}
			else
			{
				chatMessageInput.clear();
				
				chatMessageStatusLabel.setText(response.getMessage());
				chatMessageStatusLabel.setTextFill(Color.GREEN);
				
				String messages = sentMessagesTextArea.getText();
				if(messages == null || messages.equals(""))
					sentMessagesTextArea.setText(message);
				else
					sentMessagesTextArea.appendText("\n" + message);
				
				addMessageToHistory(selectedStation, receiver, message, null);
			}
		});
		
		executor.submit(tempTask);
    }
	
    @FXML
    void selectChatNotification(Event event)
    {
    	FileOrTextMessage message = messageNotifications.getSelectionModel().getSelectedItem();
    	if(message == null)
    		return;
    	
    	chatTab.setStyle("-fx-text-base-color: black;");
    	
    	messageNotifications.getItems().remove(message);
    	
    	// iterate through chatTrainstationsComboBox and find the TrainstationUsers instance that contains the user with username from FileOrTextMessage.sender
    	
    	User sender = new User(null, message.sender, null ,null);
    	
    	for(TrainstationUsers tu : chatTrainstationsComboBox.getItems())
    	{
    		for(User user : tu.users)
    		{
    			if(user.getUsername().equals(sender.getUsername()))
    			{
    				chatTrainstationsComboBox.getSelectionModel().select(tu);
    				chatUsersComboBox.getSelectionModel().select(user);
    				
    				ChatMessageHistory tmp = new ChatMessageHistory(tu, user, null, null);
    				ChatMessageHistory history = chatHistory.get(tmp);
    				
    				if(history != null)
    				{
    					sentMessagesTextArea.setText(history.getMyMessageHistory());
    					receiverTextArea.setText(history.getReceiverMessageHistory());
    				}
    			}
    		}
    	}
    }
    
    @FXML
    void chat_stationSelect(Event event)
    {
    	TrainstationUsers selectedStation = chatTrainstationsComboBox.getSelectionModel().getSelectedItem();
    	if(selectedStation == null)
    		return;
    	
    	sentMessagesTextArea.clear();
    	receiverTextArea.clear();
    	
    	chatFilesListView.getItems().clear();
    	
    	chatMessageInput.clear();
    	
    	chatUsersComboBox.getItems().clear();
    	chatUsersComboBox.getItems().addAll(selectedStation.users);
    }
    
    @FXML
    void recordTrainPass(Event event)
    {
    	int selectedIndex = trainLinesList_recordTrainPassTab.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    	{
    		recordTrainPassStatusLabel.setText("Select a line first.");
    		return;
    	}
    	
    	TrainLine selectedLine = trainLinesListViewItems.get(selectedIndex);
    	
    	String time = recordTrainPass_timeInput.getText();
    	LocalDate date = LocalDate.now();
    	
    	LocalTime localTime = null;
    	
    	try
    	{
    		localTime  = LocalTime.parse(time);
    	}
    	catch(DateTimeParseException e)
    	{
    		recordTrainPassStatusLabel.setText("Time is of incorrect format!");
    		recordTrainPassStatusLabel.setTextFill(Color.RED);
    		return;
    	}
    	
    	LocalDateTime dateTime = LocalDateTime.of(date, localTime);
    	
    	Task<String> tempTask = new Task<String>()
    	{
			@Override
			protected String call() throws Exception
			{
				return frontendController.reportTrainPass(dateTime, selectedLine);
			}
		};
    	
    	tempTask.setOnRunning((runningEvent)->
    	{
    		recordTrainPass_sendInfoButton.setDisable(true);
    	});
    	
    	tempTask.setOnSucceeded((successEvent)->
    	{
    		recordTrainPass_sendInfoButton.setDisable(false);
    		
    		String responseMessage = tempTask.getValue();
    		recordTrainPassStatusLabel.setText(responseMessage);
    	});
    	
    	executor.submit(tempTask);
    }

    @FXML
    void refreshLineSchedulesTab(Event event)
    {
    	if(recordTrainPassTab.isSelected())
    		getLineSchedules(refreshLinesList_recordTab, recordTrainPassStatusLabel);
    	else
    		getLineSchedules(refreshLineSchedulesTabButton, getLinesStatusLabel);
    }
 
    @FXML
    void trainLineSelected(Event event)
    {
    	int selectedIndex = trainLinesListView_lineSchedulesTab.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	lineStations_lineSchedulesTabItems.clear();
    	
    	TrainLine selectedLine = trainLinesListViewItems.get(selectedIndex);
    	
    	for(StationArrival arrival : selectedLine.stationArrivals)
    	{
    		String suffix = (arrival.passed == true) ? "yes" : "no";
    		String value = "station: " + arrival.trainStation.getID() + ", time of passing: " + arrival.timeOfArrival + ", arrived: " + suffix;
    		lineStations_lineSchedulesTabItems.add(value);
    	}
    }
    
    private void getLineSchedules(Button activationButton, Label infoLabel)
    {
    	// refreshLineSchedulesTabButton - original Button
    	// getLinesStatusLabel - original label
    	
    	Task<LinesOfTrainstation> tempTask = new Task<LinesOfTrainstation>() 
    	{
			@Override
			protected LinesOfTrainstation call() throws Exception
			{
				return frontendController.getLines();
			}
    	};
    	
    	tempTask.setOnRunning((runningEvent)->
    	{
    		activationButton.setDisable(true);
    	});
    	
    	tempTask.setOnSucceeded((successEvent)->
    	{
    		activationButton.setDisable(false);
    		
    		LinesOfTrainstation lines = tempTask.getValue();
    		
    		if(lines == null)
    			return;
    		
    		trainLinesListViewItems.clear();
    		for(TrainLine line : lines.linesThroughStation)
	    	{
	    		trainLinesListViewItems.add(line);
	    	}
    	});
    	
    	tempTask.setOnFailed((failEvent)->
    	{
    		activationButton.setDisable(false);
    		
    		infoLabel.setText("Error");
    		infoLabel.setTextFill(Color.RED);
    	});
    	
    	executor.submit(tempTask);
    	
    	/*Optional<LinesOfTrainstation> lines = frontendController.getLines();
    	
    	if(lines.isEmpty())
    	{
    		getLinesStatusLabel.setText("Error");
    		getLinesStatusLabel.setTextFill(Color.RED);
    		return;
    	}
    	
    	getLinesStatusLabel.setText("");
    	
    	LinesOfTrainstation linesData = lines.get();*/
    }

   


    @FXML
    void activateRecordTrainPassTab(Event event)
    {
    	if(recordTrainPassTab.isSelected() == false)
    		return;
    	
    	
    }

    
    @FXML
    void logout(Event event)
    {
    	Task<Code_response> tempTask = new Task<Code_response>()
    	{
			@Override
			protected Code_response call() throws Exception
			{
				return frontendController.logout();
			}
		};
		
		tempTask.setOnFailed((failedEvent)->
		{
			Code_response response = tempTask.getValue();
			logoutSuccessLabel.setText(response.getMessage());
			logoutSuccessLabel.setTextFill(Color.RED);
		});
		
		tempTask.setOnSucceeded((successEvent)->
		{
			Code_response response = tempTask.getValue();
			
			if(response.getCode() != 200)
			{
				logoutSuccessLabel.setText(response.getMessage());
				logoutSuccessLabel.setTextFill(Color.RED);
				return;
			}
			
			try
			{
				Thread.sleep(500);
			} 
			catch (InterruptedException e){}
			
			logoutSuccessLabel.setText(response.getMessage());
			logoutSuccessLabel.setTextFill(Color.GREEN);
			
			activateWindow(loginPanelFXMLPath, "Login", event);
		});
		
		executor.submit(tempTask);
    }

    @FXML
    void openFilePicker(Event event)
    {
    	FileChooser fileChooser = new FileChooser();
    	
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("File type:" , "*.pdf"));
    	chosenReport = fileChooser.showOpenDialog(((Node)(event.getSource())).getScene().getWindow());
    	
    	selectedFileLabel.setText(chosenReport.getAbsolutePath());
    }
    
    @FXML
    void sendReport(Event event)
    {
    	Task<Code_response> tempTask = new Task<Code_response>() 
    	{
			@Override
			protected Code_response call() throws Exception
			{
				return frontendController.uploadReport(chosenReport);
			}
    	};
    	
    	tempTask.setOnRunning((runningEvent)->
    	{
    		sendReportButton.setDisable(true);
    	});
    	
    	tempTask.setOnFailed((failedEvent)->
    	{
    		reportUploadStatusLabel.setText("Error");
    		reportUploadStatusLabel.setTextFill(Color.RED);
    		
    		sendReportButton.setDisable(false);
    	});
    	
    	tempTask.setOnSucceeded((successEvent)->
    	{
    		sendReportButton.setDisable(false);
    		
    		Code_response response = tempTask.getValue();
    		
    		if(response.getCode() / 100 != 2) // any code other than 2xx is error
    		{
    			reportUploadStatusLabel.setText(response.getMessage());
    			reportUploadStatusLabel.setTextFill(Color.RED);
    		}
    		else
    		{
    			reportUploadStatusLabel.setText(response.getMessage());
    			reportUploadStatusLabel.setTextFill(Color.GREEN);
    		}
    	});
    	
    	executor.submit(tempTask);
    }
}
