package LT_sdp_HashMap;

public class LTinstance {
	
	public int[] demandMean1;
	public int[] demandMean2;
	public int maxInventory;
	public int minInventory;
	public int maxQuantity;
	public double K;
	public double z;
	public double R;
	public double v;
	public double h;
	public double b;
	public double tail;
	
	public LTinstance(	
			 int[] demandMean1,
			 int[] demandMean2,
			 int maxInventory,
			 int minInventory,
			 int maxQuantity,
			 double K,
			 double z,
			 double R,
			 double v,
			 double h,
			 double b,
			 double tail) {
		 this.demandMean1 = demandMean1;
		 this.demandMean1 = demandMean2;
		 this.maxInventory = maxInventory;
		 this.minInventory = minInventory;
		 this.maxQuantity = maxQuantity;
		 this.K = K;
		 this.z = z;
		 this.R = R;
		 this.v = v;
		 this.h = h;
		 this.b = b;
		 this.tail = tail;
	}
}
