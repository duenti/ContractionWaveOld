package controllers;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacv.Java2DFrameUtils;

import edu.mines.jtk.awt.ColorMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller_Scale implements Initializable{
	
	private double mask_value;
	private double scale_start;
	private double scale_end;
	private int width = 30;
	private int height = 200;
	private int spacing = 20;
	private int font_size = 15;
	public static final IndexColorModel JET = ColorMap.getJet();
	private ColorMap this_jet;
	
//	@FXML
//	private Canvas testcanvas;
	@FXML
	private TextField heightInput, widthInput, spacingInput, fontInput;
	
	@FXML
	private CheckBox tickCheck;
	
	@FXML
	private Button btnScale;
	
	private static Path rootDir; // The chosen root or source directory
	private static final String DEFAULT_DIRECTORY =
            System.getProperty("user.dir"); //  or "user.home"
	
	private static Path getInitialDirectory() {
        return (rootDir == null) ? Paths.get(DEFAULT_DIRECTORY) : rootDir;
    }
	
	@FXML
	void handleGenScale(ActionEvent event) {
		//Ask for directory
		DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Saving path selection:");
        chooser.setInitialDirectory(getInitialDirectory().toFile());
    	Stage primaryStage;
    	primaryStage = (Stage) tickCheck.getScene().getWindow();
        File chosenDir = chooser.showDialog(primaryStage);
    	String current_filename = chosenDir.getAbsolutePath().toString() + "/" + "scale" +".jpg";
        File file = new File(current_filename);
    	BufferedImage bImage1 = generateCanvasScale();
    	Mat jetLayer = Java2DFrameUtils.toMat(bImage1);
        MatVector channels = new MatVector();
        Mat jetLayerRGB = new Mat(height+((height * 0.1 )+50), width+(width/2+80), org.bytedeco.javacpp.opencv_core.CV_8UC3);
        org.bytedeco.javacpp.opencv_core.split(jetLayer, channels);
        Mat blueCh = channels.get(1);
        Mat greenCh = channels.get(2);
        Mat redCh = channels.get(3);
        MatVector channels2 = new MatVector(3);
        channels2.put(0, redCh);
        channels2.put(1, greenCh);
        channels2.put(2, blueCh);
        org.bytedeco.javacpp.opencv_core.merge(channels2, jetLayerRGB);
        BufferedImage bImage = Java2DFrameUtils.toBufferedImage(jetLayerRGB);
    	if (bImage == null) {
    		System.out.println("Null Buffer");
    	}
    	try {
    		System.out.println("Saving....");
    		ImageIO.write(bImage, "jpg", file);
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}
		
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		heightInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	heightInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
				height = Integer.valueOf(newValue);			        	
				if (height < 50) {
					height = 50;
					heightInput.setText(String.valueOf(height));
				}	        	
		    }
		});
		
		widthInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	widthInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
				width = Integer.valueOf(newValue);		        	
				if (width < 30) {
					width = 30;
					widthInput.setText(String.valueOf(width));
				}
		    }
		});
		
		spacingInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	spacingInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
	        	if (spacing < height && spacing < width) {
	        		spacing = Integer.valueOf(newValue);
	        	}
		    }
		});
		
		fontInput.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, 
		        String newValue) {
		        if (!newValue.matches("\\d*")) {
		        	fontInput.setText(newValue.replaceAll("[^\\d]", ""));
		        }
	        	if (font_size < height && font_size < width) {
	        		font_size = Integer.valueOf(newValue);
	        	}
		    }
		});
		
	}
	
	public void setContext(double mask_value1, double scale_start1, double scale_end1) {
		System.out.println("About to gen test scale 1");
		mask_value = mask_value1;
		scale_start = scale_start1;
		scale_end = scale_end1;
		this_jet = new ColorMap(scale_start, scale_end, JET);
	}
	
	private double convertToMag(double x) {
		double a = (scale_end-scale_start) / height;
		double b = scale_start;
		return ((x*a)+b);
	}
	
	private final int ARR_SIZE = 8;
	
	void drawArrow(GraphicsContext gc, int x1, int y1, int x2, int y2) {
	    gc.setFill(Color.BLACK);

	    double dx = x2 - x1, dy = y2 - y1;
	    double angle = Math.atan2(dy, dx);
	    int len = (int) Math.sqrt(dx * dx + dy * dy);

	    Transform transform = Transform.translate(x1, y1);
	    transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
	    gc.setTransform(new Affine(transform));
	    gc.strokeLine(0, 0, len, 0);
	    gc.fillPolygon(new double[]{len, len - ARR_SIZE, len - ARR_SIZE, len}, new double[]{0, -ARR_SIZE, ARR_SIZE, 0},
	            4);
	}
	
	private BufferedImage generateCanvasScale() {
//		System.out.println("About to gen test scale 2");
//		testcanvas.setWidth(width);
//		testcanvas.setHeight(height);
		Canvas canvas1 = new Canvas((int) width+(width/2+80), (int) height+((height * 0.1 )+50));
		GraphicsContext gc = canvas1.getGraphicsContext2D();
    	gc.setFill(Color.WHITE);
    	gc.fillRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
		PixelWriter pixel_writing = canvas1.getGraphicsContext2D().getPixelWriter();
		for (int x = 0; x < width; x++) {
			for (int y = spacing; y < height+spacing; y++) {
				int y_new = y-spacing;
        		java.awt.Color a = this_jet.getColor(convertToMag(y_new));
//				java.awt.Color a = this_jet.getColor(convertToMag(y));
        		Color this_color = new Color( (Double.valueOf(a.getRed()) / 255.0), (Double.valueOf(a.getGreen()) / 255.0 ) , (Double.valueOf(a.getBlue()) / 255.0), 1.0);
				pixel_writing.setColor(x, y, this_color);
			}
		}
		//write tick labels
		if (tickCheck.isSelected() == true) {
			gc.setFill(Color.BLACK);
			gc.setTextAlign(TextAlignment.CENTER);
	        gc.setTextBaseline(VPos.CENTER);
	        gc.setFont(new Font("Arial", font_size));
			int tick_number = height / spacing;
			for (int t_i = 0; t_i < tick_number+1; t_i++) {
				int x = width+20;
				int y = ((t_i+1) * spacing);
				int y_new = (t_i * spacing);
				gc.fillText(String.valueOf(convertToMag(y_new)).substring(0, 4), x, y);
			}
		}
//		gc.save();
		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(new Font("Arial", 15));
//        gc.rotate(-90);
	    gc.fillText("\u00B5/s", (int)canvas1.getWidth()-30, (int)(canvas1.getHeight()/2));
//	    gc.rotate(90);
//	    gc.restore();
		drawArrow(gc, (int)canvas1.getWidth()-45, (int)(canvas1.getHeight()/2)-(10*height/50), (int)canvas1.getWidth()-45, (int)(canvas1.getHeight()/2)+(10*height/50));
//		gc.restore();
		
    	WritableImage writableImage = new WritableImage((int) canvas1.getWidth(), (int)canvas1.getHeight());
    	SnapshotParameters sp = new SnapshotParameters();
    	sp.setFill(Color.TRANSPARENT);
        canvas1.snapshot(sp, writableImage);
        BufferedImage final_scale = SwingFXUtils.fromFXImage(writableImage, null);
        return final_scale;
	}

}