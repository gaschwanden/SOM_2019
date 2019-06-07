import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import processing.core.PApplet;
import processing.core.PVector;


public class pSOM {
	PApplet p;
	int dgrid = 20;
	int w; int h;
	Neuron neurons[][];
	
	//
	float 	learnDecay;
	float 	radiusDecay;
	int 	maxIters;
	float 	timeConstant;
	float 	radius;
	float 	learnRate = 0.05f;
	int 	inputDimension = 16;
	
	boolean displayKMeansCluster = false;
	boolean kmeansinited = false;
	int 	clusters = 7; // number of klusters for the kMeans
	int		clusterCentres[][];  // this points to a neuron!!
	float	distanceToKCentres[][][];
	
	Color 	c[];
	
	public pSOM(PApplet _p, int _maxIters, int _dgrid)
	{
		p=_p;
		w = p.width;
		h = p.height;
		dgrid = _dgrid;
		radius = (h/dgrid + w/dgrid) / 2;
		maxIters = _maxIters;
		neurons = new Neuron[w/dgrid][h/dgrid];
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				neurons[i][j] = new Neuron(p, dgrid, i,j, this);
			}
		}
		c = new Color[13];
		//c[0] = new Color(255,243,78);
		//c[1] = new Color(207,157,40);
		//c[2] = new Color(115,61,2);
		c[0] = new Color(235,151,151);
		c[1] = new Color(234,81,86);
		c[2] = new Color(190,22,34);
		
		//c[3] = new Color(136,227,0);
		//c[4] = new Color(62,162,6);
		//c[5] = new Color(6,90,15);
		c[3] = new Color(115,186,224);
		c[4] = new Color(29,113,184);
		c[5] = new Color(0,59,159);
		
		c[6] = new Color(100,100,100);
		c[7] = new Color(130,130,130);
		c[8] = new Color(180,180,180);
		c[9] = new Color(220,220,220);
		
		c[10] = new Color(223,114,89);
		c[11] = new Color(189,50,32);
		c[12] = new Color(131,9,1);
		
		clusterCentres = new int[clusters][2];
		distanceToKCentres = new float[clusters][w/dgrid][h/dgrid];
		for (int k = 0; k < clusters; k++) {
			for (int i = 0; i < w/dgrid; i++) {
				for (int j = 0; j < h/dgrid; j++) {
					distanceToKCentres[k][i][j] = -1.0f;
				}
			}
		}
	}
	
	public void init()
	{
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				neurons[i][j] = new Neuron(p, dgrid, i,j, this);
			}
		}
		initTraining(maxIters);
		displayKMeansCluster = false;
		kmeansinited = false;
	}
	
	public void draw()
	{
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				neurons[i][j].drawIcon();
			}
		}
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				neurons[i][j].drawIconLarge();
			}
		}
		// draw cluster centres
		p.rectMode(p.CENTER);
		for (int k = 0; k < clusters; k++) {
			//p.stroke(c[k].getRed(), c[k].getGreen(), c[k].getBlue());
			p.stroke(255,0,0);
			p.strokeWeight(2);
			p.fill(255,150);
			p.ellipse(clusterCentres[k][0] * dgrid, clusterCentres[k][1] * dgrid, 15,15);
			p.strokeWeight(1);
		}
	}
	
	void kmeans()
	{
		displayKMeansCluster = true;
		// init
		//if (!kmeansinited) {
			//kmeansinited = true;
			for (int i = 0; i < w/dgrid; i++) {
				for (int j = 0; j < h/dgrid; j++) {
					neurons[i][j].clusterID = -1;
				}
			}
			//random pick
			for (int k = 0; k < clusters; k++) {
				int i = (int) p.random(neurons.length);
				int j = (int) p.random(neurons[0].length);
				neurons[i][j].clusterID = k;
				clusterCentres[k][0] = i;
				clusterCentres[k][1] = j;
				
				clusterDistanceMatrix(k);
				
			}
			setClusterMembership();
		//}
	}
	
	void kmeansIterate()
	{
		for (int k = 0; k < clusters; k++) {
			float dist = moveClusterCentre(k);  // move the cluster centre
			clusterDistanceMatrix(k);			// update the distance matrix
		}
		setClusterMembership();  // renew cluster membership
	}
	
	float moveClusterCentre(int k) {
		
		float x = 0; float y = 0;
		float count = 0;
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				if (neurons[i][j].clusterID != k) continue;
				x+=neurons[i][j].posx;
				y+=neurons[i][j].posy;
				count++;
			}
		}
		x/=count;y/=count;
		//float dx = x - clusterCentres[k][0];
		//float dy = y - clusterCentres[k][1];
		float dx = x - neurons[clusterCentres[k][0]][clusterCentres[k][1]].posx;
		float dy = y - neurons[clusterCentres[k][0]][clusterCentres[k][1]].posy;
		//clusterCentres[k][0] = x;
		//clusterCentres[k][1] = y;
		clusterCentres[k][0] = (int) (x / dgrid);
		clusterCentres[k][1] = (int) (y / dgrid);
		return p.sqrt(dx*dx + dy*dy);
	}
	
	void setClusterMembership()
	{
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				float min = -1;
				int cl = 0;
				
				for (int k = 0; k < clusters; k++) {
					if (distanceToKCentres[k][i][j] < min || min < 0) {
						min = distanceToKCentres[k][i][j];
						cl = k;
					}
				}
				neurons[i][j].clusterID = cl;
			}
		}
	}
	
	void clusterDistanceMatrix(int k) {
		float x; float y;
		for (int i = 0; i < w/dgrid; i++) {
			for (int j = 0; j < h/dgrid; j++) {
				//x = neurons[i][j].posx - clusterCentres[k][0];
				//y = neurons[i][j].posy - clusterCentres[k][1];
				//x = neurons[i][j].posx - neurons[clusterCentres[k][0]][clusterCentres[k][1]].posx;
				//y = neurons[i][j].posy - neurons[clusterCentres[k][0]][clusterCentres[k][1]].posy;
				//float d = x*x + y*y;
				float d = weight_distance(neurons[i][j].weights, neurons[clusterCentres[k][0]][clusterCentres[k][1]].weights);
				distanceToKCentres[k][i][j] = d;
			}
		}
	}
	
	
	void initTraining(int iterations)
	{
		timeConstant = iterations/p.log(radius);   
	}
	
	public void train(int i, float weight[])
	{   
		//3 Training the initialized SOM 
	   radiusDecay = radius*p.exp(-1*i/timeConstant); 		// this is the radius of impact
	   learnDecay = learnRate*p.exp(-1*i/timeConstant); 	// this defines the decay over time
//	   weightDecay = weightFunctions[x][]; 					// defining the impact of each wight with regards of its function
	   
	   //get best matching unit
	   PVector coord = bestMatch(weight); // 				//
	   int x = (int) coord.x;
	   int y = (int) coord.y;
		
	   //p.println(w);
	   //p.println(x+" "+y);

	   //if (bDebug) println("bestMatch: " + x + ", " + y + " ndx: " + ndxComposite);
	   
	 
	   //scale best match and neighbors...
	   for(int a = 0; a < w/dgrid; a++) {
	     for(int b = 0; b < h/dgrid; b++) {
	       
	        //float d = distance(nodes[x][y], nodes[a][b]);
	        float d = p.dist(neurons[x][y].x, neurons[x][y].y, neurons[a][b].x, neurons[a][b].y);
	        float influence = p.exp((-1*p.sq(d)) / (2*radiusDecay*i));
	        //p.println(d);
	        //p.println("Best Node: ("+x+", "+y+") Current Node ("+a+", "+b+") distance: "+d+" radiusDecay: "+radiusDecay);
	        
	        if (d < radiusDecay)  {        
	          for(int k = 0; k < inputDimension; k++)
	        	  neurons[a][b].weights[k] += influence*learnDecay*(weight[k] - neurons[a][b].weights[k]);
	        }
	     } //for j
	   } // for i
	  
	 }
	
	PVector bestMatch(float weights[]) {
		// 4 Finding neuron with the closest vector
		float minDist = p.sqrt(999999);
//		int minIndex = 0;
		PVector coord = new PVector();
		float tmp = 99999999;

		// going through the array of neurons
		for (int i = 0; i < w / dgrid; i++) {
			for (int j = 0; j < h / dgrid; j++) {
				
				tmp = weight_distance(neurons[i][j].weights, weights);
				
				if (tmp < minDist) {
					
					minDist = tmp;
					coord.x = i;
					coord.y = j;
				} // if
			} // for j
		} // for i
		// note this index is x << 16 + y.
//		System.out.println("tmp = "+tmp);
//		System.out.println("perfect coordinates "+coord.x+"/"+coord.y);
		return coord;
	}
	
	float weight_distance(float x[], float y[])
	 {
	    if (x.length != y.length) {
	      p.println ("Error in SOM::distance(): array lens don't match");
	      p.exit();
	    }
	    float tmp = 0.0f;
	    for(int i = 0; i < x.length; i++)
	       tmp += p.sq( (x[i] - y[i]));
	    tmp = p.sqrt(tmp);
	    return tmp;
	 }
	
	public void export(String filename)
	{
		BufferedWriter writer = null;
        try
        {
                writer = new BufferedWriter( new FileWriter( filename));
 
                for (int i = 0; i < w/dgrid; i++) {
       		     	for (int j = 0; j < h/dgrid; j++) {
       		     		Neuron n = neurons[i][j];
       		     		String s = new Float(n.weights[0]).toString();
       		     		for (int k = 1; k < n.weights.length; k++) {
       		     			s = s+","+new Float(n.weights[k]).toString();
       		     		}
       		     		s = s+","+n.clusterID;
       		     		writer.write(s);
       		     		writer.newLine();
       		     	}
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
}
