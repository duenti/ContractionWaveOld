package controllers;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.CalculationTaskSave;
import model.ContinueData;
import model.Group;
import model.IntervalPeak;
import model.IntervalPeaks;
import model.PackageData;
import model.Peak;
import model.TimeSpeed;
import model.XYCircleAnnotation;

public class Controller_3d2_PeakParametersPlot implements Initializable {
	private PackageData main_package;
	private double lowerBoundDomain;
    private double upperBoundDomain;
	private double fps_val;
	private double pixel_val;
	private double average_value;
	private double upper_limit;
	private static Group currentGroup;
	private JFreeChart currentChart;
	private Peak currentPeak;
	private static boolean zoomGridLinesState = true;
	private List<IntervalMarker> intervalsList;
	private List<Integer> maximum_list;
	private List<Integer> minimum_list;
	private List<Integer> first_points;
	private List<Integer> fifth_points;
	private IntervalPeaks intervalPeaks;
	private boolean ask_saved;
	//	private ToggleGroup toggleGroup = new ToggleGroup();
	private int start;
	private int stop;
	private int step = 1;
	private List<TimeSpeed> timespeedlist;
	private double delta;
	private double intra;
	private double inter;
	
    @FXML
    private Button cmdBack;

    @FXML
    private Button cmdNext;
    
    @FXML
    private TableView<Peak> timeTableView;

    @FXML
    private TableColumn<Peak, String> posCol;

    @FXML
    private TableColumn<Peak, Double> tcrCol;

    @FXML
    private TableColumn<Peak, Double> tcCol;

    @FXML
    private TableColumn<Peak, Double> trCol;

    @FXML
    private TableColumn<Peak, Double> tc_vmcCol;

    @FXML
    private TableColumn<Peak, Double> tc_vmc_minCol;

    @FXML
    private TableColumn<Peak, Double> tr_vmrCol;

    @FXML
    private TableColumn<Peak, Double> tr_vmr_bCol;

    @FXML
    private TableColumn<Peak, Double> t_vmc_vmrCol;
    
    @FXML
    private TableView<Peak> speedTableView;

    @FXML
    private TableColumn<Peak, String> speedPosCol;

    @FXML
    private TableColumn<Peak, Double> speedVMCCol;

    @FXML
    private TableColumn<Peak, Double> speedVMRCol;

    @FXML
    private TableColumn<Peak, Double> speed_DVMCVMRCol;

//    @FXML
//    private TableColumn<Peak, Double> speedVMINCol;
    
    @FXML
    private TableView<Peak> areaTableView;
    
    @FXML
    private TableColumn<Peak, String> areaPosCol;

    @FXML
    private TableColumn<Peak, Double> speedAREATCol;

    @FXML
    private TableColumn<Peak, Double> speedAREACCol;

//    @FXML
//    private TableColumn<Peak, Double> speedAREARCol;
    
    @FXML
    private SwingNode swgNode;
    
    @FXML
    private RadioButton timeRadio, speedRadio, areaRadio;

    private ToggleGroup radioGroup = new ToggleGroup();
    
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
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
//    	javafx.geometry.Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
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
    void handleExportTSV(ActionEvent event) throws Exception{
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialFileName("time-speed.tsv");
        Stage primaryStage;
    	primaryStage = (Stage) cmdNext.getScene().getWindow();
        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
		writeTSV(file);
		JOptionPane.showMessageDialog(null, "File was saved successfully.");
    }
    
