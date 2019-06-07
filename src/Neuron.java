import processing.core.PApplet;


public class Neuron {
	int ID;
	String name;
	float weights[]; //these are the values
	PApplet p;
	int dgrid;
	int x; int y;
	int posx; int posy;
	float step;  // for drawing the bins
	float displayscale = 3.0f;
	pSOM parent;

	boolean selected = false;

	public int clusterID = -1; //defines to which cluster in kMeans it belongs

	public Neuron(PApplet _p, int res, int _x, int _y, pSOM _parent)
	{
		p=_p;
		dgrid = res;
		parent = _parent;
		weights = new float[16];
		x=_x;
		y=_y;
		posx=_x*res;
		posy=_y*res;
		step = (float) (((float)dgrid) / 16.0); 
		for (int i = 0; i < 16; i++) {
			weights[i] = p.random(100) / 1000.0f; //these are the initial values in random
			//p.println(weights[i]);
		}
	}

	public void drawIconLarge()
	{
		float xx;
		float yy;
		// draw enlarged image
		if ((p.mouseX > posx && p.mouseX < posx+dgrid && p.mouseY > posy && p.mouseY < posy+dgrid)
				|| selected) {
			p.pushMatrix();

			if (clusterID>-1 && clusterID<parent.c.length) {
				p.fill(parent.c[clusterID].getRed(),parent.c[clusterID].getGreen(),parent.c[clusterID].getBlue(), 150 );
			}
			else p.fill(255,150);
			//p.fill(255,150);
			float xtranslate = posx;
			float ytranslate = posy;
			if (posx<dgrid/2*10) xtranslate = dgrid/2*10;
			if (posy<dgrid/2*10) ytranslate = dgrid/2*10;
			if (posx>p.width-dgrid/2*10) xtranslate = p.width-dgrid/2*10;
			if (posy>p.height-dgrid/2*10) ytranslate = p.height-dgrid/2*10;
			p.translate(xtranslate, ytranslate);
			p.rectMode(p.CENTER);
			p.rect(0, 0, dgrid*10.0f, dgrid*10.0f); 

			p.noStroke();
			p.fill(255,0,0);
			p.rectMode(p.CORNER);
			float bw = dgrid*10/20;
			for (int i = 0; i < 16; i++) {
				xx = i*step *10 - dgrid/2*10;
				yy = weights[i] * dgrid*10 * displayscale ;
				p.rect(xx-bw/2.0f, dgrid/2*10, bw, -yy);
			}
			p.popMatrix();
		}
	}

	public void drawIcon()
	{	

		float xx;
		float yy;
		p.pushMatrix();
		p.translate(posx, posy);

		if (clusterID>-1 && clusterID<parent.c.length) {
			p.fill(parent.c[clusterID].getRed(),parent.c[clusterID].getGreen(),parent.c[clusterID].getBlue() );
		}
		else p.fill(255);
		//else p.fill(0);
		p.rectMode(p.CORNER);
		p.noStroke();
		p.rect(0,0,dgrid, dgrid);
		p.fill(255);
		p.stroke(0);
		//p.stroke(255);

		//	if (p.mouseX > posx && p.mouseX < posx+dgrid && p.mouseY > posy && p.mouseY < posy+dgrid)
		//		p.scale(10,10);
		//p.rect(0, 0, dgrid, dgrid);
		for (int i = 0; i < 16; i++) {
			xx = i*step;
			yy = weights[i] * dgrid * displayscale;
			p.line(xx,dgrid,xx,dgrid-yy);
		}
		p.popMatrix();

	}

}
