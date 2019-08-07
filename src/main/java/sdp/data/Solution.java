package sdp.data;

public class Solution {

   public int[] inventory;
   
   /** Explain meaning of indexes **/
   public double optimalAction[][];
   public double optimalCost[][];
   
   public Solution(double optimalAction[][], double optimalCost[][], int[] inventory) {
      this.optimalAction = optimalAction;
      this.optimalCost = optimalCost;
      this.inventory = inventory;
   }

}