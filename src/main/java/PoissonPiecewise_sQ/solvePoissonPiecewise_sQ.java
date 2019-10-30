package PoissonPiecewise_sQ;

import java.util.Timer;

import ilog.opl.IloOplFactory;

public class solvePoissonPiecewise_sQ {
	
	
	Timer _timer = new Timer();
	
	static public void main(String[] args) {
		
		int status = 127;
		
		try {
			//to create instances of OPL objects
	        IloOplFactory.setDebugMode(true);
	        IloOplFactory oplF = new IloOplFactory();
		}catch() {
			
		}
		
		
	}
	
	
	

}
