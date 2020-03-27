package minlp_Normal;

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

public class sQTminlpNormal_oneRun{

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
	public sQTminlpNormal_oneRun(double[] demandMean, double holdingCost, double fixedCost,  double unitCost, double penaltyCost, 
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

	public double solvesQtminlpNormal (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		IloOplDataSource dataSource = new sQTminlpNormal_oneRun.sQTsingleDataNormal(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);
		boolean status =  cplex.solve();        
		if ( status )
		{   
			double[] Q = new double[demandMean.length];
			for(int t = 0; t < Q.length; t++){
				Q[t] = cplex.getValue(opl.getElement("Q").asNumVarMap().get(t+1));
				if((Q[t]<0)||(Q[t] > 500)) {
					Q[t] = demandMean[t];
				}
			}
			opl.postProcess();
			oplF.end();
			System.gc();
			return Q[0];
		} else {
			double[] Q = new double[demandMean.length];
			System.out.println("No solution!");
			oplF.end();
			System.gc();
			for(int t = 0; t < Q.length; t++){
				/*
            	if(demandMean[t] * penaltyCost < fixedCost + unitCost * demandMean[t]) {
            		Q[t] = demandMean[t];
            	}else {
            		Q[t] = 0;
            	}*/
				Q[t] = 0;
			}
			return Q[0];
		} 
	}	
	/**import data to .mod**/
	class sQTsingleDataNormal extends IloCustomOplDataSource{
		sQTsingleDataNormal(IloOplFactory oplF){
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

	public static double[] sQTminlpSchedule(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost,
			double initialStock, double stdParameter, 
			int partitions, double[] piecewiseProb, double[] means, double error) {
		double[] schedule = new double[demandMean.length];
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		for(int t=0; t<demandMean.length; t++) {
			try {
				sQTminlpNormal_oneRun sQmodel = new sQTminlpNormal_oneRun(
						demandMeanInput[t], holdingCost, fixedCost,  unitCost, penaltyCost, 
						initialStock, stdParameter, 
						partitions,  means, piecewiseProb, error,
						null
						);
				schedule[t] = sQmodel.solvesQtminlpNormal("sQtNormal_oneRun");
			}catch(IloException e){
				e.printStackTrace();
			}
		}
		return schedule;
	}

}
