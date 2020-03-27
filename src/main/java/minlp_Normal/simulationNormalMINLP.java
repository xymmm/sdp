package minlp_Normal;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import reorderQuantitySystem.simInstance;
import sQ.sdp.sQsolution;
import sQ.simulation.sQsimInstanceDouble;
import sdp.data.Instance;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.util.Chrono;

public class simulationNormalMINLP {
	
	
	
	/** check inventory and return action decision **/
	static int checkInventory(double reorderPoint, double inventoryLevel) {
		return (inventoryLevel<reorderPoint)? 1 : 0;
	}
	
	/** compute purchasing cost according to action decision **/
	static double computePurchasingCost(int actionDecision, int currentStageIndex, double[] schedule, double fixedCost, double unitCost) {
		return actionDecision*(
				fixedCost 
				+ unitCost*schedule[currentStageIndex]
				);
	}
	
	/** update inventory level**/
	static double updateInventoryLevel(double inventoryLevel, double inventoryAlteration) {
		return inventoryLevel + inventoryAlteration;
	}
	
	/** compute holding or penalty cost **/
	static double computeClosingCost(double inventoryLevel, double holdingCost, double penaltyCost) {
		if(inventoryLevel >= 0) {//return holding cost
			return inventoryLevel*holdingCost;
		}else {//return penalty cost
			return -inventoryLevel*penaltyCost;
		}
	}
	
	/** generate Normal random number as demand **/
	static MRG32k3a randomStream = new MRG32k3a();	
	static {
	   long seed[] = {1234,1234,1234,1234,1234,1234};
	   randomStream.setSeed(seed);
	}	
	static double generateNormalDemand(double inventoryLevel, int actionDecision, double[] demandMean, double stdParameter, int currentStageIndex) {
		double demand = NormalDist.inverseF(demandMean[currentStageIndex], 
				demandMean[currentStageIndex]*stdParameter, randomStream.nextDouble());
		return -Math.round(demand);
	}
	
	
//************************************************************************************************************************************
	public static double simNormal(simNormalInstance normalInstance, boolean print) {
		double cost = 0;
		int actionDecision;		
		int currentStageIndex = 0;
		
		List<Integer> xLabel = new ArrayList<>();//to print variation of inventory - x axis
		List<Double>  yLabel = new ArrayList<>();//to print variation of inventory - y axis
		double inventoryLevel = normalInstance.initialStock;
		
		do {
			xLabel.add(currentStageIndex);
			yLabel.add(inventoryLevel);
			
			if(print == true) System.out.println("At stage "+(currentStageIndex + 1));
			if(print == true) System.out.println("Current inventory level is "+inventoryLevel);
			
			//check inventory
			actionDecision = checkInventory(normalInstance.reorderPoints[currentStageIndex], inventoryLevel);
			
			if(print == true) System.out.println((actionDecision == 1) ? "Replenishment order placed with quantity " + normalInstance.schedule[currentStageIndex]:"No order placed. ");
			//if(currentStageIndex == 0) actionDecision = 0;
			
			//compute purchasing cost
			cost += computePurchasingCost(actionDecision, currentStageIndex, normalInstance.schedule, normalInstance.fixedCost, normalInstance.unitCost);
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, actionDecision*normalInstance.schedule[currentStageIndex]);
			if(print == true) System.out.println("Updated inventory level is "+inventoryLevel);			
			if(actionDecision == 1) {
				xLabel.add(currentStageIndex);
				yLabel.add(inventoryLevel + normalInstance.schedule[currentStageIndex]);
			}
			
			//generate, check and meet demand
			double demand = generateNormalDemand(inventoryLevel, actionDecision, normalInstance.demandMean, normalInstance.stdParameter, currentStageIndex); // as a negative
			if(print == true) System.out.println("Demand in this stage is "+(-demand));
			
			//update inventory level
			inventoryLevel = updateInventoryLevel(inventoryLevel, demand);
			if(print == true) System.out.println("Inventory level after meeting demand is "+inventoryLevel);
			
			//5. compute closing cost
			cost += computeClosingCost(inventoryLevel, normalInstance.holdingCost, normalInstance.penaltyCost);
			if(print == true) System.out.println("Cumulative cost is "+cost);
			
			if(currentStageIndex == normalInstance.demandMean.length -1) {
				xLabel.add(currentStageIndex +1);
				yLabel.add(inventoryLevel);
			}
			
			currentStageIndex++;
			if(print == true) System.out.println();
		}while(currentStageIndex < normalInstance.demandMean.length);
		
		//System.out.println(xLabel);
		//System.out.println(yLabel);
		return cost;
	}
	
	/** multiple run times **/
	public static void simulationNormalMINLPmultipleRuns(simNormalInstance normalInstance, int count) {
		for(int i=0; i<count; i++) {
			normalInstance.statCost.add(simNormal(normalInstance, false));
		}
	}
	
	public static void main(String[] args) {

		/** declare instance parameters **/
		double fixedOrderingCost = 500;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 20;
		
		double initialStock = 0;

		double stdParameter = 0.1;
		double[] demandMean = {130,150,127,76,27,
							   10,36,88,136,149,
							   121,68,22,11,42,
							   96,140,148,114,60,
							   18,14,50,104,144};
		
		double[] reorderPoints = 
				{119, 131, 105, 60, 21,
				 3,  20, 75,  129, 131, 
				 119, 54, 15, 5, 10, 
				 80, 129, 121, 99, 45,
				 13, 7, 29, 97, 129};
		double[] schedule = 
			{308.49088400286985, 434.95669794153554, 232.4366261796751, 152.69081632433642, 165.12494323474343, 
			 137.72242644347392, 263.8181617437534, 237.2508978954362, 283.9560295874421, 381.6934449299577, 
			 250.1073078871932, 126.47226742507672, 71.9861892156615, 154.12303178578043, 293.1001637158107, 
			 260.36546260631224, 297.24171168933856, 362.46077360620654, 241.8154942188296, 135.37839477415827, 
			 89.88789798358853, 71.45277066751689, 314.4867776645952, 273.4959536101555, 164.66904};


		simNormalInstance normalInstance = new simNormalInstance(
				demandMean, 
				fixedOrderingCost,
				unitCost,
				holdingCost, 
				penaltyCost, 
				initialStock, 
				stdParameter, 
				schedule, 
				reorderPoints				
				);	
		
		Chrono timer = new Chrono();
		
		int count = 1000;
		simulationNormalMINLPmultipleRuns(normalInstance, count);
		
		normalInstance.statCost.setConfidenceIntervalStudent();
		System.out.println(normalInstance.statCost.report(0.9, 3));
		System.out.println("Total CPU time: "+timer.format());

		System.out.println(normalInstance.statCost.average());
	}

}
