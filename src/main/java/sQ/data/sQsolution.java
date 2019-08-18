package sQ.data;

public class sQsolution {
	
	   public int[] inventory;
	   
	   public double totalCost[][][];
	   public double CostNoAction[][];
       public int reorderPoint[][]; 


	   
	   public sQsolution(double[][][] totalCost, 
			   			 double[][] CostNoAction, 
			   			 int[] inventory, 
			   			 int[][] reorderPoint) {
		   this.totalCost = totalCost;
		   this.CostNoAction = CostNoAction;
		   this.inventory = inventory;
		   this.reorderPoint = reorderPoint;
	   }

}
