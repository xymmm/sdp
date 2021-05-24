package LateralTransshipment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import umontreal.ssj.probdist.ContinuousDistribution;
import umontreal.ssj.probdist.DiscreteDistributionInt;
import umontreal.ssj.probdist.Distribution;
import umontreal.ssj.probdist.PoissonDist;

public class singleOrderUpTo {
	static double[] tabulateProbabilityContinuous(ContinuousDistribution dist, double tail) {
		// Note that minDemand is assumed to be 0;
		int maxDemand = (int)Math.round(dist.inverseF(1-tail));
		double[] demandProbabilities = new double[maxDemand + 1];
		for(int i = 0; i <= maxDemand; i++) {
			demandProbabilities [i] = (dist.cdf(i+0.5)-dist.cdf(i-0.5))/(dist.cdf(maxDemand+0.5)-dist.cdf(-0.5));
		}
		assert(Arrays.stream(demandProbabilities).sum() == 1);
		return demandProbabilities;
	}

	static double[] tabulateProbabilityDiscrete(DiscreteDistributionInt dist, double tail) {
		// Note that minDemand is assumed to be 0;
		int maxDemand = dist.inverseFInt(1-tail);
		double[] demandProbabilities = new double[maxDemand + 1];
		for(int i = 0; i <= maxDemand; i++) {
			demandProbabilities [i] = dist.prob(i)/dist.cdf(maxDemand);
		}
		assert(Arrays.stream(demandProbabilities).sum() == 1);
		return demandProbabilities;
	}

	static double[][][] computeDemandProbability(Instance instance) {
		double[][][] demandProbability = new double [2][instance.getStages()][];
		for(int t = 0; t < instance.getStages(); t++) {
			if(instance.demandA[t] instanceof ContinuousDistribution) {
				demandProbability[0][t] = tabulateProbabilityContinuous((ContinuousDistribution)instance.demandA[t], instance.tail);
			}else if(instance.demandA[t] instanceof DiscreteDistributionInt) {
				demandProbability[0][t] = tabulateProbabilityDiscrete((DiscreteDistributionInt)instance.demandA[t], instance.tail);
			}else
				throw new NullPointerException("Distribution not recognized.");

			if(instance.demandB[t] instanceof ContinuousDistribution) {
				demandProbability[1][t] = tabulateProbabilityContinuous((ContinuousDistribution)instance.demandB[t], instance.tail);
			}else if(instance.demandB[t] instanceof DiscreteDistributionInt) {
				demandProbability[1][t] = tabulateProbabilityDiscrete((DiscreteDistributionInt)instance.demandB[t], instance.tail);
			}else
				throw new NullPointerException("Distribution not recognized.");
		}
		return demandProbability;
	}

	static double computeImmediateEndOfPeriodCost(
			int iA, int iB, int QA, int QB, int demandA, int demandB,
			double hA, double hB, double pA, double pB) {
		double costA = 
				hA*Math.max(0, iA + QA - demandA) +
				pA*Math.max(0, demandA - iA - QA);
		double costB = 
				hB*Math.max(0, iB + QB - demandB) +
				pB*Math.max(0, demandB - iB - QB);
		return costA + costB;
	}
	
	static double computeSingleHolding(
			int iA, int QA, int demandA, double hA) {
		return hA*Math.max(0, iA + QA - demandA);
	}
	static double computeSinglePenalty(
			int iA, int QA, int demandA, double pA) {
		return pA*Math.max(0, demandA - iA - QA);
	}

	static double getOptimalCost(double[][] expectedTotalCosts) {
		double min = expectedTotalCosts[0][0];
		for(int a = 0; a < expectedTotalCosts.length; a++) {
			for(int b = 0; b < expectedTotalCosts.length; b++) {
				if(expectedTotalCosts[a][b] < min) {
					min = expectedTotalCosts[a][b];
				}
			}
		}
		return min;
	}

	static int[] getOptimalAction(double[][] expectedTotalCosts) {
		double min = expectedTotalCosts[0][0];
		int[] action = new int[2];
		for(int a = 0; a < expectedTotalCosts.length; a++) {
			for(int b = 0; b < expectedTotalCosts.length; b++) {
				if(expectedTotalCosts[a][b] < min) {
					min = expectedTotalCosts[a][b];
					action = new int[]{a,b};
				}
			}
		}
		return action;
	}

	static double getOptimalCost(double[] expectedTotalCosts) {
		double min = expectedTotalCosts[0];
		for(int a = 1; a < expectedTotalCosts.length; a++) {
			if(expectedTotalCosts[a] < min) {
				min = expectedTotalCosts[a];
			}
		}
		return min;
	}

	static int getOptimalAction(double[] expectedTotalCosts) {
		double min = expectedTotalCosts[0];
		int action = 0;
		for(int a = 1; a < expectedTotalCosts.length; a++) {
			if(expectedTotalCosts[a] < min) {
				min = expectedTotalCosts[a];
				action = a;
			}
		}
		return action;
	}

