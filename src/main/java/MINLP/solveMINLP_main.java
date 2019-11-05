package MINLP;

import ilog.concert.IloException;
import ilog.opl.IloOplDataElements;
import ilog.opl.IloOplException;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplRunConfiguration;

public class solveMINLP_main {

	static final String DATADIR = ".";

	static public void main(String[] args) throws Exception {
	    int status = 127;
	    try {
            int capFlour = 20;
            double best;
            double curr = Double.MAX_VALUE;

            IloOplFactory.setDebugMode(true);
            IloOplFactory oplF = new IloOplFactory();

            IloOplRunConfiguration rc0 = oplF.createOplRunConfiguration(DATADIR
                    + "/sQsinglePoisson_oneRun.mod", DATADIR + "/sQsinglePoisson_oneRun.dat");
            IloOplDataElements dataElements = rc0.getOplModel().makeDataElements();

            do {
                best = curr;

                IloOplRunConfiguration rc = oplF.createOplRunConfiguration(rc0
                        .getOplModel().getModelDefinition(), dataElements);
                rc.getCplex().setOut(null);
                rc.getOplModel().generate();

                System.out.println("Solve with capFlour = " + capFlour);
                if (rc.getCplex().solve()) {
                    curr = rc.getOplModel().getCplex().getObjValue();
                    System.out.println("OBJECTIVE: " + curr);
                    status = 0;
                } else {
                    System.out.println("No solution!");
                    status = 1;
                }
                capFlour++;
                dataElements.getElement("Capacity").asNumMap().set("flour",
                        capFlour);

                rc.end();
            } while (best != curr && status == 0);
            oplF.end();
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
