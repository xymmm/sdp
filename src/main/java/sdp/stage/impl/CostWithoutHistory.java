package sdp.stage.impl;

import sdp.dataProcess.Data;
import sdp.dataProcess.Demand;
import sdp.stage.ICostWithoutHistory;
import sdp.stage.OrderingCost;

public class CostWithoutHistory extends OrderingCost implements ICostWithoutHistory {
	
	/**
	 * This method computes the expected cost of the last period - last stage. 
	 * It is accessed by the interface ICostWithoutHistory.
	 * 
	 * The input includes variables in the class Demand - the state space (inventory[]), quantity[], demand[], prob[][],
	 * 					  and the ordering cost described in the abstract class OrderingCost oc[].
	 * 
	 * We create a double variable - imCost - to denote the probabilistic immediate cost for each demand value.
	 * 
	 * We create a 2-dimension array - v[][] - to store the expected cost incurred for each state with each possible replenishment quantity.
	 * 
	 * We create a 1-dimension array - f[] - to store the minimum expected cost for each state among all possible replenishment quantities.
	 */
	
	public double [] calCostWithoutHistory(Demand dm){
		int inventory [] = dm.getInventory();
		int quantity[] = dm.getQuantity();
		int demand[] = dm.getDemand();
		double prob[][] = dm.getProbability();
		double oc[] = calOderingCost(Data.maxQuantity);
		
		double imCost;
		double[][] v = new double[inventory.length][quantity.length]; 
		double[] f = new double[inventory.length];
		
		for(int i=0;i<inventory.length;i++) {
			for(int q=0;q<quantity.length;q++) {
				v[i][q] = oc[q];
				
				for(int d=0;d<demand.length;d++) {
					
					if((inventory[i]+quantity[q]-demand[d]<=Data.maxInventory) && (inventory[i]+quantity[q]-demand[d]>=-Data.stage*Data.maxDemand) ) {
						imCost = prob[d][Data.stage-1]*(Data.holdingCost*Math.max(0, inventory[i]+quantity[q]-demand[d]) + Data.penaltyCost*Math.max(0, demand[d]-inventory[i]-quantity[q]));
					}else {
						imCost = Double.POSITIVE_INFINITY;
					}
					
					v[i][q] = v[i][q] + imCost;
				}
			}
			f[i] = getMinimum(v[i]);
		}
		
		
		
		return f;
	}
}
