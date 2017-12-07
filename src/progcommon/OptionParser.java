package progcommon;

/*
Thomas Jean
CS 483 Fall 2017

OptionParser.java will take the command line
arguments and get the file names.

*/


public class OptionParser {


	public String inputFileName;
	public String outputFileName;
	public String keyFileName;
	public String encIVFileName;
    
    public OptionParser(){}

	public OptionParser(String args[],String programName){

		boolean inputFlag = false;
		boolean outputFlag = false;
		boolean keyFlag = false;
		boolean ivFlag = false;

		if(args.length == 6 || args.length == 8){

			for(int i=0; i < args.length; i=i+2){
				if(args[i].equals("-i")){
					if(inputFlag == false){
						inputFileName = args[i+1];
						inputFlag = true;
					}else{
						System.out.printf("Error: input file already specified%n");

						System.exit(1);
					}
				}else if(args[i].equals("-o")){
					if(outputFlag == false){
						outputFileName = args[i+1];
						outputFlag = true;
					}else{
						System.out.printf("Error: output file already specified%n");

						System.exit(1);
					}

				}else if(args[i].equals("-k")){
					if(keyFlag == false){
						keyFileName = args[i+1];
						keyFlag = true;
					}else{
						System.out.printf("Error: key file already specified%n");

						System.exit(1);
					}

				}else if(args[i].equals("-v")){
					if(ivFlag == false){
						encIVFileName = args[i+1];
						ivFlag = true;
					}else{
						System.out.printf("Error: IV file already specified%n");

						System.exit(1);
					}

				}else{

				}
			}

			if(inputFlag && outputFlag && keyFlag){
				// System.out.printf("Input File: %s%n",inputFileName);
				// System.out.printf("Output File: %s%n", outputFileName);
				// System.out.printf("Key File: %s%n", keyFileName);
				if(ivFlag){
					// System.out.printf("IV File: %s%n", encIVFileName);
				}else{
					encIVFileName = null;
				}
			}else{
				System.out.printf("Usage: %s -k [keyFile] -i [inputFile] -o [outputFile] -v <IVFile> %n",programName);

				System.exit(1);

			}

			}else{
				System.out.printf("Usage: %s -k [keyFile] -i [inputFile] -o [outputFile] -v <IVFile> %n",programName);

				System.exit(1);
			}

		}

		public static void main(String[] args) {
			OptionParser x = new OptionParser(args, "OptionParser");
		}

}
