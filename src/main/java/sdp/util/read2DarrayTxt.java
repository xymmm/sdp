package sdp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class read2DarrayTxt {
	
	public static double[][] getFile(String pathName) throws Exception {
        File file = new File(pathName);
        if (!file.exists())
            throw new RuntimeException("Not File!");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String str;
        List<double[]> list = new ArrayList<double[]>();
        while ((str = br.readLine()) != null) {
            int s = 0;
            String[] arr = str.split("\t");
            double[] dArr = new double[arr.length];
            for (String ss : arr) {
                if (ss != null) {
                    dArr[s++] = Double.parseDouble(ss);
                }

            }
            list.add(dArr);
        }
        br.close();
        int max = 0;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i).length)
                max = list.get(i).length;
        }
        double[][] array = new double[list.size()][max];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < list.get(i).length; j++) {
                array[i][j] = list.get(i)[j];
            }
        }
        return array;
	}
	
	public static void main(String[] args) {
		double[][] test = new double[3][4];
		try {
			test = getFile("src/main/java/sdp/util/test.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(test));
	}
}
