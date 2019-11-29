package instanceRuns.main;

public class multi_sQt_main {

	public static void main(String args[]) {
		
		/*demand - 6 periods*/
		int demandMean [][] = {
				{10	,12	,10	,6	,5	,2	},
				{2	,5	,6	,10	,12	,10	},
				{15	,4	,10	,18	,4	,10	},
				{12	,7	,10	,13	,7	,12	},
				{5	,5	,5	,5	,5	,5	},
				{8	,10	,6	,1	,3	,7	},
				{16	,36	,16	,7	,15	,2	},
				{1	,8	,11	,22	,9	,13	},
				{5	,2	,7	,10	,3	,12	},
				{3	,3	,14	,14	,3	,2	}
		};
		/* parameter - 10 periods */
		double[] fixedCost = {5, 10, 20};
		double[] penaltyCost = {2,3};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		int[] minInventory = {-250, -250, -300, -300, -200, -200, -500, -500, -500, -500};
		int[] maxInventory = {250, 250, 300, 300, 200, 200, 500, 500, 500, 500};
		int maxQuantity = 9;
		int partitions = 10;
		int initialInventoryLevel = 0;
		
		instanceRuns.sQt_sdp.multipleInstances_sQt_inAll.multi_sQt(
				demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity, 
				partitions, initialInventoryLevel);
		
		
		
	}
	
}
