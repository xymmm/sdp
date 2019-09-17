package RS.milp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class RS_MILP_costPlot {
	
	/**
	 * Read costs of RS by MILP/CPLEX from text file.
	 * This method returns cost values as String.
	 * **/
    public static String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        String values = result.toString();
        return values;
    }
    
    public static double[] convertStringToDouble(double[] RScostMILP, String result) {
    	for(int i=0; i<RScostMILP.length;i++) {
    		RScostMILP[i] = Double.valueOf(result);
    	}
    	return RScostMILP;
    }
    
    public static void main(String[] args){
    	
    	int minInventory = -500;
    	int maxInventory = 500;
    	int inventoryLength = maxInventory-minInventory+1;
    	
        File file = new File("F:/EclipseInstall/sdp/src/main/java/RS/milp/ofile1.txt");
        //System.out.println(txt2String(file));
        
        double[] RScostMILP = new double[inventoryLength];
        RScostMILP = convertStringToDouble(RScostMILP, txt2String(file));
    }
    
    

	


}
