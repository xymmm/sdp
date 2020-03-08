package minlp_Poisson;

import java.util.Arrays;

public class mainComputation_PoissonMINLP {
	
	public static void main(String[] args) throws Exception {
		
		double[] demandMean = {20, 40, 60, 40};
		double fixedCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		double initialStock = 0;
		int partitions = 10;		
		
		double pace = 100;
		boolean rangedQ = false;
		
		//**********************sQt for Poisson Demand*************************
		long timeStartsQt = System.currentTimeMillis();
		double[] sQtschedule = sQTminlp_oneRun.sQTminlpSchedule(demandMean, fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
		System.out.println("Poisson Demand: optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));
		double[] s_sQt = sQTminlp_heuristic.reorderPoint_sQtHeuristic(demandMean, fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQtschedule, rangedQ);
		System.out.println("Associated reorder point is "+Arrays.toString(s_sQt));
		long timeEndsQt = System.currentTimeMillis();
		System.out.println("Time consumed for sQt is "+(timeEndsQt - timeStartsQt)/1000.0+"s");
		
		
		//**********************sQ for Poisson Demand*************************
		long timeStartsQ = System.currentTimeMillis();
		double[] sQschedule = sQminlp_oneRun.sQPoissonMinlpSchedule(demandMean, fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
		System.out.println("Poisson Demand: optimal schedule for sQ policy is "+Arrays.toString(sQschedule));
		double[] s_sQ = sQminlp_recursive.reorderPoint_sQheuristic(demandMean, fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQschedule, rangedQ);
		System.out.println("Associated reorder point is "+Arrays.toString(s_sQ));
		long timeEndsQ = System.currentTimeMillis();
		System.out.println("Time consumed for sQ is "+(timeEndsQ - timeStartsQ)/1000.0+"s");

	}

}
