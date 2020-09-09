package lateralTransshipment;

import java.util.ArrayList;
import java.util.Arrays;

public class State {

	public int period;
	public int initialInventoryA;
	public int initialInventoryB;
	
	public State(int period, int initialInventoryA, int initialInventoryB) {
		this.period = period;
		this.initialInventoryA = initialInventoryA;
		this.initialInventoryB = initialInventoryB;
	}
	
	public String toString() {
	   return this.period + " " + this.initialInventoryA + " " + this.initialInventoryB;
	}
	
	public int hashCode() {
		String hash = "";
		hash = (hash + period) + "_" + this.initialInventoryA + " " + this.initialInventoryB;
		return hash.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o instanceof State)
			return ((State) o).period == this.period &&
				   ((State) o).initialInventoryA == this.initialInventoryA &&
				   ((State) o).initialInventoryB == this.initialInventoryB;
		else
			return false;
	}
	
	public static State parse(String state) {
	   /*
	    * Takes a string of type "125 65"
	    * Splits where it finds a space, {"125","65"}
	    * Parses each of the elements into an integer
	    */
	   String[] str = state.split(" ");
	   int[] levels = new int[3];
	   int period = Integer.parseInt(str[0]);
	   for(int i = 1; i< str.length; i++) {
	      levels[i] = Integer.parseInt(str[i]);
	   }
	   return new State(period, levels[0],levels[1]);
	}
	
	/** generate feasible actions for a given state **/
	public static ArrayList<int[]> generateFeasibleActions(State state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		State newState = null;
		
		if((state.initialInventoryA <=0)&&(state.initialInventoryB <= 0)) {//case 4: no transshipment
			System.out.println("case 4");
			for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
				for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
					if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
				}
			}
			return actions;
		}else if((state.initialInventoryA <=0)&&(state.initialInventoryB > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
			System.out.println("case 3");
			for(int t=0; t<=state.initialInventoryB; t++) {
				newState = new State(state.period, state.initialInventoryA + t, state.initialInventoryB - t);	//update inventory level after transshipment
				for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
					for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {-t, i, j});
					}
				}
			}
			return actions;
		}else if((state.initialInventoryA > 0)&&(state.initialInventoryB <= 0)) {//case 2: transship from 1 to 2, transship >= 0
			System.out.println("case 2");
			for(int t=0; t<=state.initialInventoryA; t++) {
				newState = new State(state.period, state.initialInventoryA - t, state.initialInventoryB + t);	//update inventory level after transshipment
				for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
					for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
					}
				}
			}
			return actions;
		}else {										//case 1: transhsip undirected
			//int[] feasibleTransshipment = new int[state.i1 + state.i2 +1];
			System.out.println("case 1");
			for(int t=-state.initialInventoryB; t<= state.initialInventoryA; t++) {				
				newState = new State(state.period, (state.initialInventoryA)-t, (state.initialInventoryB)+t);
				for(int i=0; i <= instance.maxInventory - newState.initialInventoryA; i++) {
					for(int j=0; j <= instance.maxInventory - newState.initialInventoryB; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
					}
				}
			}
			return actions;
		}
		
		/**  **
		
		public static double getOptimalCost(State s){
		   return this.cost.get(bla bla)
		}
		public static double getOptimalAction(State s);
		public static void store(State, Action a, double c);
		
		**/
		
	}
	
	/**test**/
	public static void main(String args[]) {
		
		/**problem input**/
		 int[] demandMean = {20, 40,60,40};
		 int maxInventory = 5;
		 int minInventory = -5;
		 int maxQuantity = 10;
		 double K = 10;
		 double z = 0;
		 double R = 7;
		 double v = 0;
		 double h = 1;
		 double b = 5;
		 double tail = 0.000000000001;
		 
		 LTinstance instance = new LTinstance(demandMean, demandMean, maxInventory, minInventory, maxQuantity, K, z, R, v, h, b, tail);
		
		ArrayList<int[]> actions = null;
		State stateTest = new State(1, 1,2);
		actions = generateFeasibleActions(stateTest, instance);
		for(int i=0; i<actions.size(); i++) {
			System.out.println(Arrays.toString(actions.get(i)));
		}
		
	}
}
