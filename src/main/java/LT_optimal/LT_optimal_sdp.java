package LT_optimal;

import java.util.Arrays;

import sS.sS;

public class LT_optimal_sdp {

	/** compute demand probability **/
	//sS.computeDemandProbability(double[] demandMean, int maxDemand, double tail)

	/** compute costs **/
	public static double computeLTcost(double Trans, double R, double r) {
		return (Math.abs(Trans) == 0)? 0: (Math.abs(Trans)*r + R);
	}	
	public static double computeClosingCost(int inventory, double h, double b) {
		return (inventory >=0)? (h*inventory) : (-b*inventory);
	}
	public static double computeOrderingCost(double Q, double K, double z) {
		return (Q>0)? (K + z*Q) : 0;
	}

	/** compute expected total cost and solve optimal actions **/
	public static LTsolution solveLT2locations(LT2locations instance) {

		int stages = instance.getStages();

		/************************** indexing inventory ***********************************************/
		int[] inventory = new int[instance.maxInventory - instance.minInventory + 1];
		for(int i=0; i<inventory.length; i++) {
			inventory[i] = i+instance.minInventory;			
		}

		/*************************** demand ***********************************************************/
		double demandProbabilities [][] = null;			//same demand distribution for two locations
		demandProbabilities = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);

		/*************************** build optimal action matrices ************************************/
		int[][][] LT = new int[stages][inventory.length][inventory.length];		//quantity of transshipment
		int[][][] Q1 = new int[stages][inventory.length][inventory.length];		//size of replenishment order for location 1
		int[][][] Q2 = new int[stages][inventory.length][inventory.length];		//size of replenishment order for location 2

		/*************************** build cost matrices **********************************************/
		//dynamically create dimensions of Trans*Q for cost
		double[][][][][] totalCost1 = null;		//[i][i][1 to 2][maxQ+1][maxQ+1], 3rd dimension is dependent on the domin of Trans, determined in every iteration
		double[][][][][] totalCost2 = null;		//[i][i][2 to 1][maxQ+1][maxQ+1], 3rd dimension is dependent on the domin of Trans, determined in every iteration
		double[][][][][] totalCost3 = new double [inventory.length][inventory.length][1][instance.maxQuantity+1][instance.maxQuantity+1];
		double[][][] optimalCost = new double[stages][inventory.length][inventory.length];		//optimal cost of the system, not for each location
		//optimal cost is not equivalent to the sum of two minimim costs		
		LTactionSolution action12 = new LTactionSolution(0, 0, 0, 0);
		LTactionSolution action21 = new LTactionSolution(0, 0, 0, 0);
		LTactionSolution action0  = new LTactionSolution(0, 0, 0, 0);


