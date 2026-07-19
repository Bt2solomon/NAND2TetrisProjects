import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Creates a FileWriter for an .asm file and provides methods for writing ASM code to it.
 * <ul>
 *     <li>{@link #CodeWriter(String, boolean)}: Constructs the CodeWriter by opening a .asm file with the given filename.
 *     <li>{@link #writeArithmetic(String)}: Writes ASM code capable of performing arithmetic operations to the file.
 *     <li>{@link #writePushPop(CommandType, String, int)}: Writes ASM code capable of performing push/pop operations to the file.
 *     <li>{@link #writeLabel(String)}: Writes an ASM label.
 *     <li>{@link #writeGoto(String)}: Writes ASM code that jumps to a given label.
 *     <li>{@link #writeIf(String)}: Writes ASM code that jumps to a given label if the last item on the stack equals 0.
 *     <li>{@link #writeFunction(String, int)}: Writes ASM code for function declarations.
 *     <li>{@link #writeCall(String, int)}: Writes ASM code for function calls.
 *     <li>{@link #writeReturn()}: Writes ASM code for function returns.
 *     <li>{@link #setFileName(String)}: Sets the name of the current file for symbol naming.
 *     <li>{@link #close()}: Closes the CodeWriter.
 * </ul>
 */
class CodeWriter {
    File file;
    FileWriter fileWriter;
    // used when creating symbols in the ASM code, like in push/pop static i operations
    String currentFileName;
    // set to functionName when writeFunction is called, to be used when creating labels in goto commands
    String currentFunction;
    // incrementer appended to the end of labels used in comparison arithmetic to prevent jumping to the wrong section of code
    int arithLabelCounter = 0;
    // incrementer appended to the end of labels used in function call and returns
    int returnLabelCounter = 0;

    /**
     * Class constructor that opens a FileWriter for a file with a given name.
     * @param fileName Name of the file to be opened. Should end with .asm suffix.
     * @throws IOException Throws exception if file cannot be opened.
     */
    public CodeWriter(String fileName, boolean includeBootstrap) throws IOException {
        // try to create FileWriter, catch exceptions
        try {
            file = new File(fileName);
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            throw new IOException(e);
        }
        // initialize currentFileName as Sys.vm for writeBootstrap. This should be overridden in main before each file is parsed.
        //setFileName("Sys.vm");

        // generate bootstrap code at the start of the file
        if (includeBootstrap) writeBootstrap();

    }

    /**
     * Writes ASM code capable of performing arithmetic operations to the file. Only writes code for valid arithmetic commands.
     * @param command Type of arithmetic operation to be translated. Valid commands include: add, sub, neg, eq, gt, lt, and, or, not
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeArithmetic(String command) throws IOException {
        // all generated code is appended to out, which is then written to file once at the end
        // add comment with VM command to ASM file for readability
        String out = "// " + command + "\n";

        // switch statement for all potential arithmetic commands
        /*
        Each command creates a String with the core arithmetic, operating on R14 as x and R15 as y and storing result in R14.
        That String is then passed to a private helper method (arithPopPush() or arithPopPopPush()).
        The helper method returns a String with one (or two) pop operations to R14 (and R15),
        then includes the operation,
        then pushes R14 back to the stack.
         */
        switch (command) {
            case "add":
                String add = "    // R14 = R14 + R15\n" +
                        "    @R15\n" +
                        "    D=M\n" +
                        "    @R14\n" +
                        "    M=D+M\n";
                out += arithPopPopPush(add);
                break;

            case "sub":
                String sub = "    // R14 = R14 - R15\n" +
                        "    @R15\n" +
                        "    D=M\n" +
                        "    @R14\n" +
                        "    M=M-D\n";
                out += arithPopPopPush(sub);
                break;

            case "neg":
                String neg = "    // R14 = -R14\n" +
                        "    @R14\n" +
                        "    M=-M\n";
                out += arithPopPush(neg);
                break;

            // cases "eq", "gt", and "lt" generate their core operation code in a helper method
            case "eq":
                String eq = arithCompare("JEQ");
                out += arithPopPopPush(eq);
                break;

            case "gt":
                String gt = arithCompare("JGT");
                out += arithPopPopPush(gt);
                break;

            case "lt":
                String lt = arithCompare("JLT");
                out += arithPopPopPush(lt);
                break;

            case "and":
                String and = "    // R14 = R14 && R15\n" +
                        "    @R15\n" +
                        "    D=M\n" +
                        "    @R14\n" +
                        "    M=D&M\n";
                out += arithPopPopPush(and);
                break;

            case "or":
                String or = "    // R14 = R14 || R15\n" +
                        "    @R15\n" +
                        "    D=M\n" +
                        "    @R14\n" +
                        "    M=D|M\n";
                out += arithPopPopPush(or);
                break;

            case "not":
                String not = "    // R14 = !R14\n" +
                        "    @R14\n" +
                        "    M=!M\n";
                out += arithPopPush(not);
                break;

            // if the command was none of the above, add an error comment to the ASM file only.
            default:
                out += "// ERROR: command could not be parsed. No code was generated for this command.\n";
        }

        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Writes ASM code capable of performing push/pop operations to the file. Only writes code for valid push/pop commands.
     * @param command CommandType C_PUSH or C_POP, determining which type of operation is to be written/
     * @param segment The memory segment to push from or pop to/
     * @param index The index of the memory segment to push from or pop to/
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writePushPop(CommandType command, String segment, int index) throws IOException {
        // all generated code is appended to out, which is then written to file once at the end
        String out = "";

        // handle push commands
        if (command == CommandType.C_PUSH) {
            // add comment with VM command to ASM file for readability
            out += String.format("// push %s %d\n", segment, index);
            switch (segment) {

                case "constant":
                    out += pushConst(index);
                    break;
                // local, argument, this, that, and temp share a helper method for generating their ASM code
                case "local":
                    out += pushLATTT("LCL", index);
                    break;

                case "argument":
                    out += pushLATTT("ARG", index);
                    break;

                case "this":
                    out += pushLATTT("THIS", index);
                    break;

                case "that":
                    out += pushLATTT("THAT", index);
                    break;
                // static and pointer share a helper method
                case "static":
                    out += pushAddr(currentFileName + "." + index);
                    break;

                case "temp":
                    out += pushLATTT("5", index);
                    break;

                case "pointer":
                    // ensure index is valid for pointer commands
                    if (index == 0 || index == 1) {
                        // pass in "THIS" if index is 0, "THAT" otherwise
                        out += pushAddr((index == 0) ? "THIS" : "THAT");
                    }
                    else {
                        // if the index is out of bounds, add an error comment to the ASM file only.
                        out += "// ERROR: 'push pointer' can only have a value of 0 or 1. No code was generated for this command.\n";
                    }
                    break;

                default:
                    // if the command was none of the above, add an error comment to the ASM file only
                    out += "// ERROR: command could not be parsed. No code was generated for this command.\n";
                    break;
            }
        }

        // handle pop commands
        else if (command == CommandType.C_POP) {
            // add comment with VM command to ASM file for readability
            out += String.format("// pop %s %d\n", segment, index);
            switch (segment) {
                // there is no "pop constant i" command so just add an error comment to the ASM file
                case "constant":
                    out += "// ERROR: 'pop constant' is not a valid command. No code was generated for this command.\n";
                    break;
                // local, argument, this, that, and temp share a helper method for generating their ASM code
                case "local":
                    out += popLATTT("LCL", index);
                    break;

                case "argument":
                    out += popLATTT("ARG", index);
                    break;

                case "this":
                    out += popLATTT("THIS", index);
                    break;

                case "that":
                    out += popLATTT("THAT", index);
                    break;
                // static and pointer share a helper method
                case "static":
                    out += popAddr(currentFileName + "." + index);
                    break;

                case "temp":
                    out += popLATTT("5", index);
                    break;

                case "pointer":
                    // ensure index is valid for pointer commands
                    if (index == 0 || index == 1) {
                        // pass in "THIS" if index is 0, "THAT" otherwise
                        out += popAddr((index == 0) ? "THIS" : "THAT");
                    }
                    else {
                        // if the index is out of bounds, add an error comment to the ASM file only.
                        out += "// ERROR: 'pop pointer' can only have a value of 0 or 1. No code was generated for this command.\n";
                    }
                    break;

                default:
                    // if the command was none of the above, add an error comment to the ASM file only
                    out += "// ERROR: command could not be parsed. No code was generated for this command.\n";
                    break;
            }


        }
        else {
            // if the command was neither C_PUSH nor C_POP, add an error comment to the ASM file only
            out += "// ERROR: Invalid command type '" + command + "'. No code was generated for this statement.\n";
        }

        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    /**
     * Writes ASM code that sets a label.
     * @param label Name of the label to be set.
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeLabel(String label) throws IOException {
        String modifiedLabel = String.format("%s.%s$%s", currentFileName, currentFunction, label);
        String out = "// label " + modifiedLabel + "\n";
        out += "(" + modifiedLabel + ")\n";

        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    /**
     * Writes ASM code that jumps to a label.
     * @param label Name of the label to go to.
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeGoto(String label) throws IOException {
        String modifiedLabel = String.format("%s.%s$%s", currentFileName, currentFunction, label);
        String out = gotoLabel(modifiedLabel);
        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    /**
     * Writes ASM code that jumps to a label if the last value on the stack is 'true'.
     * @param label Name of the label to go to.
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeIf(String label) throws IOException {
        String modifiedLabel = String.format("%s.%s$%s", currentFileName, currentFunction, label);
        String out = "// if-goto " + modifiedLabel + "\n";
        out += "    // pop R14\n" + popAddr("R14");
        out += String.format("    @R14\n" +
                "    D=M\n" +
                "    @%s\n" +
                "    D;JNE\n", modifiedLabel);

        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    /**
     * Writes ASM code capable of handling function declarations.
     * @param functionName Name of the function being declared.
     * @param nVars Number of local registers to initialize.
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeFunction(String functionName, int nVars) throws IOException {
        // save functionName for naming labels
        currentFunction = functionName;
        // using a StringBuilder because it's more efficient when appending in a loop
        StringBuilder out = new StringBuilder(String.format("// function %s %d\n", functionName, nVars));
        out.append(String.format("(%s)\n" +
                "    // push 0 %d times\n", functionName, nVars));
        for (int i = 0; i < nVars; i++) {
            out.append(pushConst(0));
        }

        // try-catch writing to the file
        try {
            fileWriter.write(out.toString());
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    /**
     * Writes ASM code capable of handling function calls.
     * @param functionName Name of the function being called.
     * @param nArgs Number of arguments used by the function.
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeCall(String functionName, int nArgs) throws IOException {
        String retAddr = String.format("%s.%s$ret.%d", currentFileName, functionName, returnLabelCounter);
        String out = String.format("// call %s %d\n", functionName, nArgs);
        out += String.format("    // R14 = %1$s\n" +
                "    @%1$s\n" +
                "    D=A\n" +
                "    @R14\n" +
                "    M=D\n" +
                "    // push R14\n" +
                "%2$s\n" +
                "    // push LCL\n" +
                "%3$s\n" +
                "    // push ARG\n" +
                "%4$s\n" +
                "    // push THIS\n" +
                "%5$s\n" +
                "    // push THAT\n" +
                "%6$s\n" +
                "    // ARG = SP - (5 + %7$d)\n" +
                "    @SP\n" +
                "    D=M\n" +
                "    // 5 + %7$d precalculated\n" +
                "    @%8$d\n" +
                "    D=D-A\n" +
                "    @ARG\n" +
                "    M=D\n" +
                "    // LCL = SP\n" +
                "    @SP\n" +
                "    D=M\n" +
                "    @LCL\n" +
                "    M=D\n" +
                "%9$s\n" +
                "    // return label\n" +
                "(%1$s)\n", retAddr, pushAddr("R14"), pushAddr("LCL"), pushAddr("ARG"), pushAddr("THIS"), pushAddr("THAT"), nArgs, 5 + nArgs, gotoLabel(functionName));


        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }
        returnLabelCounter++;

    }

    /**
     * Writes ASM code that returns from a function.
     * @throws IOException Throws exception if the file cannot be written to.
     */
    public void writeReturn() throws IOException {
        String out = "// return\n";
        out += String.format("    // endFrame (R14) = LCL\n" +
                "    @LCL\n" +
                "    D=M\n" +
                "    @R14 // endFrame\n" +
                "    M=D\n" +
                "    // R15 (retAddr) = RAM[R14 - 5]\n" +
                "    @5\n" +
                "    A=D-A // R14 - 5\n" +
                "    D=M // D = RAM[R14 - 5]\n" +
                "    @R15 // retAddr\n" +
                "    M=D\n" +
                "    // R13 = pop()\n" +
                "%s\n" +
                "    // RAM[ARG] = R13\n" +
                "    @R13\n" +
                "    D=M\n" +
                "    @ARG\n" +
                "    A=M\n" +
                "    M=D\n" +
                "    // SP = ARG + 1\n" +
                "    @ARG\n" +
                "    D=M\n" +
                "    @SP\n" +
                "    M=D+1\n" +
                "    // THAT = RAM[R14--]\n" +
                "    @R14 // endFrame\n" +
                "    MD=M-1\n" +
                "    A=D\n" +
                "    D=M\n" +
                "    @THAT\n" +
                "    M=D\n" +
                "    // THIS = RAM[R14--]\n" +
                "    @R14 // endFrame\n" +
                "    MD=M-1\n" +
                "    A=D\n" +
                "    D=M\n" +
                "    @THIS\n" +
                "    M=D\n" +
                "    // ARG = RAM[R14--]\n" +
                "    @R14 // endFrame\n" +
                "    MD=M-1\n" +
                "    A=D\n" +
                "    D=M\n" +
                "    @ARG\n" +
                "    M=D\n" +
                "    // LCL = RAM[R14--]\n" +
                "    @R14 // endFrame\n" +
                "    MD=M-1\n" +
                "    A=D\n" +
                "    D=M\n" +
                "    @LCL\n" +
                "    M=D\n" +
                "    // goto retAddr (R15)\n" +
                "    @R15 // retAddr\n" +
                "    A=M\n" +
                "    0;JMP\n", popAddr("R13"));



        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets the name of the .vm file being parsed, to be used when generating labels. Should be called when a new file starts being parsed.
     * @param vmFileName The name of the .vm file being parsed.
     */
    public void setFileName(String vmFileName) {
        // check that input is valid
        if (vmFileName != null && vmFileName.matches(".+\\.vm")) {
            // get the index of the last separator (or -1 if there are none)
            int lastIndexOfSeparator = vmFileName.lastIndexOf(File.separator);
            //set inFileNameWithoutSuffix to substring of input between last separator and ".vm"
            currentFileName = vmFileName.substring(lastIndexOfSeparator + 1, vmFileName.length() - 3);
        }
        // only print an error if no name can be extracted
        else {
            System.out.println("ERROR: setFileName input " + vmFileName + " is not a .vm file. File name not set.");
        }
    }

    /**
     * Closes the FileWriter.
     * @throws IOException Throws exception if the file cannot be closed.
     */
    public void close() throws IOException {
        try {
            fileWriter.close();
        } catch (IOException e) {
            throw new IOException(e);
        }
    }





    // Helper methods and strings

    /**
     * Writes bootstrap code, initializing SP and calling Sys.init
     * @throws IOException Throws exception if the file cannot be written to.
     */
    private void writeBootstrap() throws IOException {
        String out = "// Bootstrap code\n" +
                "    // SP = 256\n" +
                "    @256\n" +
                "    D=A\n" +
                "    @SP\n" +
                "    M=D\n";

        // try-catch writing to the file
        try {
            fileWriter.write(out);
        } catch (IOException e) {
            throw new IOException(e);
        }
        writeCall("Sys.init", 0);
        fileWriter.write("($$ENDFILE)\n    @$$ENDFILE\n    0;JMP\n");
    }

    /**
     * Generates ASM code for incrementing or decrementing the value stored in the memory at a given address.
     * @param var Symbolic or numeric address of the value to increment/decrement.
     * @param increment Generates incrementing code if true, decrementing if false.
     * @return String containing the generated ASM code
     */
    private String incdec(String var, boolean increment) {
        if (increment) {
            return String.format("    // %s++\n" +
                    "    @%<s\n" +
                    "    M=M+1\n", var);
        }
        else {
            return String.format("    // %s--\n" +
                    "    @%<s\n" +
                    "    AM=M-1\n", var);
        }
    }

    /**
     * Generates ASM code to push from local, arguments, this, that, and temp segments.
     * @param segmentPointer Symbolic address of the segment pointer or numeric value of the pointer itself.
     * @param i Index of segment to be accessed.
     * @return String containing the generated ASM code
     */
    private String pushLATTT(String segmentPointer, int i) {
        // default to "D=M" for symbolic commands to get the value stored at the segmentPointer
        String firstDdeclaration = "D=M";
        if (Pattern.matches("\\d+", segmentPointer)) {
            // if segmentPointer is numeric, use D=A to get its value directly
            firstDdeclaration = "D=A";
        }
        return String.format("    // D = RAM[%1$s + %2$d]\n" +
                "    @%1$s\n" +
                "    %5$s\n" +
                "    @%2$d\n" +
                "    A=D+A\n" +
                "    D=M\n" +
                "%3$s\n" +
                "%4$s\n", segmentPointer, i, RAMaddreqD("SP"), incdec("SP", true), firstDdeclaration);
    }

    /**
     * Generates ASM code to push from a given address.
     * Use for segments that do not use offset or other uses that require pushing directly from an address.
     * @param addr Address of the value to be pushed.
     * @return String containing the generated ASM code
     */
    private String pushAddr(String addr) {
        return String.format("    // D = %s\n" +
                "    @%<s\n" +
                "    D=M\n" +
                "%s\n" +
                "%s\n", addr, RAMaddreqD("SP"), incdec("SP", true));
    }
    /**
     * Generates ASM code to push a given constant.
     * Use for segments that require pushing a constant directly to stack.
     * @param value Value to be pushed.
     * @return String containing the generated ASM code
     */
    private String pushConst(int value) {
        return String.format("    // D = %d\n" +
                "    @%<d\n" +
                "    D=A\n" +
                "%s\n" +
                "%s\n", value, RAMaddreqD("SP"), incdec("SP", true));

    }

    /**
     * Generates ASM code to pop to local, arguments, this, that, and temp segments.
     * @param segmentPointer Symbolic address of the segment pointer or numeric value of the pointer itself.
     * @param i Index of segment to be accessed.
     * @return String containing the generated ASM code
     */
    private String popLATTT(String segmentPointer, int i) {
        // default to "D=M" for symbolic commands to get the value stored at the segmentPointer
        String firstDdeclaration = "D=M";
        if (Pattern.matches("\\d+", segmentPointer)) {
            // if segmentPointer is numeric, use D=A to get its value directly
            firstDdeclaration = "D=A";
        }
        return String.format("    // R13 = RAM[%1$s + %2$d]\n" +
                "    @%1$s\n" +
                "    %5$s\n" +
                "    @%2$d\n" +
                "    D=D+A\n" +
                "    @R13\n" +
                "    M=D\n" +
                "%3$s\n" +
                "    //D = RAM[SP]\n" +
                "    D=M\n" +
                "%4$s\n", segmentPointer, i, incdec("SP", false), RAMaddreqD("R13"), firstDdeclaration);
    }

    /**
     * Generates ASM code to pop to a given address.
     * Use for segments that do not use offset or other uses that require popping directly to an address.
     * @param addr Address of the value to be pushed.
     * @return String containing the generated ASM code
     */
    private String popAddr(String addr) {
        return String.format("    // R13 = %s\n" +
                "    @%<s\n" +
                "    D=A\n" +
                "    @R13\n" +
                "    M=D\n" +
                "%s\n" +
                "    // D = RAM[SP]\n" +
                "    D=M\n" +
                "%s\n", addr, incdec("SP", false), RAMaddreqD("R13"));
    }

    /**
     * Generates ASM code equivalent to RAM[addr] = D.
     * @param addr Address of the value to be set.
     * @return String containing the generated ASM code
     */
    private String RAMaddreqD(String addr) {
        return String.format("    // RAM[%s] = D\n" +
                "    @%<s\n" +
                "    A=M\n" +
                "    M=D\n", addr);
    }

    /**
     * Generates ASM code that pops to R15 and then R14, performs the code provided in arithASM, then pushes from R14.
     * @param arithASM ASM code to be included between popping and pushing.
     * @return String containing the generated ASM code
     */
    private String arithPopPopPush(String arithASM) {
        return "    // pop y to R15\n" +
                popAddr("R15") +
                "    // pop x to R14\n" +
                popAddr("R14") +
                arithASM +
                pushAddr("R14");
    }

    /**
     * Generates ASM code that pops to R14, performs the code provided in arithASM, then pushes from R14.
     * @param arithASM ASM code to be included between popping and pushing.
     * @return String containing the generated ASM code
     */
    private String arithPopPush(String arithASM) {
        return "    // pop x to R14\n" +
                popAddr("R14") +
                arithASM +
                pushAddr("R14");
    }

    /**
     * Generates ASM code for comparison operations on R14 and R15 with the provided jump condition, storing the binary result in R14.
     * @param condition ASM Jump condition to be used when comparing R14 - R15 to 0.
     * @return String containing the generated ASM code
     */
    private String arithCompare(String condition) {
        String out = String.format("    // D = R14 - R15\n" +
                "    @R15\n" +
                "    D=M\n" +
                "    @R14\n" +
                "    D=M-D\n" +
                "    // if D = 0 jump to $$IFTRUE.%d\n" +
                "    @$$IFTRUE.%<d\n" +
                "    D;%s\n" +
                "    // R14 = 0 if not true\n" +
                "    @R14\n" +
                "    M=0\n" +
                "    // jump past TRUE section\n" +
                "    @$$ENDIF.%1$d\n" +
                "    0;JMP\n" +
                "    // R14 = -1 if true\n" +
                "($$IFTRUE.%<d)\n" +
                "    @R14\n" +
                "    M=-1\n" +
                "($$ENDIF.%<d)\n", arithLabelCounter, condition);
        arithLabelCounter++;
        return out;
    }

    /**
     * Generates ASM code that jumps to a label.
     * @param label Name of the label to go to.
     * @return String containing the generated ASM code.
     */
    private String gotoLabel(String label) {
        String out = "// goto " + label + "\n";
        out += String.format("    @%s\n" +
                "    0;JMP\n", label);
        return out;
    }

}