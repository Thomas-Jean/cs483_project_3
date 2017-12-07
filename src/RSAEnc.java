package src;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.lang.Math;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Scanner;

/*
Thomas Jean
COSC 483
Project 2
RSAEnc.java will implement the encryption of an element in Z_n*
mod N raised to the e using RSA public key.
The modular arithmetic is done by the java BigInteger library.
*/



class RSAEnc{


	RSAEnc(){

		rand = new SecureRandom();
	}

	RSAEnc(String[] args){

		rand = new SecureRandom();

		optionParser(args);


		readKey();


		readInputFile();


		constructElement();


		encrypt();


		writeOutput();


	}

	void readKey(){

		try{
			BufferedReader rd = new BufferedReader( new FileReader(keyFileName));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
				if (nextLine.equals("")){ 
					break;
				}
	    		Scanner line_scanner = new Scanner(nextLine);

	    		if(i == 0){
	    			nBits = line_scanner.nextInt();
	    			

	    		}else if(i == 1){
	    			String x = line_scanner.next();
	    			N = new BigInteger(x);

	    		}else if(i == 2){
	    			String y = line_scanner.next();
	    			e = new BigInteger(y);

	    		}else{
	    			System.err.printf("Error: Key File is malformed.");
	    			System.exit(1);
	    		}
	    		i++;
				line_scanner.close();

			}



		}catch(IOException ioe){
			System.err.printf("IO Exception: %s%n",ioe.getMessage());
			System.exit(1);
		}

	}

	void readInputFile(){

		try{
			BufferedReader rd = new BufferedReader( new FileReader(inputFileName));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
	    		if (nextLine.equals("")){ 
	    			break;
	    		}
	    		Scanner line_scanner = new Scanner(nextLine);
	    		String x = line_scanner.next();
	    		input = new BigInteger(x);
	    		line_scanner.close();

	    	}
	    }catch(IOException ioe){

	    	System.err.printf("IO Exception: %s%n",ioe.getMessage());
			System.exit(1);
	    }

	}
	
	void constructElement(){
		int rBits = nBits/2;

		BigInteger randomString = new BigInteger(rBits,rand);
		byte[] randomBytes = randomString.toByteArray();

		byte[] rb = new byte[1];

		for(int i=0; i < randomBytes.length; i++){
			if(randomBytes[i] == 0){
				while(randomBytes[i] == 0){
					rand.nextBytes(rb);
					randomBytes[i] = rb[0];
				}
			}
		}

		byte[] messagebytes = input.toByteArray();

		// ( 0x00 || 0x02 || r || 0x00 || m )
		byte[] paddedMessageBytes = new byte[(N.bitLength()+7)/8];

		paddedMessageBytes[0] = 0;
		paddedMessageBytes[1] = 2;
		for(int i=2; i < randomBytes.length+2;i++){
			paddedMessageBytes[i] = randomBytes[i-2];

		}

		paddedMessageBytes[randomBytes.length + 2] = 0;

		// writes to the back of the messagesapce as
		// leading zeros are stripped by big int
		int	z = N.bitLength()/8 - messagebytes.length;

		for(int i = 0; i < messagebytes.length; i++){
			paddedMessageBytes[z+i] = messagebytes[i];
		}

		paddedElement = new BigInteger(paddedMessageBytes);

	}

	void encrypt(){

		encryptedElement = paddedElement.modPow(e,N);

	}

	void writeOutput(){

		try{
			PrintWriter fw = new PrintWriter( new BufferedWriter(new FileWriter(outputFileName)));

			fw.format("%s%n",encryptedElement.toString());
			fw.close();

		}catch(IOException ioe){
			System.err.printf("%s%n",ioe.getMessage());
			System.exit(1);
		}
	}

	void optionParser(String[] args){
		if( args.length == 6){

			boolean keyFlag = false;
			boolean inputFlag = false;
			boolean outputFlag = false;
			boolean errorFlag = false;

			for(int i=0; i < args.length;i=i+2){

				if(args[i].equals("-k")){
					keyFileName = args[i+1];
					keyFlag = true;
				}else if(args[i].equals("-i")){
					inputFileName = args[i+1];
					inputFlag = true;
				}else if(args[i].equals("-o")){
					outputFileName = args[i+1];
					outputFlag = true;
				}else{
					errorFlag = true;
				}

			}

			if(keyFlag && inputFlag && outputFlag && !errorFlag){
				return;
			}
		}
		
		System.err.printf("Usage: rsa-enc -k [public key file] -i [input file] -o [output file]");
		System.exit(1);

	}

	String inputFileName;
	String outputFileName;
	String keyFileName;
	int nBits;
	BigInteger N;
	BigInteger e;
	BigInteger input;
	BigInteger paddedElement;
	BigInteger encryptedElement;
	SecureRandom rand;


	public static void main(String[] args) {
		new RSAEnc(args);
	}
	
}
