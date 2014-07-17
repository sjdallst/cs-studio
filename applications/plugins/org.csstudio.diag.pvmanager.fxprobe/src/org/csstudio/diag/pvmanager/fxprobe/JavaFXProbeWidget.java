/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csstudio.diag.pvmanager.fxprobe;

import static org.epics.pvmanager.ExpressionLanguage.channel;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import static org.epics.util.time.TimeDuration.ofHertz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swt.FXCanvas;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVReaderEvent;
import org.epics.pvmanager.PVWriterEvent;
import org.epics.util.array.ListNumber;
import org.epics.util.time.TimeDuration;
import org.epics.util.time.Timestamp;
import org.epics.vtype.Alarm;
import org.epics.vtype.Display;
import org.epics.vtype.SimpleValueFormat;
import org.epics.vtype.Time;
import org.epics.vtype.VImage;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VTable;
import org.epics.vtype.ValueFormat;
import org.epics.vtype.ValueUtil;

/**
 *
 * @author sjdallst
 */
public class JavaFXProbeWidget {
    
    PV<Object , Object> pv;
    PVReader<?> formulaPV;
    private GridPane grid;
    private GridPane metaDataGrid;
    private GridPane generalInfoGrid;
    private TitledPane metaDataPane;
    private TitledPane generalInfoPane;
    Scene scene;
    Stage stage;
    private Text pvNameLabel;
    private Text pvValueLabel;
    private Text pvWriteLabel;
    private Text lastErrorLabel;
    private Text pvTimeLabel;
    private Text pvTypeLabel;
    private Text displayLimitsLabel;
    private Text alarmLimitsLabel;
    private Text warningLimitsLabel;
    private Text controlLimitsLabel;
    private Text unitLabel;
    private Text writeConnectedLabel;
    private Text connectedLabel;
    private TextField pvNameField;
    private TextField pvValueField;
    private TextField pvWriteField;
    private TextField lastErrorField;
    private TextField metadataField;
    private TextField pvTimeField;
    private TextField pvTypeField;
    private TextField displayLimitsField;
    private TextField alarmLimitsField;
    private TextField warningLimitsField;
    private TextField controlLimitsField;
    private TextField unitField;
    private TextField expressionTypeField;
    private TextField expressionNameField;
    private TextField channelHandlerField;
    private TextField usageCountField;
    private TextField connectedRWField;
    private TextField channelPropertiesField;
    private TextField writeConnectedField;
    private TextField connectedField;
    private final ValueFormat format = new SimpleValueFormat(3);
    
    private ChoiceBox<String> visualChooser;
    private boolean chooserAdded = false;
    
    private boolean showVisual = false, visualAdded = false;
    
    private boolean showMeter = false, showLineGraph = false, showImage = false, showTable = false
                    , showIntensityGraph = false;
    
    private boolean pvWriteFieldAdded = false;
    
    private boolean channelChanged = false;
    
    private HBox visualWrapper;
    private Text errorText;
    private TableView visualTable;
    private ImageView visualImageView;
    private WritableImage visualImage;
    private LineGraphApp lineGraphApp;
    private IntensityGraphApp intensityGraphApp;
    private final ArrayList<String> visualStringsArray = new ArrayList<>();
    private final ArrayList<BaseGraphApp> visualGraphArray = new ArrayList<>();
    
    LineGraphDialogue lineGraphDialogue;
    
    IntensityGraphDialogue intensityGraphDialogue;
    
    private Button visualConfigButton;
    
    // Next secondary view ID, i.e. next instance of probe should use this number.
 	// SYNC on JavaFXProbe.class for access
 	private static int next_instance = 1;
 	
 // The ID of the view as specified by the extension point
 	public static final String VIEW_ID = "org.csstudio.diag.pvmanager.fxprobe"; //$NON-NLS-1$
    
    
    public void start(){
        this.start(new Stage());
    }
    
    public void start(String pvName){
        pvNameField.setText(pvName);
        this.start(new Stage());
    }
    
