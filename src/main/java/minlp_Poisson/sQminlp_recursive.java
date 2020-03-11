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
	/***********************************************************************************************************************************/
	/*****************************************BINARY SEARCH*****************************************************************************/
	/***********************************************************************************************************************************/
	public static double costDifference (sQminlp_recursive sQmodel, double inventoryLevel, boolean rangedQ) {
		double inventoryPlusQ = inventoryLevel + sQmodel.Q;
		double difference = 0;		
		String FileName = null;	
		if(rangedQ == true) {
			FileName = "sQsinglePoisson_recursive_Qranged";
		}else {
			FileName = "sQsinglePoisson_recursive";
		}		
		try {
			sQminlp_recursive sQmodelInput = new sQminlp_recursive(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryLevel, sQmodel.partitions, null, sQmodel.Q);
			double c1 = sQmodelInput.solveMINLP_recursive(FileName);
			//System.out.println("cost of "+inventoryLevel+"\t"+c1);
			sQminlp_recursive sQmodelPlusQ = new sQminlp_recursive(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryPlusQ, sQmodel.partitions, null, sQmodel.Q);
			double c2 = sQmodelPlusQ.solveMINLP_recursive(FileName);
			//System.out.println("cost of "+inventoryPlusQ+"\t"+c2);
			difference = c1 - c2;
		}catch(IloException e){
			e.printStackTrace();
		}
		return  difference;
	}

	public static void binarySearch(double initialInputLevel, double pace, sQminlp_recursive sQmodel,
			double costLeft, double costRight, boolean rangedQ) throws Exception {
		File tempFile = new File ("src/main/java/minlp_Poisson/tempRminlp.txt"); //to save reorder point as a string in the file
		double orderingCost = (sQmodel.Q==0.0)? 0 : (sQmodel.fixedCost + sQmodel.Q*sQmodel.unitCost);
		double i1 = initialInputLevel; 
		if( (costLeft - orderingCost)*(costRight - orderingCost)<0 ) {
			double levelBinary = i1 + Math.floor(0.5*pace); 
			double costBinary = costDifference(sQmodel, levelBinary, rangedQ);
			//judge if costBinary > orderingCost or not
			if(costBinary > orderingCost) {//[binary, input]
				double costBinaryClose = costDifference (sQmodel, levelBinary + 1, rangedQ);
				if((costBinaryClose < orderingCost)||(levelBinary == i1 + pace)) {
					//System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					//System.out.println();
				}else {
					//System.out.println("binary search proceeds, right interval.");
					binarySearch(levelBinary, Math.round(0.5*pace), sQmodel, costBinary, costRight, rangedQ);
				}
			}else {//[input, binary]
				double costBinaryClose = costDifference (sQmodel, levelBinary - 1, rangedQ);
				if((costBinaryClose > orderingCost)||(levelBinary == i1)) {
					//System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					//System.out.println();
				}else {
					//System.out.println("binary search proceeds, left interval.");
					binarySearch(i1, Math.round(0.5*pace), sQmodel, costLeft, costBinary, rangedQ);
				}				
			}
		}else {//pace is not large/small enough
			if( costLeft < orderingCost) {
				//System.out.println("Cost of initial input invnetory is too small, move left");
				binarySearch(i1 - pace, pace, sQmodel,costDifference(sQmodel, i1-pace, rangedQ), costDifference(sQmodel, i1, rangedQ), rangedQ);
			}else {
				//System.out.println("Cost of initial input invnetory is too large, move right");
				binarySearch(i1 + pace, pace, sQmodel, costDifference(sQmodel, i1, rangedQ), costDifference(sQmodel, i1+pace, rangedQ), rangedQ);
			}
		}
	}
	
	public static double[] reorderPoint_sQheuristic(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost, 
			double initialStock, int partitions, double pace, double[] schedule, boolean rangedQ) throws Exception {
		//compute reorder point
		double[] reorderPoint = new double[demandMean.length];
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		for(int t=0; t<demandMean.length; t++) {
			if((schedule[t] < 1.0)||(schedule[t] >= ((rangedQ) ? 9 : 500))) {
				reorderPoint[t] = Double.NEGATIVE_INFINITY;
				//System.out.println("no replenishmeng placed.");
				//System.out.println();
			}else {
				try {
					sQminlp_recursive sQmodel = new sQminlp_recursive(
							demandMeanInput[t], holdingCost, fixedCost,  unitCost, penaltyCost, 
							initialStock,  partitions, null, schedule[t]);
					//System.out.println("orderingCost = " + (sQmodel.fixedCost + sQmodel.Q*sQmodel.unitCost));
					double costLeft = costDifference(sQmodel, initialStock, rangedQ); //System.out.println("costLeft = "+ costLeft);
					double costRight = costDifference(sQmodel, initialStock + pace, rangedQ); //System.out.println("costRight = "+ costRight);
					binarySearch(initialStock, pace, sQmodel, costLeft, costRight, rangedQ);
				}catch(IloException e){
					e.printStackTrace();
				}
				File tempFile = new File ("src/main/java/minlp_Poisson/tempRminlp.txt"); 		
				FileReader fr = new FileReader(tempFile);
				BufferedReader br = new BufferedReader(fr);
				String read = "";
				read = br.readLine();
				reorderPoint[t] = Double.parseDouble(read);
			}
		}
		return reorderPoint;
	}
	

//**************************************************************************************************
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
