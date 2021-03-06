package sdp.data;

public class Instance {
   /*** Problem instance ***/
   public double fixedOrderingCost;
   public double unitCost;
   public double holdingCost;
   public double penaltyCost;
   public int[] demandMean;
   public int initialInventory = 0;
   
   public double stdParameter;
   
   
   /** SDP boundary conditions **/
   public double tail;
   
   public int maxDemand;
   public int minInventory;
   public int maxInventory;
   public int maxQuantity;
   
   public Instance(
         double fixedOrderingCost,
         double unitCost,
         double holdingCost,
         double penaltyCost,
         int[] demandMean,
         double tail,
         int minInventory,
         int maxInventory,
         int maxQuantity,
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
