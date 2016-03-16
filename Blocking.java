/*
*	Tony Wong; Brent Yurek
*	CS 342 - Project 3
*	RSA Encryption
*
*	This is the message blocking class. It performs two primary functions:
*		1) Takes a message and breaks it into pre-specified chunks
*			e.g.,	Given a message: "Hello World!" and a chunk size of 4 results in
*
*					Hello World! = 45 74 81 81 84 05 60 84 87 81 73 06 (transform)
*											
*					067381878460058481817445
*
*					81817445
*					84600584
*					06738187
*
*		2) Takes a blocked message and unblocks it
*			e.g.,	Given a blocked message:	81817445
*												84600584
*												06738187
*					
*					Unroll the message: 067381878460058481817445
*					Decipher:			!dlroW olleH
*					Reverse:			Hello World!
*/

import java.util.Scanner;			// used for file opening
import java.io.*;					// exception handling and writing to files

public class Blocking {
	private static String RSAFile = "rsamessage.txt";		// filename for the message to be blocked
	private static int chunk;								// how long should each blocked line be
	private static Scanner scanner;							// used for opening files (blocking)
	private static Writer writer;							// used for writing files (unblocking)
	private static String BlockedFile;						// filename for the result of blocking
	private static String message;							// the entire message file in one String
	private static int offset = 27;		// the difference between ASCII and our modified ASCII table
	private static String[] myASCII;	// storage of the entire message in our modified ASCII
	private static int length;			// char length of message
	private static String eachLine;     // utility String for blocking procedure
    private static String UnblockedFile;// file that will be written to

	/*
		This is the blocking operation that takes a message and breaks it into manageable chunks
		for encryption. Each character will be coded into a shifted ASCII table of 27-offset.
	*/
	public static void block(int chunkSize, String input, String destination) {
		RSAFile = input;		// allow the user to select which file to block
		chunk = chunkSize;
		// attempting to open the message file, program will exit if no file is found
		try {
			scanner = new Scanner(new File(RSAFile));
		}
		catch (FileNotFoundException e) {
			System.out.println("No message file found. Program will now exit.");
			System.exit(-1);
		}
		message = scanner.useDelimiter("\\Z").next();		// store the file in one String
		scanner.close();									// feed it to the garbage collector
		
		length = message.length();							// length of the message

		// just a quick operation to find the appropriate sized length
		int tmpQuotient = length / chunk;					
		int tmpMod = length % chunk;
		if(tmpMod != 0) {
			tmpQuotient++;
		}
		int maxLength = chunk * tmpQuotient;				// length of the message + padding

		myASCII = new String[maxLength];					// creation of the storage String
		for(int i = 0; i < length; i++) {
			int n = message.charAt(i);						
			myASCII[i] = "";								// initialize an empty String

			// if the character can be represented between 10 and 99 in modified table
			if(n > 36) {
				myASCII[i] += (n - offset);
			}

			// if the character is between 5-9, then it needs a zero prefix
			else if(n > 31) {
				myASCII[i] += "0" + (n - offset);

			// SPECIAL CHARACTERS
			}
			else if(n == 0) {
				myASCII[i] += "00";		// NULL character
			}
			else if(n == 11) {
				myASCII[i] += "01";		// Vertical tab
			}
			else if(n == 9) {
				myASCII[i] += "02";		// Horizontal tab
			}
			else if(n == 10) {
				myASCII[i] += "03";		// New line
			}
			else if(n == 13) {
				myASCII[i] += "04";		// Carriage Return
			}
			// if the character falls outside of the modified table
			else {
				System.out.println("Character is not in our modified ASCII table");
				System.exit(-1);
			}
		}

		// padding for the blocked message
		for(int i = length; i < maxLength; i++) {
			myASCII[i] = "00";
		}

		
		// allow the user to select file destination
		BlockedFile = destination;

		// create the blocked file
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
											new FileOutputStream(BlockedFile), "utf-8"));

			// creating the blocked file line by line
			for(int i = 0; i < maxLength; i = i + chunk) {
				eachLine = "";								// a brand new line
				// reverse endian order
				for(int j = chunk - 1; j >= 0; j--) {
					eachLine += myASCII[i+j];
				}
				writer.write(eachLine + "\n");				// a complete line is created
			}
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
        This is the unblocking operation that takes a blocked message file and unblocks it into
        the (hopefully) original message. The blocked message has to be unencrypted; otherwise,
        the output will be garbled.
    */
    public static void unblock(String filename, String destination) {
        UnblockedFile = destination;            // file to write to

        // attempt to open requested blocked file
        try {
            scanner = new Scanner(new File(filename));
        }
        catch (FileNotFoundException e) {
            System.out.println("No message file found. Program will now exit.");
            System.exit(-1);
        }

        String tmpStr;              // utility String used for intermediate calculations
        message = "";               // holds the unblocked message
        
        // parse each line and append it with proper format
        while(scanner.hasNext()) {
            tmpStr = scanner.next();        // grab next line
            message += reverse(tmpStr);     // undo the reverse endian format
        }
        scanner.close();                    // feed it to the garbage collector

        message = convert(message);         // convert message from shifted table values to ASCII

        // create the unblocked file
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                                            new FileOutputStream(UnblockedFile), "utf-8"));
            writer.write(message);
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
        Utility function used to undo the reverse-endian line format.
        e.g.,   01234567 will be reversed to 67452301 NOT 76543210
    */
    private static String reverse(String s) {
        int length = s.length();
        String result = "";
        // each "element" consists of two chars, which is why they're being reversed in sets of 2
        for(int i = length - 2; i >= 0; i = i - 2) {
            result += s.charAt(i);
            result += s.charAt(i + 1);
        }
        return result;
    }

    /*
        Utility function that converts a String that is in our modified table values into a
        readable ASCII string.
    */
    private static String convert(String s) {
        int length = s.length();
        String result = "";                         // the String to be returned
        String currChar;                            // intermediate calculation variable
        int currInt;
        for(int i = 0; i < length - 1; i = i + 2) {
            currChar = "";
            currChar += "" + s.charAt(i) + s.charAt(i+1);

            // SPECIAL CHARACTERS
            if(currChar.equals("00")) {
                break;                              // found padding, break out of loop
            }
            else if(currChar.equals("01")) {
                result += (char)11;                 // Vertical tab
            }
            else if(currChar.equals("02")) {
                result += (char)9;                  // Horizontal tab
            }
            else if(currChar.equals("03")) {
                result += (char)10;                 // New line
            }
            else if(currChar.equals("04")) {
                result += (char)13;                 // Carriage Return
            }

            // NORMAL SHIFTED VALUES
            else {
                result += (char)(Integer.parseInt(currChar) + offset);
            }
        }
        return result;          // return readable String
    }
}

