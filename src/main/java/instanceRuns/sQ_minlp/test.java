package instanceRuns.sQ_minlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;

public class test {
	
    /**
     * 写入TXT，覆盖原内容
     * @param content
     * @param fileName
     * @return
     * @throws Exception
     */
    public static boolean writeTxtFile(String content,File fileName)throws Exception{
        RandomAccessFile mm=null;
        boolean flag=false;
        FileOutputStream fileOutputStream=null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(content.getBytes("gbk"));
            fileOutputStream.close();
            flag=true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
    
    public static void main(String args[]) throws Exception {
    	//String FileName = "scr/main/java/instanceRuns/sQ_minlp/s_intermediate.txt";
    	File file = new File("src/main/java/instanceRuns/sQ_minlp/temp.txt");
    	String content = "target";
    	boolean flag = writeTxtFile(content, file);
    	String content2 = "tdfad";
    	flag = writeTxtFile(content2, file);
    	int a = 3432;
    	String a_s = Integer.toString(a);
    	flag = writeTxtFile(a_s, file);
    	
        FileReader fr = new FileReader("src/main/java/instanceRuns/sQ_minlp/temp.txt");
        BufferedReader br = new BufferedReader(fr);
        String s="";
        s=br.readLine();
        int result = Integer.parseInt(s);
        System.out.println(s);
        System.out.println(result+1);
    }

}
