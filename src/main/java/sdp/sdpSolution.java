package sdp;

public class sdpSolution {

   public int[] inventory;
   
   /** Explain meaning of indexes **/
   public double optimalAction[][];
   public double optimalCost[][];
   
   public sdpSolution(double optimalAction[][], double optimalCost[][], int[] inventory) {
      this.optimalAction = optimalAction;
      this.optimalCost = optimalCost;
      this.inventory = inventory;
   }

}