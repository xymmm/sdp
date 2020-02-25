package RecedingHorizon.sQt;

public class RHsolution_sQt {
	
	public double[] scheduleQ;
	public double[] scheduleCurrentCost;
	public double	totalCost;
	
	public RHsolution_sQt(double[] scheduleQ, double[] scheduleCurrentCost, double totalCost) {
		this.scheduleQ = scheduleQ;
		this.scheduleCurrentCost = scheduleCurrentCost;
		this.totalCost = totalCost;
	}

}
