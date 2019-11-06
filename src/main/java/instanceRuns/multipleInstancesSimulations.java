package instanceRuns;

import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import umontreal.ssj.util.Chrono;

public class multipleInstancesSimulations {
	
	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;
		/** Poisson Instances irrelevant**/
		double tail = 0.00000001;
		double coe = 0.25;		
		
		/** sQt **/
		double minInventory = -50;
		double maxInventory = 50;
		
		double[][] demandMeanInput = {
				{2,4,6,4},
				{9,7,5,3},
				{2,5,7,8},
				{6,6,6,6},
				{5,7,2,5},
				{4,5,6,7,6},
				{8,6,3,7,9},
				{6,3,9,1,5},
				{2,5,4,8,3},
				{4,4,8,8,3}
				};

		double [][] reorderPointInput = {
		  		{3,5,7,3},
				{10,8,7,2},
				{3,6,8,8},
				{7,7,7,6},
				{6,8,3,5},
				{5,6,7,8,6},
				{9,8,4,8,9},
				{8,4,10,2,5},
				{3,6,6,10,2},
				{5,5,10,10,2}
		};
		
		double[][] actionQuantityInput = {
				{7.9943,0,9,0},
				{9,7.8445,8.0666,0},
				{7.8446,0,6.9942,8.0694},
				{7.0712,9,9,0},
				{5.9508,9,0,4.7264},
				{7.0356,9,0,6.8064,5.9083},
				{8.9599,9,0,6.9878,9},
				{9,0,9,6.7713,0},
				{7.8357,0,6.1914,9,0},
				{9,0,9,9,0}
		};
		double[] Qinput = {8.4971, 8.4223, 7.6361, 8.3571, 7.4754, 9, 8.98, 8.4086, 7.6757, 9};
		
		
		for(int instanceIndex=0; instanceIndex<demandMeanInput.length;instanceIndex++) {
			
			double[] demandMean = demandMeanInput[instanceIndex];
			double[] reorderPoint = reorderPointInput[instanceIndex];
			/** sQt **/
			//double [] actionQuantity = actionQuantityInput[instanceIndex];			
			/** sQ **/
			double[] actionQuantity = new double[reorderPoint.length];
			for(int t=0; t<actionQuantity.length;t++) {
				actionQuantity[t] = Qinput[instanceIndex];
			}

			sQsimInstanceDouble sQsystem1 = new sQsimInstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
					demandMean, tail, minInventory, maxInventory, actionQuantity, reorderPoint, coe);	
			
			Chrono timer = new Chrono();
			
			int count = 50000;
			sQsimPoisson.sQsimPoissonMultiRuns(sQsystem1, count);
			
			sQsystem1.statCost.setConfidenceIntervalStudent();
			System.out.println(sQsystem1.statCost.report(0.9, 3));
			System.out.println("Total CPU time: "+timer.format());
			System.out.println("==================================================================");
		}

	}


}
