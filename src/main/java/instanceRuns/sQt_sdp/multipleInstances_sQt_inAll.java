package instanceRuns.sQt_sdp;

import ilog.concert.IloException;
import minlp.sQTminlp_oneRun;
import minlp.sQTminlp_recursive;
import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import sQt.generatedQ.sQtgeneratedQ_s;
import sQt.generatedQ.sQtgeneratedQ_s_solution;
import sQt.generatedQ.sQtgeneratedQlocalM;
import sQt.generatedQ.sQtgeneratedQlocalMsolution;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class multipleInstances_sQt_inAll {
	
	/*
	 * To integret code for sQt-sdp and sQt-minlp, only for 6-period instances
	 * 
	 * The aims and classes needed are all same as the original codes
	 * 
	 * */
	
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
					
					//sdp record parameters*******************************************************************************
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

					//minlp record parameters**********************************************************************************
					//start a new line and write up parameters
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");							//Q-minlp
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");				//time_Q
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");				//st-minlp
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");						//time st
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");						//sim cost
					//fixedCost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");				//Q-minlp
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");		//time_Q
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");		//st-minlp
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");				//time st
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");				//sim cost
					//penaltyCost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");				//Q-minlp
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");	//time_Q
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");		//st-minlp
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");			//time st
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");			//sim cost
					//unitCost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");					//Q-minlp
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");		//time_Q
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");		//st-minlp
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");				//time st
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");				//sim cost

					for(int d=0; d<demandMean.length; d++) {
						/*================================================================================================*/
						/*sdp*/
						/*================================================================================================*/
						//create instance
						Instance instance = new Instance(
								fixedCost[f], unitCost[u], holdingCost, penaltyCost[p], demandMean[d],
								0.00000001, minInventory[d], maxInventory[d], maxQuantity, 0.1);
						//solve sQt - sdp
						sQtgeneratedQlocalMsolution sQtsolution = sQtgeneratedQlocalM.sQtMultipleG(instance);
						int[] Q = new int[demandMean[d].length];
						for(int t=0; t<instance.getStages();t++) { Q[t] = (int)sQtsolution.optQ[t]; }
						//record
						sdp.util.writeText.writeLong(sQtsolution.timeConsumed_sQt, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");		//time for Q & cost
						sdp.util.writeText.writeDouble(sQtsolution.minCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");						//cost
						sdp.util.writeText.writeIntArray(Q, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");										//Q
						System.out.println("instance "+(d+1)+" sQt-sdp done");
						
						//solve sQ - s_t
						sQtgeneratedQ_s_solution sQtgivenQ = sQtgeneratedQ_s.costVaryingWithInventory(Q,instance,true);
						int[] s = sQtgivenQ.getsGivenQ(instance, sQtgivenQ);
						sdp.util.writeText.writeLong(sQtgivenQ.timeConsumed_sQtst, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t
						sdp.util.writeText.writeIntArray(s, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");							//s_t
						System.out.println("instance "+(d+1)+" st-sdp done");
						
						/*================================================================================================*/
						/*MINLP*/
						/*================================================================================================*/
						int[] inventory = new int[maxInventory[d] - minInventory[d] +1];
						for(int i=0; i<inventory.length;i++) {
							inventory[i] = i + minInventory[d];
						}
						/*1. sQt minlp solve Qt*************************************************************************************************************/
						long oneRunMINLPstartTime = System.currentTimeMillis();
						double[] Qt_minlp = new double [demandMean[d].length];
						try {
							sQTminlp_oneRun sQmodel = new sQTminlp_oneRun(
									demandMean[d], holdingCost, fixedCost[f], unitCost[u], penaltyCost[p],
									initialInventoryLevel, partitions, "sQtPoisson" );
							Qt_minlp = sQmodel.solveMINLP_recursive("sQtPoisson");
						}catch(IloException e){
							e.printStackTrace();
						}
						long oneRunMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeLong(
								oneRunMINLPendTime - oneRunMINLPstartTime, 
								"src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");										//time_solveMINLP
						sdp.util.writeText.writeDoubleArray(
								Qt_minlp, 
								"src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");													//Qarr
						System.out.println("instance "+(d+1)+" sQt-minlp done");
						
						/*2. sQt minlp recursive for s*************************************************************************************************************/
						int[] s_minlp = new int[demandMean[d].length];
						double[] cost_i = new double[maxInventory[d] - minInventory[d] +1];
						long reMINLPstartTime = System.currentTimeMillis();
						int[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean[d]);
						for(int t=0; t<demandMean[d].length; t++) {
							for(int i=0; i<inventory.length; i++) {
								try {
									sQTminlp_recursive sQmodel = new sQTminlp_recursive(
											demandMeanInput[t], holdingCost, fixedCost[f], unitCost[u], penaltyCost[p],
											inventory[i], partitions, "sQtPoisson_recursive" );
									double obj = sQmodel.solveMINLP_recursive("sQtPoisson_recursive");
									cost_i[i] = obj;
								}catch(IloException e){
									e.printStackTrace();
								}
							}//i for cost
							int globalMinimumIndex = sdp.util.globalMinimum.getGlobalMinimumJavaIndex(cost_i);
							double targetCost = cost_i[globalMinimumIndex] + fixedCost[f];
							for(int i=0; i<cost_i.length;i++) {
								if(cost_i[i]<targetCost) {
									s_minlp[t] = i + minInventory[d];
									break;
								}
							}//i for s
							
						}//t					
						long reMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeLong(
								reMINLPendTime - reMINLPstartTime, 
								"src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");
						sdp.util.writeText.writeIntArray(
								s_minlp, 
								"src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");
						System.out.println("instance "+(d+1)+" st-minlp done");
						
						/*3. simulation *************************************************************************************************************/
						double[] sd = new double[demandMean[d].length];
						for(int t=0; t<demandMean[d].length; t++) {
							sd[t] = (double) s_minlp[t];
						}
						sQsimInstanceDouble sQt_Poisson = new sQsimInstanceDouble(
								fixedCost[f], unitCost[u], holdingCost, penaltyCost[p], demandMean[d],
								0.00000001, minInventory[d], maxInventory[d], Qt_minlp, sd, 0.1 );
						Chrono timer = new Chrono();
						sQsimPoisson.sQsimPoissonMultiRuns(sQt_Poisson, 100000);
						sQt_Poisson.statCost.setConfidenceIntervalStudent();
						double cost_sim = sQt_Poisson.statCost.average();
						sdp.util.writeText.writeDouble(cost_sim, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt"); 			//cost_simulated
						System.out.println("instance "+(d+1)+" simulation done");
						
						
					}
					
					
				}
			}
		}
		
		
	}

}
