package LT_sdp_HashMap;

import java.util.Set;

public class costStatePair {

	public double currentStageETC;
	public Set<State> futureStates;
	
	public costStatePair(double currentStageETC, Set<State> futureStates) {
		this.currentStageETC = currentStageETC;
		this.futureStates = futureStates;
	}
	
}
