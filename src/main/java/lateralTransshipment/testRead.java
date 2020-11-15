package lateralTransshipment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

public class testRead {

	@SuppressWarnings("resource")
	public static double[][] readFromtxt(double[][] array) throws Exception {

		Scanner sc = new Scanner( new BufferedReader(new FileReader("src/main/java/lateralTransshipment/optimalCostToRead.txt")));

		while(sc.hasNextLine()) {
			for (int i=0; i<array.length; i++) {
				String[] line = sc.nextLine().trim().split("\t");
				for (int j=0; j<line.length; j++) {
					array[i][j] = Double.parseDouble(line[j]);
				}
			}
		}
		System.out.println(Arrays.deepToString(array));
		return array;
	}
	
	public static void main(String args[]) {
		double[][] array = new double[1][4];
		try {
			array = readFromtxt(array);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
