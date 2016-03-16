/*
*	Tony Wong; Brent Yurek
*	CS 342 - Project 3
*	RSA Encryption
*
*	This class handles all the operations for the HugeInt class.
*	It's abstract to force inheritance to the KeyGen and Blocking classes,
*	which allows for cleaner method calls.
*
*	List of mathematical methods:
*		hugeAdd - addition
*		hugeSub - subtraction
*		hugeMul - multiplication
*		hugeDiv - division
*		hugeMod - modulus
*		hugeCmp - comparison
*		hugeShiftL - arithmetic shift left
*		hugeShiftR - arithmetic shift right
*
*	List of utility methods:
*		hugePrint - prints out the number as a String
*		reverse - reverses a String
*/

abstract public class HugeOps {
	/*
		Takes two huge ints, adds them, and creates a huge int.
		Wrapper function that sends parameters to helper in longest-first order.
	*/
	public static HugeInt hugeAdd(HugeInt a, HugeInt b) {
		if(a.getLength() >= b.getLength()) {
			return _hugeAdd(a, b);
		}
		else {
			return _hugeAdd(b, a);
		}
	}

	/*	
		Helper function to add two huge ints.
		Algorithm is standard addition, the same that's taught in elementary school.
		Only modification is that the first number has to be longer or equal in length
		to the second number, which is guaranteed by the wrapper function.
		Runs in O(N) time.
	*/
	private static HugeInt _hugeAdd(HugeInt a, HugeInt b) {
		int longer = a.getLength();		// upper bound of the outer addition operation
		int shorter = b.getLength();	// upper bound of the inner addition operation
		int carry = 0;					// the carry digit i from a[i-1] + b[i-1]
		int sumDigit = 0;				/* 
										   the result of a[i] + b[i] + carry[i]
										   NOTE: the "new" carry isn't calculated until
										   after the sumDigit is calculated
										*/
		String sum = "";				// the accumulator that represents the sum as a String

		/*
			The inner addition operation that is bounded by the shorter huge int.
			e.g.,	let A be	aaaaaaa
					let B be	   bbbb
					A + B =		aaaaaaa
								+  bbbb
								_______
								???XXXX	**the inner addition calculates the XXXX part of the sum
		*/
		for(int i = 0; i < shorter; i++) {
			sumDigit = a.intAt(i) + b.intAt(i) + carry;		// adding a slice of digits
			//	The case if the sum slice is greater than what can be represented by a single digit.
			if(sumDigit > 9) {
				sum += (sumDigit - 10);		/*
											   an integer gets casted into a String
											   NOTE: each slice is added to the sum in REVERSE order
											   this ordering will be fixed at the end
											*/
				carry = 1;					// the overflow needs to be handled by the next slice
			}
			//	The case if the sum slice can be represented by a single digit (0-9).
			else {
				sum += sumDigit;			// the result can be appended directly to the sum String
				carry = 0;					// no overflow resulted from add
			}
		}
		
		/*
			The outer addition operation that is upper-bounded by the longer huge int and
			lower-bounded by the shorter huge int.
			e.g.,	let A be	aaaaaaa
					let B be	   bbbb
					A + B =		aaaaaaa
								+  bbbb
								_______
								YYYXXXX	**the outer addition calculates the YYY part of the sum
		*/
		for(int i = shorter; i < longer; i++) {
			sumDigit = a.intAt(i) + carry;	// adding only a[i] because b no longer has a digit here
			//	The case if the sum slice is greater than 9.
			if(sumDigit > 9) {
				sum += (sumDigit - 10);		// append least significant digit to sum String
				carry = 1;					// the overflow needs to be handled by the next slice
			}
			//	The case if the sum slice is between 0-9 inclusive.
			else {
				sum += sumDigit;			// append result to sum String
				carry = 0;					// technically, the carry is no longer needed from here on
			}
		}
		
		/*
			Handles the remaining carry if present.
			e.g.,	let A be	aaaa
					let B be	bbbb
					A + B =		aaaa
							   +bbbb
							   _____
							   CXXXX	**this operation calculates the C part of the sum
		*/
		if(carry == 1) {
			sum += 1;
		}

		//	The sum String is in reverse order at this point and needs to be fixed.
		sum = reverse(sum);						// sum is now corrected
		HugeInt result = new HugeInt(sum);		// create the huge int that represents the sum
		return result;							// return the huge int sum
	}

