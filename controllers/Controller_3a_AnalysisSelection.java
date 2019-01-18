package controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import controllers.Controller_3e_ViewJetQuiverMergeSingle.IncrementHandler;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Group;
import model.Groups;
import model.PackageData;

public class Controller_3a_AnalysisSelection implements Initializable {
//	private static Parent root;
	private PackageData main_package;
    private ObservableList<String> doneFiles = FXCollections.observableArrayList();
	
    @FXML
    private ListView<String> groupsListView;

    @FXML
    private Button cmdNext;

    @FXML
    private Button cmdBack;

    @FXML
    void handleMenuNewImage(ActionEvent event) throws IOException{
    	Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
		URL url = getClass().getResource("FXML_2b_ImagesNew.fxml");
		FileReader.chooseSourceDirectory(primaryStage, url, main_package);
    }
    
    @FXML
    void handleMenuNewVideo(ActionEvent event) throws IOException{
    	Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
		URL url = getClass().getResource("FXML_2b_ImagesNew.fxml");
    	FileReader.chooseVideoFiles(primaryStage, url, main_package);
    }

    @FXML
    void handleClose(ActionEvent event){
    	Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
    	primaryStage.close();
    }
    
    @FXML
    void handleReinitialize(ActionEvent event) throws IOException, ClassNotFoundException{
    	Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
    	double prior_X = primaryStage.getX();
    	double prior_Y = primaryStage.getY();
    	
		URL url = getClass().getResource("FXML_1_InitialScreen.fxml");
    	FXMLLoader fxmlloader = new FXMLLoader();
    	fxmlloader.setLocation(url);
    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root;
    	root = fxmlloader.load();
//    	Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
		((Controller_1_InitialScreen)fxmlloader.getController()).setContext(new PackageData());
		primaryStage.setTitle("Image Optical Flow");
//		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setX(prior_X);
		primaryStage.setY(prior_Y);
    }
    
    @FXML
    void handleAbout(ActionEvent event) throws IOException{
    	Stage stage = new Stage();
    	Parent root = FXMLLoader.load(getClass().getResource("FXML_About.fxml"));
    	stage.setScene(new Scene(root));
    	stage.setTitle("Image Optical Flow");
		stage.initModality(Modality.APPLICATION_MODAL);
		//stage.initOwner(((Node)event.getSource()).getScene().getWindow());
    	stage.show();
    }
    
    @FXML
    void back(ActionEvent event) throws IOException {
    	Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
    	double prior_X = primaryStage.getX();
    	double prior_Y = primaryStage.getY();
    	
		URL url = getClass().getResource("FXML_1_InitialScreen.fxml");
    	FXMLLoader fxmlloader = new FXMLLoader();
    	fxmlloader.setLocation(url);
    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root;
    	root = fxmlloader.load();
//    	Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
    	
		//((Controller_1_InitialScreen)fxmlloader.getController()).setContext(exec, queuedItems, doneItems);
		//Pane root = FXMLLoader.load(getClass().getResource("FXML_1_InitialScreen.fxml"));
		//Scene scene = new Scene(root, 600, 400);
		primaryStage.setTitle("Image Optical Flow - Welcome");
//		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setX(prior_X);
		primaryStage.setY(prior_Y);

    }

    @FXML
    void navigateNextPage(ActionEvent event) throws IOException, ClassNotFoundException {
    	validateSelection();
    }

	private double fps_value = 1.0;
	private double pixel_value = 0.2375;
    
