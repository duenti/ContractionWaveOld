package controllers;

import static org.bytedeco.javacpp.opencv_core.CV_8U;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import controllers.Controller_3b2_DisplayResults.IncrementHandler;
import edu.mines.jtk.awt.ColorMap;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import model.Group;
import model.ImageGroup;
import model.PackageData;
import model.TimeSpeed;
import model.VideoGroup;
import model.XYCircleAnnotation;

public class Controller_3e_ViewJetQuiverMergeSingle implements Initializable {
	private int width = 0;
	private int height = 0;
	private String currentRenderType = "Jet";
	private boolean jet_state = false;
	private boolean quiver_state = false;
	private boolean merge_state = false;
	private IntervalMarker marker;
	private XYDataset general_dataset;
	private boolean contour_state = false;
	private double mask_value = 0.08;
	private double scale_start = 0.05;
	private double scale_end = 7.0;
	private int xwindow = 8;
	private int ywindow = 15;  
	public static final IndexColorModel JET = ColorMap.getJet();
	private ColorMap this_jet = new ColorMap(scale_start, scale_end, JET);
	private int current_index;
	private final int ARR_SIZE = 4; //size of triangle
	
    private PackageData main_package;
	private static Group currentGroup;
	private static boolean zoomGridLinesState;
	private double fps_value;
	private double pixel_value;
	private double average_value;
	private double upper_limit;
	private int a = 0;
	private int from_val;
	private int to_val;
	private int step_val;
	private List<TimeSpeed> timespeedlist;
	private List<IntervalMarker> intervalsList;
	private List<Integer> maximum_list;
	private List<Integer> minimum_list;
	private List<Integer> first_points;
	private List<Integer> fifth_points;
	private ImageView toSaveView = new ImageView();
	private Timeline sliderTimer;
		
	@FXML
	void handleMenuNewImage(ActionEvent event) throws IOException{
		Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
		URL url = getClass().getResource("FXML_2b_ImagesNew.fxml");
		main_package.getListFlows().clear();
		FileReader.chooseSourceDirectory(primaryStage, url, main_package);
	}

	@FXML
	void handleMenuNewVideo(ActionEvent event) throws IOException{
		Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
		URL url = getClass().getResource("FXML_2b_ImagesNew.fxml");
		main_package.getListFlows().clear();
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
		Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
//    	javafx.geometry.Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
		main_package.getListFlows().clear();
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

	
	private void waitCursor() {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	Stage primaryStage;
		    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
		    	primaryStage.getScene().setCursor(Cursor.WAIT);
		    }
		});
	}
	
	private void returnCursor() {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	Stage primaryStage;
		    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
		    	primaryStage.getScene().setCursor(Cursor.DEFAULT);
		    }
		});
	}
	
	private static Path rootDir; // The chosen root or source directory
	private static final String DEFAULT_DIRECTORY =
            System.getProperty("user.dir"); //  or "user.home"
	
	private static Path getInitialDirectory() {
        return (rootDir == null) ? Paths.get(DEFAULT_DIRECTORY) : rootDir;
    }
	
    void saveCurrentImageView(Image this_img) throws IOException {
    	System.out.println("Trying to save image 2");
        //Show save file dialog
        File file = new File(current_filename);
        System.out.println("New File: " + file.getAbsolutePath());
    	BufferedImage bImage = SwingFXUtils.fromFXImage(this_img, null);
    	if (this_img == null) {
    		System.out.println("Null Image");
    	}
    	if (bImage == null) {
    		System.out.println("Null Buffer");
    	}
    	try {
    		System.out.println("Saving....");
    		ImageIO.write(bImage, save_type, file);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}
        
//        if (file != null) {
//        	BufferedImage bImage = SwingFXUtils.fromFXImage(toSaveView.getImage(), null);
//            try {
//              ImageIO.write(bImage, type, file);
//            } catch (IOException e) {
//              throw new RuntimeException(e);
//            }
//        }
    }
    
	@FXML
	void handleExportScale(ActionEvent event) throws IOException {
		Stage dialogScale = new Stage();    
		URL url = getClass().getResource("FXML_Scale.fxml");
    	FXMLLoader fxmlloader = new FXMLLoader();
    	fxmlloader.setLocation(url);
    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
    	Parent root;
    	root = fxmlloader.load();
    	((Controller_Scale)fxmlloader.getController()).setContext(mask_value, scale_start, scale_end);
    	dialogScale.setScene(new Scene(root));
		dialogScale.setTitle("Save Jet Scale to Figure:");		
    	dialogScale.initModality(Modality.APPLICATION_MODAL);
    	dialogScale.initOwner(null);
    	dialogScale.setResizable(true);
    	dialogScale.show();
	}
    
    private String save_type = "";
    private String current_filename = "";
    
	@FXML
	void handleExportJetJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