    public void start(Stage primaryStage) {
        
        this.stage = primaryStage;
        
        //instantiate grid, set actions for fields, add components to grid.
        initComponents();
        
        scene.setFill(Paint.valueOf("lightGray"));
       
        if(pvNameField.getText().compareTo("") != 0) {
            setupPV(pvNameField.getText());
        }
    }

    
    /**
     * Sets the textfield labeled "Value:" to the string representation of the
     * current value being read by pv
     */
    private void setTextValue(String value) {
        if (value == null) {
            Platform.runLater(() -> {
                pvValueField.setText("");
            });
        } else {
            final String value1 = value;
            Platform.runLater(() -> {
                pvValueField.setText(value1);
            });
        }
    }

    /**
     * sets the textfield labeled "Type:" to the type of the current value
     * being read by pv..
     * @param type 
     */
    private void setType(Class type) {
        if (type == null) {
            Platform.runLater(() -> {
                pvTypeField.setText("");
            });
        } else {
            final String simpleName = type.getSimpleName();
            Platform.runLater(() -> {
                pvTypeField.setText(simpleName);
            });
        }
    }

    /**
     * Right now does nothing, functionality might be implemented in set metadata.
     * @param alarm 
     */
    //TODO: either implement this so it does something or get rid of it.
    private void setAlarm(Alarm alarm) {
        
    }

    /**
     * Updates the textfield labeled "Time:" with the latest time value read from pv
     * @param time 
     */
    private void setTime(Time time) {
        if (time == null) {
            Platform.runLater(() -> {
                pvTimeField.setText("");
            });
        } else {
            final String timeString = time.getTimestamp().toDate().toString();
            Platform.runLater(() -> {
                pvTimeField.setText(timeString);
            });
        }
    }

    /**
     * updates the textfields labeled "Display Limits:", "Alarm Limits:", "Warning Limits:",
     * "Control Limits:", and "Unit:" based on values taken from the last pv update
     * @param display 
     */
    private void setMetadata(Display display) {
        if (display == null) {
            Platform.runLater(() -> {
                metadataField.setText("");
            });
        } else {
            final String displayLimits = display.getUpperDisplayLimit() + " - " + display.getLowerDisplayLimit();
            final String alarmLimits = display.getUpperAlarmLimit() + " - " + display.getLowerAlarmLimit();
            final String warningLimits = display.getUpperWarningLimit() + " - " + display.getLowerWarningLimit();
            final String controlLimits = display.getUpperCtrlLimit() + " - " + display.getLowerCtrlLimit();
            final String unit = display.getUnits();
            
            Platform.runLater(() -> {
                displayLimitsField.setText(displayLimits);
                alarmLimitsField.setText(alarmLimits);
                warningLimitsField.setText(warningLimits);
                controlLimitsField.setText(controlLimits);
                unitField.setText(unit);
            });
        }
    }

    /**
     * updates the text field labeled "Last Error:" with the last error that occurred
     * while attempting to read from a channel, the text field remains blank if no error has occurred.
     * @param ex 
     */
    private void setLastError(Exception ex) {
        if (ex != null) {
            final String message = ex.getClass().getSimpleName() + " " + ex.getMessage();
            Platform.runLater(() -> {
                lastErrorField.setText(message);
            }); 
        } else {
        }
    }

    /**
     * updates textfield labeled "Connected:" indicating whether a read connection
     * exists.
     * @param connected 
     */
    private void setConnected(Boolean connected) {
        if (connected != null) {
            final String connectedString = connected.toString();
            Platform.runLater(() -> {
                connectedField.setText(connectedString);
            });
        } else {
            Platform.runLater(() -> {
                connectedField.setText("");
            });
        }
    }

    /**
     * updates field labeled "Write Connected:" indicating whether or not a 
     * write channel exists.
     * @param connected 
     */
    private void setWriteConnected(Boolean connected) {
        if (connected != null) {
            final boolean connected1 = connected;
            final String connectedString = connected.toString();
            Platform.runLater(() -> {
                writeConnectedField.setText(connectedString);
                if(connected && !pvWriteFieldAdded){
                    grid.addRow(3, pvWriteLabel, pvWriteField);
                    pvWriteFieldAdded = true;
                }
                pvWriteField.setEditable(connected);
            });
        } else {
            Platform.runLater(() -> {
                writeConnectedField.setText("");
            });
        }
    }
    
