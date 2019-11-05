package MINLP;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class cplexTest {
	
	public static void main(String args) {
		
		
		try {
//			declare a cplex object
			IloCplex cplex = new IloCplex();
//			lower bounds of decision variables
			double[] lb = {0.0,0.0,0.0};
//			upper bounds of decision variables
			double[] ub = {40.0,Double.MAX_VALUE,Double.MAX_EXPONENT};
//			set bounds
			IloNumVar[] x = cplex.numVarArray(3,lb, ub);
//			parameters in obj function
			double[] objvals = {1.0,2.0,3.0};
//			define obj function
			cplex.addMaximize(cplex.scalProd(x, objvals));
//			define first constraint
			cplex.addLe(cplex.sum(cplex.prod(-1.0, x[0]),
					cplex.prod(1.0, x[1]),
					cplex.prod(1.0, x[2])),20.0);
//			define second constaint
			cplex.addLe(cplex.sum(cplex.prod(1.0, x[0]),
					cplex.prod(-3, x[1]),
					cplex.prod(1, x[2])),30.0);
//			solve
			if(cplex.solve()){
				cplex.output().println("solution status:"+cplex.getStatus());
				cplex.output().println("solution value:"+cplex.getObjValue());
				
				double[] val = cplex.getValues(x);
				int ncols = cplex.getNcols();
				for(int j = 0;j<ncols;j++){
					cplex.output().println("column:"+j+"value="+val[j]);
				}
				
				cplex.end();
			}
			
		} catch (IloException e) {

			e.printStackTrace();
		}
		
		
		
	}

}
