package instanceRuns.main;

public class multi_sQ_6periods_main {

	public static void main(String args[]) throws Exception {
		/*demand - 6 periods*/
		double demandMean_6period [][] = {
				{1, 2, 4, 5, 6, 3},
				{4, 5, 6, 3 ,2, 1},
				{2.5, 1, 3.5, 4.5, 1.5, 3},
				{3.5, 1, 2, 4.5, 1, 3},
				{3, 3, 3, 3, 3, 3},
				{1.77, 1.31, 4.30, 3.81, 1.49, 4.54},
				{4, 7, 3.5, 2, 3, 1},
				{1, 2.5, 3.5, 4, 3, 2},
				{2.5, 1, 3.5, 4.5, 1.5, 5.5},
				{2, 1.5, 4, 4, 1.5, 2}
		};
		/* parameter - 10 periods*/
		double[][] costParameters = {
				{5,3},
				{10,5},
				{10,7}
		};
		double[] unitCost_6period = {0,1};//{0,1};
		double holdingCost_6period = 1;
		int minInventory_6period = -100;
		int maxInventory_6period = 100;
		int maxQuantity_6period = 9;
		int partitions = 10;
		int initialInventoryLevel = 0;

		//String filesScost = 		"./instanceResults/6periods/sS_sdp_cost.txt";
		//String filesStime = 		"./instanceResults/6periods/sS_sdp_time.txt";

		String fileConsole = 		"./instanceResults/6periods/sQ_console.txt"; 
		String fileSDPcost = 		"./instanceResults/6periods/sQ_sdp_cost.txt";
		String fileSDPQ = 			"./instanceResults/6periods/sQ_sdp_Q.txt";
		String fileTimeSDPcost = 	"./instanceResults/6periods/sQ_sdp_time_QandCost.txt"; 
		String fileSDPs = 			"./instanceResults/6periods/sQ_sdp_reorderPoints.txt";  
		String fileTimest = 		"./instanceResults/6periods/sQ_sdp_time_st.txt";

		String fileMQ = 			"./instanceResults/6periods/sQ_minlp_Q.txt";
		String fileMtime = 		"./instanceResults/6periods/sQ_minlp_time_solveMINLP.txt";
		String fileMst = 			"./instanceResults/6periods/sQ_minlp_reorderPoints.txt";
		String fileMtimest = 		"./instanceResults/6periods/sQ_minlp_time_s.txt";

		String fileSimCost = 		"./instanceResults/6periods/sQ_minlp_simCost.txt";

		for(int u=0; u<=1; u++) {
			for(int g=0; g<costParameters.length; g++) {
				//sS
				/*
				instanceRuns.sS_sdp.multipleInstances_sS.multi_sS(demandMean_6period, costParameters[g][0], costParameters[g][1], unitCost_6period[u], holdingCost_6period, 
				minInventory_6period, maxInventory_6period, maxQuantity_6period, 
				filesScost, filesStime);*/

				//sQ-sdp, sQ-minlp, sQ-simulation
				/*instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
						demandMean_6period, costParameters[g][0], costParameters[g][1], unitCost_6period[u], holdingCost_6period, 
						minInventory_6period, maxInventory_6period, maxQuantity_6period,
						partitions, initialInventoryLevel,
						true,
						fileConsole, fileSDPcost, fileSDPQ,  fileTimeSDPcost,  fileSDPs, fileTimest,
						fileMQ, fileMtime, fileMst, fileMtimest, 
						fileSimCost);*/
				//sQt-sdp, minlp, simulation are in multi-sQt_main.java
				instanceRuns.sQt_sdp.multipleInstances_sQt_inAll.multi_sQt(
						demandMean_6period, costParameters[g][0], costParameters[g][1], unitCost_6period[u], holdingCost_6period, 
						minInventory_6period, maxInventory_6period, maxQuantity_6period, 
						partitions, initialInventoryLevel);
			}

		}

	}





}
