import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

    FileWriter outfile;


    /**
     * Constructs a new VMWriter that will write VM language code to a given .vm file.
     * @param vmFile The file to be written to
     * @throws IOException Throws exception if the given output file cannot be written to
     */
    public VMWriter(File vmFile) throws IOException {
        outfile = new FileWriter(vmFile);
    }

    /**
     * Writes a push command to the .vm file.
     * @param segment The segment to be pushed to. Should be "const", "arg", "local", "this", "that", "pointer", or "temp"
     * @param index The index of the segment to be pushed to
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writePush(String segment, int index) throws IOException {
        outfile.write(String.format("    push %s %d\n", segment.toLowerCase(), index));
    }

    /**
     * Writes a pop command to the .vm file.
     * @param segment The segment to be popped to. Should be "arg", "local", "this", "that", "pointer", or "temp"
     * @param index The index of the segment to be popped to
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writePop(String segment, int index) throws IOException {
        outfile.write(String.format("    pop %s %d\n", segment.toLowerCase(), index));

    }

    /**
     * Writes an arithmetic or logical command to the .vm file.
     * @param command The arithmetic command to write. Should be "add", "sub", "neg", "eq", "gt", "lt", "and", "or", or "not"
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeArithmetic(String command) throws IOException {
        outfile.write(String.format("    %s\n", command.toLowerCase()));
    }

    /**
     * Writes a label declaration to the .vm file.
     * @param label Name of the label to declare
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeLabel(String label) throws IOException {
        outfile.write(String.format("label %s\n", label));
    }

    /**
     * Writes an unconditional branch (goto) command to the .vm file.
     * @param label Name of the label to branch to
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeGoto(String label) throws IOException {
        outfile.write(String.format("    goto %s\n", label));
    }

    /**
     * Writes a conditional branch (if-goto) command to the .vm file.
     * @param label Name of the label to branch to
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeIf(String label) throws IOException {
        outfile.write(String.format("    if-goto %s\n", label));
    }

    /**
     * Writes a function invocation command to the .vm file.
     * @param name Name of the function to invoke
     * @param nArgs Number of arguments the function has
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeCall(String name, int nArgs) throws IOException {
        outfile.write(String.format("    call %s %d\n", name, nArgs));
    }

    /**
     * Writes a function declaration to the .vm file.
     * @param name Name of the function to declare
     * @param nLocals Number of local variables the function will have
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeFunction(String name, int nLocals) throws IOException {
        outfile.write(String.format("function %s %d\n", name, nLocals));
    }

    /**
     * Writes a return command to the .vm file.
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeReturn() throws IOException {
        outfile.write("    return\n");
    }

    /**
     * Writes a comment to the .vm file.
     * @param comment The comment to write
     * @throws IOException Throws exception if the .vm file cannot be written to
     */
    public void writeComment(String comment) throws IOException {
        outfile.write(String.format("// %s\n", comment));
    }

    /**
     * Closes the VMWriter. This should always be called when finished with the VMWriter.
     * @throws IOException Throws exception if the VMWriter cannot be closed.
     */
    public void close() throws IOException {
        outfile.close();
    }
}
