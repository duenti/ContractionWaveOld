package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

//import controllers.Controller_3e_ViewJetQuiverMergeSingle.IncrementHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Group;
import model.Groups;
import model.MicroscopePreferences;
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
		((Controller_1_InitialScreen)fxmlloader.getController()).setContext(new PackageData(main_package.isLoad_preferences()));
		primaryStage.setTitle("ContractionWave");
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
    	stage.setTitle("ContractionWave");
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
		primaryStage.setTitle("ContractionWave - Welcome");
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

	private double fps_value;
	private double pixel_value;
    
	
	@FXML
	void handleDialogMicroscope(ActionEvent event) {
		showDialogMicroscope(false);
	}
	
	public void showDialogMicroscope(boolean next) {
    	Spinner<Double> txtFPS = new Spinner<Double>();    	
		SpinnerValueFactory<Double> fpsFac = facGen(1.0, 10000.0, 200.0, 1.0);
		txtFPS.setValueFactory(fpsFac);
		txtFPS.setEditable(true);
		TextFormatter<Double> formatter = new TextFormatter<Double>(fpsFac.getConverter(), fpsFac.getValue());
		txtFPS.getEditor().setTextFormatter(formatter);
		fpsFac.valueProperty().bindBidirectional(formatter.valueProperty());
		formatter.valueProperty().addListener((s, ov, nv) -> {
			fps_value = nv;
//			System.out.println("bind value");
//			System.out.println("fps_value");
//			System.out.println(fps_value);
			
		});
		
		Spinner<Double> txtPixels = new Spinner<Double>();
		SpinnerValueFactory<Double> pixelFac = facGen(0.001, 10000.0, 0.02375, 0.001);
		txtPixels.setValueFactory(pixelFac);
		txtPixels.setEditable(true);
		TextFormatter<Double> formatter2 = new TextFormatter<Double>(pixelFac.getConverter(), pixelFac.getValue());
		txtPixels.getEditor().setTextFormatter(formatter2);
		pixelFac.valueProperty().bindBidirectional(formatter2.valueProperty());
		formatter2.valueProperty().addListener((s, ov, nv) -> {
			pixel_value = nv;
//			System.out.println("bind value");
//			System.out.println("pixel_value");
//			System.out.println(pixel_value);
		});
		
		Label label1 = new Label();
		label1.setText("Video Recorder (FPS):");
		Label label2 = new Label();
		label2.setText("Pixel Value (µ):");
		
    	Dialog<Boolean> dialogMicroscope = new Dialog<>();
    	dialogMicroscope.initModality(Modality.APPLICATION_MODAL);
    	dialogMicroscope.initOwner(null); //bug do refactor
    	dialogMicroscope.setHeaderText("Set Experiment Settings:");
    	dialogMicroscope.setTitle("Experiment Settings");
    	dialogMicroscope.setResizable(true);
    	GridPane grid = new GridPane();
    	grid.add(label1, 1, 1);
    	grid.add(txtFPS, 2, 1);
    	grid.add(label2, 1, 2);
    	grid.add(txtPixels, 2, 2);
    	dialogMicroscope.getDialogPane().setContent(grid);
    	ButtonType buttonTypeOk = new ButtonType("Ok", ButtonData.OK_DONE);
    	dialogMicroscope.getDialogPane().getButtonTypes().add(buttonTypeOk);
    	dialogMicroscope.show();
    	
    	dialogMicroscope.setOnCloseRequest(event -> {
//    		System.out.println("pixel_value");
//    		System.out.println(pixel_value);
//    		System.out.println("fps_value");
//    		System.out.println(fps_value);
			fps_value = txtFPS.getValue();
			pixel_value = txtPixels.getValue();
//			System.out.println("pixel_value");
//			System.out.println(pixel_value);
//			System.out.println("fps_value");
//			System.out.println(fps_value);
        	if(dialogMicroscope.getResult() == null) return;
    		File tmpDir = new File(getInitialDirectory().toFile().getAbsolutePath() + "/microscope.pref");
    		boolean exists = tmpDir.exists();
    		if (exists == true) {
    			tmpDir.delete();
    		}
    		MicroscopePreferences a = new MicroscopePreferences(pixel_value, fps_value);
    		File tmpDir2 = new File(getInitialDirectory().toFile().getAbsolutePath() + "/microscope.pref");
    		FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(tmpDir2);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(fout);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		try {
				oos.writeObject(a);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		try {
				oos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		try {
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		if (next == true) {
    			nextWindow();
    		}
    	});
	}
	
	public void nextWindow() {
		System.out.println("pixel_value");
		System.out.println(pixel_value);
		System.out.println("fps_value");
		System.out.println(fps_value);
		String selecteditem = groupsListView.getSelectionModel().getSelectedItem();
		if (selecteditem.indexOf("_group.ser") > -1) {
			selecteditem = selecteditem.split(Pattern.quote("_group.ser"))[0];
		}
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
//    	Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
	
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
	
		try {
			((Controller_3b2_DisplayResults)fxmlloader.getController()).setContext(main_package, selecteditem, fps_value, pixel_value);
			primaryStage.setTitle("ContractionWave - Input Microscope Data");
//			primaryStage.setMaximized(true);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			primaryStage.setX(prior_X);
			primaryStage.setY(prior_Y);
			System.out.println("Moving on!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Path rootDir; // The chosen root or source directory
	private static final String DEFAULT_DIRECTORY =
            System.getProperty("user.dir"); //  or "user.home"
	
	private static Path getInitialDirectory() {
        return (rootDir == null) ? Paths.get(DEFAULT_DIRECTORY) : rootDir;
    }
	
	private void validateSelection() throws IOException, ClassNotFoundException {
		if (groupsListView.getSelectionModel().getSelectedItem() != null) {
			//check existance of file containing previous configurations

			File tmpDir = new File(getInitialDirectory().toFile().getAbsolutePath() + "/microscope.pref");
			boolean exists = tmpDir.exists();
			if (exists == false) {
				showDialogMicroscope(true);
			} else {
	    		nextWindow();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please select a valid group.","Warning",JOptionPane.WARNING_MESSAGE);
		}

	}

	public void setContext(PackageData main_package_data) throws ClassNotFoundException, IOException {
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
		
		File tmpDir = new File(getInitialDirectory().toFile().getAbsolutePath() + "/microscope.pref");
		boolean exists = tmpDir.exists();
		if (exists == true) {
	        FileInputStream fin = new FileInputStream(tmpDir);
			ObjectInputStream oin = new ObjectInputStream(fin);
			MicroscopePreferences readCase = (MicroscopePreferences) oin.readObject();
			pixel_value = readCase.getPixel_value();
			fps_value = readCase.getFps_value();
			fin.close();
			oin.close();
			
		}
		
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
  
//    class IncrementHandler implements EventHandler<MouseEvent> {
//        private Spinner spinner;
//        private boolean increment;
//        private long startTimestamp;
//        private int currentFrame = 0;
//        private int previousFrame = 0;  
//
//        private long initialDelay = 1000l * 1000L * 750L; // 0.75 sec
//        private Node button;
//
//        private final AnimationTimer timer = new AnimationTimer() {
//
//            @Override
//            public void handle(long now) {
//            	if (currentFrame == previousFrame || currentFrame % 10 == 0) {
//	                if (now - startTimestamp >= initialDelay) {
//	                    // trigger updates every frame once the initial delay is over
//	                    if (increment) {
//	                        spinner.increment();
//	                    } else {
//	                        spinner.decrement();
//	                    }
//	                }
//            	}
//            	++currentFrame;
//            }
//        };
//
//        @Override
//        public void handle(MouseEvent event) {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                Spinner source = (Spinner) event.getSource();
//                Node node = event.getPickResult().getIntersectedNode();
//
//                Boolean increment = null;
//                // find which kind of button was pressed and if one was pressed
//                while (increment == null && node != source) {
//                    if (node.getStyleClass().contains("increment-arrow-button")) {
//                        increment = Boolean.TRUE;
//                    } else if (node.getStyleClass().contains("decrement-arrow-button")) {
//                        increment = Boolean.FALSE;
//                    } else {
//                        node = node.getParent();
//                    }
//                }
//                if (increment != null) {
//                    event.consume();
//                    source.requestFocus();
//                    spinner = source;
//                    this.increment = increment;
//
//                    // timestamp to calculate the delay
//                    startTimestamp = System.nanoTime();
//
//                    button = node;
//
//                    // update for css styling
//                    node.pseudoClassStateChanged(PRESSED, true);
//
//                    // first value update
//                    timer.handle(startTimestamp + initialDelay);
//
//                    // trigger timer for more updates later
//                    timer.start();
//                }
//            }
//        }
//
//        public void stop() {
//            timer.stop();
//            button.pseudoClassStateChanged(PRESSED, false);
//            button = null;
//            spinner = null;
//            previousFrame = currentFrame;
//        }
//    }
	
	
}
