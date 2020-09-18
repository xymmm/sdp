package lateralTransshipment;

import java.util.Arrays;

import sQ.simulation.sQsimInstanceDouble;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;

public class LTsimulation {
	
	static MRG32k3a randomStream = new MRG32k3a();
	
	static {
   long seed[] = {1234,1234,1234,1234,1234,1234};
	   randomStream.setSeed(seed);
	}
	
	static int generateDemand(int demandMean) {
		RandomVariateGenInt genDemand;
		  
		genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean)); 
		int demand = genDemand.nextInt();

		return -demand;
	}
	
	public static double LTsim(LTsimInstance simInstance, boolean print) {
		if(print == true) System.out.println("-------------------------------");
		int[] inventoryLevel = {1,5};
		double cost = 0;
		
		int t=0;
		do {
			if(print == true) System.out.println("At stage "+(t + 1));
			if(print == true) System.out.println("Current inventory level is "+Arrays.toString(inventoryLevel));
			
			int inventoryLevelIndex = LT2locationsBackwards.getStateIndex(simInstance.inventoryPairs, inventoryLevel);
			int[] action = simInstance.optimalAction[t][inventoryLevelIndex];
			if(print == true) System.out.println("optimal action is "+Arrays.toString(action));
			//action cost
			cost += (Math.abs(action[0]) > 0 ? simInstance.R + simInstance.v*Math.abs(action[0]) : 0 )
					+(action[1] > 0 ? simInstance.K + action[1] * simInstance.v : 0)
					+(action[2] > 0 ? simInstance.K + action[2] * simInstance.v : 0);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			//update inventory
			inventoryLevel[0] = inventoryLevel[0] - action[0] + action[1] <= simInstance.maxInventory ? inventoryLevel[0] - action[0] + action[1] : simInstance.maxInventory;
			inventoryLevel[1] = inventoryLevel[1] + action[0] + action[2] <= simInstance.maxInventory ? inventoryLevel[1] + action[0] + action[2] : simInstance.maxInventory;
			if(print == true) System.out.println("Updated inventory level is "+Arrays.toString(inventoryLevel));
			//demand
			int demand1 = generateDemand(simInstance.demandMean1[t]); int demand2 = generateDemand(simInstance.demandMean2[t]);
			if(print == true) System.out.println("Demand in this stage is ["+(-demand1)+","+(-demand2)+"]");
			//update inventory
			inventoryLevel[0] = inventoryLevel[0] + demand1 >= simInstance.minInventory ? inventoryLevel[0] + demand1 : simInstance.minInventory;
			inventoryLevel[1] = inventoryLevel[1] + demand2 >= simInstance.minInventory ? inventoryLevel[1] + demand2 : simInstance.minInventory;
			if(print == true) System.out.println("Updated inventory level is "+Arrays.toString(inventoryLevel));
			//closing cost
			cost += ((inventoryLevel[0] >= 0) ? simInstance.h*inventoryLevel[0] : (-simInstance.b)*inventoryLevel[0])
					+ ((inventoryLevel[1] >=0 ) ? simInstance.h*inventoryLevel[1] : (-simInstance.b)*inventoryLevel[1]);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			
			//if(t==simInstance.demandMean1.length-1) inventoryLevel = simInstance.getInitialState();
			t++;
		}while(t<simInstance.demandMean1.length);
		return cost;
	}
	
	public static void LTsimMultipleRuns(LTsimInstance instance, int count, boolean print) {
		for(int i=0; i<count; i++) {
			instance.statCost.add(LTsim(instance, print));
		}
	}

}
