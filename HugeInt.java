/*
*	Tony Wong; Brent Yurek
*	CS 342 - Project 3
*	RSA Encryption
*
*	This is the structure for the huge-unsigned-integer.
*	It takes one String parameter and constructs the coded array from it.
*	Operations on this class are handled in the separate HugeOps class.
*/


public class HugeInt {
	private int length;			// the number of digits
	private int hugeArr[];		// decimal-coded int array
	private String hugeStr;		// the number represented as a string
	private int offset = 48;	// difference from char '0' and integer value 0

	/*	
		The default constructor takes a string and converts it into
		a decimal-coded integer array.
	*/
	public HugeInt(String s) {
		hugeStr = s.replaceFirst("^0+(?!$)", "");	// removing leading zeros and storing string
		length = hugeStr.length();					// storing the length of the string
		hugeArr = new int[length];					// initializing a coded array of correct size

		/*
			The least significant digit is stored first at index 0.
			The most significant digit is stored last at index length-1.
		*/
		for(int i = 0; i < length; i++) {
			hugeArr[i] = (hugeStr.charAt(length-i-1) - offset);
		}
	}

	//	Retrieves the decimal integer that's at index i in the coded array.
	public int intAt(int i) {
		return hugeArr[i];
	}

	//	Retrieves the string representation of the huge int.
	public String getStr() {
		return hugeStr;
	}

	//	Retrieves the length of the number.
	public int getLength() {
		return length;
	}
}
