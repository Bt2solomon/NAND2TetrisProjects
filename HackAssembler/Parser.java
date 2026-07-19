import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Parses a given file and extracts information about the assembly instructions it contains.
 */
class Parser {
    private final Scanner sc;
    private String instructionType = null; // "L_INSTRUCTION" = label; "A_INSTRUCTION" = A-command; "C_INSTRUCTION" = C-command
    private String symbol = null;
    private String dest = null;
    private String comp = null;
    private String jump = null;

    /**
     * Constructs a new Parser for the given file.
     * @param fileName The file to be parsed
     * @throws FileNotFoundException Throws exception if the given file cannot be found
     */
    public Parser(File fileName) throws FileNotFoundException {
        sc = new Scanner(fileName).useDelimiter("\n");
    }

    /**
     * Checks if the file has more lines.
     * @return True if there are more lines in the file, false otherwise.
     */
    public boolean hasMoreLines() {
        return sc.hasNext();
    }

    /**
     * Parses the next line of the file.
     */
    public void advance() {
        // check for labels
        // in assembly, labels are formatted as a symbol in parentheses
        String label = sc.findInLine("\\([\\w_.$:]+\\)");
        if (label != null) {
            instructionType = "L_INSTRUCTION";
            symbol = label.substring(1,label.length()-1);
            dest = null;
            comp = null;
            jump = null;
        }
        else {
            // check for a-instruction
            // in assembly, a-instructions are formatted as an '@' character followed by a symbol
            String aInstr = sc.findInLine("@[\\w_.$:]+");
            if (aInstr != null) {
                instructionType = "A_INSTRUCTION";
                symbol = aInstr.substring(1);
                dest = null;
                comp = null;
                jump = null;
            }
            else {
                //check for c-instruction
                // in assembly, c-instructions are formatted as [dest]=[comp];[jump] or some parts of that
                if (sc.hasNext("(\\h*[AMD]{0,3}=?)([AMD01+\\-&|!]{1,3})(;?[JMPGLTNEQ]{0,3}\\h*)")) {
                    instructionType = "C_INSTRUCTION";
                    symbol = null;
                    //dest
                    String cDest = sc.findInLine("[AMD]+=");
                    if (cDest != null) {
                        dest = cDest.substring(0, cDest.length() - 1);
                    } else dest = null;
                    //comp
                    comp = sc.findInLine("[AMD01+\\-&|!]+");
                    //jump
                    String cJump = sc.findInLine(";J[MPGLTNEQ]{2}");
                    if (cJump != null) {
                        jump = cJump.substring(1);
                    } else jump = null;
                }
                else {
                    instructionType = null;
                    symbol = null;
                    dest = null;
                    comp = null;
                    jump = null;
                }

            }
        }
        if (sc.hasNext()) sc.nextLine();

    }
    // getter methods:
    /**
     * Gets the type of the instruction that was last parsed.
     * @return "L_INSTRUCTION" for labels; "A_INSTRUCTION" for A-commands; "C_INSTRUCTION" for C-commands
     */
    public String getInstructionType() {
        return instructionType;
    }

    /**
     * Gets the symbol from the previous instruction, if it was a label or A-command.
     * @return The symbol from the previous instruction, or null if the last instruction was a C-command
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the destination of the previous instruction, if it included one.
     * @return The destination of the previous instruction, or null if the previous instruction did not include a destination
     */
    public String getDest() {
        return dest;
    }

    /**
     * Gets the computation segment of the previous instruction, if it was a C-command.
     * @return The computation segment of the previous instruction, or null if the previous instruction was not a C-command
     */
    public String getComp() {
        return comp;
    }

    /**
     * Gets the jump condition of the previous instruction, if it included one.
     * @return The jump condition of the previous instruction, or null if the previous instruction did not include a jump condition
     */
    public String getJump() {
        return jump;
    }

}
