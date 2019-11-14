package minlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

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

public class sQtminlp_recursive {
	
	int[] 		demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double 		initialStock;
	int 		partitions;
	
	String instanceIdentifier;
	
	public sQtminlp_recursive(int[] demandMean, 
				   double holdingCost,
				   double fixedCost,
				   double unitCost,
				   double penaltyCost,
				   double initialStock,
				   int 	  partitions,
				   String instanceIdentifier) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;
		this.initialStock 	= initialStock;
		this.partitions 	= partitions;
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
	
	//where Q[0] is an input
	public double solveMINLP_recursive (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloCplex cplex = oplF.createCplex();
        IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloOplModel opl=oplF.createOplModel(def,cplex);
        cplex.setParam(IloCplex.IntParam.Threads, 8);
        cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
        
        IloOplDataSource dataSource = new sQtminlp_recursive.sQtRecursiveData(oplF);
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
        	opl.postProcess();
        	oplF.end();
        	System.gc();
        	return objective;
        } else {
        	System.out.println("No solution!");
        	oplF.end();
        	System.gc();
        	return Double.NaN;
        } 

	}
	
	
	class sQtRecursiveData extends IloCustomOplDataSource{
		sQtRecursiveData(IloOplFactory oplF){
            super(oplF);
        }

        public void customRead(){
        
         IloOplDataHandler handler = getDataHandler();
         //problem parameters
            handler.startElement("nbmonths"); handler.addIntItem(demandMean.length); handler.endElement();
            handler.startElement("fc"); handler.addNumItem(fixedCost); handler.endElement();
            handler.startElement("h"); handler.addNumItem(holdingCost); handler.endElement();
            handler.startElement("p"); handler.addNumItem(penaltyCost); handler.endElement();
            handler.startElement("v"); handler.addNumItem(unitCost); handler.endElement();
            handler.startElement("meandemand"); handler.startArray();
            for (int j = 0 ; j<demandMean.length ; j++) {handler.addNumItem(demandMean[j]);}
            handler.endArray(); handler.endElement();
            handler.startElement("initialStock"); handler.addNumItem(initialStock); handler.endElement();
            
            //piecewise
            handler.startElement("nbpartitions"); handler.addIntItem(partitions); handler.endElement();
            
            handler.startElement("probabilities"); handler.startArray();
            for (int j = 0 ; j<demandMean.length ; j++){handler.addNumItem(1.0/partitions);}
            handler.endArray(); handler.endElement();
            
            double[][][] coefficients = sQminlp_oneRun.getLamdaMatrix (demandMean, partitions, 100000);
            handler.startElement("lamda_matrix");
            handler.startArray();
            for(int t=0; t<demandMean.length; t++) {
            	for(int j=0; j<=t; j++) {
            		for(int p =0; p<partitions; p++) {
            			handler.addNumItem(coefficients[t][j][p]);
            		}
            	}
            }handler.endArray(); handler.endElement();
        }

	}
	
	public static void writeToText(double value, boolean enter){
		
		try {
			File writename = new File("./sQtRecursiveResults.txt"); // relative path, if no file then create a new output.txt
			writename.createNewFile(); // create new file
			BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			if(!enter) {
			out.write(value+"     "); // \r\n is enter
			}else {
				out.write("\r\n");
			}
			out.flush(); // save soft drive
			out.close(); // close the file
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}	


	public static void main(String[] args) {
		int[] demandMean = {20, 40, 60, 40};
		double fixedCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		double[] initialStock = new double[1001];
		for(int i=0; i<initialStock.length;i++) {
			initialStock[i] = i - 500;
		}
		int partitions = 10;

		for(int i=0; i<1001; i++) {
			writeToText(initialStock[i], false);
			try {
				sQtminlp_recursive sQmodel = new sQtminlp_recursive(
						demandMean,
						holdingCost,
						fixedCost,
						unitCost,
						penaltyCost,
						initialStock[i],
						partitions,
						null
						);
				double obj = sQmodel.solveMINLP_recursive("sQsinglePoisson_recursive");
				System.out.println("c("+initialStock[i]+") = " +obj);
				writeToText(obj,false);
			}catch(IloException e){
				e.printStackTrace();
			}


			writeToText(initialStock[i],true);
		}
	}



}
