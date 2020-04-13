package RS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
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
import minlp_Poisson.sQTminlp_oneRun;
import umontreal.ssj.util.Chrono;

public class RSmilp_normal{

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
	String 		instanceIdentifier;

	public RSmilp_normal(double[] demandMean, double holdingCost, double fixedCost,  double unitCost, double penaltyCost, 
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

	public InputStream getMINLPmodelStream(File file) {
		FileInputStream is = null;
		try{
			is = new FileInputStream(file);
		}catch(IOException e){
			e.printStackTrace();
		}
		return is;
	}

	public RSmilpSolution solveRSNormal (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		IloOplDataSource dataSource = new RSmilp_normal.RSmilpSingle(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);
		boolean status =  cplex.solve();        
		if ( status ){   
			double[] stock = new double[demandMean.length];
			double[] purchase = new double[demandMean.length];
			for(int t=0; t<demandMean.length; t++) {
				stock[t] = cplex.getValue(opl.getElement("stock").asNumVarMap().get(t+1));
				purchase[t] = cplex.getValue(opl.getElement("purchaseDouble").asNumVarMap().get(t+1));
			}
			//System.out.println("stock by milp: "+Arrays.toString(stock));
			//System.out.println("purchase by milp: "+Arrays.toString(purchase));
			return new RSmilpSolution(purchase, stock);
		}else {
			double[] stock = new double[demandMean.length];
			double[] purchase = new double[demandMean.length];
			oplF.end();
			System.gc();
			return new RSmilpSolution(purchase, stock);
		} 
	}	
	/**import data to .mod**/
	class RSmilpSingle extends IloCustomOplDataSource{
		RSmilpSingle(IloOplFactory oplF){
			super(oplF);
		}
		public void customRead(){

			IloOplDataHandler handler = getDataHandler();
			handler.startElement("nbmonths"); handler.addIntItem(demandMean.length); handler.endElement();
			handler.startElement("fc"); handler.addNumItem(fixedCost); handler.endElement();
			handler.startElement("h"); handler.addNumItem(holdingCost); handler.endElement();
			handler.startElement("p"); handler.addNumItem(penaltyCost); handler.endElement();
			handler.startElement("v"); handler.addNumItem(unitCost); handler.endElement();
			handler.startElement("meandemand"); handler.startArray();            
			for (int j = 0 ; j<demandMean.length ; j++) {handler.addNumItem(demandMean[j]);}
			handler.endArray(); handler.endElement();
			handler.restartElement("stdParameter"); handler.addNumItem(stdParameter); handler.endElement();           
			handler.startElement("initialStock"); handler.addNumItem(initialStock); handler.endElement();
			//piecewise
			handler.startElement("nbpartitions"); handler.addIntItem(partitions); handler.endElement();
			handler.startElement("means"); handler.startArray();
			for (int j = 0 ; j<partitions; j++){handler.addNumItem(means[j]);}
			handler.endArray(); handler.endElement();
			handler.startElement("prob"); handler.startArray();
			for (int j = 0 ; j<partitions; j++){handler.addNumItem(piecewiseProb[j]);}
			handler.endArray(); handler.endElement();
			handler.startElement("error"); handler.addNumItem(error); handler.endElement();         
		}
	}

	public static RSmilpSolution RSmilpSchedule(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost,
			double initialStock, double stdParameter, 
			int partitions, double[] piecewiseProb, double[] means, double error) {

		double[] S = new double[demandMean.length];
		double[] R = new double[demandMean.length];

		try {
			RSmilp_normal RSmodel = new RSmilp_normal(
					demandMean, holdingCost, fixedCost,  unitCost, penaltyCost, 
					initialStock, stdParameter, 
					partitions,  means, piecewiseProb, error,
					null
					);
			RSmilpSolution milpsolution = RSmodel.solveRSNormal("RSnormal");
			for(int t=0; t<demandMean.length; t++) {
				S[t] = milpsolution.stock[t] + demandMean[t];
			}
			//System.out.println("S by method: "+Arrays.toString(S));
			R = milpsolution.purchase;
			//System.out.println("R by method: "+Arrays.toString(R));
		}catch(IloException e){
			e.printStackTrace();
		}
		return new RSmilpSolution (R, S);
	}

	public static void main(String[] args) {
		double holdingCost = 1;

		double[] fixedOrderingCost = {100, 500, 1000, 1500};
		double[] unitCost		   = {0,1};
		double[] penaltyCost	   = {10, 5, 10, 20};
		double[] stdParameter	   = {0.25, 0.1, 0.2, 0.3};

		double initialStock = 0;

		int partitions = 10;
		double[] piecewiseProb = {0.04206108420763477, 0.0836356495308449, 0.11074334596058821, 0.1276821455299152, 0.13587777477101692, 0.13587777477101692, 0.1276821455299152, 0.11074334596058821, 0.0836356495308449, 0.04206108420763477};
		double[] means = {-2.133986195498256, -1.3976822972668839, -0.918199946431143, -0.5265753462727588, -0.17199013069262026, 0.17199013069262026, 0.5265753462727588, 0.918199946431143, 1.3976822972668839, 2.133986195498256};
		double error = 0.005885974956458359;


		double[][] demandMean = {
				{20, 40, 60, 40}
		};

		RSmilpSolution RSmilpSolution = RSmilpSchedule(
				demandMean[0], fixedOrderingCost[0], unitCost[0], holdingCost, penaltyCost[0],
				initialStock, stdParameter[0], 
				partitions, piecewiseProb, means,  error);
		
		double[] S = RSmilpSolution.stock;
		double[] R = RSmilpSolution.purchase;
		System.out.println("final S: "+Arrays.toString(S));
		System.out.println("final R: "+Arrays.toString(R));
		
		
		RSmilpSimInstance RSinstance = new RSmilpSimInstance(
				demandMean[0], 
				stdParameter[0],
				fixedOrderingCost[0],
				unitCost[0],
				holdingCost, 
				penaltyCost[0], 
				initialStock, 
				S, 
				R				
				);	
		
		Chrono timer = new Chrono();
		
		int count = 5000000;
		RSsimulation.simulationRSmilp.simulationNormalRSmultipleRuns(RSinstance, count);
		
		RSinstance.statCost.setConfidenceIntervalStudent();
		System.out.println(RSinstance.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

		System.out.println(RSinstance.statCost.average());

	}

}
