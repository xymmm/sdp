package instanceRuns.main.sQ10periodsPairs;

public class multi_sQ_10periods7_10_main {
	
	public static void main(String args[]) throws Exception {
		
		/*demand - 10 periods*/
		int demandMean[][] = {
				{53		,117	,62		,92		,111	,50		,61		,49		,15		,41},
				{195	,377	,347	,130	,97		,160	,55		,95		,80		,27},
				{145	,102	,57		,82		,159	,59		,241	,267	,68		,130},
				{119	,206	,92		,91		,238	,125	,253	,303	,92		,87},
		};		
		/* parameter - 10 periods*/
		double[] fixedCost = {100,200,500};
		double[] penaltyCost = {5, 10, 20};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		int[] minInventory = {-1500, -1500, -1500, -1500};
		int[] maxInventory = {1500, 1500, 1500, 1500};
		int[] maxQuantity = {500, 500, 500, 500};
		int partitions = 10;
		double initialInventoryLevel = 0;
		
		String fileConsole = "./instanceResults/sQinstance710console.txt"; 

		String fileRecord =  "./instanceResults/sQinstance710.txt";

				
		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity,
				partitions, initialInventoryLevel,
				false,
				fileConsole, 
				fileRecord, fileRecord,  fileRecord,  fileRecord, fileRecord,
				fileRecord, fileRecord, fileRecord, fileRecord, 
				fileRecord);
	}
		

}
