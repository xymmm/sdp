package instanceRuns.main;

public class multi_sQ_6periods_main {

	public static void main(String args[]) throws Exception {
		/*demand - 6 periods*/
		int demandMean_6period [][] = {
				{10	,12	, 10	,6	,5	,2	},
				{2	,5	, 6		,10	,12	,10	},
				{15	,4	, 10	,18	,4	,10	},
				{12	,7	, 10	,13	,7	,12	},
				{5	,5	, 5		,5	,5	,5	},
				{8	,10	, 6		,1	,3	,7	},
				{16	,36	, 16	,7	,15	,2	},
				{1	,8	, 11	,22	,9	,13	},
				{5	,2	, 7		,10	,3	,12	},
				{3	,3	, 14	,14	,3	,2	}
		};
		/* parameter - 10 periods*/
		double[] fixedCost_6period = {5, 10, 20};
		double[] unitCost_6period = {0,1};
		double[] penaltyCost_6period = {2,3};
		double holdingCost_6period = 1;
		int[] minInventory_6period = {-250, -250, -300, -300, -200, -200, -500, -500, -500, -500};
		int[] maxInventory_6period = {250, 250, 300, 300, 200, 200, 500, 500, 500, 500};
		int[] maxQuantity_6period = {9, 9, 9, 9, 9, 9, 9, 9, 9, 9};
		int partitions = 10;
		double initialInventoryLevel = 0;
		
		 String filesScost = 		"./instanceResults/6periods/sS_sdp_cost.txt";
		 String filesStime = 		"./instanceResults/6periods/sS_sdp_time.txt";
		 
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

		//sS
		//instanceRuns.sS_sdp.multipleInstances_sS.multi_sS(demandMean_6period, fixedCost_6period, penaltyCost_6period, unitCost_6period, holdingCost_6period, 
				//minInventory_6period, maxInventory_6period, maxQuantity_6period, 
				//filesScost, filesStime);

		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean_6period, fixedCost_6period, penaltyCost_6period, unitCost_6period, holdingCost_6period, 
				minInventory_6period, maxInventory_6period, maxQuantity_6period,
				partitions, initialInventoryLevel,
				true,
				fileConsole, fileSDPcost, fileSDPQ,  fileTimeSDPcost,  fileSDPs, fileTimest,
				fileMQ, fileMtime, fileMst, fileMtimest, 
				fileSimCost);
		//sQt-sdp, minlp, simulation are in multi-sQt_main.java
	}

}
