package temp_hashtable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class GFG {
	
	/** With help of HashTable (a synchronised implementation of hashing**/
	public static void main(String args[]) {
		
		//create a HashTable to store String values corresponding to integer keys
		//Hashtable<Integer, String>  hm = new Hashtable<Integer, String>();
		
		HashMap<int[], Double> LT = new HashMap<int[], Double>();
		//List<Integer> state = Arrays.asList(1,2,3);
		int[] state = {1,2,3};
		LT.put(state, 1.2);
		for(int i=0; i<LT.size(); i++) {
			//Set<Entry<int[],Double>> statePresent = LT.entrySet();
			Set<int[]> stateContained = LT.keySet();
			System.out.println(Arrays.deepToString(stateContained.toArray()));
			//System.out.println("state = "+LT.keySet()+", value = " + LT.values());
		}
		
		
		//input the values for String hash table
		//hm.put(1, "Geeks");
		//hm.put(12,"forGeeks");
		//hm.put(15, "A computer");

		//System.out.println(hm);	//uninterpretable for arrays
				
		//int[] arr = {10, 34, 5, 10, 3, 5, 10};
		//createHashMap(arr);
		
		/** LinkedHashMap (similar to HashMap, but keeps order of elements *
		LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
        lhm.put("one", "practice.geeksforgeeks.org");  
        lhm.put("two", "code.geeksforgeeks.org");  
        lhm.put("four", "quiz.geeksforgeeks.org");
        // It prints the elements in same order   
        // as they were inserted      
        System.out.println(lhm);  
    
        System.out.println("Getting value for key 'one': " + lhm.get("one"));  
        System.out.println("Size of the map: " + lhm.size());  
        System.out.println("Is map empty? " + lhm.isEmpty());  
        System.out.println("Contains key 'two'? "+ hm.containsKey("two"));  
        System.out.println("Contains value 'practice.geeks" +"forgeeks.org'? "+ lhm.containsValue("practice"+ ".geeksforgeeks.org"));  
        System.out.println("delete element 'one': " + lhm.remove("one"));  
        System.out.println(lhm); */
	}
	
	/** With the help of HashMap (a non-synchronised faster implementation of hashing **/
	//to create HashMap from an array by taking the elements as keys and the frequencies as the values
	static void createHashMap(int[] arr) {
		//create an empty HashMap
		HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
		//traverse through the given array
		for(int i=0; i<arr.length; i++) {
			//get if the element exists
			Integer c = hmap.get(arr[i]);
			//if this is the first occurence of element, insert the element
			if(hmap.get(arr[i]) == null){
				hmap.put(arr[i], 1);
			}else {	//if elements already exits in hash map, increment the count of element by 1
				hmap.put(arr[i], ++c);
			}
		}
		System.out.println(hmap);
	}






}




