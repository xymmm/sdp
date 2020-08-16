package LT_optimal;

import java.util.Arrays;

import sS.sS;

public class LT_optimal_sdp {
	
	/** compute demand probability **/
	//sS.computeDemandProbability(double[] demandMean, int maxDemand, double tail)
	
	/** compute costs **/
	public static double computeLTcost(double Trans, double R, double r) {
		return (Math.abs(Trans) == 0)? 0: (Math.abs(Trans)*r + R);
	}	
	public static double computeClosingCost(int inventory, double h, double b) {
		return (inventory >=0)? (h*inventory) : (-b*inventory);
	}
	public static double computeOrderingCost(double Q, double K, double z) {
		return (Q>0)? (K + z*Q) : 0;
	}
	
	/** compute expected total cost and solve optimal actions **/
	public static LTsolution solveLT2locations(LT2locations instance) {
		
		int stages = instance.getStages();
		
		/* indexing inventory */
		int[][] inventory = new int[instance.maxInventory - instance.minInventory + 1][2];
		for(int i=0; i<inventory.length; i++) {
			for(int j=0; j<inventory[0].length; j++) {
				inventory[i][j] = i+instance.minInventory;
			}
		}
		
		/* demand */
		double demandProbabilities [][] = null;
		demandProbabilities = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		
		/* build action matrices */
		double[][][] LTaction = new double[stages][2][inventory.length];
		double[][][] REaction = new double[stages][2][inventory.length];
		/* build cost matrices */
		//dynamically create dimensions of Trans*Q for cost
		double[][][] optimalCost = new double[stages][2][inventory.length];
		
		
		
		
		return new LTsolution(inventory, LTaction, REaction, optimalCost);
	}
	
	
	public static void main(String[] args) {
		   double K = 100;
		   double z = 0;
		   double h = 1;
		   double b = 10;
		   double R = 200;
		   double r = 5;
		   double[] demandMean = {20, 40, 60, 40};
		   double[] initialInventory = {100, 20};	   
		   
		   double tail = 0.00000001;
		   
		   int maxDemand = 500;
		   int minInventory = -500;
		   int maxInventory = 500;
		   int maxQuantity = 200;
		   
		   double[] Q = {10, 10};
		   
		   //double[] cost = computeLTcost(initialInventory, 50.0, demandMean[0], Q, h, b, K, z, R, r);
		   //System.out.println(Arrays.toString(cost));
	}
	
}
