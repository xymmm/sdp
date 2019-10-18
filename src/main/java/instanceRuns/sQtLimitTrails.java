package instanceRuns;

import sQt.generatedQ.sQtgeneratedSolution;
import sdp.data.InstanceDouble;
import sQt.generatedQ.*;

public class sQtLimitTrails {

	public static void main(String[] args) {
		
		/** cost parameters **/
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		
		double tail = 0.00000001;
		
		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 500;
		
		double stdParameter = 0.25;
		
		
		
		int[] demandMean8 = {20,40,60,40,20,40,60,40};
		int[] demandMean10 = {20,40,60,40,20,40,60,40,20,40};
		int[] demandMean16 = {20,40,60,40,20,40,60,40,20,40,60,40,20,40,60,40};
		int[] demandMean20 = {20,40,60,40,20,40,60,40,20,40,60,40,20,40,60,40,20,40,60,40};
		int[] demandMean25 = {20,40,60,40,20,40,60,40,20,40,60,40,20,40,60,40,20,40,60,40,20,40,60,40,20};
				
		InstanceDouble instance8 = new InstanceDouble(fixedOrderingCost,unitCost,holdingCost,penaltyCost,
				demandMean8,tail,minInventory,maxInventory,maxQuantity,stdParameter);
		InstanceDouble instance10 = new InstanceDouble(fixedOrderingCost,unitCost,holdingCost,penaltyCost,
				demandMean10,tail,minInventory,maxInventory,maxQuantity,stdParameter);
		InstanceDouble instance16 = new InstanceDouble(fixedOrderingCost,unitCost,holdingCost,penaltyCost,
				demandMean16,tail,minInventory,maxInventory,maxQuantity,stdParameter);
		InstanceDouble instance20 = new InstanceDouble(fixedOrderingCost,unitCost,holdingCost,penaltyCost,
				demandMean20,tail,minInventory,maxInventory,maxQuantity,stdParameter);
		InstanceDouble instance25 = new InstanceDouble(fixedOrderingCost,unitCost,holdingCost,penaltyCost,
				demandMean25,tail,minInventory,maxInventory,maxQuantity,stdParameter);
		
		sQtgeneratedSolution solution8 = sQtgenerated.sQtgeneratedSingle(instance8);
		System.out.println("===========================================================");
		
		sQtgeneratedSolution solution10 = sQtgenerated.sQtgeneratedSingle(instance10);
		System.out.println("===========================================================");

		sQtgeneratedSolution solution16 = sQtgenerated.sQtgeneratedSingle(instance16);
		System.out.println("===========================================================");

		sQtgeneratedSolution solution20 = sQtgenerated.sQtgeneratedSingle(instance20);
		System.out.println("===========================================================");

		sQtgeneratedSolution solution25 = sQtgenerated.sQtgeneratedSingle(instance25);
		System.out.println("===========================================================");


		
	}


	
}
