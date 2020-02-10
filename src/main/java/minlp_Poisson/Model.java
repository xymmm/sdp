package minlp_Poisson;

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

import java.io.*;
import java.util.Arrays;

/**
 * To run from Mac OS
 * 
 * -Djava.library.path=/Applications/CPLEX_Studio128/opl/bin/x86-64_osx/
 * 
 * Environment variable
 * 
 * DYLD_LIBRARY_PATH /Applications/CPLEX_Studio128/opl/bin/x86-64_osx/
 * 
 * @author gwren
 *
 */

public class Model {
   
   int Nbmonths;
   double[] expDemand; 
   double ordercost;
   double holdingcost; 
   double penaltycost;
   double unitcost;
   double initialStock;
   double[][] covariance;
   int Nbpartitions;
   
   String instanceIdentifier;
   
   public Model(
         int Nbmonths, 
         double[] expDemand, 
         double ordercost, 
         double holdingcost, 
         double penaltycost,
         double unitcost,
         double initialStock,
         double[][] covariance,
         int Nbpartitions,
         String instanceIdentifier){
      this.Nbmonths = Nbmonths;
      this.expDemand = expDemand;
      this.ordercost = ordercost;
      this.holdingcost = holdingcost;
      this.penaltycost = penaltycost;
      this.unitcost = unitcost;
      this.initialStock = initialStock;
      this.covariance = covariance;
      this.Nbpartitions = Nbpartitions;
      this.instanceIdentifier = instanceIdentifier;
   }
   
   private InputStream getMILPModelStream(File file){
      FileInputStream is = null;
      try{
         is = new FileInputStream(file);
      }catch(IOException e){
         e.printStackTrace();
      }
      return is;
   }
  

   
	public static double[][] calucluteCovariance(double [] ExpDemand, double cv, double [] rho){
		double [] stdDemand =new double [ExpDemand.length];
		for (int i = 0; i < ExpDemand.length; i ++) {
			stdDemand[i] = cv*ExpDemand[i];
		}
		
		double [][] covariance = new double [ExpDemand.length][ExpDemand.length];
		/*
		for (int row=0; row<covariance.length;row++) {
			for (int col=0; col<covariance[row].length;col++) {
				if (row==col) {
					covariance[row][col]=stdDemand[row]*stdDemand[col];
				} else if (col==row+1 | col==row-1) {
					covariance[row][col]=stdDemand[row]*stdDemand[col]*rho;
				} else  {
					covariance[row][col]=0;
				}
			}
		}
		*/
		
		//number of correlation is n
		for (int row=0; row<covariance.length;row++) {
			covariance[row][row]=stdDemand[row]*stdDemand[row];
			
			for (int nbcorrelation=0; nbcorrelation<rho.length; nbcorrelation++) {
				if (row+nbcorrelation<covariance.length-1) {
					covariance[row][row+nbcorrelation+1]=rho[nbcorrelation]*stdDemand[row]*stdDemand[row+nbcorrelation+1];
				}
				
				if (row-nbcorrelation>0) {
					covariance[row][row-nbcorrelation-1]=rho[nbcorrelation]*stdDemand[row]*stdDemand[row-nbcorrelation-1];
				}
			}
		}
		return covariance;
		
	}
   
   public static void main(String[] args){
	  System.out.println(System.getProperty("java.library.path"));
      //int Nbmonths = 10;
      //double[] expDemand = {200,50,100,300,150,200,100,50,200,150}; 
      //double[] stdDemand = {60,15,30,90,45,60,30,15,60,45}; 
      int Nbmonths = 15;
      double[] expDemand = {5,8,24,39,16,29,51,39,75,69,26,20,32,11,19}; 
      double cv = 0.2; 
      double ordercost = 100;
      double holdingcost = 1;
      double penaltycost = 10;
      double unitcost = 0;
      double initialStock = 0;
      double[] rho = {0.5};
      int Nbpartitions = 10;
      
      double[][] covariance = calucluteCovariance(expDemand, cv, rho);
      
      double lb = Double.NaN;
      double ub = Double.NaN;
      try{
         Model model = new Model(
               Nbmonths, 
               expDemand, 
               ordercost, 
               holdingcost,
               penaltycost,
               unitcost, 
               initialStock, 
               covariance,
               Nbpartitions,
               null);
         lb = model.solve("rs_milp_piecewise_penalty_lb");
         
      }catch(IloException e){
         e.printStackTrace();
      }
      
      try{
         Model model = new Model(
               Nbmonths, 
               expDemand, 
               ordercost, 
               holdingcost, 
               penaltycost,
               unitcost, 
               initialStock, 
               covariance,
               Nbpartitions,
               null);
         ub = model.solve("rs_milp_piecewise_penalty_ub");
      }catch(IloException e){
         e.printStackTrace();
      }
      
      System.out.println(lb);
      System.out.println(ub);
   }
   


public double solve(String model_name) throws IloException{
        //IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloCplex cplex = oplF.createCplex();
        IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMILPModelStream(new File("./opl_models/backorders/"+model_name+".mod")),model_name);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloOplModel opl=oplF.createOplModel(def,cplex);
        cplex.setParam(IloCplex.IntParam.Threads, 8);
        cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
        /*cplex.setParam(IloCplex.IntParam.VarSel, 1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, 2);
        cplex.setParam(IloCplex.IntParam.ImplBd, 2);
        cplex.setParam(IloCplex.IntParam.FracCuts, 2);
        cplex.setParam(IloCplex.IntParam.GUBCovers, 2);
        cplex.setParam(IloCplex.IntParam.DisjCuts, 2);
        cplex.setParam(IloCplex.IntParam.Covers, 2);
        cplex.setParam(IloCplex.IntParam.Cliques, 2);
        cplex.setParam(IloCplex.IntParam.FlowCovers, 2);
        cplex.setParam(IloCplex.IntParam.FlowPaths, 2);
        cplex.setParam(IloCplex.IntParam.MIRCuts, 2);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 3);
        */

