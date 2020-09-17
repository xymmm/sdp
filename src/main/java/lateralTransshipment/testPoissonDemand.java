package lateralTransshipment;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class testPoissonDemand {
	
	public static void main(String[] args) {
		
		double[] costs = {1.23, 5.565, 56.248, 2.014, 0.02589, 2.36587};
		double minCost = Arrays.stream(costs).min().getAsDouble();
		System.out.println(minCost);
		
		int firstMinCostIdx = sdp.util.globalMinimumIndex.getGlobalMinimumJavaIndex(costs);
		
		System.out.println(firstMinCostIdx);
	}

}
