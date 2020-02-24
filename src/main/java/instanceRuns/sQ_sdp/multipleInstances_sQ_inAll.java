package instanceRuns.sQ_sdp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ilog.concert.IloException;
import minlp_Poisson.sQminlpInstance;
import minlp_Poisson.sQminlp_oneRun;
import minlp_Poisson.sQminlp_recursive;
import reorderQuantitySystem.sQsystemSolution;
import sQ.sdp.sQ;
import sQ.sdp.sQreorderPoint;
import sQ.sdp.sQreorderPointSolution;
import sQ.sdp.sQsolution;
import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import sS.sS;
import sS.sSsolution;
import sdp.data.Instance;
import sdp.data.InstanceDouble;
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

	public static void multi_sQ (double[][] demandMean, double fixedCost, double penaltyCost, double unitCost, double holdingCost, 
			int minInventory, int maxInventory, int maxQuantity,
			int partitions, double initialInventoryLevel,
			boolean rangeQ,
			String fileConsole, 
			String fileSDPcost, String fileSDPQ, String fileTimeSDPcost, String fileSDPs,  String fileTimest,
			String fileMQ, String fileMtime, String fileMst, String fileMtimest, 
			String fileSimCost) throws Exception {

		//temporary record for st in sQ-minlp 
		File tempFile = new File("src/main/java/instanceRuns/sQ_minlp/temp.txt");

						//record console for costDifference iterations
						sdp.util.writeText.writeNewLine(fileConsole);								//console
						sdp.util.writeText.writeDouble(fixedCost, fileConsole);					//console
						sdp.util.writeText.writeDouble(penaltyCost, fileConsole);				//console
						sdp.util.writeText.writeDouble(unitCost, fileConsole);					//console 

						//sdp record parameters*******************************************************************************
						//start a new line and write up parameters
						sdp.util.writeText.writeNewLine(fileSDPcost);							//cost
						//sdp.util.writeText.writeNewLine(fileSDPQ);								//Q
						//sdp.util.writeText.writeNewLine(fileSDPs);								//s_t
						//sdp.util.writeText.writeNewLine(fileTimeSDPcost);						//time for Q and cost
						//sdp.util.writeText.writeNewLine(fileTimest);							//time for s_t
						//fixed cost
						sdp.util.writeText.writeDouble(fixedCost, fileSDPcost);				//cost
						//sdp.util.writeText.writeDouble(fixedCost[f], fileSDPQ);					//Q
						//sdp.util.writeText.writeDouble(fixedCost[f], fileSDPs);					//s_t
						//sdp.util.writeText.writeDouble(fixedCost[f], fileTimeSDPcost);			//time for Q and cost
						//sdp.util.writeText.writeDouble(fixedCost[f], fileTimest);				//time for s_t
						//penalty cost
						sdp.util.writeText.writeDouble(penaltyCost, fileSDPcost);			//cost
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileSDPQ);				//Q
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileSDPs);				//s_t
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileTimeSDPcost);		//time for Q and cost
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileTimest);				//time for s_t
						//unit cost
						sdp.util.writeText.writeDouble(unitCost, fileSDPcost);				//cost 				  "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileSDPQ);					//Q    				  "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileSDPs);					//s_t  				  "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileTimeSDPcost);			//time for Q and cost "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_QandCost.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileTimest);				//time for s_t		  "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time_st.txt"

						//minlp record parameters**********************************************************************************
						//start a new line and write up parameters
						//sdp.util.writeText.writeNewLine(fileMQ);							//Q-minlp
						//sdp.util.writeText.writeNewLine(fileMtime);							//time_Q
						//sdp.util.writeText.writeNewLine(fileMst);							//st-minlp
						//sdp.util.writeText.writeNewLine(fileMtimest);						//time st
						//sdp.util.writeText.writeNewLine(fileSimCost);						//sim cost
						//fixed cost
						//sdp.util.writeText.writeDouble(fixedCost[f], fileMQ);				//Q-minlp
						//sdp.util.writeText.writeDouble(fixedCost[f], fileMtime);			//time_Q
						//sdp.util.writeText.writeDouble(fixedCost[f], fileMst);				//st-minlp
						//sdp.util.writeText.writeDouble(fixedCost[f], fileMtimest);			//time st
						//sdp.util.writeText.writeDouble(fixedCost[f], fileSimCost);			//sim cost
						//penalty cost
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileMQ);				//Q-minlp
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileMtime);			//time_Q
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileMst);			//st-minlp
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileMtimest);		//time st
						//sdp.util.writeText.writeDouble(penaltyCost[p], fileSimCost);		//sim cost
						//unit cost
						//sdp.util.writeText.writeDouble(unitCost[u], fileMQ);				//Q-minlp  "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileMtime);				//time_Q   "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileMst);				//st-minlp "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileMtimest);			//time st  "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt"
						//sdp.util.writeText.writeDouble(unitCost[u], fileSimCost);			//sim cost "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt"
						
						for(int d=0; d<demandMean.length; d++) {
						/*================================================================================================*/
						/*sS*/
						/*================================================================================================
						InstanceDouble sSinstance = new InstanceDouble(
								fixedCost, unitCost, holdingCost,penaltyCost,demandMean[d],
								0.00000001, minInventory[d], maxInventory[d], maxQuantity[d], 0.1 );
						sSsolution solution = sS.solveInstance(sSinstance, true);
						//write cost
						sdp.util.writeText.writeDouble(solution.optimalCost[-sSinstance.minInventory][0], fileSDPcost);  
						//write time
						sdp.util.writeText.writeLong(solution.timeConsumed, fileSDPcost);*/

						/*================================================================================================*/
						/*sdp sQ*/ 
						/*================================================================================================*/

						//create instance
							InstanceDouble instance = new InstanceDouble(fixedCost, unitCost, holdingCost, penaltyCost,
									demandMean[d], 0.00000001, minInventory, maxInventory, maxQuantity, 0.1);
							long timesQsingle = System.currentTimeMillis();
						//solve sQ - sdp
							sQsystemSolution sQsolution = reorderQuantitySystem.optimalSchedule_sQ.optimalSchedule_sQ(instance);
							long timesQsingleEnd = System.currentTimeMillis();
						//record
						sdp.util.writeText.writeLong(timesQsingleEnd-timesQsingle,fileTimeSDPcost);	//time for Q & cost
						sdp.util.writeText.writeDouble(sQsolution.optimalCost, fileSDPcost); 		//cost
						int[] Q = sQsolution.optimalSchedule;
						sdp.util.writeText.writeIntArray(Q, fileSDPQ);								//Q
						System.out.println("instance "+(d+1)+" Q-sdp done.");

						//solve sQ - s_t
						long timeReorderPointSingle = System.currentTimeMillis();
						int[] s_sdp = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQsolution);
						long timeReorderPointSingleEnd = System.currentTimeMillis();
						sdp.util.writeText.writeLong(timeReorderPointSingleEnd - timeReorderPointSingle,fileTimest);//time for s_t
						sdp.util.writeText.writeIntArray(s_sdp, fileSDPs);											//s_t
						System.out.println("instance "+(d+1)+" st-sdp done.");

						/*================================================================================================*/
						/*MINLP*/
						/*================================================================================================*/
						/* 1. solve sQ-minlp oneRun for Q**************************************************************************************************/
						long oneRunMINLPstartTime = System.currentTimeMillis();
						double Q_minlp = Double.NaN;						
						try {
							sQminlp_oneRun sQmodel = new sQminlp_oneRun(
									demandMean[d], holdingCost, fixedCost, unitCost, penaltyCost,
									initialInventoryLevel, partitions, null);
							if(rangeQ == false) {
								Q_minlp = sQmodel.solveMINLP_oneRun("sQsinglePoisson");
							}else {
								Q_minlp = sQmodel.solveMINLP_oneRun("sQsinglePoisson_Qranged");	
							}

						}catch(IloException e){
							e.printStackTrace();
						}
						long oneRunMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeDouble(Q_minlp, fileMQ);		//Q-minlp
						sdp.util.writeText.writeLong(oneRunMINLPendTime - oneRunMINLPstartTime, fileMtime);//time-Q-minlp
						System.out.println("instance "+(d+1)+" Q-minlp done.");

						/*2. solve sQ-minlp recursive for st **********************************************************************************************/
						long reMINLPstartTime = System.currentTimeMillis();

						int Q_minlpInt = (int) Math.ceil(Q_minlp);
						double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean[d]);
						int[] s_minlp = new int[demandMean[d].length]; 
						for(int t=0; t<demandMean[d].length; t++) {
							sQminlpInstance sQminlpInstance = new sQminlpInstance(
									demandMeanInput[t], fixedCost, unitCost, holdingCost, penaltyCost, minInventory,maxInventory,
									partitions, s_sdp, Q_minlpInt);
							int i1 = s_sdp[t];
							double costDifference_s_sdp = 
									minlp_Poisson.sQminlp_recursive.costDifference(sQminlpInstance, s_sdp[t], 
											fileConsole, rangeQ);
							//s_minlp[t] = sQminlp_recursive.computeMINLP_s(costDifference_s_sdp, sQminlpInstance, i1, t, tempFile, fileConsole, rangeQ);
							s_minlp[t] = sQminlp_recursive.computeMINLP_s_bySlope(costDifference_s_sdp, sQminlpInstance, i1, tempFile, t, fileConsole, rangeQ);
							//Due to the unpublicarisation s in origian class, s is overwritten in a temporary file

							//s in the temporary file is now read and saved to the array: s_minlp
							FileReader fr = new FileReader(tempFile);
							BufferedReader br = new BufferedReader(fr);
							String read = "";
							read = br.readLine();
							s_minlp[t] = Integer.parseInt(read);
							//System.out.println("reorder point s_minlp["+(t+1)+"] in the loop = " + s_minlp[t]);
						}//save s and time
						sdp.util.writeText.writeIntArray(
								s_minlp, 
								fileMst);										//st-minlp
						long reMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeLong(
								reMINLPendTime-reMINLPstartTime, 
								fileMtimest);									//time-st-minlp
						System.out.println("instance "+(d+1)+" st-minlp done.");

						/*3. simulation ********************************************************************************************************/
						double[] actions = new double[demandMean[d].length];
						double[] sd = new double[demandMean[d].length];
						for(int t=0; t<demandMean[d].length; t++) {
							actions[t] = Q_minlpInt;
							sd[t] = s_minlp[t];
						}
						sQsimInstanceDouble sQ_Poisson = new sQsimInstanceDouble(
								fixedCost, unitCost, holdingCost,penaltyCost, demandMean[d],
								0.00000001, minInventory, maxInventory, actions, sd, 0.1);	
						Chrono timer = new Chrono();
						sQsimPoisson.sQsimPoissonMultiRuns(sQ_Poisson, 100000);
						sQ_Poisson.statCost.setConfidenceIntervalStudent();
						double cost_sim = sQ_Poisson.statCost.average();
						sdp.util.writeText.writeDouble(cost_sim, fileSimCost); 			//cost_simulated			
						System.out.println("instance "+(d+1)+" simulation done.");

						sdp.util.writeText.writeString("--------------------------- instance "+(d+0)+" done", fileSDPcost);


					}//d




	}

}
