package minlp_Normal;

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
import minlp_Poisson.sQminlpInstance;
import minlp_Poisson.sQminlp_recursive;

public class sQminlpNormal_recursive {

	double[] 	demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double 		initialStock;
	double 		stdParameter;
	int 		partitions;
	double[] 	means;
	double[] 	piecewiseProb;
	double 		error;
	double		Q;				//given an optimal Q from MINLP, solve recursively for reorder point


	String instanceIdentifier;

	public sQminlpNormal_recursive(double[] demandMean, double holdingCost, double fixedCost,  double unitCost, double penaltyCost, 
			double initialStock, double stdParameter, 
			int partitions, double[] means, double[] piecewiseProb, double error,
			String instanceIdentifier, double Q) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;		
		this.initialStock 	= initialStock;
		this.stdParameter 	= stdParameter;

		this.partitions 	= partitions;		
		this.means = means;
		this.piecewiseProb = piecewiseProb;
		this.error = error;

		this.Q = Q;
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
	public double solveMINLP_recursive_Normal (String model_name) throws IloException{		
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);

		IloOplDataSource dataSource = new sQminlpNormal_recursive.sQrecursiveData(oplF);
		opl.addDataSource(dataSource);
		opl.generate();

		cplex.setOut(null);

		boolean status =  cplex.solve();