//    	Stage dialog = new Stage();
//    	dialog.initOwner(primaryStage);
//    	dialog.initModality(Modality.APPLICATION_MODAL); 
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
	    	current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_Jet" +".jpg";
	    	save_type = "jpg";
			renderImageView(i, "Jet", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportJetPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
	    	current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_Jet" +".png";
	    	save_type = "png";
			renderImageView(i, "Jet", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportMergeBothJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
	    	current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_MergeJetQuiver"  +".jpg";
	    	save_type = "jpg";
			renderImageView(i, "JetQuiverMerge", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportMergeBothPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_MergeJetQuiver" +".png";
	    	save_type = "png";
			renderImageView(i, "JetQuiverMerge", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportMergeJetJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_MergeJet" +".jpg";
	    	save_type = "jpg";
			renderImageView(i, "JetMerge", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportMergeJetPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_MergeJet" +".png";
	    	save_type = "png";
			renderImageView(i, "JetMerge", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportMergeQuiverJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_MergeQuiver"  +".jpg";
	    	save_type = "jpg";
			renderImageView(i, "QuiverMerge", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportMergeQuiverPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i+ "_MergeQuiver" +".png";
	    	save_type = "png";
			renderImageView(i, "QuiverMerge", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportQuiverJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_Quiver" +".jpg";
	    	save_type = "jpg";
			renderImageView(i, "Quiver", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}

	@FXML
	void handleExportQuiverPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_Quiver" +".png";
	    	save_type = "png";
			renderImageView(i, "Quiver", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}
	
	@FXML
	void handleExportJetQuiverJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_JetQuiver"+".jpg";
	    	save_type = "jpg";
			renderImageView(i, "JetQuiver", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}
	
	@FXML
	void handleExportJetQuiverPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		for (int i = 0; i < general_dataset.getItemCount(0)-1; i++) {
			current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +i + "_JetQuiver" +".png";
	    	save_type = "png";
			renderImageView(i, "JetQuiver", true);
		}
		returnCursor();
    	JOptionPane.showMessageDialog(null, "Files saved successfully");
	}
	
	@FXML
	void handleExportCurrentJPG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		current_filename =chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +current_index + "_Current" +".jpg";
	    save_type = "jpg";
		renderImageView(current_index, currentRenderType , true);
		returnCursor();
	    JOptionPane.showMessageDialog(null, "File saved successfully");
	}
	
	@FXML
	void handleExportCurrentPNG(ActionEvent event) throws IOException {
    	DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) sliderGroups.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
        waitCursor();
		current_filename = chosenDir.getAbsolutePath().toString() + "/"+ currentGroup.getName() +  "_" +current_index + "_Current" +".png";
	    save_type = "png";
		renderImageView(current_index, currentRenderType , true);
		returnCursor();
	    JOptionPane.showMessageDialog(null, "File saved successfully");
	}
	
	@FXML
	void handlePlayStopButton(ActionEvent event){
		System.out.println(cmdSliderPlay.getText());
		if(cmdSliderPlay.getText().equals("Play")){
			System.out.println("Play");
			sliderTimer.play();
			
			cmdSliderPlay.setText("Stop");
		}else{
			System.out.println("Stop");
			sliderTimer.stop();
			
			cmdSliderPlay.setText("Play");
		}
	}
	
    @FXML
    private Button cmdBack;

    @FXML
    private ImageView imgview1;

    @FXML
    private Slider sliderGroups;

    @FXML
    private CheckBox quiverCheck;

    @FXML
    private CheckBox jetCheck;

    @FXML
    private CheckBox mergeCheck;

    @FXML
    private Button buttonJetAdvanced;

    @FXML
    private Spinner<Double> spinnerMask;

    @FXML
    private Spinner<Double> spinnerScaleStart;

    @FXML
    private Spinner<Double> spinnerScaleEnd;

    @FXML
    private Spinner<Integer> spinnerFPS;
    
    @FXML
    private ToggleButton togglebuttonContour;

    @FXML
    private Button buttonMergeAdvanced;

    @FXML
    private SwingNode chartNode;
	
	@FXML
	private Button cmdSliderPlay;
    
    private JFreeChart currentChart;
	private double lowerBoundDomain;
	private double upperBoundDomain;
	private double alpha_under_two = 1.0;
	private double alpha_above_two = 0.3;
	private double alpha_under_three = 1.0;
	private double alpha_above_three = 0.3;
	private boolean ask_saved;
	
    @FXML
    void handleMarkerAlpha(ActionEvent event) {
    	Dialog<Boolean> dialogJet = new Dialog<>();
    	dialogJet.setHeaderText("Set Marker Alpha:");
    	dialogJet.setResizable(true);
    	Label label1 = new Label("Marker Alpha: ");
    	Spinner<Double> xwindowSpin = new Spinner<Double>();
    	xwindowSpin.setValueFactory(facGen(0.0, 1.0, (double) main_package.getPlot_preferences().getMarkerAlpha(), 0.01));
    	xwindowSpin.setEditable(true);
		IncrementHandler handler2 = new IncrementHandler();
		xwindowSpin.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, handler2);
		xwindowSpin.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                    handler2.stop();
	                }
	        }
	    });
		xwindowSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				main_package.getPlot_preferences().setMarkerAlpha(Float.valueOf(newValue));
				marker.setAlpha(Float.valueOf(newValue));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		xwindowSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	xwindowSpin.increment(0);
	        } 
	    });
    	GridPane grid = new GridPane();
    	grid.add(label1, 1, 1);
    	grid.add(xwindowSpin, 2, 1);
    	dialogJet.getDialogPane().setContent(grid);
    	ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
    	dialogJet.getDialogPane().getButtonTypes().add(buttonTypeOk);
    	dialogJet.show();
    }

    @FXML
    void handleMarkerColor(ActionEvent event) {
    	java.awt.Color initialColor = main_package.getPlot_preferences().getMarkerColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Marker color", initialColor);
        main_package.getPlot_preferences().setMarkerColorRGB(newColor);
        marker.setPaint(newColor);
    }
    
    @FXML
    void handleSeriesColor(ActionEvent event) {
    	XYPlot plot = currentChart.getXYPlot();
    	java.awt.Color initialColor = main_package.getPlot_preferences().getSeriesColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Series color", initialColor);
        plot.getRenderer().setSeriesPaint(0, newColor);
        main_package.getPlot_preferences().setSeriesColorRGB(newColor);
    }
    
    
    @FXML
    void handleMinimumColor(ActionEvent event) {
    	XYPlot plot = currentChart.getXYPlot();
    	XYDataset dataset = plot.getDataset();
    	java.awt.Color initialColor = main_package.getPlot_preferences().getSeriesColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Peak minimum color", initialColor);
        main_package.getPlot_preferences().setMinimumDotColorRGB(newColor);
        plot.clearAnnotations();
		if (maximum_list.size() + minimum_list.size() < 1500) {
	        for(int x1 = 0; x1 < dataset.getItemCount(0); x1++){
	        	double x = dataset.getXValue(0, x1);
	        	double y = dataset.getYValue(0, x1);
	        	if (maximum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)  && !first_points.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
	        	}
	        }
		}
    }
	
    @FXML
    void handleMaximumColor(ActionEvent event) {
    	XYPlot plot = currentChart.getXYPlot();
    	XYDataset dataset = plot.getDataset();
    	java.awt.Color initialColor = main_package.getPlot_preferences().getSeriesColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Peak minimum color", initialColor);
        main_package.getPlot_preferences().setMaximumDotColorRGB(newColor);
        plot.clearAnnotations();
		if (maximum_list.size() + minimum_list.size() < 1500) {
	        for(int x1 = 0; x1 < dataset.getItemCount(0); x1++){
	        	double x = dataset.getXValue(0, x1);
	        	double y = dataset.getYValue(0, x1);
	        	if (maximum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)  && !first_points.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
	        	}
	        }
		}
    }
    
    @FXML
    void handleFirstColor(ActionEvent event) {
    	XYPlot plot = currentChart.getXYPlot();
    	XYDataset dataset = plot.getDataset();
    	java.awt.Color initialColor = main_package.getPlot_preferences().getSeriesColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Peak minimum color", initialColor);
        main_package.getPlot_preferences().setFirstDotColorRGB(newColor);
        plot.clearAnnotations();
		if (maximum_list.size() + minimum_list.size() < 1500) {
	        for(int x1 = 0; x1 < dataset.getItemCount(0); x1++){
	        	double x = dataset.getXValue(0, x1);
	        	double y = dataset.getYValue(0, x1);
	        	if (maximum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)  && !first_points.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
	        	}
	        }
		}
    }
    
    @FXML
    void handleLastColor(ActionEvent event) {
    	XYPlot plot = currentChart.getXYPlot();
    	XYDataset dataset = plot.getDataset();
    	java.awt.Color initialColor = main_package.getPlot_preferences().getSeriesColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Peak minimum color", initialColor);
        main_package.getPlot_preferences().setLastDotColorRGB(newColor);
        plot.clearAnnotations();
		if (maximum_list.size() + minimum_list.size() < 1500) {
	        for(int x1 = 0; x1 < dataset.getItemCount(0); x1++){
	        	double x = dataset.getXValue(0, x1);
	        	double y = dataset.getYValue(0, x1);
	        	if (maximum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1+minimum_value_this) && !minimum_list.contains(x1+minimum_value_this)  && !first_points.contains(x1+minimum_value_this)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
	        	}
	        }
		}
    }
	
	private void commitColors() {
    	XYPlot plot = currentChart.getXYPlot();
    	main_package.getPlot_preferences().setSeriesColorRGB( (java.awt.Color) plot.getRenderer().getSeriesPaint(0));
    	main_package.getPlot_preferences().setRangeGridColorRGB( (java.awt.Color) plot.getRangeGridlinePaint());
    	main_package.getPlot_preferences().setDomainGridColorRGB( (java.awt.Color) plot.getDomainGridlinePaint());
    	main_package.getPlot_preferences().setBackgroundColorRGB( (java.awt.Color) plot.getBackgroundPaint());
//    	main_package.getPlot_preferences().setMarkerAlpha();
//    	main_package.getPlot_preferences().setMarkerColorRGB();
    }
	
    
    @FXML
    void back(ActionEvent event) throws ClassNotFoundException, IOException {
		Stage primaryStage = (Stage) cmdBack.getScene().getWindow();
    	double prior_X = primaryStage.getX();
    	double prior_Y = primaryStage.getY();
		URL url = getClass().getResource("FXML_3d2_PeakParametersPlot.fxml");
    	FXMLLoader fxmlloader = new FXMLLoader();
    	fxmlloader.setLocation(url);
    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root;
    	root = fxmlloader.load();
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
//    	javafx.geometry.Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
    	commitColors();
    	main_package.getListFlows().clear();
    	((Controller_3d2_PeakParametersPlot)fxmlloader.getController()).setContext(main_package, currentGroup, fps_value, pixel_value, average_value, upper_limit, intervalsList, maximum_list, minimum_list, first_points, fifth_points, timespeedlist, ask_saved);
    	primaryStage.setTitle("Image Optical Flow - Peak Parameters Plot");
//    	primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setX(prior_X);
		primaryStage.setY(prior_Y);
    }
    
    
        
    @FXML
    void showAdvancedJet(ActionEvent event) throws IOException {
    	Dialog<Boolean> dialogJet = new Dialog<>();
    	dialogJet.setHeaderText("Quiver X and Y Window Size Options:");
    	dialogJet.setResizable(true);
    	Label label1 = new Label("X Window: ");
    	Label label2 = new Label("Y Window: ");
    	Spinner<Integer> xwindowSpin = new Spinner<Integer>();
    	Spinner<Integer> ywindowSpin= new Spinner<Integer>();
    	xwindowSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, xwindow, 1));
    	xwindowSpin.setEditable(true);
		IncrementHandler handler12 = new IncrementHandler();
		xwindowSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler12);
		xwindowSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                    handler12.stop();
	                }
	        }
	    });
		xwindowSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				xwindow = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType, false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		xwindowSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	xwindowSpin.increment(0);
	        } 
	    });
		
    	ywindowSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, ywindow, 1));
    	ywindowSpin.setEditable(true);
		IncrementHandler handler11 = new IncrementHandler();
		ywindowSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler11);
		ywindowSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                    handler11.stop();
	                }
	        }
	    });
		ywindowSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				ywindow = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType, false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		ywindowSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	ywindowSpin.increment(0);
	        } 
	    });
    	GridPane grid = new GridPane();
    	grid.add(label1, 1, 1);
    	grid.add(xwindowSpin, 2, 1);
    	grid.add(label2, 1, 2);
    	grid.add(ywindowSpin, 2, 2);
    	dialogJet.getDialogPane().setContent(grid);
    	ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
    	dialogJet.getDialogPane().getButtonTypes().add(buttonTypeOk);
    	dialogJet.show();
    }
    

    @FXML
    void showAdvancedMerge(ActionEvent event) throws IOException {
    	

//    	private double alpha_under_two = 0.7;
//    	private double alpha_above_two = 0.3;
//    	private double alpha_under_three = 0.7;
//    	private double alpha_above_three = 0.3;
    	
    	Dialog<Boolean> dialogJet = new Dialog<>();
    	dialogJet.setHeaderText("Cell Segmentation Options:");
    	dialogJet.setResizable(true);
    	Label label1 = new Label("Blur Size: ");
    	Label label2 = new Label("Kernel Dilation: ");
    	Label label3 = new Label("Kernel Erosion: ");
    	Label label4 = new Label("Kernel Smoothing: ");
    	Label label5 = new Label("Border Width: ");
    	Label label6 = new Label("Sigma Value: ");
    	Label label7 = new Label("Merge Alpha Background: ");
    	Label label8 = new Label("Merge Alpha Foreground: ");
    	Label label9 = new Label("Merge Alpha Background 2: ");
    	Label label10 = new Label("Merge Alpha Foreground 2: ");
    	Spinner<Integer> blurSpin = new Spinner<Integer>();
    	Spinner<Integer> dilationSpin= new Spinner<Integer>();
    	Spinner<Integer> erosionSpin = new Spinner<Integer>();
    	Spinner<Integer> smoothingSpin= new Spinner<Integer>();
    	Spinner<Integer> borderSpin = new Spinner<Integer>();
    	Spinner<Double> sigmaSpin = new Spinner<Double>();
    	Spinner<Double> alphabackSpin = new Spinner<Double>();
    	Spinner<Double> alphaforeSpin = new Spinner<Double>();
    	Spinner<Double> alphaback2Spin = new Spinner<Double>();
    	Spinner<Double> alphafore2Spin = new Spinner<Double>();
    	blurSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, blur_size, 1));
    	blurSpin.setEditable(true);
		IncrementHandler handler_0 = new IncrementHandler();
		blurSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_0);
		blurSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_0.stop();
	                }
	        }
	    });
		blurSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				blur_size = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		blurSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	blurSpin.increment(0);
	        } 
	    });

    	dilationSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, kernel_dilation, 1));
    	dilationSpin.setEditable(true);
		IncrementHandler handler_1 = new IncrementHandler();
		dilationSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_1);
		dilationSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_1.stop();
	                }
	        }
	    });
		dilationSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				kernel_dilation = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		dilationSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	dilationSpin.increment(0);
	        } 
	    });
		
    	erosionSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, kernel_erosion, 1));
    	erosionSpin.setEditable(true);
		IncrementHandler handler_2 = new IncrementHandler();
		erosionSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_2);
		erosionSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_2.stop();
	                }
	        }
	    });
		erosionSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				kernel_erosion = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		erosionSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	erosionSpin.increment(0);
	        } 
	    });
		
	   	smoothingSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, kernel_smoothing_contours, 1));
	   	smoothingSpin.setEditable(true);
		IncrementHandler handler_3 = new IncrementHandler();
		smoothingSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_3);
		smoothingSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_3.stop();
	                }
	        }
	    });
		smoothingSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				kernel_smoothing_contours = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		smoothingSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	smoothingSpin.increment(0);
	        } 
	    });
		
	   	borderSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, border_value, 1));
	   	borderSpin.setEditable(true);
		IncrementHandler handler_4 = new IncrementHandler();
		borderSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_4);
		borderSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_4.stop();
	                }
	        }
	    });
		borderSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				border_value = Integer.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		borderSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	borderSpin.increment(0);
	        } 
	    });
		
	   	sigmaSpin.setValueFactory(facGen(0, Double.MAX_VALUE, sigma, 0.01));
	   	sigmaSpin.setEditable(true);
		IncrementHandler handler_5 = new IncrementHandler();
		sigmaSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_5);
		sigmaSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_5.stop();
	                }
	        }
	    });
		sigmaSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				sigma = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		sigmaSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	sigmaSpin.increment(0);
	        } 
	    });
		
	   	alphabackSpin.setValueFactory(facGen(0, 1.0, alpha_under_two, 0.01));
	   	alphabackSpin.setEditable(true);
		IncrementHandler handler_6 = new IncrementHandler();
		alphabackSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_6);
		alphabackSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_6.stop();
	                }
	        }
	    });
		alphabackSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				alpha_under_two = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		alphabackSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	alphabackSpin.increment(0);
	        } 
	    });
		
	   	alphaback2Spin.setValueFactory(facGen(0, 1.0, alpha_under_three, 0.01));
	   	alphaback2Spin.setEditable(true);
		IncrementHandler handler_7 = new IncrementHandler();
		alphaback2Spin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_7);
		alphaback2Spin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_7.stop();
	                }
	        }
	    });
		alphaback2Spin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				alpha_under_three = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		alphaback2Spin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	alphaback2Spin.increment(0);
	        } 
	    });
		
	   	alphaforeSpin.setValueFactory(facGen(0, 1.0, alpha_above_two, 0.01));
	   	alphaforeSpin.setEditable(true);
		IncrementHandler handler_8 = new IncrementHandler();
		alphaforeSpin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_8);
		alphaforeSpin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_8.stop();
	                }
	        }
	    });
		alphaforeSpin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				alpha_above_two = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		alphaforeSpin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	alphaforeSpin.increment(0);
	        } 
	    });
		
	   	alphafore2Spin.setValueFactory(facGen(0, 1.0, alpha_above_three, 0.01));
	   	alphafore2Spin.setEditable(true);
		IncrementHandler handler_9 = new IncrementHandler();
		alphafore2Spin.addEventFilter(MouseEvent.MOUSE_PRESSED, handler_9);
		alphafore2Spin.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                	handler_9.stop();
	                }
	        }
	    });
		alphafore2Spin.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				alpha_above_three = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		alphafore2Spin.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	alphafore2Spin.increment(0);
	        } 
	    });
		
    	GridPane grid = new GridPane();
    	grid.add(label1, 1, 1);
    	grid.add(blurSpin, 2, 1);
    	grid.add(label2, 1, 2);
    	grid.add(dilationSpin, 2, 2);
    	grid.add(label3, 1, 3);
    	grid.add(erosionSpin, 2, 3);
    	grid.add(label4, 1, 4);
    	grid.add(smoothingSpin, 2, 4);
    	grid.add(label5, 1, 5);
    	grid.add(borderSpin, 2, 5);
    	grid.add(label6, 1, 6);
    	grid.add(sigmaSpin, 2, 6);
    	grid.add(label7, 1, 7);
    	grid.add(alphabackSpin, 2, 7);
    	grid.add(label8, 1, 8);
    	grid.add(alphaforeSpin, 2, 8);
    	grid.add(label9, 1, 9);
    	grid.add(alphaback2Spin, 2, 9);
    	grid.add(label10, 1, 10);
    	grid.add(alphafore2Spin, 2, 10);
    	dialogJet.getDialogPane().setContent(grid);
    	ButtonType buttonTypeOk = new ButtonType("Okay", ButtonData.OK_DONE);
    	dialogJet.getDialogPane().getButtonTypes().add(buttonTypeOk);
    	dialogJet.show();
    }

    @FXML
    void toggleContour(ActionEvent event) throws IOException {
    	contour_state = !contour_state;
    	renderImageView(current_index, currentRenderType , false);
    	if (contour_state == true) {
    		togglebuttonContour.setText("Contour Filter is ON");
    	} else {
    		togglebuttonContour.setText("Contour Filter is OFF");
    	}
    }

    
    public void setContext(PackageData main_package_data, Group g1, double fps_value1, double pixel_value1, double average_value1, double upper_limit1, int start, int stop, int step, List<IntervalMarker> intervalsList2, List<Integer> maximum_list2, List<Integer> minimum_list2, List<Integer> first_points2, List<Integer> fifth_points2, List<TimeSpeed> timespeedlist2, boolean saved) throws IOException{
    	main_package = main_package_data;
		currentGroup = g1;
		fps_value = fps_value1;
		pixel_value = pixel_value1;
		average_value = average_value1;
		upper_limit = upper_limit1;
		from_val = start;
		to_val = stop;
		step_val = step;
		timespeedlist = timespeedlist2;
		intervalsList = intervalsList2;
		maximum_list = maximum_list2;
		minimum_list = minimum_list2;
		first_points = first_points2;
		fifth_points = fifth_points2;
		width = currentGroup.getWidth();
		height = currentGroup.getHeight();
		ask_saved = saved;
    	current_index = 0;
		sliderGroups.setMin(1);
		sliderGroups.setMax(main_package.getListFlows().size() + 1);
		sliderGroups.setValue(1);
		sliderGroups.setBlockIncrement(1.0);
		sliderGroups.setMajorTickUnit(1.0);
		sliderGroups.setMinorTickCount(0);
		sliderGroups.setShowTickLabels(false);
		sliderGroups.setShowTickMarks(true);
		sliderGroups.setSnapToTicks(true);
		jetCheck.setSelected(true);
		constructRenderType();
    	writeLinePlot(start, stop);
    }
    
    
    public void constructRenderType() {
    	if (jet_state == true && quiver_state == true && merge_state == true) {
    		currentRenderType = "JetQuiverMerge";
    	} else if (jet_state == false && quiver_state == false && merge_state == false) {
        	currentRenderType = "None";
        } else if (jet_state == true && quiver_state == false && merge_state == false) {
    		currentRenderType = "Jet";
    	} else if (jet_state == false && quiver_state == true && merge_state == false) {
        	currentRenderType = "Quiver";
        } else if (jet_state == false && quiver_state == false && merge_state == true) {
    		currentRenderType = "Merge";
    	} else if (jet_state == false && quiver_state == true && merge_state == true) {
        	currentRenderType = "QuiverMerge";
        } else if (jet_state == true && quiver_state == false && merge_state == true) {
    		currentRenderType = "JetMerge";
    	} else if (jet_state == true && quiver_state == true && merge_state == false) {
        	currentRenderType = "JetQuiver";
        }
    }
    
	private void createSliderAnimation(double seconds){
		sliderTimer = new Timeline(new KeyFrame(Duration.seconds(seconds), event -> {
		    double i = sliderGroups.getValue();
		    if(i >= sliderGroups.getMax())
		    	sliderGroups.setValue(1);
		    else
		    	sliderGroups.setValue(i+1);
		}));
		sliderTimer.setCycleCount(Timeline.INDEFINITE);
	}
    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
		Image imgBack = new Image(getClass().getResourceAsStream("/left-arrow-angle.png"));
		cmdBack.setGraphic(new ImageView(imgBack));
		Tooltip tooltip5 = new Tooltip();
		tooltip5.setText("Back to Peak Parameters table");
		cmdBack.setTooltip(tooltip5);
