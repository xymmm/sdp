package instanceRuns.sQ_minlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ilog.concert.IloException;
import minlp.sQminlpInstance;
import minlp.sQminlp_oneRun;
import minlp.sQminlp_recursive;
import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimInstanceInt;
import sQ.simulation.sQsimPoisson;
import umontreal.ssj.util.Chrono;

public class multipleInstances_sQ_minlp {

	/*
	 * Aim to resolve MINLP for s,Q with single Q: with I0 = 0
	 * And simulation
	 * 
	 * Q-minlp, 
	 * s_t-minlp, 
	 * time_Q, 
	 * time_st,
	 * sQ_sim_cost.
	 * 
	 * Class needed: (to compute)
	 * minlp.sQminlp_oneRun			-> Q_minlp, time_Q
	 * minlp.sQminlp_recursive		-> st_minlp, time_st
	 * sQ.simulation.sQsimPoisson	->sQ_sim_cost
	 * 
	 * where to record results:
	 * Q_minlp:     "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_Q.txt"
	 * time_Q:      "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_solveMINLP.txt"
	 * st_mminlp:   "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt"
	 * time_st:     "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt"
	 * sQ_sim_cost: "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt"
	 */

	public static void main(String args[]) throws Exception {

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
		double minInventory = -150;
		double maxInventory = 150;
		double initialInventoryLevel = 0;
		int partitions = 10;
		int[][] s_sdp = {};*/

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
		double minInventory = -1500;
		double maxInventory = 1500;
		double initialInventoryLevel = 0;
		int partitions = 10;
		int[][] s_sdp = {};
		boolean rangedQ = false;
		
		/*classic instance
		int[][] demandMean = {{20,40,60,40}};
		double[] fixedCost = {100};
		double[] penaltyCost = {10};
		double[] unitCost = {0};
		double holdingCost = 1;
		double minInventory = -1500;
		double maxInventory = 1500;
		double initialInventoryLevel = 0;
		int partitions = 10;
		int[][] s_sdp = {{10, 32, 57, 20}};*/
		
		File file = new File("src/main/java/instanceRuns/sQ_minlp/temp.txt");

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
						/* 1. solve sQ-minlp oneRun for Q**************************************************************************************************/
						long oneRunMINLPstartTime = System.currentTimeMillis();
						double Q_minlp = Double.NaN;						
						try {
							sQminlp_oneRun sQmodel = new sQminlp_oneRun(
									demandMean[d],
									holdingCost,
									fixedCost[f],
									unitCost[u],
									penaltyCost[p],
									initialInventoryLevel,
									partitions,
									null
									);
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
						
						/*2. solve sQ-minlp recursive for st **********************************************************************************************/
						long reMINLPstartTime = System.currentTimeMillis();
						int Q_minlpInt = (int) Math.ceil(Q_minlp);
						int[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean[d]);
						int[] s_minlp = new int[demandMean[d].length];
						
						for(int t=0; t<demandMean[d].length; t++) {
							sQminlpInstance sQminlpInstance = new sQminlpInstance(
									demandMeanInput[t], fixedCost[f], unitCost[u], holdingCost, penaltyCost[p], 
									partitions, s_sdp[d], Q_minlpInt);
							int i1 = s_sdp[d][t];
							double costDifference_s_sdp = minlp.sQminlp_recursive.costDifference(sQminlpInstance, s_sdp[d][t], 
									"src/main/java/instanceRuns/main/sQ_console.txt", rangedQ);
							//s_minlp[t] = sQminlp_recursive.computeMINLP_s(costDifference_s_sdp, sQminlpInstance, i1, t, file);
							s_minlp[t] = sQminlp_recursive.computeMINLP_s_bySlope(costDifference_s_sdp, sQminlpInstance, i1, file, t, 
									"src/main/java/instanceRuns/main/sQ_console.txt", rangedQ);
							//Due to the unpublicarisation s in origian class, s is overwritten in a temporary file
							
							//s in the temporary file is now read and saved to the array: s_minlp
					        FileReader fr = new FileReader("src/main/java/instanceRuns/sQ_minlp/temp.txt");
					        BufferedReader br = new BufferedReader(fr);
					        String read = "";
					        read = br.readLine();
					        s_minlp[t] = Integer.parseInt(read);
					        System.out.println("reorder point s_minlp["+(t+1)+"] in the loop = " + s_minlp[t]);
						}//save s and time
						sdp.util.writeText.writeIntArray(
								s_minlp, 
								"src/main/java/instanceRuns/sQ_minlp/sQ_minlp_reorderPoints.txt");										//st-minlp
						long reMINLPendTime = System.currentTimeMillis();
						sdp.util.writeText.writeLong(
								reMINLPendTime-reMINLPstartTime, 
								"src/main/java/instanceRuns/sQ_minlp/sQ_minlp_time_s.txt");												//time-st-minlp
						
						/*3. simulation ********************************************************************************************************/
						double[] actions = new double[demandMean[d].length];
						double[] sd = new double[demandMean[d].length];
						for(int t=0; t<demandMean[d].length; t++) {
							actions[t] = Q_minlpInt;
							sd[t] = s_minlp[t];
						}
						sQsimInstanceDouble sQ_Poisson = new sQsimInstanceDouble(
								fixedCost[f], unitCost[u], holdingCost,penaltyCost[p], demandMean[d],
								0.00000001, minInventory, maxInventory, actions, sd, 0.1);	
						Chrono timer = new Chrono();
						sQsimPoisson.sQsimPoissonMultiRuns(sQ_Poisson, 100000);
						sQ_Poisson.statCost.setConfidenceIntervalStudent();
						double cost_sim = sQ_Poisson.statCost.average();
						sdp.util.writeText.writeDouble(cost_sim, "src/main/java/instanceRuns/sQ_minlp/sQ_minlp_simCost.txt"); 			//cost_simulated
						
					}//d

					//group ends
					count++;
					long groupEndTime = System.currentTimeMillis();
					System.out.println("time Consumed for this group = "+(groupEndTime - groupStartTime)/1000+" s");


				}//u
			}//p
		}//f
		
		
	}//main
	
	
}



