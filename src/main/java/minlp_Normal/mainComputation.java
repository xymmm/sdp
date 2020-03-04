package minlp_Normal;

import java.util.Arrays;

public class mainComputation {
	
	public static void main(String args[]) throws Exception {
		
		double[] demandMean = {20, 40, 60, 40};
		double fixedCost = 100;//5,10,20
		double unitCost = 1;//0,1
		double holdingCost = 1;
		double penaltyCost = 10;//2,3
		double initialStock = 0;
		double stdParameter = 0.25;

		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;
		
		double pace = 100;
		
		//============================== sQt minlp ==========================
		//sQt minlp 
		
		double[] sQtschedule = sQTminlpNormal_oneRun.sQTminlpSchedule(
				demandMean, fixedCost, unitCost, holdingCost, penaltyCost,
				initialStock, stdParameter, 
				partitions, piecewiseProb, means, error);
		double[] s_sQt = sQTminlpNormal_heuristic.reorderPoint_sQtHeuristic(
				demandMean, fixedCost, unitCost, holdingCost, penaltyCost, 
				initialStock, stdParameter, partitions, piecewiseProb, means, error, pace, sQtschedule);
		
		System.out.println("optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));
		System.out.println("Associated reorder poing is "+Arrays.toString(s_sQt));
		System.out.println();
		
		//============================== sQ minlp ==========================
		//schedule
		double[] sQschedule = sQminlpNormal_oneRun.sQminlpSchedule(
				demandMean, fixedCost, unitCost, holdingCost, penaltyCost,
			 initialStock, stdParameter, partitions, piecewiseProb, means, error);		
		//reorder point
		double[] s_sQ = sQminlpNormal_recursive.reorderPoint_sQheuristic(
				demandMean, fixedCost, unitCost, holdingCost, penaltyCost, 
				initialStock, stdParameter,
				partitions, piecewiseProb, means, error, pace, sQschedule);
		System.out.println("optimal schedule for sQt policy is "+Arrays.toString(sQschedule));
		System.out.println("Associated reorder poing is "+Arrays.toString(s_sQ));
		System.out.println();



	}

}
