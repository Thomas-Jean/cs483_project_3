package src;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.lang.Math;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.nio.file.Paths;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Scanner;

/*
Thomas Jean
COSC 483
Project 2
RSAKeyGen.java will implement the generatation of N, e, and d of an
RSA public and private key. The generations of the primes and modular
arithmetic is done by the java BigInteger library.
*/


public class RSAKeyGen{


	RSAKeyGen(String[] args){

		signKeyFilename = null;
		getParameters(args);
		if (securityParameter == -1){

			System.err.printf("Usage: rsa-keygen -p [public key file] -s [secret key file] -n [number of bits in key] -c <CA secret key>%n");
			System.exit(1);
		}

		SecureRandom rand = new SecureRandom();

		BigInteger e;

		do{
			calcuateN(securityParameter, rand);

			e = picke(rand);

		}while (!(e.gcd(order).compareTo(BigInteger.ONE) == 0));

		BigInteger d = e.modInverse(order);


		writePublicKey(securityParameter, N, e, publicKeyFilename);

		writeSecretKey(securityParameter, N, d, secretKeyFilename);


		if(signKeyFilename == null){
			signPubKey(true,d);
		}else{
			readSignFile();
			signPubKey(false,d);
		}
			

	}

	void getParameters(String [] args){
		if( args.length >= 6){

			String pub = "";
			String sec = "";
			int secPar = -1;
			boolean pubFlag = false;
			boolean secFlag = false;
			boolean secParFlag = false;
			boolean errorFlag = false;

			for(int i=0; i < args.length;i=i+2){

				if(args[i].equals("-p")){
					pub = args[i+1];
					pubFlag = true;
				}else if(args[i].equals("-s")){
					sec = args[i+1];
					secFlag = true;
				}else if(args[i].equals("-n")){
					secPar = Integer.parseInt(args[i+1]);
					secParFlag = true;
				}else if(args[i].equals("-c")){
					signKeyFilename = args[i+1];
				}else{
					errorFlag = true;
				}

			}

			if(pubFlag && secFlag && secParFlag && !errorFlag){
				
				secretKeyFilename = sec;
				publicKeyFilename = pub;
				securityParameter = secPar;

				return;
			}
		}
		
		System.err.printf("Usage: rsa-keygen -p [public key file] -s [secret key file] -n [number of bits in key] -c <CA secret key>%n");
		System.exit(1);

		secretKeyFilename = "";
		publicKeyFilename = "";
		securityParameter = -1;

	}

	void calcuateN(int nbits, SecureRandom secRandom){
		int p1bits = nbits/2;
		int p2bits = nbits - p1bits;

		BigInteger prime1 = BigInteger.probablePrime(p1bits,secRandom);
		BigInteger prime2 = BigInteger.probablePrime(p2bits,secRandom);

		BigInteger N_cal = prime1.multiply(prime2);

		/*
		if the 2 primes that are n/2 are multiplied then
		the ouput will be n-1 or n bits long if it is n-1
		we generate a slightly larger prime to get a n bit
		prime number.
		*/
		while(N_cal.bitLength() != nbits){
			prime1 = BigInteger.probablePrime(p1bits+1,secRandom);
			N_cal = prime1.multiply(prime2);
		}

		prime1 = prime1.subtract(BigInteger.ONE);
		prime2 = prime2.subtract(BigInteger.ONE);

		// The order of the group is (p-1)(q-1)
		order = prime1.multiply(prime2);

		N = N_cal;
	}

	BigInteger picke(SecureRandom rand){
		int[] lowOddPrimes = {3, 5, 7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101};

		int p = lowOddPrimes[Math.abs(rand.nextInt())%lowOddPrimes.length];

		BigInteger bigP = BigInteger.valueOf(p);
		return bigP;

	}

	void writePublicKey(int numBits, BigInteger N, BigInteger e, String publicKeyFilename){
		
		try{
			PrintWriter fw = new PrintWriter( new BufferedWriter(new FileWriter(publicKeyFilename)));

			fw.format("%d%n",numBits);
			fw.format("%s%n",N.toString());
			fw.format("%s%n",e.toString());
			fw.close();

		}catch(IOException ioe){
			System.err.printf("%s%n",ioe.getMessage());
			System.exit(1);
		}
		

	}

	void writeSecretKey(int numBits, BigInteger N, BigInteger d, String secretKeyFilename){

		try{
			PrintWriter fw = new PrintWriter( new BufferedWriter(new FileWriter(secretKeyFilename)));

			fw.format("%d%n",numBits);
			fw.format("%s%n",N.toString());
			fw.format("%s%n",d.toString());
			fw.close();

		}catch(IOException ioe){
			System.err.printf("%s%n",ioe.getMessage());
			System.exit(1);
		}
		
		
	}

	void readSignFile(){
		try{
			BufferedReader rd = new BufferedReader( new FileReader(signKeyFilename));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
				if (nextLine.equals("")){ 
					break;
				}
	    		Scanner line_scanner = new Scanner(nextLine);

	    		if(i == 0){
	    			int nBits_sign = line_scanner.nextInt();

	    		}else if(i == 1){
	    			String x = line_scanner.next();
	    			N_sign = new BigInteger(x);

	    		}else if(i == 2){
	    			String y = line_scanner.next();
	    			d_sign = new BigInteger(y);

	    		}else{
	    			System.err.printf("Error: Key File is malformed.%n");
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

	void signPubKey(boolean selfSign, BigInteger d){
		
		BigInteger messageHash;

		if(selfSign){
			try{
				byte[] content = Files.readAllBytes(Paths.get(publicKeyFilename));
				MessageDigest hash = MessageDigest.getInstance("SHA-256");
				messageHash = new BigInteger(hash.digest(content)).abs();
			
				BigInteger out = messageHash.modPow(d,N);
				
				PrintWriter fw = new PrintWriter( new BufferedWriter(new FileWriter(publicKeyFilename + "-casig")));

				fw.format("%s%n",out.toString());
				fw.close();

			}catch(Exception ioe){
				System.err.printf("%s%n",ioe.getMessage());
				System.exit(1);
			}
		}else{
			try{
				byte[] content = Files.readAllBytes(Paths.get(publicKeyFilename));
				MessageDigest hash = MessageDigest.getInstance("SHA-256");
				messageHash = new BigInteger(hash.digest(content)).abs();
			
				BigInteger out = messageHash.modPow(d_sign,N_sign);
				
				PrintWriter fw = new PrintWriter( new BufferedWriter(new FileWriter(publicKeyFilename + "-casig")));

				fw.format("%s%n",out.toString());
				fw.close();

			}catch(Exception ioe){
				System.err.printf("%s%n",ioe.getMessage());
				System.exit(1);
			}
		}
	}


	BigInteger N;
	BigInteger order;

	int securityParameter;
	String publicKeyFilename;
	String secretKeyFilename;
	String signKeyFilename;

	BigInteger N_sign;
	BigInteger d_sign;


	public static void main(String[] args) {
		new RSAKeyGen(args);
	}
}
