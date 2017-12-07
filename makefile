all: RSAKeyGen.jar RSASig.jar RSAVal.jar cbc_tag.jar cbc_val.jar CryptoLock.jar CryptoUnlock.jar

CryptoLock.jar: src/CryptoLock.class src/cbc_encrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class src/RSAVal.class src/RSASig.class src/RSAEnc.class src/cbc_tag.class
	jar cfm CryptoLock.jar src/MANIFEST_CRYPTOLOCK.MF src/CryptoLock.class src/cbc_encrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class src/RSAVal.class src/RSASig.class src/RSAEnc.class src/cbc_tag.class

CryptoUnlock.jar: src/CryptoUnlock.class src/RSADec.class src/RSAVal.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class src/cbc_val.class src/cbc_decrypt.class src/NoTags.class
	jar cfm CryptoUnlock.jar src/MANIFEST_CRYPTOUNLOCK.MF src/CryptoUnlock.class src/RSADec.class src/RSAVal.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class src/cbc_val.class src/cbc_decrypt.class src/NoTags.class

src/NoTags.class: src/NoTags.java
	javac -cp .:src/ src/NoTags.java

src/CryptoLock.class: src/CryptoLock.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/CryptoLock.java

src/CryptoUnlock.class: src/CryptoUnlock.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/CryptoUnlock.java

RSAKeyGen.jar: src/RSAKeyGen.class 
	jar cfe RSAKeyGen.jar src.RSAKeyGen src/RSAKeyGen.class

src/RSAKeyGen.class: src/RSAKeyGen.java
	javac -cp .:src/ src/RSAKeyGen.java

RSAEnc.jar: src/RSAEnc.class 
	jar cfe RSAEnc.jar src.RSAEnc src/RSAEnc.class

src/RSAEnc.class: src/RSAEnc.java
	javac -cp .:src/ src/RSAEnc.java

RSADec.jar: src/RSADec.class 
	jar cfe RSADec.jar src.RSADec src/RSADec.class

RSASig.jar: src/RSASig.class 
	jar cfe RSASig.jar src.RSASig src/RSASig.class

src/RSASig.class: src/RSASig.java
	javac -cp .:src/ src/RSASig.java

RSAVal.jar: src/RSAVal.class 
	jar cfe RSAVal.jar src.RSAVal src/RSAVal.class

src/RSAVal.class: src/RSAVal.java
	javac -cp .:src/ src/RSAVal.java

src/RSADec.class: src/RSADec.java
	javac -cp .:src/ src/RSADec.java

cbc_encrypt.jar: src/cbc_encrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm cbc_encrypt.jar src/MANIFEST_CBC_ENCRYPT.MF src/cbc_encrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class

src/cbc_encrypt.class: src/cbc_encrypt.java src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/cbc_encrypt.java

cbc_decrypt.jar: src/cbc_decrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm cbc_decrypt.jar src/MANIFEST_CBC_DECRYPT.MF src/cbc_decrypt.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class

cbc_tag.jar: src/cbc_tag.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm cbc_tag.jar src/MANIFEST_CBC_TAG.MF src/cbc_tag.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class

src/cbc_tag.class: src/cbc_tag.java src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/cbc_tag.java

cbc_val.jar: src/cbc_val.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	jar cfm cbc_val.jar src/MANIFEST_CBC_VAL.MF src/cbc_val.class src/progcommon/ReadPad.class src/progcommon/OptionParser.class

src/cbc_val.class: src/cbc_val.java src/progcommon/ReadPad.class src/progcommon/OptionParser.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/cbc_val.java

src/cbc_decrypt.class: src/cbc_decrypt.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/cbc_decrypt.java

src/progcommon/ReadPad.class: src/progcommon/ReadPad.java src/progcommon/OptionParser.class
	javac -cp .:src/:lib/commons-codec-1.10.jar src/progcommon/ReadPad.java

src/progcommon/OptionParser.class: src/progcommon/OptionParser.java
	javac -cp .:src/:lib/commons-codec-1.10.jar src/progcommon/OptionParser.java

clean:
	rm -f src/*.class
	rm -f src/progcommon/*.class

scrub: clean
	rm -f *.jar

