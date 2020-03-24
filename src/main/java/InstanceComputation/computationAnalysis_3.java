package InstanceComputation;

import java.util.Arrays;

import RecedingHorizon.sQt.RH_sQt;
import sS.sSsolution;
import sdp.data.InstanceDouble;

public class computationAnalysis_3 {
	
	public static void main(String[] args) throws Exception {
				
		double holdingCost = 1;
		double unitCost	   = 0;

		double[] fixedOrderingCost = {500};	//250, 500, 1000, 2000	
		double[] penaltyCost	   = {2, 5, 10};//2, 5, 10
		double[] stdParameter	   = {0.1};//0.1 0.2 0.3

		double initialStock = 0;

		double tail = 0.00000001;

		int minInventory = -1500;
		int maxInventory = 1500;
		int maxQuantity = 500;

		boolean Normal = true;
		boolean print  = false;
		
		
		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;
		
/*		
		int partitions = 10;
		double[] piecewiseProb = {0.04206108420763477, 0.0836356495308449, 0.11074334596058821, 0.1276821455299152, 0.13587777477101692, 0.13587777477101692, 0.1276821455299152, 0.11074334596058821, 0.0836356495308449, 0.04206108420763477};
		double[] means = {-2.133986195498256, -1.3976822972668839, -0.918199946431143, -0.5265753462727588, -0.17199013069262026, 0.17199013069262026, 0.5265753462727588, 0.918199946431143, 1.3976822972668839, 2.133986195498256};
		double error = 0.005885974956458359;
*/
		double pace = 32;
		int count = 10;
		
		double[][] demandMean = {
				//{209,91,33,79,2,76,109,115,224,22,48,136,13,211,85,86,180,37,50,73,195,77,246,3}
				//{157,100,43,20,43,100,157,180,157,100,43,20,43,100,157,180,157,100,43,20,43,100,157,180},
				{121,100,79,70,79,100,121,130,121,100,79,70,79,100,121,130,121,100,79,70,79,100,121,130},
				//{14,21,32,46,63,84,106,130,152,172,186,194,194,186,172,152,130,106,84,63,46,32,21,14},
				//{27,36,48,61,77,93,110,126,141,153,162,166,166,162,153,141,126,110,93,77,61,48,36,27},
				//{100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100}
		};
		
		double[][][][][] RHresults = new double [demandMean.length][stdParameter.length][fixedOrderingCost.length][penaltyCost.length][count];
		double[][][][]	 sSresults = new double [demandMean.length][stdParameter.length][fixedOrderingCost.length][penaltyCost.length];
		
		for(int d=0; d<demandMean.length; d++) {
			
			System.out.println("=====================================================");
			sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/RHresults.txt");
			sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/RHresults_time.txt");
			//sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/RHresults_sS.txt");
			
			for(int s=0; s < stdParameter.length; s++) {
				System.out.println("std = "+stdParameter[s]);
				//sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/RHresults.txt");
				sdp.util.writeText.writeString("std = "+stdParameter[s], "src/main/java/InstanceComputation/RHresults.txt");
				sdp.util.writeText.writeString("std = "+stdParameter[s], "src/main/java/InstanceComputation/RHresults_time.txt");

				for(int f=0; f<fixedOrderingCost.length; f++) {
					System.out.println("K = "+fixedOrderingCost[f]);
					//sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/RHresults.txt");
					sdp.util.writeText.writeString("K = "+fixedOrderingCost[f], "src/main/java/InstanceComputation/RHresults.txt");
					sdp.util.writeText.writeString("K = "+fixedOrderingCost[f], "src/main/java/InstanceComputation/RHresults_time.txt");
					for(int p=0; p<penaltyCost.length; p++) {
						System.out.println("p = "+penaltyCost[p]);
						//sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/RHresults.txt");
						sdp.util.writeText.writeString("p = "+penaltyCost[p], "src/main/java/InstanceComputation/RHresults.txt");
						sdp.util.writeText.writeString("p = "+penaltyCost[p], "src/main/java/InstanceComputation/RHresults_time.txt");
						
						//============sS sdp
						/*InstanceDouble instance = new InstanceDouble(
								fixedOrderingCost[f], unitCost, holdingCost, penaltyCost[p], demandMean[d], tail,
								minInventory, maxInventory, maxQuantity, stdParameter[s]);

						sSsolution sSsolution = sS.sS.solveInstance(instance, true, Normal);
						sSresults[d][s][f][p] = sSsolution.optimalCost[(int) (instance.initialInventory - instance.minInventory)][0];
						sdp.util.writeText.writeDouble(sSresults[d][s][f][p], "src/main/java/InstanceComputation/RHresults_sS.txt");
						*/
						for(int c=0; c<count; c++) {
							long start = System.currentTimeMillis();
							//===========receding horizon of sQt
							RHresults[d][s][f][p][c] = RH_sQt.RHcomplete_sQt(demandMean[d], stdParameter[s], 
									 holdingCost, fixedOrderingCost[f], unitCost, penaltyCost[p], 
									 partitions, means, piecewiseProb, error,
									 initialStock, pace, 
									 print);
							System.out.println(RHresults[d][s][f][p][c]);
							sdp.util.writeText.writeDouble(RHresults[d][s][f][p][c], "src/main/java/InstanceComputation/RHresults.txt");
							long end = System.currentTimeMillis();
							sdp.util.writeText.writeDouble((end-start)/1000.0, "src/main/java/InstanceComputation/RHresults_time.txt");
						}
						
						
					}
				}
			}
		}
		

	}

}