    /**
     * Method responsible for setting up the visual that the user has chosen to see.
     * Only certain visuals are possible for given types of data
     * - VNumber - meter
     * - VNumberArray - LineGraph, IntensityGraph
     * - VTable - tableView
     * - VImage - image
     * @param value 
     */
    private void setVisual(Object value){
        
        final Object value1 = value;
        
        if(value instanceof VNumber){
            
        }
        
        if(value instanceof VNumberArray){
            
            if(showLineGraph){
                showGraph((VNumberArray) value, lineGraphApp);
            }
            
            if(showIntensityGraph){
                showGraph((VNumberArray) value, intensityGraphApp);
            }
            
        }
        
        if(value instanceof VTable){
            if(showTable){
                final VTable value2 = (VTable)value;
                Platform.runLater(() -> {
                    VTable table = (VTable)value2;
                    //make an array of table columns.
                    TableColumn<Map, String> [] tableColumns = new TableColumn[table.getColumnCount()];
                    //set the name and cell factory for each column
                    for(int i = 0; i < table.getColumnCount(); i++) {
                        tableColumns[i] = new TableColumn(table.getColumnName(i));
                        tableColumns[i].setCellValueFactory(new MapValueFactory(table.getColumnName(i)));
                    }
                    
                    //create table
                    visualTable = new TableView<>(generateDataInMap(table));

                    //safely add all columns to table
                    visualTable.getColumns().clear();
                    visualTable.getColumns().addAll(tableColumns);
                    
                    //set columns to fit to the current width of the table unless
                    //the user changes them.
                    visualTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                    
                    //create a cell factory that will work for the columns we have set up.
                    Callback<TableColumn<Map, String>, TableCell<Map, String>>
                        cellFactoryForMap = new Callback<TableColumn<Map, String>,
                            TableCell<Map, String>>() {
                                @Override
                                public TableCell call(TableColumn p) {
                                    return new TextFieldTableCell(new StringConverter() {
                                        @Override
                                        public String toString(Object t) {
                                            return t.toString();
                                        }
                                        @Override
                                        public Object fromString(String string) {
                                            return string;
                                        }                                    
                                    });
                                }
                    };
                    
                    //set the cell factory for each column to the cell factory we just made.
                    for(int i = 0; i < visualTable.getColumns().size(); i++){
                        ((TableColumn<Map, String>)visualTable.getColumns().get(i)).setCellFactory(cellFactoryForMap);
                    }
                    
                    //make it so table grows when user stretches screen.
                    visualTable.maxWidth(Double.MAX_VALUE);
                    visualWrapper.maxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(visualTable, Priority.ALWAYS);
                    
                    //update table.
                    if(visualAdded){
                        visualWrapper.getChildren().remove(visualWrapper.getChildren().size()-1);
                        visualWrapper.getChildren().add(visualTable);
                    }
                    //add the table to the grid if it has not been already.
                    if(!visualAdded) {
                        visualWrapper.getChildren().add(visualTable);
                        grid.add(visualWrapper, 0, 5, 2, 2);
                        visualAdded = true;  
                        grid.getRowConstraints().get(5).setMaxHeight(Double.MAX_VALUE);
                        grid.getRowConstraints().get(5).setVgrow(Priority.ALWAYS);
                    }
                });    
            }
        }
        
        if(value instanceof VImage){
            if(showImage){
                final VImage value3 = (VImage)value;
                Platform.runLater(() -> {
                    drawByteArray(value3.getData(), value3.getWidth(), value3.getHeight());
                    if(!visualAdded){
                        visualWrapper.getChildren().add(visualImageView);
                        grid.add(visualWrapper, 0, 4, 2, 2);
                        visualAdded = true;
                        grid.getRowConstraints().get(4).setMaxHeight(Double.MAX_VALUE);
                        grid.getRowConstraints().get(4).setVgrow(Priority.ALWAYS);
                    }
                });
            }
        }
         
    }
    
