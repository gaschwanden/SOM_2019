
public class Item {
	int ID;
	String Name;
	int cluster;
	float distance;
	float[] profile;
	int xCord;
	int yCord;
	
	public Item(String nme, int id, float[] p) {
		ID = id;
		Name = nme;
		profile = p;
		cluster=0;
		xCord = 0;
		yCord = 0;
	}
}
