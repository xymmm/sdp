package sdp.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import sdp.data.Instance;

public class writeText {
	
	/**
	 * This class contains a method to write a variable to the given text.
	 * The type of this variable is predefined to be ----double----.
	 * write as System.out.println
	 * **/
	
	//sdp.util.writeText.writeInt(100, "src/main/java/instanceRuns/sQ_sdp/sQ_sdp_Q.txt");
	
	public static void writeDouble(double value, String fileName) {
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(value);
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeInt(int value, String fileName) {
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(value);
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeLong(long time, String fileName) {
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(time);
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeNewLine(String fileName) {
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println();
		pw.println("====================================");
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeIntArray(int[] arr, String fileName) {
		FileWriter fw = null;
		try {
			File f = new File(fileName);
			fw = new FileWriter(f, true);//true, continue to write
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(fw);
		pw.println(Arrays.toString(arr));
		pw.flush();
		try {
			fw.flush();
			pw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
