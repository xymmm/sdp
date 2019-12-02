package instanceRuns.sS_sdp;

import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;

public class multipleInstances_sS {
	
	/*
	 * Aim to resolve SDP for s,S policy as benchmarks: with I0 = 0
	 * 
	 * cost (initial order = 1)
	 * time, 
	 * 
	 * Class needed: (to compute)
	 * sQ.sdp.sS 		-> cost and time
	 * 
	 * where to record results:
	 * cost	-> "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt"
	 * time	-> "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt"
	 */
	
	public static void multi_sS(int[][] demandMean, double[] fixedCost, double[] penaltyCost, double[] unitCost, double holdingCost, 
								int[] minInventory, int maxInventory[], int maxQuantity[]) {		


		System.out.println("total number of instances = "+demandMean.length * fixedCost.length * penaltyCost.length * unitCost.length);
		System.out.println("number of parameter groups = "+fixedCost.length * penaltyCost.length * unitCost.length);
		System.out.println("==========================");
		
		int count = 1;			//to capture computation progress
		
		for(int f=0; f<fixedCost.length; f++) {
			for(int p=0; p<penaltyCost.length; p++) {
				for(int u=0; u<unitCost.length; u++) {
					
					//new parameter group
					System.out.println("parameter group "+count);
					long groupStartTime = System.currentTimeMillis();

					//write up parameters
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");					
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					
					for(int d=0; d<demandMean.length; d++) {
						
						Instance instance = new Instance(
								fixedCost[f], unitCost[u], holdingCost,penaltyCost[p],demandMean[d],
								0.00000001, minInventory[d], maxInventory[d], maxQuantity[d], 0.1 );
						sSsolution solution = sS.solveInstance(instance, true);
						//write cost
						sdp.util.writeText.writeDouble(solution.optimalCost[-instance.minInventory][0], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");  
						//write time
						sdp.util.writeText.writeLong(solution.timeConsumed, "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					}
					
					//group ends
					count++;
					long groupEndTime = System.currentTimeMillis();
					System.out.println("time Consumed for this group = "+(groupEndTime - groupStartTime)/1000+" s");
				}
			}
		}
	
		
		
	}

}