    public void writeTSV(File file) throws Exception {
	    Writer writer = null;
	    try {
	        writer = new BufferedWriter(new FileWriter(file));
	        String text2 = "Time(s)\tSpeed(\u00B5/s)\n";
	        writer.write(text2);
	        
	        for (int i = 0; i < currentGroup.getMagnitudeSize(); i++) {
				double average = currentGroup.getMagnitudeListValue(i);
				writer.write(String.valueOf(i / fps_val));
				writer.write(",");
				writer.write(String.valueOf(average * fps_val * pixel_val));
				writer.write("\n");
			}	        
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    finally {
	        writer.flush();
	        writer.close();
	    } 
	}
    
    @FXML
    void handleExportXLS(ActionEvent event) throws IOException{
    	Workbook workbook = new HSSFWorkbook();
		Sheet spreadsheet = workbook.createSheet("sample");
		Row row = spreadsheet.createRow(0);
		
		row.createCell(0).setCellValue("Time(s)");
		row.createCell(1).setCellValue("Speed(\u00B5/s)");
		
		for (int i = 0; i < currentGroup.getMagnitudeSize(); i++) {
			double average = currentGroup.getMagnitudeListValue(i);
			row = spreadsheet.createRow(i + 1);
			
			row.createCell(0).setCellValue(i / fps_val);
			row.createCell(1).setCellValue(average * fps_val * pixel_val);			
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName("time-speed.xls");
        Stage primaryStage;
    	primaryStage = (Stage) cmdNext.getScene().getWindow();
        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
		
		JOptionPane.showMessageDialog(null, "File was saved successfully.");
    }
    
    @FXML
    void handleExportJPEG(ActionEvent event) throws Exception{
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialFileName("time-speed.jpg");
        Stage primaryStage;
    	primaryStage = (Stage) cmdNext.getScene().getWindow();
        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
        ChartPanel panel = (ChartPanel) swgNode.getContent();
        OutputStream out = new FileOutputStream(file);
        ChartUtils.writeChartAsJPEG(out,
                currentChart,
                panel.getWidth(),
                panel.getHeight());
		JOptionPane.showMessageDialog(null, "File was saved successfully.");
    }
    
    @FXML
    void handleExportPNG(ActionEvent event) throws Exception{
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialFileName("time-speed.png");
        Stage primaryStage;
    	primaryStage = (Stage) cmdNext.getScene().getWindow();
        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);
        ChartPanel panel = (ChartPanel) swgNode.getContent();
        OutputStream out = new FileOutputStream(file);
        ChartUtils.writeChartAsPNG(out,
                currentChart,
                panel.getWidth(),
                panel.getHeight());
		JOptionPane.showMessageDialog(null, "File was saved successfully..");
    }
    
    @FXML
    void handleExportTableXLS(ActionEvent event) throws Exception{
    	if (radioGroup.getSelectedToggle().getUserData().equals("time") == true) {
    		exportTable(timeTableView,0,"time-table.xls"); 
    	} else if (radioGroup.getSelectedToggle().getUserData().equals("speed") == true) {
    		exportTable(speedTableView,0,"speed-table.xls");
    	} else {
    		exportTable(areaTableView,0,"area-table.xls");
    	}
    	JOptionPane.showMessageDialog(null, "File was saved successfully.");
    }
    
    @FXML
    void handleExportTableTSV(ActionEvent event) throws Exception{
    	if (radioGroup.getSelectedToggle().getUserData().equals("time") == true) {
    		exportTable(timeTableView,1,"time-table.tsv"); 
    	} else if (radioGroup.getSelectedToggle().getUserData().equals("speed") == true) {
    		exportTable(speedTableView,1,"speed-table.tsv");
    	} else {
    		exportTable(areaTableView,1,"area-table.tsv");
    	}
    	JOptionPane.showMessageDialog(null, "File was saved successfully.");
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
	        	if (maximum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1) && !minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1) && !minimum_list.contains(x1)  && !first_points.contains(x1)) {
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
	        	if (maximum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1) && !minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1) && !minimum_list.contains(x1)  && !first_points.contains(x1)) {
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
	        	if (maximum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1) && !minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1) && !minimum_list.contains(x1)  && !first_points.contains(x1)) {
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
	        	if (maximum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMaximumDotColorRGB()));
	        	}
	        	if (minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getMinimumDotColorRGB()));
	        	}
	        	if (first_points.contains(x1) && !minimum_list.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getFirstDotColorRGB()));
	        	}
	        	if (fifth_points.contains(x1) && !minimum_list.contains(x1)  && !first_points.contains(x1)) {
	        		plot.addAnnotation(new XYCircleAnnotation(x, y, 5.0, main_package.getPlot_preferences().getLastDotColorRGB()));
	        	}
	        }
		}
    }
    
    
    void save() throws IOException {
    	System.out.println("Save status:");
    	System.out.println(ask_saved);
    	if (ask_saved == false) {

            Stage primaryStage;
        	primaryStage = (Stage) cmdNext.getScene().getWindow();
        	
    		Button buttonTypeOk = new Button("Save");
    		Button buttonTypeSkip = new Button("Skip");
    		Button buttonTypeCancel = new Button("Cancel");
    		
    		Stage dialogMicroscope= new Stage();    		
	    	dialogMicroscope.initModality(Modality.APPLICATION_MODAL);
	    	dialogMicroscope.initOwner(null);
	    	dialogMicroscope.setResizable(true);
	    	GridPane grid = new GridPane();
//	    	grid.setGridLinesVisible(true);
	    	grid.setPrefWidth(250);
	    	grid.setPrefHeight(80);
	    	Label askQuestion = new Label("Save Progress?");
	    	grid.add(askQuestion, 2, 0, 1, 1);
	    	GridPane.setHalignment(askQuestion, HPos.CENTER);
	    	GridPane.setHgrow(askQuestion, Priority.ALWAYS);
	    	GridPane.setVgrow(askQuestion, Priority.ALWAYS);
	    	grid.add(buttonTypeOk, 0, 1, 1, 1);
	    	GridPane.setHalignment(buttonTypeOk, HPos.CENTER);
	    	GridPane.setHgrow(buttonTypeOk, Priority.ALWAYS);
	    	GridPane.setVgrow(buttonTypeOk, Priority.ALWAYS);
	    	grid.add(buttonTypeSkip, 2, 1, 1, 1);
	    	GridPane.setHalignment(buttonTypeSkip, HPos.CENTER);
	    	GridPane.setHgrow(buttonTypeSkip, Priority.ALWAYS);
	    	GridPane.setVgrow(buttonTypeSkip, Priority.ALWAYS);
	    	grid.add(buttonTypeCancel, 4, 1, 1, 1);
	    	GridPane.setHalignment(buttonTypeCancel, HPos.CENTER);
	    	GridPane.setHgrow(buttonTypeCancel, Priority.ALWAYS);
	    	GridPane.setVgrow(buttonTypeCancel, Priority.ALWAYS);
	    	buttonTypeOk.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	            	ContinueData continue_obj = new ContinueData(main_package.getCurrent_groups(), main_package.getCores(), currentGroup, fps_val, pixel_val, average_value, upper_limit, intervalsList, maximum_list, minimum_list, first_points, fifth_points, timespeedlist, main_package.getDelta(), main_package.getInter(), main_package.getIntra());
					FileChooser fileChooser = new FileChooser();
			        File file = fileChooser.showSaveDialog(dialogMicroscope);
					FileOutputStream fout = null;
					try {
						fout = new FileOutputStream(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ObjectOutputStream oos = null;
					try {
						oos = new ObjectOutputStream(fout);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						oos.writeObject(continue_obj);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						oos.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						fout.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					Stage buttonTypeOk = (Stage) cmdNext.getScene().getWindow();
	                dialogMicroscope.close();
	                navigation();
	            }
	        });
	    	buttonTypeSkip.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                dialogMicroscope.close();
	                navigation();
	            }
	        });
	    	buttonTypeCancel.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	                dialogMicroscope.close();
	            }
	        });
        	dialogMicroscope.setScene(new Scene(grid));
	    	dialogMicroscope.show();
	    	
	    	Boolean return_bool;
		} else {
            navigation();
		}
    }
        
    @FXML
    void handleSeriesColor(ActionEvent event) {
    	XYPlot plot = currentChart.getXYPlot();
    	java.awt.Color initialColor = main_package.getPlot_preferences().getSeriesColorRGB();
        java.awt.Color newColor = JColorChooser.showDialog(null, "Choose Series color", initialColor);
        plot.getRenderer().setSeriesPaint(0, newColor);
        main_package.getPlot_preferences().setSeriesColorRGB(newColor);
    }
    
    private void commitColors() {
    	XYPlot plot = currentChart.getXYPlot();
    	main_package.getPlot_preferences().setSeriesColorRGB( (java.awt.Color) plot.getRenderer().getSeriesPaint(0));
    	main_package.getPlot_preferences().setRangeGridColorRGB( (java.awt.Color) plot.getRangeGridlinePaint());
    	main_package.getPlot_preferences().setDomainGridColorRGB( (java.awt.Color) plot.getDomainGridlinePaint());
    	main_package.getPlot_preferences().setBackgroundColorRGB( (java.awt.Color) plot.getBackgroundPaint());
    }
    
    @FXML
    void back(ActionEvent event) throws ClassNotFoundException, IOException {
    	//ask for saving object
		Stage primaryStage = (Stage) cmdNext.getScene().getWindow();
    	double prior_X = primaryStage.getX();
    	double prior_Y = primaryStage.getY();
		URL url = getClass().getResource("FXML_3d_MagnitudeFirstCharts.fxml");
    	FXMLLoader fxmlloader = new FXMLLoader();
    	fxmlloader.setLocation(url);
    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root;
    	root = fxmlloader.load();
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
//    	javafx.geometry.Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
    	Group g1 = currentGroup;
    	commitColors();
		((Controller_3d_MagnitudeFirstCharts)fxmlloader.getController()).setContext(main_package, g1, fps_val, pixel_val, average_value, upper_limit, timespeedlist);	
		primaryStage.setTitle("Image Optical Flow - Subset Main Chart");
//		primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setX(prior_X);
		primaryStage.setY(prior_Y);
    }

    @FXML
    void nextPageNavigate(ActionEvent event) throws IOException {
    	int validation = validateRadio();
    	if(validation == -1) return;
    	save();
    }
    
    void navigation() {
       	double peak_size = (stop - start);
    		int option = alertRAM(peak_size);
    		if(option == -1) return;
    		CalculationTaskSave new_task = new CalculationTaskSave(currentGroup, main_package, stop, start, 1, 0);
    		main_package.getExec().submit(new_task);
    		new_task.setOnSucceeded(ignoredArg -> {
    			System.out.println("Done!");
    			try {
    				System.out.println("Next page!");
    				nextPageDo();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		});
    }
        	
	public void writeTableTSV(File file, TableView viewResultsTable) throws Exception {
	    Writer writer = null;
	    try {
	        writer = new BufferedWriter(new FileWriter(file));
	        String text2 = "";
	        
	        for (int j = 0; j < viewResultsTable.getColumns().size(); j++) {
				if (j == 0) {
					text2 = ((TableColumn<Peak, String>) viewResultsTable.getColumns().get(j)).getText();
				} else {
					text2 = ((TableColumn<Peak, Double>) viewResultsTable.getColumns().get(j)).getText();
				}
				writer.write(text2 + "\t");
			}
	        writer.write("\n");
	        
	        for (int i = 0; i < viewResultsTable.getItems().size(); i++) {
	        	for (int j = 0; j < viewResultsTable.getColumns().size(); j++) {
	        		if (j == 0) {
						if(((TableColumnBase<Peak, String>) viewResultsTable.getColumns().get(j)).getCellData(i) != null) {
							text2 = ((TableColumnBase<Peak, String>) viewResultsTable.getColumns().get(j)).getCellData(i).toString();
						}
						else {
							text2 = "";
						}
						
					} else {
						if(((TableColumnBase<Peak, Double>) viewResultsTable.getColumns().get(j)).getCellData(i) != null) {
							text2 = ((TableColumnBase<Peak, Double>) viewResultsTable.getColumns().get(j)).getCellData(i).toString(); 
						}
						else {
							text2 = "";
						}
					}
	        		writer.write(text2 + "\t");
	        	}
	        	writer.write("\n");
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    finally {
	        writer.flush();
	        writer.close();
	    } 
	}
    
    void exportTable(TableView viewResultsTable, int type, String filename) throws Exception {
		if (type == 0){
			Workbook workbook = new HSSFWorkbook();
			Sheet spreadsheet = workbook.createSheet("sample");
			Row row = spreadsheet.createRow(0);
			
			for (int j = 0; j < viewResultsTable.getColumns().size(); j++) {
				if (j == 0) {
					row.createCell(j).setCellValue(((TableColumn<Peak, String>) viewResultsTable.getColumns().get(j)).getText());
				} else {
					row.createCell(j).setCellValue(((TableColumn<Peak, Double>) viewResultsTable.getColumns().get(j)).getText());
				}
			}
			for (int i = 0; i < viewResultsTable.getItems().size(); i++) {
				row = spreadsheet.createRow(i + 1);
				for (int j = 0; j < viewResultsTable.getColumns().size(); j++) {

					if (j == 0) {
						if(((TableColumnBase<Peak, String>) viewResultsTable.getColumns().get(j)).getCellData(i) != null) {
							row.createCell(j).setCellValue(((TableColumnBase<Peak, String>) viewResultsTable.getColumns().get(j)).getCellData(i).toString()); 
						}
						else {
							row.createCell(j).setCellValue("");
						}
						
					} else {
						if(((TableColumnBase<Peak, Double>) viewResultsTable.getColumns().get(j)).getCellData(i) != null) {
							row.createCell(j).setCellValue(((TableColumnBase<Peak, Double>) viewResultsTable.getColumns().get(j)).getCellData(i).toString()); 
						}
						else {
							row.createCell(j).setCellValue("");
						}
					}
				}
			}
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialFileName(filename);
	        Stage primaryStage;
	    	primaryStage = (Stage) cmdNext.getScene().getWindow();
	        //Show save file dialog
	        File file = fileChooser.showSaveDialog(primaryStage);
			FileOutputStream fileOut = new FileOutputStream(file);
			workbook.write(fileOut);
			workbook.close();
			fileOut.close();
		} else {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialFileName(filename);
	        Stage primaryStage;
	    	primaryStage = (Stage) cmdNext.getScene().getWindow();
	        //Show save file dialog
	        File file = fileChooser.showSaveDialog(primaryStage);
			writeTableTSV(file, viewResultsTable);
		}
    }
        
    private void nextPageDo() throws IOException {
		Stage primaryStage = (Stage) cmdNext.getScene().getWindow();
    	double prior_X = primaryStage.getX();
    	double prior_Y = primaryStage.getY();
//		URL url = getClass().getResource("FXML_3e_ViewJetQuiverMerge.fxml");
		URL url = getClass().getResource("FXML_3e_ViewJetQuiverMergeSingle.fxml");
    	FXMLLoader fxmlloader = new FXMLLoader();
    	fxmlloader.setLocation(url);
    	fxmlloader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent root;
    	root = fxmlloader.load();
    	Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
//    	javafx.geometry.Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
//    	Scene scene = new Scene(root, screenSize.getWidth(), screenSize.getHeight());
    	commitColors();
//    	((Controller_3e_ViewJetQuiverMerge)fxmlloader.getController()).setContext(main_package, currentGroup, fps_val, pixel_val, average_value, upper_limit, use_double, start, stop, step, timespeedlist);
    	((Controller_3e_ViewJetQuiverMergeSingle)fxmlloader.getController()).setContext(main_package, currentGroup, fps_val, pixel_val, average_value, upper_limit, start, stop, step, intervalsList, maximum_list, minimum_list, first_points, fifth_points, timespeedlist, ask_saved);
    	primaryStage.setTitle("Image Optical Flow - Peak Parameters Plot");
//    	primaryStage.setMaximized(true);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		primaryStage.setX(prior_X);
		primaryStage.setY(prior_Y);
    }
    
    
	private int alertRAM(double frames){
		double height = currentGroup.getHeight();
		double width = currentGroup.getWidth();
		double consumption = (frames * height * width * Double.BYTES) / (1024 * 1024);
		double freeRAM = Runtime.getRuntime().freeMemory();
		
		if(consumption > freeRAM){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Generating Plots Alert");
			alert.setHeaderText("The approximate memory usage for this operation is: " + String.valueOf(consumption) + "MB");
			alert.setContentText("Choose your option:");
	
			ButtonType buttonTypeOne = new ButtonType("Run using Memory (faster)");
			ButtonType buttonTypeTwo = new ButtonType("Save each result in files (slower)");
			ButtonType buttonTypeCancel = new ButtonType("Cancel operation", ButtonData.CANCEL_CLOSE);
	
			alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
	
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == buttonTypeOne){
			    // ... user chose "One"
				return 0;
			} else if (result.get() == buttonTypeTwo) {
			    // ... user chose "Two"
				// TODO: Write option for buffering files
				return 1;
			} else {
			    // ... user chose CANCEL or closed the dialog
				return -1;
			}
		}
		return 0;
	}
	
	private int validateRadio() {
		if (timeTableView.getSelectionModel().getSelectedItem() == null) return -1;
		return 1;
	}
        
	public void setContext(PackageData main_package_data, Group g1, double fps_val1, double pixel_val1, double average_value2, double upper_limit2, List<IntervalMarker> intervalsList1, List<Integer> maximum_list1, List<Integer> minimum_list1, List<Integer> first_points1, List<Integer> fifth_points1, List<TimeSpeed>timespeedlist2, boolean saved) throws IOException, ClassNotFoundException {
		//read file, plot first graph, adapt second
		main_package = main_package_data;
		average_value = average_value2;
		upper_limit = upper_limit2;
		currentGroup = g1;
//		currentChart = currentChart2;
		intervalsList = intervalsList1;
		maximum_list = maximum_list1;
		minimum_list = minimum_list1;
		first_points = first_points1;
		fifth_points = fifth_points1;
		fps_val = fps_val1;
		pixel_val = pixel_val1;
		//peak parameters should be calculated here
		intervalPeaks = new IntervalPeaks();
		intervalPeaks.getListPeaks().clear();
		timespeedlist = timespeedlist2;
		ask_saved = saved;
		
    	System.out.println("intervalsList.size()");
    	System.out.println(intervalsList.size());
      	for (IntervalMarker e : intervalsList) {
    		IntervalPeak f = new IntervalPeak(currentGroup, fps_val, pixel_val, e, maximum_list, minimum_list, first_points, fifth_points);
    		intervalPeaks.addIntervalPeak(f);
    	}
    	System.out.println("intervalPeaks.getListPeaks().size()");
    	System.out.println(intervalPeaks.getListPeaks().size());
    	//for each peak in intervalPeaks, a reference in table should be created
    	timeTableView.setItems(intervalPeaks.getObservableList());
    	speedTableView.setItems(intervalPeaks.getObservableList());
    	areaTableView.setItems(intervalPeaks.getObservableList());
    	currentPeak = intervalPeaks.getListPeaks().get(0);
		int to_val;
		int from_val;
		if (currentPeak.getF_point() - 1 >= 0) {
			from_val = currentPeak.getF_point() - 1;
		} else {
			from_val = 0;
		}
		start = from_val;
		if (currentPeak.getEnd_point() + 1 <= currentGroup.size() - 1) {
			to_val = currentPeak.getEnd_point() + 1;
		} else {
			to_val = currentGroup.size() - 1;
		}
		stop = to_val;
		System.out.println("start , stop:");
		System.out.println(start + "," + stop);
    	writeLinePlot(start, stop);
    	timeTableView.getSelectionModel().selectFirst();
    	speedTableView.getSelectionModel().selectFirst();
    	areaTableView.getSelectionModel().selectFirst();
	}
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		timeRadio.setToggleGroup(radioGroup);
		speedRadio.setToggleGroup(radioGroup);
		areaRadio.setToggleGroup(radioGroup);
		timeRadio.setUserData("time");
		speedRadio.setUserData("speed");
		areaRadio.setUserData("area");
		timeRadio.setSelected(true);
		radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			
			@Override
		    public void changed(ObservableValue<? extends Toggle> ov,
		        Toggle old_toggle, Toggle new_toggle) {
		            if (radioGroup.getSelectedToggle() != null) {
		            	if (radioGroup.getSelectedToggle().getUserData().equals("time") == true) {
		            		timeTableView.setVisible(true);
		            		speedTableView.setVisible(false);
		            		areaTableView.setVisible(false);
		            	} else if (radioGroup.getSelectedToggle().getUserData().equals("speed") == true) {
		            		timeTableView.setVisible(false);
		            		speedTableView.setVisible(true);
		            		areaTableView.setVisible(false);
		            	} else {
		            		timeTableView.setVisible(false);
		            		speedTableView.setVisible(false);
		            		areaTableView.setVisible(true);
		            	}
		            }                
		        }
		});
		
		
		Image imgBack = new Image(getClass().getResourceAsStream("/left-arrow-angle.png"));
		cmdBack.setGraphic(new ImageView(imgBack));
		Tooltip tooltip5 = new Tooltip();
		tooltip5.setText("Back to microscope parametrization");
		cmdBack.setTooltip(tooltip5);
		
		Image imgNext = new Image(getClass().getResourceAsStream("/right-arrow-angle.png"));
		cmdNext.setGraphic(new ImageView(imgNext));
		Tooltip tooltip6 = new Tooltip();
		tooltip6.setText("Display and Save Jet, Quiver and Merge plots");
		cmdNext.setTooltip(tooltip6);
		
		
		speedTableView.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		speedTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		speedTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if ((newSelection != null) && (currentPeak.getF_point() != timeTableView.getSelectionModel().getSelectedItem().getF_point()) && (currentPeak.getEnd_point() != timeTableView.getSelectionModel().getSelectedItem().getEnd_point())) {
		    	currentPeak = timeTableView.getSelectionModel().getSelectedItem();
		    	int index = timeTableView.getSelectionModel().getSelectedIndex();
				int to_val;
				int from_val;
				if (currentPeak.getF_point() - 1 >= 0) {
					System.out.println("case start");
					from_val = currentPeak.getF_point() - 1;
				} else {
					from_val = 0;
				}
				start = from_val;
				if (currentPeak.getEnd_point() + 1 <= currentGroup.size() - 1) {
					System.out.println("case end");
					to_val = currentPeak.getEnd_point() + 1;
				} else {
					to_val = currentGroup.size() - 1;
				}
				stop = to_val;
				System.out.println("start , stop:");
				System.out.println(start + "," + stop);
		    	writeLinePlot(start, stop);
		    	timeTableView.getSelectionModel().select(index);
		    	areaTableView.getSelectionModel().select(index);
		    }
		});
		
		
		timeTableView.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		timeTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		timeTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if ((newSelection != null) && (currentPeak.getF_point() != timeTableView.getSelectionModel().getSelectedItem().getF_point()) && (currentPeak.getEnd_point() != timeTableView.getSelectionModel().getSelectedItem().getEnd_point())) {
		    	currentPeak = timeTableView.getSelectionModel().getSelectedItem();
		    	int index = timeTableView.getSelectionModel().getSelectedIndex();
				int to_val;
				int from_val;
				if (currentPeak.getF_point() - 1 >= 0) {
					from_val = currentPeak.getF_point() - 1;
				} else {
					from_val = 0;
				}
				start = from_val;
				if (currentPeak.getEnd_point() + 1 <= currentGroup.size() - 1) {
					to_val = currentPeak.getEnd_point() + 1;
				} else {
					to_val = currentGroup.size() - 1;
				}
				stop = to_val;
				System.out.println("start , stop:");
				System.out.println(start + "," + stop);
		    	writeLinePlot(start, stop);
		    	speedTableView.getSelectionModel().select(index);
		    	areaTableView.getSelectionModel().select(index);
		    }
		});
		
		areaTableView.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		areaTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		areaTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
		    if ((newSelection != null) && (currentPeak.getF_point() != areaTableView.getSelectionModel().getSelectedItem().getF_point()) && (currentPeak.getEnd_point() != areaTableView.getSelectionModel().getSelectedItem().getEnd_point())) {
		    	currentPeak = areaTableView.getSelectionModel().getSelectedItem();
		    	int index = areaTableView.getSelectionModel().getSelectedIndex();
				int to_val;
				int from_val;
				if (currentPeak.getF_point() - 1 >= 0) {
					from_val = currentPeak.getF_point() - 1;
				} else {
					from_val = 0;
				}
				start = from_val;
				if (currentPeak.getEnd_point() + 1 <= currentGroup.size() - 1) {
					to_val = currentPeak.getEnd_point() + 1;
				} else {
					to_val = currentGroup.size() - 1;
				}
				stop = to_val;
				System.out.println("start , stop:");
				System.out.println(start + "," + stop);
		    	writeLinePlot(start, stop);
		    	speedTableView.getSelectionModel().select(index);
		    	timeTableView.getSelectionModel().select(index);
		    }
		});		
		

	    
	    
