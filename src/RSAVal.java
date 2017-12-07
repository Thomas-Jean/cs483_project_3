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
import java.nio.file.Files;
import java.security.MessageDigest;
import java.nio.file.Paths;






/*
Thomas Jean
COSC 483
Project 3
RSASig.java will implement the validation of a RSA Sig
*/



public class RSAVal{

	public RSAVal(){}

	public RSAVal(String[] args){

		rand = new SecureRandom();

		optionParser(args);


		readKey();
		readSig();

		readInputFile();


		status = validate();


		if(status == true){
			System.out.printf("True%n");

		}else{
			System.out.printf("False%n");

		}


	}

	public void readKey(){

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

	public void readInputFile(){

		try{
			byte[] content = Files.readAllBytes(Paths.get(messageFileName));
			MessageDigest hash = MessageDigest.getInstance("SHA-256");
			messageHash = new BigInteger(hash.digest(content)).abs();
		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);

		}
	}

	public void readSig(){
		try{
			BufferedReader rd = new BufferedReader( new FileReader(sigFileName));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
	    		if (nextLine.equals("")){ 
	    			break;
	    		}
	    		Scanner line_scanner = new Scanner(nextLine);
	    		String x = line_scanner.next();
	    		sigInt = new BigInteger(x);
	    		line_scanner.close();

	    	}
	    }catch(IOException ioe){

	    	System.err.printf("IO Exception: %s%n",ioe.getMessage());
			System.exit(1);
	    }

	}

	public boolean validate(){

		sigInt = sigInt.modPow(d,N);


		if(sigInt.compareTo(messageHash) == 0){
			return true;
		}else{
			return false;
		}

	}

	public void optionParser(String[] args){
		if( args.length == 6){

			boolean keyFlag = false;
			boolean inputFlag = false;
			boolean outputFlag = false;
			boolean errorFlag = false;

			for(int i=0; i < args.length;i=i+2){

				if(args[i].equals("-k")){
					keyFileName = args[i+1];
					keyFlag = true;
				}else if(args[i].equals("-m")){
					messageFileName = args[i+1];
					inputFlag = true;
				}else if(args[i].equals("-s")){
					sigFileName = args[i+1];
					outputFlag = true;
				}else{
					errorFlag = true;
				}

			}

			if(keyFlag && inputFlag && outputFlag && !errorFlag){
				return;
			}
		}
		
		System.err.printf("Usage: rsa-val -k [public key file] -m [message file] -s [signature file]%n");
		System.exit(1);

	}

	public String messageFileName;
	public String sigFileName;
	public String keyFileName;
	public int nBits;
	public BigInteger N;
	public BigInteger d;
	public BigInteger messageHash;
	public BigInteger sigInt;
	public SecureRandom rand;
	public boolean status;

	public static void main(String[] args) {
		new RSAVal(args);
	}
	
}
