package RecedingHorizon.sQt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import ilog.concert.IloException;
import ilog.opl.IloCplex;
import ilog.opl.IloCustomOplDataSource;
import ilog.opl.IloOplDataHandler;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;
import minlp_Normal.sQminlpNormal_oneRun;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.rng.MRG32k3a;

public class RH_sQt {
	
	double[] 	demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double 		initialStock;
	double 		stdParameter;
	int 		partitions;
	double[] 	means;
	double[] 	piecewiseProb;
	double 		error;	
	String 		instancIdentifier;
	
	public RH_sQt(double[] demandMean, double holdingCost, double fixedCost,  double unitCost, double penaltyCost, 
				  double initialStock, double stdParameter, 
				  int partitions, double[] means, double[] piecewiseProb, double error,
				  String instanceIdentifier) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;		
		this.initialStock 	= initialStock;
		this.stdParameter = stdParameter;	
		this.partitions 	= partitions;		
		this.means = means;
		this.piecewiseProb = piecewiseProb;
		this.error = error;
	}
	
	/** generate Normal random number as demand **/
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
	   //long seed[] = {1234,1234,1234,1234,1234,1234};
		long seed[] = {5678,5678,5678,5678,5678,5678};
	   randomStream.setSeed(seed);
	}	
	static double generateNormalDemand(double demandMean, double stdParameter) {
		double demand = NormalDist.inverseF(demandMean, 
				demandMean*stdParameter, randomStream.nextDouble());
		return Math.round(demand);
	}


	/**For one step of receding horizon. 
	 * solve problem FROM the current period with mean of demand
	 * initialStock is substituted as the closing inventory of last iteration
	 * @throws Exception **/
	public static singleRHsolution oneStepRH_sQt(double[] demandMean, double randomDemand, double stdParameter, 
									 double holdingCost, double fixedCost, double unitCost, double penaltyCost, 
									 int partitions, double[] means, double[] piecewiseProb, double error,
									 double initialStock, int currentTimeIndex, double pace, 
									 boolean print) throws Exception {
		
		//obtain the demandMean from the current period to T
		double[] futureDemand = generateNormalDemandSeries.futureDemandSegment(demandMean, currentTimeIndex);
		
		//solve current MINLP
		double[] schedule = minlp_Normal.sQTminlpNormal_oneRun.sQTminlpSchedule(
				futureDemand, fixedCost, unitCost, holdingCost, penaltyCost, 
				initialStock, stdParameter, 
				partitions, piecewiseProb, means, error);	//where initialStock will be an input, taking value of previous closing inventory
		if(print) System.out.println("schedule = "+Arrays.toString(schedule));
		double[] reorderPoints = minlp_Normal.sQTminlpNormal_heuristic.reorderPoint_sQtHeuristic(
				futureDemand, fixedCost, unitCost, holdingCost, penaltyCost, 
				initialStock, stdParameter, 
				partitions, piecewiseProb, means, error, 
				pace, schedule);							//same for initialStock
		if(print) System.out.println("reorderPoints = "+Arrays.toString(reorderPoints));
		
		//get solution to this current period and simulate current period
		singleRHSimSolution currentPeriodEnd = onePeriodSimulation.singlSimulationRH(
				schedule[0], reorderPoints[0], 
				randomDemand, 								//generated demand with demandMean
				fixedCost, holdingCost, penaltyCost, unitCost, initialStock, //same for the initialStock
				print);	

		return new singleRHsolution(currentPeriodEnd.cost, currentPeriodEnd.closingInventory);
	}
	
	
	/**Main computation of receding horizon - sQt
	 * @throws Exception **/
	public static double RHcomplete_sQt(double[] demandMean, double stdParameter, 
			 									double holdingCost, double fixedCost, double unitCost, double penaltyCost, 
			 									int partitions, double[] means, double[] piecewiseProb, double error,
			 									double initialStock, double pace,
			 									boolean print) throws Exception {
		double[] CurrentCost = new double[demandMean.length];
		double[] randomDemand = new double[demandMean.length];
		for(int t=0; t<demandMean.length; t++) {
			randomDemand[t] = generateNormalDemand(demandMean[t], stdParameter);
		}
		//System.out.println(Arrays.toString(randomDemand));
		
		double[] closingInventory = new double[demandMean.length];
		closingInventory[0] = initialStock;
		
		for(int t=0; t<demandMean.length; t++) {
			if(print) System.out.println((t+1));
			singleRHsolution solution = oneStepRH_sQt(demandMean, randomDemand[t], stdParameter, 
					 holdingCost, fixedCost, unitCost, penaltyCost, 
					 partitions, means, piecewiseProb, error,
					 closingInventory[t], t, pace, print);	
			CurrentCost[t] = solution.cost;
			if(t<demandMean.length-1)closingInventory[t+1] = solution.closingInventory;
		}

		
		return sdp.util.sum.summation(CurrentCost);
	}
	
	public static void main(String[] args) throws Exception {
		double[] demandMean = {209,91,33,79,2,76,109,115,224,22,48,136,13,211,85,86,180,37,50,73,195,77,246,3};
		double stdParameter = 0.1;
		double fixedCost = 250;
		double holdingCost = 1;
		double unitCost = 0;
		double penaltyCost = 2;
		double initialStock = 0;
		
		double pace = 40;
		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;
		
		boolean print = true;
		
		long timeStart = System.currentTimeMillis();
		
		int count = 2;
		double[] costAll = new double[count];
		
		for(int c=0; c<count; c++) {
			costAll[c] = RHcomplete_sQt(demandMean, stdParameter, 
					 holdingCost, fixedCost, unitCost, penaltyCost, 
					 partitions, means, piecewiseProb, error,
					 initialStock, pace, 
					 print);
			System.out.println(costAll[c]);
		}
		System.out.println();
		System.out.println("Average = "+sdp.util.sum.average(costAll));
		System.out.println("time = "+ (System.currentTimeMillis() - timeStart)/1000.0);
		

	
	}


}
