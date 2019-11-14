package minlp;

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


public class sQminlp {

	int[] demandMean;
	double holdingCost;
	double fixedCost;
	double unitCost;
	double penaltyCost;
	double initialStock;
	int partitions;
	
	String instanceIdentifier;
	
	public sQminlp(int[] demandMean, 
				   double holdingCost,
				   double fixedCost,
				   double unitCost,
				   double penaltyCost,
				   double initialStock,
				   int partitions,
				   String instanceIdentifier) {
		this.demandMean = demandMean;
		this.holdingCost =  holdingCost;
		this.fixedCost = fixedCost;
		this.unitCost = unitCost;
		this.penaltyCost = penaltyCost;
		this.initialStock = initialStock;
		this.partitions = partitions;
	}
	
	/** compute lamda matrix for linearisation of Poisson distribution **/
	public static double[][][] getLamdaMatrix (int[] demandMean, int partitions, int nbSamples){
		double[][][] coefficients = PoissonPiecewise_sQ.possionPiecewisePartitions.lamdaMatrix(demandMean, partitions, nbSamples);
		return coefficients;
	}
	
	private InputStream getMINLPmodelStream(File file) {
	      FileInputStream is = null;
	      try{
	         is = new FileInputStream(file);
	      }catch(IOException e){
	         e.printStackTrace();
	      }
	      return is;
	}
	
	public double solveMINLP (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloCplex cplex = oplF.createCplex();
        IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/backorders/"+model_name+".mod")),model_name);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloOplModel opl=oplF.createOplModel(def,cplex);
        cplex.setParam(IloCplex.IntParam.Threads, 8);
        cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
        
        IloOplDataSource dataSource = new sQminlp.MyData(oplF);
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
        	double Q = cplex.getValue((IloNumVar) opl.getElement("Q"));
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
	
	
	class MyData extends IloCustomOplDataSource{
        MyData(IloOplFactory oplF){
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
            handler.startElement("expDemand"); handler.startArray();
            for (int j = 0 ; j<demandMean.length ; j++) {handler.addNumItem(demandMean[j]);}
            handler.endArray(); handler.endElement();
            handler.startElement("initialStock"); handler.addNumItem(initialStock); handler.endElement();
            
            //piecewise
            handler.startElement("nbpartitions"); handler.addIntItem(partitions); handler.endElement();
            
            handler.startElement("probabilities"); handler.startArray();
            for (int j = 0 ; j<demandMean.length ; j++){handler.addNumItem(1.0/partitions);}
            handler.endArray(); handler.endElement();
            
            double[][][] coefficients = getLamdaMatrix (demandMean, partitions, 100000);
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

	
	
	public static void main(String[] args) {
		int[] demandMean = {20, 40, 60, 40};
		double fixedCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		double initialStock = 0;
		int partitions = 10;
		
		double Q = Double.NaN;
		
		try {
			sQminlp sQmodel = new sQminlp(
					demandMean,
					holdingCost,
					fixedCost,
					unitCost,
					penaltyCost,
					initialStock,
					partitions,
					null
					);
			Q = sQmodel.solveMINLP("sQsinglePoisson");
		}catch(IloException e){
	         e.printStackTrace();
	    }
		System.out.println("Q = "+Q);
	}
}
