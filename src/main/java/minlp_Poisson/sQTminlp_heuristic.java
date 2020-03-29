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
import java.math.BigDecimal;
import java.util.Arrays;

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
import minlp_Normal.sQTminlpNormal_heuristic;
import minlp_Normal.sQminlpNormal_recursive;

public class sQTminlp_heuristic {

	double[] 	demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double 		initialStock;
	int 		partitions;
	double[]	optQ;		//future Q schedule

	String instanceIdentifier;

	public sQTminlp_heuristic(double[] demandMean, 
			double holdingCost,
			double fixedCost,
			double unitCost,
			double penaltyCost,
			double initialStock,
			int 	  partitions,
			String instanceIdentifier, 
			double[] optQ) {
		this.demandMean 	= demandMean;
		this.holdingCost 	= holdingCost;
		this.fixedCost 		= fixedCost;
		this.unitCost 		= unitCost;
		this.penaltyCost 	= penaltyCost;
		this.initialStock 	= initialStock;
		this.partitions 	= partitions;
		this.optQ 			= optQ;
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
	//where Q[0] is an input
	public double solveMINLP_heuristic (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);       
		IloOplDataSource dataSource = new sQTminlp_heuristic.sQtRecursiveData(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);      
		boolean status =  cplex.solve();        
		if ( status ){   
			double objective = cplex.getObjValue();
			opl.postProcess();
			oplF.end();
			System.gc();
			return objective;
		}else{
			System.out.println("No solution!");
			oplF.end();
			System.gc();
			return Double.NaN;
		} 

	}


