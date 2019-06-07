import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PVector;

/*
 * press r to run
 * press k to initialise clustering and l to iterate the clustering algorithm
 * press c to classify the stops
 * press e to export the som to file and the classified stops to file as well
 */


public class SOM extends PApplet {

	int w=20;
	int h=20;
	public int dgrid = 15;
	
	pSOM som;
	
	//String classifiedStopsfile ="C:\\Users\\Eva\\Desktop\\Singapore\\_Output\\Easylink\\train_bus_bins_classified.txt";
	//String trainedSOMfile ="C:\\Users\\Eva\\Desktop\\Singapore\\_Output\\Easylink\\train_bus_bins_SOM.txt";
	//String testVecFile = "C:\\Users\\Eva\\Desktop\\Singapore\\_Output\\Easylink\\train_bus_bins_sub.txt";
	
//	String classifiedStopsfile ="/Users/GA/Desktop/Akademia/PhD/_Eva-Maria_Friedrich/train_bus_bins_classified.txt";
//	String trainedSOMfile ="/Users/GA/Desktop/Akademia/PhD/_Eva-Maria_Friedrich/train_bus_bins_SOM.txt";
//	String testVecFile = "/Users/GA/Desktop/Akademia/PhD/_Eva-Maria_Friedrich/train_bus_bins_sub.txt";
	
	String classifiedStopsfile ="/Users/GA/Documents/workspace/UniMelb_SOM/Data/train_bus_bins_classified.txt";
	String trainedSOMfile ="/Users/GA/Documents/workspace/UniMelb_SOM/Data/train_bus_bins_SOM.txt";
	String testVecFile = "/Users/GA/Documents/workspace/UniMelb_SOM/Data/train_bus_bins_sub.txt";
	
	//
	int iter;
	int maxIters = 500;
	boolean bGo = false;
	boolean startSOM = false;
	//
	
	int minNumTrips = 2000; // so many trips at minimum to qualify as a trainingsvector
	
	Vector<float[]> trainingvecs;
	Vector<Item> itemsToClassify;
	