//		loadedColumn.setMaxWidth( 1f * Integer.MAX_VALUE * 3 ); // 1% width
		posCol.setMaxWidth( 1f * Integer.MAX_VALUE * 12 ); // 11% width
		
		//Time Labels and Size
		
		Label tcrLabel = new Label("CRT");
	    tcrLabel.setTooltip(new Tooltip("Contraction-Relaxation Time"));
	    tcrCol.setText("");
	    tcrCol.setGraphic(tcrLabel);
		tcrCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label ctLabel = new Label("CT");
	    ctLabel.setTooltip(new Tooltip("Contraction Time"));
	    tcCol.setText("");
	    tcCol.setGraphic(ctLabel);
		tcCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label rtLabel = new Label("RT");
	    rtLabel.setTooltip(new Tooltip("Relaxation Time"));
	    trCol.setText("");
	    trCol.setGraphic(rtLabel);
		trCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label vmcLabel = new Label("CT-VMC");
	    vmcLabel.setTooltip(new Tooltip("Contraction Time up to VMC"));
	    tc_vmcCol.setText("");
	    tc_vmcCol.setGraphic(vmcLabel);
		tc_vmcCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label vmcMinLabel = new Label("CT-MS");
		vmcMinLabel.setTooltip(new Tooltip("Contraction Time up to Minimum Speed"));
	    tc_vmc_minCol.setText("");
	    tc_vmc_minCol.setGraphic(vmcMinLabel);
		tc_vmc_minCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label tr_vmrLabel = new Label("RT-VMR");
		tr_vmrLabel.setTooltip(new Tooltip("Relaxation Time up to VMR"));
	    tr_vmrCol.setText("");
	    tr_vmrCol.setGraphic(tr_vmrLabel);
		tr_vmrCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label tr_vmr_bLabel = new Label("RT-B");
		tr_vmr_bLabel.setTooltip(new Tooltip("Relaxation Time up to Basal"));
	    tr_vmr_bCol.setText("");
	    tr_vmr_bCol.setGraphic(tr_vmr_bLabel);
		tr_vmr_bCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width
		
		Label t_vmc_vmrLabel = new Label("MCS/MRS-DT");
		t_vmc_vmrLabel.setTooltip(new Tooltip("MCS/MRS Difference Time"));
	    t_vmc_vmrCol.setText("");
	    t_vmc_vmrCol.setGraphic(t_vmc_vmrLabel);
		t_vmc_vmrCol.setMaxWidth( 1f * Integer.MAX_VALUE * 11 ); // 11% width

		
		//Speed Labels and Size
		
		speedPosCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
		
		Label speedVMCLabel = new Label("MCS");
		speedVMCLabel.setTooltip(new Tooltip("Maximum Contraction Speed"));
	    speedVMCCol.setText("");
	    speedVMCCol.setGraphic(speedVMCLabel);
		speedVMCCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
		
		Label speedVMRLabel = new Label("MRS");
		speedVMRLabel.setTooltip(new Tooltip("Maximum Relaxation Speed"));
	    speedVMRCol.setText("");
	    speedVMRCol.setGraphic(speedVMRLabel);
		speedVMRCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
		
		Label speed_DVMCVMRLabel = new Label("MCS/MRS-DS");
		speed_DVMCVMRLabel.setTooltip(new Tooltip("MCS/MRS Difference Speed"));
	    speed_DVMCVMRCol.setText("");
	    speed_DVMCVMRCol.setGraphic(speed_DVMCVMRLabel);
		speed_DVMCVMRCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
		
