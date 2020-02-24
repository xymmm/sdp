package minlp_Normal;

public class sQminlpInstanceNormal {
	
	public double[] demandMean;
	public double fixedCost;
	public double unitCost;
	public double holdingCost;
	public double penaltyCost;
	public int minInventory;
	public int maxInventory;
	public int initialStock;
	public double stdParameter;
	
	public int partitions;
	public double[] means;
	public double[] piecewiseProb;
	public double error;
	
	public int[] s_sdp;
	public double Q_minlp;

	public sQminlpInstanceNormal(
			double[] demandMean,
			double fixedCost,
			double unitCost,
			double holdingCost,
			double penaltyCost,
			int minInventory,
			int maxInventory,
			double stdParameter,

			int partitions,
			double[] means,
			double[] piecewiseProb,
			double error,
			
			int[] s_sdp,
			double Q_minlp
			) {
		this.demandMean = demandMean;
		this.fixedCost = fixedCost;
		this.unitCost = unitCost;
		this.holdingCost = holdingCost;
		this.penaltyCost = penaltyCost;
		this.partitions = partitions;
		this.error = error;
		this.means = means;
		this.piecewiseProb = piecewiseProb;
		this.s_sdp = s_sdp;
		this.minInventory = minInventory;
		this.maxInventory = maxInventory;

	}
	
	   public int getQ() {
		      return (int) this.Q_minlp;
		   }
		   
	   

}
