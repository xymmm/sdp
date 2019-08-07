package sdp.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DemandTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testTabulateProbability() {
      double[] tabulatedProbabilities = Demand.tabulateProbability(50, 0.1);
      assertEquals(" The sum is: "+Arrays.stream(tabulatedProbabilities).sum(),
            1.0,
            Arrays.stream(tabulatedProbabilities).sum(),
            0.0001);
   }

}
