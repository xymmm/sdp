package instanceRuns.main;

public class multi_sQ_main {

	public static void main(String args[]) throws Exception {
		
		/*demand - 10 periods*/
		int demandMean[][] = {
				{23		,42		,70		,103	,136	,161	,170	,161	,136	,103},
				{103	,136	,161	,170	,161	,136	,103	,70		,42		,23},
				{76		,27		,10		,36		,88		,68		,22		,11		,42		,96},
				{103	,85		,73		,98		,124	,130	,109	,80		,69		,78},
				{100	,100	,100	,100	,100	,100	,100	,100	,100	,100},
				{97		,129	,79		,173	,192	,39		,106	,60		,31		,56},
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
		int[] minInventory = {-2000, -2000, -2000, -2000, -2000, -2000, -3000, -5000, -5000, -5000};
		int[] maxInventory = {2000, 2000, 2000, 2000, 2000, 2000, 3000, 5000, 5000, 5000};
		int[] maxQuantity = {600, 600,  500, 500, 800, 800, 1500, 1500, 1500, 1500};
		int partitions = 10;
		double initialInventoryLevel = 0;
		
		//sS
		instanceRuns.sS_sdp.multipleInstances_sS.multi_sS(demandMean, fixedCost, penaltyCost, unitCost, holdingCost, minInventory, maxInventory, maxQuantity);
		
		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean, fixedCost, penaltyCost, unitCost, holdingCost, 
				minInventory, maxInventory, maxQuantity,
				partitions, initialInventoryLevel,
				false);
		
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

		//sS
		instanceRuns.sS_sdp.multipleInstances_sS.multi_sS(demandMean_6period, fixedCost_6period, penaltyCost_6period, unitCost_6period, holdingCost_6period, 
														  minInventory_6period, maxInventory_6period, maxQuantity_6period);
		
		//sQ-sdp, sQ-minlp, sQ-simulation
		instanceRuns.sQ_sdp.multipleInstances_sQ_inAll.multi_sQ (
				demandMean_6period, fixedCost_6period, penaltyCost_6period, unitCost_6period, holdingCost_6period, 
				minInventory_6period, maxInventory_6period, maxQuantity_6period,
				partitions, initialInventoryLevel,
				true);
		//sQt-sdp, minlp, simulation are in multi-sQt_main.java
	}
	
}
