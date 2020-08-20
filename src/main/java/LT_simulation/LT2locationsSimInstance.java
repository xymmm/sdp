package LT_simulation;

public class LT2locationsSimInstance {
	
	   /*** Problem instance ***/
	   public double K;
	   public double z;
	   public double h;
	   public double b;
	   public double R;
	   public double r;
	   public int[] demandMean;
	   public int[] initialInventory;	  
	   
	   public int[] transshipment;
	   
	   public int[][] S;
	   public int[][] s;
	   
	   
	   public LT2locationsSimInstance(
	         double K,
	         double z,
	         double h,
	         double b,
	         double R,
	         double r,
	         int[] demandMean,
	         int[] initialInventory,
	         int[] transshipment,
	         int[][] S,
	         int[][] s) {
	      this.K = K;
	      this.z = z;
	      this.h = h;
	      this.b = b;
	      this.R = R;
	      this.r = r;
	      this.initialInventory = initialInventory;
	      this.demandMean = demandMean;
	      this.transshipment = transshipment;
	      this.S = S;
	      this.s = s;
	   }
	   
		public int[] getInitialInventory() {
			return this.initialInventory;
		}

}
