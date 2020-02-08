package reorderQuantitySystem;

import java.util.Arrays;

import sS.sSsolution;
import sdp.data.InstanceDouble;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.util.Chrono;

public class mainComputation {
	
	public static void main(String args[]) {
		
		/** create instance**/
		double fixedOrderingCost = 5;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 3;
		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		//int[] demandMean = {60, 60, 60, 60};
		//int[] demandMean = {2,4,6,4};
		double[] demandMean = {1, 2, 1.5, 3};
		
		InstanceDouble instance = new InstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
				demandMean, tail, minInventory, maxInventory, maxQuantity, stdParameter );
		
		long timeStart = System.currentTimeMillis();
		
		/**sS**/
		sSsolution sSsolution = sS.sS.solveInstance(instance, true);	//with initial order 
		System.out.println("Optimal cost under (s,Qt) policy is: "+sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0]);
		
		/**sQt**/
		System.out.println();
		sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance);		
		System.out.println("Optimal cost under (s,Qt) policy is: "+sQtsolution.optimalCost);
		System.out.println("Optimal reordering schedule under (s,Qt) policy is: "+Arrays.toString(sQtsolution.optimalSchedule));
		int[] reorderPointsQt = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQtsolution);
		System.out.println("Reorder point = "+Arrays.toString(reorderPointsQt));
		long timeEndsQt = System.currentTimeMillis();
		System.out.println("Time consumed for (s,Qt) is "+(timeEndsQt - timeStart)/1000 + " s.");

		/**sQ**/
		System.out.println();		
		sQsystemSolution sQsolution = reorderQuantitySystem.optimalSchedule_sQ.optimalSchedule_sQ(instance);		
		System.out.println("Optimal cost under (s,Q) policy is: "+sQsolution.optimalCost);
		System.out.println("Optimal reordering schedule under (s,Q) policy is: "+Arrays.toString(sQsolution.optimalSchedule));
		int[] reorderPointsQ = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQsolution);
		System.out.println("Reorder point = "+Arrays.toString(reorderPointsQ));
		
		long timeEndsQ = System.currentTimeMillis();
		System.out.println("Time consumed for (s,Q) is "+(timeEndsQ - timeStart)/1000 + " s.");
				
	}

}
