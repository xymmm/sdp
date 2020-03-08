package RecedingHorizon.sQt;

public class onePeriodSimulation {

		public static singleRHSimSolution singlSimulationRH(
				double Q, double reorderPoint, double demand, 
				double fixedOrderingCost, double holdingCost, double penaltyCost, double unitCost, double openingInventory) {
			
			double cost = 0;
			double inventoryLevel = openingInventory;
			
			if (inventoryLevel < reorderPoint) {
				cost += fixedOrderingCost + unitCost * Q;
				inventoryLevel += Q;
			}
			
			inventoryLevel = inventoryLevel - demand;
			double closingInventory = inventoryLevel;
			
			if(closingInventory >=0) {
				cost+= holdingCost * closingInventory;
			}else {
				cost+= -penaltyCost * closingInventory;
			}

			return new singleRHSimSolution(cost, closingInventory);
		}
	
}
