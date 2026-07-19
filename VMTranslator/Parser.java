import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Provides methods necessary for parsing a .vm file and extracting information about its commands.
 * <ul>
 *     <li>{@link #Parser(String)}: Constructor that opens the file and scanner.
 *     <li>{@link #hasMoreLines()}: returns true if there is another line to be parsed.
 *     <li>{@link #advance()}: Parses the next line.
 *     <li>{@link #commandType()}: returns the commandType of the line parsed with advance()
 *     <li>{@link #arg1()}: returns the first argument of the command parsed by advance(), if any.
 *     <li>{@link #arg2()}: returns the second argument of the command parsed by advance(), if any.
 * </ul>
 */
class Parser {

    File vmFile;
    Scanner sc;
    // properties of each command; these are set in advance() and retrieved from the class in their eponymous getter methods
    private CommandType commandType = null;
    private String arg1 = null;
    private String arg2 = null;

    /**
     * Class constructor that takes the name of the .vm file to be parsed.
     * @param vmFileName Name of the file to be opened and parsed. The file should have the .vm suffix.
     * @throws FileNotFoundException Throws exception if the file cannot be opened.
     */
    public Parser(String vmFileName) throws FileNotFoundException {
        // tries to open file and scanner, throws FileNotFoundException if it can't
        try {
            vmFile = new File(vmFileName);
            sc = new Scanner(vmFile);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException();
        }
    }

    /**
     * Returns true if there is another line in the file for the Parser to parse and false if not.
     * @return true if there is another line that can be parsed, false if not.
     */
    public boolean hasMoreLines() {
        try {
            boolean hasNextLine = sc.hasNextLine();
            if (!hasNextLine) sc.close();
            return hasNextLine;
        }
        catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Parses the next line of the file, making it the 'current command' if it is a valid command. Should not be run if {@link #hasMoreLines()} has returned false since last calling the constructor.
     */
    public void advance() {

        // search for match to a valid command with some nasty regex (described in comments at the end of each line)
        String cmd = sc.findInLine(
                "(?m)^\\h*" +                            // (?m) enables multiline; matches start of token followed by any amount of horizontal whitespace
                        "(add|sub|neg|eq|gt|lt|and|or|not" +    // THEN '(' starts capture group 1; matches "add" OR "sub" OR ...
                        "|push\\h+\\S+\\h+\\d+" +               // OR matches "push", then some amount of h-whitespace, then some amount of non-whitespace, then some h-whitespace, then some digits
                        "|pop\\h+\\S+\\h+\\d+" +                // OR matches the above but with "pop" instead of "push"
                        "|function\\h+\\S+\\h+\\d+" +           // OR matches the above but with "function"
                        "|call\\h+\\S+\\h+\\d+" +               // OR matches the above but with "call"
                        "|label\\h+\\S+" +                      // OR matches "label", then some amount of h-whitespace, then some amount of non-whitespace
                        "|goto\\h+\\S+" +                       // OR matches the above but with "goto" instead of "label"
                        "|if-goto\\h+\\S+" +                    // OR matches the above but with "if-goto"
                        "|return)" +                            // OR matches "return"; ')' ends capture group 1
                        "\\h*(?=//|$)");                        // THEN matches any amount of h-whitespace until '//' or end of token

        // if no command was matched, set all properties to null, move scanner to start of next line, then end method early
        if (cmd == null) {
            commandType = null;
            arg1 = null;
            arg2 = null;
            if (sc.hasNextLine()) sc.nextLine();
            return;
        }

        // split command into individual words
        String[] args = cmd.split("\\h+", 0);
        // whitespace at the start of string will cause the first arg to be an empty string, so shift everything up an index:
        if (args[0].isEmpty()) {
            for (int i = 0; i < args.length - 1; i++) {
                args[i] = args[i+1];
            }
        }

        // set properties based on command words; unused properties are set to null
        switch (args[0]) {
            case "add":
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                commandType = CommandType.C_ARITHMETIC;
                arg1 = args[0];
                arg2 = null;
                break;
            case "push":
                commandType = CommandType.C_PUSH;
                arg1 = args[1];
                arg2 = args[2];
                break;
            case "pop":
                commandType = CommandType.C_POP;
                arg1 = args[1];
                arg2 = args[2];
                break;

            case "label":
                commandType = CommandType.C_LABEL;
                arg1 = args[1];
                arg2 = null;
                break;

            case "goto":
                commandType = CommandType.C_GOTO;
                arg1 = args[1];
                arg2 = null;
                break;

            case "if-goto":
                commandType = CommandType.C_IF;
                arg1 = args[1];
                arg2 = null;
                break;

            case "function":
                commandType = CommandType.C_FUNCTION;
                arg1 = args[1];
                arg2 = args[2];
                break;

            case "call":
                commandType = CommandType.C_CALL;
                arg1 = args[1];
                arg2 = args[2];
                break;

            case "return":
                commandType = CommandType.C_RETURN;
                arg1 = null;
                arg2 = null;
                break;

            default:
                commandType = null;
                arg1 = null;
                arg2 = null;
        }
        // move scanner to start of next line
        if (sc.hasNext()) sc.nextLine();
    }

    /**
     * Gets the type of command in the current command set by advance()
     * @return the {@link CommandType} matching the command type of the current command, or null if the last line had no valid command.
     */
    public CommandType commandType() {
        return commandType;
    }

    /**
     * Gets the first argument in the current command set by advance()
     * @return the first argument of the current command, or the command itself if the current CommandType is C_ARITHMETIC. Otherwise, returns null.
     */
    public String arg1() {
        return arg1;
    }

    /**
     * Gets the second argument in the current command set by advance()
     * @return the second argument of the current command if the current CommandType is C_PUSH, C_POP, C_FUNCTION, or C_CALL. Otherwise, returns null.
     */
    public String arg2() {
        return arg2;
    }

}
