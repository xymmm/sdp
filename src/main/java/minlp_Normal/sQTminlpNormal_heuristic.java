package minlp_Normal;

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

public class sQTminlpNormal_heuristic {

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
	double[]	optQ;		//future Q schedule
	String instanceIdentifier;

	public sQTminlpNormal_heuristic(double[] demandMean, double holdingCost, double fixedCost,  double unitCost, double penaltyCost, 
			double initialStock, double stdParameter, 
			int partitions, double[] means, double[] piecewiseProb, double error,
			String instanceIdentifier, double[] optQ) {
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
		this.optQ = optQ;
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
	public double solveMINLP_heuristic_Normal (String model_name) throws IloException{		
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		IloOplDataSource dataSource = new sQTminlpNormal_heuristic.sQheuristicData(oplF);
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
	class sQheuristicData extends IloCustomOplDataSource{
		sQheuristicData(IloOplFactory oplF){
			super(oplF);
		}
		public void customRead(){
			IloOplDataHandler handler = getDataHandler();
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
			handler.startElement("optQ"); handler.startArray();            
			for (int t = optQ.length-1 ; t >= 0; t--) {handler.addNumItem(optQ[t]);}
			handler.endArray(); handler.endElement();
		}

	}

	/***********************************************************************************************************************************/
	/*****************************************BINARY SEARCH for sQt heuristic***********************************************************/
	/***********************************************************************************************************************************/
	public static double costDifferencesQtHeuristic (sQTminlpNormal_heuristic sQmodel, double inventoryLevel, double currentQ) {
		double inventoryPlusQ = inventoryLevel + currentQ;
		double difference = 0;
		try {
			sQTminlpNormal_heuristic sQmodelInput = new sQTminlpNormal_heuristic(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryLevel, sQmodel.stdParameter, 
					sQmodel.partitions,  sQmodel.means, sQmodel.piecewiseProb, sQmodel.error,
					null, sQmodel.optQ);
			double c1 = sQmodelInput.solveMINLP_heuristic_Normal("sQtNormal_heuristic");
			sQTminlpNormal_heuristic sQmodelPlusQ = new sQTminlpNormal_heuristic(
					sQmodel.demandMean, sQmodel.holdingCost, sQmodel.fixedCost,  sQmodel.unitCost, sQmodel.penaltyCost, 
					inventoryPlusQ, sQmodel.stdParameter, 
					sQmodel.partitions,  sQmodel.means, sQmodel.piecewiseProb, sQmodel.error,
					null, sQmodel.optQ);
			double c2 = sQmodelPlusQ.solveMINLP_heuristic_Normal("sQtNormal_heuristic");
			difference = c1 - c2;
		}catch(IloException e){
			e.printStackTrace();
		}
		return  difference;
	}

	public static void binarySearchsQtHeuristic(double initialInputLevel, double pace, sQTminlpNormal_heuristic sQmodel,
			double costLeft, double costRight, double currentQ) throws Exception {

		File tempFile = new File ("src/main/java/minlp_Normal/tempRminlp.txt"); //to save reorder point as a string in the file
		double orderingCost = (currentQ==0.0)? 0 : (sQmodel.fixedCost + currentQ*sQmodel.unitCost);
		double i1 = initialInputLevel; 
		//System.out.println("cost("+i1+") = " + costLeft +"\t" + "cost("+(i1+pace)+") = "+costRight);

		if( (costLeft - orderingCost)*(costRight - orderingCost)<0 ) {
			double levelBinary = i1 + Math.floor(0.5*pace); 
			double costBinary = costDifferencesQtHeuristic(sQmodel, levelBinary, currentQ);
			//System.out.println("cost(binary="+levelBinary+") = " + costBinary);

			//judge if costBinary > orderingCost or not
			if(costBinary > orderingCost) {//[binary, input]
				double costBinaryClose = costDifferencesQtHeuristic (sQmodel, levelBinary + 1, currentQ);
				if((costBinaryClose < orderingCost)||(levelBinary == i1 + pace)) {
					//System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					//System.out.println();
				}else {
					//System.out.println("binary search proceeds, right interval.");
					binarySearchsQtHeuristic(levelBinary, Math.round(0.5*pace), sQmodel, costBinary, costRight, currentQ);
				}
			}else {//[input, binary]
				double costBinaryClose = costDifferencesQtHeuristic (sQmodel, levelBinary - 1, currentQ);
				if((costBinaryClose > orderingCost)||(levelBinary == i1)) {
					//System.out.println("cost("+(levelBinary + 1) +") = " +costBinaryClose);
					String s_string = Double.toString(levelBinary+1);
					boolean flag = writeTxtFile(s_string, tempFile);
					//System.out.println();
				}else {
					//System.out.println("binary search proceeds, left interval.");
					binarySearchsQtHeuristic(i1, Math.round(0.5*pace), sQmodel, costLeft, costBinary, currentQ);
				}				
			}
		}else {//pace is not large/small enough
			if( costLeft < orderingCost) {
				//System.out.println("Cost of initial input invnetory is too small, move left");
				binarySearchsQtHeuristic(i1 - pace, pace, sQmodel,
						costDifferencesQtHeuristic(sQmodel, i1-pace, currentQ), 
						costDifferencesQtHeuristic(sQmodel, i1, currentQ), currentQ);
			}else {
				//System.out.println("Cost of initial input invnetory is too large, move right");
				binarySearchsQtHeuristic(i1 + pace, pace, sQmodel, 
						costDifferencesQtHeuristic(sQmodel, i1, currentQ), 
						costDifferencesQtHeuristic(sQmodel, i1+pace, currentQ), currentQ);
			}
		}
	}

	public static double[] reorderPoint_sQtHeuristic(
			double[] demandMean, double fixedCost, double unitCost, double holdingCost, double penaltyCost, 
			double initialStock, double stdParameter, int partitions, double[] piecewiseProb, double[] means, double error, 
			double pace, double[] schedule) throws Exception {
		double[] reorderPoint = new double[demandMean.length];
		double[][] demandMeanInput = sdp.util.demandMeanInput.createDemandMeanInput(demandMean);

		//solve reorderpoints
		for(int t=0; t<demandMean.length; t++) {
			if(schedule[t] == 0.0) {
				reorderPoint[t] = Double.NEGATIVE_INFINITY;
				//System.out.println("no replenishment placed.");
				//System.out.println();
			}else {
				if(t==demandMean.length-1) {
					try {
						sQminlpNormal_recursive sQmodelBR = new sQminlpNormal_recursive(
								demandMeanInput[t], holdingCost, fixedCost,  unitCost, penaltyCost, 
								initialStock, stdParameter, 
								partitions,  means, piecewiseProb, error,
								null, schedule[t]);
						//System.out.println("orderingCost = " + (sQmodelBR.fixedCost + Q[t]*sQmodelBR.unitCost));
						double costLeft = minlp_Normal.sQminlpNormal_recursive.costDifference(sQmodelBR, initialStock); 
						double costRight = minlp_Normal.sQminlpNormal_recursive.costDifference(sQmodelBR, initialStock + pace); 
						minlp_Normal.sQminlpNormal_recursive.binarySearch(initialStock, pace, sQmodelBR, costLeft, costRight);
					}catch(IloException e){
						e.printStackTrace();
					}	
				}else{
					double[] futureQ = sdp.util.decomposeDoubleArray.decomposeArray(schedule, demandMean.length-t-1);
					//System.out.println("future Q: "+Arrays.toString(futureQ));
					sQTminlpNormal_heuristic sQmodelBRT = new sQTminlpNormal_heuristic(
							demandMeanInput[t], holdingCost, fixedCost,  unitCost, penaltyCost, 
							initialStock, stdParameter, 
							partitions, means, piecewiseProb, error,
							null, futureQ);
					double costLeft = costDifferencesQtHeuristic(sQmodelBRT, initialStock, schedule[t]); 
					double costRight = costDifferencesQtHeuristic(sQmodelBRT, initialStock + pace, schedule[t]); 
					binarySearchsQtHeuristic(initialStock, pace, sQmodelBRT, costLeft, costRight, schedule[t]);				
				}
				//record reorder point in an array
				File tempFile = new File ("src/main/java/minlp_Normal/tempRminlp.txt"); 		
				FileReader fr = new FileReader(tempFile);
				BufferedReader br = new BufferedReader(fr);
				String read = "";
				read = br.readLine();
				reorderPoint[t] = Double.parseDouble(read);
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