		if ( status ){   
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


	/**import data to .mod**/
	class sQrecursiveData extends IloCustomOplDataSource{
		sQrecursiveData(IloOplFactory oplF){
			super(oplF);
		}

		public void customRead(){

			IloOplDataHandler handler = getDataHandler();
			//problem parameters
			//demandMean, holdingCost, fixedCost,  unitCost, penaltyCost, 
			//initialStock, stdParameter, 
			//partitions,  means, piecewiseProb, error

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
	/***********************************************************************************************************************************/
	/*****************************************BINARY SEARCH*****************************************************************************/
	/***********************************************************************************************************************************/
	public static double costDifference (sQminlpNormal_recursive sQmodel, double inventoryLevel) {
		double inventoryPlusQ = inventoryLevel + sQmodel.Q;

		double difference = 0;

		try {
			sQminlpNormal_recursive sQmodelInput = new sQminlpNormal_recursive(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryLevel, sQmodel.stdParameter, 
					sQmodel.partitions,  sQmodel.means, sQmodel.piecewiseProb, sQmodel.error,
					null, sQmodel.Q);
			double c1 = sQmodelInput.solveMINLP_recursive_Normal("sQsingleNormal_recursive");

			sQminlpNormal_recursive sQmodelPlusQ = new sQminlpNormal_recursive(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryPlusQ, sQmodel.stdParameter, 
					sQmodel.partitions,  sQmodel.means, sQmodel.piecewiseProb, sQmodel.error,
					null, sQmodel.Q);
			double c2 = sQmodelPlusQ.solveMINLP_recursive_Normal("sQsingleNormal_recursive");
			difference = c1 - c2;
			//System.out.println("c("+inventoryLevel+") - c("+inventoryPlusQ+") = "+difference);
		}catch(IloException e){
			e.printStackTrace();
		}
		return  difference;
	}



	public static void binarySearch(double initialInputLevel, double pace, sQminlpNormal_recursive sQmodel) throws Exception {

		File tempFile = new File ("src/main/java/minlp_Normal/tempRminlp.txt"); //to save reorder point as a string in the file
		double orderingCost = sQmodel.fixedCost + sQmodel.Q*sQmodel.unitCost;

		double i1 = initialInputLevel; 

		double cost1 = costDifference (sQmodel, i1);
		double cost2 = costDifference (sQmodel, i1+pace);
		System.out.println("cost("+i1+") = " + cost1 +"\t" + "cost("+(i1+pace)+") = "+cost2);

		if( (cost1 - orderingCost)*(cost2 - orderingCost)<0 ) {
			double levelBinary = Math.floor(0.5*pace + i1);
			double costBinary = costDifference(sQmodel, levelBinary);
			System.out.println("cost(binary="+levelBinary+")= " + costBinary);

			//judge if costBinary > orderingCost or not
			if(costBinary > orderingCost) {//[input, binary]
				double costBinaryClose = costDifference (sQmodel, levelBinary + 1);
				if(costBinaryClose < orderingCost) {
					System.out.println("cost (binary + 1) = " +costBinaryClose);
					String s_string = Double.toString(levelBinary);
					boolean flag = writeTxtFile(s_string, tempFile);
					System.out.println();
				}else {
					binarySearch(levelBinary, Math.round(0.5*pace), sQmodel);
					System.out.println("binary search proceeds.");
				}
			}else {//[binary, intput+Q]
				double costBinaryClose = costDifference (sQmodel, levelBinary - 1);
				if(costBinaryClose > orderingCost) {
					System.out.println("cost (binary + 1) = " +costBinaryClose);
					String s_string = Double.toString(levelBinary);
					boolean flag = writeTxtFile(s_string, tempFile);
					System.out.println();
				}else {
					binarySearch(i1+pace, Math.round(0.5*pace), sQmodel);
					System.out.println("binary search proceeds.");
				}				
			}
		}else {//pace is not large/small enough
			if( cost1 < orderingCost) {
				binarySearch(i1 - pace, pace, sQmodel);
				System.out.println("Cost of initial input invnetory is too small, move left");
			}else {
				binarySearch(i1 + pace, pace, sQmodel);
				System.out.println("Cost of initial input invnetory is too large, move right");
			}
		}

	}


	/*** @throws Exception ****************************************************************************************************/


	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();

		double[] demandMean = {20, 40, 60, 40};
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);
		double fixedCost = 100;//5,10,20
		double unitCost = 1;//0,1
		double holdingCost = 1;
		double penaltyCost = 10;//2,3
		double initialStock = 0;
		double stdParameter = 0.25;

		double Q = 85;
		/*
		int partitions = 4;
		double[] piecewiseProb = {0.187555, 0.312445, 0.312445, 0.187555};
		double[] means = {-1.43535, -0.415223, 0.415223, 1.43535};
		double error = 0.0339052;
		 */

		int partitions = 10;
		double[] piecewiseProb = {0.04206108420763477, 0.0836356495308449, 0.11074334596058821, 0.1276821455299152, 0.13587777477101692, 0.13587777477101692, 0.1276821455299152, 0.11074334596058821, 0.0836356495308449, 0.04206108420763477};
		double[] means = {-2.133986195498256, -1.3976822972668839, -0.918199946431143, -0.5265753462727588, -0.17199013069262026, 0.17199013069262026, 0.5265753462727588, 0.918199946431143, 1.3976822972668839, 2.133986195498256};
		double error = 0.005885974956458359;

		double pace = 100;
		
		double[] s = new double[demandMean.length];

		for(int t=0; t<demandMean.length; t++) {

			try {
				sQminlpNormal_recursive sQmodel = new sQminlpNormal_recursive(
						demandMeanInput[t], holdingCost, fixedCost,  unitCost, penaltyCost, 
						initialStock, stdParameter, 
						partitions,  means, piecewiseProb, error,
						null, Q
						);
				System.out.println("orderingCost = " + (sQmodel.fixedCost + sQmodel.Q*sQmodel.unitCost));
				binarySearch(initialStock, pace, sQmodel);
			}catch(IloException e){
				e.printStackTrace();
			}

			File tempFile = new File ("src/main/java/minlp_Normal/tempRminlp.txt"); 		
			FileReader fr = new FileReader(tempFile);
			BufferedReader br = new BufferedReader(fr);
			String read = "";
			read = br.readLine();
			s[t] = Double.parseDouble(read);
		}
		long endTime = System.currentTimeMillis();		
		System.out.println("time consumed = "+(endTime - startTime)/1000+"s");
		
		System.out.println(Arrays.toString(s));
	}







	/*************************************************************************************/
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





