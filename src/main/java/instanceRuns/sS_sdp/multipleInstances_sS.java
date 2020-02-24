package instanceRuns.sS_sdp;

import sS.sS;
import sS.sSsolution;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

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

	public static void multi_sS(double[][] demandMean, double fixedCost, double penaltyCost, double unitCost, double holdingCost, 
			int minInventory, int maxInventory, int maxQuantity,
			String filesScost, String filesStime) {		



		int count = 1;			//to capture computation progress

		for(int d=0; d<demandMean.length; d++) {
			


						//new parameter group
						System.out.println("parameter group "+count);
						long groupStartTime = System.currentTimeMillis();

						//write up parameters
						//sdp.util.writeText.writeNewLine(filesScost);
						sdp.util.writeText.writeNewLine(filesStime);					
						//sdp.util.writeText.writeDouble(fixedCost[f], filesScost);
						sdp.util.writeText.writeDouble(fixedCost, filesStime);
						//sdp.util.writeText.writeDouble(penaltyCost[p], filesScost);
						sdp.util.writeText.writeDouble(penaltyCost, filesStime);
						//sdp.util.writeText.writeDouble(unitCost[u], filesScost);
						sdp.util.writeText.writeDouble(unitCost, filesStime);



						InstanceDouble instance = new InstanceDouble(
								fixedCost, unitCost, holdingCost,penaltyCost,demandMean[d],
								0.00000001, minInventory, maxInventory, maxQuantity, 0.1 );
						sSsolution solution = sS.solveInstance(instance, true);
						
						//write cost
						sdp.util.writeText.writeDouble(solution.optimalCost[-instance.minInventory][0], filesScost);  
						//write time
						sdp.util.writeText.writeLong(solution.timeConsumed, filesStime);
						
						//group ends
						count++;
						long groupEndTime = System.currentTimeMillis();
						System.out.println("time Consumed for this group = "+(groupEndTime - groupStartTime)/1000+" s");

						

		}//d



	}

}
