package LateralTransshipment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

public class LTsingleAction {

	public static double[][] readFromTXT(double[][] array) throws Exception {

		Scanner sc = new Scanner( new BufferedReader(new FileReader("src/main/java/lateralTransshipment/futureCost.txt")));
		while(sc.hasNextLine()) {
			for (int i=0; i<array.length; i++) {
				for (int j=0; j<array[i].length; j++) {
					String line = sc.nextLine();
					array[i][j] = Double.parseDouble(line);
				}
			}
		}
		//		System.out.println(Arrays.deepToString(array));
		return array;
	}

	public static void testSingleAction(Instance instance, int[] testInventory, int[] testAction) {

		int transshipment = testAction[0];
		int QA = testAction[1];
		int QB = testAction[2];

		double demandProbabilities [][][] = LTbackwards_2stages.computeDemandProbability(instance);

		int periodTotal = 2;
		double CnOrder[][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()];
		double CnTransshipment[][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()];
		double optimalCost[][] = new double[instance.stateSpaceSize()][instance.stateSpaceSize()];
		readFromTXT(optimalCost);

		double orderingCost[] = new double[2];

		// Orders last
		double totalCostO[] = new double[2];//for each location
		orderingCost[0] = QA>0 ? instance.KA + instance.vA * QA : 0;
		orderingCost[1] = QB>0 ? instance.KB + instance.vB * QB : 0;
		
		double totalProbabilityMass = 0;
		double[] expholding = new double[2];
		double[] exppenalty = new double[2];
		for(int dA = 0; dA < demandProbabilities[0][0].length; dA++) {       // Demand A
			for(int dB = 0; dB < demandProbabilities[1][0].length; dB++) {    // Demand B
				double immediateCost = 0;
				double futureCost = 0;
				if(
						(testInventory[0] + QA - dA <= instance.maxInventory) && (testInventory[0] + QA - dA >= instance.minInventory) &&
						(testInventory[1] + QB - dB <= instance.maxInventory) && (testInventory[1] + QB - dB >= instance.minInventory)) {
					expholding[0] += demandProbabilities[0][0][dA]*LTbackwards_2stages.computeSingleHolding(testInventory[0], QA, dA, instance.hA);
					expholding[1] += demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSingleHolding(testInventory[1], QB, dB, instance.hB);
					exppenalty[0] += demandProbabilities[0][0][dA]*LTbackwards_2stages.computeSinglePenalty(testInventory[0], QA, dA, instance.pA);
					exppenalty[1] += demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSinglePenalty(testInventory[1], QB, dB, instance.pB);
					futureCost = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*optimalCost[iA+QA-dA][iB+QB-dB];
						totalProbabilityMass += demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB];
					}
					totalCostO[iA][iB][QA][QB] += immediateCost + futureCost;
				}
			}
			totalCostO[iA][iB][QA][QB]/=totalProbabilityMass;

			GnOrder[0][iA][iB] = totalCostO[iA][iB][0][0];
			CnOrder[0][iA][iB] = LTbackwards_2stages.getOptimalCost(totalCostO[iA][iB]);


			// Then transshipment
			double totalCostT[][][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1];
			for(int iA = 0; iA < instance.stateSpaceSize(); iA++) {                          // Inventory A
				for(int iB = 0; iB < instance.stateSpaceSize(); iB++) {                       // Inventory B
					for(int T = 0; T <= instance.stateSpaceSize(); T++) {                      // Transship

						totalCostT[iA][iB][T] += instance.R + instance.u * Math.abs(instance.inventory(T));
						double futureCost = 0;
						if(instance.inventory(T) > 0 && instance.inventory(T) > instance.inventory(iA))
							totalCostT[iA][iB][T] = Double.MAX_VALUE;
						else if(instance.inventory(T) < 0 && -instance.inventory(T) > instance.inventory(iB))
							totalCostT[iA][iB][T] = Double.MAX_VALUE;
						else if(
								(instance.inventory(iA) - instance.inventory(T) <= instance.maxInventory) && (instance.inventory(iA) - instance.inventory(T) >= instance.minInventory) &&
								(instance.inventory(iB) + instance.inventory(T) <= instance.maxInventory) && (instance.inventory(iB) + instance.inventory(T) >= instance.minInventory)
								) {
							futureCost = CnOrder[0][instance.index(instance.inventory(iA) - instance.inventory(T))][instance.index(instance.inventory(iB) + instance.inventory(T))];
							totalCostT[iA][iB][T] += futureCost;
						}else {
							totalCostT[iA][iB][T] = Double.MAX_VALUE;
						}           

					}
					CnTransshipment[0][iA][iB] = LTbackwards_2stages.getOptimalCost(totalCostT[iA][iB]);
				}
			}
	}




	public static void printTestComparison(Instance instance, Solution solution, int[] testInventory, int[] testAction) {
		System.out.println("testInventory = "+Arrays.toString(testInventory));
		System.out.println("Optimal Action = "
				+solution.CnTransshipment[0][testInventory[0]-instance.minInventory][testInventory[1]-instance.minInventory]);
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
		int[] testInventory = {1,1};
		int[] testAction = {0,1,1};
		testSingleAction(instance, testInventory, testAction);
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000.0+"s");
	}

	public static void main(String[] args) {
		Instances instance = Instances.SAMPLE_POISSON;
		solveSampleInstance(instance);
		/*		double[][] Cn = new double[51][51];
		try {
			readFromTXT(Cn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i=0; i<51; i++) {
			for(int j=0; j<51; j++) {
				System.out.print(Cn[i][j] + "\t");
			}
			System.out.println();
		}
		 */
	}

}




