package LateralTransshipment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import LateralTransshipment.LT_MILP.LT_MILP_solution;
import minlp_Poisson.sQminlp_oneRun;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.PoissonGen;
import umontreal.ssj.randvar.RandomVariateGenInt;
import umontreal.ssj.rng.MRG32k3a;

/**
 * solve single problem to get order quantities, 
 * meet demand,
 * receding for the next period
 * 
 * no transshipment involved
 * **/

public class singleProblemRH {
	
	static singleProblemRHsolution singleSimulationRH(
			double quantityA, double quantityB, 
			double demandA, double demandB,
			double fixedOrderingCost, double unitOrderingCost,
			double holdingCost, double penaltyCost, double openingInventoryA, double openingInventoryB,
			boolean print) {

		double costA = 0; double costB = 0; 
		double inventoryLevelA = openingInventoryA;
		double inventoryLevelB = openingInventoryB;
		
		if(quantityA>0) {
			costA += fixedOrderingCost + unitOrderingCost * quantityA;
			inventoryLevelA += quantityA;
		}
		if(quantityB>0) {
			costB += fixedOrderingCost + unitOrderingCost * quantityB;
			inventoryLevelB += quantityB;
		}
		if(print) System.out.println("ordering cost = ["+costA +", "+ costB+"].");
		if(print)System.out.println("inventory after action = ["+inventoryLevelA +", "+ inventoryLevelB+"].");

		if(print) System.out.println("demand = ["+demandA+", "+demandB+"].");
		inventoryLevelA = inventoryLevelA - demandA;
		inventoryLevelB = inventoryLevelB - demandB;
		if(print) System.out.println("closing inventory = [ "+inventoryLevelA +", "+ inventoryLevelB+"].");

		if(inventoryLevelA >=0) {
			costA += inventoryLevelA * holdingCost;
		}else {
			costA += -inventoryLevelA * penaltyCost;
		}

		if(inventoryLevelB >=0) {
			costB += inventoryLevelB * holdingCost;
		}else {
			costB += -inventoryLevelB * penaltyCost;
		}
		if(print) System.out.println("closing total cost = "+(costA + costB)+".");
		if(print) System.out.println();

		return new singleProblemRHsolution(costA, costB, inventoryLevelA, inventoryLevelB);
	}

	public static double[] futureDemandSegment(double[] demand, int currentTimeIndex) {
		double[] demandSec = new double[demand.length - currentTimeIndex];
		for(int t=0; t<demandSec.length;t++) {
			demandSec[t] = demand[currentTimeIndex + t];
		}
		return demandSec;
	}

	static MRG32k3a randomStream = new MRG32k3a();	
	static {
		long seed[] = {1234,1234,1234,1234,1234,1234};
		randomStream.setSeed(seed);
	}
	static int generateDemand(double demandMean1) {
		RandomVariateGenInt genDemand;	  
		genDemand = new PoissonGen(randomStream, new PoissonDist(demandMean1)); 
		int demand = genDemand.nextInt();
		return demand;
	}


	/**For one step of receding horizon. 
	 * solve problem FROM the current period with mean of demand
	 * initialStock is substituted as the closing inventory of last iteration (here solves all scenarioes)
	 * @throws Exception **/
	public static singleProblemRHsolution singleProblemOneStepRH(
			double[] demandMeanA, double[] demandMeanB, 
			double randomDemandA, double randomDemandB, 
			double fixedOrderingCost, double unitOrderingCost,
			double holdingCost, double penaltyCost,
			double openingInventoryA, double openingInventoryB,
			int minInventory, int maxInventory, int maxQuantity,
			int currentTimeIndex, boolean print) throws Exception {

		//solve current SDP
		double q1 = sS.solveSinglePeriod.solveSinglePeriodsS(fixedOrderingCost, unitOrderingCost, holdingCost, penaltyCost, 
				0.0001, minInventory, maxInventory, maxQuantity, 0.25, 
				demandMeanA, currentTimeIndex, (int)openingInventoryA);
		double q2 = sS.solveSinglePeriod.solveSinglePeriodsS(fixedOrderingCost, unitOrderingCost, holdingCost, penaltyCost, 
				0.0001, minInventory, maxInventory, maxQuantity, 0.25, 
				demandMeanB, currentTimeIndex, (int)openingInventoryB);
		if(print)System.out.println("action = "+ q1+"\t"+q2);

		//get solution to this current period and simulate current period
		singleProblemRHsolution currentPeriodEnd = singleSimulationRH(
				q1, q2,
				randomDemandA, randomDemandB,		//generated demand with demandMean
				fixedOrderingCost, unitOrderingCost, 
				holdingCost, penaltyCost, openingInventoryA, openingInventoryB,		//same for the initialStock
				print);

		return new singleProblemRHsolution(currentPeriodEnd.costA, currentPeriodEnd.costB, 
				currentPeriodEnd.closingInventoryA, currentPeriodEnd.closingInventoryB);
	}


