package instanceRuns.sS_sdp;

import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;

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
	
	public static void main(String args[]) {
		
		/*demand - 6 periods
		int demandMean [][] = {
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
		};*/
		/* parameter - 10 periods
		double[] fixedCost = {5, 10, 20};
		double[] unitCost = {0,1};
		double holdingCost = 1;
		double[] penaltyCost = {2,3};
		int minInventory = -150;
		int maxInventory = 150;
		int maxQuantity = 9;*/
		
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
		int minInventory = -1500;
		int maxInventory = 1500;
		int maxQuantity = 600;

		System.out.println("total number of instances = "+demandMean.length * fixedCost.length * penaltyCost.length * unitCost.length);
		System.out.println("number of parameter groups = "+fixedCost.length * penaltyCost.length * unitCost.length);
		System.out.println("==========================");
		
		int count = 1;			//to capture computation progress
		
		for(int f=0; f<fixedCost.length; f++) {
			for(int p=0; p<penaltyCost.length; p++) {
				for(int u=0; u<unitCost.length; u++) {
					
					//new parameter group
					System.out.println("parameter group "+count);
					long groupStartTime = System.currentTimeMillis();

					//write up parameters
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");					
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					
					for(int d=0; d<demandMean.length; d++) {
						
						Instance instance = new Instance(
								fixedCost[f], unitCost[u], holdingCost,penaltyCost[p],demandMean[d],
								0.00000001, minInventory, maxInventory, maxQuantity, 0.1 );
						sSsolution solution = sS.solveInstance(instance, true);
						//write cost
						sdp.util.writeText.writeDouble(solution.optimalCost[-instance.minInventory][0], "src/main/java/instanceRuns/sS_sdp/sS_sdp_cost.txt");  
						//write time
						sdp.util.writeText.writeLong(solution.timeConsumed, "src/main/java/instanceRuns/sS_sdp/sS_sdp_time.txt");
					}
					
					//group ends
					count++;
					long groupEndTime = System.currentTimeMillis();
					System.out.println("time Consumed for this group = "+(groupEndTime - groupStartTime)/1000+" s");
				}
			}
		}
	
		
		
	}

}