        IloOplDataSource dataSource = new Model.MyData(oplF);
        opl.addDataSource(dataSource);
        opl.generate();

        cplex.setOut(null);
        
        double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        double end = cplex.getCplexImpl().getCplexTime();
        if ( status )
        {   
         double objective = cplex.getObjValue();
         double time = end - start;
            //System.out.println("OBJECTIVE: " + objective);  
            //s = new double[Nbmonths];
            boolean[] R = new boolean[Nbmonths];
            double[] S = new double[Nbmonths];
            for(int i = 0; i < Nbmonths; i++){
               //s[i] = cplex.getValue(opl.getElement("sValue").asNumVarMap().get(1+i));
               R[i] = Math.round(cplex.getValue(opl.getElement("purchase").asIntVarMap().get(1+i))) == 1 ? true : false;
               S[i] = cplex.getValue(opl.getElement("stock").asNumVarMap().get(1+i))+expDemand[i];
               //System.out.println("S["+(i+1)+"]="+S[i]);
            }
            opl.postProcess();
            //opl.printSolution(System.out);
            //opl.end();
            oplF.end();
            //errHandler.end();
            //cplex.end();
            System.gc();

            //return objective;
            //System.out.println(S[0]);
            return S[0];
        } else {
            System.out.println("No solution!");
            //opl.end();
            oplF.end();
            //errHandler.end();
            //cplex.end();
            System.gc();
            return Double.NaN;
        } 
        
    }
   
   
   class MyData extends IloCustomOplDataSource
    {
        MyData(IloOplFactory oplF)
        {
            super(oplF);
        }

        public void customRead()
        {
         IloOplDataHandler handler = getDataHandler();
         
            handler.startElement("Nbmonths");
            handler.addIntItem(Nbmonths);
            handler.endElement();
            
            handler.startElement("expDemand");
            handler.startArray();
            for (int j = 0 ; j<expDemand.length ; j++)
                handler.addNumItem(expDemand[j]);
            handler.endArray();
            handler.endElement();
            

            handler.startElement("ordercost");
            handler.addNumItem(ordercost);
            handler.endElement();
            
            handler.startElement("holdingcost");
            handler.addNumItem(holdingcost);
            handler.endElement();
            
            handler.startElement("penaltycost");
            handler.addNumItem(penaltycost);
            handler.endElement();
            
            handler.startElement("unitcost");
            handler.addNumItem(unitcost);
            handler.endElement();
            
            handler.startElement("initialStock");
            handler.addNumItem(initialStock);
            handler.endElement();
            
            handler.startElement("covariance");
            handler.startArray();
            for (int i = 0 ; i<covariance.length; i++) {
            	handler.startArray();
               	for (int j = 0 ; j<covariance.length; j++) 
                   handler.addNumItem(covariance[i][j]);
                handler.endArray();
            }
            handler.endArray();
            handler.endElement();
            
            handler.startElement("Nbpartitions");
            handler.addIntItem(Nbpartitions);
            handler.endElement();
            
            double[] means = getMeans(Nbpartitions);
            handler.startElement("means");
            handler.startArray();
            for (int j = 0 ; j<means.length ; j++)
                handler.addNumItem(means[j]);
            handler.endArray();
            handler.endElement();
            
            double[] probabilities = getProbabilities(Nbpartitions);
            handler.startElement("probabilities");
            handler.startArray();
            for (int j = 0 ; j<probabilities.length ; j++)
                handler.addNumItem(probabilities[j]);
            handler.endArray();
            handler.endElement();
            
            double error = getError(Nbpartitions);
            handler.startElement("error");
            handler.addNumItem(error);
            handler.endElement();
        }
    };
    
    
    public static double getError(int partitions){
        double[] errors = {
              0.3989422804014327,
              0.1206560496714961,
              0.05784405029198253,
              0.033905164962384104,
              0.022270929512393414,
              0.01574607463566398,
              0.011721769576577057,
              0.00906528789647753,
              0.007219916411227892,
              0.005885974956458359
              };
        return errors[partitions-1];
     }
     
     public static double[] getProbabilities(int partitions){
        switch(partitions){
        case 1:
           {
              double[] probabilities = {1};
              return probabilities;
           }
        case 2:
           {
              double[] probabilities = {0.5, 0.5};
              return probabilities;
           }  
        case 3:
           {
              double[] probabilities = {0.28783338731597996, 0.4243332253680401, 0.28783338731597996};
              return probabilities;
           }
        case 4:
           {
              double[] probabilities = {0.18755516774758485, 0.31244483225241515, 0.31244483225241515, 0.18755516774758485};
              return probabilities;
           }
        case 5:
           {
              double[] probabilities = {0.1324110437406592, 0.23491250409192982, 0.26535290433482195, 0.23491250409192987, 0.13241104374065915};
              return probabilities;
           }
        case 6:
           {
              double[] probabilities = {0.09877694599482933, 0.18223645973091096, 0.2189865942742597, 0.2189865942742597, 0.18223645973091096, 0.09877694599482933};
              return probabilities;
           }
        case 7:
           {
              double[] probabilities = {0.07669891602586965, 0.14538182479573014, 0.18144834397745296, 0.19294183040189444, 0.18144834397745302, 0.14538182479573014, 0.07669891602586965};
              return probabilities;
           }
        case 8:
           {
              double[] probabilities = {0.061394553470121016, 0.11872108750901467, 0.15205073490726895, 0.16783362411359537, 0.16783362411359537, 0.15205073490726895, 0.11872108750901467, 0.061394553470121016};
              return probabilities;
           }
        case 9:
           {
              double[] probabilities = {0.05033061540430428, 0.09884442068481658, 0.12900389832341652, 0.1460368860056377, 0.15156835916364986, 0.1460368860056377, 0.12900389832341652, 0.09884442068481658, 0.05033061540430428};
              return probabilities;
           }
        case 10:
           {
              double[] probabilities = {0.04206108420763477, 0.0836356495308449, 0.11074334596058821, 0.1276821455299152, 0.13587777477101692, 0.13587777477101692, 0.1276821455299152, 0.11074334596058821, 0.0836356495308449, 0.04206108420763477};
              return probabilities;
           }
        default:
           return null;
        }
     }
     
     public static double[] getMeans(int partitions){
        switch(partitions){
        case 1:
           {
              double[] means = {0};
              return means;
           }
        case 2:
           {
              double[] means = {-0.7978845608028654, 0.7978845608028654};
              return means;
           }  
        case 3:
           {
              double[] means = {-1.1850544278068644, 0, 1.1850544278068644};
              return means;
           }
        case 4:
           {
              double[] means = {-1.4353532729205845, -0.41522324304905966, 0.41522324304905966, 1.4353532729205845};
              return means;
           }
        case 5:
           {
              double[] means = {-1.6180463502161044, -0.6914240068499904, 0, 0.6914240068499903, 1.6180463502161053};
              return means;
           }
        case 6:
           {
              double[] means = {-1.7608020666235031, -0.8960107374480083, -0.28188851144130117, 0.28188851144130117, 0.8960107374480083, 1.7608020666235031};
              return means;
           }
        case 7:
           {
              double[] means = {-1.8773528492652836, -1.0572304450884658, -0.4934048390251067, 0, 0.4934048390251065, 1.0572304450884658, 1.8773528492652836};
              return means;
           }
        case 8:
           {
              double[] means = {-1.9754694729585056, -1.1895340795157716, -0.6615516528578579, -0.213586638906901, 0.213586638906901, 0.6615516528578579, 1.1895340795157716, 1.9754694729585056};
              return means;
           }
        case 9:
           {
              double[] means = {-2.059957433491476, -1.30127090280595, -0.8004000560466271, -0.3845969617811554, 0, 0.3845969617811554, 0.8004000560466271, 1.30127090280595, 2.059957433491476};
              return means;
           }
        case 10:
           {
              double[] means = {-2.133986195498256, -1.3976822972668839, -0.918199946431143, -0.5265753462727588, -0.17199013069262026, 0.17199013069262026, 0.5265753462727588, 0.918199946431143, 1.3976822972668839, 2.133986195498256};
              return means;
           }
        default:
           return null;
        }
     }
}

