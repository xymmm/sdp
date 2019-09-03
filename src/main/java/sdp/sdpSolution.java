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
   
   public static int[] getsSDP(double optimalAction[][]) {
	   int[] s = new int [optimalAction[0].length];
	   optimalAction = transpose(optimalAction);
	   for(int t=0; t<optimalAction.length; t++) {
		   for(int i=0; i<optimalAction[0].length; i++) {
			   if(optimalAction[t][i] == 0) {
				   s[t] = i - (optimalAction[0].length - 1)/2 - 1;
				   break;
			   }
		   }
	   }
	   return s;
   }
   private static double[][] transpose(double[][] arr){
	   double[][] arrT = new double[arr[0].length][arr.length];
	   for(int i=0; i<arrT.length; i++) {
		   for(int j=0; j<arrT[0].length; j++) {
			   arrT[i][j] = arr[j][i];
		   }
	   }
	   return arrT;
   }

 


}