	/*
		Takes two huge ints, subtracts the second parameter from the first, and creates a huge int.
		Wrapper function that checks if the second huge int is indeed smaller than the first before
		sending values to the helper.
		Program exits on error.
	*/
	public static HugeInt hugeSub(HugeInt a, HugeInt b) {
		//	If b is larger than a, then we have an error. Program will have unintended effects.
		if(hugeCmp(a, b) == -1) {
			System.out.println("RESULTS ARE INVALID. NEGATIVE DIFFERENCES ARE NOT ACCOUNTED FOR.");
			System.exit(-1);	
		}
		return _hugeSub(a, b);
	}

	/*
		Helper function to subtract two huge ints.
		Algorithm is standard subtraction, the same that's taught in elementary school.
		Only modification is that the first number has to be longer or equal in length
		to the second number, which is guaranteed by the wrapper function.
		Runs in O(N) time.
	*/
	private static HugeInt _hugeSub(HugeInt a, HugeInt b) {
		int longer = a.getLength();		// upper bound on the outer subtraction operation
		int shorter = b.getLength();	// upper bound on the inner subtraction operation
		int carry = 0;					// accounts if a carry was needed
		int difDigit = 0;				// the result of a subtraction slice
		String difference = "";			// the accumulator that represents the difference as a String
		
		//	The inner subtraction operation that is bounded by a length of 0 to the length of b
		for(int i = 0; i < shorter; i++) {
			// if the digit at a[i] is smaller than b[i], then a carry operation is needed
			if((a.intAt(i) - carry) < b.intAt(i)) {
				difDigit = a.intAt(i) - b.intAt(i) - carry + 10;
				carry = 1;				// essentially subtracts one a[i+1]
			}
			// if the digit at a[i] is of sufficient size to perform normal subtraction
			else {
				difDigit = a.intAt(i) - b.intAt(i) - carry;
				carry = 0;
			}
			difference += difDigit;		// appends difference slice in reverse order to final String
		}

		//	The outer subtraction operation that handles the remainding digits of the longer String
		for(int i = shorter; i < longer; i++) {
			// only happens when there was a propagation carry from inner operation
			if((a.intAt(i) - carry) < 0) {
				difDigit = a.intAt(i) - carry + 10;
				carry = 1;
			}
			// normal subtraction with no carry, stays in this state once here
			else {
				difDigit = a.intAt(i) - carry;
				carry = 0;
			}
			difference += difDigit;
		}

		//	The difference String is in reverse order at this point and needs to be fixed.
		difference = reverse(difference);						// reverses String, but not done yet
		difference = difference.replaceFirst("^0+(?!$)", "");	// removes leading zeros
		HugeInt result = new HugeInt(difference);
		return result;
	}

	/*
		Utility function that compares two huge ints a and b.
		Returns 0 if they are equal.
		Returns 1 if a is larger.
		Returns -1 if b is larger.
	*/
	public static int hugeCmp(HugeInt a, HugeInt b) {
		int lengthA = a.getLength();
		int lengthB = b.getLength();
		//	Check the lengths first as the longer huge int is inherently larger
		if(lengthA > lengthB) {
			return 1;		// huge int a is larger
		}
		else if(lengthA < lengthB) {
			return -1;		// huge int b is larger
		}
		/*
			If the huge ints are of the same length, then compare digits
			starting at the most significant.
		*/
		else {
			for(int i = (lengthA-1); i >=0; i--) {
				if(a.intAt(i) > b.intAt(i)) {
					return 1;		// huge int a is larger
				}
				else if(a.intAt(i) < b.intAt(i)) {
					return -1;		// huge int b is larger
				}
			}
		}
		//	If the comparison operation gets here, then the huge ints are the same
		return 0;					// huge int a = huge int b
	}

	/*
		Wrapper function that passes the longer number first to the helper function.
		Takes two huge ints and finds the product.
	*/
	public static HugeInt hugeMul(HugeInt a, HugeInt b) {
		if(a.getLength() >= b.getLength()) {
			return _hugeMul(a, b);
		}
		else {
			return _hugeMul(b, a);
		}
	}

