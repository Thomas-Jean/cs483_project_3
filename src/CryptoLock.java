package src;


import java.nio.file.*;
import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.lang.Math;
import java.security.MessageDigest;
import java.util.Scanner;
import progcommon.OptionParser;
import progcommon.ReadPad;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.DecoderException;

public class CryptoLock {
	
	CryptoLock(String[] args){
		optionParser(args);

		if(!directoryPath.endsWith("/")){
			directoryPath = directoryPath + "/";
		}


		File dir = new File(directoryPath);

		String [] fileNames = dir.list();

		validatePublicKey();

		readPrivateKey();

		keyGenAndOutput();

		for(String fn : fileNames){
			encryptAndTag(directoryPath + fn);
		}
	}


	public void keyGenAndOutput(){
		generateKey();
		RSAEnc keyEncrypter = new RSAEnc();

		keyEncrypter.input = new BigInteger(AES_Key).abs();
		AES_Key = keyEncrypter.input.toByteArray();

		keyEncrypter.e = public_e;
		keyEncrypter.N = public_N;
		keyEncrypter.outputFileName = directoryPath + "SymmetricKeyManifest";

		keyEncrypter.constructElement();
		keyEncrypter.encrypt();
		keyEncrypter.writeOutput();

		RSASig keySigner = new RSASig();
		keySigner.N = secret_N;
		keySigner.d = secret_d;
		keySigner.messageFileName = directoryPath + "SymmetricKeyManifest";
		keySigner.sigFileName = directoryPath + "SymmetricKeyManifest-sign";
		keySigner.readInputFile();
		keySigner.sign();
		keySigner.writeOutput();

	}

	public void encryptAndTag(String FileName){
		if(FileName.compareTo(directoryPath + "SymmetricKeyManifest") != 0 && FileName.compareTo(directoryPath + "SymmetricKeyManifest-sign") != 0){

			ReadPad myreader = new ReadPad();

			File inputFile = new File(FileName);

			if(!inputFile.exists()){
				System.out.printf("Error: input file does not exist.%n");
				System.exit(1);
			}
			myreader.input = new byte[(int)(inputFile.length())];
			myreader.readInput(inputFile,true);

			cbc_encrypt crypter = new cbc_encrypt();

			crypter.plaintext = myreader.input;
			crypter.key = AES_Key;
			crypter.iv = crypter.generateIV();
			crypter.encrypt();


			OptionParser ops = new OptionParser();
			ops.outputFileName = FileName;

			crypter.write_ciphertext(ops);

			ops.inputFileName = FileName;
			ops.outputFileName = FileName + ".tag";

			File encryptedFile = new File(FileName);
			ReadPad tagReader = new ReadPad();
			if(!encryptedFile.exists()){
				System.out.printf("Error: input file does not exist.%n");
				System.exit(1);
			}
			tagReader.input = new byte[(int)(encryptedFile.length())];
			tagReader.readInput(encryptedFile,true);

			cbc_tag tagger = new cbc_tag();

			tagger.plaintext = tagReader.input;
			tagger.key = AES_Key;
			tagger.iv = tagger.generateIV();

			tagger.prependLength();
			tagger.encrypt();
			tagger.write_ciphertext(ops);
		}
	}

	public void generateKey(){
		SecureRandom rand = new SecureRandom();
		AES_Key = new byte[16];
		rand.nextBytes(AES_Key);

	}

	public void validatePublicKey(){
		readPublicKeyFile();
		readValidationKeyFile();

		RSAVal pubVal = new RSAVal();

		pubVal.d = validating_e;
		pubVal.N = validating_N;

		pubVal.messageFileName = publicKeyFile;
		pubVal.sigFileName = publicKeyFile + "-casig";
		pubVal.readSig();

		pubVal.readInputFile();


		boolean status = pubVal.validate();

		if(status == true){
			System.out.printf("Public Key Validated.%n");
			return;
		}else{
			System.out.printf("Public Key Not Validated.... Aborting%n");
			System.exit(1);
		}


	}

