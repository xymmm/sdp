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

public class LT_RQ_RecedingHorizon {

	public static singleSimRH singleSimulationRH(
			double quantityA, double quantityB, double transshipment,
			double demandA, double demandB,
			double fixedOrderingCost, double unitOrderingCost, double fixedTransshippingCost, double unitTransshippingCost,
			double holdingCost, double penaltyCost, double openingInventoryA, double openingInventoryB,
			boolean print) {

		double costA = 0; double costB = 0; double costT = 0;
		double inventoryLevelA = openingInventoryA;
		double inventoryLevelB = openingInventoryB;

		if(Math.abs(transshipment) >0) {
			costT += fixedTransshippingCost + Math.abs(transshipment) * unitTransshippingCost;
			inventoryLevelA = inventoryLevelA - transshipment;
			inventoryLevelB = inventoryLevelB + transshipment;
			if(print) System.out.println("transshipment cost = "+costT+", inventory = ["+inventoryLevelA +", "+ inventoryLevelB+"].");
		}		
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
		if(print) System.out.println("closing total cost = "+(costA + costB + costT)+".");
		if(print) System.out.println();

		return new singleSimRH(costA, costB, costT, inventoryLevelA, inventoryLevelB);
	}

	public static double[] futureDemandSegment(double[] demand, int currentTimeIndex) {
		double[] demandSec = new double[demand.length - currentTimeIndex];
		for(int t=0; t<demandSec.length;t++) {
			demandSec[t] = demand[currentTimeIndex + t];
		}
		return demandSec;
	}

	double[] 	demandMeanA;
	double[]	demandMeanB;

	double 		fixedOrderingCost;
	double 		unitOrderingCost;

	double		fixedTransshippingCost;
	double		unitTransshippingCost;

	double 		holdingCost;
	double 		penaltyCost;

	double 		initialStockA;
	double		initialStockB;

	int 		partitions;

	String 		instancIdentifier;

