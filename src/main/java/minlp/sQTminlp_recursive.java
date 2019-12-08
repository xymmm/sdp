package minlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;

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

public class sQTminlp_recursive {
	
	int[] 		demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	int 		initialStock;
	int 		partitions;
	
	String instanceIdentifier;
	
	public sQTminlp_recursive(int[] demandMean, 
				   double holdingCost,
				   double fixedCost,
				   double unitCost,
				   double penaltyCost,
				   int initialStock,
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
        
        IloOplDataSource dataSource = new sQTminlp_recursive.sQtRecursiveData(oplF);
        opl.addDataSource(dataSource);
        opl.generate();

        cplex.setOut(null);
        
        boolean status =  cplex.solve();        
        if ( status )
        {   
        	double objective = cplex.getObjValue();
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
            for (int j = 0 ; j<demandMean.length ; j++) {handler.addIntItem(demandMean[j]);}
            handler.endArray(); handler.endElement();
            handler.startElement("initialStock"); handler.addIntItem(initialStock); handler.endElement();
            
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
	
	public static void writeToText(double value, boolean enter){
		FileWriter fw = null;
		try {
			File f = new File("./sQtRecursiveResults_reorderPoints.txt"); // relative path, if no file then create a new output.txt
			fw = new FileWriter(f, true);//true, continue to write
		}catch(IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		if(!enter) {
			pw.print(value+"  "); // \r\n is enter
		}else {
			pw.print("\r\n");
		}
		pw.flush(); // save soft drive
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}


	public static void main(String[] args) {
		int[] demandMean = {2,4,6,4};
		int[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		double fixedCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;
		int partitions = 20;
		int minInventory = -10;
		int maxInventory = 50;
		int[] initialStock = new int[maxInventory - minInventory +1];
		for(int i=0; i<initialStock.length;i++) {
			initialStock[i] = i + minInventory;
		}
		

		
		double[] cost_i = new double[initialStock.length];
		long startTime = System.currentTimeMillis();
		
		for(int t=0; t<demandMean.length; t++) {
			long singleStartTime = System.currentTimeMillis();
			for(int i=0; i<initialStock.length; i++) {
				//writeToText(initialStock[i], false);
				try {
					sQTminlp_recursive sQmodel = new sQTminlp_recursive(
							demandMeanInput[t],
							holdingCost,
							fixedCost,
							unitCost,
							penaltyCost,
							initialStock[i],
							partitions,
							"sQtPoisson_recursive"
							);
					double obj = sQmodel.solveMINLP_recursive("sQtPoisson_recursive");
					//System.out.println("c("+initialStock[i]+") = " +obj);
					System.out.println(obj);
					cost_i[i] = obj;
					//writeToText(obj,false);
				}catch(IloException e){
					e.printStackTrace();
				}
				//writeToText(initialStock[i],true);
			}
			//sdp.util.plotOneDimensionArray.plotCostGivenQGivenStage(cost_i, initialStock, "Opening inventory level", "Approximated expected cost", "Approximated expected total cost by sQt-MINLP");
			int s = 0;
			int globalMinimumIndex = sdp.util.globalMinimum.getGlobalMinimumJavaIndex(cost_i);
			double targetCost = cost_i[globalMinimumIndex] + fixedCost;
			for(int i=0; i<cost_i.length;i++) {
				if(cost_i[i]<targetCost) {
					s = i + minInventory;
					System.out.println("s("+(t+1)+") = "+s);
					writeToText(s,false);
					break;
				}
			}
			long singleEndTime = System.currentTimeMillis();
			System.out.println("time consumed = "+(singleEndTime - singleStartTime)/1000+" s");
			System.out.println("cost(s="+s+") = "+ cost_i[s-minInventory]+", with global minimum = cost_i("+(globalMinimumIndex+minInventory)+") = "+cost_i[globalMinimumIndex]);
		}
		
		System.out.println();
		long endTime = System.currentTimeMillis();
		System.out.println("time consumed = "+(endTime - startTime)/1000+" s");
		
		
		
		
	}



}
