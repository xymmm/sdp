package LateralTransshipment;

import java.util.Arrays;

public class LTsingleAction {

	public static void testSingleAction(Instance instance, Solution solution, int[] testInventory, int[] testAction) {

		int transshipment = testAction[0];
		int testQA = testAction[1];
		int testQB = testAction[2];
		int iA = instance.index(testInventory[0]);
		int iB = instance.index(testInventory[1]);
		
		double demandProbabilities [][][] = LTbackwards_2stages.computeDemandProbability(instance);
/*
		double optimalCost[][] = new double [instance.getStages()][instance.stateSpaceSize()];
		try {
			optimalCost = sdp.util.read2DarrayTxt.getFile("src/main/java/lateralTransshipment/Cn.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
*/
		double optimalCost[][] = solution.CnTransshipment[1];
		double transshippingCost = (Math.abs(transshipment)>0) ? (Math.abs(transshipment)*instance.u + instance.R) : 0;
		double orderingCost[] = new double [2];
		orderingCost[0] = (testQA >0) ? (instance.KA + testQA*instance.vA) : 0;
		orderingCost[1] = (testQB >0) ? (instance.KB + testQB*instance.vB) : 0;
		
		double mass = 0;  
		double imHolding[] = new double[2]; double imPenalty[] = new double[2];
		double futureCost = 0; double immediateCost = 0; double totalFutureCost = 0;
		
		double totalCost = 0;
		double[] totalHolding = new double[2];
		double[] totalPenalty = new double[2];
		for(int dA = 0; dA < demandProbabilities[0][0].length; dA++) {       // Demand A
			for(int dB = 0; dB < demandProbabilities[1][0].length; dB++) {    // Demand B
				if((testInventory[0] - transshipment + testQA - dA <= instance.maxInventory) && (testInventory[0] - transshipment + testQA - dA >= instance.minInventory) &&
				   (testInventory[1] + transshipment + testQB - dB <= instance.maxInventory) && (testInventory[1] + transshipment + testQB - dB >= instance.minInventory)) {
					mass += demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB];
					immediateCost = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*
							LTbackwards_2stages.computeImmediateEndOfPeriodCost(instance.inventory(iA)-transshipment,instance.inventory(iB)+transshipment, testQA, testQB, dA, dB, instance.hA, instance.hB, instance.pA, instance.pB);
					int[] closeInventory = {testInventory[0] - transshipment + testQA - dA, testInventory[1] + transshipment + testQB - dB};
					futureCost = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*optimalCost[instance.index(closeInventory[0])][instance.index(closeInventory[1])];

					imHolding[0] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*Math.max(0, instance.hA * closeInventory[0]);
					imHolding[1] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*Math.max(0, instance.hB * closeInventory[1]);
					imPenalty[0] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*Math.max(0, -instance.pA * closeInventory[0]);
					imPenalty[1] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*Math.max(0, -instance.pB * closeInventory[1]);
//					immediateCost = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*(
//							imHolding[0]+imHolding[1]+imPenalty[0]+imPenalty[1]);
					}//if
				totalCost += (futureCost + immediateCost);
				totalFutureCost += futureCost;
				totalHolding[0] += imHolding[0]; totalHolding[1] += imHolding[1];
				totalPenalty[0] += imPenalty[0]; totalPenalty[1] += imPenalty[1];
			}//dB
		}//dA
		totalCost += (transshippingCost + orderingCost[0] + orderingCost[1]);
		totalCost /= mass;
		totalHolding[0] /= mass; totalHolding[1] /= mass; totalPenalty[0]/=mass; totalPenalty[1]/=mass; totalFutureCost /= mass;
		
		//print
		System.out.println();
		System.out.println("testInventory = "+Arrays.toString(testInventory));
		System.out.println("testAction = "+Arrays.toString(testAction));
		
		System.out.println("---------------");
		
		System.out.println("transshipping cost = "+transshippingCost);
		System.out.println("ordering cost = " + (orderingCost[0]+orderingCost[1])+" = "+Arrays.toString(orderingCost));

		System.out.println("---------------");

		System.out.println("exp imCost = "+(totalHolding[0] + totalHolding[1] + totalPenalty[0] + totalPenalty[1]));
		System.out.println("exp futureCost = "+totalFutureCost);
		System.out.println("holding cost = " 
				+ (totalHolding[0] + totalHolding[1])+" = "
				+ Arrays.toString(totalHolding));
		System.out.println("penalty cost = " 
				+ (totalPenalty[0] + totalPenalty[1])+" = "
				+ Arrays.toString(totalPenalty));

		System.out.println("ETC = "+(transshippingCost + orderingCost[0] + orderingCost[1] +(totalHolding[0] + totalHolding[1] + totalPenalty[0] + totalPenalty[1])+totalFutureCost));
		System.out.println("------------------------------------------------------------");

	}
/*
	public static void solveSampleInstance(Instances problemInstance) {      
		Instance instance; 
		switch(problemInstance) {
		case SAMPLE_POISSON:
		default:
			instance = InstancePortfolio.generateSamplePoissonInstance();
			break;
		}
		* test single Action*
		System.out.println("=============================================");
		long timeStart = System.currentTimeMillis();
		int[] testInventory = {30,30};
		int[] testAction = {0,0,0};
		LTsingleAction.testSingleAction(instance, solution, testInventory, testAction);
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000.0+"s");

	}

	public static void main(String[] args) {
		Instances instance = Instances.SAMPLE_POISSON;
		solveSampleInstance(instance);
	}
*/
}




