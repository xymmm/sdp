package sQt.sdp;

import sQ.simulation.sQsim;
import sQ.simulation.sQsimInstance;
import sS.sdp.sS;
import sdp.data.Instance;
import umontreal.ssj.util.Chrono;

public class sQt {

	/***** Output methods *****/
	
	/** print reorder points s **/
	static void prints(Instance instance, sQtsolution sQtsolution) {
		System.out.println();
		System.out.println("Reorder points: ");
		for(int t=0; t<instance.getStages(); t++) {
			System.out.println(sQtsolution.getssQt(instance, sQtsolution)[t]);
		}
	}
	
	/** print Qt **/
	static void printQt(Instance instance, sQtsolution sQtsolution) {
		System.out.println();
		System.out.println("Order quantities:");
		for(int t=0; t<instance.getStages(); t++) {
			System.out.println(sQtsolution.getQt(instance, sQtsolution)[t]);
		}
	}	
	
	/** print optimal costs **/
	static void printOptimalCost(int inventoryIndex, Instance instance, sQtsolution sQtsolution) {
		int[] Qt = sQtsolution.getQt(instance, sQtsolution);
		System.out.println("Optimal Cost: ");
		System.out.println(sQtsolution.totalCost1[instance.initialInventory - instance.minInventory][Qt[0]][Qt[1]][Qt[2]][Qt[3]]);
	}
	
	/** plot costs*
	static void plotCost(Instance instance, sQtsolution sQtsolution, int t) {
		XYSeries series = new XYSeries("No action");
		for(int a=0;a<instance.maxQuantity+1;a++) {
			series.add(a,sQtsolution.totalCost[t][instance.initialInventory - instance.minInventory][a]);
		}
		XYSeriesCollection collection = new XYSeriesCollection();
		collection.addSeries(series);
		JFreeChart chart = ChartFactory.createXYLineChart("Expected Total Cost for Period "+ (t+1), "Feasible Replenishment Quantity", "Expected total cost",
				collection, PlotOrientation.VERTICAL, true, true, false);
		ChartFrame frame = new ChartFrame("Period "+(t+1),chart);
		frame.setVisible(true);
		frame.setSize(1500,1200);
	}*/
	
