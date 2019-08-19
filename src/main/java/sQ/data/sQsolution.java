package sQ.data;

public class sQsolution {
	
	   public int[] inventory;
	   public double totalCost[][][];
	   public boolean optimalAction[][][];
	   public int[] s;
	   public int opt_a;
	   
	   public sQsolution(double[][][] totalCost, 
			   			 boolean[][][] optimalAction, 
			   			 int[] inventory, 
			   			 int[] s,
			   			 int opt_a) {
		   this.totalCost = totalCost;
		   this.optimalAction = optimalAction;
		   this.inventory = inventory;
		   this.s = s;
		   this.opt_a = opt_a; 
	   }

}
