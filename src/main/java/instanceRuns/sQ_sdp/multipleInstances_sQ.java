package instanceRuns.sQ_sdp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import sQ.sdp.sQ;
import sQ.sdp.sQgivenQ;
import sQ.sdp.sQgivenQsolution;
import sQ.sdp.sQsolution;
import sS.sdp.sS;
import sS.sdp.sSsolution;
import sdp.data.Instance;

public class multipleInstances_sQ{
	
	/*
	 * Aim to resolve SDP for s,Q policy: with I0 = 0
	 * 
	 * cost (initial order = 1)
	 * Q, 
	 * s_t, 
	 * time_Q, 
	 * time_st.
	 * 
	 * Class needed: (to compute)
	 * sQ.sdp.sQ 		-> cost, Q and time_Q
	 * sQ.sdp.sQgivenQ	-> s_t and time_st
	 * 
	 * where to record results:
	 * cost	-> "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_cost.txt"
	 * Q	-> "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt"
	 * s_t	-> "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_reorderPoints.txt"
	 * time	-> "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_time.txt"
	 * 		*Note that for each instance, there are two value for time, one for Q/cost, another for s_t(total),
	 * 		 An empty line is followed after s_t of each instance.
	 */
	



	


}
