package temp_hashtable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public final class CustomKey<F,S> {
	private final F firstValue;
	private final S secondValue;
	private final int[] thirdValue;

	public CustomKey(final F firstValue,
			final S secondValue,
			final Integer[] thirdValue){
		this.firstValue = firstValue;
		this.secondValue = secondValue;
		this.thirdValue = unboxingArray(thirdValue);
	}

	public S getSecondValue() {
		return secondValue;
	}


	public F getFirstValue() {
		return firstValue;
	}

	public int[] getThirdValue() {
		return thirdValue;
	}

	private static int[] unboxingArray(final Integer[] array){
		final int[] result = new int[array.length];
		IntStream.range(0,array.length)
		.forEach(index -> result[index] =  array[index]);
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final CustomKey<?, ?> customKey = (CustomKey<?, ?>) o;
		return getFirstValue().equals(customKey.getFirstValue()) 
				&& getSecondValue().equals(customKey.getSecondValue()) 
				&& Arrays.equals(getThirdValue(), customKey.getThirdValue());

	}

	@Override
	public int hashCode() {
		int result = getFirstValue().hashCode();
		result = 31 * result + getSecondValue().hashCode();
		result = 31 * result + Arrays.hashCode(getThirdValue());
		return result;
	}

	//***********************************************************************************************
	
	public static void main(String[]args){
	    //Assume your Value is a String which does not really matter
	    Map<CustomKey<Integer,Integer>,String> customKeyMap =
	            new HashMap<>();
	    customKeyMap.put(new CustomKey<>(1,2,new Integer[]{1,2,3}),"First Value");
	    customKeyMap.put(new CustomKey<>(1,3,new Integer[]{1,2,3}),"Second Value");

	    //Can use anonymous instance since equals is implemented
	    //Expect Second Value
	    System.out.println(customKeyMap.get(new CustomKey<>(1,3,new Integer[]{1,2,3})));
	}
}
