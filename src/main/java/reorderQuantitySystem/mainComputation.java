package reorderQuantitySystem;

import java.util.Arrays;

import sdp.data.Instance;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.util.Chrono;

public class mainComputation {
	
	public static void main(String args[]) {
		
		/** create instance**/
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		double tail = 0.00000001;

		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 500;

		double stdParameter = 0.25;

		int[] demandMean = {60, 60, 60, 60};
		//int[] demandMean = {2,4,6,4};
		
		Instance instance = new Instance(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
				demandMean, tail, minInventory, maxInventory, maxQuantity, stdParameter );
		
		long timeStart = System.currentTimeMillis();
		
		/**sQt*
		sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance);		
		System.out.println("Optimal cost under (s,Qt) policy is: "+sQtsolution.optimalCost);
		System.out.println("Optimal reordering schedule under (s,Qt) policy is: "+Arrays.toString(sQtsolution.optimalSchedule));
		int[] reorderPointsQt = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQtsolution);
		System.out.println("Reorder point = "+Arrays.toString(reorderPointsQt));
		long timeEndsQt = System.currentTimeMillis();
		System.out.println("Time consumed for (s,Qt) is "+(timeEndsQt - timeStart)/1000 + " s.");*/

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