		/*************************** SDP **************************************************************/
		for(int t=stages-1; t>=0; t--) {
			for(int i1=0; i1<inventory.length; i1++) {
				for(int i2 = 0; i2<inventory.length; i2++) {
					System.out.println("i = (" + inventory[i1]+","+inventory[i2] + ")");

					/******* positive inventory at location 1 that can afford transship from 1 to 2 ***/
					if(inventory[i1]>0) {
						System.out.println("LT action 1 to 2");
						for(int l = 0; l<=inventory[i1]; l++) {
							double transCost = computeLTcost(l, instance.R, instance.r);
							totalCost1 = new double[inventory.length][inventory.length][inventory[i1]+1][instance.maxQuantity+1][instance.maxQuantity+1];
							//reorder
							for(int q1=0; q1<Q1.length; q1++) {		//not yet consider 0 initial order
								for(int q2=0; q2<Q2.length; q2++) { //not yet consider 0 initial order
									double scenarioProb1 = 0; double scenarioProb2 = 0;
									for(int d1=0; d1<demandProbabilities[t].length; d1++) {
										for(int d2 = 0; d2 < demandProbabilities[t].length; d2++) {
											if(
													(inventory[i1]-l+q1-d1 <= instance.maxInventory) && (inventory[i1]-l+q1-d1 >= instance.minInventory) &&
													(inventory[i2]+l+q2-d2 <= instance.maxInventory) && (inventory[i2]+l+q2-d2 >= instance.minInventory)
													){
												double orderCost = computeOrderingCost(q1, instance.K, instance.z) + computeOrderingCost(q2, instance.K, instance.z);
												double immediateCost = computeClosingCost(inventory[i1]-l+q1-d1, instance.h, instance.b)  
														+ computeClosingCost(inventory[i2]+l+q2-d2, instance.h, instance.b);
												//totalCost1 [i][i][1 to 2][maxQ+1][maxQ+1]
												int i1UpdateIndex = inventory[i1]-l+q1-d1 - instance.minInventory + 1;
												int i2UpdateIndex = inventory[i2]+l+q2-d2 - instance.minInventory + 1;
												totalCost1 [i1][i2][l][q1][q2] += demandProbabilities[t][d1]*
														demandProbabilities[t][d2]*
														(transCost + orderCost + immediateCost + (
																(t==0)? 0 : optimalCost[t+1][i1UpdateIndex][i2UpdateIndex])
																);
												scenarioProb1 += demandProbabilities[t][d1]; scenarioProb2 += demandProbabilities[t][d2];
											}else {
												
											}

										}//d1
									}//d2
									totalCost1 [i1][i2][l][q1][q2] = totalCost1[i1][i2][l][q1][q2]/(scenarioProb1 * scenarioProb2);
								}//q1
							}//q2

						}//iterating quantity of transshipment
						action12 = LTactionSolution.actionsIndex(totalCost1[i1][i2]);
						System.out.println(action12.minCost);
					}//end for transship from 1 to 2
					
					/******* positive inventory at location 2 that can afford transship from 2 to 1 ***/				
					if(inventory[i2]>0) {
						System.out.println("LT action 2 to 1");
						totalCost2 = new double[inventory.length][inventory.length][inventory[i2]+1][instance.maxQuantity+1][instance.maxQuantity+1];
						for(int l = 0; l<=inventory[i2]; l++) {
							double transCost = computeLTcost(l, instance.R, instance.r);							
							//reorder
							for(int q1=0; q1<Q1.length; q1++) {		//not yet consider 0 initial order
								for(int q2=0; q2<Q2.length; q2++) { //not yet consider 0 initial order
									double scenarioProb1 = 0; double scenarioProb2 = 0;
									for(int d1=0; d1<demandProbabilities[t].length; d1++) {
										for(int d2 = 0; d2 < demandProbabilities[t].length; d2++) {
											if(
													(inventory[i1]+l+q1-d1 <= instance.maxInventory) && (inventory[i1]+l+q1-d1 >= instance.minInventory) &&
													(inventory[i2]-l+q2-d2 <= instance.maxInventory) && (inventory[i2]-l+q2-d2 >= instance.minInventory)
													){
												double orderCost = computeOrderingCost(q1, instance.K, instance.z) + computeOrderingCost(q2, instance.K, instance.z);
												double immediateCost = computeClosingCost(inventory[i1]+l+q1-d1, instance.h, instance.b)  
														+ computeClosingCost(inventory[i2]-l+q2-d2, instance.h, instance.b);
												//totalCost1 [i][i][1 to 2][maxQ+1][maxQ+1]
												int i1UpdateIndex = inventory[i1]+l+q1-d1 - instance.minInventory + 1;
												int i2UpdateIndex = inventory[i2]-l+q2-d2 - instance.minInventory + 1;
												totalCost2 [i1][i2][l][q1][q2] += demandProbabilities[t][d1]*
														demandProbabilities[t][d2]*
														(transCost + orderCost + immediateCost + (
																(t==0)? 0 : optimalCost[t+1][i1UpdateIndex][i2UpdateIndex])
																);
												scenarioProb1 += demandProbabilities[t][d1]; scenarioProb2 += demandProbabilities[t][d2];
											}//inventory exceeded, else nothing to do

										}//d1
									}//d2
									totalCost2 [i1][i2][l][q1][q2] = totalCost2[i1][i2][l][q1][q2]/(scenarioProb1 * scenarioProb2);
								}//q1
							}//q2

						}//iterating quantity of transshipment
						action21 = LTactionSolution.actionsIndex(totalCost2[i1][i2]);
						System.out.println(action21.minCost);
					}//end for transship from 2 to 1

					/******************* both inventory levels are negative, not possible to transship *****/
					if( (inventory[i1] <= 0) && (inventory[i2] <= 0) ) {
						System.out.println("no LT action");
						double transCost = 0;	//no transshipping action
						System.out.println("transCost = "+transCost);
						for(int q1=0; q1<Q1.length; q1++) {		//not yet consider 0 initial order
							for(int q2=0; q2<Q2.length; q2++) { //not yet consider 0 initial order
								double scenarioProb1 = 0; double scenarioProb2 = 0;
								for(int d1=0; d1<demandProbabilities[t].length; d1++) {
									for(int d2 = 0; d2 < demandProbabilities[t].length; d2++) {
										if(
												(inventory[i1]+q1-d1 <= instance.maxInventory) && (inventory[i1]+q1-d1 >= instance.minInventory) &&
												(inventory[i2]+q2-d2 <= instance.maxInventory) && (inventory[i2]+q2-d2 >= instance.minInventory)
												){
											double orderCost = computeOrderingCost(q1, instance.K, instance.z) + computeOrderingCost(q2, instance.K, instance.z);
											double immediateCost = computeClosingCost(inventory[i1]+q1-d1, instance.h, instance.b)  
													+ computeClosingCost(inventory[i2]+q2-d2, instance.h, instance.b);
											//totalCost1 [i][i][1 to 2][maxQ+1][maxQ+1]
											int i1UpdateIndex = inventory[i1]+q1-d1 - instance.minInventory + 1;
											int i2UpdateIndex = inventory[i2]+q2-d2 - instance.minInventory + 1;
											totalCost3 [i1][i2][0][q1][q2] += demandProbabilities[t][d1]*
													demandProbabilities[t][d2]*
													(transCost + orderCost + immediateCost + (
															(t==0)? 0 : optimalCost[t+1][i1UpdateIndex][i2UpdateIndex])
															);
											scenarioProb1 += demandProbabilities[t][d1]; scenarioProb2 += demandProbabilities[t][d2];
										}//inventory exceeded, else nothing to do

									}//d1
								}//d2
								totalCost3 [i1][i2][0][q1][q2] = totalCost3[i1][i2][0][q1][q2]/(scenarioProb1 * scenarioProb2);
							}//q1
						}//q2
						action0  = LTactionSolution.actionsIndex(totalCost3[i1][i2]);
						System.out.println(action0.minCost);
					}//end no transshipment

					/*********************** determine the optimal cost and actions ************************/					
					optimalCost[t][i1][i2] = Math.min(Math.min(action12.minCost, action21.minCost), action0.minCost);
					if(optimalCost[t][i1][i2] == action12.minCost) {
						LT[t][i1][i2] = action12.LT; Q1[t][i1][i2] = action12.Q1; Q2[t][i1][i2] = action12.Q2;
					}else if(optimalCost[t][i1][i2] == action21.minCost) {
						LT[t][i1][i2] = action21.LT; Q1[t][i1][i2] = action21.Q1; Q2[t][i1][i2] = action21.Q2;
					}else {
						LT[t][i1][i2] = action0.LT;  Q1[t][i1][i2] = action0.Q1;  Q2[t][i1][i2] = action0.Q2;
					}

					//-----------------------------					
				}//i2
			}//i1
		}//for t

		return new LTsolution(inventory, LT, Q1, Q2, optimalCost);
	}


	public static void main(String[] args) {
		double K = 100;
		double z = 0;
		double h = 1;
		double b = 10;
		double R = 200;
		double r = 5;
		double[] demandMean = {2};
		int[] initialInventory = {1, 0};	   

		double tail = 0.00000001;

		int minInventory = -10;
		int maxInventory = 10;
		int maxQuantity = 5;

		LT2locations instance = new LT2locations(K, z, h, b, R, r, demandMean, tail, minInventory, maxInventory, maxQuantity, initialInventory);
		LTsolution solution = solveLT2locations(instance);

		//present cost
		//System.out.println(Arrays.deepToString(solution.optimalCost[0]));
		//System.out.println(Arrays.deepToString(solution.LTaction[0]));

	}

}
