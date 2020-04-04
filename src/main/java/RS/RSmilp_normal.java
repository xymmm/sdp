package RS;

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

public class RSmilp_normal {

	double[] demandMean;
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
		this.stdParameter   = stdParameter;
		this.partitions 	= partitions;		
		this.means          = means;
		this.piecewiseProb  = piecewiseProb;
		this.error          = error;
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

	
	public RSmilpSolution solveRSmilp (String model_name) throws IloException{		
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		IloOplDataSource dataSource = new RSmilp_normal.RSdata(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);
		boolean status =  cplex.solve();
		
		double[] Q = new double[demandMean.length];
		double[] purchase = new double[demandMean.length];
		double[] stock = new double[demandMean.length];
		
		if ( status ){   
			for(int t = 0; t < demandMean.length; t++){
				Q[t] = 		  cplex.getValue(opl.getElement("Q").asNumVarMap().get(t+1));				
				purchase[t] = cplex.getValue(opl.getElement("purchaseDouble").asNumVarMap().get(t+1));
				stock[t] = 	  cplex.getValue(opl.getElement("stock").asNumVarMap().get(t));
			}
			System.out.println("model solved Q = "+Arrays.toString(Q));
			System.out.println("model solved stock = "+Arrays.toString(stock));
			System.out.println("model solved purchase = "+Arrays.toString(purchase));
			opl.postProcess(); oplF.end(); System.gc();
			return new RSmilpSolution(purchase, stock, Q);
		}else{
			oplF.end(); System.gc();
			return new RSmilpSolution(purchase, stock, demandMean);
		} 
	}

	
	/**import data to .mod**/
	class RSdata extends IloCustomOplDataSource{
		RSdata(IloOplFactory oplF){
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
	
	public static RSparameters RSmilpParameters(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost,
			double initialStock, double stdParameter, 
			int partitions, double[] piecewiseProb, double[] means, double error) {
		
		System.out.println("_______");
		System.out.println("demandMean = "+Arrays.toString(demandMean));
		System.out.println("stdParameter = "+stdParameter);
		System.out.println("f = "+fixedCost);
		System.out.println("h = "+holdingCost);
		System.out.println("p = "+penaltyCost);
		System.out.println("v = "+unitCost);
		System.out.println("_______");

		
		double[] S = new double[demandMean.length];
		double[] purchase = new double[demandMean.length];
		
		try {
			RSmilp_normal RSmodel = new RSmilp_normal(
					demandMean, fixedCost, unitCost, holdingCost, penaltyCost,
					initialStock, stdParameter, 
					partitions, piecewiseProb, means, error,
					null);
			RSmilpSolution RSsolution = RSmodel.solveRSmilp("RSnormal");
			for(int t=0; t<demandMean.length; t++) {
				S[t] = RSsolution.Q[t] + RSsolution.stock[t];
				purchase[t] = RSsolution.purchase[t];
			}

		}catch(IloException e) {
			e.printStackTrace();
		}
		System.out.println("method solved S = "+Arrays.toString(S));
		System.out.println("method solved purchase = "+Arrays.toString(purchase));
		return new RSparameters(S, purchase);
	}
	
	
	public static void main(String[] args) {
		
		double holdingCost = 1;
		
		double fixedCost = 100;
		double penaltyCost = 10;
		
		double unitCost = 0;
		
		double[] demandMean = {20, 40, 60, 40};
		double stdParameter = 0.25;
		
		double initialStock = 0;

		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;
		
		RSparameters RSparameters = RSmilpParameters(
				demandMean, fixedCost, unitCost, holdingCost, penaltyCost,
				initialStock, stdParameter, 
				partitions, piecewiseProb, means, error);
		
		double[] orderUpToLevel = RSparameters.S;
		double[] purchase = RSparameters.purchase;
		
		System.out.println(Arrays.toString(orderUpToLevel));
		System.out.println(Arrays.toString(purchase));
		
	}

	
	
}
