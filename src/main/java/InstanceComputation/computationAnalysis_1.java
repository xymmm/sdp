package InstanceComputation;

import java.util.Arrays;

import minlp_Poisson.sQTminlp_heuristic;
import minlp_Poisson.sQTminlp_oneRun;
import minlp_Poisson.sQminlp_oneRun;
import minlp_Poisson.sQminlp_recursive;
import minlp_Poisson.simPoissonInstance;
import reorderQuantitySystem.sQsystemSolution;
import sS.sSsolution;
import sdp.data.InstanceDouble;

public class computationAnalysis_1 {

	public static void main(String[] args) throws Exception {
		
		double holdingCost = 1;
		double initialStock = 0;
		
		double[] unitCostA = {0,1};
		double[][] KPgroup = {
				{5, 3},
				{10, 5},
				{10, 7}
		};
		
		int partitions = 10;
		boolean rangedQ = true;
		boolean Normal = false;
		double pace = 4;
		int count = 100000;

		
		double tail = 0.00000001;
		double stdParameter = 0.25;
		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;
		
		double demandMean [][] = {
				{1, 2, 4, 5, 6, 3},
				{4, 5, 6, 3 ,2, 1},
				{2.5, 1, 3.5, 4.5, 1.5, 3},
				{3.5, 1, 2, 4.5, 1, 3},
				{3, 3, 3, 3, 3, 3},
				{1.77, 1.31, 4.30, 3.81, 1.49, 4.54},
				{4, 7, 3.5, 2, 3, 1},
				{1, 2.5, 3.5, 4, 3, 2},
				{2.5, 1, 3.5, 4.5, 1.5, 5.5},
				{2, 1.5, 4, 4, 1.5, 2}
		};
		
		double[][][][] results = new double[demandMean.length][unitCostA.length][KPgroup.length][5];
		double[][][][] resultsTime = new double[demandMean.length][unitCostA.length][KPgroup.length][5];
		
		for(int u=0; u<unitCostA.length; u++) {
			System.out.println("=====================================================");
			System.out.println("unit cost = "+unitCostA[u]);
			double unitCost = unitCostA[u];
			for(int g=0; g<KPgroup.length; g++) {
				System.out.println("fixed cost = "+KPgroup[g][0]);
				double fixedCost = KPgroup[g][0];
				System.out.println("penalty cost = "+KPgroup[g][1]);
				double penaltyCost = KPgroup[g][1];
				for(int d=0; d<demandMean.length; d++) {
					System.out.println("instance "+(d+1));
					
					// SDP------------------------------
					InstanceDouble instance = new InstanceDouble(fixedCost, unitCost, holdingCost, penaltyCost,
							demandMean[d], tail, minInventory, maxInventory, maxQuantity, stdParameter );										
					/**sS**/
					long timesSsdp = System.currentTimeMillis();
					sSsolution sSsolution = sS.sS.solveInstance(instance, true, Normal);	//with initial order 
					results[d][u][g][0] = sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0];
					resultsTime[d][u][g][0] = (System.currentTimeMillis() - timesSsdp)/1000.0;
					/**sQt**/
					long timesQtsdp = System.currentTimeMillis();
					sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance, Normal);
					results[d][u][g][1] = sQtsolution.optimalCost;
					resultsTime[d][u][g][1] = (System.currentTimeMillis() - timesQtsdp)/1000.0;
					/**sQ**/
					long timesQsdp = System.currentTimeMillis();
					sQsystemSolution sQsolution = reorderQuantitySystem.optimalSchedule_sQ.optimalSchedule_sQ(instance, Normal);
					results[d][u][g][2] = sQsolution.optimalCost;
					resultsTime[d][u][g][2] = (System.currentTimeMillis() - timesQsdp)/1000.0;
					
					//MINLP-----------------------------
					
					//**********************sQt for Poisson Demand*************************
					long timeStartsQt = System.currentTimeMillis();
					double[] sQtschedule = sQTminlp_oneRun.sQTminlpSchedule(demandMean[d], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
					//System.out.println("Poisson Demand: optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));
					double[] s_sQt = sQTminlp_heuristic.reorderPoint_sQtHeuristic(demandMean[d], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQtschedule, rangedQ);
					//System.out.println("Associated reorder point is "+Arrays.toString(s_sQt));
					simPoissonInstance PoissonInstance_sQt = new simPoissonInstance(
							demandMean[d], fixedCost, unitCost, holdingCost, penaltyCost, 
							initialStock, sQtschedule, s_sQt);					
					minlp_Poisson.simulationPoissonMINLP.simulationPoissonMINLPmultipleRuns(PoissonInstance_sQt, count);
					PoissonInstance_sQt.statCost.setConfidenceIntervalStudent();
					results[d][u][g][3] = PoissonInstance_sQt.statCost.average();
					resultsTime[d][u][g][3] = (System.currentTimeMillis() - timeStartsQt)/1000.0;
					
					//**********************sQ for Poisson Demand*************************
					long timeStartsQ = System.currentTimeMillis();
					double[] sQschedule = sQminlp_oneRun.sQPoissonMinlpSchedule(demandMean[d], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
					//System.out.println("Poisson Demand: optimal schedule for sQ policy is "+Arrays.toString(sQschedule));
					double[] s_sQ = sQminlp_recursive.reorderPoint_sQheuristic(demandMean[d], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQschedule, rangedQ);
					//System.out.println("Associated reorder point is "+Arrays.toString(s_sQ));
					//System.out.println("Time consumed for sQ is "+(timeEndsQ - timeStartsQ)/1000.0+"s");					
					simPoissonInstance PoissonInstance_sQ = new simPoissonInstance(
							demandMean[d],fixedCost,unitCost,holdingCost,penaltyCost, 
							initialStock,sQschedule,s_sQ);
					minlp_Poisson.simulationPoissonMINLP.simulationPoissonMINLPmultipleRuns(PoissonInstance_sQ, count);
					PoissonInstance_sQ.statCost.setConfidenceIntervalStudent();
					results[d][u][g][4] = PoissonInstance_sQ.statCost.average();
					resultsTime[d][u][g][4] = (System.currentTimeMillis() - timeStartsQ)/1000.0;
					
					sdp.util.writeText.writeDoubleArray(results[d][u][g],
							"src/main/java/InstanceComputation/PoissonResults.txt");
					sdp.util.writeText.writeDoubleArray(resultsTime[d][u][g],
							"src/main/java/InstanceComputation/PoissonResultsTime.txt");
					
					System.out.println(Arrays.toString(results[d][u][g]));
					System.out.println(Arrays.toString(resultsTime[d][u][g]));
				}
			}
		}
		
		
		
	}
	
}
