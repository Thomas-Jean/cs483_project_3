package src;

import progcommon.ReadPad;
import progcommon.OptionParser;
import java.security.SecureRandom;
import java.io.FileOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.lang.IllegalArgumentException;
import java.security.InvalidKeyException;


/*
Thomas Jean
CS 483 Fall 2017

cbc_decrypt.java take the file information
from OptionParser and ReadPad and then
decypts a message from cbc_enc, the message is
then written to the output file specificed.

*/

public class cbc_decrypt{
	

	public byte[] plaintext;
	public byte[] key;
	public byte[] iv;
	public byte[] ciphertext;

	/*
	decrypt takes the input helpers information
	and then decrypts the message. it will then
	depad the message. No IV will be generated
	as the IV will be the first 16 bytes of the
	cipher text
	*/

	cbc_decrypt(){}
	
	cbc_decrypt(String args[]){
		OptionParser op = new OptionParser(args,"cbc_dec");
		ReadPad rp = new ReadPad(op,false);

		ciphertext = rp.input;
		key = rp.key;

		decrypt();
		depad();
		write_plaintext(op);
	}

	/*
	decrypt take the ciphertext from cbc_enc and decrypts it. 
	*/

	protected void decrypt(){
		plaintext = new byte[ciphertext.length -16];

		try{
			SecretKeySpec sks = new SecretKeySpec(key,"AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, sks);

			/*
			The ith block of the ciphertext is decrypted though the AES block cipher and
			then XOR'ed with the ith-1 block. The zero block is the IV.
			*/

			byte[] workingBlock = new byte[16];
			for(int i=1;i<((ciphertext.length/16));i++){
				int ret = cipher.update(ciphertext,(i*16),16,workingBlock);
				byte[] xorBlock = blockXOR(workingBlock,0,16,ciphertext,(i-1)*16);


				int k=0;
				for(int j=(i-1)*16; j<(i*16);j++){
					plaintext[j] = xorBlock[k];
					k++;
				}

			}



		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}		

	}

	/*
	removes the pad from the message.
	*/

	protected void depad(){
		int padLen = plaintext[plaintext.length-1];

		byte[] unpaddedPlaintext = new byte[plaintext.length-padLen];

		for(int i=0;i < unpaddedPlaintext.length;i++){
			unpaddedPlaintext[i] = plaintext[i];
		}

		plaintext = unpaddedPlaintext;
	}

	/*
	writes the plaintext block to the output file.
	*/

	protected void write_plaintext(OptionParser op){

		try{
			FileOutputStream out = new FileOutputStream(op.outputFileName);
			out.write(plaintext);
			out.close();
		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}
	}


	byte[] blockXOR(byte[] text,int text_start,int text_offset, byte[] pad, int pad_start){

		byte[] ret;

		ret = new byte[text_offset];
		
		for(int i=0; i <text_offset;i++){
				ret[i] = (byte)(text[text_start+i] ^ pad[pad_start+i]);
		}

		return ret;


	}



	public static void main(String[] args) {
		cbc_decrypt x = new cbc_decrypt(args);


		/*

		System.out.printf("Plaintext: ");

		for(int i=0;i<x.plaintext.length;i++){
			System.out.printf("%02x",x.plaintext[i]);
		}

		System.out.printf("%n");
		*/
	}

}
