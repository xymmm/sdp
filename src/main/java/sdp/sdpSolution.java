package sdp;

import sdp.data.Instance;

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
   
   public int gets(double optimalAction[][], double optimalCost[][], int[] inventory, Instance instance) {
	   int s;
	   optimalCost = transpose(optimalCost);
	   double minCost = getMinimum(optimalCost[0]);
		   s = getIndex(minCost + instance.fixedOrderingCost, optimalCost[0]) - (inventory.length - 1)/2;
	   return s;
   }
   
   public int[] getS(double optimalAction[][], double optimalCost[][], int[] inventory, Instance instance) {
	   int[] S = new int[optimalAction[0].length];
	   optimalCost = transpose(optimalCost);
	   for(int t=0;t<S.length;t++) {
		   S[t] = getMinimumIndex(optimalCost[t]) - (inventory.length - 1)/2;
	   }
	   return S;
   }
 
   private double getMinimum(double[] arr) {
	   double min = arr[0];
	   for(int i=1;i<arr.length;i++) {
		   if(arr[i]<min) {
			   min = arr[i];
		   }
	   }
	   return min;
   }
   
   private int getMinimumIndex(double[] arr) {
	   double min = arr[0];
	   int index = 0;
	   for(int i=1;i<arr.length;i++) {
		   if(arr[i]<min) {
			   min = arr[i];
			   index = i;
		   }
	   }
	   return index;
   }
   
   private int getIndex(double value, double[] arr) {
	   int index= 0;
	   for(int i=0;i<arr.length;i++) {
		   if(arr[i] == value) {
			   index = i;
		   }
	   }
	   return index;
   }
   
   private double[][] transpose(double[][] arr) {
	   double[][] arrT = new double [arr[0].length][arr.length];
	   for(int i=0; i<arrT.length;i++) {
		   for(int j=0; j<arrT[0].length; j++) {
			   arrT[i][j] = arr[j][i];
		   }
	   }
	   return arrT;
   }

}