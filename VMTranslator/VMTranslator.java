import java.io.*;
import java.util.Arrays;
/**
 * VM Translator for translating Jack VM code to Hack assembly.
 * <p>Created as part of the "Build a Modern Computer from First Principles: Nand to Tetris Part II" course by Noam Nisan Shimon Schocken.
 * <p><a href="https://www.nand2tetris.org/">https://www.nand2tetris.org/</a>
 * <p><a href="https://www.coursera.org/learn/nand2tetris2">https://www.coursera.org/learn/nand2tetris2</a>
 * @author Brady Solomon
 */

// enum for types of commands that Parser can recognize
enum CommandType {
    C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
}


public class VMTranslator {

    /**
     * Main method loops through all provided .vm files, converts viable VM commands within to assembly,
     * and writes that code to a corresponding generated .asm file.
     * Note that
     * @param args List of .vm files to be translated, passed in as arguments when running the program.
     *             If no args are passed, a message is printed to System.out and the program ends.
     * @throws IOException Throws IOException if main is unable to access a given .vm file or unable
     * to generate or write to a corresponding .asm file.
     */
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("ERROR: No input provided. Ending program.");
            return;
        }
        for (String arg : args) {

            File f = new File(arg);
            String[] vmFileList;
            String outFileName;
            boolean writeBootstrap = false;
            if (f.isDirectory()) {
                // if f is a directory, get a list of all files within ending in .vm
                vmFileList = f.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.matches(".+\\.vm");
                    }
                });
                if (vmFileList != null) {
                    for (int i = 0; i < vmFileList.length; i++) {
                        vmFileList[i] = arg + File.separator + vmFileList[i];
                    }
                    Arrays.sort(vmFileList);
                    writeBootstrap = true;
                }
                else {
                    System.out.println("ERROR: Unable to find any .vm files in " + f.getName() + ". Ending program.");
                    return;
                }
                outFileName = arg + File.separator + f.getName() + ".asm";

            }
            else if (f.isFile() && arg.matches(".+\\.vm")) {
                // if f is a single .vm file, add it to vmFileList
                vmFileList = new String[] {arg};
                outFileName = arg.substring(0, arg.length() - 2) + "asm";

            }
            else {
                System.out.println("ERROR: Unable to find any .vm files at input. Ending program.");
                return;
            }


            Parser p;
            CodeWriter cw;

            // try to initialize CodeWriter
            try {
                cw = new CodeWriter(outFileName, writeBootstrap);
            } catch (IOException e) {
                System.out.println("ERROR: IOException occurred. Ending program.");
                throw new IOException(e);
            }
            // loop through all files
            for (String vmFileName : vmFileList) {
                // try to initialize Parser
                try {
                    p = new Parser(vmFileName);
                } catch (IOException e) {
                    System.out.println("ERROR: IOException occurred. Ending program.");
                    throw new IOException(e);
                }
                // set cw internal filename for generating symbols
                cw.setFileName(vmFileName);

                while (p.hasMoreLines()) {
                    p.advance();
                    CommandType commandType = p.commandType();
                    if (commandType == null) continue;
                    switch (commandType) {
                        case C_ARITHMETIC:
                            cw.writeArithmetic(p.arg1());
                            break;
                        case C_PUSH:
                        case C_POP:
                            cw.writePushPop(commandType, p.arg1(), Integer.parseInt(p.arg2()));
                            break;
                        case C_LABEL:
                            cw.writeLabel(p.arg1());
                            break;
                        case C_GOTO:
                            cw.writeGoto(p.arg1());
                            break;
                        case C_IF:
                            cw.writeIf(p.arg1());
                            break;
                        case C_FUNCTION:
                            cw.writeFunction(p.arg1(), Integer.parseInt(p.arg2()));
                            break;
                        case C_CALL:
                            cw.writeCall(p.arg1(), Integer.parseInt(p.arg2()));
                            break;
                        case C_RETURN:
                            cw.writeReturn();
                            break;
                    }
                }
            }
            cw.close();
        }

    }
}
