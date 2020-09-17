package lateralTransshipment;

import java.util.ArrayList;

import lateralTransshipment.LT2locations.State;

public class actionTest {

	/** generate feasible actions for a given state **/
	public ArrayList<int[]> generateFeasibleActions(State state, LTinstance instance){
		ArrayList<int[]> actions = new ArrayList<int[]>();
		State newState = null;

		if((state.initialInventoryA <=0)&&(state.initialInventoryB <= 0)) {//case 4: no transshipment
			//System.out.println("case 4");
			for(int i=0; i <= instance.maxInventory - state.initialInventoryA; i++) {
				for(int j=0; j <= instance.maxInventory - state.initialInventoryB; j++) {
					if((i<=instance.maxQuantity)&&(j<=instance.maxQuantity)) actions.add(new int[] {0, i, j});
				}
			}
			return actions;
		}else if((state.initialInventoryA <=0)&&(state.initialInventoryB > 0)) {//case 3: transship from 2 to 1, transshipment <= 0
			//System.out.println("case 3");
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
			//System.out.println("case 2");
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
			//System.out.println("case 1");
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
	}
	
	public static void main(String[] args) {
		int[] demandMean1 = {2, 4};
		int[] demandMean2 = {6, 4};
		int maxInventory  = 10;
		int minInventory  = -10;
		int maxQuantity   = 10;
		double K = 10;
		double z = 0;
		double R = 5;
		double v = 0;
		double h = 1;
		double b = 3; 
		double tail = 0.000000000001;

		LTinstance instance = new LTinstance(demandMean1,demandMean2,maxInventory,minInventory,maxQuantity,K,z,R,v,h,b,tail);
		
		State initialState = new State(1, 1, 1);
		
	}
	
}
