package minlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class sQTminlp_oneRun {

	int[] 		demandMean;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	int 		initialStock;
	int 		partitions;

	String instanceIdentifier;

	public sQTminlp_oneRun(int[] demandMean, 
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

	public double[] solveMINLP_recursive (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);

		IloOplDataSource dataSource = new sQTminlp_oneRun.sQtOneRunData(oplF);
		opl.addDataSource(dataSource);
		opl.generate();

		cplex.setOut(null);

		boolean status =  cplex.solve();        
		if ( status )
		{   
			double objective = cplex.getObjValue();
			double[] Q = new double[demandMean.length];
            for(int t = 0; t < Q.length; t++){
                Q[t] = cplex.getValue(opl.getElement("Q").asNumVarMap().get(t+1));
                //System.out.println("Q["+(t+1)+"] = "+Q[t]);
             }
			opl.postProcess();
			oplF.end();
			System.gc();
			//return objective;
			return Q;
		} else {
			double[] Q = new double[demandMean.length];
			System.out.println("No solution!");
			oplF.end();
			System.gc();
            for(int t = 0; t < Q.length; t++){
                Q[t] = Double.NaN;
                }
			return Q;
		} 

	}

	class sQtOneRunData extends IloCustomOplDataSource{
		sQtOneRunData(IloOplFactory oplF){
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

	public static void main(String[] args) {
		int[] demandMean = {2,4,6,4};
		double fixedCost = 10;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 5;

		int initialStock = 0;

		int partitions = 10;

		long startTime = System.currentTimeMillis();

		double[] Q = new double[demandMean.length];
		
		try {
			sQTminlp_oneRun sQmodel = new sQTminlp_oneRun(
					demandMean,
					holdingCost,
					fixedCost,
					unitCost,
					penaltyCost,
					initialStock,
					partitions,
					"sQtPoisson"
					);
			Q = sQmodel.solveMINLP_recursive("sQtPoisson");
			//System.out.println("c("+initialStock+") = " +obj);
			
		}catch(IloException e){
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();

		System.out.println("time consumed = "+(endTime - startTime)/1000+" s");		

		System.out.println(Arrays.toString(Q));
	}


}
