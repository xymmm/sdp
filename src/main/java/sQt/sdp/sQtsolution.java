package sQt.sdp;

import sdp.data.Instance;

public class sQtsolution {

	public int[] inventory;
	public double totalCost1[][][][][];
	public double totalCost2[][][][];
	public double totalCost3[][][];
	public double totalCost4[][];
	//public boolean optimalAction[][][];
	
	public sQtsolution(double totalCost1[][][][][], 
			double totalCost2[][][][],
			double totalCost3[][][],
			double totalCost4[][],
			//boolean[][][] optimalAction, 
			int[] inventory) {
		this.totalCost1 = totalCost1;
		this.totalCost2 = totalCost2;
		this.totalCost3 = totalCost3;
		this.totalCost4 = totalCost4;
		//this.optimalAction = optimalAction;
		this.inventory = inventory;
	}
	
	public double[] getCostBenchmark(sQtsolution sQtsolution, Instance instance) {
		double [] costBenchmark = new double[instance.getStages()];
		costBenchmark[0] = sQtsolution.totalCost1[instance.initialInventory-instance.minInventory][0][0][0][0];
		costBenchmark[1] = sQtsolution.totalCost2[instance.initialInventory-instance.minInventory][0][0][0];
		costBenchmark[2] = sQtsolution.totalCost3[instance.initialInventory-instance.minInventory][0][0];
		costBenchmark[3] = sQtsolution.totalCost4[instance.initialInventory-instance.minInventory][0];
		return costBenchmark;
	}
	
	public double[] getMinimumCost(sQtsolution sQtsolution, Instance instance) {
		double[] minimumCost = new double[sQtsolution.inventory.length];
		for(int i=0; i<minimumCost.length;i++) {
			double min = sQtsolution.totalCost1[i][0][0][0][0];
			for(int q1=1; q1<=instance.maxQuantity; q1++) {
				for(int q2=0; q2<=instance.maxQuantity;q2++) {
					for(int q3=0; q3<=instance.maxQuantity;q3++) {
						for(int q4=0; q4<= instance.maxQuantity;q4++) {
							if(sQtsolution.totalCost1[instance.initialInventory-instance.minInventory][q1][q2][q3][q4]<min) {
								min = sQtsolution.totalCost1[instance.initialInventory-instance.minInventory][q1][q2][q3][q4];
							}
						}
					}
				}
			}
		}
		return minimumCost;
	}
	
	
	public int[] getQt(Instance instance, sQtsolution sQtsolution) {
		int[] Qt = new int[instance.getStages()];
		
		double min=totalCost1[instance.initialInventory-instance.minInventory][0][0][0][0];
		//find the minimum cost among all feasible combinations of Q of all periods
		for(int q1=1; q1<=instance.maxQuantity; q1++) {
			for(int q2=0; q2<=instance.maxQuantity;q2++) {
				for(int q3=0; q3<=instance.maxQuantity;q3++) {
					for(int q4=0; q4<= instance.maxQuantity;q4++) {
						if(totalCost1[instance.initialInventory-instance.minInventory][q1][q2][q3][q4]<min) {
							min = totalCost1[instance.initialInventory-instance.minInventory][q1][q2][q3][q4];
							Qt[0] = q1;
							Qt[1] = q2;
							Qt[2] = q3;
							Qt[3] = q4;
						}
					}
				}
			}
		}
		return Qt;
	}
	
	
	public int[] getssQt(Instance instance, sQtsolution sQtsolution) {
		int[] st = new int[instance.getStages()];
		double[] costBenchmark = sQtsolution.getCostBenchmark(sQtsolution, instance);
		int[] Qt = sQtsolution.getQt(instance, sQtsolution);
		for(int i=0;i<inventory.length-1;i++) {  // Inventory   
			//period 4
			if(sQtsolution.totalCost4[i+1][Qt[3]] < costBenchmark[3]) {
			   if(Qt[3] > 0)
			      st[3] = i+1 + instance.minInventory;
			   else
			      st[3] = Integer.MIN_VALUE;
			}
			if(sQtsolution.totalCost3[i+1][Qt[2]][Qt[3]] < costBenchmark[2]) {
			   if(Qt[2] > 0)
			      st[2] = i+1 + instance.minInventory;
			   else
               st[2] = Integer.MIN_VALUE;
			}
			if(sQtsolution.totalCost2[i+1][Qt[1]][Qt[2]][Qt[3]] < costBenchmark[1]) {
			   if(Qt[1] > 0)
			      st[1] = i+1 + instance.minInventory;
			   else
               st[1] = Integer.MIN_VALUE;
			}
			if(sQtsolution.totalCost1[i+1][Qt[0]][Qt[1]][Qt[2]][Qt[3]] < costBenchmark[0]) {
			   if(Qt[0] > 0)
			      st[0] = i+1 + instance.minInventory;
			   else
               st[0] = Integer.MIN_VALUE;
			}
		}


		return st;
	}
	
	/*
	private int getMinimumIndex(double[] arr) {
		int index = 0;
		double min = arr[0];
		for(int i=0; i<arr.length-1;i++) {
			if(arr[i+1]<min) {
				min = arr[i+1];
				index = i+1;
			}
		}
		return index;
	}
	*/
	
}
