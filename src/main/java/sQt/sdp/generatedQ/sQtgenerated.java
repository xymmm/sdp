package sQt.sdp.generatedQ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
			File f = new File("E:\\sQtGenerated.txt");
			fw = new FileWriter(f, true);//true,进行追加写。
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
		
		long startTime=System.currentTimeMillis();
		
		//create inventory state space
		double[] inventory = new double[(int) (instance.maxInventory - instance.minInventory + 1)];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		//demand
		double demandProbabilities [][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail);
		

		//totalCost[i][t] stores the values of costs for an inventory level at a stage with a given Q combination
		double totalCost[][] = new double[inventory.length][instance.getStages()];
		
		//optQ[i][t] stores the Q combination with the optimal ETC amongst all Q combinations
		double[][] optQ = new double[inventory.length][instance.getStages()];
		
		//minCost[i] stores the optimal ETC amongst all Q combinations
		//it is obtained by comparing the existing value with a new-computed totalCost[i][0] for a new Q combination.
		double[] minCost = new double[inventory.length];//the optimal cost for an initial inventory level among all combinations
		for(int i=0; i<inventory.length;i++) {
			minCost[i] = Double.POSITIVE_INFINITY;
		}
		
		//to generate Q
		//int[] generator = new int[(int) Math.pow(10, instance.getStages())];
		
		//for each possible combination
		for(int g=0; g<(int) Math.pow(10, instance.getStages());g++) {
			
			long singleTime = System.currentTimeMillis();
			
			//initial Qs
			double[] Q = generateQ(instance.getStages(), g);

			//compute cost for each combination
			for(int t=instance.getStages()-1; t>=0; t--) {
				for(int i=0; i<inventory.length;i++) {
					totalCost[i][t] = sS.computePurchasingCost((int)Q[t], (int)instance.fixedOrderingCost, (int)instance.unitCost);
					double scenarioProb = 0;
					for(int d=0; d<demandProbabilities[t].length;d++) {
						if((inventory[i] + Q[t] - d <= instance.maxInventory) && (inventory[i] + Q[t] - d >= instance.minInventory)) {
							totalCost[i][t] += demandProbabilities[t][d]*(
												sS.computeImmediateCost(
														inventory[i], 
														Q[t], 
														d, 
														instance.holdingCost, 
														instance.penaltyCost, 
														instance.fixedOrderingCost, 
														instance.unitCost)
												+((t==instance.getStages()-1) ? 0 : totalCost[(int) (i+Q[t]-d)][t+1])
												);
							scenarioProb += demandProbabilities[t][d];
						}//if
					}//d
					totalCost[i][t] /= scenarioProb;
				}//i
			}//t
			
			//update optimal values of cost and Qs for each initial inventory level
			for(int i=0; i<inventory.length;i++) {
				if(minCost[i] > totalCost[i][0]) {
					//update cost
					minCost[i] = totalCost[i][0];
					//update Qs
					for(int t=0; t<instance.getStages();t++) {
						optQ[i][t] = Q[t];
					}
				}
			}//update
			
			//update computation status
			long endTime=System.currentTimeMillis();
			System.out.println("Computation completed for generator = "+g);
			//System.out.println("For I0 = 0: ETC = "+totalCost[(int) -instance.minInventory][0]);
			//System.out.println("Total comsumed time = "+(endTime - startTime)+" ms");
			//System.out.println("Consumed time for single combination = "+(endTime-singleTime)+" ms");
			//System.out.println("---------------------------------------------------------");
			writeToText(g, totalCost[(int) -instance.minInventory][0]);
		}//g
		
		return new sQtgeneratedSolution(inventory, minCost, optQ);
	}
	
	public static void main(String[] args) {
		
		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		
		double tail = 0.00000001;
		
		int minInventory = -500;
		int maxInventory = 500;
		int maxQuantity = 500;
		
		double stdParameter = 0.25;
		
		int[] demandMean = {20,40,60,40};
		
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
		/*
		for(int i=0; i<solution.inventory.length; i++) {
			System.out.print(i+" "+solution.minCost[i] +" ");
			for(int t=0; t<instance.getStages();t++) {
				System.out.print(solution.optQ[t] + " ");
			}
			System.out.println();
		}
		*/
	}
	

	
}
