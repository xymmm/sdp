package sQt.generatedQ;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sS.sdp.sS;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class sQtgeneratedQlocalM {
	
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
			File f = new File("./sQtGenerated.txt");
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


	public static double sQtgeneratedSingle(Instance instance, int g, double[][] demandProbabilities) {		
		
		double[] inventory = new double[(int) (instance.maxInventory - instance.minInventory + 1)];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		
		//totalCost[g][i][t] stores the values of costs for an inventory level at a stage with a given combination of generator g
		double totalCost[][] = new double[inventory.length][instance.getStages()];

		//initial Qs
		double[] Q = generateQ(instance.getStages(), g);
		
		//compute cost for each combination
		for(int t=instance.getStages()-1; t>=0; t--) {			
			for(int i=0; i<inventory.length;i++) {
				//minCost[i] = totalCost[i][0];
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
		return totalCost[(int)(instance.initialInventory -instance.minInventory)][0];
	}
	
	public static sQtgeneratedQlocalMsolution sQtMultipleG(Instance instance) {
		
		//double demandProbabilities [][] = sS.computeNormalDemandProbability(instance.demandMean, instance.stdParameter, instance.maxDemand, instance.tail); //normal
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);//Poisson

		double costPrevious;
		double costCurrent;
		double costLater;
		
		int T = instance.getStages();
		int maxG = (int) Math.pow(10, T);
		
		List<Double> LocalMinCostsList = new ArrayList<>();
		List<Integer> minGsList		   = new ArrayList<>();
		
		long startTime = System.currentTimeMillis();
		
		//SPECIAL CASE: g = 0,1,2
		costPrevious = sQtgeneratedSingle(instance, 0, demandProbabilities);
		costCurrent  = sQtgeneratedSingle(instance, 1, demandProbabilities);
		costLater    = sQtgeneratedSingle(instance, 2, demandProbabilities);
		if((costPrevious > costCurrent)&&(costCurrent < costLater)) {
			LocalMinCostsList.add(costCurrent);
			minGsList.add(1);
		}
		int g = 3;
		do {
			costPrevious = costCurrent;
			costCurrent = costLater;
			costLater = sQtgeneratedSingle(instance, g, demandProbabilities);
			if((costPrevious > costCurrent)&&(costCurrent < costLater)) {
				LocalMinCostsList.add(costCurrent);
				minGsList.add(g-1);
			}
			if(g%100000 == 0) System.out.println("Computation completed for generator = "+g);
			g = g+1;
		}while(g < maxG);
		
		
		double[] LocalMinCosts = new double[LocalMinCostsList.size()];
		int[] minGs	  = new int[minGsList.size()];
		for(int i=0; i<LocalMinCosts.length; i++) {
			LocalMinCosts[i] = (double) LocalMinCostsList.get(i);
			minGs[i] = (int) minGsList.get(i);
			//System.out.println(minGs[i] + "   "+LocalMinCosts[i]);
		}
		
		double minCost = LocalMinCosts[0];
		int optG = minGs[0];
		for(int i=1; i<LocalMinCosts.length; i++) {
			if((LocalMinCosts[i] < minCost)&&(LocalMinCosts[i] < LocalMinCosts[i-1])) {
				minCost = LocalMinCosts[i];
				optG = minGs[i];
			}
		}
		double[] optQ = generateQ(T, optG);
		
		long endTime = System.currentTimeMillis();
		long timeConsumed_sQt = endTime - startTime;
	
		return new sQtgeneratedQlocalMsolution(minCost, optQ, optG, timeConsumed_sQt);		
	}

	

	public static void main(String[] args) {

		double fixedOrderingCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		double tail = 0.00000001;

		int minInventory = -110;
		int maxInventory = 110;
		int maxQuantity = 9;

		double stdParameter = 0.25;

		int[] demandMean = {2,4,6,4};

		Instance instance = new Instance(
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

		sQtgeneratedQlocalMsolution solution = sQtMultipleG(instance);
		
		System.out.println("optimal Cost for I0 = 0 is "+solution.minCost);
		
		System.out.print("Qt = {");
		for(int t=0; t<instance.getStages();t++) {
			System.out.print((int)solution.optQ[t]);
			if(t<instance.getStages()-1)System.out.print(",");
		}System.out.print("}");	
		
		System.out.println();
		System.out.println(solution.timeConsumed_sQt/1000 + "s");
	}

	
	
	

}
