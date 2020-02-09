package instanceRuns.main.sQ10periodsPairs;

public class multi_sQ_10periods_34_main {

	public static void main(String args[]) throws Exception {
		
		/*demand - 10 periods*/
		double demandMean[][] = {
				{76		,27		,10		,36		,88		,68		,22		,11		,42		,96},
				{103	,85		,73		,98		,124	,130	,109	,80		,69		,78}
		};		
		/* parameter - 10 periods*/
		double[] fixedCost = {100,200,500};
		double[] penaltyCost = {5, 10, 20};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		int[] minInventory = {-1500, -1500};
		int[] maxInventory = {1500, 1500};
		int[] maxQuantity = {500, 500};
		int partitions = 10;
		double initialInventoryLevel = 0;

		String fileConsole = "./instanceResults/sQinstance34console.txt"; 

		String fileRecord =  "./instanceResults/sQinstance34.txt";

		
		
		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity,
				partitions, initialInventoryLevel,
				false,
				fileConsole, 
				fileRecord, fileRecord,  fileRecord,  fileRecord, fileRecord,
				fileRecord, fileRecord,  fileRecord,  fileRecord, 
				fileRecord);
	}
		
	
}
