package instanceRuns.sQt_sdp;

import ilog.concert.IloException;
import minlp_Poisson.sQTminlp_oneRun;
import minlp_Poisson.sQTminlp_recursive;
import reorderQuantitySystem.sQsystemSolution;
import reorderQuantitySystem.simInstance;
import sQ.simulation.sQsimInstanceDouble;
import sQ.simulation.sQsimPoisson;
import sQt.generatedQ.sQtReorderPoint;
import sQt.generatedQ.sQtReorderPointSolution;
import sQt.generatedQ.sQt;
import sQt.generatedQ.sQtSolution;
import sdp.data.Instance;
import sdp.data.InstanceDouble;
import umontreal.ssj.util.Chrono;

public class multipleInstances_sQt_inAll {

	/*
	 * To integret code for sQt-sdp and sQt-minlp, only for 6-period instances
	 * 
	 * The aims and classes needed are all same as the original codes
	 * 
	 * */

	public static void multi_sQt(double[][] demandMean, double fixedCost, double penaltyCost, double unitCost, double holdingCost, 
			int minInventory, int maxInventory, int maxQuantity, int partitions, int initialInventoryLevel) {


		//record console for costDifference iterations
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/main/sQt_console.txt");								//console
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/main/sQt_console.txt");					//console
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/main/sQt_console.txt");				//console
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/main/sQt_console.txt");					//console


		//sdp record parameters*******************************************************************************
		//start a new line and write up parameters
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");							//cost
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");							//Q
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");				//s_t
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");				//time for Q and cost
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");						//time for s_t
		//fixed cost
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");			//cost
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");			//Q
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");	//s_t
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");	//time for Q and cost
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t
		//penalty cost
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");			//cost
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");			//Q
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");	//s_t
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");	//time for Q and cost
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");		//time for s_t
		//unit cost
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");				//cost
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");				//Q
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");	//s_t
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");	//time for Q and cost
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t

		/*     minlp record parameters*********************************************************************************
		//start a new line and write up parameters
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");							//Q-minlp
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");				//time_Q
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");				//st-minlp
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");						//time st
		sdp.util.writeText.writeNewLine("src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");						//sim cost
		//fixedCost
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");				//Q-minlp
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");		//time_Q
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");		//st-minlp
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");				//time st
		sdp.util.writeText.writeDouble(fixedCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");				//sim cost
		//penaltyCost
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");				//Q-minlp
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");	//time_Q
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");		//st-minlp
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");			//time st
		sdp.util.writeText.writeDouble(penaltyCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");			//sim cost
		//unitCost
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_Qarr.txt");					//Q-minlp
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_solveMINLP.txt");		//time_Q
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");		//st-minlp
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");				//time st
		sdp.util.writeText.writeDouble(unitCost, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt");				//sim cost
		 */

		for(int d=0; d<demandMean.length; d++) {
			/*================================================================================================*/
			/*sdp*/
			/*================================================================================================*/
			//create instance
			/*InstanceDouble instance = new InstanceDouble(fixedCost, unitCost, holdingCost, penaltyCost,
					demandMean[d], 0.00000001, minInventory, maxInventory, maxQuantity, 0.1 );
			long timesQtsingle = System.currentTimeMillis();
			sQsystemSolution sQtsolution = reorderQuantitySystem.optimalSchedule_sQt.optimalSchedule_sQt(instance);	
			int[] Q = sQtsolution.optimalSchedule;
			//record
			sdp.util.writeText.writeLong(System.currentTimeMillis() - timesQtsingle, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_QandCost.txt");		//time for Q & cost
			sdp.util.writeText.writeDouble(sQtsolution.optimalCost, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_cost.txt");						//cost
			sdp.util.writeText.writeIntArray(Q, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_Qarr.txt");										//Q
			System.out.println("instance "+(d+1)+" sQt-sdp done");

			//solve sQ - s_t
			long timesQtReorderPoint = System.currentTimeMillis();
			int[] reorderPointsQt = reorderQuantitySystem.reorderPoint.computeReorderPoint(instance, sQtsolution);
			sdp.util.writeText.writeLong(System.currentTimeMillis() - timesQtReorderPoint, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_time_st.txt");			//time for s_t
			sdp.util.writeText.writeIntArray(reorderPointsQt, "src/main/java/instanceRuns/sQt_sdp/sQt_sdp_reorderPoints.txt");							//s_t
			System.out.println("instance "+(d+1)+" st-sdp done");

			/*================================================================================================*/
			/*MINLP*/
			/*================================================================================================*/
			int[] inventory = new int[50 - -10 +1];
			for(int i=0; i<inventory.length;i++) {
				inventory[i] = i -10;//*****************
			}
			//1. sQt minlp solve Qt************************************************************************************************************
			long oneRunMINLPstartTime = System.currentTimeMillis();
			double[] Qt_minlp = new double [demandMean[d].length];
			try {
				sQTminlp_oneRun sQmodel = new sQTminlp_oneRun(
						demandMean[d], holdingCost, fixedCost, unitCost, penaltyCost,
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

			/*2. sQt minlp recursive for s***********************************/
			long reMINLPstartTime = System.currentTimeMillis();
			int[] s_minlp = new int[demandMean[d].length];
			for(int t=0; t<demandMean[d].length; t++) {
				if(Qt_minlp[t] == 0.0) {
					s_minlp[t] = -100;
				}else {
					double[] cost_i = new double[50 - -10 +1];
					double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean[d]);
						for(int i=0; i<inventory.length; i++) {
							try {
								sQTminlp_recursive sQmodel = new sQTminlp_recursive(
										demandMeanInput[t], holdingCost, fixedCost, unitCost, penaltyCost,
										inventory[i], partitions, "sQtPoisson_recursive" );
								double obj = sQmodel.solveMINLP_recursive("sQtPoisson_recursive");
								cost_i[i] = obj;
							}catch(IloException e){
								e.printStackTrace();
							}
							System.out.println(inventory[i]+"  "+cost_i[i]);
						}//i for cost
						int globalMinimumIndex = sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(cost_i);
						double targetCost = cost_i[globalMinimumIndex] + fixedCost;
						for(int i=0; i<cost_i.length;i++) {
							if(cost_i[i]<targetCost) {
								s_minlp[t] = inventory[i];
								break;
							}
						}
						
				}//else
			}
			long reMINLPendTime = System.currentTimeMillis();
			sdp.util.writeText.writeLong(
					reMINLPendTime - reMINLPstartTime, 
					"src/main/java/instanceRuns/sQt_minlp/sQt_minlp_time_s.txt");
			sdp.util.writeText.writeIntArray(
					s_minlp, 
					"src/main/java/instanceRuns/sQt_minlp/sQt_minlp_reorderPoints.txt");
			System.out.println("instance "+(d+1)+" st-minlp done");

			/*3. simulation *******************************************************************/

			simInstance sQsystem = new simInstance(
					fixedCost,
					unitCost,
					holdingCost,
					penaltyCost,
					demandMean[d],
					0.00000001,
					minInventory,
					maxInventory,
					0.25,
					s_minlp,
					Qt_minlp				
					);
			Chrono timer = new Chrono();

			reorderQuantitySystem.sQsystemSimulation.sQsimPoissonMultiRuns(sQsystem, 500000);
			sQsystem.statCost.setConfidenceIntervalStudent();
			double cost_sim = sQsystem.statCost.average();
			sdp.util.writeText.writeDouble(cost_sim, "src/main/java/instanceRuns/sQt_minlp/sQt_minlp_simCost.txt"); 			//cost_simulated
			System.out.println("instance "+(d+1)+" simulation done");


		}



	}

}
