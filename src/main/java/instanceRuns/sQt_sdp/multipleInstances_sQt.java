package instanceRuns.sQt_sdp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import sQ.sdp.sQ;
import sQ.sdp.sQgivenQ;
import sQ.sdp.sQgivenQsolution;
import sQ.sdp.sQsolution;
import sQt.generatedQ.sQtgenerated;
import sQt.generatedQ.sQtgeneratedQ_s;
import sQt.generatedQ.sQtgeneratedQ_s_solution;
import sQt.generatedQ.sQtgeneratedQlocalM;
import sQt.generatedQ.sQtgeneratedQlocalMsolution;
import sQt.generatedQ.sQtgeneratedSolution;
import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class multipleInstances_sQt {

	/*
	 * Aim to resolve SDP for s,Q policy: with I0 = 0
	 * 
	 * cost (initial order = 1)
	 * Q, 
	 * s_t, 
	 * time_Q, 
	 * time_st.
	 * 
	 * Class needed: (to compute)
	 * sQ.sdp.sQ 		-> cost, Q and time_Q
	 * sQ.sdp.sQgivenQ	-> s_t and time_st
	 * 
	 * 
	 * where to record results:
	 * cost	-> "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt"
	 * Q	-> "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt"
	 * s_t	-> "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt"
	 * time	-> for Q and cost: "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt"
	 * 				  for s_t: "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt"
	 */
	
	public static void main(String[] args) {
			
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
		int minInventory = -150;
		int maxInventory = 150;
		int maxQuantity = 9;
		
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
					
					//start a new line and write up parameters
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");							//cost
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");							//Q
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");				//s_t
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");				//time for Q and cost
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");						//time for s_t
					//fixed cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");			//cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");			//Q
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");	//s_t
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");	//time for Q and cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t
					//penalty cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");			//cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");			//Q
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");	//s_t
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");	//time for Q and cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");		//time for s_t
					//unit cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");				//cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");				//Q
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");	//s_t
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");	//time for Q and cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t

					for(int d=0; d<demandMean.length; d++) {
						//create instance
						Instance instance = new Instance(
								fixedCost[f], unitCost[u], holdingCost, penaltyCost[p], demandMean[d],
								0.00000001, minInventory, maxInventory, maxQuantity, 0.1);
						//solve sQt - sdp
						sQtgeneratedQlocalMsolution sQtsolution = sQtgeneratedQlocalM.sQtMultipleG(instance);
						int[] Q = new int[demandMean[d].length];
						for(int t=0; t<instance.getStages();t++) { Q[t] = (int)sQtsolution.optQ[t]; }
						//record
						sdp.util.writeText.writeLong(sQtsolution.timeConsumed_sQt, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");		//time for Q & cost
						sdp.util.writeText.writeDouble(sQtsolution.minCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");						//cost
						sdp.util.writeText.writeIntArray(Q, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");										//Q
						
						//solve sQ - s_t
						sQtgeneratedQ_s_solution sQtgivenQ = sQtgeneratedQ_s.costVaryingWithInventory(Q,instance,true);
						int[] s = sQtgivenQ.getsGivenQ(instance, sQtgivenQ);
						sdp.util.writeText.writeLong(sQtgivenQ.timeConsumed_sQtst, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t
						sdp.util.writeText.writeIntArray(s, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");							//s_t
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
