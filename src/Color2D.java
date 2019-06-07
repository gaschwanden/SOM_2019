import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Color2D
{
	//  https://github.com/dominikjaeckle/Color2D
	// rewritten in Java by Kerry Nice, 2018
	
	//   Explorative Analysis of 2D Color Maps
//	Steiger, M., Bernard, J., Mittelstädt, S., Hutter, M., Keim, D., Thum, S., Kohlhammer, J.
//	Proceedings of WSCG (23), 151-160, Eurographics Assciation, Vaclav Skala - Union Agency, 2015

	/*
	 * Copyright 2017 Dominik Jäckle
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

	/*
	 * 2D Colormap mapping interface
	 *
	 * Inspired by "Explorative Analysis of 2D Color Maps" (Steiger et al., 2015)
	 *
	 * Usage:
	 * 1. Color2D.setColormap(Color2D.colormaps.BREMM, function() { DO STUFF });
	 * 2. Color2D.getColor(x, y);
	 *
	 * Options BEFORE calling getColor():
	 * - add a new colormap in Color2D.colormaps and set the image dimensions in
	 *   Color2D.dimensions
	 * - set the data range: e.g. Color2D.ranges.x = [20, 450];
	 */

	private static String imgFolder = "/home/kerryn/git/Color2D/data/";


	/*
	 * dimensions of the colormap image
	 */
	private int width = 512;
	private int height = 512;
	
	/*
	 * Available 2D colormaps - reference the png file
	 */
    public static String BREMM = imgFolder + "bremm.png";
    public static String SCHUMANN = imgFolder + "schumann.png";
    public static String STEIGER = imgFolder + "steiger.png";
    public static String TEULING2 = imgFolder + "teulingfig2.png";
    public static String ZIEGLER = imgFolder + "ziegler.png";
	
	/*
	 * Active colormap - if you want to set another one, call Color2D.setColormap(c)
	 */
	String colormap = BREMM; // standard colormap
	
	File colorMapFile = new File(colormap);
	BufferedImage colorMapImage;
	
	/*
	 * data ranges = min and max values of x and y dimensions
	 */

	  double[] xRanges = new double[]{0.0, 1.0};
	  double[] yRanges = new double[]{0.0, 1.0};
	
    
	public static void main(String[] args)
	{
		Color2D color2D = new Color2D();
		Color color = color2D.getColor(0.2, 0.3);
		System.out.println(color.getRed()+ " " + color.getGreen() + " " + color.getBlue());
		color2D.setColormap(SCHUMANN);
		color = color2D.getColor(0.2, 0.3);
		System.out.println(color.getRed()+ " " + color.getGreen() + " " + color.getBlue());
		color2D.setColormap(TEULING2);
		color = color2D.getColor(0.2, 0.3);
		System.out.println(color.getRed()+ " " + color.getGreen() + " " + color.getBlue());
		
	}
	
	public Color getColor(double x, double y)
	{
		int scaledX = getScaledX(x);
		int scaledY = getScaledY(y);
		int pixelColor = colorMapImage.getRGB(scaledX, scaledY);
		Color color = new Color(pixelColor);
		return color;
	}
	
	public Color2D()
	{
		super();
		try
		{
			this.colorMapImage = ImageIO.read(colorMapFile);
		}
		catch (IOException e)
		{			
			e.printStackTrace();
		}	
	}

	/*
	 * computes the scaled X value
	 */
	public int getScaledX(double x)
	{
	    double val = ((x+1.0) - (xRanges[0]+1.0)) / ((xRanges[1]+1.0) - (xRanges[0]+1.0));
	    double newVal = (val * (width-1.0));
	    return (int)Math.round(newVal);		
	}
	
	/*
	 * computes the scaled Y value
	 */
	public int getScaledY(double y)
	{
	    double val = ((y+1.0) - (yRanges[0]+1.0)) / ((yRanges[1]+1.0) - (yRanges[0]+1.0));
	    double newVal = (val * (height-1.0));
	    return (int)Math.round(newVal);		
	}
	
	public String getColormap()
	{
		return colormap;
	}

	public void setColormap(String colormap)
	{
		this.colormap = colormap;
		colorMapFile = new File(colormap);
		
		try
		{
			this.colorMapImage = ImageIO.read(colorMapFile);
		}
		catch (IOException e)
		{			
			e.printStackTrace();
		}	
	}
	
}