	public LT_RQ_RecedingHorizon(double[] demandMeanA, double[] demandMeanB,
			double fixedOrderingCost, double unitOrderingCost,
			double fixedTransshippingCost, double unitTransshippingCost,
			double holdingCost, double penaltyCost,
			double initialStockA, double initialStockB, 
			int partitions, 
			String instanceIdentifier) {
		this.demandMeanA 	= demandMeanA;
		this.demandMeanB 	= demandMeanB;
		this.fixedOrderingCost = fixedOrderingCost;
		this.unitOrderingCost = unitOrderingCost;
		this.fixedTransshippingCost = fixedTransshippingCost;
		this.unitTransshippingCost = unitTransshippingCost;
		this.holdingCost 	= holdingCost;
		this.penaltyCost 	= penaltyCost;			
		this.partitions 	= partitions;		

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
	 * initialStock is substituted as the closing inventory of last iteration
	 * @throws Exception **/
	public static singleRHsolution oneStepRH_sQt(
			double[] demandMeanA, double[] demandMeanB, 
			double randomDemandA, double randomDemandB, 
			double fixedOrderingCost, double unitOrderingCost,
			double fixedTransshippingCost, double unitTransshippingCost,
			double holdingCost, double penaltyCost,
			double openingInventoryA, double openingInventoryB,
			int partitions,
			int currentTimeIndex, boolean print,
			String model, double[][][] coefficients) throws Exception {

		//obtain the demandMean from the current period to T
		double[] futureDemandA = futureDemandSegment(demandMeanA, currentTimeIndex);
		double[] futureDemandB = futureDemandSegment(demandMeanB, currentTimeIndex);

		//solve current MINLP
		double[] initialStock = {openingInventoryA, openingInventoryB};
		LT_MILP milpInstance = new LT_MILP(futureDemandA, futureDemandB, 
				fixedOrderingCost, unitOrderingCost,
				fixedTransshippingCost, unitTransshippingCost,
				holdingCost, penaltyCost,
				initialStock,  partitions, null, coefficients);//where initialStock will be an input, taking value of previous closing inventory
		LT_MILP_solution milpSolution = milpInstance.solveLT_combinedS(model);
		if(print)System.out.println("action = "+ milpSolution.order1[0]+"\t"+milpSolution.order2[0]+"\t"+milpSolution.transship[0]);

		//get solution to this current period and simulate current period
		singleSimRH currentPeriodEnd = singleSimulationRH(
				milpSolution.order1[0], milpSolution.order2[0], milpSolution.transship[0],
				randomDemandA, randomDemandB,		//generated demand with demandMean
				fixedOrderingCost, unitOrderingCost, fixedTransshippingCost, unitTransshippingCost,
				holdingCost, penaltyCost, openingInventoryA, openingInventoryB,		//same for the initialStock
				print);

		return new singleRHsolution(currentPeriodEnd.costA, currentPeriodEnd.costB, currentPeriodEnd.costT, 
				currentPeriodEnd.closingInventoryA, currentPeriodEnd.closingInventoryB);
	}


	/**Main computation of receding horizon - lateral transshipment with static strategy
	 * @throws Exception **/
	public static double LT_RHcomplete(double[] demandMeanA, double[] demandMeanB,
			double fixedOrderingCost, double unitOrderingCost,
			double fixedTransshippingCost, double unitTransshippingCost,
			double holdingCost, double penaltyCost,
			double openingInventoryA, double openingInventoryB, 			 									
			int partitions, String model,
			boolean print,
			double[][][][] coefficients) throws Exception {


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
			singleRHsolution solution = oneStepRH_sQt(
					demandMeanA, demandMeanB, 
					randomDemandA[t], randomDemandB[t], 
					fixedOrderingCost, unitOrderingCost,
					fixedTransshippingCost, unitTransshippingCost,
					holdingCost, penaltyCost,
					closingInventoryA[t], closingInventoryB[t],
					partitions,
					t, print,
					model, coefficients[t]);
			totalCost[t] = solution.costA + solution.costB + solution.costT;
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
		double fixedTransshippingCost = 5;
		double unitTransshippingCost = 0.5;

		double holdingCost = 0.25;
		double penaltyCost = 5;

		double openingInventoryA;
		double openingInventoryB;

		int partitions = 10;

		double minInventory = -20;
		double maxInventory = 30;//60;
		double[][] RHapproximatedCost = new double[(int) (maxInventory - minInventory+1)][(int) (maxInventory - minInventory+1)];

		boolean print = false;

		String model = "LT_MILP_C";

		long timeStart = System.currentTimeMillis();

		int count = 50;
		
		double[][][][] coefficients = new double[demandMeanA.length][demandMeanA.length][demandMeanA.length][partitions];
		for(int t=0; t<demandMeanA.length;t++) {
			double[] demandMeanRow = sdp.util.demandMeanInput.createDemandMeanInputRow(demandMeanA, t);
			coefficients[t] = sQminlp_oneRun.getLamdaMatrix (demandMeanRow, partitions, 100000);
		}


		for(int i=0; i<RHapproximatedCost.length; i++) {
			for(int j=0; j<RHapproximatedCost[i].length;j++) {
				System.out.println((i+minInventory)+", "+(j+minInventory));
				double[] costAll = new double[count];
				for(int c=0; c<count; c++) {
					costAll[c] = LT_RHcomplete(demandMeanA, demandMeanB,
							fixedOrderingCost,  unitOrderingCost,
							fixedTransshippingCost,  unitTransshippingCost,
							holdingCost,  penaltyCost,
							i+minInventory,  j+minInventory, 			 						//openingInventory A and B			
							partitions,  model,
							print, coefficients);
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
			File f = new File("src/main/java/LateralTransshipment/RH_static.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("C_n approximated by receding horizon - static policy: ");		      
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



class RHpolicy{
	public double quantityA;
	public double quantityB;
	public double transshipment;
	public double totalCost;

	public RHpolicy(double quantityA, double quantityB, double transshipment, double totalCost) {
		this.quantityA = quantityA;
		this.quantityB = quantityB;
		this.transshipment = transshipment;
		this.totalCost = totalCost;		
	}

}

class singleRHsolution{
	public double costA;
	public double costB;
	public double costT;
	public double closingInventoryA;
	public double closingInventoryB;

	public singleRHsolution(double costA, double costB, double costT, double closingInventoryA, double closingInventoryB) {
		this.costA = costA;
		this.costB = costB;
		this.costT = costT;
		this.closingInventoryA = closingInventoryA;
		this.closingInventoryB = closingInventoryB;
	}
}

class singleSimRH{
	public double costA;
	public double costB;
	public double costT;
	public double closingInventoryA;
	public double closingInventoryB;

	public singleSimRH(double costA, double costB, double costT, double closingInventoryA, double closingInventoryB) {
		this.costA = costA;
		this.costB = costB;
		this.costT = costT;
		this.closingInventoryA = closingInventoryA;
		this.closingInventoryB = closingInventoryB;
	}
}
