package minlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class sQminlp_recursive {
	
	int[] 	demandMean;
	double 	holdingCost;
	double 	fixedCost;
	double 	unitCost;
	double 	penaltyCost;
	int 	initialStock;
	int 	partitions;
	double 	Q;
	
	String instanceIdentifier;
	
	public sQminlp_recursive(int[] demandMean, 
				   double holdingCost,
				   double fixedCost,
				   double unitCost,
				   double penaltyCost,
				   int initialStock,
				   int partitions,
				   double Q,
				   String instanceIdentifier) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;
		this.initialStock 	= initialStock;
		this.partitions 	= partitions;
		this.Q				= Q;
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
	
	//where Q is an input
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
        
        IloOplDataSource dataSource = new sQminlp_recursive.sQrecursiveData(oplF);
        opl.addDataSource(dataSource);
        opl.generate();

        cplex.setOut(null);
        
        //double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        //double end = cplex.getCplexImpl().getCplexTime();
        
        if ( status )
        {   
        	double objective = cplex.getObjValue();
        	//double time = end - start;
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
	

	class sQrecursiveData extends IloCustomOplDataSource{
		sQrecursiveData(IloOplFactory oplF){
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
            handler.startElement("initialStock"); handler.addIntItem(initialStock); handler.endElement();
            handler.restartElement("Q");handler.addNumItem(Q); handler.endElement();
            
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
			File f = new File("./sQrecursiveResults.txt"); // relative path, if no file then create a new output.txt
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
		
		long startTime = System.currentTimeMillis();
		
		int[] demandMean = {40};
		double fixedCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		
		int minInventory = 0;
		int maxInventory = 200;
		int[] initialStock = new int[maxInventory - minInventory +1];
		for(int i=0; i<initialStock.length;i++) {
			initialStock[i] = i + minInventory;
		}
		
		int maxQuantity = 200;
		
		int partitions = 10;
		
		double[][] cost_Qi = new double[maxQuantity+1][maxInventory - minInventory + 1];
		

		//for(int q=0; q<=maxQuantity; q++) {
			//writeToText(q, false);
			long singleStartTime = System.currentTimeMillis();
			for(int i=0; i<initialStock.length; i++) {
				try {
					//System.out.println("Onging (i,Q) = ("+(i+minInventory)+", "+q+")");
					sQminlp_recursive sQmodel = new sQminlp_recursive(
							demandMean,
							holdingCost,
							fixedCost,
							unitCost,
							penaltyCost,
							initialStock[i],
							partitions,
							82,
							"sQsinglePoisson_recursive"
							);
					double obj = sQmodel.solveMINLP_recursive("sQsinglePoisson_recursive");
					cost_Qi[83][i] = obj;
					System.out.println("c("+82+", "+initialStock[i]+") = " +cost_Qi[83][i]);

					writeToText(obj,false);
				}catch(IloException e){
					e.printStackTrace();
				}

			}
			//long singleEndTime = System.currentTimeMillis();
			//System.out.println("time for Q = "+q+" is "+(singleEndTime - singleStartTime)/1000+" s");
			//writeToText(t,true);
		//}
		System.out.println();
		
		//System.out.println(Arrays.deepToString(cost_iQ));
		/*
		int[] s = new int[maxQuantity+1];
		for(int q=0; q<=maxQuantity; q++) {
			for(int i=0; i<initialStock.length-q; i++) {
				if(cost_Qi[q][i] - cost_Qi[q][i+q] <= fixedCost) {
					s[q] = i+minInventory;
					break;
				}
			}		
		}
		writeToText(0,true);
		for(int i=0; i<s.length; i++) {
			writeToText(s[i],false);
		}
		System.out.println(Arrays.toString(s));
		*/
		
		long endTime = System.currentTimeMillis();
		System.out.println("timeConsumed = "+(endTime - startTime)/1000 +"s");
		
		
		
		
	}

	


}
