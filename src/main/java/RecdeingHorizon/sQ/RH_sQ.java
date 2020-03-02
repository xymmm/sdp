package RecdeingHorizon.sQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import RecedingHorizon.sQt.RH_sQt;
import RecedingHorizon.sQt.RHsolution_sQt;
import RecedingHorizon.sQt.singleRHminlpSolution_sQt;
import RecedingHorizon.sQt.singleRHsolution_sQt;
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

public class RH_sQ {
	
	double[] 	demand;	
	double		holdingCost;
	double		fixedCost;
	double		penaltyCost;
	double		unitCost;	
	double		initialStock;	
	String 		instancIdentifier;
	
	double		Q;
	
	RH_sQ(double[] demand, double holdingCost, double fixedCost, double penaltyCost, double unitCost, double initialStock, 
			String instancIdentifier, double Q){
		this.demand 		= demand;
		this.fixedCost 		= fixedCost;
		this.holdingCost 	= holdingCost;
		this.penaltyCost	= penaltyCost;
		this.unitCost		= unitCost;
		this.initialStock	= initialStock;
		this.Q				= Q;
	}
	
	public InputStream getRHmodelStream(File file) {
	      FileInputStream is = null;
	      try{
	         is = new FileInputStream(file);
	      }catch(IOException e){
	         e.printStackTrace();
	      }
	      return is;
	}
	
	
	/**solve a single step of receding horizon with MINLP, RETURN the stockplb, stockhlb of current period**/
	public singleRHminlpSolution_sQ solveSingleRHsQ(String model_name) throws IloException {
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getRHmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		
		IloOplDataSource dataSource = new singleRHdata_sQ(oplF);
        opl.addDataSource(dataSource);
        opl.generate();
        cplex.setOut(null);
        //double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        //double end = cplex.getCplexImpl().getCplexTime();
        
        if(status) {
        	double[] stockhlb = new double[demand.length];
        	double[] stockplb = new double[demand.length];
        	for(int t=0; t<demand.length; t++) {
        		stockhlb[t]	= cplex.getValue(opl.getElement("stockhlb").asNumVarMap().get(t+1));
        		stockplb[t]	= cplex.getValue(opl.getElement("stockplb").asNumVarMap().get(t+1));
        	}
        	opl.postProcess();
        	oplF.end();
        	System.gc();
        	return new singleRHminlpSolution_sQ(stockhlb, stockplb);
        }else {
        	double[] stockhlb = new double[demand.length];
        	double[] stockplb = new double[demand.length];
			System.out.println("No solution!");
			oplF.end();
			System.gc();
        	for(int t=0; t<demand.length; t++) {
        		stockhlb[t]	= Double.NaN;
        		stockplb[t]	= Double.NaN;
        	}
        	return  new singleRHminlpSolution_sQ(stockhlb, stockplb);
        }//if-else
	}
	
	
	/**import data to .mod**/
	class singleRHdata_sQ extends IloCustomOplDataSource{
		singleRHdata_sQ(IloOplFactory oplF){super(oplF);}
		public void customRead(){
			IloOplDataHandler handler = getDataHandler();
			//start import data
			handler.startElement("nbmonths"); handler.addIntItem(demand.length); handler.endElement();
			handler.startElement("demand"); handler.startArray();            
			for (int j = 0 ; j<demand.length ; j++) {handler.addNumItem(demand[j]);}
			handler.endArray(); handler.endElement();
			handler.startElement("fc"); handler.addNumItem(fixedCost); handler.endElement();
			handler.startElement("h"); handler.addNumItem(holdingCost); handler.endElement();
			handler.startElement("p"); handler.addNumItem(penaltyCost); handler.endElement();
			handler.startElement("v"); handler.addNumItem(unitCost); handler.endElement();
			handler.startElement("initialStock"); handler.addNumItem(initialStock); handler.endElement();  
			handler.startElement("Q"); handler.addNumItem(Q);handler.endElement();
		}

	}
	
