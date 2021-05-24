package LateralTransshipment;

import sdp.data.InstanceDouble;
import umontreal.ssj.probdist.Distribution;

public class singleOrderUpTo {
	
	public static double computeSumInventory(int i1, int i2, int targetPeriodIndex,
			double fixedOrderingCost, double unitCost, double holdingCost, double penaltyCost,
			double[] demandMean1, double[] demandMean2, 
			double tail, int minInventory, int maxInventory, int maxQuantity, 
			boolean initialOrder, double stdParameter) {
		
		boolean Normal = false;
		double[] demandMean = new double[demandMean1.length];
		for(int i=0; i<demandMean.length; i++) {
			demandMean[i] = demandMean1[i] + demandMean2[i];
		}
		InstanceDouble instance = new InstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
				demandMean, tail, 2*minInventory, 2*maxInventory, 2*maxQuantity, stdParameter);
		sS.sSsolution solution = sS.sS.solveInstance(instance, initialOrder, Normal);	

		double sumInventory = i1 + i2 + solution.optimalAction[i1+i2+2*minInventory][targetPeriodIndex];
		return sumInventory;
	}
	
	public static Solution sumInventoryAction(Instance instance) {
		double demandProbabilities [][][] = LTbackwards_2stages.computeDemandProbability(instance);

		int optimalActionOrder[][][][] = new int [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()][];
		double GnOrder[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];
		double CnOrder[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];

		int optimalActionTransshipment[][][] = new int [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];
		double GnTransshipment[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];
		double CnTransshipment[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];

		/** Compute Expected Cost **/

		for(int t = instance.getStages()-1; t >= 0; t--) {                               // Time

			// Orders last
			double totalCostO[][][][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1][instance.stateSpaceSize()+1];
			for(int iA = 0; iA < instance.stateSpaceSize(); iA++) {                          // Inventory A
				for(int iB = 0; iB < instance.stateSpaceSize(); iB++) {                       // Inventory B
					for(int QA = 0; QA <= instance.stateSpaceSize(); QA++) {                   // Order A
						for(int QB = 0; QB <= instance.stateSpaceSize(); QB++) {                // Order B
							totalCostO[iA][iB][QA][QB] += (QA > 0) ? (instance.KA + instance.vA * QA) : 0;
							totalCostO[iA][iB][QA][QB] += (QB > 0) ? (instance.KB + instance.vB * QB) : 0;
							double totalProbabilityMass = 0;
							for(int dA = 0; dA < demandProbabilities[0][t].length; dA++) {       // Demand A
								for(int dB = 0; dB < demandProbabilities[1][t].length; dB++) {    // Demand B
									double immediateCost = 0;
									double futureCost = 0;
									if(
											(instance.inventory(iA) + QA - dA <= instance.maxInventory) && (instance.inventory(iA) + QA - dA >= instance.minInventory) &&
											(instance.inventory(iB) + QB - dB <= instance.maxInventory) && (instance.inventory(iB) + QB - dB >= instance.minInventory)) {
										immediateCost = demandProbabilities[0][t][dA]*demandProbabilities[1][t][dB]*
												LTbackwards_2stages.computeImmediateEndOfPeriodCost(instance.inventory(iA),instance.inventory(iB), QA, QB, dA, dB, instance.hA, instance.hB, instance.pA, instance.pB);
										futureCost = demandProbabilities[0][t][dA]*demandProbabilities[1][t][dB]*( (t==instance.getStages()-1) ? 0 : CnTransshipment[t+1][iA+QA-dA][iB+QB-dB]);
										totalProbabilityMass += demandProbabilities[0][t][dA]*demandProbabilities[1][t][dB];
									}
									totalCostO[iA][iB][QA][QB] += immediateCost + futureCost;
								}
							}
							totalCostO[iA][iB][QA][QB]/=totalProbabilityMass;
						}
					}
					GnOrder[t][iA][iB] = totalCostO[iA][iB][0][0];
					CnOrder[t][iA][iB] = LTbackwards_2stages.getOptimalCost(totalCostO[iA][iB]);
					optimalActionOrder[t][iA][iB] = LTbackwards_2stages.getOptimalAction(totalCostO[iA][iB]);
				}
			}

			// Then transshipment
			for(int iA = 0; iA < instance.stateSpaceSize(); iA++) {                          // Inventory A
				for(int iB = 0; iB < instance.stateSpaceSize(); iB++) {                       // Inventory B
					for(int T = 0; T <= instance.stateSpaceSize(); T++) {                      // Transship
							GnTransshipment[t][iA][iB] = 0;
					}
				}
			}
		}

		
		
		return new Solution(optimalActionOrder, optimalActionTransshipment, GnTransshipment, GnOrder, CnTransshipment, CnOrder);
	}
	
	public static void solveSampleInstance(Instances problemInstance) {      
		Instance instance; 
		switch(problemInstance) {
		case SAMPLE_POISSON:
		default:
			instance = InstancePortfolio.generateSamplePoissonInstance();
			break;
		}
		long timeStart = System.currentTimeMillis();
		Solution solution = sumInventoryAction();
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000.0+"s");
//		printSolution(instance, solution);
//		writeResults(0, instance, solution, "src/main/java/LateralTransshipment/OverallResults.txt");
//		writeCn(instance, solution, "src/main/java/LateralTransshipment/Cn.txt");
		
		timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000.0+"s");

	}
	
	
	public static void main(String[] args) {
		Instances instance = Instances.SAMPLE_POISSON;
		solveSampleInstance(instance);		
	}
}
	

	
	
	
	
	
	
	
	
		







