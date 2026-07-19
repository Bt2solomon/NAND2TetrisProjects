import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Jack Compiler for tokenizing a Jack file and interpreting its grammar, then translating it to VM language and writing it to a .vm file.
 * <p>Created as part of the "Build a Modern Computer from First Principles: Nand to Tetris Part II" course by Noam Nisan Shimon Schocken.
 * <p><a href="https://www.nand2tetris.org/">https://www.nand2tetris.org/</a>
 * <p><a href="https://www.coursera.org/learn/nand2tetris2">https://www.coursera.org/learn/nand2tetris2</a>
 * @author Brady Solomon
 */
public class JackCompiler {
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("ERROR: No input provided. Ending program.");
            return;
        }

        for (String arg : args) {
            File f = new File(arg);
            String[] jackFileList;
            if (f.isDirectory()) {
                // if f is a directory, get a list of all files within ending in .jack
                jackFileList = f.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.matches(".+\\.jack");
                    }
                });
                if (jackFileList != null) {
                    for (int i = 0; i < jackFileList.length; i++) {
                        jackFileList[i] = arg + File.separator + jackFileList[i];
                    }
                    Arrays.sort(jackFileList);
                }
                else {
                    System.out.println("ERROR: Unable to find any .jack files in " + f.getName() + ". Ending program.");
                    return;
                }
            }
            else if (f.isFile() && arg.matches(".+\\.jack")) {
                // if f is a single .jack file, add it to jackFileList
                jackFileList = new String[] {arg};

            }
            else {
                System.out.println("ERROR: Unable to find any .jack files at input. Ending program.");
                return;
            }

            for (String jackFileName : jackFileList) {
                run(jackFileName);

            }
        }
    }

    /**
     * Analyzes the .jack file with the given name and generates a .vm file with the translated VM code.
     * @param jackFileName The name of the .jack file to compile.
     * @throws Exception Throws exception if a file cannot be read or written to, or if there is a grammatical mismatch in the Jack code.
     */
    public static void run(String jackFileName) throws Exception {
        String outFileName = jackFileName.substring(0, jackFileName.length() - 4) + "vm";
        File jackFile = new File(jackFileName);
        File xmlFile = new File(outFileName);
        JackTokenizer tokenizer = new JackTokenizer(jackFile);
        CompilationEngine compEngine = new CompilationEngine(tokenizer, xmlFile);
        compEngine.compileClass();
        compEngine.close();

    }
}