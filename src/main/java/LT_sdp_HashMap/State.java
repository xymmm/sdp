package LT_sdp_HashMap;

import java.util.ArrayList;
import java.util.Arrays;

public class State {

	public int i1;
	public int i2;
	
	public State(int i1, int i2) {
		this.i1 = i1;
		this.i2 = i2;
	}
	
	public String toString() {
	   return this.i1 + " " + this.i2;
	}
	
	public static State parse(String state) {
	   /*
	    * Takes a string of type "125 65"
	    * Splits where it finds a space, {"125","65"}
	    * Parses each of the elements into an integer
	    */
	   String[] str = state.split(" ");
	   int[] levels = new int[2];
	   for(int i = 0; i< str.length; i++) {
	      levels[i] = Integer.parseInt(str[i]);
	   }
	   return new State(levels[0],levels[1]);
	}
	
	/** generate feasible actions for a given state **/
	public static ArrayList<int[]> generateFeasibleActions(State state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		State newState = null;
		
		if((state.i1 <=0)&&(state.i2 <= 0)) {//case 4: no transshipment
			System.out.println("case 4");
			for(int i=0; i <= instance.maxInventory - state.i1; i++) {
				for(int j=0; j <= instance.maxInventory - state.i2; j++) {
					if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
				}
			}
			return actions;
		}else if((state.i1 <=0)&&(state.i2 > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
			System.out.println("case 3");
			for(int t=0; t<=state.i2; t++) {
				newState = new State(state.i1 + t, state.i2 - t);	//update inventory level after transshipment
				for(int i=0; i <= instance.maxInventory - state.i1; i++) {
					for(int j=0; j <= instance.maxInventory - state.i2; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {-t, i, j});
					}
				}
			}
			return actions;
		}else if((state.i1 > 0)&&(state.i2 <= 0)) {//case 2: transship from 1 to 2, transship >= 0
			System.out.println("case 2");
			for(int t=0; t<=state.i1; t++) {
				newState = new State(state.i1 - t, state.i2 + t);	//update inventory level after transshipment
				for(int i=0; i <= instance.maxInventory - state.i1; i++) {
					for(int j=0; j <= instance.maxInventory - state.i2; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
					}
				}
			}
			return actions;
		}else {										//case 1: transhsip undirected
			//int[] feasibleTransshipment = new int[state.i1 + state.i2 +1];
			System.out.println("case 1");
			for(int t=-state.i2; t<= state.i1; t++) {				
				newState = new State((state.i1)-t, (state.i2)+t);
				for(int i=0; i <= instance.maxInventory - newState.i1; i++) {
					for(int j=0; j <= instance.maxInventory - newState.i2; j++) {
						if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {t, i, j});
					}
				}
			}
			return actions;
		}
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
		State stateTest = new State(1,2);
		actions = generateFeasibleActions(stateTest, instance);
		for(int i=0; i<actions.size(); i++) {
			System.out.println(Arrays.toString(actions.get(i)));
		}
		
	}
}