	/*
		Helper function.
		Multiplies two huge ints together. If the numbers are short, then a naive
		algorithm is used. This is the same as the quadratic time "grade school"
		algorithm. If the numbers are large (defined by a length greater than the 
		pre-set cutoff), then Karatsuba's multiplication is used. This alternative
		finds the product in O(n^{lg3}) time.
	*/
	private static HugeInt _hugeMul(HugeInt a, HugeInt b) {
		int cutoff = 40;					// use Karatsuba's algorithm if longer than cutoff
		int lengthA = a.getLength();
		int lengthB = b.getLength();

		if((lengthA > cutoff) && (lengthB > cutoff)) {
			return karatsuba(a, b);
		}


		// naive quadratic time multiplication algorithm
		int carry;							// carries the most significant digit if necesesary
		int proDigit;						// the result of a multiplication slice
		String product;						// the String that keeps track of the product
		HugeInt[] array = new HugeInt[lengthB];		// an array that stores the subproducts
		//	Each digit of number b creates a subproduct
		for(int i = 0; i < lengthB; i++) {
			product = "";
			carry = 0;						// the start of a new subproduct has 0 carry
			//	Multiplication by 0 results in 0.	
			if(b.intAt(i) == 0) {
				array[i] = new HugeInt("0");		// add to subproduct array
			}
			//	Multiplication by 1 identity.
			else if(b.intAt(i) == 1) {
				product = a.getStr();
				for(int j = 0; j < i; j++) {
					product += 0;					// shifting the result to the correct power
				}
				array[i] = new HugeInt(product);	// add to subproduct array
			}
			
			else {
			//	Every digit of number a needs to be multiplied by b[i]
			for(int j = 0; j < lengthA; j++) {
				proDigit = b.intAt(i) * a.intAt(j) + carry;
				carry = 0;					// carry is a counter of the most significant digit
				while(proDigit > 9) {		// while the product slice can't be represented by a digit
					carry += 1;				// each multiple of 10
					proDigit -= 10;			// subtract a multiple of 10
				}
				product += proDigit;		// resulting digit is appended to final string
			}
			product += carry;				// make sure to include the final carry digit
			product = reverse(product);						// the string needs to be reversed
			product = product.replaceFirst("^0+(?!$)", "");	// remove any leading zeros
			for(int j = 0; j < i; j++)
				product += 0;								// raise the subproduct to correct power
			
			array[i] = new HugeInt(product);				// add to subproduct array
			}
		}
		//	Finish the product by adding all the subproducts.
		HugeInt result = array[0];
		for(int i = 1; i < lengthB; i++) {
			result = hugeAdd(result, array[i]);
		}
		return result;
	}

	/*
		Helper function that implements Karatsuba's multiplication.
		Divides the multiplication into multiple multiplications of smaller numbers.
		e.g., let A =	1112222
			  let B =	333444

				lengthA = 7
				lengthB = 6
				lengthDif = 7 - 6 = 1
				middle = 4

				A0 = 111
				A1 = 2222
				B0 = 033
				B1 = 3444
				
				Z2 = A0 * B0 = 111 * 033 = 3663
				Z1 = (A0 * B1) + (A1 * B0) = (111*3444)+(2222*033) = 455610
				Z0 = 2222*3444 = 7652568
		
				product = Z2*(base^(2*middle)) + Z1*(base^middle) + Z0
						= 3663*(10^8) + 455610(10^4) + 7652568
						= 366300000000 + 4556100000 + 7652568
						= 370863752568
								

		This implementation finds the product in O(n^{lg3}) time.
	*/
	private static HugeInt karatsuba(HugeInt a, HugeInt b) {
		int lengthA = a.getLength();
		int lengthB = b.getLength();
		int lengthDif = lengthA - lengthB;
		String strA = a.getStr();
		String strB = "";
		// pad the front of b with the-difference-in-length amount of zeroes
		for(int i = 0; i < lengthDif; i++) {
			strB += 0;
		}
		strB += b.getStr();
		int middle = lengthA / 2;
		// modify middle if the length is odd
		if(lengthA % 2 == 1) {
			middle += 1;
		}
		// splitting of the strings into appropriate substrings
		String sA0 = strA.substring(0, lengthA - middle);
		String sA1 = strA.substring(lengthA - middle, lengthA);
		String sB0 = strB.substring(0, lengthA - middle);
		String sB1 = strB.substring(lengthA - middle, lengthA);

		// creating the huge int subparts
		HugeInt a0 = new HugeInt(sA0);
		HugeInt a1 = new HugeInt(sA1);
		HugeInt b0 = new HugeInt(sB0);
		HugeInt b1 = new HugeInt(sB1);

		// creation of subparts
		HugeInt z2 = hugeMul(a0,b0);
		HugeInt z1 = hugeAdd(hugeMul(a0,b1),hugeMul(a1,b0));
		HugeInt z0 = hugeMul(a1,b1);
		z2 = hugeShiftL(z2, middle*2);			// the base^2 portion
		z1 = hugeShiftL(z1, middle);			// the base portion

		// final summation of subparts
		HugeInt result = z0;
		result = hugeAdd(result, z1);
		result = hugeAdd(result, z2);
		return result;
	}

