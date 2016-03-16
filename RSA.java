/*
*	Tony Wong; Brent Yurek
*	CS 342 - Project 3
*	RSA Encryption
*	
*	This is the driver class for the RSA program. Call this to display a GUI
*	that presents the user with 5 options:
*		1) Make keys - currently set to pull 9-digit-length prime numbers and creates
*				the public and private key pairs.
*		2) Block a file - takes a message and blocks either 2, 4, or 8 characters per line.
*		3) Unblock a file - takes a blocked file with an arbitrary block size and recreates
*				the message.
*		4) Encrypt a file - takes a blocked message file and encrypts each line.
*				Assume X = line in blocked message, then this performs
*				Resulting line = X^e mod n, where e is the public key
*		5) Decrypt a file - takes an encrypted message file and decrypts each line.
*				Assume Y = line in encrypted message, then this performs
*				Resulting line = Y^d mod n, where d is the public key
*/

import java.io.*;					// exception handling
import java.util.*;					// file opening
import javax.swing.*;				// GUI elements
import java.awt.*;					// handlers
import java.awt.event.*;			// ActionListener

/*
	The driver and GUI class of the RSA program.
*/
public class RSA extends JFrame {
	private static JPanel pane;								// the visual content pane for the GUI
	private static JButton keyButton;						// the button used for creating keys
	private static JButton blockButton;						// the button for blocking a message
	private static JButton unblockButton;					// the button used for unblocking a file
	private static JButton encryptButton;					// click to encrypt
	private static JButton decryptButton;					// click to decrypt

	private static int primeLength = 9;						// hard-coded value for length of prime numbers

	// action listeners for each button object
	private static kbListener kb = new kbListener();
	private static bbListener bb = new bbListener();
	private static ubListener ub = new ubListener();
	private static ebListener eb = new ebListener();
	private static dbListener db = new dbListener();
	private static Scanner scanner;

	// the driver method
	public static void main(String[] args) {
		RSA app = new RSA();

		// create a pane and have each element within be centered in the middle
		pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		// creation of each button
		keyButton = new JButton("Make keys");
		blockButton = new JButton("Block a file");
		unblockButton = new JButton("Unblock a file");
		encryptButton = new JButton("Encrypt a file");
		decryptButton = new JButton("Decrypt a file");

		// adding the appropriate action listeners
		keyButton.addActionListener(kb);
		blockButton.addActionListener(bb);
		unblockButton.addActionListener(ub);
		encryptButton.addActionListener(eb);
		decryptButton.addActionListener(db);

		// set each button to uniform size
		setSize(keyButton);
		setSize(blockButton);
		setSize(unblockButton);
		setSize(encryptButton);
		setSize(decryptButton);

		// add each button to the window pane
		pane.add(keyButton);
		pane.add(blockButton);
		pane.add(unblockButton);
		pane.add(encryptButton);
		pane.add(decryptButton);

		// add pane to window
		app.getContentPane().add(pane);

		// display JFrame and set default interactions
		app.pack();
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		app.setSize(400,350);
		app.setVisible(true);
	}	

	// helper function to set buttons to uniform size
	private static void setSize(JButton jbutton) {
		jbutton.setMinimumSize(new Dimension(400,70));
		jbutton.setMaximumSize(new Dimension(400,70));
	}

	// what happens when the user wants to create keys
	static class kbListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			KeyGen keygen = new KeyGen(primeLength);			// CREATION OF KEYS

