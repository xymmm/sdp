package instanceRuns.sQ_sdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ilog.concert.IloException;
import minlp.sQminlpInstance;
import minlp.sQminlp_oneRun;
import minlp.sQminlp_recursive;
import sQ.sdp.sQ;
import sQ.sdp.sQgivenQ;
import sQ.sdp.sQgivenQsolution;
import sQ.sdp.sQsolution;
import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class multipleInstances_sQ_inAll {

	/*
	 * To integret code for sQ-sdp and sQ-minlp, 
	 * for the reason that sQ-minlp needs s-sdp as an initial input when computing s-minlp
	 * 
	 * The aims and classes needed are all same as the original codes
	 * 
	 * write console /sdp/src/main/java/instanceRuns/main/sQ_console.txt
	 * 
	 * */
	
	public static void multi_sQ (int[][] demandMean, double[] fixedCost, double[] penaltyCost, double[] unitCost, double holdingCost, 
								 int[] minInventory, int[] maxInventory, int[] maxQuantity,
								 int partitions, double initialInventoryLevel) throws Exception {
		
		System.out.println("total number of instances = "+demandMean.length * fixedCost.length * penaltyCost.length * unitCost.length);
		System.out.println("number of parameter groups = "+fixedCost.length * penaltyCost.length * unitCost.length);
		System.out.println("==========================");
		System.out.println();
		
		int count = 1;			//to capture computation progress
		
		//temporary record for st in sQ-minlp 
		File file = new File("src/main/java/instanceRuns/sQ_minlp/temp.txt");

		for(int f=0; f<fixedCost.length; f++) {
			for(int p=0; p<penaltyCost.length; p++) {
				for(int u=0; u<unitCost.length; u++) {
					
					//new parameter group
					System.out.println("------------------------------ parameter group "+count);
					long groupStartTime = System.currentTimeMillis();
					
					//record console for costDifference iterations
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/main/sQ_console.txt");								//console
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/main/sQ_console.txt");					//console
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/main/sQ_console.txt");				//console
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/main/sQ_console.txt");					//console

					//sdp record parameters*******************************************************************************
					//start a new line and write up parameters
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt");							//cost
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt");								//Q
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt");					//s_t
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_QandCost.txt");					//time for Q and cost
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_st.txt");						//time for s_t
					//fixed cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt");				//cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt");					//Q
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt");		//s_t
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_QandCost.txt");		//time for Q and cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_st.txt");			//time for s_t
					//penalty cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt");			//cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt");				//Q
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt");	//s_t
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_QandCost.txt");	//time for Q and cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_st.txt");			//time for s_t
					//unit cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt");				//cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt");					//Q
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt");		//s_t
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_QandCost.txt");		//time for Q and cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_st.txt");			//time for s_t

					//minlp record parameters**********************************************************************************
					//start a new line and write up parameters
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt");							//Q-minlp
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt");			//time_Q
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt");				//st-minlp
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt");						//time st
					sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt");					//sim cost
					//fixed cost
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt");					//Q-minlp
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt");	//time_Q
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt");		//st-minlp
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt");			//time st
					sdp.util.writeText.writeDouble(fixedCost[f], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt");			//sim cost
					//penalty cost
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt");				//Q-minlp
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt");	//time_Q
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt");	//st-minlp
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt");			//time st
					sdp.util.writeText.writeDouble(penaltyCost[p], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt");			//sim cost
					//unit cost
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt");					//Q-minlp
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt");	//time_Q
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt");		//st-minlp
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt");				//time st
					sdp.util.writeText.writeDouble(unitCost[u], "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt");			//sim cost

					for(int d=0; d<demandMean.length; d++) {
						/*================================================================================================*/
						/*sdp*/
						/*================================================================================================*/
						//create instance
						Instance instance = new Instance(
								fixedCost[f], unitCost[u], holdingCost,penaltyCost[p],demandMean[d],
								0.00000001, minInventory[d], maxInventory[d], maxQuantity[d], 0.1 );
						//solve sQ - sdp
						sQsolution sQsolution = sQ.solvesQInstance(instance);
						//record
						sdp.util.writeText.writeLong(
								sQsolution.timeConsumedsQ, 
								"src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_QandCost.txt");														//time for Q & cost
						sdp.util.writeText.writeDouble(
								sQsolution.totalCost[instance.initialInventory - instance.minInventory][sQsolution.getOpt_aSQ(instance)+1][0], 
								"src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt");																//cost
						int Q = sQsolution.getOpt_aSQ(instance)+1;
						sdp.util.writeText.writeInt(Q, "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt");											//Q
						System.out.println("instance "+(d+1)+" Q-sdp done.");
						
						//solve sQ - s_t
						sQgivenQsolution sQgivenQorder = sQgivenQ.costVaryingWithInventory(Q, instance, true);
						int[] s_sdp = sQgivenQsolution.getsGivenQ(instance, sQgivenQorder);
						sdp.util.writeText.writeLong(
								sQgivenQorder.timeConsumed, 
								"src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_st.txt");															//time for s_t
						sdp.util.writeText.writeIntArray(
								s_sdp, 
								"src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt");														//s_t
						System.out.println("instance "+(d+1)+" st-sdp done.");

						
						/*================================================================================================*/
						/*MINLP*/
						/*================================================================================================*/
						/* 1. solve sQ-minlp oneRun for Q**************************************************************************************************/
						long oneRunMINLPstartTime = System.currentTimeMillis();
						double Q_minlp = Double.NaN;						
						try {
							sQminlp_oneRun sQmodel = new sQminlp_oneRun(
									demandMean[d], holdingCost, fixedCost[f], unitCost[u], penaltyCost[p],
									initialInventoryLevel, partitions, null);
							Q_minlp = sQmodel.solveMINLP_oneRun("sQsinglePoisson");
						}catch(IloException e){
							e.printStackTrace();
						}
						long oneRunMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeInt(
								(int) Math.ceil(Q_minlp), 
								"src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt");													//Q-minlp
						sdp.util.writeText.writeLong(
								oneRunMINLPendTime - oneRunMINLPstartTime, 
								"src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt");									//time-Q-minlp
						System.out.println("instance "+(d+1)+" Q-minlp done.");

						
						/*2. solve sQ-minlp recursive for st **********************************************************************************************/
						long reMINLPstartTime = System.currentTimeMillis();
						int Q_minlpInt = (int) Math.ceil(Q_minlp);
						int[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean[d]);
						int[] s_minlp = new int[demandMean[d].length];
						
						for(int t=0; t<demandMean[d].length; t++) {
							sQminlpInstance sQminlpInstance = new sQminlpInstance(
									demandMeanInput[t], fixedCost[f], unitCost[u], holdingCost, penaltyCost[p], 
									partitions, s_sdp, Q_minlpInt);
							int i1 = s_sdp[t];
							double costDifference_s_sdp = minlp.sQminlp_recursive.costDifference(sQminlpInstance, s_sdp[t], "src/main/java/instanceRuns/main/sQ_console.txt");
							//s_minlp[t] = sQminlp_recursive.computeMINLP_s(costDifference_s_sdp, sQminlpInstance, i1, t, file, "src/main/java/instanceRuns/main/sQ_console.txt");
							s_minlp[t] = sQminlp_recursive.computeMINLP_s_bySlope(costDifference_s_sdp, sQminlpInstance, i1, file, t, "src/main/java/instanceRuns/main/sQ_console.txt");
							//Due to the unpublicarisation s in origian class, s is overwritten in a temporary file
							
							//s in the temporary file is now read and saved to the array: s_minlp
					        FileReader fr = new FileReader("src/main/java/instanceRuns/sQ_minlp/temp.txt");
					        BufferedReader br = new BufferedReader(fr);
					        String read = "";
					        read = br.readLine();
					        s_minlp[t] = Integer.parseInt(read);
					        //System.out.println("reorder point s_minlp["+(t+1)+"] in the loop = " + s_minlp[t]);
						}//save s and time
						sdp.util.writeText.writeIntArray(
								s_minlp, 
								"src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt");										//st-minlp
						long reMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeLong(
								reMINLPendTime-reMINLPstartTime, 
								"src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt");												//time-st-minlp
						System.out.println("instance "+(d+1)+" st-minlp done.");
						
						/*3. simulation ********************************************************************************************************/
						double[] actions = new double[demandMean[d].length];
						double[] sd = new double[demandMean[d].length];
						for(int t=0; t<demandMean[d].length; t++) {
							actions[t] = Q_minlpInt;
							sd[t] = s_minlp[t];
						}
						sQsimInstanceDouble sQ_Poisson = new sQsimInstanceDouble(
								fixedCost[f], unitCost[u], holdingCost,penaltyCost[p], demandMean[d],
								0.00000001, minInventory[d], maxInventory[d], actions, sd, 0.1);	
						Chrono timer = new Chrono();
						sQsimPoisson.sQsimPoissonMultiRuns(sQ_Poisson, 100000);
						sQ_Poisson.statCost.setConfidenceIntervalStudent();
						double cost_sim = sQ_Poisson.statCost.average();
						sdp.util.writeText.writeDouble(cost_sim, "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt"); 			//cost_simulated			
						System.out.println("instance "+(d+1)+" simulation done.");

					}//d
					
					
					//group ends
					count++;
					long groupEndTime = System.currentTimeMillis();
					System.out.println("------------------------------ time Consumed for this group = "+(groupEndTime - groupStartTime)/1000+" s");

					
				}//u
			}//p
		}//f
		
		
		
	}
	
}
