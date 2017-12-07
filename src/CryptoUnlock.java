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

public class CryptoUnlock {
	
	CryptoUnlock(String[] args){
		optionParser(args);

		if(!directoryPath.endsWith("/")){
			directoryPath = directoryPath + "/";
		}
		validatePublicKey();

		readPrivateKey();

		getAESKey();

		File dir = new File(directoryPath);
		String [] fileNames = dir.list(new NoTags());

		for(String fn : fileNames){
			validateTag(directoryPath + fn);
		}

		
		for(String fn : fileNames){
			File tag = new File(directoryPath + fn + ".tag");
			tag.delete();
			decryptFile(directoryPath + fn);
		}
		File Manifest = new File(directoryPath + "SymmetricKeyManifest");
		File ManifestSig = new File(directoryPath + "SymmetricKeyManifest-sign");
		Manifest.delete();
		ManifestSig.delete();
	}


	public void getAESKey(){
		validateAESSign();
		decryptAESKey();

	}

	public void validateAESSign(){
		RSAVal AESVal = new RSAVal();

		AESVal.d = public_e;
		AESVal.N = public_N;

		AESVal.messageFileName = directoryPath + "SymmetricKeyManifest";
		AESVal.sigFileName = directoryPath + "SymmetricKeyManifest-sign";
		AESVal.readSig();

		AESVal.readInputFile();


		boolean status = AESVal.validate();

		if(status == true){
			System.out.printf("AES Key Validated.%n");
			return;
		}else{
			System.out.printf("AES Key Not Validated.... Aborting%n");
			System.exit(1);
		}
	}

	public void decryptAESKey(){
		RSADec keyDecrypter = new RSADec();

		keyDecrypter.inputFileName = directoryPath + "SymmetricKeyManifest";
		keyDecrypter.d = secret_d;
		keyDecrypter.N = secret_N;

		keyDecrypter.readInputFile();
		keyDecrypter.decrypt();

		keyDecrypter.depadElement();
		AES_Key = keyDecrypter.unpaddedElement.toByteArray();

	}

	public void validateTag(String FileName){
		if(FileName.compareTo(directoryPath + "SymmetricKeyManifest") != 0 && FileName.compareTo(directoryPath + "SymmetricKeyManifest-sign") != 0){

			File encryptedFile = new File(FileName);
			ReadPad tagReader = new ReadPad();
			if(!encryptedFile.exists()){
				System.out.printf("Error: input file does not exist.%n");
				System.exit(1);
			}
			tagReader.input = new byte[(int)(encryptedFile.length())];
			tagReader.readInput(encryptedFile,true);
			OptionParser ops = new OptionParser();

			ops.inputFileName = FileName;
			ops.outputFileName = FileName + ".tag";

			cbc_val tagChecker = new cbc_val();
			tagChecker.plaintext = tagReader.input;
			tagChecker.key = AES_Key;
			tagChecker.iv = tagChecker.generateIV();
			tagChecker.messageFileName = FileName;
			tagChecker.tagFileName = FileName + ".tag";

			tagChecker.prependLength();
			tagChecker.encrypt();
			tagChecker.getTag();
			tagChecker.readTag();

			boolean status = tagChecker.compareTag();

			if(status == true){
				System.out.printf("Tag for %s validated%n",FileName);

			}else{
				System.out.printf("Tag for %s not validated... Aborting.%n",FileName);
				System.exit(1);

			}

		}
	}

	public void decryptFile(String FileName){
		if(FileName.compareTo(directoryPath + "SymmetricKeyManifest") != 0 && FileName.compareTo(directoryPath + "SymmetricKeyManifest-sign") != 0){

			OptionParser ops = new OptionParser();
			ReadPad encReader = new ReadPad();

			ops.inputFileName = FileName;
			ops.outputFileName = FileName;

			File encryptedFile = new File(FileName);
			if(!encryptedFile.exists()){
				System.out.printf("Error: input file does not exist.%n");
				System.exit(1);
			}
			encReader.input = new byte[(int)(encryptedFile.length())];
			encReader.readInput(encryptedFile,false);
			cbc_decrypt decrypter = new cbc_decrypt();

			decrypter.ciphertext = encReader.input;
			decrypter.key = AES_Key;

			decrypter.decrypt();
			decrypter.depad();
			decrypter.write_plaintext(ops);

		}
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
		
		System.err.printf("Usage: unlock -d [dir] -p [action public key] -r [action private key] -vk [validating public key]%n");
		System.exit(1);

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
		new CryptoUnlock(args);
	}
}