	/*
		Wrapper function.
		Takes two huge ints a and b: a is the dividend, b is the divisor
		Sends valid parameters to the helper function.
		Otherwise, prints error to console and exits program.
	*/
	public static HugeInt hugeDiv(HugeInt a, HugeInt b) {
		// divide by 0 error case
		if((b.getStr()).equals("0")) {
			System.out.println("DON'T TRY TO DIVIDE BY 0. IT AIN'T HAPPENING!");
			System.exit(-1);
		}
		int comparison = hugeCmp(a,b);
		// if b is larger than a, then the quotient is 0
		if(comparison == -1) {
			return new HugeInt("0");
		}
		// if a and b are equal, then the quotient is 1
		else if(comparison == 0) {
			return new HugeInt("1");
		}
		else {
			return _hugeDiv(a,b);
		}
	}

	/*
		Helper function.
		Not sure what the name of this algorithm is. I figured it out by staring at a 
		whiteboard for some time.
		An example of how it works:
			Let A = 1249
			Let B =   36
			Remainder = A = 1249
	
			Length of A is 4
			Length of B is 2
			Maximum shift is 4 - 2 = 2
			
			Begin by shifting B 2 places
			B << 2 = 3600
			See if it goes into the Remainder
				1249
			-	3600
				____
				(Value is negative, so move to the next shift value)

			Shift B 1 place
			B << 1 = 360
			See if it goes into the Remainder
				1249
				-360
				____
				 889 -> 529 -> 169	(first digit of quotient is 3)

			Shift B 0 places
			B << 0 = 36
			See if it goes into the Remainder
				169
				-36
				___
				133 -> 97 -> 61 -> 25 (second digit of quotient is 4)
	*/
	private static HugeInt _hugeDiv(HugeInt a, HugeInt b) {
		int lengthA = a.getLength();
		int lengthB = b.getLength();
		int shift = lengthA - lengthB;				// maximum significant digit value for quotient
		String quotient = "";						// String representation of the quotient
		HugeInt remainder = a;						// set current remainder to a
		HugeInt innerDivisor;						// the inner subtractor
		int count;									// keeps track of the current digit
		//	For loop that builds the quotient String. Bounded by the maximum shift length.
		for(int i = shift; i >= 0; i--) {
			count = 0;								// initialize current digit to 0
			innerDivisor = hugeShiftL(b, i);		// shift inner subtractor to correct significance
			//	As long as the divisor is less than the dividend, increment the current digit
			while(	(hugeCmp(remainder, innerDivisor) == 1) ||
					(hugeCmp(remainder, innerDivisor) == 0)	) {
				count++;
				remainder = hugeSub(remainder, innerDivisor);
			}
			quotient += count;						// append the next quotient digit
		}
		return new HugeInt(quotient);
	}

	/*
		Wrapper function.
		Basically is a modified hugeDiv operation. Will end up returning the remainder
		instead of the quotient. Therefore, there will be minimal commenting for this 
		operation.
	*/
	public static HugeInt hugeMod(HugeInt a, HugeInt b) {
		// divide by 0 error case
		if((b.getStr()).equals("0")) {
			System.out.println("DON'T TRY TO MOD BY 0. IT AIN'T HAPPENING!");
			System.exit(-1);
		}
		int comparison = hugeCmp(a,b);
		if(comparison == -1) {
			return a;
		}
		else if(comparison == 0) {
			return new HugeInt("0");
		}
		else {
			return _hugeMod(a,b);
		}
	}

