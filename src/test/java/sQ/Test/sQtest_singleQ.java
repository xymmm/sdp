package sQ.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sQ.sQ;
import sQ.sQsolution;
import sdp.sdpSolution;
import sdp.data.Instance;

public class sQtest_singleQ {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public static Instance getInstanceClassic() {
	      // Problem instance
	      double fixedOrderingCost = 100;
	      double unitCost = 0;
	      double holdingCost = 1;
	      double penaltyCost = 10;
	      int[] demandMean = {20,40,60,40};
	      
	      // SDP boundary conditions
	      double tail = 0.00000001;
	      int minInventory = -250;
	      int maxInventory = 250;
	      int maxQuantity = 1000;

	      return new Instance(
	            fixedOrderingCost,
	            unitCost,
	            holdingCost,
	            penaltyCost,
	            demandMean,
	            tail,
	            minInventory,
	            maxInventory,
	            maxQuantity
	            );
	   }
	   @Test
	   public void testInstanceClassic() {
	      Instance instance = getInstanceClassic();
	      
	     sQsolution sQsolution = sQ.solvesQInstance(instance);
	      
	     /*
	      double expectedTotalCost = 2500.0;
	      assertEquals(
	            expectedTotalCost,
	            sQsolution.CostNoAction[0][3],
	            0.001);
	      */
	      
	      double expectedTotalCost = 5.7411040216635155E17;
	      assertEquals(
	    		  expectedTotalCost,
	    		  sQsolution.totalCost[0][0][2],
	    		  0.001
	    		  );
	   }
	   
/************************************* Instance A **********************************************************/	
	   
	public static Instance getInstanceA() {
	      // Problem instance
	      double fixedOrderingCost = 178;
	      double unitCost = 0;
	      double holdingCost = 1;
	      double penaltyCost = 4;
	      int[] demandMean = {20, 10, 93, 29, 49, 97, 37, 60, 38, 47};
	      	      
	      // SDP boundary conditions
	      double tail = 0.0001;
	      int minInventory = -1000;
	      int maxInventory = 1000;
	      int maxQuantity = 1000;

	      return new Instance(
	            fixedOrderingCost,
	            unitCost,
	            holdingCost,
	            penaltyCost,
	            demandMean,
	            tail,
	            minInventory,
	            maxInventory,
	            maxQuantity
	            );
	   }
	   @Test
	   public void testInstanceA() {
	      Instance instance = getInstanceA();
	      
	      sQsolution sQsolution = sQ.solvesQInstance(instance);
	      
	      double expectedTotalCost = 9.571778965387995E20;
	      assertEquals(
	            expectedTotalCost,
	            sQsolution.totalCost[0][0][8],
	            0.001);
	   }
	   	   
	   /************************************* Instance B **********************************************************/	
	   	   
	   public static Instance getInstanceB() {
	      // Problem instance
	      double fixedOrderingCost = 336;
	      double unitCost = 0;
	      double holdingCost = 1;
	      double penaltyCost = 5;
	      int[] demandMean = {50, 12, 97, 27, 74, 59, 7, 46, 78, 63};
	    
   	      //SDP boundary conditions
	      double tail = 0.0001;
	      int minInventory = -1000;
	      int maxInventory = 1000;
	      int maxQuantity = 1000;

	      return new Instance(
	            fixedOrderingCost,
	            unitCost,
	            holdingCost,
	            penaltyCost,
	            demandMean,
	            tail,
	            minInventory,
	            maxInventory,
	            maxQuantity
	            );
	   }
	   @Test
	   public void testInstanceB() {
	      Instance instance = getInstanceB();
	      
	      sQsolution sQsolution = sQ.solvesQInstance(instance);
	      
	      double expectedTotalCost = 1.2041562679020247E28;
	      assertEquals(
	            expectedTotalCost,
	            sQsolution.totalCost[0][0][8],
	            0.001);
	   }
	   
	   /************************************* Instance C **********************************************************/	
 
	   public static Instance getInstanceC() {
	      // Problem instance 
	      double fixedOrderingCost = 122;
	      double unitCost = 0;
	      double holdingCost = 1;
	      double penaltyCost = 2;
	      int[] demandMean = {6, 18, 44, 54, 26, 50, 54, 73, 18, 53};
	            
	      // SDP boundary conditions
	      double tail = 0.0001;
	      int minInventory = -1000;
	      int maxInventory = 1000;
	      int maxQuantity = 1000;

	      return new Instance(
	            fixedOrderingCost,
	            unitCost,
	            holdingCost,
	            penaltyCost,
	            demandMean,
	            tail,
	            minInventory,
	            maxInventory,
	            maxQuantity
	            );
	   } 
	   @Test
	   public void testInstanceC() {
	      Instance instance = getInstanceC();
	      
	      sQsolution sQsolution = sQ.solvesQInstance(instance);
	      
	      double expectedTotalCost = 2.3525420541466773E23;
	      assertEquals(
	            expectedTotalCost,
	            sQsolution.totalCost[0][0][8],
	            0.001);
	   }
	   
}
