package sQt.generatedQ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import sS.sdp.sS;
//Q \in {0,1,2,3,4,5,6,7,8,9}
import sdp.data.InstanceDouble;

public class sQtgenerated {

	public static void writeToText(int generator, double totalCost){
		/*
		try {
			File writename = new File("E:\\sQtGenerated.txt"); // relative path, if no file then create a new output.txt
			writename.createNewFile(); // create new file
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			out.write(generator+"       "+ totalCost+"\r\n"); // \r\n is enter
			out.flush(); // save soft drive
			out.close(); // close the file
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		 */
		FileWriter fw = null;
		try {
			File f = new File("\"./sQtGenerated.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.print(generator+"       "+ totalCost+"\r\n");
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** create an array, where each entry is a duoble number ranged in [0, 10^T-1].**/
	public static double[] createGenerator(InstanceDouble instance) {
		//generator = 0-9999
		double generator[] = new double[(int) (Math.pow(10, instance.getStages()))];
		for(int i=0; i<generator.length;i++) {
			generator[i] = i;
		}
		return generator;
	}

	/**summation of entries*10^t of an array before a given index**/
	public static double sumWithPower(double[] arr, int stopIndex) {//befor stopIndex, not include it
		double sum = 0;
		for(int i=0;i<stopIndex;i++) {
			sum = sum + arr[i]*Math.pow(10, i);
		}
		return sum;
	}
	/**reverse an array**/
	public static double[] reverseArray(double[] arr) {
		double[] arrNew = new double[arr.length];
		for(int i=0; i<arr.length;i++) {
			arrNew[i] = arr[arr.length-1-i];
		}
		return arrNew;
	}

	/**
	 * Given a value of generator ranged in [0,10^T-1], a series of Qt is created by modular operation.
	 * The assignment starts with the last period, and is completed backwards.
	 */
	public static double[] generateQ(int timeHorizon, double generator) {
		double[] Q = new double [timeHorizon];
		double[] divisor = new double [timeHorizon];
		for(int t=0; t<timeHorizon;t++) {
			divisor[t] = Math.pow(10, t+1);
		}
		for(int t=0; t<timeHorizon;t++) {	
			Q[t] = (generator % divisor[t] - ((t==0)? 0: sumWithPower(Q,t)))/Math.pow(10, t);
		}
		return reverseArray(Q);
	}


	public static sQtgeneratedSolution sQtgeneratedSingle(InstanceDouble instance) {

		//create inventory state space
		double[] inventory = new double[(int) (instance.maxInventory - instance.minInventory + 1)];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		//demand
		//double demandProbabilities [][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail); //normal
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson

		int nbCombinations = (int) Math.pow(10, instance.getStages());

		//totalCost[g][i][t] stores the values of costs for an inventory level at a stage with a given combination of generator g
		double totalCost[][][] = new double[nbCombinations][inventory.length][instance.getStages()];

		//optQ[i][t] stores the Q combination with the optimal ETC amongst all Q combinations
		double[][] optQ = new double[inventory.length][instance.getStages()];
		int[] optG = new int [inventory.length];

		//minCost[i] stores the optimal ETC amongst all Q combinations
		//it is obtained by comparing the existing value with a new-computed totalCost[i][0] for a new Q combination.
		double[] minCost = new double[inventory.length];//the optimal cost for an initial inventory level among all combinations

		long startTime=System.currentTimeMillis();
		
		//for each possible combination
		for(int g=0; g<(int) Math.pow(10, instance.getStages());g++) {

			//long singleTime = System.currentTimeMillis();

			//initial Qs
			double[] Q = generateQ(instance.getStages(), g);

			//compute cost for each combination
			for(int t=instance.getStages()-1; t>=0; t--) {			
				for(int i=0; i<inventory.length;i++) {

					totalCost[g][i][t] = sS.computePurchasingCost((int)Q[t], (int)instance.fixedOrderingCost, (int)instance.unitCost);
					double scenarioProb = 0;
					for(int d=0; d<demandProbabilities[t].length;d++) {
						if((inventory[i] + Q[t] - d <= instance.maxInventory) && (inventory[i] + Q[t] - d >= instance.minInventory)) {
							totalCost[g][i][t] += demandProbabilities[t][d]*(
									sS.computeImmediateCost(
											inventory[i], 
											Q[t], 
											d, 
											instance.holdingCost, 
											instance.penaltyCost, 
											instance.fixedOrderingCost, 
											instance.unitCost)
									+((t==instance.getStages()-1) ? 0 : totalCost[g][(int) (i+Q[t]-d)][t+1])
									);
							scenarioProb += demandProbabilities[t][d];
						}//if
					}//d
					totalCost[g][i][t] /= scenarioProb;
				}//i
			}//t

			//update computation status
			//long endTime=System.currentTimeMillis();
			if(g%1000 == 0) System.out.println("Computation completed for generator = "+g);
			//System.out.println("For I0 = 0: ETC = "+totalCost[(int) -instance.minInventory][0]);
			//System.out.println("Total comsumed time = "+(endTime - startTime)+" ms");
			//System.out.println("Consumed time for single combination = "+(endTime-singleTime)+" ms");
			//System.out.println("---------------------------------------------------------");
			//writeToText(g, totalCost[g][(int) (instance.initialInventory -instance.minInventory)][0]);

		}//g

		//if we store all cost information
		//choose the minimum cost for i = initial inventory //instance.initialInventory - instance.minInventory
		// by completing minCost[i]
		for(int i=0;i<inventory.length;i++) {
			minCost[i] = totalCost[0][i][0];
			for(int g=1; g<nbCombinations; g++) {
				if((totalCost[g][i][0] < totalCost[g-1][i][0])&&( totalCost[g][i][0] < minCost[i])) {
					minCost[i] = totalCost[g][i][0];
				}
			}
		}

		//get optQ[i][]
		for(int i=0; i<inventory.length;i++) {
			for(int g=0; g<nbCombinations; g++) {
				if(totalCost[g][i][0] == minCost[i]) {
					optG[i] = g;
					for(int t=0; t<instance.getStages(); t++) {
						optQ[i] = generateQ(instance.getStages(), g);
					}
				}
			}
		}

		long terminalTime = System.currentTimeMillis();
		//System.out.println("total time consumed = "+(terminalTime - startTime)+" ms");

		//System.out.println(Arrays.deepToString(totalCost));
		
		long timeConsumed_sQt = terminalTime - startTime;
		return new sQtgeneratedSolution(inventory, minCost, optQ, totalCost, optG, timeConsumed_sQt);

	}


	public static void main(String[] args) {

		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -50;
		int maxInventory = 50;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		int[] demandMean = {5,5,10,10,2};

		InstanceDouble instance = new InstanceDouble(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity,
				stdParameter
				);

		sQtgeneratedSolution solution = sQtgeneratedSingle(instance);
		
		System.out.println("optG = "+ solution.optG[(int) (instance.initialInventory - instance.minInventory)]);
		System.out.println("optCost = "+ solution.totalCost[solution.optG[(int) (instance.initialInventory - instance.minInventory)]][(int) (instance.initialInventory - instance.minInventory)][0]);
		/*
		for(int t=0; t<instance.getStages();t++) {
		    System.out.println("t: "+ (t+1)+ "\t"+ solution.totalCost[solution.optG[(int) (instance.initialInventory - instance.minInventory)]][(int) (instance.initialInventory - instance.minInventory)][t]);
			//plotTwoCostGivenQ(sQgivenQ.costOrder, sQgivenQ.costNoOrder, Q[t], t, instance);
		}
		
		
		for(int i=0; i<solution.inventory.length; i++) {
			System.out.print(i+" "+solution.minCost[i] +" ");
			for(int t=0; t<instance.getStages();t++) {
				System.out.print(solution.optQ[t] + " ");
			}
			System.out.println();
		}
		 */
		
		System.out.print("Qt = {");
		for(int t=0; t<instance.getStages();t++) {
			System.out.print((int)solution.optQ[(int) (instance.initialInventory - instance.minInventory)][t]);
			if(t<instance.getStages()-1)System.out.print(",");
		}System.out.print("}");
		/*
		System.out.println("Qt");
		for(int i=0; i<solution.inventory.length;i++) {
			System.out.print((int)(i +instance.minInventory)+" ");
			for(int t=0; t<instance.getStages();t++) {
				System.out.print(solution.optQ[i][t] + " ");
			}System.out.println();
		}
		 */
	}



}
