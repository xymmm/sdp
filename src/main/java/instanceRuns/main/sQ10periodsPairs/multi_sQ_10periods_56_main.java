package instanceRuns.main.sQ10periodsPairs;

public class multi_sQ_10periods_56_main {

	public static void main(String args[]) throws Exception {
		
		/*demand - 10 periods*/
		int demandMean[][] = {
				{100	,100	,100	,100	,100	,100	,100	,100	,100	,100},
				{97		,129	,79		,173	,192	,39		,106	,60		,31		,56}
		};		
		/* parameter - 10 periods*/
		double[] fixedCost = {200,500};
		double[] penaltyCost = {5,10,20};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		int[] minInventory = {-1500, -1500};
		int[] maxInventory = {1500, 1500};
		int[] maxQuantity = {500, 500};
		int partitions = 10;
		double initialInventoryLevel = 0;
		
		
		String fileConsole = "./instanceResults/sQinstance56console.txt"; 

		String fileRecord =  "./instanceResults/sQinstance56.txt";
		
		
		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity,
				partitions, initialInventoryLevel,
				false,
				fileConsole, 
				fileRecord, fileRecord, fileRecord, fileRecord, fileRecord,
				fileRecord, fileRecord, fileRecord, fileRecord, 
				fileRecord);
	}
		
	
}
