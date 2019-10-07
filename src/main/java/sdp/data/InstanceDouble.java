package sdp.data;

public class InstanceDouble {

	
	   /*** Problem instance ***/
	   public double fixedOrderingCost;
	   public double unitCost;
	   public double holdingCost;
	   public double penaltyCost;
	   public double[] demandMean;
	   public double initialInventory = 0;
	   
	   public double stdParameter;
	   
	   
	   /** SDP boundary conditions **/
	   public double tail;
	   
	   public double maxDemand;
	   public double minInventory;
	   public double maxInventory;
	   public double maxQuantity;
	   
	   public InstanceDouble(
	         double fixedOrderingCost,
	         double unitCost,
	         double holdingCost,
	         double penaltyCost,
	         double[] demandMean,
	         double tail,
	         double minInventory,
	         double maxInventory,
	         double maxQuantity,
	         double stdParameter) {
	      this.fixedOrderingCost = fixedOrderingCost;
	      this.unitCost = unitCost;
	      this.holdingCost = holdingCost;
	      this.penaltyCost = penaltyCost;
	      this.demandMean = demandMean;
	      this.tail = tail;
	      this.minInventory = minInventory;
	      this.maxInventory = maxInventory;
	      this.maxQuantity = maxQuantity;
	      this.stdParameter = stdParameter;
	   }
	   
	   public int getStages() {
	      return this.demandMean.length;
	   }
	
}