	/*
		Helper function for the mod operation. Essentially a mirror of _hugeDiv that
		returns the remainder instead of quotient.
	*/
	private static HugeInt _hugeMod(HugeInt a, HugeInt b) {
		int lengthA = a.getLength();
		int lengthB = b.getLength();
		int shift = lengthA - lengthB;				// maximum significant digit value for quotient
		HugeInt remainder = a;						// set current remainder to a
		HugeInt innerDivisor;						// the inner subtractor
		int count;									// keeps track of the current digit
		for(int i = shift; i >= 0; i--) {
			count = 0;								// initialize current digit to 0
			innerDivisor = hugeShiftL(b, i);		// shift inner subtractor to correct significance
			//	As long as the divisor is less than the dividend, increment the current digit
			while(	(hugeCmp(remainder, innerDivisor) == 1) ||
					(hugeCmp(remainder, innerDivisor) == 0)	) {
				count++;
				remainder = hugeSub(remainder, innerDivisor);
			}
		}
		return remainder;
	}

	/*
		This performs the modular exponentiation that's used for encrypting and decrypting
		values. Given three parameters a, b, and c this method computes:
			result = a^b mod c

		Algorithm was adapted from https://www.khanacademy.org/computing/computer-science/cryptography/modarithmetic/a/fast-modular-exponentiation
	*/
	public static HugeInt hugeModExp(HugeInt a, HugeInt b, HugeInt c) {
		String bBinStr = "";					// represents the huge int b in binary

		// variables used for method calculations
		HugeInt tmp = b;						// intermediate calculation variable
		HugeInt hugeZero = new HugeInt("0");	// huge int representing the value 0
		HugeInt hugeTwo = new HugeInt("2");		// huge int representing the value 2

		// creation of the binary string
		while(hugeCmp(tmp, hugeZero) != 0) {
			if(tmp.intAt(0) % 2 == 1) {
				bBinStr += 1;					// a power of 2 was found
			}
			else {
				bBinStr += 0;					// no power of 2 here, move on
			}
			tmp = hugeDiv(tmp, hugeTwo);		// moving to the next power of 2
		}

		int length = bBinStr.length();			// length of the binary string
		HugeInt[] powers = new HugeInt[length];	// creating an array for interal calculations
		powers[0] = hugeMod(a, c);				// submods for each power of 2

		// creation of submods, e.g., submods of a^1, a^2, a^4, ..., a^b
		for(int i = 1; i < length; i++) {
			powers[i] = hugeMod(hugeMul(powers[i-1], powers[i-1]), c);
		}

		HugeInt acc = new HugeInt("1");				// accumulator representing final mod value
		// combine all the submods together, this is the exponentiation by squaring step
		for(int i = length - 1; i >= 0; i--) {
			if((bBinStr.charAt(i)) == '1') {
				acc = hugeMul(acc, powers[i]);		// combine submod if one exists
			}
		}
		acc = hugeMod(acc, c);						// perform mod operation on final submod
		return acc;
	}

	/*
		An arithmetic left shift operation that essentially appends
		a shift-number of zeros and returns the resulting huge int.
	*/
	public static HugeInt hugeShiftL(HugeInt a, int shift) {
		String s = a.getStr();				// get the string to be shifted
		for(int i = 0; i < shift; i++) {
			s += 0;							// appends a single 0 on each iteration
		}
		s = s.replaceFirst("^0+(?!$)", "");	// handles the case of left-shifting 0
		HugeInt result = new HugeInt(s);	
		return result;
	}

	/*
		An arithmetic right shift operation that essentially chops off the
		trailing shift-number of digits.
	*/
	public static HugeInt hugeShiftR(HugeInt a, int shift) {
		int length = a.getLength();
		// if the required shift results in shifting every digit away
		if(shift >= length) {
			return new HugeInt("0");				// returns a huge int of 0
		}
		String originalStr = a.getStr();

		// grabs the substring that is kept after shift
		String s = originalStr.substring(0, length-shift);
		HugeInt result = new HugeInt(s);
		return result;
	}

	/*
		Utility function that returns a given String reversed.
		e.g.,	Given a String	"abcd"
				will return		"dcba"
	*/			
	private static String reverse(String s) {
		String revStr = "";				// initializes empty String to store reversing result
		int length = s.length();		// reverse the entire length of the String

		//	Start from the end of the original String and append it to the front of the new String.
		for(int i = 0; i < length; i++) {
			revStr += s.charAt(length-i-1);
		}
		return revStr;					// return the reversed String
	}

	//	Prints out the huge int to the console.
	public static void hugePrint(HugeInt h) {
		System.out.println(h.getStr());
	}
}
