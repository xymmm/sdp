package sdp.data;

public class sQsolution {
	
	   public int[] inventory;
	   
	   /** Explain meaning of indexes **/
	   public double totalCost[][][];
	   public double CostNoAction[][];
	   public int optimalAction[][][];

	   
	   public sQsolution(double[][][] totalCost, 
			   			 double[][] CostNoAction, 
			   			 int[] inventory, 
			   			 int[][][] optimalAction) {
		   this.totalCost = totalCost;
		   this.CostNoAction = CostNoAction;
		   this.inventory = inventory;
		   this.optimalAction = optimalAction;
	   }

}
