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

public class sQminlp_recursive {
	
	double[] 	demandMean;
	double 	holdingCost;
	double 	fixedCost;
	double 	unitCost;
	double 	penaltyCost;
	int 	initialStock;
	int 	partitions;
	
	String instanceIdentifier;
	
	public sQminlp_recursive(double[] demandMean, 
				   double holdingCost,
				   double fixedCost,
				   double unitCost,
				   double penaltyCost,
				   int initialStock,
				   int partitions,
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
	
	public static double costDifference (sQminlpInstance instance, int i1, String consoleFileName, boolean rangedQ) {
		double d = 0;
		int i1_Q = i1 + instance.Q_minlp;
		String modFileName;
		if(rangedQ == true) {
			modFileName = "sQsinglePoisson_recursive_Qranged";
		}else {
			modFileName = "sQsinglePoisson_recursive";
		}
		try {
			sQminlp_recursive sQmodel1 = new sQminlp_recursive(
					instance.demandMean, instance.holdingCost, instance.fixedCost, instance.unitCost, instance.penaltyCost,
					i1, instance.partitions, modFileName);
			double c1 = sQmodel1.solveMINLP_recursive("sQsinglePoisson_recursive");
			sQminlp_recursive sQmodel2 = new sQminlp_recursive(
					instance.demandMean, instance.holdingCost, instance.fixedCost, instance.unitCost, instance.penaltyCost,
					i1_Q, instance.partitions, modFileName);
			double c1_Q = sQmodel2.solveMINLP_recursive(modFileName);
			d = c1 - c1_Q;
			System.out.println("c("+i1+") - c("+(i1_Q)+") = "+d);
			sdp.util.writeText.writeString("c("+i1+") - c("+(i1_Q)+") = "+d, consoleFileName);
		}catch(IloException e){
			e.printStackTrace();
		}
		return  d;
	}

	/** by identifing direction increase or decrease ****************************************************************************/
	public static int computeMINLP_sGreaterK(sQminlpInstance sQminlpInstance, int i1, int currentPeriodIndex, 
											 File FileName, String ConsoleFileName, boolean rangedQ) throws Exception {
		int s = sQminlpInstance.s_sdp[currentPeriodIndex];
		int i2 = i1 + 1;
		double d2 = costDifference (sQminlpInstance, i2, ConsoleFileName, rangedQ);
		if(d2 < sQminlpInstance.fixedCost + sQminlpInstance.unitCost * sQminlpInstance.Q_minlp) {
			s = i2;
			//System.out.println("s found "+s);
			//writeToText((int) s, false,FileName);
			String s_string = Integer.toString(s);
			boolean flag = writeTxtFile(s_string, FileName);
		}else {
			computeMINLP_sGreaterK(sQminlpInstance, i1 + 1, currentPeriodIndex, FileName, ConsoleFileName, rangedQ);
		}
		return s;
	}	
	public static int computeMINLP_sLessK(sQminlpInstance sQminlpInstance, int i1 , int currentPeriodIndex, 
										  File FileName, String ConsoleFileName, boolean rangedQ) throws Exception {
		int s = sQminlpInstance.s_sdp[currentPeriodIndex];
		int i2 = i1 - 1;
		double d2 = costDifference (sQminlpInstance, i2, ConsoleFileName, rangedQ);
		if(d2 > sQminlpInstance.fixedCost + sQminlpInstance.unitCost * sQminlpInstance.Q_minlp) {//next > K, found s
			s = i2 +1;
			//System.out.println("s found = "+s);
			//writeToText((int) s, false,FileName);
			String s_string = Integer.toString(s);
			boolean flag = writeTxtFile(s_string, FileName);
		}else {
			computeMINLP_sLessK(sQminlpInstance, i1 - 1, currentPeriodIndex, FileName, ConsoleFileName, rangedQ);
		}
		return s;
	}	
	public static int computeMINLP_s(double costDifference_s_sdp, sQminlpInstance sQminlpInstance, int i1, int currentPeriodIndex, 
									 File FileName, String consoleFileName, boolean rangedQ) throws Exception {//before this, declare i1 = s_sdp[d]
		if((i1<=sQminlpInstance.minInventory) ||(i1>=sQminlpInstance.maxInventory)) return i1;
		if(costDifference_s_sdp > sQminlpInstance.fixedCost + sQminlpInstance.unitCost * sQminlpInstance.Q_minlp) {
			return computeMINLP_sGreaterK(sQminlpInstance , i1, currentPeriodIndex, FileName, consoleFileName, rangedQ);
		}else {
			return computeMINLP_sLessK(sQminlpInstance, i1, currentPeriodIndex,FileName, consoleFileName, rangedQ);
		}
	}
	/** by identifing direction increase or decrease ****************************************************************************/

	/**by approximating slope==================================================================================================**/
	public static int computeMINLP_s_bySlope(double costDifference_s_sdp, sQminlpInstance instance, int i1, 
			File FileName, int currentPeriodIndex, String consoleFileName, boolean rangedQ) throws Exception {
		//check if s_sdp[t]+-1 is s_minlp[t]
		int i2 = (costDifference_s_sdp > instance.fixedCost + instance.unitCost * instance.Q_minlp)? (i1+1):(i1-1);
		double value_i2 = costDifference (instance, i2, consoleFileName, rangedQ);
		if(	((costDifference_s_sdp > instance.fixedCost + instance.unitCost * instance.Q_minlp)&&(value_i2 < instance.fixedCost + instance.unitCost * instance.Q_minlp))
				||
				((costDifference_s_sdp < instance.fixedCost + instance.unitCost * instance.Q_minlp)&&(value_i2 > instance.fixedCost + instance.unitCost * instance.Q_minlp))
				) {
			String s_string = Integer.toString(i2);
			boolean flag = writeTxtFile(s_string, FileName);
			return i2;
		//--------------------check done. If not, continue
		}else {
			double slope = Math.abs(value_i2 - costDifference_s_sdp);
			if(slope <1) {slope = instance.holdingCost +instance.penaltyCost;}
			System.out.println("slope = "+slope);
			sdp.util.writeText.writeString("slope = "+slope, consoleFileName);
			System.out.println("costDifference_s_sdp = " + costDifference_s_sdp);
			double target = instance.fixedCost + instance.unitCost * instance.Q_minlp;
			System.out.println("target = "+target);
			int steps = (int) Math.round(Math.abs(costDifference_s_sdp - target)/slope);
			System.out.println("steps = "+steps);
			if(costDifference_s_sdp > target) {
				int startPoint = (int) (i1 + steps);
				System.out.println("approximated start point = " + startPoint);
				sdp.util.writeText.writeString("approximated start point = " + startPoint, consoleFileName);
				return computeMINLP_s(costDifference(instance, startPoint, consoleFileName, rangedQ), instance, startPoint, currentPeriodIndex, FileName, consoleFileName, rangedQ);
			}else {
				int startPoint = (int) (i1 - steps);
				System.out.println("approximated start point = " + startPoint);
				sdp.util.writeText.writeString("approximated start point = " + startPoint, consoleFileName);
				return computeMINLP_s(costDifference(instance, startPoint, consoleFileName, rangedQ), instance, startPoint, currentPeriodIndex, FileName, consoleFileName, rangedQ);
			}
			//}

		}
	}
	
	/**by approximating slope==================================================================================================**/

	
	
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
		int minInventory = -100;
		int maxInventory = 100;
	
		int partitions = 10;
		int[] s_sdp = {13,33,54,24};

		int Q_minlp = 84;
		
		boolean rangedQ = false;
		
		int[] s = new int[demandMean.length];
		
		File file = new File("src/main/java/instanceRuns/sQ_minlp/temp.txt");
		for(int d=0; d<demandMeanInput.length; d++) {
			sQminlpInstance sQminlpInstance = new sQminlpInstance(demandMeanInput[d], fixedCost, unitCost, holdingCost, penaltyCost, minInventory, maxInventory,
					partitions, s_sdp, Q_minlp);
			double costDifference_s_sdp = costDifference(sQminlpInstance, s_sdp[d], "src/main/java/instanceRuns/main/sQ_console.txt", rangedQ);

			int i1 = s_sdp[d];
			//s[d] = computeMINLP_s(costDifference_s_sdp, sQminlpInstance, i1, d, file, "src/main/java/instanceRuns/main/sQ_console.txt");
			s[d] = computeMINLP_s_bySlope(costDifference_s_sdp, sQminlpInstance, i1, file, d, "src/main/java/instanceRuns/main/sQ_console.txt", rangedQ);
			System.out.println("==================");
			

			//writeToText(0, true,writeFileName);

	        FileReader fr = new FileReader("src/main/java/instanceRuns/sQ_minlp/temp.txt");
	        BufferedReader br = new BufferedReader(fr);
	        String read = "";
	        read = br.readLine();
	        s[d] = Integer.parseInt(read);
		}

		System.out.println(Arrays.toString(s));

		long end = System.currentTimeMillis();
		System.out.println(end-start);
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

    
    
    //not used


}