	private void validateSelection() throws IOException, ClassNotFoundException {
		if (groupsListView.getSelectionModel().getSelectedItem() != null) {
			
	    	Spinner<Double> txtFPS = new Spinner<Double>();
	    	txtFPS.setValueFactory(facGen(1.0, 10000.0, 200.0, 1.0));
	    	txtFPS.setEditable(true);
			IncrementHandler handler_9 = new IncrementHandler();
			txtFPS.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_9);
			txtFPS.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
		        Node node = evt.getPickResult().getIntersectedNode();
		        if (node.getStyleClass().contains("increment-arrow-button") ||
		            node.getStyleClass().contains("decrement-arrow-button")) {
		                if (evt.getButton() == MouseButton.PRIMARY) {
		                	handler_9.stop();
		                }
		        }
		    });
			txtFPS.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
				try {
					fps_value = Double.valueOf(newValue);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
		    });
			txtFPS.focusedProperty().addListener((obs, oldValue, newValue) -> {
		        if (newValue == false) {
		        	txtFPS.increment(0);
		        } 
		    });
			
			Spinner<Double> txtPixels = new Spinner<Double>();
			txtPixels.setValueFactory(facGen(0.001, 10000.0, 0.02375, 0.001));
			txtPixels.setEditable(true);
			IncrementHandler handler3 = new IncrementHandler();
			txtPixels.addEventFilter(MouseEvent.MOUSE_PRESSED, handler3);
			txtPixels.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
		        Node node = evt.getPickResult().getIntersectedNode();
		        if (node.getStyleClass().contains("increment-arrow-button") ||
		            node.getStyleClass().contains("decrement-arrow-button")) {
		                if (evt.getButton() == MouseButton.PRIMARY) {
		                    handler3.stop();
		                }
		        }
		    });
			txtPixels.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
				try {
					pixel_value = Double.valueOf(newValue);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
		    });
			txtPixels.focusedProperty().addListener((obs, oldValue, newValue) -> {
		        if (newValue == false) {
		        	txtPixels.increment(0);
		        } 
			});
			
			Label label1 = new Label();
			label1.setText("Frames Per Second:");
			Label label2 = new Label();
			label2.setText("Pixel Value:");
			
	    	Dialog<Boolean> dialogMicroscope = new Dialog<>();
	    	dialogMicroscope.initModality(Modality.APPLICATION_MODAL);
	    	dialogMicroscope.initOwner(null); //bug do refactor
	    	dialogMicroscope.setHeaderText("Set Microscope Settings:");
	    	dialogMicroscope.setTitle("Microscope Settings");
	    	dialogMicroscope.setResizable(true);
	    	GridPane grid = new GridPane();
	    	grid.add(label1, 1, 1);
	    	grid.add(txtFPS, 2, 1);
	    	grid.add(label2, 1, 2);
	    	grid.add(txtPixels, 2, 2);
	    	dialogMicroscope.getDialogPane().setContent(grid);
	    	ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
	    	dialogMicroscope.getDialogPane().getButtonTypes().add(buttonTypeOk);
	    	dialogMicroscope.show();
	    	
	    	dialogMicroscope.setOnCloseRequest(event ->
	        {
	        	if(dialogMicroscope.getResult() == null) return;
	        	//else System.out.println("FALSE");
				Double value = txtFPS.getValue();
				if (value < 0) {
					JOptionPane.showMessageDialog(null, "FPS value must be a real number above 0.","Warning",JOptionPane.WARNING_MESSAGE);
					return;
				}
				value = txtPixels.getValue();
				if (value < 0) {
					JOptionPane.showMessageDialog(null, "Pixel Size value must be a real number.","Warning",JOptionPane.WARNING_MESSAGE);
			        return;
			    }
			
			
				String selecteditem = groupsListView.getSelectionModel().getSelectedItem();
				Stage primaryStage = (Stage) cmdNext.getScene().getWindow();
		    	double prior_X = primaryStage.getX();
		    	double prior_Y = primaryStage.getY();
		    	
				URL url = getClass().getResource("FXML_3b2_DisplayResults.fxml");
		    	FXMLLoader fxmlloader = new FXMLLoader();
		    	fxmlloader.setLocation(url);
		    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
		        Parent root = null;
		    	try {
					root = fxmlloader.load();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//		    	Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
	    	
//		    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
		    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
	    	
				try {
					((Controller_3b2_DisplayResults)fxmlloader.getController()).setContext(main_package, selecteditem, txtFPS.getValue(), txtPixels.getValue());
					primaryStage.setTitle("Image Optical Flow - Input Microscope Data");
//					primaryStage.setMaximized(true);
					primaryStage.setScene(scene);
					primaryStage.show();
					
					primaryStage.setX(prior_X);
					primaryStage.setY(prior_Y);
					System.out.println("Moving on!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        });
			
		} else {
			JOptionPane.showMessageDialog(null, "Please select a valid group.","Warning",JOptionPane.WARNING_MESSAGE);
		}

	}

	public void setContext(PackageData main_package_data) {
		main_package = main_package_data;
		Groups g = main_package.getCurrent_groups();
		for (int i = 0; i < g.size(); i ++) {
			Group t = g.get(i);
			String name = t.getName();
			//TODO add check for Done/Running status
			if (doneFiles.contains(name + "_group.ser") == false) {
				doneFiles.add(name);
			}
		}
		FXCollections.sort(doneFiles);
		groupsListView.setItems(doneFiles);
		
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		Image imgBack = new Image(getClass().getResourceAsStream("/left-arrow-angle.png"));
		cmdBack.setGraphic(new ImageView(imgBack));
		Tooltip tooltip5 = new Tooltip();
		tooltip5.setText("Back to Initial Screen");
		cmdBack.setTooltip(tooltip5);
		
		Image imgNext = new Image(getClass().getResourceAsStream("/right-arrow-angle.png"));
		cmdNext.setGraphic(new ImageView(imgNext));
		Tooltip tooltip6 = new Tooltip();
		tooltip6.setText("Microscope Parametrization");
		cmdNext.setTooltip(tooltip6);
		
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		File[] groupfiles = finder(s);
		for (int i = 0; i < groupfiles.length; i++) {
			File currentfile = groupfiles[i];
			System.out.println(currentfile.getName().toString());
			doneFiles.add(currentfile.getName().toString());
		}
		System.out.println("Current relative path is: " + s);
	
	}
	
	public File[] finder( String dirName){
        File dir = new File(dirName);

        return dir.listFiles(new FilenameFilter() { 
                 public boolean accept(File dir, String filename)
                      { return filename.endsWith("_group.ser"); }
        } );

    }


	
    public SpinnerValueFactory<Double> facGen(double start, double end, double init, double step) {
    	SpinnerValueFactory.DoubleSpinnerValueFactory dblFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(start, end, init, step);
    	dblFactory.setConverter(new StringConverter<Double>() {
    		private final DecimalFormat df = new DecimalFormat("#.#########");
    		@Override public String toString(Double value) {
    			if (value == null) {
    				return "";
    				}
    			return df.format(value);
    		}
    		@Override public Double fromString(String value) {
    			try {
    				// If the specified value is null or zero-length, return null
    				if (value == null) {
    					return null;
    				}
    				value = value.trim();
    				if (value.length() < 1) {
    					return null;
    				}
    				// Perform the requested parsing
    				return df.parse(value).doubleValue();
    			} catch (ParseException ex) {
    				throw new RuntimeException(ex);
    			}
    		}
    	});
    	return dblFactory;
    }
    
      
    private static final PseudoClass PRESSED = PseudoClass.getPseudoClass("pressed");
  
    class IncrementHandler implements EventHandler<MouseEvent> {
        private Spinner spinner;
        private boolean increment;
        private long startTimestamp;
        private int currentFrame = 0;
        private int previousFrame = 0;  

        private long initialDelay = 1000l * 1000L * 750L; // 0.75 sec
        private Node button;

        private final AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
            	if (currentFrame == previousFrame || currentFrame % 10 == 0) {
	                if (now - startTimestamp >= initialDelay) {
	                    // trigger updates every frame once the initial delay is over
	                    if (increment) {
	                        spinner.increment();
	                    } else {
	                        spinner.decrement();
	                    }
	                }
            	}
            	++currentFrame;
            }
        };

        @Override
        public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY) {
                Spinner source = (Spinner) event.getSource();
                Node node = event.getPickResult().getIntersectedNode();

                Boolean increment = null;
                // find which kind of button was pressed and if one was pressed
                while (increment == null && node != source) {
                    if (node.getStyleClass().contains("increment-arrow-button")) {
                        increment = Boolean.TRUE;
                    } else if (node.getStyleClass().contains("decrement-arrow-button")) {
                        increment = Boolean.FALSE;
                    } else {
                        node = node.getParent();
                    }
                }
                if (increment != null) {
                    event.consume();
                    source.requestFocus();
                    spinner = source;
                    this.increment = increment;

                    // timestamp to calculate the delay
                    startTimestamp = System.nanoTime();

                    button = node;

                    // update for css styling
                    node.pseudoClassStateChanged(PRESSED, true);

                    // first value update
                    timer.handle(startTimestamp + initialDelay);

                    // trigger timer for more updates later
                    timer.start();
                }
            }
        }

        public void stop() {
            timer.stop();
            button.pseudoClassStateChanged(PRESSED, false);
            button = null;
            spinner = null;
            previousFrame = currentFrame;
        }
    }
	
	
}
