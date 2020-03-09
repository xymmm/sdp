package InstanceComputation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import minlp_Normal.sQTminlpNormal_heuristic;
import minlp_Normal.sQTminlpNormal_oneRun;
import minlp_Normal.sQminlpNormal_oneRun;
import minlp_Normal.sQminlpNormal_recursive;
import minlp_Normal.simNormalInstance;
import sS.sSsolution;
import sdp.data.InstanceDouble;

public class computationAnalysis_2 {

	public static void main(String args[]) throws Exception{

		double holdingCost = 1;

		double[] fixedOrderingCost = {500, 1000, 1500};
		double[] unitCost		   = {0,1};
		double[] penaltyCost	   = {5, 10, 20};
		double[] stdParameter	   = {0.1, 0.2, 0.3};

		double initialStock = 0;

		double tail = 0.00000001;

		int minInventory = -2000;
		int maxInventory = 2000;
		int maxQuantity = 500;

		boolean Normal = true;

		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;

		double pace = 100;
		int count = 100000;

		double[][] demandMean = {
				{71,92,115,138,159,175,186,190,186,175,159,138,115,92,71,53,38,26,17,11}
				//{86,103,120,136,150,161,168,170,168,161,150,136,120,103,86,70,55,42,32,23},
				//{10,36,88,136,149,121,68,22,11,42,96,140,148,114,60,18,14,50,104,144},
				//{70,81,103,124,130,118,95,75,71,84,107,126,129,115,91,73,72,87,110,127},
				//{100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100},
				//{165,47,100,62,31,43,199,172,96,69,8,29,135,97,70,248,57,11,94,13},
				//{489,446,248,281,363,155,293,220,93,107,234,124,184,223,101,123,99,31,82,0.1},
				//{287,508,391,754,694,261,195,320,111,191,160,55,84,58,0.1,0.1,0.1,0.1,0.1,0.1},
				//{198,74,183,204,114,165,318,119,482,534,136,260,299,76,218,323,102,174,284,0.1},
				//{224,223,517,291,547,646,224,215,440,116,185,211,26,55,0.1,0.1,0.1,0.1,0.1,0.1}
		};

		double[][][][][][] Results = new double[stdParameter.length][fixedOrderingCost.length][penaltyCost.length][unitCost.length][demandMean.length][3];

		for(int s = 0; s<stdParameter.length; s++) {//3
			System.out.println("stdParameter = "+stdParameter[s]);
			for(int f=0; f<fixedOrderingCost.length; f++) {//3
				System.out.println("fixed cost = "+fixedOrderingCost[f]);
				for(int p=0; p<penaltyCost.length; p++) {//3
					System.out.println("penalty cost = "+penaltyCost[p]);
					for(int u=0; u<unitCost.length; u++) {//2
						System.out.println("unit cost = "+unitCost[u]);
						for(int d=0; d<demandMean.length; d++) {
							System.out.println("=====================================================");
							//============================== sS ==========================
							InstanceDouble instance = new InstanceDouble(
									fixedOrderingCost[f],
									unitCost[u],
									holdingCost,
									penaltyCost[p],
									demandMean[d],
									tail,
									minInventory,
									maxInventory,
									maxQuantity,
									stdParameter[s]
									);

							sSsolution sSsolution = sS.sS.solveInstance(instance, true, Normal);
							Results[s][f][p][u][d][0] = sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0];
							System.out.println("optimal sS cost = "+Results[s][f][p][u][d][0]);

							//============================== sQt minlp ==========================
							double[] sQtschedule = sQTminlpNormal_oneRun.sQTminlpSchedule(
									demandMean[d], fixedOrderingCost[f], unitCost[u], holdingCost, penaltyCost[p],
									initialStock, stdParameter[s], 
									partitions, piecewiseProb, means, error);
							double[] s_sQt = sQTminlpNormal_heuristic.reorderPoint_sQtHeuristic(
									demandMean[d], fixedOrderingCost[f], unitCost[u], holdingCost, penaltyCost[p], 
									initialStock, stdParameter[s], partitions, piecewiseProb, means, error, pace, sQtschedule);

							//System.out.println("optimal schedule for sQt policy is "+Arrays.toString(sQtschedule));
							//System.out.println("Associated reorder poing is "+Arrays.toString(s_sQt));


							simNormalInstance normalInstance_sQt = new simNormalInstance(
									demandMean[d], 
									fixedOrderingCost[f],
									unitCost[u],
									holdingCost, 
									penaltyCost[p], 
									initialStock, 
									stdParameter[s], 
									sQtschedule, 
									s_sQt				
									);

							minlp_Normal.simulationNormalMINLP.simulationNormalMINLPmultipleRuns(normalInstance_sQt, count);
							normalInstance_sQt.statCost.setConfidenceIntervalStudent();
							Results[s][f][p][u][d][1] = normalInstance_sQt.statCost.average();
							System.out.println("Simulation cost = "+Results[s][f][p][u][d][1]);

							//============================== sQ minlp ==========================
							//schedule
							double[] sQschedule = sQminlpNormal_oneRun.sQminlpSchedule(
									demandMean[d], fixedOrderingCost[f], unitCost[u], holdingCost, penaltyCost[p],
									initialStock, stdParameter[s], partitions, piecewiseProb, means, error);		
							//reorder point
							double[] s_sQ = sQminlpNormal_recursive.reorderPoint_sQheuristic(
									demandMean[d], fixedOrderingCost[f], unitCost[u], holdingCost, penaltyCost[p], 
									initialStock, stdParameter[s],
									partitions, piecewiseProb, means, error, pace, sQschedule);
							//System.out.println("optimal schedule for sQ policy is "+Arrays.toString(sQschedule));
							//System.out.println("Associated reorder point is "+Arrays.toString(s_sQ));

							simNormalInstance normalInstance_sQ = new simNormalInstance(
									demandMean[d], 
									fixedOrderingCost[f],
									unitCost[u],
									holdingCost, 
									penaltyCost[p], 
									initialStock, 
									stdParameter[s], 
									sQschedule, 
									s_sQ				
									);

							minlp_Normal.simulationNormalMINLP.simulationNormalMINLPmultipleRuns(normalInstance_sQ, count);
							normalInstance_sQ.statCost.setConfidenceIntervalStudent();
							Results[s][f][p][u][d][2] = normalInstance_sQ.statCost.average();
							System.out.println("Simulation cost = "+Results[s][f][p][u][d][2]);
							System.out.println();
						}
					}
				}
			}
		}

