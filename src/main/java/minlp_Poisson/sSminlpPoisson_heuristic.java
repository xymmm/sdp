package minlp_Poisson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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

public class sSminlpPoisson_heuristic {

	double[] 	demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double 		initialStock;
	int 		partitions;
	double[][][] coefficients;

	String instanceIdentifier;

	public sSminlpPoisson_heuristic(
			double[] 	demandMean,
			double 		holdingCost,
			double 		fixedCost,
			double 		unitCost,
			double 		penaltyCost,
			double 		initialStock,
			int 		partitions,
			double[][][] coefficients
			) {
		this.demandMean = demandMean;
		this.holdingCost = holdingCost;
		this.fixedCost = fixedCost;
		this.unitCost = unitCost;
		this.penaltyCost = penaltyCost;
		this.initialStock = initialStock;
		this.partitions = partitions;
		this.coefficients = coefficients;
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
		IloOplDataSource dataSource = new sSminlpPoisson_heuristic.sSRecursiveData(oplF);
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
	class sSRecursiveData extends IloCustomOplDataSource{
		sSRecursiveData(IloOplFactory oplF){
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
			//double[][][] coefficients = sQminlp_oneRun.getLamdaMatrix (demandMean, partitions, 100000);
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
	public static double sSheuristic (sSminlpPoisson_heuristic sSmodel, double inventoryLevel, boolean rangedQ) {
		String FileName = null;	
		double c1 = 0;

		if(rangedQ == true) {
			FileName = "sS_Poisson_heuristic_Qranged";
		}else {
			FileName = "sS_Poisson_heuristic";
		}		
		try {
			sSminlpPoisson_heuristic sSmodelInput = new sSminlpPoisson_heuristic(
					sSmodel.demandMean, sSmodel.holdingCost, sSmodel.fixedCost, sSmodel.unitCost, sSmodel.penaltyCost,
					sSmodel.initialStock, sSmodel.partitions, sSmodel.coefficients);
			c1 = sSmodelInput.solveMINLP_heuristic(FileName);
		}catch(IloException e){
			e.printStackTrace();
		}
		return  c1;
	}

	public static void sSbinarySearchHeuristic(double initialInputLevel, double pace, sSminlpPoisson_heuristic sSmodel, 
			double costLeft, double costRight,
			double currentQ, double minCost, boolean rangedQ) throws Exception {	
		File tempFile = new File ("src/main/java/minlp_Poisson/sStemp.txt"); //to save reorder point as a string in the file

		System.out.println("costLeft = "+costLeft);
		System.out.println("costRight = "+costRight);
		
		double targetCost = minCost + ((currentQ==0.0)? 0 : (sSmodel.fixedCost + currentQ*sSmodel.unitCost));
		System.out.println("targetCost = "+targetCost);
		double i1 = initialInputLevel; 

		if( (costLeft>targetCost) && (costRight<targetCost) ) {
			double levelBinary = i1 + Math.floor(0.5*pace); 
			double costBinary = sSheuristic(sSmodel, levelBinary, rangedQ);
			System.out.println("cost(binary="+levelBinary+") = " + costBinary);

			//judge if costBinary > orderingCost or not
			if(costBinary > targetCost) {//[binary, right]
				double costBinaryClose = sSheuristic (sSmodel, levelBinary + 1, rangedQ);
				if((costBinaryClose < targetCost)||(levelBinary == i1 + pace)) {
					System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					System.out.println();
				}else {
					System.out.println("binary search proceeds, right interval.");
					sSbinarySearchHeuristic(levelBinary, Math.round(0.5*pace), sSmodel, costBinary, costRight, currentQ, minCost, rangedQ);
				}
			}else {//[left(i1), binary]
				double costBinaryClose = sSheuristic (sSmodel, levelBinary - 1, rangedQ);
				if((costBinaryClose > targetCost)||(levelBinary == i1)) {
					System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					System.out.println();
				}else {
					System.out.println("binary search proceeds, left interval.");
					sSbinarySearchHeuristic(i1, Math.round(0.5*pace), sSmodel, costLeft, costBinary, currentQ, minCost, rangedQ);
				}				
			}
		}else {//pace is not large/small enough
			if( costLeft < targetCost) {
				System.out.println("Cost of initial input invnetory is too small, move left");
				sSbinarySearchHeuristic(i1 - pace, pace, sSmodel,
						sSheuristic(sSmodel, i1-pace, rangedQ), 
						sSheuristic(sSmodel, i1, rangedQ), currentQ, minCost, rangedQ);
			}else {
				System.out.println("Cost of initial input invnetory is too large, move right");
				sSbinarySearchHeuristic(i1 + pace, pace, sSmodel, 
						sSheuristic(sSmodel, i1, rangedQ), 
						sSheuristic(sSmodel, i1+pace, rangedQ), currentQ, minCost, rangedQ);
			}
		}
	}


	public static double[] sS_reorderPoints(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost, 
			double initialStock, int partitions, 
			double pace, double[] schedule, double[] minCost,
			boolean rangedQ) throws Exception {

		double[] reorderPoint = new double[demandMean.length];
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);

		//solve reorderpoints
		for(int t=0; t<demandMean.length; t++) {
			long timeStart = System.currentTimeMillis();
			
			double[][][] coefficients = sQminlp_oneRun.getLamdaMatrix (demandMean, partitions, 100000);
			System.out.println("lambda matrix done.");

			sSminlpPoisson_heuristic sSmodelBR = new sSminlpPoisson_heuristic(
					demandMeanInput[t], holdingCost, fixedCost, unitCost, penaltyCost,
					initialStock, partitions, coefficients);
			double costLeft = sSheuristic(sSmodelBR, sSmodelBR.initialStock, rangedQ);
			double costRight= sSheuristic(sSmodelBR, sSmodelBR.initialStock + pace, rangedQ);
			sSbinarySearchHeuristic(initialStock, pace, sSmodelBR, costLeft, costRight,
					schedule[t] , minCost[t], rangedQ);			

			//record reorder point in an array
			File tempFile = new File ("src/main/java/minlp_Poisson/sStemp.txt"); 		
			FileReader fr = new FileReader(tempFile);
			BufferedReader br = new BufferedReader(fr);
			String read = "";
			read = br.readLine();
			reorderPoint[t] = Double.parseDouble(read);
			System.out.println("s["+(t+1)+"] = "+reorderPoint[t]);
			long timeEnd = System.currentTimeMillis();
			System.out.println("time = "+(timeEnd - timeStart)/1000.0);
		}
		return reorderPoint;
	}

	
	public static void main(String[] args) throws Exception {
		
		double fixedCost = 500;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 20;
		
		double initialStock = 0;
		boolean rangedQ = false;
		
		int partitions = 8;
		
		double pace = 32;
		
		//double[] demandMean = {76,27,10,36,88,136,149,121,68,22,11,42,96,140,148,114,60};
		//double[] schedule = {152.678528,75.599824,137.467184,264.77216,228.4653867,555.972576,418.9925867,268.8878133,146.60456,174.893088,152.707568,283.01416,240.56672,468.451728,327.31432,177.985104,62.362608};
		//double[] minCost = {3599.684553,3543.438257,3435.528526,3188.461053,2971.99419,2729.846837,2342.321367,2104.705869,1984.451267,1876.858492,1737.694604,1569.572127,1341.856613,1010.397846,709.10622,551.64116,502.362608};
	
		double[] demandMean = {20, 40, 60, 40};
		double[] schedule = {163.79727999997976, 143.5521919999929, 102.98536000001909, 41.92503999999748};
		double[] minCost = {760.5809099999094, 626.8023419999888, 534.5517900000295, 501.92503999999747};
		
		
		double[] st = sS_reorderPoints(
				 demandMean, fixedCost, unitCost, holdingCost, penaltyCost, 
				 initialStock, partitions, 
				 pace,  schedule,  minCost,rangedQ);
		System.out.println(Arrays.toString(st));
	
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
