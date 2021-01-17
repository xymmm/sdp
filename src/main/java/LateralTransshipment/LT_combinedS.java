package LateralTransshipment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

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
import minlp_Poisson.sQminlp_oneRun;
import minlp_Poisson.sQminlp_recursive;

public class LT_combinedS {
	
	double[] 	demandMean1;
	double[]	demandMean2;
	double 		holdingCost;
	double 		fixedCost;
	double 		unitCost;
	double 		penaltyCost;
	double[] 	initialStock;
	int 		partitions;

	String instanceIdentifier;

	public LT_combinedS(double[] demandMean1, double[] demandMean2, 
			double holdingCost, double fixedCost, double unitCost, double penaltyCost,
			double[] initialStock, int partitions, String instanceIdentifier) {
		this.demandMean1	= demandMean1;
		this.demandMean2	= demandMean2;

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

	/**main solving block *****************************************************/
	public double[] solveLT_combinedS (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./LT_opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		IloOplDataSource dataSource = new LT_combinedS.recursiveData(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);
		boolean status =  cplex.solve();
		if(status){   
			double[] initialOrder = new double[2];
			for(int i = 0; i < initialOrder.length; i++){
				initialOrder[i] = cplex.getValue(opl.getElement("initialOrder").asNumVarMap().get(i+1));
			}
			sdp.util.writeText.writeDoubleArray(initialOrder, "src/main/java/lateralTransshipment/temp.txt");
			return initialOrder;
		}else{
			System.out.println("No solution!");
			oplF.end();
			System.gc();
			return new double[]{Double.NaN, Double.NaN};
		}
	}

	class recursiveData extends IloCustomOplDataSource{
		recursiveData(IloOplFactory oplF){
			super(oplF);
		}
		public void customRead(){
			IloOplDataHandler handler = getDataHandler();
			//problem parameters
			handler.startElement("nbmonths"); handler.addIntItem(demandMean1.length); handler.endElement();
			handler.startElement("fc"); handler.addNumItem(fixedCost); handler.endElement();
			handler.startElement("h"); handler.addNumItem(holdingCost); handler.endElement();
			handler.startElement("p"); handler.addNumItem(penaltyCost); handler.endElement();
			handler.startElement("v"); handler.addNumItem(unitCost); handler.endElement();
			
			handler.startElement("meandemand1"); handler.startArray();            
			for (int j = 0 ; j<demandMean1.length ; j++) {handler.addNumItem(demandMean1[j]);}
			handler.endArray(); handler.endElement();
			
			handler.startElement("meandemand2"); handler.startArray();            
			for (int j = 0 ; j<demandMean2.length ; j++) {handler.addNumItem(demandMean2[j]);}
			handler.endArray(); handler.endElement();
			
			handler.startElement("initialStock"); handler.startArray();            
			for (int j = 0 ; j<initialStock.length ; j++) {handler.addNumItem(initialStock[j]);}
			handler.endArray(); handler.endElement();

			handler.startElement("nbpartitions"); handler.addIntItem(partitions); handler.endElement();

			double partitionProb = 1.0/partitions;
			handler.startElement("prob"); handler.startArray();
			for (int j = 0 ; j<partitions; j++){handler.addNumItem(partitionProb);}
			handler.endArray(); handler.endElement();

			//symmetric instance
			double[][][] coefficients = sQminlp_oneRun.getLamdaMatrix (demandMean1, partitions, 100000);
			handler.startElement("lamda_matrix");
			handler.startArray();
			for(int t=0; t<demandMean1.length; t++) {
				handler.startArray();
				for(int j=0; j<demandMean1.length; j++) {
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
	

	
	public static void main(String args[]) {
		double[] 	demandMean1 = {4,6,8,6};
		double[]	demandMean2 = {4,6,8,6};
		double 		holdingCost = 0.25;
		double 		fixedCost = 20;
		double 		unitCost = 1;
		double 		penaltyCost = 5;
		double[] 	initialStock;
		int 		partitions = 4;
		
		int minInventory = -20;
		int maxInventory = 30;
		String model = "LT_combinedS";

		
		double initialOrder[][][] = new double[maxInventory - minInventory + 1][maxInventory - minInventory + 1][2];
		double[][] S = new double[maxInventory - minInventory + 1][maxInventory - minInventory + 1];

		long timeStart = System.currentTimeMillis();
		
		for(int i=0; i<initialOrder.length; i++) {
			for(int j=0; j<initialOrder[i].length; j++) {
				initialStock = new double[]{i+minInventory,j+minInventory};
				LT_combinedS instance = new LT_combinedS(demandMean1, demandMean2, 
						 holdingCost,  fixedCost,  unitCost,  penaltyCost,
						initialStock,  partitions, null);
				try {
					initialOrder[i][j] = instance.solveLT_combinedS(model);
					S[i][j] = initialOrder[i][j][0] + initialOrder[i][j][1] + i+minInventory + j+minInventory;
					System.out.println("i="+(i+minInventory)+", j="+(j+minInventory));
				} catch (IloException e) {
					e.printStackTrace();
				}
			}
		}
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000 +"s");
		
		//Gn
		FileWriter fw = null;
		try {
			File f = new File("src/main/java/lateralTransshipment/LT_combinedS.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("approximated up-to levels: ");		      
		pw.print("\t");
		for(int j = 0; j < initialOrder.length; j++) {
			pw.print((j+minInventory) + "\t");
		}
		pw.println();
		for(int i = initialOrder.length-1; i >=0 ; i--) {
			pw.print((i+minInventory) + "\t");
			for(int j = 0; j < initialOrder.length; j++) {
				pw.print(S[i][j] + "\t");
			}
			pw.println();
		}
		pw.println();
		
		pw.println("initial orders: ");		      
		pw.print("\t");
		for(int j = 0; j < initialOrder.length; j++) {
			pw.print((j+minInventory) + "\t");
		}
		pw.println();
		for(int i = initialOrder.length-1; i >=0 ; i--) {
			pw.print((i+minInventory) + "\t");
			for(int j = 0; j < initialOrder.length; j++) {
				pw.print(initialOrder[i][j][0] + ","+ initialOrder[i][j][1] + "\t");
			}
			pw.println();
		}
		pw.println();

		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	
	}

}
