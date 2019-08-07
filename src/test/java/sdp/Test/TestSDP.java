package sdp.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sdp.SDP;
import sdp.data.Instance;
import sdp.data.Solution;

public class TestSDP {
   
   Instance instance;

   @Before
   public void setUp() throws Exception {
      /*** Problem instance ***/
      double fixedOrderingCost = 100;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 10;
      int[] demandMean = {20,40,60,40};
      
      
      /** SDP boundary conditions **/
      double tail = 0.00000001;
      
      int maxDemand = 250;
      int minInventory = -250;
      int maxInventory = 250;
      int maxQuantity = 250;


      instance = new Instance(
            fixedOrderingCost,
            unitCost,
            holdingCost,
            penaltyCost,
            demandMean,
            tail,
            maxDemand,
            minInventory,
            maxInventory,
            maxQuantity
            );
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void test_1() {
      double expectedTotalCost = 481.05454874240246;
      Solution solution = SDP.solveInstance(instance);
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }
   
   @Test
   public void test_2() {
      double expectedTotalCost = 639.9999802676688;
      Solution solution = SDP.solveInstance(instance);
      assertEquals(
            "Values not equal. Expected: " + expectedTotalCost + "; Actual: " + solution.optimalCost[instance.maxInventory-instance.minInventory][0],
            expectedTotalCost,
            solution.optimalCost[instance.maxInventory-instance.minInventory][0],
            0.001);
   }

}