			JOptionPane.showMessageDialog(null, "New keys generated.\n" +
				"Private key-pair located in file \"prikey\"\n" +
				"Public key-pair located in file \"pubkey\"",
				"Key Creation",
				JOptionPane.PLAIN_MESSAGE);
		}
	}

	// what happens when the user wants to block a file
	static class bbListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			// method variables
			boolean continueOn = true;
			String inputFile;
			String outputFile = "";
			int blockSize = -1;
			
			// grab the file to be blocked
			do {
				inputFile =  (String)JOptionPane.showInputDialog(
								null,
								"What file do you want to block?",
								"Blocking a file",
								JOptionPane.PLAIN_MESSAGE);
				try {
					scanner = new Scanner(new File(inputFile));
					break;
				}
				catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
						"Cannot find file to block.",
						"Does file exist?",
						JOptionPane.PLAIN_MESSAGE);
				}
				
				// if the user clicked cancel
				catch (Exception e) {
					continueOn = false;
					break;
				}
			}
			while(true);
			
			// where to block the file to?
			if(continueOn == true) {
			    boolean goodFile = false;
			    do {
				    outputFile =  (String)JOptionPane.showInputDialog(
								    null,
								    "Where do you want to place the blocked message?",
								    "Blocking a file",
								    JOptionPane.PLAIN_MESSAGE);
				    try {
					    if(outputFile.equals("") == false) {
						    goodFile = true;
					    }
					    else {
						    JOptionPane.showMessageDialog(null,
							    "Please input a valid file name",
							    "Blank file name",
							    JOptionPane.PLAIN_MESSAGE);
					    }
				    }

					// if the user clicked cancel
				    catch (Exception e) {
					    continueOn = false;
					    break;
				    }
			    }
			    while(goodFile == false);
			}
			
			// select blocking size
			if(continueOn == true) {
			    Object[] options = {"2","4","8"};
			    blockSize = JOptionPane.showOptionDialog(null,
			        "Select a blocking size",
				    "Blocking a file",
			        JOptionPane.YES_NO_CANCEL_OPTION,
			        JOptionPane.QUESTION_MESSAGE,
			        null,
			        options,
			        options[2]);

				// hard-coded values for blocking size
			    switch(blockSize) {
				    case 0:
					    blockSize = 2;
					    break;
				    case 1:
					    blockSize = 4;
					    break;
				    case 2:
					    blockSize = 8;
					    break;
			    }
	
				// NOW BLOCK THE FILE
			    if(blockSize != -1) {
				    Blocking blocking = new Blocking();
				    blocking.block(blockSize, inputFile, outputFile);
                    JOptionPane.showMessageDialog(null, "BLOCKING COMPLETE!");
			    }
			}	
		}
	}

	// what happens when the user wants to unblock a file
	static class ubListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			// method variables
            boolean continueOn = true;
			String inputFile;
			String outputFile = "";

			// grab the file to be unblocked
			do {
				inputFile =  (String)JOptionPane.showInputDialog(
								null,
								"What file do you want to unblock?",
								"Unblocking a file",
								JOptionPane.PLAIN_MESSAGE);
				try {
					scanner = new Scanner(new File(inputFile));
					break;
				}
				catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
						"Cannot find file to unblock.",
						"Does file exist?",
						JOptionPane.PLAIN_MESSAGE);
				}
				
				// if the user clicked cancel
				catch (Exception e) {
					continueOn = false;
					break;
				}
			}
			while(true);
			
			// where do you save the unblocked file to?
			if(continueOn == true) {
			    boolean goodFile = false;
			    do {
				    outputFile =  (String)JOptionPane.showInputDialog(
								    null,
								    "Where do you want to place the unblocked message?",
								    "Unblocking a file",
								    JOptionPane.PLAIN_MESSAGE);
				    try {
					    if(outputFile.equals("") == false) {
						    goodFile = true;
					    }
					    else {
						    JOptionPane.showMessageDialog(null,
							    "Please input a valid file name",
							    "Blank file name",
							    JOptionPane.PLAIN_MESSAGE);
					    }
				    }

					// if the user clicked cancel
				    catch (Exception e) {
					    continueOn = false;
					    break;
				    }
			    }
			    while(goodFile == false);
			}

			// NOW UNBLOCK IT		
            if(continueOn == true) {
                Blocking blocking = new Blocking();
				blocking.unblock(inputFile, outputFile);
                JOptionPane.showMessageDialog(null, "UNBLOCKING COMPLETE!");
            }
		}
	}

	// what happens if the user wants to encrypt
	static class ebListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			// method variables
            boolean continueOn = true;
			String inputFile;
			String outputFile = "";
            String keyFile = "";

			// what file do you want to encrypt?
			do {
				inputFile =  (String)JOptionPane.showInputDialog(
								null,
								"What file do you want to encrypt?",
								"Encrypting a file",
								JOptionPane.PLAIN_MESSAGE);
				try {
					scanner = new Scanner(new File(inputFile));
					break;
				}
				catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
						"Cannot find file to encrypt.",
						"Does file exist?",
						JOptionPane.PLAIN_MESSAGE);
				}
				
				// if the user clicked cancel
				catch (Exception e) {
					continueOn = false;
					break;
				}
			}
			while(true);
			
			// where do you want to save the encrypted file?
			if(continueOn == true) {
			    boolean goodFile = false;
			    do {
				    outputFile =  (String)JOptionPane.showInputDialog(
								    null,
								    "Where do you want to place the encrypted message?",
								    "Encrypting a file",
								    JOptionPane.PLAIN_MESSAGE);
				    try {
					    if(outputFile.equals("") == false) {
						    goodFile = true;
					    }
					    else {
						    JOptionPane.showMessageDialog(null,
							    "Please input a valid file name",
							    "Blank file name",
							    JOptionPane.PLAIN_MESSAGE);
					    }
				    }

					// if the user clicked cancel
				    catch (Exception e) {
					    continueOn = false;
					    break;
				    }
			    }
			    while(goodFile == false);
			}
			
			if(continueOn == true) {
			// where's the key file to use for encryption
            do {
				keyFile =  (String)JOptionPane.showInputDialog(
								null,
								"What public key file do you want to use?",
								"Encrypting a file",
								JOptionPane.PLAIN_MESSAGE);
				try {
					scanner = new Scanner(new File(keyFile));
					break;
				}
				catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
						"Cannot find file key file.",
						"Does file exist?",
						JOptionPane.PLAIN_MESSAGE);
				}
				catch (Exception e) {
					continueOn = false;
					break;
				}
			}
            while(true);
			}
	
			// NOW ENCRYPT
            if(continueOn == true) {
                Encrypt enc = new Encrypt(inputFile, outputFile, keyFile, 1);
                JOptionPane.showMessageDialog(null, "ENCRYPTION COMPLETE!");
            }
		}
	}

	// what happens when the user wants to decrypt a file
	static class dbListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			// method variables
            boolean continueOn = true;
			String inputFile;
			String outputFile = "";
            String keyFile = "";

			// what file do you want to decrypt?
			do {
				inputFile =  (String)JOptionPane.showInputDialog(
								null,
								"What file do you want to decrypt?",
								"Decrypting a file",
								JOptionPane.PLAIN_MESSAGE);
				try {
					scanner = new Scanner(new File(inputFile));
					break;
				}
				catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
						"Cannot find file to encrypt.",
						"Does file exist?",
						JOptionPane.PLAIN_MESSAGE);
				}
				
				// if the user clicked cancel
				catch (Exception e) {
					continueOn = false;
					break;
				}
			}
			while(true);
			
			// where to place the decrypted file?			
			if(continueOn == true) {
			    boolean goodFile = false;
			    do {
				    outputFile =  (String)JOptionPane.showInputDialog(
								    null,
								    "Where do you want to place the decrypted message?",
								    "Decrypting a file",
								    JOptionPane.PLAIN_MESSAGE);
				    try {
					    if(outputFile.equals("") == false) {
						    goodFile = true;
					    }
					    else {
						    JOptionPane.showMessageDialog(null,
							    "Please input a valid file name",
							    "Blank file name",
							    JOptionPane.PLAIN_MESSAGE);
					    }
				    }

					// if the user clicked cancel
				    catch (Exception e) {
					    continueOn = false;
					    break;
				    }
			    }
			    while(goodFile == false);
			}
			
			if(continueOn == true) {
			// where is the private key file?
            do {
				keyFile =  (String)JOptionPane.showInputDialog(
								null,
								"What private key file do you want to use?",
								"Decrypting a file",
								JOptionPane.PLAIN_MESSAGE);
				try {
					scanner = new Scanner(new File(keyFile));
					break;
				}
				catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
						"Cannot find file key file.",
						"Does file exist?",
						JOptionPane.PLAIN_MESSAGE);
				}
				
				// if the user clicked cancel
				catch (Exception e) {
					continueOn = false;
					break;
				}
			}
            while(true);
			}

			// NOW DECRYPT
            if(continueOn == true) {
                Encrypt enc = new Encrypt(inputFile, outputFile, keyFile, 0);
                JOptionPane.showMessageDialog(null, "DECRYPTION COMPLETE!");
            }
		}
	}
}