//		toSaveView.setVisible(false);
		sliderGroups.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,Number old_val, Number new_val) {
				System.out.println("Changed!!!!!");
				int groupIndex = new_val.intValue()-1;
				current_index = groupIndex;
				changeMarker();
				try {
					renderImageView(groupIndex, currentRenderType , false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		jetCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				jet_state = !jet_state;
				constructRenderType();
				System.out.println(currentRenderType);
				try {
					renderImageView(current_index, currentRenderType , false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		});
		
		quiverCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				quiver_state = !quiver_state;
				constructRenderType();
				System.out.println(currentRenderType);
				try {
					renderImageView(current_index, currentRenderType , false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		});
		
		mergeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				merge_state = !merge_state;
				constructRenderType();
				System.out.println(currentRenderType);
				try {
					renderImageView(current_index, currentRenderType , false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}	
		});
		
		createSliderAnimation(1);
		
		spinnerFPS.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1, 1));
		spinnerFPS.getEditor().textProperty().addListener((obs,oldValue,newValue) ->{
			if (!"".equals(newValue)) {
				int newFPS = Integer.valueOf(newValue);
				double frameSeconds = 1.0/(double)newFPS;
				sliderTimer.stop();
				createSliderAnimation(frameSeconds);
				if(cmdSliderPlay.getText().equals("Stop")){
					sliderTimer.play();
				}				
			}
		});
		spinnerFPS.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	spinnerFPS.increment(0);
	        } 
	    });
		
		spinnerMask.setValueFactory(facGen(0.0, 10000.0, 0.08, 0.1));
		spinnerMask.setEditable(true);
		IncrementHandler handler1 = new IncrementHandler();
		spinnerMask.addEventFilter(MouseEvent.MOUSE_PRESSED, handler1);
		spinnerMask.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                    handler1.stop();
	                }
	        }
	    });
		spinnerMask.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				mask_value = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		spinnerMask.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	spinnerMask.increment(0);
	        } 
	    });
		
		spinnerScaleStart.setValueFactory(facGen(0.0, 10000.0, 0.05, 0.1));
		spinnerScaleStart.setEditable(true);
		IncrementHandler handler2 = new IncrementHandler();
		spinnerScaleStart.addEventFilter(MouseEvent.MOUSE_PRESSED, handler2);
		spinnerScaleStart.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                    handler2.stop();
	                }
	        }
	    });
		spinnerScaleStart.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				scale_start = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		spinnerScaleStart.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	spinnerScaleStart.increment(0);
	        } 
		});
		
		spinnerScaleEnd.setValueFactory(facGen(0.0, 10000.0, 7.0, 0.1));
		spinnerScaleEnd.setEditable(true);
		IncrementHandler handler3 = new IncrementHandler();
		spinnerScaleEnd.addEventFilter(MouseEvent.MOUSE_PRESSED, handler3);
		spinnerScaleEnd.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        Node node = evt.getPickResult().getIntersectedNode();
	        if (node.getStyleClass().contains("increment-arrow-button") ||
	            node.getStyleClass().contains("decrement-arrow-button")) {
	                if (evt.getButton() == MouseButton.PRIMARY) {
	                    handler3.stop();
	        			try {
	        				renderImageView(current_index, currentRenderType , false);
	        			} catch (java.lang.Exception e) {
	        				e.printStackTrace();
	        			}
	                }
	        }
	    });
		spinnerScaleEnd.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
			try {
				scale_end = Double.valueOf(newValue);
				renderImageView(current_index, currentRenderType , false);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
	    });
		spinnerScaleEnd.focusedProperty().addListener((obs, oldValue, newValue) -> {
	        if (newValue == false) {
	        	spinnerScaleEnd.increment(0);
	        } 
		});
		
		
	}
    
	private void changeMarker() {
        XYPlot plot = (XYPlot) currentChart.getPlot();
        plot.removeDomainMarker(marker,Layer.BACKGROUND);
		int index = current_index;
		int t_start;
		if (index - 1 < 0) {
			t_start = index;
		} else {
			t_start = index - 1;
		}
		
		int t_end;
		if (index + 1 >= general_dataset.getItemCount(0)) {
			t_end = index;
		} else {
			t_end = index + 1;
		}
		double unit = general_dataset.getXValue(0, t_end) - general_dataset.getXValue(0, t_start);
        marker = new IntervalMarker(general_dataset.getXValue(0, index) - unit/2, general_dataset.getXValue(0, index) + unit/2);
        marker.setPaint(main_package.getPlot_preferences().getMarkerColorRGB());
        marker.setAlpha(main_package.getPlot_preferences().getMarkerAlpha());
        plot.addDomainMarker(marker,Layer.BACKGROUND);
	}
	
    
  //PLOT GENERATING FUNCTIONS
  	private void writeLinePlot(int min, int max) {
  		currentChart = createChart(createDataset(min, max), min);
  		ChartPanel linepanel = new ChartPanel(currentChart);

		JCheckBoxMenuItem gridLinesmenuItem = new JCheckBoxMenuItem();
		gridLinesmenuItem.setSelected(true);
  		gridLinesmenuItem.setText("Gridlines on/off");
  		GridLinesSwitch gridLinesZoomAction = new GridLinesSwitch(linepanel); 
  		gridLinesmenuItem.addActionListener(gridLinesZoomAction);
  		linepanel.getPopupMenu().add(gridLinesmenuItem);
  		
		JCheckBoxMenuItem showSpline = new JCheckBoxMenuItem();
		showSpline.setText("Render Splines on/off");
		SplineShow splineRendering = new SplineShow(linepanel);
		showSpline.addActionListener(splineRendering);
		linepanel.getPopupMenu().add(showSpline);
  		
  		linepanel.setRangeZoomable(false);
  		linepanel.setDomainZoomable(false);
          Border border = BorderFactory.createCompoundBorder(
                  BorderFactory.createEmptyBorder(4, 4, 4, 4),
                  BorderFactory.createEtchedBorder()
          );
          linepanel.setBorder(border);
          linepanel.setMouseWheelEnabled(true);
          
          chartNode.setContent(linepanel);
  	}
  	
  	private XYDataset createDataset(int min, int max) {
  		XYSeries series1 = new XYSeries("Optical Flow");
  		for (int i = min; i < max; i++) {
  			double average = currentGroup.getMagnitudeListValue(i);
  			series1.add(i / fps_value, average * fps_value * pixel_value);
  		}
  		//peak detection algorithm receives a group
          XYSeriesCollection dataset = new XYSeriesCollection();
          dataset.addSeries(series1);
          return dataset;
      }
  	
  	private int minimum_value_this = 0;
  	private JFreeChart createChart(XYDataset dataset, int min) {
  		  minimum_value_this = min;
          JFreeChart chart = ChartFactory.createXYLineChart(
              "",
              "Time(s)",
              "Speed(\u00B5m/s)",
              dataset,
              PlotOrientation.VERTICAL,
              true,
              true,
              false
          );
          XYPlot plot = (XYPlot) chart.getPlot();
          Font title_font = new Font("Dialog", Font.PLAIN, 17); 
          Font domain_range_axis_font = new Font("Dialog", Font.PLAIN, 14); 
          chart.getTitle().setFont(title_font);
          chart.removeLegend();
          plot.getDomainAxis().setLabelFont(domain_range_axis_font);
          plot.getRangeAxis().setLabelFont(domain_range_axis_font);
          plot.getRangeAxis().setUpperMargin(0.1);
          lowerBoundDomain = chart.getXYPlot().getDomainAxis().getLowerBound();
          upperBoundDomain = chart.getXYPlot().getDomainAxis().getUpperBound();

          plot.setDomainPannable(true);
          plot.setRangePannable(true);
          
          double unit = dataset.getXValue(0, 1) - dataset.getXValue(0, 0);
//          marker = new IntervalMarker(general_dataset.getXValue(0, index) - unit/2, general_dataset.getXValue(0, index) + unit/2);
//          marker = new IntervalMarker(dataset.getXValue(0, 0) - dataset.getXValue(0, 0) / 2  , dataset.getXValue(0, 0) + dataset.getXValue(0, 1) / 2 );
          marker = new IntervalMarker(dataset.getXValue(0, 0) - unit/2, dataset.getXValue(0, 0) + unit/2);
          general_dataset = dataset;
          
          marker.setPaint(main_package.getPlot_preferences().getMarkerColorRGB());
          marker.setAlpha(main_package.getPlot_preferences().getMarkerAlpha());
          plot.addDomainMarker(marker, Layer.BACKGROUND);
          
          //plot.setSeriesPaint(0, new Color(0x00, 0x00, 0xFF));
          plot.setBackgroundPaint(main_package.getPlot_preferences().getBackgroundColorRGB());
          plot.setDomainGridlinePaint(main_package.getPlot_preferences().getDomainGridColorRGB());
          plot.setRangeGridlinePaint(main_package.getPlot_preferences().getRangeGridColorRGB());
          plot.setDomainGridlinesVisible(main_package.getPlot_preferences().isGridlineDefaultState());
          plot.setRangeGridlinesVisible(main_package.getPlot_preferences().isGridlineDefaultState());

          if (upperBoundDomain - lowerBoundDomain <= 200) {
  	        for(int x1 = 0; x1 < dataset.getItemCount(0); x1++){
  	        	double x = dataset.getXValue(0, x1);
  	        	double y = dataset.getYValue(0, x1);
  	        	if (maximum_list.contains(x1+min)) {
  	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
  	        	}
  	        	if (minimum_list.contains(x1+min)) {
  	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
  	        	}
  	        	if (first_points.contains(x1+min) && !minimum_list.contains(x1+min)) {
  	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
  	        	}
  	        	if (fifth_points.contains(x1+min) && !minimum_list.contains(x1+min)  && !first_points.contains(x1+min)) {
  	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
  	        	}
  	        }
  		}
          plot.addChangeListener(new PlotChangeListener(){
          	@Override
          	public void plotChanged(PlotChangeEvent event) {
//          		System.out.println("I am called after a zoom event (and some other events too).");
          		if (plot.getDomainAxis().getLowerBound() != lowerBoundDomain || plot.getDomainAxis().getUpperBound() != upperBoundDomain) {
          			lowerBoundDomain = plot.getDomainAxis().getLowerBound();
          			upperBoundDomain = plot.getDomainAxis().getUpperBound();
          			if (upperBoundDomain - lowerBoundDomain <= 200) {
          		        for(int x1 = 0; x1 < dataset.getItemCount(0); x1++){
          		        	double x = dataset.getXValue(0, x1);
          		        	double y = dataset.getYValue(0, x1);
          		        	if (maximum_list.contains(x1+min)) {
          		        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
          		        	}
          		        	if (minimum_list.contains(x1+min)) {
          		        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
          		        	}
          		        	if (first_points.contains(x1+min) && !minimum_list.contains(x1+min)) {
          		        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
          		        	}
          		        	if (fifth_points.contains(x1+min) && !minimum_list.contains(x1+min)  && !first_points.contains(x1+min)) {
          		        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
          		        	}
          		        }
          			} else {
          				plot.clearAnnotations();
          			}
          		}
          }});
          if (main_package.getPlot_preferences().isSplineDefaultState() == true) {
  			XYSplineRenderer renderer = new XYSplineRenderer();
  			renderer.setDefaultShapesVisible(false);
  	        renderer.setDefaultShapesFilled(false);
  	        renderer.setSeriesPaint(0, main_package.getPlot_preferences().getSeriesColorRGB());
          } else {
          	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
      		renderer.setDefaultShapesVisible(false);
              renderer.setDefaultShapesFilled(false);
              renderer.setSeriesPaint(0, main_package.getPlot_preferences().getSeriesColorRGB());
          }
          return chart;
      }
  	
  	
  	// ACCESSORY PLOT CLASSES	
  	public class GridLinesSwitch implements ActionListener {
  	    private JFreeChart chart;
  	    private ChartPanel panel;
  	    private XYPlot plot;
  	    
  		public GridLinesSwitch(ChartPanel panel) {
  	        this.setPanel(panel);
  	        this.chart = panel.getChart();
  	        this.plot = (XYPlot) chart.getPlot();
  		}
  		
  		@Override
  		public void actionPerformed(java.awt.event.ActionEvent e) {
  			if (plot.isDomainGridlinesVisible() == true) {
  				plot.setDomainGridlinesVisible(false);
  				plot.setRangeGridlinesVisible(false);
  		        main_package.getPlot_preferences().setGridlineDefaultState(false);
  			} else {
  				plot.setDomainGridlinesVisible(true);
  				plot.setRangeGridlinesVisible(true);
  		        main_package.getPlot_preferences().setGridlineDefaultState(true);
  			}
  		}

  		public ChartPanel getPanel() {
  			return panel;
  		}

  		public void setPanel(ChartPanel panel) {
  			this.panel = panel;
  		}
  		
  	}
  	
  	
  	public static BufferedImage convertToType(BufferedImage image, int type) {
  	    BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), type);
  	    Graphics2D graphics = newImage.createGraphics();
  	    graphics.drawImage(image, 0, 0, null);
  	    graphics.dispose();
  	    return newImage;
  	}
  	
  	public static BufferedImage colorToAlpha(BufferedImage raw, java.awt.Color remove) {
  	    int WIDTH = raw.getWidth();
  	    int HEIGHT = raw.getHeight();
  	    BufferedImage image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
  	    int pixels[]=new int[WIDTH*HEIGHT];
  	    raw.getRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
  	    for(int i=0; i<pixels.length;i++)
  	    {
  	        if (pixels[i] == remove.getRGB()) 
  	        {
  	        pixels[i] = 0x00ffffff;
  	        }
  	    }
  	    image.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
  	    return image;
  	}  
    
		
	public void renderImageView(int index, String rendertype, boolean image_not_save) throws IOException{
		Image finalImage = null;
		if (rendertype.equals("Jet")) {
			Boolean to_merge = false;
			finalImage = writeJetImage(index, to_merge);
		}
		else if (rendertype.equals("JetMerge")) {
			Boolean to_merge = true;
			WritableImage myWritableImage = writeJetImage(index, to_merge);
	        Mat img_src = null;
	        if (currentGroup.getType() == 0) {
	            ImageGroup g3 = (ImageGroup) currentGroup;
	            File path_img  = g3.getImages().get(main_package.getListPoints().get(index));
	            img_src = imread(path_img.getCanonicalPath());
	        } else {
	            VideoGroup g3 = (VideoGroup) currentGroup;
	        	FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(g3.getVideo().getAbsolutePath());
	    		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
	    		frameGrabber.start();
	    		int N = frameGrabber.getLengthInFrames();
	    		for(int j = 0; j<N; j++){
	    			Frame frame = frameGrabber.grab();
	    			Mat img = converterToMat.convert(frame);
	    			if(j ==index){
	    				img_src = img.clone();
	    				break;
	    			}
	    		}
	    		frameGrabber.close();
	        }
	        BufferedImage overlay = SwingFXUtils.fromFXImage(myWritableImage, null);
	        Mat jetLayer = Java2DFrameUtils.toMat(overlay);
	        MatVector channels = new MatVector();
	        Mat jetLayerRGB = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
	        org.bytedeco.javacpp.opencv_core.split(jetLayer, channels);
	        Mat blueCh = channels.get(1);
	        Mat greenCh = channels.get(2);
	        Mat redCh = channels.get(3);
	        MatVector channels2 = new MatVector(3);
	        channels2.put(0, redCh);
	        channels2.put(1, greenCh);
	        channels2.put(2, blueCh);
	        org.bytedeco.javacpp.opencv_core.merge(channels2, jetLayerRGB);
	        Mat combinedImage = new Mat();
	        if (contour_state == true) {
	        	img_src.convertTo(img_src, CV_8U);
	        	jetLayerRGB.convertTo(jetLayerRGB, CV_8U);
	        }
			org.bytedeco.javacpp.opencv_core.addWeighted(img_src, alpha_under_two, jetLayerRGB, alpha_above_two, 0, combinedImage);
	        if (contour_state == true) {
	        	Mat mask2 = generateContour(img_src);	        	
	        	Mat combinedImage2 = new Mat();
	        	combinedImage.copyTo(combinedImage2, mask2);
	        	
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage2);
				BufferedImage combined1 = colorToAlpha(combined, java.awt.Color.BLACK);
				
				BufferedImage img_src_buffered = Java2DFrameUtils.toBufferedImage(img_src);

				// create the new image, canvas size is the max. of both image sizes
				BufferedImage combined2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

				// paint both images, preserving the alpha channels
				Graphics g = combined2.getGraphics();
				g.drawImage(img_src_buffered, 0, 0, null);
				g.drawImage(combined1, 0, 0, null);
				finalImage = SwingFXUtils.toFXImage(combined2, null);
	        } else {
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage);
		        finalImage = SwingFXUtils.toFXImage(combined, null);
	        }
		}
		else if (rendertype.equals("Quiver")) {
			Boolean to_merge = false;
			finalImage = writeCanvas(index, to_merge);
		} else if (rendertype.equals("QuiverMerge")) {
			Boolean to_merge = true;
			WritableImage writableImage = writeCanvas(index, to_merge);
			ToMat converter = new OpenCVFrameConverter.ToMat();
	        Mat img_src = null;
	        if (currentGroup.getType() == 0) {
	            ImageGroup g3 = (ImageGroup) currentGroup;
	            File path_img  = g3.getImages().get(main_package.getListPoints().get(index));
	            img_src = imread(path_img.getCanonicalPath());
	        } else {
	            VideoGroup g3 = (VideoGroup) currentGroup;
	        	FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(g3.getVideo().getAbsolutePath());
	    		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
	    		frameGrabber.start();
	    		int N = frameGrabber.getLengthInFrames();
	    		for(int j = 0; j<N; j++){
	    			Frame frame = frameGrabber.grab();
	    			Mat img = converterToMat.convert(frame);
	    			if(j ==index){
	    				img_src = img.clone();
	    				break;
	    			}
	    		}
	    		frameGrabber.close();
	        }
	        BufferedImage overlay = SwingFXUtils.fromFXImage(writableImage, null);
	        Mat quiverLayer = Java2DFrameUtils.toMat(overlay);
	        MatVector channels = new MatVector();
	        Mat quiverLayerRGB = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
	        org.bytedeco.javacpp.opencv_core.split(quiverLayer, channels);
	        Mat blueCh = channels.get(1);
	        Mat greenCh = channels.get(2);
	        Mat redCh = channels.get(3);
	        MatVector channels2 = new MatVector(3);
	        channels2.put(0, redCh);
	        channels2.put(1, greenCh);
	        channels2.put(2, blueCh);
	        org.bytedeco.javacpp.opencv_core.merge(channels2, quiverLayerRGB);
	        Mat combinedImage = new Mat();
	        if (contour_state == true) {
	        	img_src.convertTo(img_src, CV_8U);
	        	quiverLayerRGB.convertTo(quiverLayerRGB, CV_8U);
	        }
			org.bytedeco.javacpp.opencv_core.addWeighted(img_src, alpha_under_two, quiverLayerRGB, alpha_above_two, 0, combinedImage);
	        if (contour_state == true) {
	        	Mat mask2 = generateContour(img_src);	        	
	        	Mat combinedImage2 = new Mat();
	        	combinedImage.copyTo(combinedImage2, mask2);
	        	
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage2);
				BufferedImage combined1 = colorToAlpha(combined, java.awt.Color.BLACK);
				
				BufferedImage img_src_buffered = Java2DFrameUtils.toBufferedImage(img_src);

				// create the new image, canvas size is the max. of both image sizes
				BufferedImage combined2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

				// paint both images, preserving the alpha channels
				Graphics g = combined2.getGraphics();
				g.drawImage(img_src_buffered, 0, 0, null);
				g.drawImage(combined1, 0, 0, null);
				finalImage = SwingFXUtils.toFXImage(combined2, null);
	        } else {
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage);
		        finalImage = SwingFXUtils.toFXImage(combined, null);
	        }
		} else if (rendertype.equals("JetQuiver")) {
			Boolean to_merge = true;
			WritableImage myWritableImage = writeJetImage(index, to_merge);
			WritableImage myWritableImage2 = writeCanvas(index, to_merge);
	        Mat img_src = null;
	        if (currentGroup.getType() == 0) {
	            ImageGroup g3 = (ImageGroup) currentGroup;
	            File path_img  = g3.getImages().get(main_package.getListPoints().get(index));
	            img_src = imread(path_img.getCanonicalPath());
	        } else {
	            VideoGroup g3 = (VideoGroup) currentGroup;
	        	FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(g3.getVideo().getAbsolutePath());
	    		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
	    		frameGrabber.start();
	    		int N = frameGrabber.getLengthInFrames();
	    		for(int j = 0; j<N; j++){
	    			Frame frame = frameGrabber.grab();
	    			Mat img = converterToMat.convert(frame);
	    			if(j ==index){
	    				img_src = img.clone();
	    				break;
	    			}
	    		}
	    		frameGrabber.close();
	        }
	        BufferedImage overlay = SwingFXUtils.fromFXImage(myWritableImage, null);
	        BufferedImage overlay2 = SwingFXUtils.fromFXImage(myWritableImage2, null);
	        Mat jetLayer = Java2DFrameUtils.toMat(overlay);
	        Mat quiverLayer = Java2DFrameUtils.toMat(overlay2);
	        MatVector channels = new MatVector();
	        Mat jetLayerRGB = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
	        org.bytedeco.javacpp.opencv_core.split(jetLayer, channels);
	        Mat blueCh = channels.get(1);
	        Mat greenCh = channels.get(2);
	        Mat redCh = channels.get(3);
	        MatVector channels2 = new MatVector(3);
	        channels2.put(0, redCh);
	        channels2.put(1, greenCh);
	        channels2.put(2, blueCh);
	        org.bytedeco.javacpp.opencv_core.merge(channels2, jetLayerRGB);
	        MatVector channels3 = new MatVector();
	        Mat quiverLayerRGB = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
	        org.bytedeco.javacpp.opencv_core.split(quiverLayer, channels3);
	        Mat blueCh2 = channels3.get(1);
	        Mat greenCh2 = channels3.get(2);
	        Mat redCh2 = channels3.get(3);
	        MatVector channels4 = new MatVector(3);
	        channels4.put(0, redCh2);
	        channels4.put(1, greenCh2);
	        channels4.put(2, blueCh2);
	        org.bytedeco.javacpp.opencv_core.merge(channels4, quiverLayerRGB);
	        Mat combinedImage = new Mat();
	        if (contour_state == true) {
	        	jetLayerRGB.convertTo(jetLayerRGB, CV_8U);
	        	quiverLayerRGB.convertTo(quiverLayerRGB, CV_8U);
	        }
			org.bytedeco.javacpp.opencv_core.addWeighted(jetLayerRGB, alpha_under_two, quiverLayerRGB, alpha_above_two, 0, combinedImage);
	        if (contour_state == true) {
	        	Mat mask2 = generateContour(img_src);
	        	Mat combinedImage2 = new Mat();
	        	combinedImage.copyTo(combinedImage2, mask2);
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage2);
		        finalImage = SwingFXUtils.toFXImage(combined, null);
	        } else {
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage);
		        finalImage = SwingFXUtils.toFXImage(combined, null);
	        }
		} else if (rendertype.equals("JetQuiverMerge")) {
			Boolean to_merge = true;
			WritableImage myWritableImage = writeJetImage(index, to_merge);
			WritableImage myWritableImage2 = writeCanvas(index, to_merge);
	        Mat img_src = null;
	        if (currentGroup.getType() == 0) {
	            ImageGroup g3 = (ImageGroup) currentGroup;
	            File path_img  = g3.getImages().get(main_package.getListPoints().get(index));
	            img_src = imread(path_img.getCanonicalPath());
	        } else {
	            VideoGroup g3 = (VideoGroup) currentGroup;
	        	FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(g3.getVideo().getAbsolutePath());
	    		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
	    		frameGrabber.start();
	    		int N = frameGrabber.getLengthInFrames();
	    		for(int j = 0; j<N; j++){
	    			Frame frame = frameGrabber.grab();
	    			Mat img = converterToMat.convert(frame);
	    			if(j ==index){
	    				img_src = img.clone();
	    				break;
	    			}
	    		}
	    		frameGrabber.close();
	        }
	        BufferedImage overlay = SwingFXUtils.fromFXImage(myWritableImage, null);
	        BufferedImage overlay2 = SwingFXUtils.fromFXImage(myWritableImage2, null);
	        Mat jetLayer = Java2DFrameUtils.toMat(overlay);
	        Mat quiverLayer = Java2DFrameUtils.toMat(overlay2);
	        MatVector channels = new MatVector();
	        Mat jetLayerRGB = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
	        org.bytedeco.javacpp.opencv_core.split(jetLayer, channels);
	        Mat blueCh = channels.get(1);
	        Mat greenCh = channels.get(2);
	        Mat redCh = channels.get(3);
	        MatVector channels2 = new MatVector(3);
	        channels2.put(0, redCh);
	        channels2.put(1, greenCh);
	        channels2.put(2, blueCh);
	        org.bytedeco.javacpp.opencv_core.merge(channels2, jetLayerRGB);
	        MatVector channels3 = new MatVector();
	        Mat quiverLayerRGB = new Mat(height, width, org.bytedeco.javacpp.opencv_core.CV_8UC3);
	        org.bytedeco.javacpp.opencv_core.split(quiverLayer, channels3);
	        Mat blueCh2 = channels3.get(1);
	        Mat greenCh2 = channels3.get(2);
	        Mat redCh2 = channels3.get(3);
	        MatVector channels4 = new MatVector(3);
	        channels4.put(0, redCh2);
	        channels4.put(1, greenCh2);
	        channels4.put(2, blueCh2);
	        org.bytedeco.javacpp.opencv_core.merge(channels4, quiverLayerRGB);
	        if (contour_state == true) {
	        	img_src.convertTo(img_src, CV_8U);
	        	quiverLayerRGB.convertTo(quiverLayerRGB, CV_8U);
	        	jetLayerRGB.convertTo(jetLayer, CV_8U);
	        }
	        Mat combinedImage = new Mat();
			org.bytedeco.javacpp.opencv_core.addWeighted(img_src, alpha_under_two, jetLayerRGB, alpha_above_two, 0, combinedImage);
			org.bytedeco.javacpp.opencv_core.addWeighted(combinedImage, alpha_under_three, quiverLayerRGB, alpha_above_three, 0, combinedImage);
	        if (contour_state == true) {
	        	Mat mask2 = generateContour(img_src);	        	
	        	Mat combinedImage2 = new Mat();
	        	combinedImage.copyTo(combinedImage2, mask2);
	        	
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage2);
				BufferedImage combined1 = colorToAlpha(combined, java.awt.Color.BLACK);
				
				BufferedImage img_src_buffered = Java2DFrameUtils.toBufferedImage(img_src);

				// create the new image, canvas size is the max. of both image sizes
				BufferedImage combined2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

				// paint both images, preserving the alpha channels
				Graphics g = combined2.getGraphics();
				g.drawImage(img_src_buffered, 0, 0, null);
				g.drawImage(combined1, 0, 0, null);
				finalImage = SwingFXUtils.toFXImage(combined2, null);
	        } else {
				BufferedImage combined = Java2DFrameUtils.toBufferedImage(combinedImage);
		        finalImage = SwingFXUtils.toFXImage(combined, null);
	        }
		} else if (rendertype.equals("Merge")){
			//render image alone
			ToMat converter = new OpenCVFrameConverter.ToMat();
	        Mat img_src = null;
	        if (currentGroup.getType() == 0) {
	            ImageGroup g3 = (ImageGroup) currentGroup;
	            File path_img  = g3.getImages().get(main_package.getListPoints().get(index));
	            img_src = imread(path_img.getCanonicalPath());
	        } else {
	            VideoGroup g3 = (VideoGroup) currentGroup;
	        	FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(g3.getVideo().getAbsolutePath());
	    		OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
	    		frameGrabber.start();
	    		int N = frameGrabber.getLengthInFrames();
	    		for(int j = 0; j<N; j++){
	    			Frame frame = frameGrabber.grab();
	    			Mat img = converterToMat.convert(frame);
	    			if(j ==index){
	    				img_src = img.clone();
	    				break;
	    			}
	    		}
	    		frameGrabber.close();
	        }
	        if (contour_state == true) {
	        	img_src = generateContour(img_src);
	        	img_src.convertTo(img_src, CV_8U);
	        }
	        Frame original_image_frame = converter.convert(img_src);
	        Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	        BufferedImage original_image = paintConverter.getBufferedImage(original_image_frame,1);
	        finalImage = SwingFXUtils.toFXImage(original_image, null);
	        
		} else {
			//render black image
		}
		
		if (image_not_save == false) {
			imgview1.setImage(finalImage);
		} else {
//			imgview1.setImage(finalImage);
			System.out.println("Trying to save image!");
			saveCurrentImageView(finalImage);
		}
	}
	
	public WritableImage writeJetImage(int index, boolean to_merge) {
		WritableImage myWritableImage = new WritableImage(width, height);
        PixelWriter myPixelWriter = myWritableImage.getPixelWriter();
        int done = 0;
        this_jet = new ColorMap(scale_start, scale_end, JET);
        for(int x = 0; x < height; x++){
            for(int y = 0; y < width; y++){            	
            	double final_float;
            	final_float = main_package.getListMags().get(index)[done];
            	if (final_float < mask_value) {
            		Color this_color;
            		if (to_merge == true) {
            			this_color = new Color(1.0,1.0,1.0,0.0);
            		} else {
            			this_color = new Color(0.0,0.0,0.0, 1.0);
            		}
					myPixelWriter.setColor(y, x, this_color);
            		
            	} else  {
            		java.awt.Color a = this_jet.getColor(final_float);
            		Color this_color = new Color( (Double.valueOf(a.getRed()) / 255.0), (Double.valueOf(a.getGreen()) / 255.0 ) , (Double.valueOf(a.getBlue()) / 255.0), 1.0);
            		myPixelWriter.setColor(y, x, this_color);
            	}
            	done = done + 1;
            }
        }
        return myWritableImage;
	}
	
	public WritableImage writeCanvas(int index, boolean to_merge) {
		Canvas canvas1 = new Canvas((int) width, (int) height);
		Mat x_sqrd = new Mat();
    	Mat y_sqrd = new Mat();
    	Mat x_flow = main_package.getXflow(index);
    	Mat y_flow = main_package.getYflow(index);
    	
    	org.bytedeco.javacpp.opencv_core.pow(x_flow, 2.0, x_sqrd);
    	org.bytedeco.javacpp.opencv_core.pow(y_flow, 2.0, y_sqrd);
    	Mat xy_sum = new Mat();
    	org.bytedeco.javacpp.opencv_core.add(x_sqrd, y_sqrd , xy_sum);
    	Mat xy_sum_srqt = new Mat();
    	org.bytedeco.javacpp.opencv_core.sqrt(xy_sum, xy_sum_srqt);
    	Mat x_div = new Mat();
    	Mat y_div = new Mat();
    	org.bytedeco.javacpp.opencv_core.divide(x_flow,  xy_sum_srqt, x_div);
    	org.bytedeco.javacpp.opencv_core.divide(y_flow,  xy_sum_srqt, y_div);
    	org.bytedeco.javacpp.opencv_core.multiplyPut(x_div, 0.8);
    	org.bytedeco.javacpp.opencv_core.multiplyPut(y_div, 0.8);
    	
    	FloatBuffer u_diff = x_div.createBuffer();
    	float[] floatArrayOp = new float[u_diff.capacity()];
    	u_diff.get(floatArrayOp);
    	
    	FloatBuffer v_diff = y_div.createBuffer();
    	float[] floatArrayOp2 = new float[v_diff.capacity()];
    	v_diff.get(floatArrayOp2);
    	float[][] u_total = new float[height][width];
    	float[][] v_total = new float[height][width];
    	double[][] mag_total = new double[height][width];
    	int zy = -1;
    	int zx = 0;
    	for(int z = 0; z < floatArrayOp2.length; z++) {
    		if (z % width == 0) {
    			zy += 1;
    			zx = 0;
    		}
    		u_total[zy][zx] = floatArrayOp2[z];
    		v_total[zy][zx] = floatArrayOp[z];
    		mag_total[zy][zx] = main_package.getListMags().get(index)[z];
    		zx += 1;
    	}
  	
    	
    	GraphicsContext gc = canvas1.getGraphicsContext2D();
    	if (to_merge == false) {
    		gc.setFill(Color.BLACK);
    		gc.fillRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
    	}
    	int maxx = 0;
    	for(int x = 0; x < width; x=x+ywindow){
    		maxx += 1;
    	}
    	int maxy = 0;
    	for(int y = 0; y < height; y=y+xwindow){
    		maxy += 1;
    	}
    	
    	float[][] u_skip = new float[maxy][maxx];
    	float[][] v_skip = new float[maxy][maxx];
    	double[][] mag_skip = new double[maxy][maxx];
    	int zy2 = 0;
    	for (int zy1 = 0; zy1 < height; zy1=zy1+xwindow) {
    		int zx2 = 0;
    		for (int zx1 = 0; zx1 < width; zx1=zx1+ywindow) {
    			u_skip[zy2][zx2] = u_total[zy1][zx1];
    			v_skip[zy2][zx2] = v_total[zy1][zx1];
    			mag_skip[zy2][zx2] = mag_total[zy1][zx1];
    			zx2 += 1;
    		}
    		zy2 += 1;
    	}
    	float vec1[][] = new float[maxy][maxx];
    	for(int y = 0; y < maxy; y++) {
    		for (int x=0; x < maxx; x++) {
    			vec1[y][x] = ywindow * x;
    			//System.out.print(String.valueOf(vec1[y][x]) + " ");
    		}
    		//System.out.println("");
    	}
    	float vec2[][] = new float[maxy][maxx];
    	for(int y = 0; y < maxy; y++) {
    		for (int x=0; x < maxx; x++) {
//    			vec2[y][x] = (height-(xwindow * y));
    			vec2[y][x] = xwindow * y;
    			//System.out.print(String.valueOf(vec2[y][x]) + " ");
    		}
    		//System.out.println("");
    	}
        this_jet = new ColorMap(scale_start, scale_end, JET);
    	for(int y = 0; y < maxy; y++) {
    		for (int x = 0; x < maxx; x++) {
    			float x0 = vec1[y][x];
    			float x1 = vec1[y][x] + v_skip[y][x];
    			float y0 = vec2[y][x];
    			float y1 = vec2[y][x] - u_skip[y][x];
    			Color this_color;
    			if (mag_skip[y][x]< (float)mask_value) {
    				this_color = new Color(0.0,0.0,0.0, 0.0);
    			} else {
            		java.awt.Color a = this_jet.getColor(mag_skip[y][x]);
            		this_color = new Color( (Double.valueOf(a.getRed()) / 255.0), (Double.valueOf(a.getGreen()) / 255.0 ) , (Double.valueOf(a.getBlue()) / 255.0), 1.0);
            		drawArrow(gc, x0, y0, x1, y1, this_color, index);
    			}
    		}
    	}
    	WritableImage writableImage = new WritableImage((int) canvas1.getWidth(), (int)canvas1.getHeight());
    	SnapshotParameters sp = new SnapshotParameters();
    	sp.setFill(Color.TRANSPARENT);
        canvas1.snapshot(sp, writableImage);
        return writableImage;
	}
	
		
    void drawArrow(GraphicsContext gc, float x1, float y1, float x2, float y2, Color color, int index) {
        gc.setFill(color);
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        Transform transform = Transform.translate(x1, y1);
        transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
        gc.setTransform(new Affine(transform));
        gc.setStroke(color);
        gc.setLineWidth(1.8);
        gc.strokeLine(0, 0, len * -10, 0);
        gc.fillPolygon(new double[]{len + 6.5, len- ARR_SIZE, len - ARR_SIZE, len + 6.5}, new double[]{0, -ARR_SIZE , ARR_SIZE, 0}, 4);
    }
    
    private double sigma = 0.15;
    
	public Mat autoCanny(Mat img_src) {
		UByteRawIndexer sI = img_src.createIndexer();
		double [] pixel_values =  new double[img_src.rows() * img_src.cols()];
        int nbChannels = img_src.channels();
		int z = 0;
		for (int y = 0; y < img_src.rows(); y++) {
	        for (int x = 0; x < img_src.cols(); x++) {
	        	pixel_values[z] = sI.get(y, x);
	        	z++;
	        }
		}
		Median median = new Median();
		double medianValue = median.evaluate(pixel_values);
		System.out.println("medianValue");
		System.out.println(medianValue);
		int lower = (int) Math.round(Math.max(0.0, ((0.1-sigma) * medianValue) ) );
		int upper = (int) Math.round(Math.min(255.0, ((0.1+sigma) * medianValue) ) );
		Mat edged = new Mat();
		org.bytedeco.javacpp.opencv_imgproc.Canny(img_src, edged, lower, upper);
		return edged;
//		return edged;
	}
    
	private int kernel_dilation = 3;
	private int blur_size = 5;
	private int kernel_erosion = 11;
	private int kernel_smoothing_contours = 5;
	private int border_value = 60;
		
    public Mat generateContour(Mat img_src) {
//    	https://github.com/bytedeco/javacv/blob/master/samples/ImageSegmentation.java
    	Mat blur = new Mat();
    	org.bytedeco.javacpp.opencv_core.Size rect = new org.bytedeco.javacpp.opencv_core.Size(blur_size,blur_size);
    	org.bytedeco.javacpp.opencv_imgproc.GaussianBlur(img_src, blur, rect , 0);
    	Mat edged = autoCanny(blur);
    	Mat kernel = Mat.ones(kernel_dilation, kernel_dilation, CV_8U).asMat();
    	Mat dilated = new Mat();
    	org.bytedeco.javacpp.opencv_imgproc.dilate(edged, dilated, kernel);
    	Mat mask = Mat.zeros(img_src.rows()+2, img_src.cols()+2, CV_8U).asMat();
    	Mat floodfilled = dilated.clone();
//    	Rect filling = new Rect();
    	org.bytedeco.javacpp.opencv_imgproc.floodFill(floodfilled, mask, new Point(0, 0), new Scalar(255, 255, 255, 255));
    	Mat floodfill_inverse = new Mat();
    	org.bytedeco.javacpp.opencv_core.bitwise_not(floodfilled, floodfill_inverse);
    	Mat FillingHoles = org.bytedeco.javacpp.opencv_core.or(dilated, floodfill_inverse).asMat();
    	Mat kernel2 = Mat.ones(kernel_erosion, kernel_erosion, CV_8U).asMat();
    	Mat eroded = new Mat();
    	org.bytedeco.javacpp.opencv_imgproc.erode(FillingHoles, eroded, kernel2);
    	Mat kernel3 = Mat.ones(kernel_smoothing_contours , kernel_smoothing_contours , CV_8U).asMat();
    	Mat smoothedContour = new Mat();
    	org.bytedeco.javacpp.opencv_imgproc.morphologyEx(eroded, smoothedContour, org.bytedeco.javacpp.opencv_imgproc.MORPH_OPEN, kernel3);
//    	imwrite("/home/marcelo/Desktop/testsmoothedContour.tif", smoothedContour);
    	Mat smoothedContourThresholded = new Mat();
    	double ret = org.bytedeco.javacpp.opencv_imgproc.threshold(smoothedContour, smoothedContourThresholded, 250.0, 255.0, 0);
    	MatVector contours = new MatVector();
    	Mat hierarchy = new Mat();
    	org.bytedeco.javacpp.opencv_imgproc.findContours(smoothedContourThresholded, contours, hierarchy, org.bytedeco.javacpp.opencv_imgproc.RETR_LIST, org.bytedeco.javacpp.opencv_imgproc.CHAIN_APPROX_SIMPLE);
    	System.out.println("contoured!");
    	Mat mask2 = Mat.zeros(img_src.rows(), img_src.cols(), CV_8U).asMat();
    	int areaIndex = -1;
    	double largestArea = 0.0;
    	MatVector contour = new MatVector();
        for (int i = 0; i < contours.size(); i++) {
            //  Find the area of contour
            double area = org.bytedeco.javacpp.opencv_imgproc.contourArea(contours.get(i), false);
            if (area > largestArea) {
            	largestArea = area;
            	areaIndex = i;
            }
        }
    	contour.put(contours.get(areaIndex));
		//https://stackoverflow.com/questions/33800557/python-opencv-draw-contour-only-on-the-outside-border
        org.bytedeco.javacpp.opencv_imgproc.drawContours(mask2, contour, 0, new Scalar(255,255,255,255), border_value, 8, new Mat(), 0, new Point(0,0));
        org.bytedeco.javacpp.opencv_imgproc.drawContours(mask2, contours, areaIndex, new Scalar(255,255,255,255));
        org.bytedeco.javacpp.opencv_imgproc.fillPoly(mask2, contour, new Scalar(255,255,255,255));
        Mat ObjectInterest = mask2.clone();
		Mat combinedImage = new Mat();
//		org.bytedeco.javacpp.opencv_core.addWeighted(img_src, 0.7, mask2, 0.3, 0, combinedImage);
//		imwrite("/home/marcelo/Desktop/mask2.tif", mask2); //TODO REMOVE
//		imwrite("/home/marcelo/Desktop/test.tif", combinedImage); //TODO REMOVE
		Mat cropped = new Mat();
		img_src.copyTo(cropped, mask2);
//		imwrite("/home/marcelo/Desktop/cropped.tif", cropped); //TODO REMOVE
//		return cropped;
		return mask2;
    }
    
    
    
    
	public class SplineShow implements ActionListener {
	    private JFreeChart chart;
	    private ChartPanel panel;
	    private XYPlot plot;
	    private XYDataset dataset;
	    private XYLineAndShapeRenderer original_render;
		private boolean is_curves_on;
		
		public SplineShow (ChartPanel panel1) {
	        this.panel = panel1;
	        this.chart = panel.getChart();
	        this.plot = (XYPlot) chart.getPlot();
	        this.original_render = (XYLineAndShapeRenderer) plot.getRenderer();
	        this.is_curves_on = false;
//			parent_controller.setPolynonState(!this.is_curves_on);
		}
		
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e) {
			boolean state = this.is_curves_on;
			this.is_curves_on = !state;
			if (this.is_curves_on) {
	  			XYSplineRenderer renderer = new XYSplineRenderer();
	  			renderer.setDefaultShapesVisible(false);
	  	        renderer.setDefaultShapesFilled(false);
		        for (int i = 0; i < this.plot.getDataset().getSeriesCount(); i++) {
			        renderer.setSeriesPaint(i, original_render.getSeriesPaint(i));		        	
		        }
		        this.chart.getXYPlot().setRenderer(renderer);
		        main_package.getPlot_preferences().setSplineDefaultState(true);
			} else {
	  			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	  			renderer.setDefaultShapesVisible(false);
	  	        renderer.setDefaultShapesFilled(false);
		        for (int i = 0; i < this.plot.getDataset().getSeriesCount(); i++) {
			        renderer.setSeriesPaint(i, original_render.getSeriesPaint(i));		        	
		        }
		        this.chart.getXYPlot().setRenderer(renderer);
			}
		}
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
