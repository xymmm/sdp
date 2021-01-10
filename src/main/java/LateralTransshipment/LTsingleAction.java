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
		sc.close();
		//		System.out.println(Arrays.deepToString(array));
		return array;
	}

	public static void testSingleAction(Instance instance, int[] testInventory, int[] testAction) {

		int transshipment = testAction[0];
		int testQA = testAction[1];
		int testQB = testAction[2];

		double demandProbabilities [][][] = LTbackwards_2stages.computeDemandProbability(instance);

		double CnOrder[][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()];
		int optimalActionOrder[][][] = new int [instance.stateSpaceSize()][instance.stateSpaceSize()][2];
		double CnHolding[][][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()][2];
		double CnPenalty[][][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()][2];
		double optimalCost[][] = new double[instance.stateSpaceSize()][instance.stateSpaceSize()];
		try {
			readFromTXT(optimalCost);
		} catch (Exception e) {
			e.printStackTrace();
		}

		double totalCostO[][][][] = new double [instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1][instance.stateSpaceSize()+1];
		double imholding[][][][][] = new double[instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1][instance.stateSpaceSize()+1][2];
		double impenalty[][][][][] = new double[instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1][instance.stateSpaceSize()+1][2];
		double expholding[][][][][] = new double[instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1][instance.stateSpaceSize()+1][2];
		double exppenalty[][][][][] = new double[instance.stateSpaceSize()][instance.stateSpaceSize()][instance.stateSpaceSize()+1][instance.stateSpaceSize()+1][2];

		for(int iA = 0; iA < instance.stateSpaceSize(); iA++) {    // Inventory A
			for(int iB = 0; iB < instance.stateSpaceSize(); iB++) {// Inventory B
				//for target inventory
				if(instance.inventory(iA) == testInventory[0] && instance.inventory(iB) == testInventory[1]) {	
					totalCostO[iA][iB][testQA][testQB] += (testQA > 0) ? instance.KA + instance.vA * testQA : 0;
					totalCostO[iA][iB][testQA][testQB] += (testQB > 0) ? instance.KB + instance.vB * testQB : 0;
					//for target inventory and target action
					double immediateCost = 0;
					double futureCost = 0;
					double totalProbabilityMass = 0;
					for(int dA = 0; dA < demandProbabilities[0][0].length; dA++) {       // Demand A
						for(int dB = 0; dB < demandProbabilities[1][0].length; dB++) {    // Demand B
							if(
									(instance.inventory(iA) + testQA - dA <= instance.maxInventory) && (instance.inventory(iA) + testQA - dA >= instance.minInventory) &&
									(instance.inventory(iB) + testQB - dB <= instance.maxInventory) && (instance.inventory(iB) + testQB - dB >= instance.minInventory)) {
								imholding[iA][iB][testQA][testQB][0] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSingleHolding(instance.inventory(iA), testQA, dA, instance.hA);
								imholding[iA][iB][testQA][testQB][1] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSingleHolding(instance.inventory(iB), testQB, dB, instance.hB);
								impenalty[iA][iB][testQA][testQB][0] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSinglePenalty(instance.inventory(iA), testQA, dA, instance.pA);
								impenalty[iA][iB][testQA][testQB][1] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSinglePenalty(instance.inventory(iB), testQB, dB, instance.pB);

								immediateCost = expholding[iA][iB][testQA][testQB][0] + expholding[iA][iB][testQA][testQB][1] 
										+ exppenalty[iA][iB][testQA][testQB][0] + exppenalty[iA][iB][testQA][testQB][1];
								futureCost = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*optimalCost[iA+testQA-dA][iB+testQB-dB];
								totalProbabilityMass += demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB];
							}
							totalCostO[iA][iB][testQA][testQB] += immediateCost + futureCost;
						}
					}
					totalCostO[iA][iB][testQA][testQB]/=totalProbabilityMass;System.out.println("if cost = "+totalCostO[iA][iB][testQA][testQB]);
					CnOrder[iA][iB] = totalCostO[iA][iB][testQA][testQB];System.out.println("CnOrder(target) = "+CnOrder[iA][iB]);
					CnHolding[iA][iB][0] = imholding[iA][iB][testQA][testQB][0];
					CnHolding[iA][iB][1] = imholding[iA][iB][testQA][testQB][1];
					CnPenalty[iA][iB][0] = impenalty[iA][iB][testQA][testQB][0];
					CnPenalty[iA][iB][0] = impenalty[iA][iB][testQA][testQB][1];
				}else {
					for(int QA = 0; QA <= instance.stateSpaceSize(); QA++) {                   // Order A
						for(int QB = 0; QB <= instance.stateSpaceSize(); QB++) {                // Order B
							totalCostO[iA][iB][QA][QB] += (QA > 0) ? instance.KA + instance.vA * QA : 0;
							totalCostO[iA][iB][QA][QB] += (QB > 0) ? instance.KB + instance.vB * QB : 0;
							double mass = 0;
							for(int dA = 0; dA < demandProbabilities[0][0].length; dA++) {       // Demand A
								for(int dB = 0; dB < demandProbabilities[1][0].length; dB++) {    // Demand B
									double immediateCost2 = 0;
									double futureCost2 = 0;
									if(
											(instance.inventory(iA) + QA - dA <= instance.maxInventory) && (instance.inventory(iA) + QA - dA >= instance.minInventory) &&
											(instance.inventory(iB) + QB - dB <= instance.maxInventory) && (instance.inventory(iB) + QB - dB >= instance.minInventory)) {
										imholding[iA][iB][QA][QB][0] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSingleHolding(instance.inventory(iA), QA, dA, instance.hA);
										imholding[iA][iB][QA][QB][1] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSingleHolding(instance.inventory(iB), QB, dB, instance.hB);
										impenalty[iA][iB][QA][QB][0] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSinglePenalty(instance.inventory(iA), QA, dA, instance.pA);
										impenalty[iA][iB][QA][QB][1] = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*LTbackwards_2stages.computeSinglePenalty(instance.inventory(iB), QB, dB, instance.pB);

										immediateCost2 = expholding[iA][iB][QA][QB][0] + expholding[iA][iB][QA][QB][1] 
												+ exppenalty[iA][iB][QA][QB][0] + exppenalty[iA][iB][QA][QB][1];
										futureCost2 = demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB]*optimalCost[iA+QA-dA][iB+QB-dB];
										mass += demandProbabilities[0][0][dA]*demandProbabilities[1][0][dB];
					
									}
									totalCostO[iA][iB][QA][QB] += immediateCost2 + futureCost2;
									expholding[iA][iB][QA][QB][0] += imholding[iA][iB][QA][QB][0]; 
									expholding[iA][iB][QA][QB][1] += imholding[iA][iB][QA][QB][1];
									exppenalty[iA][iB][QA][QB][0] += impenalty[iA][iB][QA][QB][0]; 
									exppenalty[iA][iB][QA][QB][1] += impenalty[iA][iB][QA][QB][1]; 
								}
							}
							totalCostO[iA][iB][QA][QB]/=mass;
							expholding[iA][iB][QA][QB][0]/=mass; expholding[iA][iB][QA][QB][1]/=mass;
							exppenalty[iA][iB][QA][QB][0]/=mass; exppenalty[iA][iB][QA][QB][1]/=mass;
						}
					}
					CnOrder[iA][iB] = LTbackwards_2stages.getOptimalCost(totalCostO[iA][iB]);
					if(iA == 21 && iB == 21)System.out.println(CnOrder[iA][iB]);
					optimalActionOrder[iA][iB] = LTbackwards_2stages.getOptimalAction(totalCostO[iA][iB]);
					CnHolding[iA][iB] = expholding[iA][iB][optimalActionOrder[iA][iB][0]][optimalActionOrder[iA][iB][1]];
					CnPenalty[iA][iB] = exppenalty[iA][iB][optimalActionOrder[iA][iB][0]][optimalActionOrder[iA][iB][1]];
				}
			}
		}

		// Then transshipment, for the target inventory, no need to compute others
		double transhippingCost = (Math.abs(transshipment)>0) ? (Math.abs(transshipment)*instance.u + instance.R) : 0;
		int[] inventoryAfterTransship = {testInventory[0] - transshipment, testInventory[1] + transshipment};
		double finalCost = transhippingCost + CnOrder[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])];

		double tempOrderinCompute = ((testQA > 0) ? instance.KA + instance.vA * testQA : 0) + ((testQB > 0) ? instance.KB + instance.vB * testQB : 0);


		//print
		System.out.println();
		System.out.println("testInventory = "+Arrays.toString(testInventory));
		System.out.println("testAction = "+Arrays.toString(testAction));
		System.out.println("---------------");
		System.out.println("transshipping cost = "+transhippingCost);
		System.out.println("ordering cost = " + tempOrderinCompute);
		System.out.println("holding cost = " 
				+ (CnHolding[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])][0] 
				+ CnHolding[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])][1])+" = "
				+ Arrays.toString(CnHolding[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])]));
		System.out.println("penalty cost = " 
				+ (CnPenalty[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])][0] 
				+ CnPenalty[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])][1])+" = "
				+ Arrays.toString(CnPenalty[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])]));
		System.out.println("CnOrder(transshipped) = "+CnOrder[instance.index(inventoryAfterTransship[0])][instance.index(inventoryAfterTransship[1])]);
		System.out.println("ETC = "+finalCost);
		

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
		int[] testInventory = {30,2};
		int[] testAction = {5,0,21};
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




