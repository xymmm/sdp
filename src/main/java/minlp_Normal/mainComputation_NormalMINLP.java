package minlp_Normal;

import java.util.Arrays;

import umontreal.ssj.util.Chrono;

public class mainComputation_NormalMINLP {
	
	public static void main(String args[]) throws Exception {
		
		double[] demandMean = {20, 40, 60, 40};
		double fixedCost = 100;//5,10,20
		double unitCost = 0;//0,1
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
		System.out.println("optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));

		double[] s_sQt = sQTminlpNormal_heuristic.reorderPoint_sQtHeuristic(
				demandMean, fixedCost, unitCost, holdingCost, penaltyCost, 
				initialStock, stdParameter, partitions, piecewiseProb, means, error, pace, sQtschedule);
		
		System.out.println("Associated reorder poing is "+Arrays.toString(s_sQt));

		
		simNormalInstance normalInstance_sQt = new simNormalInstance(
				demandMean, 
				fixedCost,
				unitCost,
				holdingCost, 
				penaltyCost, 
				initialStock, 
				stdParameter, 
				sQtschedule, 
				s_sQt				
				);
		
		int count = 2;
		minlp_Normal.simulationNormalMINLP.simulationNormalMINLPmultipleRuns(normalInstance_sQt, count);
		normalInstance_sQt.statCost.setConfidenceIntervalStudent();
		double sQt_simCost = normalInstance_sQt.statCost.average();
		System.out.println(sQt_simCost);
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
		System.out.println("optimal schedule for sQ policy is "+Arrays.toString(sQschedule));
		System.out.println("Associated reorder point is "+Arrays.toString(s_sQ));
		
		simNormalInstance normalInstance_sQ = new simNormalInstance(
				demandMean, 
				fixedCost,
				unitCost,
				holdingCost, 
				penaltyCost, 
				initialStock, 
				stdParameter, 
				sQschedule, 
				s_sQ				
				);

		minlp_Normal.simulationNormalMINLP.simulationNormalMINLPmultipleRuns(normalInstance_sQ, count);
		normalInstance_sQ.statCost.setConfidenceIntervalStudent();
		double sQ_simCost = normalInstance_sQ.statCost.average();
		System.out.println(sQ_simCost);



	}

}
