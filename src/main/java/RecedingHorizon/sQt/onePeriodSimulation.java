package RecedingHorizon.sQt;

public class onePeriodSimulation {

		public static singleRHSimSolution singlSimulationRH(
				double Q, double reorderPoint, double demand, 
				double fixedOrderingCost, double holdingCost, double penaltyCost, double unitCost, double openingInventory,
				boolean print) {
			
			double cost = 0;
			double inventoryLevel = openingInventory;
			
			if (inventoryLevel < reorderPoint) {
				cost += fixedOrderingCost + unitCost * Q;
				inventoryLevel += Q;
				if(print) System.out.println("ordering Cost incurred, totalCost = "+cost);
			}else {
				if(print) System.out.println("no ordering cost.");
			}
			if(print) System.out.println("demand is "+demand);
			inventoryLevel = inventoryLevel - demand;
			double closingInventory = inventoryLevel;
			if(print) System.out.println("closing inventory is "+closingInventory);
			
			if(closingInventory >=0) {
				cost+= holdingCost * closingInventory;
			}else {
				cost+= -penaltyCost * closingInventory;
			}
			if(print) System.out.println("closing Cost  = "+cost);


			return new singleRHSimSolution(cost, closingInventory);
		}
	
}
