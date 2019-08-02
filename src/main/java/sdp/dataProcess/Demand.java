package sdp.dataProcess;

import java.text.DecimalFormat;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class Demand {
	
	private int [] quantity = null;
	private int [] demand = null;
	private int [] inventory = null;
	private double probability [][] = null; 

	private static DecimalFormat df = new DecimalFormat("#.000000"); 


	public Demand() {
		quantity = calQuantity(Data.maxQuantity);
		inventory = calInventory(Data.maxDemand, Data.maxInventory, Data.stage);
		demand = calDemand(Data.maxDemand);
		//demand = testDemand(Data.maxDemand);
		//probability = calProbability(Data.maxDemand,Data.stage,Data.demandMean,Data.stdCoefficient);
		probability = PoissonProbability(Data.maxDemand, Data.stage, Data.demandMean, Data.tail);
		//probability = testProbability(Data.maxDemand, Data.stage, 0.5);
	}
	

	
	 // the following method is to test the generated data locally.
	  
	  public static void main(String[] args) { 
		  
		  Demand demand = new Demand();
	  
  
		  for(int i=0;i<demand.probability.length;i++) {
			  System.out.print(demand.demand[i]+" ");
			  for(int j=0;j<demand.probability[0].length;j++) {
				  System.out.print(df.format(demand.probability[i][j])+" "); 
			  }
			  System.out.println(); 
		  }
	  
	  
	  }
	 
	
	/**
	 * Method: CalInventory
	 * 
	 * This method generates all entries of state space.
	 * 
	 * @param maxDemand: the maximum value of demand  
	 * @param maxInventory: the maximum value of inventory storage
	 * @param stage: the number of stages
	 * @return: a 1-dimension array representing the state space  
	 */
	private int [] calInventory(int maxDemand,int maxInventory, int stage) {
		int stateSpace = maxInventory + maxDemand*stage+1;
		int[] inventory = new int[stateSpace];
		for(int i = 0; i<stateSpace;i++) {
			inventory[i] = i+1 - (stage*maxDemand+1);
		}
		return inventory;
	}
	
	
	/**
	 * Method: calQuantity
	 * 
	 * This method generates all possible value of a replenishment order, from 0 to the maxQuantity.
	 * 
	 * @param maxQuantity: the maximum size of a replenishment order
	 * @return: a 1-dimension array
	 */
	private int [] calQuantity(int maxQuantity) {
		int[] Q = new int[maxQuantity+1];
		for(int i=0; i<Q.length;i++) {
			Q[i] = i;
		}
		return Q;
	}
	
	/**
	 * Method: calDemand
	 * 
	 * This method generates all possible demand value within the planning horizon.
	 * 
	 * @param maxDemand: the maximum value of a possible demand
	 * @return: a 1-dimension array
	 */
	private int [] calDemand(int maxDemand) {
		int[] demand = new int[maxDemand+1];
		for(int i=0;i<demand.length;i++) {
			demand[i] = i;
		}
		return demand;
	}
	
	private int [] testDemand(int maxDemand) {
		int [] demand = new int[maxDemand];
		for(int i=0;i<demand.length;i++) {
			demand[i] = i+1;
		}
		return demand;
	}
	
	
	/**
	 * Method: calProbability
	 * 
	 * This method generates the probability of possible demand at each stage.
	 * First, for each stage, it generates a NORMAL distribution according to the input mean and std coefficient.
	 * Then, based on the value of maximum demand, a normalization factor is calculated.
	 * Last, the probability of a specific demand at a stage is calculated and normalized.
	 * 
	 * @param maxDemand: the maximum value of a possible demand
	 * @param stage: the number of stages
	 * @param demandMean: the mean of the distribution of demand
	 * @param stdCoefficient: the standard deviation coefficient
	 * @return
	 */
	private double [][] calProbability(int maxDemand,int stage,int [] demandMean,double stdCoefficient){
		double [][] prob = new double [maxDemand+1][stage];
		double factor;
		
		for(int t=0;t<stage;t++) {
			NormalDistribution dist = new NormalDistribution(demandMean[t],stdCoefficient*demandMean[t]);
			factor = dist.cumulativeProbability(maxDemand + 0.5) - dist.cumulativeProbability(0 - 0.5);
			
			for(int i=0;i<maxDemand;i++) {
				prob[i][t] = dist.probability(i-0.5, i+0.5);
				prob[i][t] = prob[i][t]/factor;
			}
		}
		return prob;
	}
	
	private double [][] PoissonProbability(int maxDemand, int stage, int[] demandMean, double tail){
		double [][] prob = new double [maxDemand+1][stage];
		
		for(int t=0;t<stage;t++) {
			PoissonDistribution dist = new PoissonDistribution(demandMean[t]);
			
			for(int i=0;i<maxDemand;i++) {
				prob[i][t] = dist.probability(i);
				if(prob[i][t]<=tail) {
					prob[i][t] = 0;
				}
			}
		}	
		
		for(int t=0;t<stage;t++) {
			double sum = 0;
			for(int i=0;i<maxDemand;i++) {
				sum = sum + prob[i][t];
			}
			for(int i=0;i<maxDemand;i++) {
				prob[i][t] = prob[i][t]/sum;
			}
		}
		
		return prob;
	}
	
	private double [][] testProbability(int maxDemand, int stage, double definedprob){
		double [][] prob = new double [maxDemand][stage];
		for(int i=0;i<maxDemand;i++) {
			for(int j=0;j<stage;j++) {
				prob[i][j] = definedprob;
			}
		}
		return prob;
	}
	
	
	
	
	public int[] getQuantity() {
		return quantity;
	}

	public int[] getDemand() {
		return demand;
	}

	public int[] getInventory() {
		return inventory;
	}

	public double[][] getProbability() {
		return probability;
	}
	
	
}
