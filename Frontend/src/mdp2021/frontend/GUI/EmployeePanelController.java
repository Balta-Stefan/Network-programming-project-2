package mdp2021.frontend.GUI;


import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainLine;
import mdp2021.backend.shared.Code_response;

public class EmployeePanelController extends BaseFXController
{

    @FXML
    private Tab recordTrainPassTab;

    @FXML
    private Tab announcementsTab;

    @FXML
    private Tab chatTab;
	
    @FXML
	private Label getLinesStatusLabel;
    
    @FXML
	private Label logoutSuccessLabel;

    @FXML
    private ListView<TrainLine> trainLinesList_lineSchedulesTab;
    private ObservableList<TrainLine> trainLinesList_lineSchedulesTabItems;
    
    @FXML
    private ListView<StationArrival> lineStations_lineSchedulesTab;
    private ObservableList<StationArrival> lineStations_lineSchedulesTabItems;
    
    @FXML
    private Button refreshLineSchedulesTabButton;
    
    public void initialize()
    {
    	trainLinesList_lineSchedulesTabItems = trainLinesList_lineSchedulesTab.getItems();
    	lineStations_lineSchedulesTabItems = lineStations_lineSchedulesTab.getItems();
    }
	

    @FXML
    void refreshLineSchedulesTab(Event event)
    {
    	getLineSchedules();
    }
 
    @FXML
    void trainLineSelected(Event event)
    {
    	int selectedIndex = trainLinesList_lineSchedulesTab.getSelectionModel().getSelectedIndex();
    	if(selectedIndex == -1)
    		return;
    	
    	lineStations_lineSchedulesTabItems.clear();
    	
    	TrainLine selectedLine = trainLinesList_lineSchedulesTabItems.get(selectedIndex);
    	
    	for(StationArrival arrival : selectedLine.stationArrivals)
    	{
    		lineStations_lineSchedulesTabItems.add(arrival);
    	}
    }
    
    private void getLineSchedules()
    {
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
    		refreshLineSchedulesTabButton.setDisable(true);
    	});
    	
    	tempTask.setOnSucceeded((successEvent)->
    	{
    		refreshLineSchedulesTabButton.setDisable(false);
    		
    		LinesOfTrainstation lines = tempTask.getValue();
    		trainLinesList_lineSchedulesTabItems.clear();
    		for(TrainLine line : lines.linesThroughStation)
	    	{
	    		trainLinesList_lineSchedulesTabItems.add(line);
	    	}
    	});
    	
    	tempTask.setOnFailed((failEvent)->
    	{
    		refreshLineSchedulesTabButton.setDisable(false);
    		
    		getLinesStatusLabel.setText("Error");
    		getLinesStatusLabel.setTextFill(Color.RED);
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
    void activateChatTab(Event event) 
    {
    	if(chatTab.isSelected() == false)
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

}
