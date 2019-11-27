package minlp;

public class sQminlpInstance {
	
	public int[] demandMean;
	public double fixedCost;
	public double unitCost;
	public double holdingCost;
	public double penaltyCost;

	
	public int partitions;
	public int[] s_sdp;
	public int Q_minlp;

	public sQminlpInstance(
			int[] demandMean,
			double fixedCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,

			int partitions,
			int[] s_sdp,
			int Q_minlp
			) {
		this.demandMean = demandMean;
		this.fixedCost = fixedCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.partitions = partitions;
		this.s_sdp = s_sdp;
		this.Q_minlp = Q_minlp;
	}
		   
	   

}