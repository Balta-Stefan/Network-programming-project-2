package mdp2021.frontend.GUI;


import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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
import mdp2021.backend.shared.Code_response;

public class EmployeePanelController extends BaseFXController
{
	private File chosenReport;

    @FXML
    private Tab recordTrainPassTab;

    @FXML
    private Tab announcementsTab;

    @FXML
    private Tab chatTab;
	
    @FXML
	private Tab lineSchedulesTab;
    
    @FXML
	private Label getLinesStatusLabel;
    
    @FXML
	private Label logoutSuccessLabel;

    @FXML
    private Label selectedFileLabel;
    
    @FXML
    private ListView<TrainLine> trainLinesListView_lineSchedulesTab;
    private ObservableList<TrainLine> trainLinesListViewItems = FXCollections.observableArrayList();
    
    @FXML
    private ListView<String> lineStations_lineSchedulesTab;
    private ObservableList<String> lineStations_lineSchedulesTabItems =  FXCollections.observableArrayList();
    
    @FXML
    private ListView<TrainLine> trainLinesList_recordTrainPassTab;
    
    @FXML
    private Button refreshLineSchedulesTabButton;
    
    @FXML
    private Button refreshLinesList_recordTab;
    
    @FXML
    private Button sendReportButton;
    
    @FXML
    private Label recordTrainPassStatusLabel;
    
    @FXML
    private Label reportUploadStatusLabel;
    
    @FXML
    private TextField recordTrainPass_timeInput;
    
    @FXML
    private Button recordTrainPass_sendInfoButton;
    
    @FXML
    private ComboBox<TrainstationUsers> chatTrainstationsComboBox;
    
    @FXML
    private ComboBox<User> chatUsersComboBox;
    
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
    void chat_stationSelect(Event event)
    {
    	System.out.println("I have selected trainstation combobox in chat");
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
    void activateAnnouncementsTab(Event event)
    {
    	if(announcementsTab.isSelected() == false)
    		return;
    	
    	
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
