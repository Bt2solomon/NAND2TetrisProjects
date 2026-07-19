import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Parses through a file and extracts valid tokens/terminal elements.
 * <p>Created as part of the "Build a Modern Computer from First Principles: Nand to Tetris Part II" course by Noam Nisan Shimon Schocken.
 * <p><a href="https://www.nand2tetris.org/">https://www.nand2tetris.org/</a>
 * <p><a href="https://www.coursera.org/learn/nand2tetris2">https://www.coursera.org/learn/nand2tetris2</a>
 * @author Brady Solomon
 */
public class JackTokenizer {

    // matches keyword tokens
    static String keywordPattern = "class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return";
    // matches symbol tokens
    static String symbolPattern = "[{}()\\[\\].,;+\\-*/&|<>=~]";
    // matches 0-32767
    static String intConstantPattern = "3276[0-7]|327[0-5][0-9]|32[0-6][0-9]{2}|3[0-1][0-9]{3}|[0-2][0-9]{4}|[0-9]{1,4}";
    // matches anything between two double quotes
    static String stringConstantPattern = "\"[^\"]*\"";
    // matches any sequence of alphanumeric characters and underscore, but not if the first character is a digit
    static String identifierPattern = "[a-zA-Z_][a-zA-Z0-9_]*";
    // matches any of the previous patterns, putting the match into different capture groups (1-5) based on which sub-pattern was matched
    static String tokenPattern = "(" + symbolPattern + ")|(" + intConstantPattern + ")|(" + stringConstantPattern + ")|(" + identifierPattern + ")";
    // matches any amount of comments or whitespace
    static String whitespacePattern = "(//.*|/\\*[\\V\\v]*?\\*/|\\s)*";

    String token;
    String tokenType;

    File f;
    Scanner sc;

    /**
     * Constructs a new JackTokenizer for the provided file. The JackTokenizer has methods to advance through the input file and get the type and value of tokens it parses.
     * @param infile The file to be parsed.
     * @throws FileNotFoundException Throws exception if the file cannot be found
     */
    public JackTokenizer(File infile) throws FileNotFoundException {
        f = infile;
        sc = new Scanner(f).useDelimiter(whitespacePattern);

    }

    /**
     * Checks if there are further tokens to parse.
     * @return True if there is more to be parsed in the file, false otherwise.
     */
    public boolean hasMoreTokens() {
        if (sc != null) {
            return sc.hasNext();
        }
        else return false;
    }

    /**
     * Advances through the file until the next token and its type are identified, or until the end of the file.
     */
    public void advance() {
        token = null;
        tokenType = null;
        while (token == null) {
            // skip comments and whitespace before checking for content
            sc.skip(whitespacePattern);
            // if the whitespace skip brought the scanner to EOF, exit method
            if (!sc.hasNext()) return;
            // attempt to find a match for the tokenPattern in the upcoming line. The output isn't saved because it's retrieved as a MatchResult in the try block
            sc.findInLine(tokenPattern);

            try {
                // stores the result of the regex search run by sc.findInLine() in a format that allows checking what each capture group matched. In the tokenPattern, each capture group corresponds to a different token type.
                // if findInLine didn't get a match, sc.match will throw an error, so this code is in a try block and the code for when no token is found is in the catch block
                MatchResult result = sc.match();
                token = null;
                tokenType = null;
                // group 1 matches symbols
                token = result.group(1);
                if (token != null) {
                    tokenType = "symbol";
                    return;
                }
                // group 2 matches integerConstants
                token = result.group(2);
                if (token != null) {
                    tokenType = "integerConstant";
                    return;
                }
                // group 3 matches stringConstants
                token = result.group(3);
                if (token != null) {
                    tokenType = "stringConstant";
                    return;
                }
                // group 4 matches identifiers
                token = result.group(4);
                if (token != null) {
                    if (Pattern.matches(keywordPattern, token)) {
                        tokenType = "keyword";
                        return;
                    }
                    tokenType = "identifier";
                    return;
                }
            }
            // this block is run when no token match was found
            catch (IllegalStateException e) {
                // if there is more to scan, move the scanner to the next line.
                if (sc.hasNextLine()) {
                    sc.nextLine();

                }
                else {
                    sc.close();
                    return;
                }
            }
        }

    }

    /**
     * Gets the type of the current token.
     * Types of token include: "KEYWORD", "SYMBOL", "IDENTIFIER", "INT_CONST", "STRING_CONST".
     * @return the type of the current token, or null if the token is none of the listed types.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Gets the current token. There are five types of token (identified by getTokenType()), each of which has a different definition. The token types and their definitions are as follows:
     * <ul>
     *     <li>
     *         KEYWORD:
     *         <ul><li>
     *             'class'|'constructor'|'function'|
     *             'method'|'field'|'static'|'var'|
     *             'int'|'char'|'boolean'|'void'|'true'|
     *             'false'|'null'|'this'|'let'|'do'|
     *             'if'|'else'|'while'|'return'
     *         </li></ul>
     *     </li>
     *     <li>
     *         SYMBOL:
     *         <ul><li>
     *             '{' | '}' | '(' | ')' | '[' | ']' | '.' |
     *             ',' | ';' | '+' | '-' | '*' | '/' | '&' |
     *             '|' | '<' | '>' | '=' | '~'
     *         </li></ul>
     *     </li>
     *     <li>
     *         INT_CONST:
     *         <ul><li>
     *             A decimal number in the range 0 .. 32767
     *         </li></ul>
     *     </li>
     *     <li>
     *         STRING_CONST:
     *         <ul><li>
     *             '"' A sequence of Unicode characters not including double quote or newline '"'
     *         </li></ul>
     *     </li>
     *     <li>
     *         IDENTIFIER:
     *         <ul><li>
     *              A sequence of letters, digits, and underscore ('_') not starting with a digit
     *         </li></ul>
     *     </li>
     * </ul>
     * @return A token matching one of the types listed above, or null if the token doesn't match any type.
     */
    public String getToken() {
        return token;
    }

    /**
     * Prints the entire file that the JackTokenizer is attached to.
     * This method is for debugging purposes only and should not be called outside of error handling.
     */
    public void printFile() throws FileNotFoundException {
        sc.close();
        sc = new Scanner(f);
        while (sc.hasNextLine()) {
            System.out.println(sc.nextLine());
        }
        sc.close();
    }
}