	int testi;  
	
	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "SOM" });
		//setBackground(Color.red);
	}
	
	public void setup() {
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice devices[] = environment.getScreenDevices();
        h= devices[0].getDisplayMode().getHeight();
        w=h;
		size(w,h);
		
		som = new pSOM(this, maxIters, dgrid);
		//importLastAnalysis(trainedSOMfile);
		boolean loaded = false;
		try {
			loaded = initTestVecs(testVecFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			println("Input file not found - using default training vectors.");
		}
		if (!loaded) initTestVecs();
	}
	
	public void onStartSOM()
	{
		bGo = true;		
		som.init(); // starting the neurons in random manner
		iter = 0;
	}
	
	public void draw() {
		background(255);
		som.draw();
		
		int t = (int) (random(trainingvecs.size()));
		if (keyPressed && key == 'q') {
			if (testi==-1) testi = t;
			showTrainingVector(testi);
		}
		if (iter < maxIters && bGo){
			som.train(iter, trainingvecs.elementAt(t));
		    iter++;
		}
		if (startSOM) {
			onStartSOM();
			startSOM = false;
		}
	}
	
	public void importLastAnalysis(String filename)
	{
		File file=new File(filename);
		BufferedReader br=null;
		
		try {
			br=new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
	    String text=null;
	    
	    int linecount = 0;
	
	    // loading training neurons
	    try {
	    	int line = 0;
	    	int i = -1; int j = 0;
			while((text=br.readLine())!=null){
				if (line % som.neurons[0].length == 0) 
				{
						j = 0;
						i++;
				}
				//println(i+" "+j);
				if ( i < som.neurons.length && j<som.neurons[0].length) {
					String [] subtext = splitTokens(text,",");
					
					if (subtext.length == som.inputDimension + 1) { // dimensions plus cluster ID
						for (int k = 0; k < som.inputDimension; k++) {
							float f = new Float(subtext[k]);
							som.neurons[i][j].weights[k] = f;
						}
						int c = Integer.parseInt(subtext[som.inputDimension]);
						som.neurons[i][j].clusterID = c;
					}
				}
				j++;
				line++;
				
			}
	    }
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void export (String filename)
	{
		println("Exporting SOM.");
		som.export(filename);
	}
	
	public void showTrainingVector(int t)
	{
		rectMode(CORNER);
		fill(255,100);
		//rect(0,0,som.dgrid*10, som.dgrid*10);

		float xx;
		float yy;
		// draw enlarged image
		pushMatrix();
			float xtranslate = som.dgrid/2*10;
			float ytranslate = som.dgrid/2*10;
			float step = (float) (((float)som.dgrid) / 16.0); 

			translate(xtranslate, ytranslate);
			rectMode(CENTER);
			rect(0, 0, som.dgrid*10.0f, som.dgrid*10.0f); 
			stroke(0,255,0);
			strokeWeight(4);
			for (int i = 0; i < 16; i++) {
						xx = i*step *10 - som.dgrid/2*10;
						yy = trainingvecs.elementAt(t)[i] * som.dgrid*10 * 2 ;
						line(xx,som.dgrid/2*10,xx,som.dgrid/2*10-yy );
			}
			strokeWeight(1);
		popMatrix();
	}
	
	
	public void mousePressed()
	{
		int x = mouseX / som.dgrid;
		int y = mouseY / som.dgrid;
		som.neurons[x][y].selected = !som.neurons[x][y].selected;
	}
	public void keyPressed()
	{
		if (key=='r') {
			startSOM = true;
		}
		else if (key=='e') {
			export(trainedSOMfile);
			exportClassified(classifiedStopsfile);
		}
		else if (key=='c') {
			classify();
		}
		else if (key=='k') {  // init clustering
			println("Initialising k-means.");
			som.kmeans();
		}
		else if (key=='l') {  // iterate clustering
			println("k-means iterate.");
			som.kmeansIterate();
		}
		if (key=='b') {
			
		}
	}
	
	private void classify() {
		println("Classify.");
		for (int i = 0; i < itemsToClassify.size(); i++) {
			Item it = itemsToClassify.elementAt( i);
			it.profile[0] = 0;
			PVector v = som.bestMatch(it.profile);
			int x = (int) v.x;
			int y = (int) v.y;
			it.cluster = som.neurons[x][y].clusterID;
			it.xCord = x;
			it.yCord = y;
			System.out.println(it.cluster);
			it.distance = som.distanceToKCentres[it.cluster][x][y];
			System.out.println(i+" has ID = "+ it.ID+" and cluster "+ it.distance);
		}
	}

	private void exportClassified(String filename) {
		println("Exporting classified items.");
		classify();
		BufferedWriter writer = null;
        try
        {
                writer = new BufferedWriter( new FileWriter( filename));
                writer.write("ID,Name,cluster");
                writer.newLine();
                
                for (int i = 0; i < itemsToClassify.size(); i++) 
        		{
        			Item it = itemsToClassify.elementAt(i);
        			StringBuffer s = new StringBuffer("");;
    				s.append( it.ID);
    				s.append( it.cluster);
    				s.append( it.distance);
    				s.append( ","+it.xCord+ "," + it.yCord);
        			writer.write(s.toString());
   		     		writer.newLine();
        		}

        }
        catch ( IOException e)
        {
        }
        finally
        {
                try
                {
                        if ( writer != null)
                                writer.close( );
                }
                catch ( IOException e)
                {
                }
        }
	}

	public void keyReleased()
	{
		testi = -1;
	}
	
	
	boolean initTestVecs(String filename) throws FileNotFoundException
	{
		File file=new File(filename);
		BufferedReader br=null;
		
		br=new BufferedReader(new FileReader(file));
	    String text=null;
	    
	    int linecount = 0;
	    trainingvecs = new Vector<float[]>();
	    itemsToClassify = new Vector<Item>();
	    try {
			while((text=br.readLine())!=null){
				
				if (linecount>0) {
					String [] subtext = splitTokens(text,",");
					if (subtext.length == 37) {
						
						int c = Integer.parseInt( subtext[20] ); 
						
						float v[] = new float[16];
						v[0] = new Float(subtext[21]);
						v[1] = new Float(subtext[22]);
						v[2] = new Float(subtext[23]);
						v[3] = new Float(subtext[24]);
						v[4] = new Float(subtext[25]);
						v[5] = new Float(subtext[26]);
						v[6] = new Float(subtext[27]);
						v[7] = new Float(subtext[28]);
						v[8] = new Float(subtext[29]);
						v[9] = new Float(subtext[30]);
						v[10] = new Float(subtext[31]);
						v[11] = new Float(subtext[32]);
						v[12] = new Float(subtext[33]);
						v[13] = new Float(subtext[34]);
						v[14] = new Float(subtext[35]);
						v[15] = new Float(subtext[36]);
						
						Item it = new Item(subtext[2], Integer.parseInt( subtext[0] ), v);
						itemsToClassify.addElement(it);
						//println(subtext[2]);
						if (c>minNumTrips) {
							trainingvecs.addElement(v);
							//println(subtext[2]);
							//if (subtext[2].equals("\"STN Clementi\"")) {
								//trainingvecs.addElement(v);
								//println(subtext[2]);
								//println(c);
							//}
							//
						}
					}
				}

			    linecount++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	    //println(trainingvecs.size());
	    return true;
	}
	void initTestVecs()
	{
		trainingvecs = new Vector<float[]>();
		
		float v[] = new float[16];
		v[0] = 0.6f;
		v[1] = 0.58f;
		v[2] = 0.56f;
		v[3] = 0.52f;
		v[4] = 0.48f;
		v[5] = 0.44f;
		v[6] = 0.38f;
		v[7] = 0.32f;
		v[8] = 0.26f;
		v[9] = 0.20f;
		v[10] = 0.12f;
		v[11] = 0.2f;
		v[12] = 0.0f;
		v[13] = 0.0f;
		v[14] = 0.0f;
		v[15] = 0.0f;
		trainingvecs.addElement(v);
		
		v = new float[16];
		v[0] = 0.8f;
		v[1] = 0.7f;
		v[2] = 0.6f;
		v[3] = 0.5f;
		v[4] = 0.4f;
		v[5] = 0.3f;
		v[6] = 0.2f;
		v[7] = 0.1f;
		v[8] = 0.0f;
		v[9] = 0.0f;
		v[10] = 0.0f;
		v[11] = 0.0f;
		v[12] = 0.0f;
		v[13] = 0.0f;
		v[14] = 0.0f;
		v[15] = 0.0f;
		trainingvecs.addElement(v);
		
		v = new float[16];
		v[0] = 0.8f;
		v[1] = 0.5f;
		v[2] = 0.3f;
		v[3] = 0.2f;
		v[4] = 0.1f;
		v[5] = 0.05f;
		v[6] = 0.02f;
		v[7] = 0.01f;
		v[8] = 0.01f;
		v[9] = 0.01f;
		v[10] = 0.0f;
		v[11] = 0.0f;
		v[12] = 0.0f;
		v[13] = 0.0f;
		v[14] = 0.0f;
		v[15] = 0.0f;
		trainingvecs.addElement(v);
		
		v = new float[16];
		v[0] = 0.0f;
		v[1] = 0.01f;
		v[2] = 0.02f;
		v[3] = 0.04f;
		v[4] = 0.16f;
		v[5] = 0.32f;
		v[6] = 0.64f;
		v[7] = 0.7f;
		v[8] = 0.7f;
		v[9] = 0.64f;
		v[10] = 0.32f;
		v[11] = 0.16f;
		v[12] = 0.04f;
		v[13] = 0.02f;
		v[14] = 0.01f;
		v[15] = 0.0f;
		trainingvecs.addElement(v);
		
		v = new float[16];
		v[0] = 0.04f;
		v[1] = 0.16f;
		v[2] = 0.32f;
		v[3] = 0.70f;
		v[4] = 0.64f;
		v[5] = 0.32f;
		v[6] = 0.3f;
		v[7] = 0.32f;
		v[8] = 0.5f;
		v[9] = 0.5f;
		v[10] = 0.16f;
		v[11] = 0.04f;
		v[12] = 0.02f;
		v[13] = 0.01f;
		v[14] = 0.0f;
		v[15] = 0.0f;
		trainingvecs.addElement(v);
		
		
	}

}
