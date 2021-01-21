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
import java.util.Arrays;

import LateralTransshipment.LT_MILP.LT_MILP_solution;
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
import umontreal.ssj.stat.Tally;
import umontreal.ssj.util.Chrono;

public class LT_MILP {

	double[] 	demandMean1;
	double[]	demandMean2;
	double 		holdingCost;
	double 		fixedOrderingCost;
	double 		unitOrderingCost;
	double 		penaltyCost;
	double[] 	initialStock;
	int 		partitions;
	double		fixedTransshippingCost;
	double		unitTransshippingCost;

	String instanceIdentifier;
	public Tally statCost = new Tally("stats on cost");

	public LT_MILP(double[] demandMean1, double[] demandMean2, 
			double holdingCost, double fixedCost, double unitCost, double penaltyCost,
			double[] initialStock, int partitions, 
			double fixedTransshippingCost, double unitTransshippingCost, 
			String instanceIdentifier) {
		this.demandMean1	= demandMean1;
		this.demandMean2	= demandMean2;

		this.holdingCost 	= holdingCost;
		this.fixedOrderingCost 		= fixedCost;
		this.unitOrderingCost 		= unitCost;
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
	public LT_MILP_solution solveLT_combinedS (String model_name) throws IloException{
		IloOplFactory oplF = new IloOplFactory();
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
		IloCplex cplex = oplF.createCplex();
		IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMINLPmodelStream(new File("./LT_opl_models/"+model_name+".mod")),model_name);
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
		IloOplModel opl=oplF.createOplModel(def,cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
		IloOplDataSource dataSource = new LT_MILP.recursiveData(oplF);
		opl.addDataSource(dataSource);
		opl.generate();
		cplex.setOut(null);
		boolean status =  cplex.solve();

		double[] transship = new double[demandMean1.length];
		double[] order1 = new double[demandMean1.length];
		double[] order2 = new double[demandMean1.length];
		double obj;

		if(status){   
			obj = cplex.getObjValue();
			for(int t = 0; t < transship.length; t++){
				transship[t] = cplex.getValue(opl.getElement("transship").asNumVarMap().get(t+1));
				order1[t] = cplex.getValue(opl.getElement("Q1").asNumVarMap().get(t+1));
				order2[t] = cplex.getValue(opl.getElement("Q2").asNumVarMap().get(t+1));
			}
			opl.postProcess();
			oplF.end();
			System.gc();
			return new LT_MILP_solution(obj, transship, order1, order2);
		}else{
			System.out.println("No solution!");
			obj = Double.NaN;
			for(int t = 0; t < transship.length; t++){
				transship[t] = Double.NaN;
				order1[t] = Double.NaN;
				order2[t] = Double.NaN;
			}
			oplF.end();
			System.gc();
			return new LT_MILP_solution(obj, transship, order1, order2);
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
			handler.startElement("fc"); handler.addNumItem(fixedOrderingCost); handler.endElement();
			handler.startElement("h"); handler.addNumItem(holdingCost); handler.endElement();
			handler.startElement("p"); handler.addNumItem(penaltyCost); handler.endElement();
			handler.startElement("v"); handler.addNumItem(unitOrderingCost); handler.endElement();
			handler.startElement("ft");handler.addNumItem(fixedTransshippingCost); handler.endElement();
			handler.startElement("ut");handler.addNumItem(unitTransshippingCost); handler.endElement();

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



	public static void main(String args[]) throws Exception {
		double[] 	demandMean1 = {4,6,8,6};
		double[]	demandMean2 = {4,6,8,6};
		double 		holdingCost = 0.25;
		double 		fixedCost = 20;
		double 		unitCost = 1;
		double 		penaltyCost = 5;
		double[] 	initialStock;
		int 		partitions = 10;
		double 		R = 5;
		double 		u = 0.5;


		int minInventory = -20;//-20;
		int maxInventory = 60;//30;
		String model = "LT_MILP_G";

		double inventory[][][] = new double[maxInventory - minInventory + 1][maxInventory - minInventory + 1][2];

		double cplexObj[][] = new double[maxInventory - minInventory + 1][maxInventory - minInventory + 1];				
		double simCost[][] = new double[maxInventory - minInventory + 1][maxInventory - minInventory + 1];

		long timeStart = System.currentTimeMillis();
		for(int i=0; i<inventory.length; i++) {
			for(int j=0; j<inventory[i].length; j++) {
				initialStock = new double[]{i+minInventory,j+minInventory};
				System.out.println(Arrays.toString(initialStock));
				LT_MILP milpInstance = new LT_MILP(demandMean1, demandMean2, 
						holdingCost,  fixedCost,  unitCost,  penaltyCost,
						initialStock,  partitions, R, u, null);

				LT_MILP_solution milpSolution = milpInstance.solveLT_combinedS(model);
				//System.out.println(Arrays.toString(milpSolution.transship));
				//System.out.println(Arrays.toString(milpSolution.order1));
				//System.out.println(Arrays.toString(milpSolution.order2));
				//System.out.println("initial inventory = "+Arrays.toString(initialStock) + ", obj = "+milpSolution.obj);
				cplexObj[i][j] = milpSolution.obj;
				
				//simulation
				int count = 10000;
				boolean print = false;				
				LT_simulation.LTsimMultipleRuns(count, milpInstance, milpSolution, print);
				milpInstance.statCost.setConfidenceIntervalStudent();
				//System.out.println(milpInstance.statCost.report(0.9, 3));
				//System.out.println(milpInstance.statCost.average());
				simCost[i][j] = milpInstance.statCost.average();

			}
		}
		long timeEnd = System.currentTimeMillis();
		System.out.println("time consumed = "+(timeEnd - timeStart)/1000 +"s");

		//obj
		FileWriter fw = null;
		try {
			File f = new File("src/main/java/lateralTransshipment/LT_MILP.txt");
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println("obj from cplex: ");		      
		pw.print("\t");
		for(int j = 0; j < inventory.length; j++) {
			pw.print((j+minInventory) + "\t");
		}
		pw.println();
		for(int i = inventory.length-1; i >=0 ; i--) {
			pw.print((i+minInventory) + "\t");
			for(int j = 0; j < inventory.length; j++) {
				pw.print(cplexObj[i][j] + "\t");
			}
			pw.println();
		}
		pw.println();

		//simCost
		pw.println("ETC by simulation");		      
		pw.print("\t");
		for(int j = 0; j < inventory.length; j++) {
			pw.print((j+minInventory) + "\t");
		}
		pw.println();
		for(int i = inventory.length-1; i >=0 ; i--) {
			pw.print((i+minInventory) + "\t");
			for(int j = 0; j < inventory.length; j++) {
				pw.print(simCost[i][j] + "\t");
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

	class LT_MILP_solution {
		public double obj;
		public double[] transship;
		public double[] order1;
		public double[] order2;

		public LT_MILP_solution(double obj, double[] transship, double[] order1, double[] order2) {
			this.obj = obj;
			this.transship = transship;
			this.order1 = order1;
			this.order2 = order2;
		}
	}

}
