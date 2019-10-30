package PoissonPiecewise_sQ;

import java.util.Timer;

import ilog.cplex.IloCplex;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

public class solvePoissonPiecewise_sQ {
	
	
	Timer _timer = new Timer();
	
	static public void main(String[] args) {
		
		int status = 127;
		
		try {
			//to create instances of OPL objects
	        IloOplFactory.setDebugMode(true);
	        IloOplFactory oplF = new IloOplFactory();
	        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
	        IloOplModelSource modelSource = oplF.createOplModelSource(DATADIR
	                + "/sQ_1.mod");
	        IloOplSettings settings = oplF.createOplSettings(errHandler);
	        IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource,settings);
	        IloCplex cplex = oplF.createCplex();
	        cplex.setOut(null);
	        IloOplModel opl = oplF.createOplModel(def, cplex);
	        IloOplDataSource dataSource = oplF.createOplDataSource(DATADIR
	                + "/sQ_1.dat");
		}catch() {
			
		}
		
		
	}
	
	
	

}
