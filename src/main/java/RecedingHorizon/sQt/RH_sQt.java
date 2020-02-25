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

public class RH_sQt {
	
	double[] 	demand;	
	double		holdingCost;
	double		fixedCost;
	double		penaltyCost;
	double		unitCost;	
	double		initialStock;	
	String 		instancIdentifier;
	
	public RH_sQt(	double[] demand, double holdingCost, double fixedCost, double penaltyCost, double unitCost, double initialStock, 
								String instancIdentifier) {
		this.demand 		= demand;
		this.fixedCost 		= fixedCost;
		this.holdingCost 	= holdingCost;
		this.penaltyCost	= penaltyCost;
		this.unitCost		= unitCost;
		this.initialStock	= initialStock;
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
	
	/**solve a single step of receding horizon with MINLP, RETURN the optimal quantity of current period**/
	public singleRHminlpSolution_sQt solveSingleRHsQt(String model_name) throws IloException {
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getRHmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		
		IloOplDataSource dataSource = new singleRHdata_sQt(oplF);
        opl.addDataSource(dataSource);
        opl.generate();
        cplex.setOut(null);
        //double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        //double end = cplex.getCplexImpl().getCplexTime();
        
        if(status) {
        	double[] Q = new double[demand.length];
        	double[] stockhlb = new double[demand.length];
        	double[] stockplb = new double[demand.length];
        	for(int t=0; t<Q.length; t++) {
        		Q[t]  		= cplex.getValue(opl.getElement("Q").asNumVarMap().get(t+1));
        		stockhlb[t]	= cplex.getValue(opl.getElement("stockhlb").asNumVarMap().get(t+1));
        		stockplb[t]	= cplex.getValue(opl.getElement("stockplb").asNumVarMap().get(t+1));
        	}
        	opl.postProcess();
        	oplF.end();
        	System.gc();
        	return new singleRHminlpSolution_sQt(Q, stockhlb, stockplb);
        }else {
        	double[] Q = new double[demand.length];
        	double[] stockhlb = new double[demand.length];
        	double[] stockplb = new double[demand.length];
			System.out.println("No solution!");
			oplF.end();
			System.gc();
        	for(int t=0; t<Q.length; t++) {
        		Q[t]  		= Double.NaN;
        		stockhlb[t]	= Double.NaN;
        		stockplb[t]	= Double.NaN;
        	}
        	return new singleRHminlpSolution_sQt(Q, stockhlb, stockplb);
        }//if-else
	}
	
	
	/**import data to .mod**/
	class singleRHdata_sQt extends IloCustomOplDataSource{
		singleRHdata_sQt(IloOplFactory oplF){super(oplF);}
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
		}

	}
	
	
	/**For one step of receding horizon. 
	 * 'demand' is generated in the main, containing all information of demand that is being dealt with.
	 * initialStock is substituted as the closing inventory of last iteration**/
	public static singleRHsolution_sQt oneStepRH_sQt(double[] demand, double stdParameter, 
									 double holdingCost, double fixedCost, double unitCost, double penaltyCost, 
									 double initialStock, int currentTimeIndex) {
		double[] futureDemand = RecedingHorizon.sQt.generateNormalDemandSeries.demandRecedingHorizon(demand, currentTimeIndex);
		System.out.println("t = "+(currentTimeIndex+1)+"\t"+"future demand: "+Arrays.toString(futureDemand));
		singleRHminlpSolution_sQt solution = null;
		try {
			RH_sQt sQtRHmodel = new RH_sQt(
					futureDemand, 
					holdingCost, fixedCost, penaltyCost, unitCost, initialStock, null
					);
			solution = sQtRHmodel.solveSingleRHsQt("recedingHorizon_sQt_Normal");
		}catch(IloException e){
	         e.printStackTrace();
	    }
		System.out.println("MINLP"+"\t"+"Q: "+Arrays.toString(solution.Q));
		System.out.println("MINLP"+"\t"+"holding: "+Arrays.toString(solution.stockhlb));
		System.out.println("MINLP"+"\t"+"penalty: "+Arrays.toString(solution.stockplb));
		System.out.println();
		
		double optimalQ = solution.Q[0];
		double currentCost = ((optimalQ>0)?1:0)*(fixedCost + optimalQ*unitCost) 
								+ holdingCost * solution.stockhlb[0] + penaltyCost * solution.stockplb[0];
		double closingInventory = (solution.stockhlb[0]>0) ? solution.stockhlb[0] : (-solution.stockplb[0]);
		return new singleRHsolution_sQt(optimalQ, currentCost, closingInventory);
	}
	
	/**Main computation of receding horizon - sQt**/
	public static RHsolution_sQt RHcomplete_sQt(double[] demand,
									double stdParameter, double holdingCost, double fixedCost, double unitCost, double penaltyCost,
									double initialStock) {
		double[] scheduleQ = new double[demand.length];
		double[] scheduleCurrentCost = new double[demand.length];
		
		double[] closingInventory = new double[demand.length];
		closingInventory[0] = initialStock;
		
		for(int t=0; t<demand.length; t++) {
			singleRHsolution_sQt solution = oneStepRH_sQt(demand, stdParameter, 
					 holdingCost, fixedCost, unitCost, penaltyCost, 
					 closingInventory[t], t);
			scheduleQ[t] = solution.Q;
			scheduleCurrentCost[t] = solution.currentCost;
			if(t<=demand.length-2)closingInventory[t] = solution.closingInventory;
		}
		
		
		return new RHsolution_sQt(scheduleQ, scheduleCurrentCost);
	}
	
	
	
	public static void main(String[] args) {
		double[] demandMean = {20,40,60,40};
		double stdParameter = 0.25;
		double holdingCost = 1;
		double fixedCost = 100;
		double unitCost = 0;
		double penaltyCost = 10;
		double initialStock = 0;
		
		double[] demand = RecedingHorizon.sQt.generateNormalDemandSeries.generateNormalDemand(demandMean, stdParameter);
		System.out.println("Generated Normal Demand: "+Arrays.toString(demand));
		System.out.println();
		
		RHsolution_sQt solution = RHcomplete_sQt(demand, stdParameter, 
				 holdingCost, fixedCost, unitCost, penaltyCost, 
				 initialStock);
		
		System.out.println("===================================");
		System.out.println("RH-Q"+"\t"+Arrays.toString(solution.scheduleQ));
		System.out.println("RH-C"+"\t"+Arrays.toString(solution.scheduleCurrentCost));

	}


}