	/**Main computation of receding horizon - lateral transshipment with static strategy
	 * @throws Exception **/
	public static double singleProblemLT_RHcomplete(double[] demandMeanA, double[] demandMeanB,
			double fixedOrderingCost, double unitOrderingCost,
			double holdingCost, double penaltyCost,
			double openingInventoryA, double openingInventoryB, 			 									
			boolean print,
			int minInventory, int maxInventory, int maxQuantity) throws Exception {

		double[] totalCost = new double[demandMeanA.length];

		double[] randomDemandA = new double[demandMeanA.length];
		double[] randomDemandB = new double[demandMeanB.length];
		for(int t=0; t<demandMeanA.length; t++) {
			randomDemandA[t] = generateDemand(demandMeanA[t]);
			randomDemandB[t] = generateDemand(demandMeanB[t]);
		}
		//System.out.println(Arrays.toString(randomDemand));

		double[] closingInventoryA = new double[demandMeanA.length];
		closingInventoryA[0] = openingInventoryA;
		double[] closingInventoryB = new double[demandMeanB.length];
		closingInventoryB[0] = openingInventoryB;

		for(int t=0; t<demandMeanA.length; t++) {
			if(print) System.out.println((t+1)+": openingInventory = "+ closingInventoryA[t] +", "+closingInventoryB[t]);
			singleProblemRHsolution solution = singleProblemOneStepRH(
					demandMeanA, demandMeanB, 
					randomDemandA[t], randomDemandB[t], 
					fixedOrderingCost, unitOrderingCost,
					holdingCost, penaltyCost,
					closingInventoryA[t], closingInventoryB[t],
					minInventory, maxInventory, maxQuantity,
					t, print);
			totalCost[t] = solution.costA + solution.costB;
			if(t<demandMeanA.length-1) {
				closingInventoryA[t+1] = solution.closingInventoryA;
				closingInventoryB[t+1] = solution.closingInventoryB;
			}
		}

		return sdp.util.sum.summation(totalCost);
	}



	/**main**/
	public static void main(String[] args) throws Exception {
		double[] 	demandMeanA = {4,6,8,6};
		double[]	demandMeanB = {4,6,8,6};

		double fixedOrderingCost = 20;
		double unitOrderingCost = 1;

		double holdingCost = 0.25;
		double penaltyCost = 5;

		double minInventory = -20;
		double maxInventory = 60;
		double maxQuantity = maxInventory - minInventory;
		double[][] RHapproximatedCost = new double[(int) (maxInventory - minInventory+1)][(int) (maxInventory - minInventory+1)];

		boolean print = false;

		long timeStart = System.currentTimeMillis();

		int count = 10;

		for(int i=0; i<RHapproximatedCost.length; i++) {
			for(int j=0; j<RHapproximatedCost[i].length;j++) {
				System.out.println((i+minInventory)+", "+(j+minInventory));
				double[] costAll = new double[count];
				for(int c=0; c<count; c++) {
					costAll[c] = singleProblemLT_RHcomplete(
							demandMeanA, demandMeanB,
							fixedOrderingCost, unitOrderingCost,
							holdingCost, penaltyCost,
							i+minInventory,  j+minInventory,  			 									
							print,
							(int) minInventory, (int) maxInventory, (int) maxQuantity);
					//System.out.println(costAll[c]);
				}
				RHapproximatedCost[i][j] = sdp.util.sum.average(costAll);
			}
		}
		//System.out.println();
		//System.out.println("Average = "+sdp.util.sum.average(costAll));
		System.out.println("time = "+ (System.currentTimeMillis() - timeStart)/1000.0);

		FileWriter fw = null;
		try {
			File f = new File("src/main/java/LateralTransshipment/singleProblemRH.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("C_n approximated by receding horizon - solving sinble problem separately: ");		      
		pw.print("\t");
		for(int j = 0; j < RHapproximatedCost.length; j++) {
			pw.print( (j+minInventory) + "\t");
		}
		pw.println();
		for(int i = RHapproximatedCost.length-1; i >=0 ; i--) {
			pw.print((i+minInventory) + "\t");
			for(int j = 0; j < RHapproximatedCost.length; j++) {
				pw.print(RHapproximatedCost[i][j] + "\t");
			}
			pw.println();
		}
		pw.println();
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}	

}


class singleProblemRHsolution{
	public double costA;
	public double costB;
	public double closingInventoryA;
	public double closingInventoryB;

	public singleProblemRHsolution(double costA, double costB, double closingInventoryA, double closingInventoryB) {
		this.costA = costA;
		this.costB = costB;
		this.closingInventoryA = closingInventoryA;
		this.closingInventoryB = closingInventoryB;
	}
}



