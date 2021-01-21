package LateralTransshipment;

import java.util.Arrays;

import LateralTransshipment.LT_MILP.LT_MILP_solution;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;

public class LT_simulation {
	
	static MRG32k3a randomStream = new MRG32k3a();
	
	static {
   long seed[] = {1234,1234,1234,1234,1234,1234};
	   randomStream.setSeed(seed);
	}
	
	static int generateDemand(double demandMean1) {
		RandomVariateGenInt genDemand;
		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean1)); 
		int demand = genDemand.nextInt();

		return -demand;
	}
	
	public static double singleSim(LT_MILP milpInstance, LT_MILP_solution milpSolution, boolean print) {
		double etc = 0;
		double[] inventory = milpInstance.initialStock;
		double[] action = new double[3];
		int[] demand = new int[2];
		
		int t=0;
		do {
			if(print == true) System.out.println("At stage "+(t + 1));
			if(print == true) System.out.println("opening inventory level = "+Arrays.toString(inventory));
			action = new double[] {milpSolution.transship[t], milpSolution.order1[t], milpSolution.order2[t]};
			double actionCost = (Math.abs(action[0])>0 ? milpInstance.fixedTransshippingCost + milpInstance.unitTransshippingCost*Math.abs(action[0]) : 0)
								+ (action[1]>0 ? milpInstance.fixedOrderingCost + milpInstance.unitOrderingCost*action[1] : 0)
								+ (action[2]>0 ? milpInstance.fixedOrderingCost + milpInstance.unitOrderingCost*action[2] : 0);
			if(print == true) System.out.println("action cost = "+actionCost);
			inventory = new double[] {inventory[0] - action[0] + action[1], inventory[1] + action[0] + action[2]};
			if(print == true) System.out.println("inventory after action = "+Arrays.toString(inventory));
			
			demand = new int[] {generateDemand(milpInstance.demandMean1[t]), generateDemand(milpInstance.demandMean2[t])};
			if(print == true) System.out.println("demand = "+Arrays.toString(demand));
			inventory = new double[] {inventory[0] + demand[0], inventory[1] + demand[1]};
			if(print == true) System.out.println("inventory after demand = "+Arrays.toString(inventory));
			double closingCost = (inventory[0]>=0 ? inventory[0] * milpInstance.holdingCost : -inventory[0]*milpInstance.penaltyCost)
									+ (inventory[1]>=0 ? inventory[1] * milpInstance.holdingCost : -inventory[1]*milpInstance.penaltyCost);
			if(print == true) System.out.println("closing cost = "+closingCost);
			
			etc = etc + actionCost + closingCost;
			if(print == true) System.out.println("total cost for period "+(t+1)+" = "+etc);
			
			t++;
		}while(t<milpInstance.demandMean1.length);
		
		return etc;
	}
	
	public static void LTsimMultipleRuns(int count, LT_MILP milpInstance, LT_MILP_solution milpSolution, boolean print) {
		for(int i=0; i<count; i++) {
			milpInstance.statCost.add(singleSim(milpInstance, milpSolution, print));
		}
	}
	
}
