package sdp.stage.impl;

import sdp.dataProcess.Data;
import sdp.dataProcess.Demand;
import sdp.stage.ICostOriginalInventory;
import sdp.stage.OrderingCost;

public class CostOriginalInventory extends OrderingCost implements ICostOriginalInventory {

	/**
	 * This method computes the expected cost of the first period - first stage. 
	 * It is accessed by the interface ICostOriginalInventory
	 * 
	 * The input includes variables in the class Demand - the state space (inventory[]), quantity[], demand[], prob[][],
	 * 					  the ordering cost described in the abstract class OrderingCost oc[],
	 * 				      and the cumulative expected total cost of the previous stages (backwards).
	 * 
	 * We create a double variable - imCost - to denote the probabilistic immediate cost for each demand value.
	 * 
	 * We create a 2-dimension array - v[][] - to store the expected cost incurred for each state with each possible replenishment quantity.
	 * 
	 * We create a 1-dimension array - f[] - to store the minimum expected cost for each state among all possible replenishment quantities.
	 * This array is returned, and regarded as the final result - the total expected cost.
	 */
	public double[] calCostOriginalInventory(Demand dm, double [] preCost) {
		int inventory [] = dm.getInventory();
		int quantity[] = dm.getQuantity();
		int demand[] = dm.getDemand();
		double prob[][] = dm.getProbability();
		//double oc[] = calOderingCost(Data.maxQuantity);
		
		double imCost;
		double[][] v = new double[inventory.length][quantity.length]; 
		double[] f = new double[inventory.length];
		
		double[] arr = new double[quantity.length];
		for(int i=0;i<arr.length;i++) {
			arr[i] = 1;
		}
		for(int i = Data.stage*Data.maxDemand;i<inventory.length;i++) {
			imCost = 0;
			for(int a=0;a<arr.length;a++) {
				arr[a] = Data.unitCost*(inventory[i]-1);
			}
			v[i] = arr;
			for(int d=0;d<demand.length;d++) {
				imCost=prob[d][0]*(Data.holdingCost*Math.max(inventory[i]-demand[d],0)+Data.penaltyCost*Math.max(demand[d]-inventory[i],0))+prob[d][0]*preCost[i-d];
				for(int q=0;q<quantity.length;q++) {
					v[i][q] = v[i][q] + imCost;
				}
			}
			f[i] = getMinimum(v[i]);
		}
		
		
		

		/*
		 * for(int i = 0;i< inventory.length;i++) { for(int q=0;q<quantity.length;q++) {
		 * 
		 * v[i][q] = oc[q]; imCost = 0;
		 * 
		 * for(int d=0;d<demand.length;d++) {
		 * if((inventory[i]-demand[d]<=Data.maxInventory) &&
		 * (inventory[i]-demand[d]>=-Data.stage*Data.maxDemand) ) {
		 * imCost=prob[d][0]*(Data.holdingCost*Math.max(inventory[i]-demand[d],0)+Data.
		 * penaltyCost*Math.max(demand[d]-inventory[i],0))+prob[d][0]*preCost[i-d];//
		 * prob[d][0]*f[i-d][1]; }else { imCost = Double.POSITIVE_INFINITY; }
		 * 
		 * v[i][q] = v[i][q] + imCost; } } f[i] = getMinimum(v[i]); }
		 */
		
		return f;
	}
	
}
