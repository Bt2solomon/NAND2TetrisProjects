import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Hack assembler for translating assembly language into Hack binary.
 * <p>Created as part of the "Build a Modern Computer from First Principles: Nand to Tetris Part I" course by Noam Nisan Shimon Schocken.
 * <p><a href="https://www.nand2tetris.org/">https://www.nand2tetris.org/</a>
 * <p><a href="https://www.coursera.org/learn/nand2tetris">https://www.coursera.org/learn/nand2tetris</a>
 * @author Brady Solomon
 */
public class HackAssembler {
    /**
     * The main method collects .asm file names passed in either as args when running the program or by prompting the user to enter one, then runs the assembler on each file name.
     * @param args A list of user inputs passed when running the program. Each arg should be either a .asm file or the name of a directory containing .asm files.
     * @throws IOException Throws exception if a file corresponding to a provided file name cannot be found.
     */
    public static void main(String[] args) throws IOException {
        // need to find file names and call the run method on them
        // if no additional args were provided when running the program, prompt the user to enter a filename.
        if (args.length == 0) {
            Scanner inputScanner = new Scanner(System.in);
            System.out.println("Enter .asm file name: ");
            String asmFileName = inputScanner.nextLine();
            // ensure that the provided file name is .asm and call run
            if (asmFileName.matches(".+\\.asm")) {
                run(asmFileName);
            }
            else {
                System.out.println("ERROR: Invalid filename. Ending program.");
            }
            return;
        }

        // if arguments are provided, iterate through them to get file names
        for (String arg : args) {
            File f = new File(arg);
            String[] asmFileList;
            if (f.isDirectory()) {
                // if f is a directory, get a list of all files within ending in .asm
                asmFileList = f.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.matches(".+\\.asm");
                    }
                });
                if (asmFileList != null) {
                    for (int i = 0; i < asmFileList.length; i++) {
                        asmFileList[i] = arg + File.separator + asmFileList[i];
                    }
                    Arrays.sort(asmFileList);
                }
                else {
                    System.out.println("ERROR: Unable to find any .asm files in " + f.getName() + ". Ending program.");
                    return;
                }
            }
            else if (f.isFile() && arg.matches(".+\\.asm")) {
                // if f is a single .asm file, add it to asmFileList
                asmFileList = new String[] {arg};

            }
            else {
                System.out.println("ERROR: Invalid filename. Ending program.");
                return;
            }

            for (String asmFileName : asmFileList) {
                run(asmFileName);

            }
        }


    }

    /**
     * Opens a provided .asm file, parses through it, and generates corresponding hack binary code in a .hack file.
     * @param asmFileName The name of the file to be parsed
     * @throws IOException Throws exception if the provided file cannot be opened
     */
    public static void run(String asmFileName) throws IOException {
        // get name of output hack file and open asmFile
        String hackFileName = asmFileName.substring(0, asmFileName.length()-4) + ".hack";
        File asmFile = new File(asmFileName);

        // try block to catch if the file wasn't opened properly
        try {
            // initialize Parser, Code, and SymbolTable objects
            Parser p = new Parser(asmFile);
            Code c = new Code();
            SymbolTable st = new SymbolTable();

            // The asm file is parsed twice; first to find any labels and add them to the symbolTable, then to generate the actual code instructions.
            //label pass
            int lineNum = 0;
            while (p.hasMoreLines()) {
                String out = "";
                p.advance();
                String instr = p.getInstructionType();
                if (instr != null) {
                    switch (p.getInstructionType()) {
                        // for non-label instructions, just iterate line counter
                        case "A_INSTRUCTION", "C_INSTRUCTION":
                            lineNum++;
                            break;
                        // for label instructions, add the label's symbol and lineNum to the symbolTable
                        case "L_INSTRUCTION":
                            st.addEntry(p.getSymbol(), lineNum);

                        default:
                            break;
                    }
                }
            }
            // reset the parser ahead of the instruction pass
            p = new Parser(asmFile);

            //instruction pass
            try (FileWriter hackFile = new FileWriter(hackFileName)) {
                lineNum = 0;
                while (p.hasMoreLines()) {
                    // 'out' stores the command being constructed
                    String out = "";
                    p.advance();
                    String instr = p.getInstructionType();
                    if (instr != null) {
                        switch (p.getInstructionType()) {
                            // A-instructions set the A register to a given value. If the value is a symbol, consult the symbol table to find its value (or add it if it is a new symbol)
                            // A-instructions in Hack are represented by 0 followed by the 15-digit binary value, so simply converting the given value to 16-digit binary will result in the correct instruction
                            case "A_INSTRUCTION":
                                String symbol = p.getSymbol();
                                String symBinary = c.convertToBinary(symbol, 16);
                                if (symBinary != null) { // system is number
                                    out = symBinary;
                                } else { //symbol is variable
                                    // if the symbol is a variable, get or set its value in the SymbolTable
                                    int addr;
                                    if (st.contains(symbol)) {
                                        addr = st.getAddress(symbol);
                                    }
                                    else {
                                        addr = st.addEntry(symbol);
                                    }
                                    out = c.convertToBinary(addr, 16);
                                }
                                if (p.hasMoreLines()) out += "\n";
                                lineNum++;
                                break;
                            // C-instructions tell the Hack computer to perform some computation.
                            // C-instructions in Hack are formatted with three 1s, followed by a 7-digit computation command, then  3 digits for destination, then 3 digits for a jump condition
                            case "C_INSTRUCTION":
                                out = "111" + c.comp(p.getComp()) + c.dest(p.getDest()) + c.jump(p.getJump());
                                if (p.hasMoreLines()) out += "\n";
                                lineNum++;
                                break;
                            default:
                                break;
                        }
                    }
                    // once a command is constructed, write it to the output file.
                    hackFile.write(out);
                }
            }
        // catch and print any errors thrown when trying to access the input or output files.
        } catch (FileNotFoundException e) {
            System.out.println("Error: ");
            e.printStackTrace();
        }

    }
}