	/***** main computation *****/
	public static sQtsolution solvesQcombinations(Instance instance) {
		
		//working matrix
		int[] inventory = new int [instance.maxInventory - instance.minInventory + 1];
		for(int i=0;i<inventory.length;i++) {
			inventory[i] = i + instance.minInventory;
		}
		double demandProbabilities [][] = sS.computeDemandProbability(instance.demandMean, instance.maxDemand, instance.tail);
		
		//different dimension index from sdp or sQ, as period takes the first.
		double totalCost4[][] = new double[inventory.length][instance.maxQuantity+1];
		//boolean optimalAction[][][] = new boolean [instance.getStages()][inventory.length][instance.maxQuantity+1];
		//to construct Q-combinations
		int[] Q = new int [instance.maxQuantity+1];
		
		//for last period t=4
		for(int q4 = 0; q4 < Q.length; q4++) {
			for(int i=0; i<inventory.length; i++) {
				totalCost4[i][q4] = sS.computePurchasingCost(q4, instance.fixedOrderingCost, instance.unitCost);
				double scenarioProb = 0;
				for(int d=0;d<demandProbabilities[3].length;d++) {
					if((inventory[i] + q4 - d <= instance.maxInventory) && (inventory[i] + q4 - d >= instance.minInventory)) {
						totalCost4[i][q4] += demandProbabilities[3][d]*(
								sS.computeImmediateCost(
										inventory[i], 
										q4, 
										d, 
										instance.holdingCost, 
										instance.penaltyCost, 
										instance.fixedOrderingCost, 
										instance.unitCost) 
										);
						scenarioProb += demandProbabilities[3][d];
					}
				}
				totalCost4[i][q4] /= scenarioProb;
			}
		}System.out.println("First loop - totalCost4");
		
		double[][][] totalCost3 = new double [inventory.length][Q.length][Q.length];
		for(int q3 = 0; q3<Q.length; q3++) {
			for(int q4 = 0; q4<Q.length; q4++) {
				for(int i=0; i<inventory.length; i++) {
					totalCost3[i][q3][q4] = sS.computePurchasingCost(q3, instance.fixedOrderingCost, instance.unitCost);
					double scenarioProb = 0;
					for(int d=0; d<demandProbabilities[2].length;d++) {
						if((inventory[i] + q3 - d <= instance.maxInventory)&&(inventory[i] + q3 - d >= instance.minInventory)) {
							totalCost3[i][q3][q4] += demandProbabilities[2][d]*(
									sS.computeImmediateCost(inventory[i], q3, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
									+totalCost4[i+q3-d][q4]);
							scenarioProb += demandProbabilities[2][d];
						}
					}
					totalCost3[i][q3][q4] /= scenarioProb;
				}
			}
		}System.out.println("Second loop - totalCost3");
		
		double[][][][] totalCost2 = new double [inventory.length][Q.length][Q.length][Q.length];
		for(int q2 = 0; q2<Q.length; q2++) {
			for(int q3=0; q3<Q.length; q3++) {
				for(int q4=0; q4<Q.length; q4++) {
					for(int i=0; i<inventory.length;i++) {
						totalCost2[i][q2][q3][q4] = sS.computePurchasingCost(q2, instance.fixedOrderingCost, instance.unitCost);
						double scenarioProb = 0;
						for(int d=0; d<demandProbabilities[1].length;d++) {
							if((inventory[i] + q2 - d <= instance.maxInventory)&&(inventory[i] + q2 - d >= instance.minInventory)) {
								totalCost2[i][q2][q3][q4] += demandProbabilities[1][d]*(
										sS.computeImmediateCost(inventory[i], q2, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
										+totalCost3[i+q2-d][q3][q4]);
								scenarioProb += demandProbabilities[1][d];
							}
						}
						totalCost2[i][q2][q3][q4] /= scenarioProb;
					}
				}
			}
		}System.out.println("Third loop - totalCost2");
		
		double[][][][][] totalCost1 = new double [inventory.length][Q.length][Q.length][Q.length][Q.length];
		for(int q1=0; q1<Q.length; q1++) {
			for(int q2 = 0; q2<Q.length; q2++) {
				for(int q3=0; q3<Q.length; q3++) {
					for(int q4=0; q4<Q.length; q4++) {
						for(int i=0; i<inventory.length;i++) {
							totalCost1[i][q1][q2][q3][q4] = sS.computePurchasingCost(q1, instance.fixedOrderingCost, instance.unitCost);
							double scenarioProb = 0;
							for(int d=0; d<demandProbabilities[0].length;d++) {
								if((inventory[i] + q1 - d <= instance.maxInventory)&&(inventory[i] + q1 - d >= instance.minInventory)) {
									totalCost1[i][q1][q2][q3][q4] += demandProbabilities[0][d]*(
											sS.computeImmediateCost(inventory[i], q1, d, instance.holdingCost, instance.penaltyCost, instance.fixedOrderingCost, instance.unitCost)
											+totalCost2[i+q1-d][q2][q3][q4]);
									scenarioProb += demandProbabilities[0][d];
								}
							}
							totalCost1[i][q1][q2][q3][q4] /= scenarioProb;
						}
					}
				}
			}
		}System.out.println("Forth loop - totalCost1");

		
		return new sQtsolution(totalCost1, totalCost2, totalCost3, totalCost4, inventory);
	}
	

	
	public static void main(String[] args) {
		
		Chrono timer = new Chrono();

		double fixedOrderingCost = 100;
		double unitCost = 0;
		double holdingCost = 1;
		double penaltyCost = 10;
		int[] demandMean = {5,10,15,10};//{20,40,60,40};

		double tail = 0.00000001;

		int minInventory = -35;//-500;
		int maxInventory = 35;//500;
		int maxQuantity = 25;//500;

		Instance instance = new Instance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				maxQuantity
				);

		/** Solve the classic instance **/
		sQtsolution sQtsolution = solvesQcombinations(instance);

		printOptimalCost(instance.initialInventory-instance.minInventory, instance, sQtsolution);

		printQt(instance, sQtsolution);
		prints(instance, sQtsolution);
		
		/** simulations **/
		System.out.println();
		System.out.println("Simulations:");
		int[] reorderPoint = sQtsolution.getssQt(instance, sQtsolution);
		int[] actionQuantity = sQtsolution.getQt(instance, sQtsolution);
		sQsimInstance sQsystem = new sQsimInstance(
				fixedOrderingCost,
				unitCost,
				holdingCost,
				penaltyCost,
				demandMean,
				tail,
				minInventory,
				maxInventory,
				actionQuantity,
				reorderPoint
				);

		int count = 100000;
		sQsim.simulationsQinstanceRuns(sQsystem, count);

		sQsystem.statCost.setConfidenceIntervalStudent();
		System.out.println(sQsystem.statCost.report(0.9, 3));
		
		System.out.println();
		System.out.println("Total CPU time: "+timer.format());

		}//main
	
}//class