	public static Solution solveInstance(Instance instance) {

		double demandProbabilities [][][] = computeDemandProbability(instance);

		int optimalActionOrder[][][][] = new int [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()][];
		double GnOrder[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];
		double CnOrder[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];

		int optimalActionTransshipment[][][] = new int [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];
		double GnTransshipment[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];
		double CnTransshipment[][][] = new double [instance.getStages()][instance.stateSpaceSize()][instance.stateSpaceSize()];

		/** Compute Expected Cost **/

		for(int t = instance.getStages()-1; t >= 0; t--) {                               // Time
//			System.out.println("t = "+(t+1));
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
												computeImmediateEndOfPeriodCost(instance.inventory(iA),instance.inventory(iB), QA, QB, dA, dB, instance.hA, instance.hB, instance.pA, instance.pB);
										futureCost = demandProbabilities[0][t][dA]*demandProbabilities[1][t][dB]*( (t==instance.getStages()-1) ? 0 : CnOrder[t+1][iA+QA-dA][iB+QB-dB]);
										totalProbabilityMass += demandProbabilities[0][t][dA]*demandProbabilities[1][t][dB];
									}
									totalCostO[iA][iB][QA][QB] += immediateCost + futureCost;
								}
							}
							totalCostO[iA][iB][QA][QB]/=totalProbabilityMass;
						}
					}
					GnOrder[t][iA][iB] = totalCostO[iA][iB][0][0];
					CnOrder[t][iA][iB] = getOptimalCost(totalCostO[iA][iB]);
					optimalActionOrder[t][iA][iB] = getOptimalAction(totalCostO[iA][iB]);
				}
			}
		}

		return new Solution(optimalActionOrder, optimalActionTransshipment, GnTransshipment, GnOrder, CnTransshipment, CnOrder);
	}
	
	public static void writeResults(int timeIndex, Instance instance, Solution solution, String FileName) {

		FileWriter fw = null;
		try {
			File f = new File(FileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		
		//Cn
		pw.println("Expected total cost (Cn): ");		      
		pw.print("\t");
		for(int j = 0; j < instance.stateSpaceSize(); j++) {
			pw.print(instance.inventory(j) + "\t");
		}
		pw.println();
		for(int i = instance.stateSpaceSize()-1; i >=0 ; i--) {
			pw.print(instance.inventory(i) + "\t");
			for(int j = 0; j < instance.stateSpaceSize(); j++) {
				pw.print(solution.CnOrder[timeIndex][i][j] + "\t");
			}
			pw.println();
		}
		pw.println();
		//overall action
		pw.println("overall action");
		pw.print("\t");
		for(int j = 0; j < instance.stateSpaceSize(); j++) {
			pw.print(instance.inventory(j) + "\t");
		}
		pw.println();
		for(int i = instance.stateSpaceSize()-1; i >= 0 ; i--) {
			pw.print(instance.inventory(i) + "\t");
			for(int j = 0; j < instance.stateSpaceSize(); j++) {
				pw.print(solution.optimalActionTransshipment[timeIndex][i][j] + 
						"|" + solution.optimalActionOrder[timeIndex][i][j][0] + 
						"|" + solution.optimalActionOrder[timeIndex][i][j][1] + "\t");
			}
			pw.println();
		}
		
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	public static void printSolution(Instance instance, Solution solution) {
		int t = 0;
		System.out.println("Expected total cost with zero initial inventory (Gn): "+(solution.CnTransshipment[t][-instance.minInventory][-instance.minInventory]));		      
		System.out.print("\t");
		for(int j = 0; j < instance.stateSpaceSize(); j++) {
			System.out.print(instance.inventory(j) + "\t");
		}
		System.out.println();
		//		      for(int i = 0; i < instance.stateSpaceSize(); i++) {
		for(int i = instance.stateSpaceSize()-1; i >=0 ; i--) {
			System.out.print(instance.inventory(i) + "\t");
			for(int j = 0; j < instance.stateSpaceSize(); j++) {
				System.out.print(solution.GnTransshipment[t][i][j] + "\t");
			}
			System.out.println();
		}

		System.out.println();

		System.out.print("\t");
		for(int j = 0; j < instance.stateSpaceSize(); j++) {
			System.out.print(instance.inventory(j) + "\t");
		}
		System.out.println();
		for(int i = 0; i < instance.stateSpaceSize(); i++) {
			System.out.print(instance.inventory(i) + "\t");
			for(int j = 0; j < instance.stateSpaceSize(); j++) {
				System.out.print(solution.optimalActionTransshipment[t][i][j] + "\t");
			}
			System.out.println();
		}

		System.out.println();

		System.out.print("\t");
		for(int j = 0; j < instance.stateSpaceSize(); j++) {
			System.out.print(instance.inventory(j) + "\t");
		}
		System.out.println();
		for(int i = 0; i < instance.stateSpaceSize(); i++) {
			System.out.print(instance.inventory(i) + "\t");
			for(int j = 0; j < instance.stateSpaceSize(); j++) {
				System.out.print(solution.optimalActionOrder[t][i][j][0] + "\t");
			}
			System.out.println();
		}

		System.out.println();

		System.out.print("\t");
		for(int j = 0; j < instance.stateSpaceSize(); j++) {
			System.out.print(instance.inventory(j) + "\t");
		}
		System.out.println();
		for(int i = 0; i < instance.stateSpaceSize(); i++) {
			System.out.print(instance.inventory(i) + "\t");
			for(int j = 0; j < instance.stateSpaceSize(); j++) {
				System.out.print(solution.optimalActionOrder[t][i][j][1] + "\t");
			}
			System.out.println();
		}
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
		Solution solution = solveInstance(instance);
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000.0+"s");
//		printSolution(instance, solution);
		writeResults(0, instance, solution, "src/main/java/LateralTransshipment/OverallResults.txt");

		timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000.0+"s");

	}

	public static void main(String[] args) {
		Instances instance = Instances.SAMPLE_POISSON;
		solveSampleInstance(instance);		
	}
}








