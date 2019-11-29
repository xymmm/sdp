package minlp;

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
	
	public static void writeToText(double value, boolean enter, String fileName){
		FileWriter fw = null;
		try {
			File f = new File(fileName); // relative path, if no file then create a new output.txt
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
	
	
	public static double costDifference (sQminlpInstance instance, int i1) {
		double d = 0;
		int i1_Q = i1 + instance.Q_minlp;
		try {
			sQminlp_recursive sQmodel1 = new sQminlp_recursive(
					instance.demandMean, instance.holdingCost, instance.fixedCost, instance.unitCost, instance.penaltyCost,
					i1, instance.partitions, instance.Q_minlp, "sQsinglePoisson_recursive"
					);
			double c1 = sQmodel1.solveMINLP_recursive("sQsinglePoisson_recursive");
			sQminlp_recursive sQmodel2 = new sQminlp_recursive(
					instance.demandMean, instance.holdingCost, instance.fixedCost, instance.unitCost, instance.penaltyCost,
					i1_Q, instance.partitions, instance.Q_minlp, "sQsinglePoisson_recursive"
					);
			double c1_Q = sQmodel2.solveMINLP_recursive("sQsinglePoisson_recursive");
			d = c1 - c1_Q;
			//System.out.println("c("+i1+") - c("+(i1_Q)+") = "+d);
		}catch(IloException e){
			e.printStackTrace();
		}
		return  d;
	}
	
	public static int computeMINLP_sGreaterK(sQminlpInstance sQminlpInstance, int i1, int currentPeriodIndex, File FileName) throws Exception {
		int s = sQminlpInstance.s_sdp[currentPeriodIndex];
		int i2 = i1 + 1;
		double d2 = costDifference (sQminlpInstance, i2);
		if(d2 < sQminlpInstance.fixedCost) {
			s = i2;
			//System.out.println("s found "+s);
			//writeToText((int) s, false,FileName);
			String s_string = Integer.toString(s);
			boolean flag = writeTxtFile(s_string, FileName);

		}else {
			computeMINLP_sGreaterK(sQminlpInstance, i1 + 1, currentPeriodIndex, FileName);
		}
		return s;
	}
	
	public static int computeMINLP_sLessK(sQminlpInstance sQminlpInstance, int i1 , int currentPeriodIndex, File FileName) throws Exception {
		int s = sQminlpInstance.s_sdp[currentPeriodIndex];
		int i2 = i1 - 1;
		double d2 = costDifference (sQminlpInstance, i2);
		if(d2 > sQminlpInstance.fixedCost) {//next > K, found s
			s = i2 +1;
			//System.out.println("s found = "+s);
			//writeToText((int) s, false,FileName);
			String s_string = Integer.toString(s);
			boolean flag = writeTxtFile(s_string, FileName);
		}else {
			computeMINLP_sLessK(sQminlpInstance, i1 - 1, currentPeriodIndex, FileName);
		}
		return s;
	}
	
	public static int computeMINLP_s(sQminlpInstance sQminlpInstance, int i1, int currentPeriodIndex, File FileName) throws Exception {//before this, declare i1 = s_sdp[d]
		if(costDifference (sQminlpInstance, i1) > sQminlpInstance.fixedCost) {
			return computeMINLP_sGreaterK(sQminlpInstance , i1, currentPeriodIndex, FileName);
		}else {
			return computeMINLP_sLessK(sQminlpInstance, i1, currentPeriodIndex,FileName);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		String writeFileName = "src/main/java/instanceRuns/sQ_minlp/temp.txt";
		
		//writeToText(0,true, writeFileName);
			
		int[] demandMean = {20,40,60,40};
		int[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		
		double fixedCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
	
		int partitions = 10;
		int[] s_sdp = {12, 32, 55, 23};//{13, 33, 54, 24};

		int Q_minlp = 83;
		
		int[] s = new int[demandMean.length];
		File file = new File("src/main/java/instanceRuns/sQ_minlp/temp.txt");
		for(int d=0; d<demandMeanInput.length; d++) {
			sQminlpInstance sQminlpInstance = new sQminlpInstance(demandMeanInput[d], fixedCost, unitCost, holdingCost, penaltyCost, 
					partitions, s_sdp,Q_minlp);

			int i1 = s_sdp[d];
			s[d] = computeMINLP_s(sQminlpInstance, i1, d, file);

			//writeToText(0, true,writeFileName);

		}
		System.out.println(Arrays.toString(s));

		
		//writeToText(0, true,writeFileName);
	
		/** for multiple instances, put writeToText(·,true) at the end**/
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


}
