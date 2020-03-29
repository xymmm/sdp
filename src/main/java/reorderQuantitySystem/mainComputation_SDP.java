package reorderQuantitySystem;

import java.util.Arrays;

import sS.sSsolution;
import sdp.data.InstanceDouble;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.util.Chrono;

public class mainComputation_SDP {

	public static void main(String args[]) {

		/** create instance**/
		double holdingCost = 1;
		double tail = 0.00000001;

		double[] fixedOrderingCostA = {5};
		double[] unitCostA = {1};
		double[] penaltyCostA = {3};


		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		//double[] demandMean = {4, 5, 6, 3 ,2, 1};
		//double[] demandMean = {1.77, 1.31, 4.30, 3.81, 1.49, 4.54};
		//double[] demandMean = {20, 40, 60, 40};
		double demandMean [][] = {
				//{1, 2, 4, 5, 6, 3},
				{4, 5, 6, 3 ,2, 1},
				//{2.5, 1, 3.5, 4.5, 1.5, 3},
				//{3.5, 1, 2, 4.5, 1, 3},
				//{3, 3, 3, 3, 3, 3},
				//{1.77, 1.31, 4.30, 3.81, 1.49, 4.54},
				//{4, 7, 3.5, 2, 3, 1},
				//{1, 2.5, 3.5, 4, 3, 2},
				//{2.5, 1, 3.5, 4.5, 1.5, 5.5},
				//{2, 1.5, 4, 4, 1.5, 2}
		};

		boolean Normal = false;



		for(int u=0; u<unitCostA.length; u++) {
			double unitCost = unitCostA[u];
			for(int g=0; g<fixedOrderingCostA.length; g++) {
				double fixedOrderingCost = fixedOrderingCostA[g]; double penaltyCost = penaltyCostA[g];
				for(int i=0; i<demandMean.length; i++) {
					InstanceDouble instance = new InstanceDouble(fixedOrderingCost, unitCost, holdingCost, penaltyCost,
							demandMean[i], tail, minInventory, maxInventory, maxQuantity, stdParameter );

					/**sS**/
					sSsolution sSsolution = sS.sS.solveInstance(instance, true, Normal);	//with initial order 
					System.out.println("Optimal cost under (s,S) policy is: "+sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0]);

					/**sQt**/
					System.out.println();
					sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance, Normal);		
					System.out.println("Optimal cost under (s,Qt) policy is: "+sQtsolution.optimalCost);
					System.out.println("Optimal reordering schedule under (s,Qt) policy is: "+Arrays.toString(sQtsolution.optimalSchedule));
					int[] reorderPointsQt = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQtsolution);
					System.out.println("Reorder point = "+Arrays.toString(reorderPointsQt));
					long timeEndsQt = System.currentTimeMillis();
					//System.out.println("Time consumed for (s,Qt) is "+(timeEndsQt - timeStart)/1000 + " s.");


					/**sQ**/
					System.out.println();		
					sQsystemSolution sQsolution = reorderQuantitySystem.optimalSchedule_sQ.optimalSchedule_sQ(instance, Normal);		
					System.out.println("Optimal cost under (s,Q) policy is: "+sQsolution.optimalCost);
					System.out.println("Optimal reordering schedule under (s,Q) policy is: "+Arrays.toString(sQsolution.optimalSchedule));
					int[] reorderPointsQ = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQsolution);
					System.out.println("Reorder point = "+Arrays.toString(reorderPointsQ));


				}
			}
		}
	}

}
