package sdp.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sdp.SDP;
import sdp.sdpSolution;
import sdp.data.Instance;

public class TestSDP {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }
   
   public static Instance getInstanceA() {
      /*** Problem instance ***/
      double fixedOrderingCost = 178;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 4;
      int[] demandMean = {20, 10, 93, 29, 49, 97, 37, 60, 38, 47};
      
      
      /** SDP boundary conditions **/
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
   
   public static Instance getInstanceB() {
      /*** Problem instance ***/
      double fixedOrderingCost = 336;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 5;
      int[] demandMean = {50, 12, 97, 27, 74, 59, 7, 46, 78, 63};
      
      
      /** SDP boundary conditions **/
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
   
   public static Instance getInstanceC() {
      /*** Problem instance ***/
      double fixedOrderingCost = 122;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 2;
      int[] demandMean = {6, 18, 44, 54, 26, 50, 54, 73, 18, 53};
      
      
      /** SDP boundary conditions **/
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
      
      sdpSolution solution = SDP.solveInstance(instance,false);
      
      double expectedTotalCost = 1100.4741964293196;
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }
   
   @Test
   public void testInstanceB() {
      Instance instance = getInstanceB();
      
      sdpSolution solution = SDP.solveInstance(instance,false);
      
      double expectedTotalCost = 1765.903451217189;
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }
   
   @Test
   public void testInstanceC() {
      Instance instance = getInstanceC();
      
      sdpSolution solution = SDP.solveInstance(instance,false);
      
      double expectedTotalCost = 724.244847692616;
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }

}