	public void readPublicKeyFile(){
		try{
			BufferedReader rd = new BufferedReader( new FileReader(publicKeyFile));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
				if (nextLine.equals("")){ 
					break;
				}
	    		Scanner line_scanner = new Scanner(nextLine);

	    		if(i == 0){
	    			int nBits = line_scanner.nextInt();
	    			

	    		}else if(i == 1){
	    			String x = line_scanner.next();
	    			public_N = new BigInteger(x);

	    		}else if(i == 2){
	    			String y = line_scanner.next();
	    			public_e = new BigInteger(y);

	    		}else{
	    			System.err.printf("Error: Public key File is malformed.%n");
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

	public void readValidationKeyFile(){
		try{
			BufferedReader rd = new BufferedReader( new FileReader(validationKeyFile));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
				if (nextLine.equals("")){ 
					break;
				}
	    		Scanner line_scanner = new Scanner(nextLine);

	    		if(i == 0){
	    			int nBits = line_scanner.nextInt();
	    			

	    		}else if(i == 1){
	    			String x = line_scanner.next();
	    			validating_N = new BigInteger(x);

	    		}else if(i == 2){
	    			String y = line_scanner.next();
	    			validating_e = new BigInteger(y);

	    		}else{
	    			System.err.printf("Error: Validating key File is malformed.%n");
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

	public void readPrivateKey(){
		try{
			BufferedReader rd = new BufferedReader( new FileReader(privateKeyFile));

			int i = 0;
			String nextLine;
			while ((nextLine = rd.readLine()) != null) {
				if (nextLine.equals("")){ 
					break;
				}
	    		Scanner line_scanner = new Scanner(nextLine);

	    		if(i == 0){
	    			int nBits = line_scanner.nextInt();
	    			

	    		}else if(i == 1){
	    			String x = line_scanner.next();
	    			secret_N = new BigInteger(x);

	    		}else if(i == 2){
	    			String y = line_scanner.next();
	    			secret_d = new BigInteger(y);

	    		}else{
	    			System.err.printf("Error: Validating key File is malformed.%n");
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



	public void optionParser(String[] args){
		if( args.length == 8){

			boolean publicKeyFlag = false;
			boolean privateKeyFlag = false;
			boolean directoryFlag = false;
			boolean validatingFlag = false;
			boolean errorFlag = false;

			for(int i=0; i < args.length;i=i+2){

				if(args[i].equals("-d")){
					directoryPath = args[i+1];
					directoryFlag = true;
				}else if(args[i].equals("-p")){
					publicKeyFile = args[i+1];
					publicKeyFlag = true;
				}else if(args[i].equals("-r")){
					privateKeyFile = args[i+1];
					privateKeyFlag = true;
				}else if(args[i].equals("-vk")){
					validationKeyFile = args[i+1];
					validatingFlag = true;
				}else{
					errorFlag = true;
				}

			}

			if(directoryFlag && publicKeyFlag && privateKeyFlag && validatingFlag && !errorFlag){
				return;
			}
		}
		
		System.err.printf("Usage: lock -d [dir] -p [action public key] -r [action private key] -vk [validating public key]%n");
		System.exit(1);

	}

	public void debugKeyPrint(){
		Hex hexobj = new Hex();
		byte[] HexKey = hexobj.encode(AES_Key);
		try{
			FileOutputStream out = new FileOutputStream("debugkey.txt");
			out.write(HexKey);
			out.close();
		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}

	}

	String directoryPath;
	String publicKeyFile;
	String privateKeyFile;
	String validationKeyFile;
	BigInteger public_N;
	BigInteger public_e;
	BigInteger secret_N;
	BigInteger secret_d;
	BigInteger validating_N;
	BigInteger validating_e;
	byte [] AES_Key;


	public static void main(String[] args) {
		new CryptoLock(args);
	}
}