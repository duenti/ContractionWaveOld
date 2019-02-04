package model;

import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.cartToPolar;
import static org.bytedeco.javacpp.opencv_core.extractChannel;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_video.calcOpticalFlowFarneback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class CalculationTask extends Task<Void> {
    private Group thisgroup;
    private double pyrScale;
    private double polySigma;
    private int levels;
    private int winSize;
    private int iterations;
    private int polyN;
    private PackageData parent;
    private boolean save_data;

    public CalculationTask(Group group, PackageData parentPackage, boolean save_data1) {
        this.thisgroup = group;
        this.pyrScale = group.getPyrScale();
        this.pyrScale = group.getPyrScale();
        this.polySigma = group.getPolySigma();
        this.levels = group.getLevels();
        this.winSize = group.getWinSize();
        this.iterations = group.getIterations();
        this.polyN = group.getPolyN();
        this.parent = parentPackage;
        this.save_data = save_data1;
    }
    
    public long averageLongs(List<Long> time_averages) {
    	long sum = 0;
    	for (long a : time_averages) {
    		sum += a;
    	}
    	return (long) sum/ (long) time_averages.size();
    }
    
    

    public void calculateMagnitudes(Group g2, PackageData parent) {
    	ImageGroup g = (ImageGroup) g2;
		g.clearMagnitudeList();
		thisgroup.setStatus("Running");
		List<File> images = g.getImages();
		List<Long> time_average = new ArrayList<Long>();
		for(int i = 0; i < images.size()-1; i++){
			System.out.print(i + " ");
			Instant start = Instant.now();
			File file1 = images.get(i);
			File file2 = images.get(i+1);
			Mat img1 = imread(file1.getAbsolutePath());
			cvtColor(img1, img1, COLOR_BGR2GRAY);
			Mat img2 = imread(file2.getAbsolutePath());
			cvtColor(img2, img2, COLOR_BGR2GRAY);
			
			Mat flow = new Mat();
			Mat x_flow = new Mat();
		   	Mat y_flow = new Mat();
		   	Mat mag = new Mat();
		   	Mat ang = new Mat();
			calcOpticalFlowFarneback(img1, img2, flow, this.pyrScale, this.levels, this.winSize, this.iterations, this.polyN, this.polySigma, 0);
			extractChannel(flow, x_flow, 0);
		   	extractChannel(flow, y_flow, 1);
		   	cartToPolar(x_flow, y_flow, mag, ang);
		   	Mat floatMags = new Mat();
		   	mag.convertTo(floatMags, CV_32F);
		   	FloatBuffer floatBufferMag = floatMags.createBuffer();
		   	float[] floatArrayMag = new float[floatBufferMag.capacity()];
		   	floatBufferMag.get(floatArrayMag);
		   	float sum = 0;
		   	for(float izx : floatArrayMag) {       
		   	    sum += izx;
		   	}
		   	double magAverage = java.lang.Math.abs(sum / floatArrayMag.length);
		   	g.addToMagnitudeList(magAverage);  	
		   	System.out.print(sum);
		   	System.out.print(" ");
		   	System.out.println(floatArrayMag.length);
		   	double perc = (double)(i+1)/(double) (images.size()-1);
			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			
			time_average.add(timeElapsed.toMillis());
			long total_seconds = averageLongs(time_average) * (images.size() - i);
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
//					parent.setProgress(perc);
					thisgroup.setProgress(perc);
					long time_end = Long.valueOf(total_seconds);
					//System.out.println(total_seconds);
					String ending = " milliseconds";
					if (time_end > 1000) {
						time_end = Long.valueOf(time_end / 1000);
						ending = " seconds";
					}
					if (time_end > 60) {
						time_end = Long.valueOf(time_end / 60);
						ending = " minutes";
					}
					if (time_end > 60) {
						time_end = Long.valueOf(time_end / 60);
						ending = " hours";
					}

					String timeAvg = String.valueOf(time_end) + ending;
//					parent.setTimeAvg(timeAvg);
					thisgroup.setRemainingTime(timeAvg);
				}
	        });

		}
	}

	public void calculateMagnitudesVideo(Group g2, PackageData parent) throws Exception{
			VideoGroup g = (VideoGroup) g2;
			g.clearMagnitudeList();
			thisgroup.setStatus("Running");
			List<Long> time_average = new ArrayList<Long>();
			try{
				File video = g.getVideo();
				FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video.getAbsolutePath());
				OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
				frameGrabber.start();
				Frame frame1 = frameGrabber.grab();
				
				int N = frameGrabber.getLengthInFrames();
				for(int i = 1; i<N; i++){
					System.out.print(i + " ");
					Instant start = Instant.now();
					Mat img1 = converterToMat.convert(frame1);
					cvtColor(img1, img1, COLOR_BGR2GRAY);
					Frame frame2 = frameGrabber.grab();
					Mat img2 = converterToMat.convert(frame2);
					cvtColor(img2, img2, COLOR_BGR2GRAY);
					frame1 = frame2;
					
					Mat flow = new Mat();
					Mat x_flow = new Mat();
			    	Mat y_flow = new Mat();
			    	Mat mag = new Mat();
			    	Mat ang = new Mat();
					calcOpticalFlowFarneback(img1, img2, flow, this.pyrScale, this.levels, this.winSize, this.iterations, this.polyN, this.polySigma, 0);
					extractChannel(flow, x_flow, 0);
			    	extractChannel(flow, y_flow, 1);
			    	cartToPolar(x_flow, y_flow, mag, ang);
			    	Mat floatMags = new Mat();
			    	mag.convertTo(floatMags, CV_32F);
			    	FloatBuffer floatBufferMag = floatMags.createBuffer();
			    	float[] floatArrayMag = new float[floatBufferMag.capacity()];
			    	floatBufferMag.get(floatArrayMag);
			    	
			    	float sum = 0;
			    	for(float izx : floatArrayMag) {       
			    	    sum += izx;
			    	}
			    	
			    	double magAverage = java.lang.Math.abs(sum / floatArrayMag.length);
			    	g.addToMagnitudeList(magAverage);
			    	System.out.print(sum);
				   	System.out.print(" ");
				   	System.out.println(floatArrayMag.length);
			    	double perc = (double)(i+1)/(double) (N);
			    	
					Instant end = Instant.now();
					Duration timeElapsed = Duration.between(start, end);
					time_average.add(timeElapsed.toMillis());
					long total_seconds = averageLongs(time_average) * (N - i);
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
//							parent.setProgress(perc);
							thisgroup.setProgress(perc);
							long time_end = Long.valueOf(total_seconds);
							String ending = " milliseconds";
							if (time_end > 1000) {
								time_end = Long.valueOf(time_end / 1000);
								ending = " seconds";
							}
							if (time_end > 60) {
								time_end = Long.valueOf(time_end / 60);
								ending = " minutes";
							}
							if (time_end > 60) {
								time_end = Long.valueOf(time_end / 60);
								ending = " hours";
							}
							String timeAvg = String.valueOf(time_end) + ending;
//							parent.setTimeAvg(timeAvg);
							thisgroup.setRemainingTime(timeAvg);
						}
			        });
				}
				frameGrabber.flush();
				frameGrabber.close();
			}catch(Exception e){
				e.printStackTrace();
			}
	}
    
	@Override
	protected Void call() throws Exception {
		try {
			String name = this.thisgroup.getName();
			updateMessage(name);
			int type = this.thisgroup.getType();
			if (type == 0) {//Image
				calculateMagnitudes(this.thisgroup, this.parent);
			} else {//Video
				calculateMagnitudesVideo(this.thisgroup, this.parent);
			}
			Group localgroup = this.thisgroup;
			boolean save_data = this.save_data;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					localgroup.setStatus("Done");
					localgroup.setRemainingTime("0 miliseconds");
					if (save_data == true) {
						FileOutputStream fout = null;
						try {
							String home = System.getProperty("user.dir");
							System.out.println(home);
	//						java.nio.file.Path path = java.nio.file.Paths.get(home);
							fout = new FileOutputStream(System.getProperty("user.dir") + File.separator + name + "_group.ser");
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ObjectOutputStream oos = null;
						try {
							oos = new ObjectOutputStream(fout);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							oos.writeObject(localgroup);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					//System.out.println("--\n--\n--\nStatus of group:" + localgroup.getName() + " is done\n--\n--\n--\n");
				}
	        });
		} catch (Exception ex) {
			   System.out.println("Marcelo erro teste 2");
			   ex.getCause().printStackTrace();
			   System.out.println("Marcelo erro teste 3");
			   JOptionPane.showMessageDialog(null, "Could not run flow. Please try again", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
}