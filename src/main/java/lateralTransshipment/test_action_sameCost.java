package lateralTransshipment;

public class test_action_sameCost {
	
	
	public static void main(String[] args) {
		int[] demandMean1 = {4};
		int[] demandMean2 = {4};
		int maxInventory  = 10;
		int minInventory  = -10;
		int maxQuantity   = 20;
		double K = 20;
		double z = 0;
		double[] R = {50};//{0, 1, 3, 5, 8, 10, 12, 14, 16, 18, 20, 25, 30, 35, 40, 50, 1000000};
		double v = 0;
		double h = 1;
		double b = 5; 
		double tail = 0.0001;
//		boolean noInitialTransship = false;
//		boolean noInitialOrder = true;
		boolean[] noInitialTransship = {false};//{false, true, true, false}; both actions, neither, no transship, no order
		boolean[] noInitialOrder 	 = {false};//{false, true, false, true};

		for(int i=0; i<noInitialTransship.length; i++) {
			for(int k=0; k<R.length; k++) {
				LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R[k],v,h,b,tail);

				long timeStart = System.currentTimeMillis();
				LTsolution solution = LT2Backwards2Stages.computeLTinstance2stages(instance, noInitialTransship[i], noInitialOrder[i]);
				long timeEnd = System.currentTimeMillis();
				System.out.println("time consumed for SDP = "+(timeEnd - timeStart)/1000 +"s");
				LT2locationsBackwards.writeSolution(solution, "src/main/java/lateralTransshipment/writeResults.txt");		

				LT2locationsBackwards.convertCostMatrix(instance, solution, 0, "src/main/java/lateralTransshipment/convertCostMatrix.txt");
//				LT2locationsBackwards.convertActionMatrix(instance, solution, 0, "src/main/java/lateralTransshipment/convertActionMatrix.txt");
//				LT2locationsBackwards.convertActionMatrix(instance, solution, 1, "src/main/java/lateralTransshipment/convertActionMatrixSecondPeriod.txt");

/*				for(int l=0; l<solution.inventoryPairs.length; l++) {
					System.out.print(Arrays.toString(solution.inventoryPairs[l])+"\t");
					for(int t=0; t<instance.demandMean1.length; t++) {
						System.out.print(solution.optimalCost[t][l]+"\t");
					}
					System.out.println();
				}
				*/
			}
		}
	}


}
