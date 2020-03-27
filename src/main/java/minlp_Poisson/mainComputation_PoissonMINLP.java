package minlp_Poisson;

import java.util.Arrays;

import minlp_Normal.simNormalInstance;
import sS.sSsolution;
import sdp.data.InstanceDouble;

public class mainComputation_PoissonMINLP {
	
	public static void main(String[] args) throws Exception {
		
		double fixedCost = 500;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 20;
		double initialStock = 0;
		int partitions = 10;		
		
		double pace = 32;
		boolean rangedQ = false;
		
		double demandMean [][] = {
				{76,27,10,36,88,136,149,121,68,22,11,42,96,140,148,114,60}
				//{20, 40, 60, 40}
		};
		
		int count = 1000;
		boolean Normal = false;
		double tail = 0.00000001;
		double stdParameter = 0.1;

		int minInventory = -1500;
		int maxInventory = 1500;
		int maxQuantity = 800;
		
		for(int i=0; i<demandMean.length; i++) {
			
			InstanceDouble instance = new InstanceDouble(fixedCost, unitCost, holdingCost, penaltyCost,
					demandMean[i], tail, minInventory, maxInventory, maxQuantity, stdParameter );
								
			/**sS*
			sSsolution sSsolution = sS.sS.solveInstance(instance, true, Normal);	//with initial order 
			System.out.println("Optimal cost under (s,S) policy is: "+sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0]);
*/
			
			
		//**********************sQt for Poisson Demand*************************
		long timeStartsQt = System.currentTimeMillis();
		//double[] sQtschedule = sQTminlp_oneRun.sQTminlpSchedule(demandMean[i], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, rangedQ);
		double[] sQtschedule = {86,63,149,254,170,418,276,158,90,169,161,267,177,358,202,78,39};
		System.out.println("Poisson Demand: optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));
		
		double[] s_sQt = minlp_Poisson.sQTminlp_heuristic.reorderPoint_sQtHeuristic(demandMean[i], fixedCost, unitCost, holdingCost, penaltyCost, initialStock, partitions, pace, sQtschedule, rangedQ);
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
		
		
		
		//**********************sQ for Poisson Demand*************************
		/*long timeStartsQ = System.currentTimeMillis();
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
		*/
		}
	}

}
