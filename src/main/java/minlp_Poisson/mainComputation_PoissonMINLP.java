package minlp_Poisson;

import java.util.Arrays;

import minlp_Normal.simNormalInstance;

public class mainComputation_PoissonMINLP {
	
	public static void main(String[] args) throws Exception {
		
		double fixedCost = 5;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 3;
		double initialStock = 0;
		int partitions = 10;		
		
		double pace = 8;
		boolean rangedQ = true;
		
		double demandMean [][] = {
				//{1, 2, 4, 5, 6, 3},
				{4, 5, 6, 3 ,2, 1}
				//{2.5, 1, 3.5, 4.5, 1.5, 3},
				//{3.5, 1, 2, 4.5, 1, 3},
				//{3, 3, 3, 3, 3, 3},
				//{1.77, 1.31, 4.30, 3.81, 1.49, 4.54},
				//{4, 7, 3.5, 2, 3, 1},
				//{1, 2.5, 3.5, 4, 3, 2},
				//{2.5, 1, 3.5, 4.5, 1.5, 5.5},
				//{2, 1.5, 4, 4, 1.5, 2}
		};
		
		int count = 10;
		
		for(int i=0; i<demandMean.length; i++) {
		//**********************sQt for Poisson Demand*************************
		/*long timeStartsQt = System.currentTimeMillis();
		double[] sQtschedule = sQTminlp_oneRun.sQTminlpSchedule(demandMean[i], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
		System.out.println("Poisson Demand: optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));
		double[] s_sQt = sQTminlp_heuristic.reorderPoint_sQtHeuristic(demandMean[i], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQtschedule, rangedQ);
		System.out.println("Associated reorder point is "+Arrays.toString(s_sQt));
		long timeEndsQt = System.currentTimeMillis();
		System.out.println("Time consumed for sQt is "+(timeEndsQt - timeStartsQt)/1000.0+"s");
		
		simPoissonInstance PoissonInstance_sQt = new simPoissonInstance(
				demandMean[i], 
				fixedCost,
				unitCost,
				holdingCost, 
				penaltyCost, 
				initialStock,  
				sQtschedule, 
				s_sQt				
				);
		
		
		minlp_Poisson.simulationPoissonMINLP.simulationPoissonMINLPmultipleRuns(PoissonInstance_sQt, count);
		PoissonInstance_sQt.statCost.setConfidenceIntervalStudent();
		double sQt_simCost = PoissonInstance_sQt.statCost.average();
		System.out.println("Simulated cost = "+sQt_simCost);
		System.out.println();
		*/
		
		
		//**********************sQ for Poisson Demand*************************
		long timeStartsQ = System.currentTimeMillis();
		double[] sQschedule = sQminlp_oneRun.sQPoissonMinlpSchedule(demandMean[i], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
		System.out.println("Poisson Demand: optimal schedule for sQ policy is "+Arrays.toString(sQschedule));
		double[] s_sQ = sQminlp_recursive.reorderPoint_sQheuristic(demandMean[i], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQschedule, rangedQ);
		System.out.println("Associated reorder point is "+Arrays.toString(s_sQ));
		long timeEndsQ = System.currentTimeMillis();
		System.out.println("Time consumed for sQ is "+(timeEndsQ - timeStartsQ)/1000.0+"s");
		
		simPoissonInstance PoissonInstance_sQ = new simPoissonInstance(
				demandMean[i], 
				fixedCost,
				unitCost,
				holdingCost, 
				penaltyCost, 
				initialStock, 
				sQschedule, 
				s_sQ				
				);

		minlp_Poisson.simulationPoissonMINLP.simulationPoissonMINLPmultipleRuns(PoissonInstance_sQ, count);
		PoissonInstance_sQ.statCost.setConfidenceIntervalStudent();
		double sQ_simCost = PoissonInstance_sQ.statCost.average();
		System.out.println("Simulated cost = "+sQ_simCost);
		}
	}

}
