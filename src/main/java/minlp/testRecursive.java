package minlp;

public class testRecursive {
	
	public static void testR (int i1) {
		int i2 = Integer.MIN_VALUE;
		do {
			if(i1>0) {
				i2 = i1;
				System.out.println("i2 = "+i2);
				break;
			}else {
				testR(i1 + 1);
			}
		}while(i2 != Integer.MIN_VALUE);
	}

	
	
	
	public static void main(String[] args) {
		testR(-1);
	}
	
}
