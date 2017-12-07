package src;

import progcommon.ReadPad;
import progcommon.OptionParser;
import java.security.SecureRandom;
import java.io.FileOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.IllegalArgumentException;
import java.security.InvalidKeyException;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;


/*
Thomas Jean
CS 483 Fall 2017

cbc_encrypt.java take the file information
from OptionParser and ReadPad and then
encrypts a message, the message is then
written to the output file specificed.

*/


public class cbc_val{
	

	public byte[] plaintext;
	public byte[] key;
	public byte[] iv;
	public byte[] ciphertext;
	public byte[] read_tag;
	public byte[] gen_tag;
	public String keyFileName;
	public String tagFileName;
	public String messageFileName;
	public boolean status;

	/*
	cbc_encrypt takes the input helpers and then
	generates an IV if needed
	*/

	cbc_val(){}

	cbc_val(String args[]){

		getOptions(args);

		OptionParser op = new OptionParser();

		op.inputFileName = messageFileName;
		op.outputFileName = tagFileName;
		op.keyFileName = keyFileName;

		ReadPad rp = new ReadPad(op, true);

		plaintext = rp.input;
		key = rp.key;
		iv = generateIV();

		prependLength();

		encrypt();

		getTag();

		readTag();

		status = compareTag();

		if(status == true){
			System.out.printf("True%n");

		}else{
			System.out.printf("False%n");

		}

		
	}


	/*
	generateIV will if an IV has not
	been provided generate the pseudo-random
	IV for the encyption.	
	*/
	protected byte[] generateIV(){
		byte[] zero_iv = new byte[16];
		return zero_iv;

	}

	void prependLength(){
		int length = plaintext.length * 8;
		byte [] converted_int = ByteBuffer.allocate(4).putInt(length).array();

		byte[] newPlaintext = new byte[plaintext.length + 16];

		for(int i=12; i < 16; i++){
			newPlaintext[i] = converted_int[i-12];
		}

		for(int i=16; i < newPlaintext.length;i++){
			newPlaintext[i] = plaintext[i-16];
		}

		plaintext = newPlaintext;


		
	}

	/*
	encypt simply produces the ciphertext from the plaintext
	using CBC mode. The plaintext will have already
	been padded by ReadPad, and the ciphertext includes additonal
	space for the IV. The IV will start the xor chain before encyption.
	*/
	protected void encrypt(){

		ciphertext = new byte[plaintext.length + 16];
		/*
		The Cipher is intialized and then the iv is copied over. Then the
		the IV is XOR'ed with the first block of plaintext and put through
		block cipher, and then written ciphertext space. Then then next block
		is XOR'ed with the previous block and so on. The cipertext is then written
		to the output file.
		*/
		try{
			SecretKeySpec sks = new SecretKeySpec(key,"AES");

			try{
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
				cipher.init(Cipher.ENCRYPT_MODE, sks);

				try{
					for(int i=0; i<16;i++){
						ciphertext[i] = iv[i];
					}
					byte[] workingBlock = new byte[16];
					for(int i=1;i<((plaintext.length/16)+1);i++){
						if(i == 1){
							byte[] xorBlock = blockXOR(plaintext,0,16,iv,0);
							int ret = cipher.update(xorBlock,0,16,workingBlock);
							// System.out.printf("%d%n",ret);
						}else{
							byte[] xorBlock = blockXOR(ciphertext,(i-1)*16,16,plaintext,(i-1)*16);
							int ret = cipher.update(xorBlock,0,16,workingBlock);
						}


						int k = 0;
						for(int j=(i*16);j<	(i*16)+16;j++){
							ciphertext[j] = workingBlock[k];
							k++;
						}
					}

				}catch(Exception e){
					System.err.printf("%s%n",e.getMessage());
					System.exit(1);
				}

			}catch(Exception e){
				System.err.printf("%s%n",e.getMessage());
				System.exit(1);

			}

		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}

	}

	/*
	writes the ciphertext block to the output file.
	*/

	protected void getTag(){
		byte[] lastBlock = new byte[16];

		int j = 0;
		for(int i = ciphertext.length - 16; i < ciphertext.length;i++){
			lastBlock[j] = ciphertext[i];
			j++;
		}

		gen_tag = lastBlock;
	}

	byte[] blockXOR(byte[] text, byte[] pad){

		byte[] ret;

		ret = new byte[text.length];
		
		for(int i=0; i <text.length;i++){
				ret[i] = (byte)(text[i] ^ pad[i]);
		}

		return ret;


	}

	byte[] blockXOR(byte[] text,int text_start,int text_offset, byte[] pad, int pad_start){

		byte[] ret;

		ret = new byte[text_offset];
		
		for(int i=0; i <text_offset;i++){
				ret[i] = (byte)(text[text_start+i] ^ pad[pad_start+i]);
		}

		return ret;


	}

	void getOptions(String[] args){
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
				}else if(args[i].equals("-t")){
					tagFileName = args[i+1];
					outputFlag = true;
				}else{
					errorFlag = true;
				}

			}

			if(keyFlag && inputFlag && outputFlag && !errorFlag){
				return;
			}
		}
		
		System.err.printf("Usage: cbcmac-validate -k [key file] -m [message file] -t [tag file]%n");
		System.exit(1);

	}

	void readTag(){

		read_tag = new byte[16];

		try{
			FileInputStream in = new FileInputStream(tagFileName);

			try{
				in.read(read_tag);
				in.close();
			}catch( IOException ioe){
				System.err.printf(ioe.getMessage());
				System.exit(1);
			}

		}catch(FileNotFoundException fnfe){
			System.err.printf(fnfe.getMessage());
			System.exit(1);
		}

	}

	boolean compareTag(){

		for(int i = 0; i < 16;i++){
			if(gen_tag[i] != read_tag[i]){
				return false;
			}
		}
		return true;


	}


	public static void main(String[] args) {
		cbc_val x = new cbc_val(args);


		/*
		System.out.printf("Ciphertext: ");

		for(int i=0;i<x.ciphertext.length;i++){
			System.out.printf("%02x",x.ciphertext[i]);
		}

		System.out.printf("%n");
		*/

	}


}
