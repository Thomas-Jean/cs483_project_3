package progcommon;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.DecoderException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.lang.ArrayIndexOutOfBoundsException;
import java.io.File;



/*
Thomas Jean
CS 483 Fall 2017

ReadPad.java will read files that
have been parsed: input, key, and
optionally the IV file. It will
also pad the message if needed.

*/



public class ReadPad {

	public byte[] input;
	public byte[] key;
	public byte[] iv;

	public ReadPad(){}
	
	public ReadPad(OptionParser op, boolean pad){
		

		/*
		checks to make sure that the files given
		exist.
		*/

		File inputFile = new File(op.inputFileName);

		if(!inputFile.exists()){
			System.out.printf("Error: input file does not exist.%n");
			System.exit(1);
		}
		input = new byte[(int)(inputFile.length())];

		File keyFile = new File(op.keyFileName);
		if(!keyFile.exists()){
			System.out.printf("Error: key file does not exist.%n");
			System.exit(1);
		}
		key = new byte[(int)keyFile.length()];


		File ivFile = null;
		if(op.encIVFileName != null){
			ivFile = new File(op.encIVFileName);
			if(!ivFile.exists()){
				System.out.printf("Warning: IV file does not exist, using Random IV%n");
				op.encIVFileName = null;
				iv = null;
			}else{
				iv = new byte[(int)(ivFile.length())];
			}
		}
		

		// System.out.printf("%d%n",inputFile.length());
		
		readInput(inputFile, pad);

		readKey(keyFile);


		if(op.encIVFileName != null){
			readIV(ivFile);
		}

	}

	public void readInput(File inputFile, boolean pad){

		/*
		opens, reads, and if needed pads the input.
		*/

		try{
			FileInputStream in = new FileInputStream(inputFile);

			try{
				in.read(input);
				in.close();
			}catch( IOException ioe){
				System.err.printf(ioe.getMessage());
				System.exit(1);
			}

		}catch(FileNotFoundException fnfe){
			System.err.printf(fnfe.getMessage());
			System.exit(1);
		}

		if(pad == true){
			padMessage();
		}

	}


	/*
	Reads the key file, strips a newline if included, and
	decodes the hex representation to the byte values.
	*/


	public void readKey(File keyFile){
		
		Hex hexObj = new Hex();

		try{
			FileInputStream keyin = new FileInputStream(keyFile);
			
			try{
				keyin.read(key);
				keyin.close();
			}catch( IOException ioe){
				System.err.printf(ioe.getMessage());
				System.exit(1);
			}

		}catch(FileNotFoundException fnfe){
			System.err.printf(fnfe.getMessage());
			System.exit(1);
		}
		try{
			if(key[key.length-1] == 10 && key.length%2 !=0){
				/*
				System.out.printf("Removing endline%n");
				*/
				byte[] keyNew = new byte[key.length-1];
				for(int i=0; i < keyNew.length; i++){
					keyNew[i] = key[i];
				}
				key = keyNew;
			}
		}catch(ArrayIndexOutOfBoundsException aio){
			System.err.printf("Key file empty.%n");
			System.exit(1);
		}

		try{
			byte[] decodedKey = new byte[key.length/2];
			String text = new String(key);

			decodedKey = hexObj.decodeHex(text.toCharArray());
			key = decodedKey;

			/*
			for(int i=0; i< key.length;i++){
				System.out.printf("%c",key[i]);
			}
			System.out.printf("%n%d%n",key.length)
			*/

		}catch(org.apache.commons.codec.DecoderException de){
			System.err.printf(de.getMessage());
			System.exit(1);
		}

	}

	public void readIV(File ivFile){

		Hex hexObj = new Hex();

		try{
			FileInputStream ivin = new FileInputStream(ivFile);
			
			try{
				ivin.read(iv);
				ivin.close();
			}catch( IOException ioe){
				System.err.printf(ioe.getMessage());
				System.exit(1);
			}

		}catch(FileNotFoundException fnfe){
			System.err.printf(fnfe.getMessage());
			System.exit(1);
		}


		try{

			if(iv[iv.length-1] == 10 && iv.length%2 != 0){
				/*
				System.out.printf("Removing endline%n");
				*/
				byte[] ivNew = new byte[iv.length-1];
				for(int i=0; i < ivNew.length; i++){
					ivNew[i] = iv[i];
				}
				iv = ivNew;
			}
		}catch (ArrayIndexOutOfBoundsException aio){
			System.err.printf("IV file is empty.%n");
			System.exit(1);
		}

		try{
			byte[] decodedIV = new byte[key.length/2];
			String text = new String(iv);

			decodedIV = hexObj.decodeHex(text.toCharArray());
			iv = decodedIV;

			/*
			for(int i=0; i< key.length;i++){
				System.out.printf("%c",key[i]);
			}
			System.out.printf("%n%d%n",key.length)
			*/

		}catch(org.apache.commons.codec.DecoderException de){
			System.err.printf(de.getMessage());
			System.exit(1);
		}
	}

	public void padMessage(){

		byte[] unpaddedinput = input;

		int remainder = unpaddedinput.length % 16;
		int padSize = 16 - remainder;
	
		byte[] paddedinput = new byte[unpaddedinput.length + padSize];
		for(int i=0; i < unpaddedinput.length; i++){
			paddedinput[i] = unpaddedinput[i];
		}

		for(int i=unpaddedinput.length; i < paddedinput.length;i++){
			paddedinput[i] = (byte)padSize;
		}

		input = paddedinput;
	}

	/*
	main prints out the input and key in hex for verification.
	*/

	public static void main(String[] args) {
		OptionParser x = new OptionParser(args, "ReadPad");
		ReadPad y = new ReadPad(x,true);

		System.out.printf("Input: ");


		for(int i=0; i < y.input.length; i++){
			System.out.printf("%02x",y.input[i]);
		}

		System.out.printf("%n");

		System.out.printf("Key: ");


		for(int i=0; i < y.key.length; i++){
			System.out.printf("%02x",y.key[i]);
		}

		System.out.printf("%n");



	}
	
}