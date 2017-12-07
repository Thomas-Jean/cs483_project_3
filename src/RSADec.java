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
RSADec.java will implement the decryption of an element in Z_n*
mod N raised to the d using RSA private key.
The modular arithmetic is done by the java BigInteger library.
*/


class RSADec{

	RSADec(){}

	RSADec(String[] args){

		rand = new SecureRandom();

		optionParser(args);

		readKey();

		readInputFile();

		decrypt();

		depadElement();

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
	    			d = new BigInteger(y);

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

	void depadElement(){

		byte[]  decryptArray = decryptedElement.toByteArray();

		int z = 0;
		for(int i = 0; i < decryptArray.length; i++){
			if(decryptArray[i] == 0){
				z = i;
				break;
			}
		}

		int l = decryptArray.length - (z+1);

		byte[] unpaddedArray = new byte[l];

		/*
		All elements after the first zero byte are considered
		part of the message. If the message was smaller than
		the n/2 -24 bits space then there will be leading zero
		when the array is used to construct the big integer for
		output these bytes are discarded as they do not affect
		the value of the integer.
		*/

		for(int i = z+1; i < decryptArray.length; i++){
			unpaddedArray[i-(z+1)] = decryptArray[i];
		}


		unpaddedElement = new BigInteger(unpaddedArray);

	}

	void decrypt(){

		decryptedElement = input.modPow(d,N);

	}

	void writeOutput(){

		try{
			PrintWriter fw = new PrintWriter( new BufferedWriter(new FileWriter(outputFileName)));

			fw.format("%s%n",unpaddedElement.toString());
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
		
		System.err.printf("Usage: rsa-enc -k [private key file] -i [input file] -o [output file]");
		System.exit(1);

	}

	String inputFileName;
	String outputFileName;
	String keyFileName;
	int nBits;
	BigInteger N;
	BigInteger d;
	BigInteger input;
	BigInteger unpaddedElement;
	BigInteger decryptedElement;
	SecureRandom rand;


	public static void main(String[] args) {
		new RSADec(args);
	}
	
}
