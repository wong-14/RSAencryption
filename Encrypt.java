/*
*   Tony Wong; Brent Yurek
*   CS 342 - Project 3
*   RSA Encryption
*
*   This is the message encryption and decryption class. It performs the same operation, namely,
*       To encrypt a value M into C ->  C = M^e mod n
*       To decrypt a value C into M ->  M = C^d mod n
*
*   The only difference stems from the source file and whether a public or private key was used.
*
*   Note: class extends HugeOps because certain mathematical operations on HugeInts are needed
*/

import java.util.Scanner;				// used for reading in files
import java.io.*;						// used for exception handling and file writing

public class Encrypt extends HugeOps {
    private static String ENCfile = "encrypted.txt";        // filename for the encrypted file
    private static String DECfile = "decrypted.txt";        // filename for the decrypted file
    private static int blockSize;							// to keep track of the blocking size
    private static String PUBfile = "pubkey";				// name of the public key file
    private static String PRIfile = "prikey";				// name of the private key file
    private static Scanner scanner;							// used for file opening
    private static Writer writer;							// used for file writing
    private static HugeInt n = null;						// the n value of the RSA algorithm
    private static HugeInt exp = null;						// the RSA d or e value
   
	/*
		The main method for this class. It selects either a decryption or encryption process
		based on the "operation" parameter. If given the String "e", the method will
		select the encode method in encryption mode. If given the String "d", the method will
		instead select the encode method in decryption mode.
	*/
	public Encrypt(String inputFile, String outputFile, String keyFile, int select) {
		if(select == 1) {		// user wants to encrypt file
			ENCfile = outputFile;
			PUBfile = keyFile;
			encode(inputFile, ENCfile, 1);
		}
		else if(select == 0) {	// user wants to decrypt file
			DECfile = outputFile;
			PRIfile = keyFile;
			encode(inputFile, DECfile, 0);
		}
		else {
			System.out.println("Select either encryption or decryption with fourth parameter.\n" +
				"If you want to encrypt, put 1. Put 0 for decryption.");
		}
	}
 
	/*
		This is the encryption/decryption method. The distinguishing traits:
			1) which key to use
				a) public key for encryption
				b) private key for decryption
			2) what file to open
				a) rsablocking file for encryption
				b) encrypted file for decryption
			3) what file to write to
				a) encrypted.txt for encryption
				b) decrypted.txt for decryption
	*/
    private static void encode(String filename, String destination, int usePublic) {
		// help variables
		HugeInt h;
        String keyFile = PUBfile;
		String tmp;

		// determines which key pair to use
        if(usePublic == 0) {		// if decryption was selected, use the private key
            keyFile = PRIfile;
        }
        
		// open the proper RSA key file
        try {
            scanner = new Scanner(new File(keyFile));
        }
        catch (FileNotFoundException e) {
            System.out.println("Key file not found. Exiting program.");
            System.exit(-1);
        }

		// parses the XML formatted file for the keys	
        while(scanner.hasNext()) {
            tmp = scanner.next();
            if(tmp.startsWith("<evalue>")) {
                tmp = tmp.replace("</evalue>", "");
                tmp = tmp.replace("<evalue>", "");
                exp = new HugeInt(tmp);
            }
            if(tmp.startsWith("<nvalue>")) {
                tmp = tmp.replace("</nvalue>", "");
                tmp = tmp.replace("<nvalue>", "");
                n = new HugeInt(tmp);
            }
            if(tmp.startsWith("<dvalue>")) {
                tmp = tmp.replace("</dvalue>", "");
                tmp = tmp.replace("<dvalue>", "");
                exp = new HugeInt(tmp);
            }
        }

		// check to see if keys were found, exit program if no keys present
		if(n == null || exp == null) {
			System.out.println("NO KEYS WERE FOUND. Exiting program.");
			System.exit(-1);
		}
        
		// opening the file to be encrypted or decrypted
        try {
            scanner = new Scanner(new File(filename));
        }
        catch (FileNotFoundException e) {
            System.out.println("Source file not found. Exiting program.");
            System.exit(-1);
        }
        
		// write the result of the encryption/decryption process to appropriate file
        try {
            writer = new BufferedWriter(
                        new OutputStreamWriter(
                            new FileOutputStream(destination),
                            "utf-8"));
			// while there are values to be encrypted or decrypted
            do {
				tmp = scanner.next();
				h = hugeModExp(new HugeInt(tmp), exp, n);		// h = tmp^exp mod n
				String result = h.getStr();						// the resulting String
				/*
					If the String is an odd length, which means there is a character
					with a value within the range of 05-09 and the leading zero is cut.
					Thus, a padding of one zero is needed in order to maintain proper
					blocking length.
				*/
				if(result.length() % 2 == 1) {
					result = "0" + h.getStr();
				}
                writer.write(result + "\n");	// write encrypted/decrypted value to file
            }
            while(scanner.hasNext());
            
        }
        catch (IOException e) {}
        finally {
            try {
                writer.close();
            }
            catch (Exception e) {}
        }

		try {
			scanner.close();
		}
		catch (Exception e) {}
    }
}