	public double solve_lastPeriod (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);       
		IloOplDataSource dataSource = new sQTminlp_heuristic.sQtRecursiveData(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);      
		boolean status =  cplex.solve();        
		if ( status ){   
			double objective = cplex.getObjValue();
			opl.postProcess();
			oplF.end();
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
			double partitionProb = 1.0/partitions;
			handler.startElement("prob"); handler.startArray();
			for (int j = 0 ; j<partitions; j++){handler.addNumItem(partitionProb);}
			handler.endArray(); handler.endElement();
			//picecwise - lambda matrix
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
	/*****************************************BINARY SEARCH for sQt heuristic***********************************************************/
	/***********************************************************************************************************************************/
	public static double costDifferencesQtHeuristic (sQTminlp_heuristic sQmodel, double inventoryLevel, double currentQ, boolean rangedQ) {
		double inventoryPlusQ = inventoryLevel + currentQ;
		double difference = 0;		

		String FileName = null;	
		if(rangedQ == true) {
			FileName = "sQtPoisson_recursive_Qranged";
		}else {
			FileName = "sQtPoisson_recursive";
		}

		try {
			sQTminlp_heuristic sQmodelInput = new sQTminlp_heuristic(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryLevel, sQmodel.partitions, 
					null, sQmodel.optQ);
			double c1 = sQmodelInput.solveMINLP_heuristic(FileName);
			sQTminlp_heuristic sQmodelPlusQ = new sQTminlp_heuristic(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryPlusQ, sQmodel.partitions, null, sQmodel.optQ);
			double c2 = sQmodelPlusQ.solveMINLP_heuristic(FileName);
			difference = c1 - c2;
		}catch(IloException e){
			e.printStackTrace();
		}
		return  difference;
	}

	public static void binarySearchsQtHeuristic(double initialInputLevel, double pace, sQTminlp_heuristic sQmodel,
			double costLeft, double costRight, double currentQ, boolean rangedQ) throws Exception {	

		File tempFile = new File ("src/main/java/minlp_Normal/tempRminlp.txt"); //to save reorder point as a string in the file
		double orderingCost = (currentQ==0.0)? 0 : (sQmodel.fixedCost + currentQ*sQmodel.unitCost);
		double i1 = initialInputLevel; 
		System.out.println("cost("+i1+") = " + costLeft +"\t" + "cost("+(i1+pace)+") = "+costRight);

		if( (costLeft - orderingCost)*(costRight - orderingCost)<0 ) {
			double levelBinary = i1 + Math.floor(0.5*pace); 
			double costBinary = costDifferencesQtHeuristic(sQmodel, levelBinary, currentQ, rangedQ);
			System.out.println("cost(binary="+levelBinary+") = " + costBinary);

			//judge if costBinary > orderingCost or not
			if(costBinary > orderingCost) {//[binary, input]
				double costBinaryClose = costDifferencesQtHeuristic (sQmodel, levelBinary + 1, currentQ, rangedQ);
				if((costBinaryClose < orderingCost)||(levelBinary == i1 + pace)) {
					System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					System.out.println();
				}else {
					//System.out.println("binary search proceeds, right interval.");
					binarySearchsQtHeuristic(levelBinary, Math.round(0.5*pace), sQmodel, costBinary, costRight, currentQ, rangedQ);
				}
			}else {//[input, binary]
				double costBinaryClose = costDifferencesQtHeuristic (sQmodel, levelBinary - 1, currentQ, rangedQ);
				if((costBinaryClose > orderingCost)||(levelBinary == i1)) {
					System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					System.out.println();
				}else {
					System.out.println("binary search proceeds, left interval.");
					binarySearchsQtHeuristic(i1, Math.round(0.5*pace), sQmodel, costLeft, costBinary, currentQ, rangedQ);
				}				
			}
		}else {//pace is not large/small enough
			if( costLeft < orderingCost) {
				System.out.println("Cost of initial input invnetory is too small, move left");
				binarySearchsQtHeuristic(i1 - pace, pace, sQmodel,
						costDifferencesQtHeuristic(sQmodel, i1-pace, currentQ, rangedQ), 
						costDifferencesQtHeuristic(sQmodel, i1, currentQ, rangedQ), currentQ, rangedQ);
			}else {
				System.out.println("Cost of initial input invnetory is too large, move right");
				binarySearchsQtHeuristic(i1 + pace, pace, sQmodel, 
						costDifferencesQtHeuristic(sQmodel, i1, currentQ, rangedQ), 
						costDifferencesQtHeuristic(sQmodel, i1+pace, currentQ, rangedQ), currentQ, rangedQ);
			}
		}
	}

	public static double[] reorderPoint_sQtHeuristic(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost, 
			double initialStock, int partitions, 
			double pace, double[] schedule,
			boolean Qranged) throws Exception {
		double[] reorderPoint = new double[demandMean.length];
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);

		//solve reorderpoints
		for(int t=0; t<demandMean.length; t++) {
			if(schedule[t] < 1.0) {
				reorderPoint[t] = Double.NEGATIVE_INFINITY;
				System.out.println("no replenishment placed.");
				//System.out.println();
			}else {
				if(t==demandMean.length-1) {		//for the last period, only apply the method as sQ, so refer to sQminlp_recursive
					double[] cost = new double[101];
					for(int i=0; i<cost.length; i++) {
						String FileName = null;	
						if(Qranged == true) {
							FileName = "sQtPoisson_recursive_Qranged";
						}else {
							FileName = "sQtPoisson_recursive";
						}					
						try {
							sQTminlp_heuristic sQmodelInput = new sQTminlp_heuristic(
									demandMean, holdingCost, fixedCost,  unitCost, penaltyCost, 
									i-50, partitions, null, schedule);
							cost[i] = sQmodelInput.solveMINLP_heuristic(FileName);
						}catch(IloException e){
							e.printStackTrace();
						}
					}
					double targetCost = sdp.util.globleMinimum.getGlobalMinimum(cost) + schedule[t] *unitCost + fixedCost;
					for(int i=0; i<cost.length-1; i++) {
						if((cost[i]>targetCost)&&(cost[i] < targetCost)) {
							reorderPoint[t] = i+50;
						}
						break;
					}
					System.out.println("s0 = "+reorderPoint[t]);
				}else{
					double[] futureQ = sdp.util.decomposeDoubleArray.decomposeArray(schedule, demandMean.length-t-1);
					//System.out.println("future Q: "+Arrays.toString(futureQ));
					sQTminlp_heuristic sQmodelBRT = new sQTminlp_heuristic(
							demandMeanInput[t], holdingCost, fixedCost,  unitCost, penaltyCost, 
							initialStock, 
							partitions, 
							null, futureQ);
					double costLeft = costDifferencesQtHeuristic(sQmodelBRT, initialStock, schedule[t], Qranged); 
					double costRight = costDifferencesQtHeuristic(sQmodelBRT, initialStock + pace, schedule[t], Qranged); 
					binarySearchsQtHeuristic(initialStock, pace, sQmodelBRT, costLeft, costRight, schedule[t], Qranged);				
				}
				//record reorder point in an array
				if(t<demandMean.length-1) {
				File tempFile = new File ("src/main/java/minlp_Normal/tempRminlp.txt"); 		
				FileReader fr = new FileReader(tempFile);
				BufferedReader br = new BufferedReader(fr);
				String read = "";
				read = br.readLine();
				reorderPoint[t] = Double.parseDouble(read);}
			}
		}
		return reorderPoint;
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
