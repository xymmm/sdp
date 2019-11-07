package instanceRuns;

import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import umontreal.ssj.util.Chrono;

public class multipleInstancesSimulations {
	
	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		/** Poisson Instances irrelevant**/
		double tail = 0.00000001;
		double coe = 0.25;		
		
		/** sQt **/
		double minInventory = -1000;
		double maxInventory = 1000;
		/*
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
				*/
		
		/*
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
*/
		/*
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
		};*/
		//double[] Qinput = {8.4971, 8.4223, 7.6361, 8.3571, 7.4754, 9, 8.98, 8.4086, 7.6757, 9};

		
		double[][] demandMeanInput = {
				{11,38,115,175,115,38},
				{122,77,124,95,107,73},
				{100,62,31,43,199,172},
				{100,100,100,100,100,100,100,100},
				{211,119,165,47,100,62,31,82},
				{97,33,41,76,122,29,37,66},
				{54,23,35,49,66,26,37,41,71,19},
				{65,73,59,42,36,31,28,23,18,11},
				{11,18,23,28,31,36,42,59,73,65},
				{9,17,22,35,49,52,68,53,50,36,23,18},
				{76,58,22,54,69,21,13,89,75,66,45,29},
				{60,60,60,60,60,60,60,60,60,60,60,60}
		};

		double[][] reorderPointInput = {
				{8, 28, 106, 167, 115, 30},
				{115,68,114,80,97,60},
				{91,56,23,29,207,160},
				{90,90,90,90,90,90,90,89},
				{199,108,165,36,86,60,24,70},
				{113,26,46,58,116,23,29,71},
				{50,16,30,40,62,20,34,31,70,10},
				{68,72,57,38,33,25,25,23,20,0},
				{10,17,20,25,26,37,40,52,70,55},
				{5,11,18,25,43,42,61,46,43,32,19,9},
				{70,53,17,45,66,20,4,82,66,60,45,21},
				{53,47,54,48,54,48,54,48,54,48,54,47}
		};
		double[] Qinput = {114.85, 177, 220.46, 101.39, 185.22, 146.91, 83.675, 93.313, 64.91, 104.58, 84.63, 109.28};
		
		for(int ins=0; ins < demandMeanInput.length;ins++) {
			
			double[] demandMean = demandMeanInput[ins];
			double[] reorderPoint = reorderPointInput[ins];
			/** sQt **/
			//double [] actionQuantity = actionQuantityInput[instanceIndex];			
			/** sQ **/
			double Q = Qinput[ins];
			double[] actionQuantity = new double[demandMean.length];
			for(int t=0; t<actionQuantity.length;t++) {
				actionQuantity[t] = Q;
			}

			sQsimInstanceDouble sQsystem1 = new sQsimInstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
					demandMean, tail, minInventory, maxInventory, actionQuantity, reorderPoint, coe);	
			
			Chrono timer = new Chrono();
			
			sQsimPoisson.sQsimPoissonMultiRuns(sQsystem1, 50000);
			
			sQsystem1.statCost.setConfidenceIntervalStudent();
			System.out.println(sQsystem1.statCost.report(0.9, 3));
			System.out.println("Total CPU time: "+timer.format());
			System.out.println("==================================================================");
		}

	}


}
