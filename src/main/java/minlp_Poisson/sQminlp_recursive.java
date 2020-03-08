package minlp_Poisson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
import minlp_Normal.sQminlpNormal_recursive;

public class sQminlp_recursive {
	
	double[] 	demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double 		initialStock;
	int 		partitions;

	double		Q;				//given an optimal Q from MINLP, solve recursively for reorder point
	
	String instanceIdentifier;
	
	public sQminlp_recursive(double[] demandMean, double holdingCost, double fixedCost, double unitCost, double penaltyCost,
				   double initialStock,
				   int partitions,
				   String instanceIdentifier, double Q) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;
		this.initialStock 	= initialStock;
		this.partitions 	= partitions;
		this.Q 				= Q;
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
	
	/**main solving block *****************************************************/
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
		boolean status =  cplex.solve();
		if(status){   
			double objective = cplex.getObjValue();
			opl.postProcess();
			//opl.end();
			oplF.end();
			//errHandler.end();
			//cplex.end();
			System.gc();
			return objective;
		}else{
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

			handler.startElement("initialStock"); handler.addNumItem(initialStock); handler.endElement();
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
	
	
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		String writeFileName = "src/main/java/instanceRuns/sQ_minlp/temp.txt";
		
		//writeToText(0,true, writeFileName);
			
		double[] demandMean = {20, 40, 60, 40};
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		
		double fixedCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;

		
		double initialStock = 0;
	
		int partitions = 10;

		int Q_minlp = 84;
		

		String model_name = "sQsinglePoisson_recursive";
		
		sQminlp_recursive sQmodelInput = new sQminlp_recursive(
				demandMean, holdingCost, fixedCost,  unitCost, penaltyCost, 
				initialStock, 
				partitions,  
				null, 84);
		double c1 = sQmodelInput.solveMINLP_recursive("sQsinglePoisson_recursive");
		System.out.println(c1);
		

	}

	
    public static boolean writeTxtFile(String content,File fileName)throws Exception{
        RandomAccessFile mm=null;
        boolean flag=false;
        FileOutputStream fileOutputStream=null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(content.getBytes("gbk"));
            fileOutputStream.close();
            flag=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    
    
    //not used


}
