package instanceRuns.main;

public class multi_sQ_10periods1_6_main {

	public static void main(String args[]) throws Exception {
		
		/*demand - 10 periods*/
		int demandMean[][] = {
				{23		,42		,70		,103	,136	,161	,170	,161	,136	,103},
				{103	,136	,161	,170	,161	,136	,103	,70		,42		,23},
				{76		,27		,10		,36		,88		,68		,22		,11		,42		,96},
				{103	,85		,73		,98		,124	,130	,109	,80		,69		,78},
				{100	,100	,100	,100	,100	,100	,100	,100	,100	,100},
				{97		,129	,79		,173	,192	,39		,106	,60		,31		,56}
		};		
		/* parameter - 10 periods*/
		double[] fixedCost = {100,200,500};
		double[] penaltyCost = {5, 10, 20};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		int[] minInventory = {-2000, -2000, -2000, -2000, -2000, -2000};
		int[] maxInventory = {2000, 2000, 2000, 2000, 2000, 2000};
		int[] maxQuantity = {600, 600,  500, 500, 800, 800};
		int partitions = 10;
		double initialInventoryLevel = 0;
		
		 String filesScost = 		"./instanceResults/10periods_1_6/sS_sdp_cost.txt";
		 String filesStime = 		"./instanceResults/10periods_1_6/sS_sdp_time.txt";
		 
		 String fileConsole = 		"./instanceResults/10periods_1_6/sQ_console.txt"; 
		 String fileSDPcost = 		"./instanceResults/10periods_1_6/sQ_sdp_cost.txt";
		 String fileSDPQ = 			"./instanceResults/10periods_1_6/sQ_sdp_Q.txt";
		 String fileTimeSDPcost = 	"./instanceResults/10periods_1_6/sQ_sdp_time_QandCost.txt"; 
		 String fileSDPs = 			"./instanceResults/10periods_1_6/sQ_sdp_reorderPoints.txt";  
		 String fileTimest = 		"./instanceResults/10periods_1_6/sQ_sdp_time_st.txt";
		 
		 String fileMQ = 			"./instanceResults/10periods_1_6/sQ_minlp_Q.txt";
		 String fileMtime = 		"./instanceResults/10periods_1_6/sQ_minlp_time_solveMINLP.txt";
		 String fileMst = 			"./instanceResults/10periods_1_6/sQ_minlp_reorderPoints.txt";
		 String fileMtimest = 		"./instanceResults/10periods_1_6/sQ_minlp_time_s.txt";
		 
		 String fileSimCost = 		"./instanceResults/10periods_1_6/sQ_minlp_simCost.txt";

		
		//sS
		instanceRuns.sS_sdp.multipleInstances_sS.multi_sS(demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity, filesScost, filesStime);
		
		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity,
				partitions, initialInventoryLevel,
				false,
				fileConsole, fileSDPcost, fileSDPQ,  fileTimeSDPcost,  fileSDPs, fileTimest,
				fileMQ, fileMtime, fileMst, fileMtimest, 
				fileSimCost);
	}
		
	
}
