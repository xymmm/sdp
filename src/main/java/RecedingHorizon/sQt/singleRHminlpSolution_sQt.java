package RecedingHorizon.sQt;

public class singleRHminlpSolution_sQt {
	public double[] Q;
	public double[] stockhlb;
	public double[] stockplb;
	
	
	public singleRHminlpSolution_sQt(double[] Q, double[] stockhlb, double[] stockplb){
		this.stockhlb = stockhlb;
		this.stockplb = stockplb;
		this.Q		  = Q;
	}
}