//		Label speedVMINLabel = new Label("VMIN");
//		speedVMINLabel.setTooltip(new Tooltip(""));
////	    speedVMINCol.setText("");
//	    speedVMINCol.setGraphic(speedVMINLabel);
//		speedVMINCol.setMaxWidth( 1f * Integer.MAX_VALUE * 20 );
//		
		//Area Labels and Size
		
		areaPosCol.setMaxWidth( 1f * Integer.MAX_VALUE * 34 );
		
		Label speedAREATLabel = new Label("CRA");
		speedAREATLabel.setTooltip(new Tooltip("Contraction-Relaxation Area"));
	    speedAREATCol.setText("");
	    speedAREATCol.setGraphic(speedAREATLabel);
		speedAREATCol.setMaxWidth( 1f * Integer.MAX_VALUE * 33 );
		
		Label speedAREACLabel = new Label("SFA");
		speedAREACLabel.setTooltip(new Tooltip("Shortening Fraction Area"));
	    speedAREACCol.setText("");
	    speedAREACCol.setGraphic(speedAREACLabel);
		speedAREACCol.setMaxWidth( 1f * Integer.MAX_VALUE * 33 );
		
//		Label speedAREARLabel = new Label("");
//		speedAREARLabel.setTooltip(new Tooltip(""));
//	    speedAREARCol.setText("");
//	    speedAREARCol.setGraphic(speedAREARLabel);
//		speedAREARCol.setMaxWidth( 1f * Integer.MAX_VALUE * 25 );
		
		//Fit data to table
		posCol.setCellValueFactory(new PropertyValueFactory<Peak,String>("pos"));
		tcrCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tcr"));
		tcCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tc"));
		trCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tr"));
		tc_vmcCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tc_vmc"));
		tc_vmc_minCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tc_vmc_min"));
		tr_vmrCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tr_vmr"));
		tr_vmr_bCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("tr_vmr_b"));
		t_vmc_vmrCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("t_vmc_vmr"));
		
		speedPosCol.setCellValueFactory(new PropertyValueFactory<Peak,String>("pos"));
		speedVMCCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("vmc"));
		speedVMRCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("vmr"));
		speed_DVMCVMRCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("d_vmc_vmr"));
//		speedVMINCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("vmin"));
		
		areaPosCol.setCellValueFactory(new PropertyValueFactory<Peak,String>("pos"));
		speedAREATCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("area_t"));
		speedAREACCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("area_c"));
//		speedAREARCol.setCellValueFactory(new PropertyValueFactory<Peak,Double>("area_r"));
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

        swgNode.setContent(linepanel);
	}
	
	private XYDataset createDataset(int min, int max) {
		XYSeries series1 = new XYSeries("Optical Flow");
		for (int i = min; i < max; i++) {
			double average = currentGroup.getMagnitudeListValue(i);
			series1.add(i / fps_val, average * fps_val * pixel_val);
		}
		//peak detection algorithm receives a group
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        return dataset;
    }
	
	
	private JFreeChart createChart(XYDataset dataset, int min) {			
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Main Plot",
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
//        		System.out.println("I am called after a zoom event (and some other events too).");
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
}