    /**
     * Sets the visual options available to the user based on the data given.
     * Different datasets have different options.
     * - VNumber - meter
     * - VNumberArray - LineGraph, IntensityGraph
     * - VTable - tableView
     * - VImage - image 
     * @param value 
     */
    private void setChoiceBox(Object value){
        
        if(value instanceof VNumber) {
            
        }
        
        if(value instanceof VNumberArray) {
            visualStringsArray.add("Hide");
            
            //decide which graphs the user should be able to choose by seeing
            //which ones can be drawn without error.
            try {
                lineGraphApp.render((VNumberArray)value, 100, 100);
                visualStringsArray.add("Line Graph");
                visualGraphArray.add(lineGraphApp);
            }
            catch(Throwable e){
                
            }
            
            try {
                intensityGraphApp.render((VNumberArray)value, 100, 100);
                visualStringsArray.add("Intensity Graph");
                visualGraphArray.add(intensityGraphApp);
            }
            catch(Throwable e){
                
            }
            
            Platform.runLater(() -> {
                visualChooser.setItems(FXCollections.observableArrayList(visualStringsArray));
                visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                                Number oldValue, Number newValue) -> {
                            switch(newValue.intValue()){
                                case 0:
                                    hideVisual();
                                    break;
                                case 1:
                                    setupGraph(visualGraphArray.get(0));
                                    break;
                                case 2:
                                    setupGraph(visualGraphArray.get(1));
                                    break;
                            }
                        });
                visualChooser.getSelectionModel().selectFirst();
                grid.add(visualChooser, 0, 4);
                chooserAdded = true;
            });
        }
        
        if(value instanceof VTable) {
            Platform.runLater(() -> {
                visualChooser.setItems(FXCollections.observableArrayList(
                        "Hide", "Table"));
                visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                                Number oldValue, Number newValue) -> {
                            switch(newValue.intValue()){
                                case 0:
                                    hideVisual();
                                    break;
                                case 1:
                                    swapVisual();
                                    showVisual = true;
                                    showTable = true;
                                    break;
                            }
                        });
                visualChooser.getSelectionModel().selectFirst();
                grid.add(visualChooser, 0, 4, 2, 1);
                chooserAdded = true;
            });
        }
        
        if(value instanceof VImage) {
            Platform.runLater(() -> {
                visualChooser.setItems(FXCollections.observableArrayList(
                        "Hide", "Image"));
                visualChooser.getSelectionModel().selectedIndexProperty().addListener(
                        (ObservableValue<? extends Number> ov,
                                Number oldValue, Number newValue) -> {
                            switch(newValue.intValue()){
                                case 0:
                                    hideVisual();
                                    break;
                                case 1:
                                    swapVisual();
                                    showVisual = true;
                                    showImage = true;
                                    break;
                            }
                        });
                visualChooser.getSelectionModel().selectFirst();
                grid.add(visualChooser, 0, 4, 2, 1);
                chooserAdded = true;
            });
        }
        
    }
    
    /**
     * Changes the image held in visualImageView by setting it to the writable
     * image which is written to from the given byte array "pixels".
     * @param pixels
     * @param width
     * @param height 
     */
    private void drawByteArray(byte[] pixels, int width, int height) {
        visualImage = new WritableImage(width, height); //Clear to write new image freely
        PixelWriter writer = visualImage.getPixelWriter();
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                int argb = 0;
                argb += (pixels[i*width*3 + 3*j + 0] & 0xFF);
                argb += (pixels[i*width*3 + 3*j + 1] & 0xFF) << 8;
                argb += (pixels[i*width*3 + 3*j + 2] & 0xFF) << 16;
                argb += 0xFF << 24;
                writer.setArgb(j, i, argb);
            }
        }
        visualImageView.setImage(visualImage);
    }
    
    /**
     * Calls swapVisual, then shrinks the space that the visual took.
     */
    private void hideVisual(){
        swapVisual();
        grid.getRowConstraints().get(5).setVgrow(Priority.NEVER);
    }
    
    /**
     * Removes any visuals that have been added to the application and resets any
     * booleans that are associated with them. Also closes any dialogues that might be open.
     */
    private void swapVisual() {
        showVisual = visualAdded = false;
        
        showMeter = showLineGraph = showImage = showTable = showIntensityGraph = false;
        
        lineGraphApp = new LineGraphApp();
        intensityGraphApp = new IntensityGraphApp();
        
        
        if(grid.getChildren().contains(visualWrapper)){
            grid.getChildren().remove(visualWrapper);
        }
        
        if(grid.getChildren().contains(errorText)) {
            grid.getChildren().remove(errorText);
        }
        
        if(grid.getChildren().contains(visualConfigButton)){
            grid.getChildren().remove(visualConfigButton);
        }
        
        if(grid.getChildren().contains(pvWriteField) && grid.getChildren().contains(pvWriteLabel)){
            grid.getChildren().removeAll(pvWriteField, pvWriteLabel);
        }
        
        while(visualWrapper.getChildren().size() != 0) {
            visualWrapper.getChildren().remove(visualWrapper.getChildren().size() - 1);
        }
        
        closeDialogues();
    }
    
    /**
     * Resets all text fields to blank text (except pvNameField)
     */
    private void clearFields(){
        pvValueField.setText("");
        pvWriteField.setText("");
        lastErrorField.setText("");
        metadataField.setText("");
        pvTimeField.setText("");
        pvTypeField.setText("");
        displayLimitsField.setText("");
        alarmLimitsField.setText("");
        warningLimitsField.setText("");
        controlLimitsField.setText("");
        unitField.setText("");
        expressionTypeField.setText("");
        expressionNameField.setText("");
        channelHandlerField.setText("");
        usageCountField.setText("");
        connectedRWField.setText("");
        channelPropertiesField.setText("");
        writeConnectedField.setText("");
        connectedField.setText("");
        
        //if the field for the user to write values is present it should be removed
        //as there is no guarentee that the user will be able to use it when the next
        //channel is opened.
        if(grid.getChildren().contains(pvWriteField)){
            grid.getChildren().remove(pvWriteField);
        }
        pvWriteFieldAdded = false;
        pvWriteField.setEditable(true);
        
    }
    
    /**
     * Removes the choicebox from the grid if it is present, and resets any variables
     * that are associated with it.
     */
    private void resetChoiceBox(){
        
        if(grid.getChildren().contains(visualChooser)){
            grid.getChildren().remove(visualChooser);
        }
        
        visualStringsArray.clear();
        visualGraphArray.clear();
        
        visualChooser = new ChoiceBox<String>();
        chooserAdded = false;
    }
    
    /**
     * Sets onAction for various static components as well as sets the styling
     * for various components (such as the various grids in the application).
     */
    private void initComponents(){
    	grid = new GridPane();
        metaDataGrid = new GridPane();
        generalInfoGrid = new GridPane();
        metaDataPane = new TitledPane();
        generalInfoPane = new TitledPane();
        scene = new Scene(grid);
        pvNameLabel = new Text("PV Name: ");
        pvValueLabel = new Text("Value: ");
        pvWriteLabel = new Text("Write Value: ");
        lastErrorLabel = new Text("Last Error: ");
        pvTimeLabel = new Text("Time: ");
        pvTypeLabel = new Text("Type: ");
        displayLimitsLabel = new Text("Display Limits: ");
        alarmLimitsLabel = new Text("Alarm Limits: ");
        warningLimitsLabel = new Text("Warning Limits: ");
        controlLimitsLabel = new Text("Control Limits: ");
        unitLabel = new Text("Unit: ");
        writeConnectedLabel = new Text("Write Connected: ");
        connectedLabel = new Text("Connected: ");
        pvNameField = new TextField();
        pvValueField = new TextField();
        pvWriteField = new TextField();
        lastErrorField = new TextField();
        metadataField = new TextField();
        pvTimeField = new TextField();
        pvTypeField = new TextField();
        displayLimitsField = new TextField();
        alarmLimitsField = new TextField();
        warningLimitsField = new TextField();
        controlLimitsField = new TextField();
        unitField = new TextField();
        expressionTypeField = new TextField();
        expressionNameField = new TextField();
        channelHandlerField = new TextField();
        usageCountField = new TextField();
        connectedRWField = new TextField();
        channelPropertiesField = new TextField();
        writeConnectedField = new TextField();
        connectedField = new TextField();
        
        visualChooser = new ChoiceBox<String>();
        chooserAdded = false;
        
        visualWrapper = new HBox();
        errorText = new Text();
        visualImageView = new ImageView();
        visualImage = new WritableImage(100, 100);
        lineGraphApp = new LineGraphApp();
        intensityGraphApp = new IntensityGraphApp();
        
        lineGraphDialogue = new LineGraphDialogue();
        
        intensityGraphDialogue = new IntensityGraphDialogue();
        
        visualConfigButton = new Button("Configure");
        pvNameField.setOnAction((ActionEvent event) -> {
            setupPV(pvNameField.getText());
        });
        
        pvWriteField.setOnAction((ActionEvent event) -> {
            if(pv.isWriteConnected()){
                pv.write(pvWriteField.getText());
            }
        });
        
        pvValueField.setEditable(false);
        lastErrorField.setEditable(false);
        metadataField.setEditable(false);
        pvTimeField.setEditable(false);
        pvTypeField.setEditable(false);
        displayLimitsField.setEditable(false);
        alarmLimitsField.setEditable(false);
        warningLimitsField.setEditable(false);
        controlLimitsField.setEditable(false);
        unitField.setEditable(false);
        expressionTypeField.setEditable(false);
        expressionNameField.setEditable(false);
        channelHandlerField.setEditable(false);
        usageCountField.setEditable(false);
        connectedRWField.setEditable(false);
        channelPropertiesField.setEditable(false);
        writeConnectedField.setEditable(false);
        connectedField.setEditable(false);
      
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        metaDataGrid.setAlignment(Pos.TOP_LEFT);
        metaDataGrid.setHgap(10);
        metaDataGrid.setVgap(10);
        metaDataGrid.setPadding(new Insets(10, 25, 10, 25));
        
        generalInfoGrid.setAlignment(Pos.TOP_LEFT);
        generalInfoGrid.setHgap(10);
        generalInfoGrid.setVgap(10);
        generalInfoGrid.setPadding(new Insets(10, 25, 10, 25));
        
        Separator seperator1 = new Separator();
        
        visualWrapper.setAlignment(Pos.CENTER);
        
        grid.addRow(0, pvNameLabel, pvNameField);
        grid.add(seperator1, 0, 1, 2, 1);
        grid.addRow(2, pvValueLabel, pvValueField);
        grid.addRow(7, pvTimeLabel, pvTimeField);
        grid.add(metaDataPane, 0, 8, 2, 1);
        grid.add(generalInfoPane, 0, 9, 2, 1);

        
        metaDataGrid.addRow(0, pvTypeLabel, pvTypeField);
        metaDataGrid.addRow(1, displayLimitsLabel, displayLimitsField);
        metaDataGrid.addRow(2, alarmLimitsLabel, alarmLimitsField);
        metaDataGrid.addRow(3, warningLimitsLabel, warningLimitsField);
        metaDataGrid.addRow(4, controlLimitsLabel, controlLimitsField);
        metaDataGrid.addRow(5, unitLabel, unitField);
        
        generalInfoGrid.addRow(0, lastErrorLabel, lastErrorField);
        generalInfoGrid.addRow(1, writeConnectedLabel, writeConnectedField);
        generalInfoGrid.addRow(2, connectedLabel, connectedField);
        
        metaDataPane.setText("Metadata");
        metaDataPane.setContent(metaDataGrid);
        
        generalInfoPane.setText("General Information");
        generalInfoPane.setContent(generalInfoGrid);
        
        ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
        ColumnConstraints column2 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS); //column2 contains textfields, this causes the textfields to grow with the window.
        grid.getColumnConstraints().addAll(column1, column2);
        metaDataGrid.getColumnConstraints().addAll(column1, column2);
        generalInfoGrid.getColumnConstraints().addAll(column1, column2);
        
        for(int i = 0; i < 10; i++){
            grid.getRowConstraints().add(new RowConstraints());
        }
        
    }
    
    /**
     * Clears previous visual if it exists, then adds a config button and sets
     * the boolean to draw a linegraph on the next pvAction to true.
     */
    private void setupLineGraph() {
        swapVisual();
        visualConfigButton = new Button("Configure");
        visualConfigButton.setOnAction((ActionEvent event) -> {
            lineGraphDialogue.start(lineGraphApp);
        });
        grid.add(visualConfigButton, 1, 4);
        showVisual = true;
        showLineGraph = true;
    }
    
    /**
     * Clears previous visual if it exists, then adds a config button and sets
     * the boolean to draw an intensity graph on the next pvAction to true.
     */
    private void setupIntensityGraph() {
        swapVisual();
        visualConfigButton = new Button("Configure");
        visualConfigButton.setOnAction((ActionEvent event) -> {
            intensityGraphDialogue.start(intensityGraphApp);
        });
        grid.add(visualConfigButton, 1, 4);
        showVisual = true;
        showIntensityGraph = true;
    }
    
    /**
     * Decides which setup function to call based on what the instance of graph is.
     * @param graph 
     */
    private void setupGraph(BaseGraphApp graph){
        if(graph instanceof LineGraphApp){
            setupLineGraph();
        }
        if (graph instanceof IntensityGraphApp) {
            setupIntensityGraph();
        }
    }
    
    /**
     * Calculates the size of the graph to be drawn then uses the render function
     * from graph to draw to an image of said size.
     * @param value
     * @param graph 
     */
    private void showGraph(VNumberArray value, BaseGraphApp graph) {
        
        /**
        *Graph height is calculated by subtracting the total height of the all of the currently visible
        *text fields from the height of the grid (which is equal to the distance between the top and bottom
        *of the window border.)
        */
        int graphHeight;
        if(metaDataPane.isExpanded() && generalInfoPane.isExpanded()){ 
            graphHeight = (int)(grid.getHeight() - (17*10 + 16*pvNameField.getHeight() + 50));
        } 
        else if(metaDataPane.isExpanded()) {
            graphHeight = (int)(grid.getHeight() - (14*10 + 13*pvNameField.getHeight() + 50));
        } 
        else if(generalInfoPane.isExpanded()) {
            graphHeight = (int)(grid.getHeight() - (11*10 + 10*pvNameField.getHeight() + 50));
        } 
        else {
            graphHeight = (int)(grid.getHeight() - (7*10 + 6*pvNameField.getHeight() + 50));
        }
        
        final int graphHeightFinal = Math.max(60, graphHeight);
        
        final int graphWidthFinal = Math.max(60, (int)(grid.getWidth() - 50));
        
        final byte[] pixels = graph.render(value, graphWidthFinal, graphHeightFinal);

        Platform.runLater(() -> {
            drawByteArray(pixels, graphWidthFinal, graphHeightFinal);
            if(!visualAdded) {
                visualWrapper.getChildren().add(visualImageView);
                grid.add(visualWrapper, 0, 5, 2, 2);
                grid.getRowConstraints().get(5).setMaxHeight(Double.MAX_VALUE);
                grid.getRowConstraints().get(5).setVgrow(Priority.ALWAYS);
                visualAdded = true;
            }
        });
    }
    
    /**
     * Closes any dialogues that might be open.
     */
    private void closeDialogues(){
        lineGraphDialogue.close();
        intensityGraphDialogue.close();
    }
    
    /**
     * Makes a an observable list of maps where each map corresponds to a row.
     * @param table
     * @return 
     */
    private ObservableList<Map> generateDataInMap(VTable table){
        ObservableList<Map> allData = FXCollections.observableArrayList();
        for(int i = 0; i < table.getRowCount(); i++) {
            Map<String, String> row = new HashMap<>();
            for(int j = 0; j < table.getColumnCount(); j++) {
                if(table.getColumnData(j) instanceof List) {
                    if(i < ((List)(table.getColumnData(j))).size()) {
                        if(((List)(table.getColumnData(j))).get(i) instanceof Timestamp) {
                            row.put(table.getColumnName(j), ValueUtil.getDefaultTimestampFormat().format(((List)(table.getColumnData(j))).get(i)));
                        }
                        else {
                            row.put(table.getColumnName(j), ((List)(table.getColumnData(j))).get(i).toString());
                        }
                    }
                    else {
                        row.put(table.getColumnName(j) ,"");
                    }
                }
                else {
                    if(i < ((ListNumber)(table.getColumnData(j))).size()) {
                        row.put(table.getColumnName(j), ((ListNumber)(table.getColumnData(j))).getDouble(i) + "");
                    }
                    else {
                        row.put(table.getColumnName(j), "");
                    }
                }
            }
            allData.add(row); // add each row to allData
        }
        return allData;
    }
    
    /**
     * Creates a pv that will read and/or write from the channel specified by pvName.
     * @param pvName 
     */
    private void setupPV(String pvName) {

        if (pv != null || formulaPV != null) {

            if(pv != null){
                pv.close();
            }

            if(formulaPV != null) {
                formulaPV.close();
            }

            channelChanged = true;

            hideVisual();
            resetChoiceBox();
            clearFields();
            closeDialogues();
        }

        //attempt to set up a channel from user input
        try {
            if(pvName.startsWith("=")){
                formulaPV = PVManager.read(formula(pvName))
                        .timeout(TimeDuration.ofSeconds(5))
                        .readListener((PVReaderEvent<Object> event1) -> {
                            if(!channelChanged) {
                                setLastError(formulaPV.lastException());
                                Object value = formulaPV.getValue();
                                setTextValue(format.format(value));
                                setType(ValueUtil.typeOf(value));
                                setTime(ValueUtil.timeOf(value));
                                setMetadata(ValueUtil.displayOf(value));
                                setAlarm(ValueUtil.alarmOf(value));
                                setConnected(formulaPV.isConnected());
                                if(value != null && !chooserAdded){
                                    setChoiceBox(value);
                                }
                                if(showVisual && (value != null)){
                                    setVisual(value);
                                }
                            }
                            else {
                                channelChanged = false;
                            }
                })
                        .maxRate(ofHertz(10));
            }
            else {
                pv = PVManager.readAndWrite(channel(pvName))
                        .timeout(TimeDuration.ofSeconds(5))
                        .readListener((PVReaderEvent<Object> event1) -> {
                            if(!channelChanged) {
                                setLastError(pv.lastException());
                                Object value = pv.getValue();
                                setTextValue(format.format(value));
                                setType(ValueUtil.typeOf(value));
                                setTime(ValueUtil.timeOf(value));
                                setMetadata(ValueUtil.displayOf(value));
                                setAlarm(ValueUtil.alarmOf(value));
                                setConnected(pv.isConnected());
                                if(value != null && !chooserAdded){
                                    chooserAdded = true;
                                    setChoiceBox(value);
                                }
                                if(showVisual && (value != null)){
                                    setVisual(value);
                                }
                            }
                            else {
                                channelChanged = false;
                            }
                        })
                        .writeListener((PVWriterEvent<Object> event1) -> {
                            setWriteConnected(pv.isWriteConnected());
                        })
                        .asynchWriteAndMaxReadRate(ofHertz(10));
            }
        }
        catch (RuntimeException ex) { //if an error is thrown, update the associated textfield
            setLastError(ex);
        }
    }

    public void setPVName(String pVName){
    	pvNameField.setText(pVName);
    	setupPV(pVName);
    }
    
    public static String createNewInstance() {
		synchronized (JavaFXProbeWidget.class)
		{
			return Integer.toString(next_instance++);
		}
	}
    
    public Scene getScene(){
    	return scene;
    }
    
    public void close() {
    	if(pv != null) {
    		pv.close();
    	}
    	
    	if(formulaPV != null) {
    		formulaPV.close();
    	}
    }
}


