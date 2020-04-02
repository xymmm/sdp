package InstanceComputation;

import sQ.sdp.sQsolution;
import sS.sSsolution;
import sdp.data.InstanceDouble;

public class temp {

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
		int maxQuantity = 1000;

		boolean Normal = true;

		double[][] demandMean = {
				//{20, 40, 60, 40}
				//{11,17,26,38,53,71,92,115,138,159,175,186,190,186,175,159,138,115,92,71,53,38,26,17,11},
				//{23,32,42,55,70,86,103,120,136,150,161,168,170,168,161,150,136,120,103,86,70,55,42,32,23},
				//{130,150,127,76,27,10,36,88,136,149,121,68,22,11,42,96,140,148,114,60,18,14,50,104,144},
				//{122,130,120,98,77,70,81,103,124,130,118,95,75,71,84,107,126,129,115,91,73,72,87,110,127},
				//{100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100,100},
				//{178,178,136,211,119,165,47,100,62,31,43,199,172,96,69,8,29,135,97,70,248,57,11,94,13},
				//{2,51,152,467,268,489,446,248,281,363,155,293,220,93,107,234,124,184,223,101,123,99,31,82,0.1},
				{47,81,236,394,164,287,508,391,754,694,261,195,320,111,191,160,55,84,58,0.1,0.1,0.1,0.1,0.1,0.1},
				{44,116,264,144,146,198,74,183,204,114,165,318,119,482,534,136,260,299,76,218,323,102,174,284,0.1},
				{49,188,64,279,453,224,223,517,291,547,646,224,215,440,116,185,211,26,55,0.1,0.1,0.1,0.1,0.1,0.1}
		};



		for(int d=0; d<demandMean.length; d++) {
			System.out.println("=====================================================");
			sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/Normal25.txt");
			sdp.util.writeText.writeString("      (s,Q)-sdp for instance "+(d+8)+"      ", "src/main/java/InstanceComputation/Normal25.txt");
			sdp.util.writeText.writeNewLine("src/main/java/InstanceComputation/Normal25.txt");

			for(int s = 0; s<stdParameter.length; s++) {//3
				System.out.println("stdParameter = "+stdParameter[s]);
				for(int f=0; f<fixedOrderingCost.length; f++) {//3
					System.out.println("fixed cost = "+fixedOrderingCost[f]);
					for(int p=0; p<penaltyCost.length; p++) {//3
						System.out.println("penalty cost = "+penaltyCost[p]);
						for(int u=0; u<unitCost.length; u++) {//2
							System.out.println("unit cost = "+unitCost[u]);

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

							sQsolution sQsolution = sQ.sdp.sQ.solvesQInstance(instance, Normal);
							double costsQ = sQsolution.totalCost[sQsolution.getOpt_a(instance)+1][(int) (instance.initialInventory - instance.minInventory)][0];
							sdp.util.writeText.writeDouble(costsQ,
									"src/main/java/InstanceComputation/Normal25.txt");
							System.out.println(costsQ);
						}
					}
				}
			}
		}



	}


}
