package instanceRuns.main;

public class multi_sQ_10periods7_10_main {
	
	public static void main(String args[]) throws Exception {
		
		/*demand - 10 periods*/
		int demandMean[][] = {
				{107	,234	,124	,184	,223	,101	,123	,99		,31		,82},
				{391	,754	,694	,261	,195	,320	,111	,191	,160	,55},
				{290	,204	,114	,165	,318	,119	,482	,534	,136	,260},
				{279	,453	,224	,223	,517	,291	,547	,646	,224	,215}
		};		
		/* parameter - 10 periods*/
		double[] fixedCost = {100,200,500};
		double[] penaltyCost = {5, 10, 20};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		int[] minInventory = {-3000, -5000, -5000, -5000};
		int[] maxInventory = {3000, 5000, 5000, 5000};
		int[] maxQuantity = {1500, 1500, 1500, 1500};
		int partitions = 10;
		double initialInventoryLevel = 0;
		
		 String filesScost = 		"./instanceResults/10periods_7_10/sS_sdp_cost.txt";
		 String filesStime = 		"./instanceResults/10periods_7_10/sS_sdp_time.txt";
		 
		 String fileConsole = 		"./instanceResults/10periods_7_10/sQ_console.txt"; 
		 String fileSDPcost = 		"./instanceResults/10periods_7_10/sQ_sdp_cost.txt";
		 String fileSDPQ = 			"./instanceResults/10periods_7_10/sQ_sdp_Q.txt";
		 String fileTimeSDPcost = 	"./instanceResults/10periods_7_10/sQ_sdp_time_QandCost.txt"; 
		 String fileSDPs = 			"./instanceResults/10periods_7_10/sQ_sdp_reorderPoints.txt";  
		 String fileTimest = 		"./instanceResults/10periods_7_10/sQ_sdp_time_st.txt";
		 
		 String fileMQ = 			"./instanceResults/10periods_7_10/sQ_minlp_Q.txt";
		 String fileMtime = 		"./instanceResults/10periods_7_10/sQ_minlp_time_solveMINLP.txt";
		 String fileMst = 			"./instanceResults/10periods_7_10/sQ_minlp_reorderPoints.txt";
		 String fileMtimest = 		"./instanceResults/10periods_7_10/sQ_minlp_time_s.txt";
		 
		 String fileSimCost = 		"./instanceResults/10periods_7_10/sQ_minlp_simCost.txt";

		
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