		System.out.println();
		//============================Output the results===========================================
		//=================For each pattern=================
		int N_pattern = stdParameter.length*fixedOrderingCost.length*penaltyCost.length*unitCost.length;
		double[][][] patternResults = new double[demandMean.length][2][N_pattern];
		for(int i=0; i<demandMean.length; i++) {
			int flag = 0;
			do {
				for(int s = 0; s<stdParameter.length; s++) {
					for(int f=0; f<fixedOrderingCost.length; f++) {
						for(int p=0; p<penaltyCost.length; p++) {
							for(int u=0; u<unitCost.length; u++) {
								patternResults[i][0][flag] = (Results[s][f][p][u][i][1] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								patternResults[i][1][flag] = (Results[s][f][p][u][i][2] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								flag++;
							}
						}
					}
				}
				
			}while(flag < N_pattern);
			//System.out.println(Arrays.deepToString(patternResults[i]));
		}
		double[][] pivotPattern = new double[demandMean.length][2];
		for(int i=0; i<demandMean.length; i++) {
			pivotPattern[i][0] = sdp.util.sum.average(patternResults[i][0]);
			pivotPattern[i][1] = sdp.util.sum.average(patternResults[i][1]);
		}
		System.out.println("===pivot pattern===");
		System.out.println(Arrays.deepToString(pivotPattern));
		System.out.println();


		//=====================For each stdParameter=============================
		int N_std = demandMean.length*fixedOrderingCost.length*penaltyCost.length*unitCost.length;
		double[][][] stdResults = new double[stdParameter.length][2][N_std];
		for(int s=0; s<stdParameter.length; s++) {
			int flag = 0;
			do {
				for(int i = 0; i<demandMean.length; i++) {
					for(int f=0; f<fixedOrderingCost.length; f++) {
						for(int p=0; p<penaltyCost.length; p++) {
							for(int u=0; u<unitCost.length; u++) {
								stdResults[s][0][flag] = (Results[s][f][p][u][i][1] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								stdResults[s][1][flag] = (Results[s][f][p][u][i][2] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								flag++;
							}
						}
					}
				}
				
			}while(flag < N_std);
			//System.out.println(Arrays.deepToString(stdResults[s]));
		}
		double[][] pivotStd = new double[stdParameter.length][2];
		for(int s=0; s<stdParameter.length; s++) {
			pivotStd[s][0] = sdp.util.sum.average(stdResults[s][0]);
			pivotStd[s][1] = sdp.util.sum.average(stdResults[s][1]);
		}
		System.out.println("===pivot std parameter===");
		System.out.println(Arrays.deepToString(pivotStd));
		System.out.println();
		
		//===========================For each fixed ordering cost============================
		int N_K = demandMean.length*stdParameter.length*penaltyCost.length*unitCost.length;
		double[][][] kResults = new double[fixedOrderingCost.length][2][N_K];
		for(int f=0; f<fixedOrderingCost.length; f++) {
			int flag = 0;
			do {
				for(int i = 0; i<demandMean.length; i++) {
					for(int s=0; s<stdParameter.length; s++) {
						for(int p=0; p<penaltyCost.length; p++) {
							for(int u=0; u<unitCost.length; u++) {
								kResults[f][0][flag] = (Results[s][f][p][u][i][1] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								kResults[f][1][flag] = (Results[s][f][p][u][i][2] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								flag++;
							}
						}
					}
				}
				
			}while(flag < N_K);
			//System.out.println(Arrays.deepToString(kResults[f]));
		}
		double[][] pivotK = new double[fixedOrderingCost.length][2];
		for(int f=0; f<fixedOrderingCost.length; f++) {
			pivotK[f][0] = sdp.util.sum.average(kResults[f][0]);
			pivotK[f][1] = sdp.util.sum.average(kResults[f][1]);
		}
		System.out.println("===pivot fixed ordering cost===");
		System.out.println(Arrays.deepToString(pivotK));
		System.out.println();
		
		//===========================For each unit Cost============================
		int N_U = demandMean.length*stdParameter.length*penaltyCost.length*fixedOrderingCost.length;
		double[][][] uResults = new double[unitCost.length][2][N_U];
		for(int u=0; u<unitCost.length; u++) {
			int flag = 0;
			do {
				for(int i = 0; i<demandMean.length; i++) {
					for(int s=0; s<stdParameter.length; s++) {
						for(int p=0; p<penaltyCost.length; p++) {
							for(int f=0; f<fixedOrderingCost.length; f++) {
								uResults[u][0][flag] = (Results[s][f][p][u][i][1] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								uResults[u][1][flag] = (Results[s][f][p][u][i][2] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								flag++;
							}
						}
					}
				}
				
			}while(flag < N_U);
			//System.out.println(Arrays.deepToString(uResults[u]));
		}
		double[][] pivotU = new double[unitCost.length][2];
		for(int u=0; u<unitCost.length; u++) {
			pivotU[u][0] = sdp.util.sum.average(uResults[u][0]);
			pivotU[u][1] = sdp.util.sum.average(uResults[u][1]);
		}
		System.out.println("===pivot unit cost===");
		System.out.println(Arrays.deepToString(pivotU));
		System.out.println();

		//===========================For each penalty Cost============================
		int N_P = demandMean.length*stdParameter.length*unitCost.length*fixedOrderingCost.length;
		double[][][] pResults = new double[penaltyCost.length][2][N_P];
		for(int p=0; p<penaltyCost.length; p++) {
			int flag = 0;
			do {
				for(int i = 0; i<demandMean.length; i++) {
					for(int s=0; s<stdParameter.length; s++) {
						for(int u=0; u<unitCost.length; u++) {
							for(int f=0; f<fixedOrderingCost.length; f++) {
								pResults[p][0][flag] = (Results[s][f][p][u][i][1] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								pResults[p][1][flag] = (Results[s][f][p][u][i][2] - Results[s][f][p][u][i][0])/Results[s][f][p][u][i][0];
								flag++;
							}
						}
					}
				}
				
			}while(flag < N_P);
			//System.out.println(Arrays.deepToString(pResults[p]));
		}
		double[][] pivotP = new double[penaltyCost.length][2];
		for(int p=0; p<penaltyCost.length; p++) {
			pivotP[p][0] = sdp.util.sum.average(pResults[p][0]);
			pivotP[p][1] = sdp.util.sum.average(pResults[p][1]);
		}
		System.out.println("===pivot penalty cost===");
		System.out.println(Arrays.deepToString(pivotP));		
		
	}

}
