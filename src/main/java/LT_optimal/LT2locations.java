package LT_optimal;

public class LT2locations {
	
	   /*** Problem instance ***/
	   public double K;
	   public double z;
	   public double h;
	   public double b;
	   public double R;
	   public double r;
	   public double[] demandMean;
	   public double[] initialInventory;	   
	   
	   /** SDP boundary conditions **/
	   public double tail;
	   
	   public int maxDemand;
	   public int minInventory;
	   public int maxInventory;
	   public int maxQuantity;
	   
	   public LT2locations(
	         double K,
	         double z,
	         double h,
	         double b,
	         double R,
	         double r,
	         double[] demandMean,
	         double tail,
	         int minInventory,
	         int maxInventory,
	         int maxQuantity) {
	      this.K = K;
	      this.z = z;
	      this.h = h;
	      this.b = b;
	      this.R = R;
	      this.r = r;
	      this.initialInventory = initialInventory;
	      this.demandMean = demandMean;
	      this.tail = tail;
	      this.minInventory = minInventory;
	      this.maxInventory = maxInventory;
	      this.maxQuantity = maxQuantity;
	   }
	   
	   public int getStages() {
	      return this.demandMean.length;
	   }

}