	/**For one step of receding horizon. 
	 * 'demand' is generated in the main, containing all information of demand that is being dealt with.
	 * initialStock is substituted as the closing inventory of last iteration
	 * Q is substituted as predefined**/
	public static singleRHsolution_sQ oneStepRH_sQ(double[] demand, double stdParameter, 
									 double holdingCost, double fixedCost, double unitCost, double penaltyCost, 
									 double initialStock, int currentTimeIndex, double Q) {
		double[] futureDemand = RecedingHorizon.sQt.generateNormalDemandSeries.demandRecedingHorizon(demand, currentTimeIndex);
		System.out.println("t = "+(currentTimeIndex+1)+"\t"+"future demand: "+Arrays.toString(futureDemand));
		singleRHminlpSolution_sQ solution = null;
		try {
			RH_sQ sQRHmodel = new RH_sQ(
					futureDemand, 
					holdingCost, fixedCost, penaltyCost, unitCost, initialStock, null, Q
					);
			solution = sQRHmodel.solveSingleRHsQ("recedingHorizon_sQ_Normal");
		}catch(IloException e){
	         e.printStackTrace();
	    }
		System.out.println("MINLP"+"\t"+"Given Q: "+ Q);
		System.out.println("MINLP"+"\t"+"holding: "+Arrays.toString(solution.stockhlb));
		System.out.println("MINLP"+"\t"+"penalty: "+Arrays.toString(solution.stockplb));
		System.out.println();
		
		double currentCost = ((Q>0)?1:0)*(fixedCost + Q*unitCost) 
								+ holdingCost * solution.stockhlb[0] + penaltyCost * solution.stockplb[0];
		double closingInventory = (solution.stockhlb[0]>0) ? solution.stockhlb[0] : (-solution.stockplb[0]);
		return new singleRHsolution_sQ(currentCost, closingInventory);
	}

	/**Main computation of receding horizon - sQt**/
	public static RHsolution_sQ RHcomplete_sQ(double[] demand,
									double stdParameter, double holdingCost, double fixedCost, double unitCost, double penaltyCost,
									double initialStock, double Q) {
		double[] scheduleCurrentCost = new double[demand.length];
		
		double[] closingInventory = new double[demand.length];
		closingInventory[0] = initialStock;
		
		for(int t=0; t<demand.length; t++) {
			System.out.println("MINLP"+"\t"+"opening Inventory: "+closingInventory[t]);
			singleRHsolution_sQ solution = oneStepRH_sQ(demand, stdParameter, 
					 holdingCost, fixedCost, unitCost, penaltyCost, 
					 closingInventory[t], t,Q);
			scheduleCurrentCost[t] = solution.currentCost;
			if(t<=demand.length-2)closingInventory[t] = solution.closingInventory;
		}
		double totalCost = sdp.util.sum.summation(scheduleCurrentCost);
		return new RHsolution_sQ(scheduleCurrentCost,totalCost);
	}
	
	public static void main(String[] args) {
		double[] demandMean = {20,40,60,40};
		double stdParameter = 0.25;
		double holdingCost = 1;
		double fixedCost = 100;
		double unitCost = 0;
		double penaltyCost = 10;
		double initialStock = 0;
		
		double Q = 20;
		
		double[] demand = RecedingHorizon.sQt.generateNormalDemandSeries.generateNormalDemand(demandMean, stdParameter);
		System.out.println("Generated Normal Demand: "+Arrays.toString(demand));
		System.out.println();
		
		RHsolution_sQ solution = RHcomplete_sQ(demand, stdParameter, 
				 holdingCost, fixedCost, unitCost, penaltyCost, 
				 initialStock, Q);
		
		System.out.println("===================================");
		System.out.println("RH-Q"+"\t"+ Q);
		System.out.println("RH-C"+"\t"+Arrays.toString(solution.scheduleCurrentCost));
		System.out.println("RH-C"+"\t"+solution.totalCost);


	}


}
