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

cbc_encrypt.java take the file information
from OptionParser and ReadPad and then
encrypts a message, the message is then
written to the output file specificed.

*/


public class cbc_encrypt{
	

	public byte[] plaintext;
	public byte[] key;
	public byte[] iv;
	public byte[] ciphertext;

	/*
	cbc_encrypt takes the input helpers and then
	generates an IV if needed
	*/

	cbc_encrypt(){}

	cbc_encrypt(String args[]){
		OptionParser op = new OptionParser(args, "cbc_enc");
		ReadPad rp = new ReadPad(op, true);

		plaintext = rp.input;
		key = rp.key;
		iv = rp.iv;
		if(iv == null){
			iv = generateIV();
		}

		encrypt();

		write_ciphertext(op);


		
	}


	/*
	generateIV will if an IV has not
	been provided generate the pseudo-random
	IV for the encyption.	
	*/
	protected byte[] generateIV(){
		SecureRandom srng = new SecureRandom();
		byte[] randIV = new byte[16];

		srng.nextBytes(randIV);
		return randIV;

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


						/*
						for(int n=0; n<16; n++){
							System.out.printf("%02x",workingBlock[n]);
						}
						System.out.printf("%n");
						*/

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

	protected void write_ciphertext(OptionParser op){

		try{
			FileOutputStream out = new FileOutputStream(op.outputFileName);
			out.write(ciphertext);
			out.close();
		}catch(Exception e){
			System.err.printf("%s%n",e.getMessage());
			System.exit(1);
		}
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

	public static void main(String[] args) {
		cbc_encrypt x = new cbc_encrypt(args);


		/*
		System.out.printf("Ciphertext: ");

		for(int i=0;i<x.ciphertext.length;i++){
			System.out.printf("%02x",x.ciphertext[i]);
		}

		System.out.printf("%n");
		*/

	}


}
