package LT_sdp_HashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class BuildHashTable {

	public HashMap<int[], Double> LT;
	
	public BuildHashTable(HashMap LT) {
		this.LT = LT;
	}
	
	/** print states when they are stored as int array*
	public static void printState(HashMap<int[], Double> map) {
		for(int i=0; i<map.size(); i++) {
			Set<int[]> statePresent = map.keySet();
			System.out.println(Arrays.deepToString(statePresent.toArray()));
		}
	}*/
	
}
