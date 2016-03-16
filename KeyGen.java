/*
*	Tony Wong; Brent Yurek
*	CS 342 - Project 3
*	RSA Encryption
*
*	This class handles key creation and storage. It extends the abstract HugeOps class.
*	The default constructor takes one integer, which signifies the bit length of the
*	prime numbers. For example, calling KeyGen with a value of 512 will create a key-pair
*	using the RSA algorithm on 512-bit primes.
*/

import java.util.Random;		// used for selecting a random prime number
import java.util.Scanner;		// used for file opening
import java.io.*;				// used for exception handling

public class KeyGen extends HugeOps {
	private static String filename;		// the prime numbers filename
	private static Scanner scanner;		// used to parse through primes file
	private static int nPrimes;			// the number of primes in selected file, designated by line 1
	private static int firstPrime;		// index for the first randomly selected prime number
	private static int secondPrime;		// index for the second randomly selected prime number
	private static Random rnd;			// used for random number generation
	private static String[] primesArr;	// prime number storage

	// variables for RSA algorithm
	private static HugeInt p;			// prime number p
	private static HugeInt q;			// prime number q
	private static HugeInt n;			// the result of p * q
	private static HugeInt phi;			// the result of (p-1) * (q-1)
	private static HugeInt d;			// private key value d
	private static int k;				// value used for intermediate calculations to arrive at d/e
	// e starts at 3 and is incremented by 2 until it is co-prime with phi
	private static HugeInt e = new HugeInt("3");
	private static HugeInt hugeOne = new HugeInt("1");		// common value that's used in RSA

	// file writing variables
	private static Writer writer;							// used for file writing
	private static String priKeyFileName = "prikey";		// xml format file to store private key
	private static String pubKeyFileName = "pubkey";		// xml format file to store public key

	/*
		Constructor that is given the minimum bit length of primes wanted.
		For example, if the bitLength is 128, then the file primeNumbers128.rsc is opened.
		The first line contains the number of primes in the file. Every line thereafter is an 
		individual prime of the selected bit length.
	*/
	public KeyGen(int bitLength) {
		// creating the filename String to be opened
		filename = "primeNumbers" + bitLength + ".rsc";
		try {
			scanner = new Scanner(new File(filename));		
		}
		// exit the program if the file cannot be found
		catch (FileNotFoundException e) {
			System.out.println("File not found. Exiting program.");
			System.exit(-1);
		}
	
		// gets how many prime numbers the file contains	
		nPrimes = scanner.nextInt();
			
		// each file has to contain at least 2 primes as p and q are required to be different
		if(nPrimes < 2) {
			System.out.println("Add more prime numbers to " + filename);
			System.exit(-1);
		}

		// stores each prime number in a String array, will exit if there's a missing prime
		primesArr = new String[nPrimes];
		for(int i = 0; i < nPrimes; i++) {
			if(scanner.hasNext()) {
				primesArr[i] = scanner.next();
			}
			else {
				System.out.println("Incorrect count of prime numbers. Exiting program.");
				System.exit(-1);
			}
		}

		// randomly picks two prime numbers within the file
		rnd = new Random();
		firstPrime = rnd.nextInt(nPrimes);
		secondPrime = firstPrime;
		// keep generating a different index if secondPrime is the same as firstPrime
		while(secondPrime == firstPrime) {
			secondPrime = rnd.nextInt(nPrimes);
		}
		scanner.close();		// scanner has done its job, feed it to the garbage collector

		// selection of p and q to begin the key generation
		p = new HugeInt(primesArr[firstPrime]);
		q = new HugeInt(primesArr[secondPrime]);

		// calculation of n and phi values	
		n = hugeMul(p,q);											// p * q
		phi = hugeMul(hugeSub(p, hugeOne), hugeSub(q, hugeOne));	// (p-1) * (q-1)
	
		// checking if e and phi are co-prime
		while((GCD(phi,e)).getStr().equals("1") == false) {
			e = hugeAdd(e, new HugeInt("2"));
		}
	
		// calculate d value through the Extended Euclidean Algorithm	
		d = EEA();
		// checks to see if d was calculated correctly; otherwise, exit the program	
		if((hugeMod(hugeMul(d,e), phi).getStr()).equals("1") == false) {
			System.out.println("D value was not calculated correctly. This should not print.");
			System.exit(-1);
		}

		/*
			Storing the public key to file "pubkey" in XML format.
			Note that writer truncates the file.
		*/
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
											new FileOutputStream(pubKeyFileName), "utf-8"));
			writer.write("<rsakey>\n");
			writer.write("\t<evalue>" + e.getStr() + "</evalue>\n");
			writer.write("\t<nvalue>" + n.getStr() + "</nvalue>\n");
			writer.write("</rsakey>\n");
		}
		catch (IOException e) {}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {}
		}
		
		/*
			Storing the private key to file "prikey" in XML format.
			Note that writer truncates the file.
		*/
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
											new FileOutputStream(priKeyFileName), "utf-8"));
			writer.write("<rsakey>\n");
			writer.write("\t<dvalue>" + d.getStr() + "</dvalue>\n");
			writer.write("\t<nvalue>" + n.getStr() + "</nvalue>\n");
			writer.write("</rsakey>\n");
		}
		catch (IOException e) {}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {}
		}
	}

	/*
		Wrapper function for the Greatest Common Denominator.
		Used to check co-prime status.
	*/
	private static HugeInt GCD(HugeInt a, HugeInt b) {
		// returns HugeInt value 1 if a and b are co-prime
		if((a.getStr()).equals("0")) {
			return b;
		}
		if((b.getStr()).equals("0")) {
			return a;
		}

		// keep recursing until GCD is found
		if(hugeCmp(a, b) == 1) {
			return _GCD(a,b);
		}
		else {
			return _GCD(b,a);
		}
	}

	/*
		Helper function for GCD.
	*/
	private static HugeInt _GCD(HugeInt a, HugeInt b) {
		a = hugeMod(a, b);
		return GCD(b, a);
	}

	/*
		Wrapper function for the Extended Euclidean Algorithm.
		Automatically grabs phi, e, and starter integer to throw into helper function.
		Formula re-engineered through watching the following youtube video:
			https://www.youtube.com/watch?v=Z8M2BTscoD4
	*/
	private static HugeInt EEA() {
		return _EEA(phi, e, phi, hugeOne);
	}

	/*
		Helper function for the Extended Euclidean Algorithm
		I do not understand how this works mathematically, but it is verified to work.
	*/
	private static HugeInt _EEA(HugeInt a, HugeInt b, HugeInt c, HugeInt d) {
		// d value is found!
		if((b.getStr()).equals("1")) {
			return d;
		}
		// intermediate multiple value
		HugeInt x = hugeDiv(a,b);

		// variables used for next recursive call
		HugeInt a2 = b;
		HugeInt b2 = hugeMod(a,b);
		HugeInt c2 = d;
		HugeInt d2 = hugeMul(d,x);

		// accounting for possible negative differences		
		if(hugeCmp(c, d2) == -1) {
			d2 = hugeSub(d2, c);
			// phi is larger than d2, normal subtraction ensues
			if(hugeCmp(phi, d2) == 1) {
				d2 = hugeSub(phi, d2);
			}
			// negative difference case of d2 being larger than phi
			else {
				d2 = hugeMod(d2, phi);
				d2 = hugeSub(phi, d2);
			}
		}
		// normal subtraction if no negative differences are detected
		else {
			d2 = hugeSub(c, d2);
		}
		// keep recursing until d is found
		return _EEA(a2, b2, c2, d2);
	}
}

