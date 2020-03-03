package minlp_Normal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

public class sQminlpNormal_oneRun{

	double[] 	demandMean;
	double 	holdingCost;
	double 	fixedCost;
	double 	unitCost;
	double 	penaltyCost;
	double 	initialStock;
	double stdParameter;
	int 	partitions;
	double[] means;
	double[] piecewiseProb;
	double error;
	
	String instanceIdentifier;
	
	public sQminlpNormal_oneRun(double[] demandMean, double holdingCost, double fixedCost,  double unitCost, double penaltyCost, 
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
	
	public double solveMINLP_oneRun_Normal (String model_name) throws IloException{		
		IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloCplex cplex = oplF.createCplex();
        IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloOplModel opl=oplF.createOplModel(def,cplex);
        cplex.setParam(IloCplex.IntParam.Threads, 8);
        cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
        
        IloOplDataSource dataSource = new sQminlpNormal_oneRun.sQsingleData(oplF);
        opl.addDataSource(dataSource);
        opl.generate();

        cplex.setOut(null);
        
        double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        double end = cplex.getCplexImpl().getCplexTime();
        
        if ( status )
        {   
        	double objective = cplex.getObjValue();
        	double time = end - start;
        	//System.out.println("OBJECTIVE: " + objective);  
        	double Q = cplex.getValue(opl.getElement("Q").asNumVar());
        	//System.out.println("time = "+time);
        	opl.postProcess();
        	//opl.printSolution(System.out);
        	//opl.end();
        	oplF.end();
        	//errHandler.end();
        	//cplex.end();
        	System.gc();

        	//return objective;
        	//System.out.println(S[0]);
        	return Q;
        } else {
        	System.out.println("No solution!");
        	//opl.end();
        	oplF.end();
        	//errHandler.end();
        	//cplex.end();
        	System.gc();
        	return Double.NaN;
        } 

	}
	
	
	/**import data to .mod**/
	class sQsingleData extends IloCustomOplDataSource{
		sQsingleData(IloOplFactory oplF){
            super(oplF);
        }

        public void customRead(){
        
         IloOplDataHandler handler = getDataHandler();
         //problem parameters
         		//demandMean, holdingCost, fixedCost,  unitCost, penaltyCost, 
		 		//initialStock, stdParameter, 
		 		//partitions,  means, piecewiseProb, error,

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

	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		
		double[] demandMean = {11,17,26,38,53,71,92,115,138,159,175,186,190,186,175,159,138,115,92,71};// {20,40,60,40};
		double fixedCost = 100;//5,10,20
		double unitCost = 1;//0,1
		double holdingCost = 1;
		double penaltyCost = 10;//2,3
		double initialStock = 0;
		double stdParameter = 0.1;
		
		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;
		
/*
		int partitions = 10;
		double[] piecewiseProb = {0.04206108420763477, 0.0836356495308449, 0.11074334596058821, 0.1276821455299152, 0.13587777477101692, 0.13587777477101692, 0.1276821455299152, 0.11074334596058821, 0.0836356495308449, 0.04206108420763477};
		double[] means = {-2.133986195498256, -1.3976822972668839, -0.918199946431143, -0.5265753462727588, -0.17199013069262026, 0.17199013069262026, 0.5265753462727588, 0.918199946431143, 1.3976822972668839, 2.133986195498256};
		double error = 0.005885974956458359;
		*/
		double Q = Double.NaN;
		
		try {
			sQminlpNormal_oneRun sQmodel = new sQminlpNormal_oneRun(
					demandMean, holdingCost, fixedCost,  unitCost, penaltyCost, 
					initialStock, stdParameter, 
					partitions,  means, piecewiseProb, error,
					null
					);
			Q = sQmodel.solveMINLP_oneRun_Normal("sQsingleNormal_oneRun");
		}catch(IloException e){
	         e.printStackTrace();
	    }
		long endTime = System.currentTimeMillis();
		System.out.println("Q = "+Math.ceil(Q));
		System.out.println("time consumed = "+(endTime - startTime)/1000.0+"s");
	}
	

}
