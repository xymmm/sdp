package minlp_Poisson;

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
import minlp_Normal.sQminlpNormalSolution;
import minlp_Normal.sQminlpNormal_oneRun;

public class sQminlp_oneRun{

	double[] 	demandMean;
	double 	holdingCost;
	double 	fixedCost;
	double 	unitCost;
	double 	penaltyCost;
	double 	initialInventoryLevel;
	int 	partitions;
	
	String instanceIdentifier;
	
	public sQminlp_oneRun(double[] demandMean, 
				   double holdingCost,
				   double fixedCost,
				   double unitCost,
				   double penaltyCost,
				   double initialStock,
				   int partitions,
				   String instanceIdentifier) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;
		this.initialInventoryLevel 	= initialStock;
		this.partitions 	= partitions;
	}
	
	/** compute lamda matrix for linearisation of Poisson distribution **/
	public static double[][][] getLamdaMatrix (double[] demandMean, int partitions, int nbSamples){
		double[][][] coefficients = PoissonPiecewise.possionPiecewisePartitions.lamdaMatrix(demandMean, partitions, nbSamples);
		return coefficients;
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
	
	public sQminlpPoissonSolution solveMINLP_oneRun (String model_name) throws IloException{		
		IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloCplex cplex = oplF.createCplex();
        IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloOplModel opl=oplF.createOplModel(def,cplex);
        cplex.setParam(IloCplex.IntParam.Threads, 8);
        cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
        
        IloOplDataSource dataSource = new sQminlp_oneRun.sQsingleData(oplF);
        opl.addDataSource(dataSource);
        opl.generate();

        cplex.setOut(null);
        
        double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        double end = cplex.getCplexImpl().getCplexTime();
        
        if (status){   
        	double Q = cplex.getValue(opl.getElement("Q").asNumVar());
			double[] purchase = new double[demandMean.length];
			for(int t = 0; t < purchase.length; t++){
				purchase[t] = cplex.getValue(opl.getElement("purchaseDouble").asNumVarMap().get(t+1));
			}
        	opl.postProcess();
        	oplF.end();
        	System.gc();
        	return new sQminlpPoissonSolution(purchase, Q);
        } else {
        	System.out.println("No solution!");
        	//opl.end();
        	oplF.end();
        	//errHandler.end();
        	//cplex.end();
        	System.gc();
        	return new sQminlpPoissonSolution(null, Double.NaN);
        } 

	}
	
	class sQsingleData extends IloCustomOplDataSource{
		sQsingleData(IloOplFactory oplF){
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
            handler.startElement("initialStock"); handler.addNumItem(initialInventoryLevel); handler.endElement();            
            //piecewise
            handler.startElement("nbpartitions"); handler.addIntItem(partitions); handler.endElement();           
            double partitionProb = 1.0/partitions;
            handler.startElement("prob"); handler.startArray();
            for (int j = 0 ; j<partitions; j++){handler.addNumItem(partitionProb);}
            handler.endArray(); handler.endElement();           
			double[][][] coefficients = sQminlp_oneRun.getLamdaMatrix (demandMean, partitions, 100000);
			handler.startElement("lamda_matrix");
			handler.startArray();
			for(int t=0; t<demandMean.length; t++) {
				handler.startArray();
				for(int j=0; j<demandMean.length; j++) {
					handler.startArray();
					for(int p = 0; p<partitions; p++) {
						handler.addNumItem(coefficients[t][j][p]);
					}
					handler.endArray(); 
				}
				handler.endArray(); 
			}
			handler.endArray(); 
			handler.endElement();
        }		
	}
	
	public static double[] sQPoissonMinlpSchedule(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost,
			double initialStock, int partitions, boolean Qranged) {
		double[] schedule = new double[demandMean.length];
		try {
			sQminlp_oneRun sQmodel = new sQminlp_oneRun(demandMean, 
					   holdingCost,
					   fixedCost,
					   unitCost,
					   penaltyCost,
					   initialStock,
					   partitions,
					null
					);
			if(Qranged == true) {
				sQminlpPoissonSolution solution = sQmodel.solveMINLP_oneRun("sQsinglePoisson_Qranged");
				for(int t=0; t<demandMean.length; t++) {
					schedule[t] = solution.Q * solution.purchase[t];
				}
			}else {
				sQminlpPoissonSolution solution = sQmodel.solveMINLP_oneRun("sQsinglePoisson");
				for(int t=0; t<demandMean.length; t++) {
					schedule[t] = solution.Q * solution.purchase[t];
				}
			}
			
		}catch(IloException e){
			e.printStackTrace();
		}
		return schedule;
	}

	
	
	public static void main(String[] args) {		
		
		double[] demandMean = {20, 40, 60, 40};
		double fixedCost = 100;//5,10,20
		double unitCost = 0;//0,1
		double holdingCost = 1;
		double penaltyCost = 10;//2,3
		double initialStock = 0;
		int partitions = 4;
		
		boolean Qranged = false;
		
		double[] schedule = sQPoissonMinlpSchedule(demandMean, fixedCost, unitCost, holdingCost, penaltyCost,
			 initialStock, partitions, Qranged);
		System.out.println(Arrays.toString(schedule));
		
		
	}
	
	




	
	

}
