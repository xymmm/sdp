package instanceRuns;

import ilog.concert.IloException;
import ilog.opl.IloCplex;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplException;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

public class solveMINLP {

    static final String DATADIR = ".";

    static public void main(String[] args) throws Exception
    {
      int status = 127;    
      try {
        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
        IloOplModelSource modelSource = oplF.createOplModelSource(DATADIR
                + "/sQsinglePoisson_oneRun.mod");
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource,settings);
        IloCplex cplex = oplF.createCplex();
        cplex.setOut(null);
        IloOplModel opl = oplF.createOplModel(def, cplex);
        IloOplDataSource dataSource = oplF.createOplDataSource(DATADIR
                + "/sQsinglePoisson_oneRun.dat");
        opl.addDataSource(dataSource);
        opl.generate();
        if (cplex.solve())
        {
            System.out.println("OBJECTIVE: " + opl.getCplex().getObjValue());
            opl.postProcess();
            opl.printSolution(System.out);
        }
        else
        {
            System.out.println("No solution!");
        }
        oplF.end();
		status = 0;
      } catch (IloOplException ex) {
        System.err.println("### OPL exception: " + ex.getMessage());
        ex.printStackTrace();
        status = 2;
      } catch (IloException ex) {
        System.err.println("### CONCERT exception: " + ex.getMessage());
        ex.printStackTrace();
        status = 3;
      } catch (Exception ex) {
        System.err.println("### UNEXPECTED UNKNOWN ERROR ...");
        ex.printStackTrace();
        status = 4;
      }
      System.exit(status);
    }
	
}
