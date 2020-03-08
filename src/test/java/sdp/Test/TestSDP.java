package sdp.Test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sS.sS;
import sS.sSsolution;
import sdp.data.Instance;
import sdp.data.InstanceDouble;

public class TestSDP {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }
   
   public static InstanceDouble getInstanceA() {
      /*** Problem instance ***/
      double fixedOrderingCost = 178;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 4;
      double[] demandMean = {20, 10, 93, 29, 49, 97, 37, 60, 38, 47};
      
      
      /** SDP boundary conditions **/
      double tail = 0.0001;
      int minInventory = -1000;
      int maxInventory = 1000;
      int maxQuantity = 1000;
      
      double stdParameter = 0.25;


      return new InstanceDouble(
            fixedOrderingCost,
            unitCost,
            holdingCost,
            penaltyCost,
            demandMean,
            tail,
            minInventory,
            maxInventory,
            maxQuantity,
            stdParameter
            );
   }
   
   public static InstanceDouble getInstanceB() {
      /*** Problem instance ***/
      double fixedOrderingCost = 336;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 5;
      double[] demandMean = {50, 12, 97, 27, 74, 59, 7, 46, 78, 63};
      
      
      /** SDP boundary conditions **/
      double tail = 0.0001;
      int minInventory = -1000;
      int maxInventory = 1000;
      int maxQuantity = 1000;

      double stdParameter = 0.25;
      
      return new InstanceDouble(
            fixedOrderingCost,
            unitCost,
            holdingCost,
            penaltyCost,
            demandMean,
            tail,
            minInventory,
            maxInventory,
            maxQuantity,
            stdParameter
            );
   }
   
   public static InstanceDouble getInstanceC() {
      /*** Problem instance ***/
      double fixedOrderingCost = 122;
      double unitCost = 0;
      double holdingCost = 1;
      double penaltyCost = 2;
      double[] demandMean = {6, 18, 44, 54, 26, 50, 54, 73, 18, 53};
      
      
      /** SDP boundary conditions **/
      double tail = 0.0001;
      int minInventory = -1000;
      int maxInventory = 1000;
      int maxQuantity = 1000;
		
      
      double stdParameter = 0.25;

      return new InstanceDouble(
            fixedOrderingCost,
            unitCost,
            holdingCost,
            penaltyCost,
            demandMean,
            tail,
            minInventory,
            maxInventory,
            maxQuantity,
            stdParameter
            );
   }
   
   @Test
   public void testInstanceA() {
      InstanceDouble instance = getInstanceA();
      
      sSsolution solution = sS.solveInstance(instance,false, false);
      
      double expectedTotalCost = 1100.4741964293196;
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }
   
   @Test
   public void testInstanceB() {
      InstanceDouble instance = getInstanceB();
      
      sSsolution solution = sS.solveInstance(instance,false, false);
      
      double expectedTotalCost = 1765.903451217189;
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }
   
   @Test
   public void testInstanceC() {
      InstanceDouble instance = getInstanceC();
      
      sSsolution solution = sS.solveInstance(instance,false, false);
      
      double expectedTotalCost = 724.244847692616;
      assertEquals(
            expectedTotalCost,
            solution.optimalCost[-instance.minInventory][0],
            0.001);
   }

}
