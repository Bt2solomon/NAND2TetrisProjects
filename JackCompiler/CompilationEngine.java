import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * The CompilationEngine parses through a file with a JackTokenizer, interprets its grammar, converts its contents to VM language, and writes the result to a .vm file.
 * <p>Created as part of the "Build a Modern Computer from First Principles: Nand to Tetris Part II" course by Noam Nisan Shimon Schocken.
 * <p><a href="https://www.nand2tetris.org/">https://www.nand2tetris.org/</a>
 * <p><a href="https://www.coursera.org/learn/nand2tetris2">https://www.coursera.org/learn/nand2tetris2</a>
 * @author Brady Solomon
 */
public class CompilationEngine {

    JackTokenizer jt;
    SymbolTable st;
    VMWriter vmw;

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
    static String tokenPattern = "(" + keywordPattern + ")|(" + symbolPattern + ")|(" + intConstantPattern + ")|(" + stringConstantPattern + ")|(" + identifierPattern + ")";
    // matches any amount of comments or whitespace
    static String whitespacePattern = "(//.*|/\\*.*\\*/|\\s)*";

    // stores the name of the current class, to be used when creating VM code
    String className;
    // used and then incremented when generating unique labels
    int labelCounter;


    /**
     * Constructs a new CompilationEngine and advances the JackTokenizer to the first token.
     * @param tokenizer JackTokenizer that is parsing the input file
     * @param vmFile Desired output file
     * @throws IOException Throws exception if the given output file cannot be written to
     */
    public CompilationEngine(JackTokenizer tokenizer, File vmFile) throws IOException {
        jt = tokenizer;
        vmw = new VMWriter(vmFile);
        if (jt.hasMoreTokens()) jt.advance();
        labelCounter = 0;

    }

    /**
     * Compiles a Jack class into VM code and writes it to the CompilationEngine's output file.
     * This should be the first method called after constructing the CompilationEngine.
     * Uses the grammar:
     * <p>
     *     class: 'class' className '{' classVarDec* subroutineDec* '}'
     * </p>
     * <p>
     *     className: identifier
     * </p>
     */
    public void compileClass() throws Exception {

        // create new SymbolTable per class and reset labelCounter
        st = new SymbolTable();
        labelCounter = 0;

        eat("class"); // 'class'
        // set global var className for method naming later
        className = eat(identifierPattern); // className
        vmw.writeComment("Start class " + className);

        eat("\\{"); // '{'
        // check that the next token matches the start of a classVarDec
        while (Pattern.matches("static|field", jt.getToken())) { // classVarDec*
            compileClassVarDec();
        }
        while (Pattern.matches("constructor|function|method", jt.getToken())) { // subroutineDec*
            compileSubroutine();
        }
        eat("\\}"); // '}'
    }

    /**
     * Compiles a class variable declaration into VM code.
     * Uses the grammar:
     * <p>
     *     classVarDec: ('static'|'field') type varName (',' varName)* ';'
     * </p>
     * <p>
     *     type: 'int'|'char'|'boolean'| className
     * </p>
     * <p>
     *     className: identifier
     * </p>
     * <p>
     *     varName: identifier
     * </p>
     */
    public void compileClassVarDec() throws Exception {
        String kind = eat("static|field"); // ('static'|'field')
        String type = eat("int|char|boolean|" + identifierPattern); // type
        String name = eat(identifierPattern); // varName
        // add variable to SymbolTable after gathering all relevant info
        st.define(name, type, kind);
        vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", name, st.typeOf(name), st.kindOf(name), st.indexOf(name)));
        while (Pattern.matches(",", jt.getToken())) { // (',' varName)*
            eat(","); // ','
            name = eat(identifierPattern); // varName
            // add additional class variables to SymbolTable
            st.define(name, type, kind);
            vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", name, st.typeOf(name), st.kindOf(name), st.indexOf(name)));
        }
        eat(";");
    }

    /**
     * Compiles a subroutine into VM code and writes it to the CompilationEngine's output file.
     * This includes the subroutine declaration, name, and body.
     * Uses the grammar:
     * <p>
     *     subroutineDec: ('constructor'|'function'|'method')
     *     ('void'| type) subroutineName '(' parameterList ')'
     *     subroutineBody
     * </p>
     * <p>
     *     subroutineName: identifier
     * </p>
     * <p>
     *     subroutineBody: '{' varDec* statements '}'
     * </p>
     * <p>
     *     type: 'int'|'char'|'boolean'| className
     * </p>
     * <p>
     *     className: identifier
     * </p>
     */
    public void compileSubroutine() throws Exception {

        // start a new subroutine segment in the SymbolTable
        st.startSubroutine();

        String subroutineType = eat("constructor|function|method"); // ('constructor'|'function'|'method')
        String type = eat("void|int|char|boolean|" + identifierPattern); // ('void'| type)

        // if subroutine is a method, add 'this' as first arg
        if (subroutineType.equals("method")) {
            st.define("this", type, "arg");
            vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", "this", st.typeOf("this"), st.kindOf("this"), st.indexOf("this")));
        }

        String subroutineName = eat(identifierPattern); // subroutineName

        vmw.writeComment(String.format("Start subroutine %s %s %s", subroutineType, type, subroutineName));
        eat("\\("); // '('
        compileParameterList();
        eat("\\)"); // ')'
        eat("\\{"); // '{'
        while (Pattern.matches("var", jt.getToken())) { // varDec*
            compileVarDec();
        }

        vmw.writeFunction(className + "." + subroutineName, st.varCount("var"));
        if (subroutineType.equals("method")) {
            vmw.writePush("argument", 0);
            vmw.writePop("pointer", 0);
        }
        else if (subroutineType.equals("constructor")) {
            vmw.writePush("constant", st.varCount("field"));
            vmw.writeCall("Memory.alloc", 1);
            vmw.writePop("pointer", 0);
        }

        compileStatements(); // statements

        eat("\\}"); // '}'
    }

    /**
     * Compiles a parameter list into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     parameterList: ((type varName) (',' type varName)*)?
     * </p>
     * <p>
     *     type: 'int'|'char'|'boolean'| className
     * </p>
     * <p>
     *     className: identifier
     * </p>
     * <p>
     *     varName: identifier
     * </p>
     */
    public void compileParameterList() throws Exception {
        if (Pattern.matches("int|char|boolean|" + identifierPattern, jt.getToken())) { // ((type varName) (',' type varName)*)?
            String type = eat("int|char|boolean|" + identifierPattern); // type
            String name = eat(identifierPattern); // varName
            // add first arg to SymbolTable
            st.define(name, type, "arg");
            vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", name, st.typeOf(name), st.kindOf(name), st.indexOf(name)));

            while (Pattern.matches(",", jt.getToken())) {
                eat(","); // ','
                type = eat("int|char|boolean|" + identifierPattern); // type
                name = eat(identifierPattern); // varName
                // add additional args to SymbolTable
                st.define(name, type, "arg");
                vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", name, st.typeOf(name), st.kindOf(name), st.indexOf(name)));
            }
        }
    }

    /**
     * Compiles a variable declaration into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     varDec: 'var' type varName (',' varName)* ';'
     * </p>
     * <p>
     *     type: 'int'|'char'|'boolean'| className
     * </p>
     * <p>
     *     className: identifier
     * </p>
     * <p>
     *     varName: identifier
     * </p>
     */
    public void compileVarDec() throws Exception {
        String kind = eat("var"); // 'var'
        String type = eat("int|char|boolean|" + identifierPattern); // type
        String name = eat(identifierPattern); // varName
        // add first var
        st.define(name, type, kind);
        vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", name, st.typeOf(name), st.kindOf(name), st.indexOf(name)));

        while (Pattern.matches(",", jt.getToken())) { // (',' varName)*
            eat(","); // ','
            name = eat(identifierPattern); // varName
            // add more vars
            st.define(name, type, kind);
            vmw.writeComment(String.format("Added %s (%s %s %d) to SymbolTable", name, st.typeOf(name), st.kindOf(name), st.indexOf(name)));
        }
        eat(";"); // ';'
    }

    /**
     * Compiles a series of statements (or nothing) into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     statements: statement*
     * </p>
     * <p>
     *     statement: letStatement | ifStatement | whileStatement |
     *     doStatement | returnStatement
     * </p>
     */
    public void compileStatements() throws Exception {
        while (Pattern.matches("let|if|while|do|return", jt.getToken())) {
            if (jt.getToken().equals("let")) {
                compileLet();
            }
            if (jt.getToken().equals("if")) {
                compileIf();
            }
            if (jt.getToken().equals("while")) {
                compileWhile();
            }
            if (jt.getToken().equals("do")) {
                compileDo();
            }
            if (jt.getToken().equals("return")) {
                compileReturn();
            }
        }
    }

    /**
     * Compiles a do statement into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     doStatement: 'do' subroutineCall ';'
     * </p>
     * <p>
     *     subroutineCall: subroutineName '(' expressionList ')' | (className |
     *     varName) '.' subroutineName '(' expressionList ')'
     * </p>
     * <p>
     *     subroutineName: identifier
     * </p>
     * <p>
     *     className: identifier
     * </p>
     * <p>
     *     varName: identifier
     * </p>
     */
    public void compileDo() throws Exception {
        eat("do"); // 'do'
//        String name = eat(identifierPattern); // subroutineName | className | varName
//        if (Pattern.matches("\\(", jt.getToken())) { // previous token was subroutineName
//            eat("\\("); // '('
            compileExpressionList();
//            eat("\\)"); // ')'
//
//        }
//        else {
//            eat("."); // '.'
//            eat(identifierPattern); // subroutineName
//            eat("\\("); // '('
//            compileExpressionList();
//            eat("\\)"); // ')'
//        }

        vmw.writeComment("throw away return value");
        vmw.writePop("temp", 0);

        eat(";"); // ';'

    }

    /**
     * Compiles a let statement into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
     * </p>
     * <p>
     *     varName: identifier
     * </p>
     */
    public void compileLet() throws Exception {
        boolean assigningArrayVal = false;
        eat("let"); // 'let'
        String name = eat(identifierPattern); // varName
        if (jt.getToken().equals("[")) {
            assigningArrayVal = true;
            eat("\\["); // '['
            vmw.writePush(st.segmentOf(name), st.indexOf(name));
            compileExpression();
            vmw.writeArithmetic("add");
            eat("\\]"); // ']'
        }
        eat("="); // '='
        compileExpression();
        if (assigningArrayVal) {
//            vmw.writePop("pointer", 1);
//            vmw.writePush("that", 0);
            vmw.writePop("temp", 0);
            vmw.writePop("pointer", 1);
            vmw.writePush("temp", 0);
            vmw.writePop("that", 0);
        }
        else {
            vmw.writePop(st.segmentOf(name), st.indexOf(name));
        }

        eat(";"); // ';'
    }

    /**
     * Compiles a while statement into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     whileStatement: 'while''(' expression ')''{' statements '}'
     * </p>
     */
    public void compileWhile() throws Exception {

        // generate two unique label names
        String label1 = "L" + labelCounter;
        labelCounter++;
        String label2 = "L" + labelCounter;
        labelCounter++;

        eat("while"); // 'while'
        vmw.writeLabel(label1);

        eat("\\("); // '('
        compileExpression();
        eat("\\)"); // ')'

        vmw.writeArithmetic("not");
        vmw.writeIf(label2);

        eat("\\{"); // '{'
        compileStatements();
        eat("\\}"); // '}'

        vmw.writeGoto(label1);
        vmw.writeLabel(label2);
    }

    /**
     * Compiles a return statement into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     returnStatement: 'return' expression? ';'
     * </p>
     */
    public void compileReturn() throws Exception {
        eat("return"); // 'return'
        if (Pattern.matches(intConstantPattern + "|" + stringConstantPattern + "|" + keywordPattern + "|" + identifierPattern + "|\\(|-|~", jt.getToken())) { // matches start of term, which is start of expression
            compileExpression();
        }
        // if not returning an expression, return dummy value
        else {
            vmw.writePush("constant", 0);
        }
        vmw.writeReturn();
        eat(";"); // ';'

    }

    /**
     * Compiles an if statement into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     ifStatement: 'if''(' expression ')''{' statements '}'
     *     ('else''{' statements '}')?
     * </p>
     */
    public void compileIf() throws Exception {
        // generate two unique label names
        String label1 = "L" + labelCounter;
        labelCounter++;
        String label2 = "L" + labelCounter;
        labelCounter++;

        eat("if"); // 'if'
        eat("\\("); // '('
        compileExpression();
        eat("\\)"); // ')'

        vmw.writeArithmetic("not");
        vmw.writeIf(label1);

        eat("\\{"); // '{'
        compileStatements();
        eat("\\}"); // '}'

        vmw.writeGoto(label2);
        vmw.writeLabel(label1);

        if (jt.getToken().equals("else")) { // eat else block if one is present
            eat("else"); // 'else'
            eat("\\{"); // '{'
            compileStatements();
            eat("\\}"); // '}'
        }
        vmw.writeLabel(label2);
    }

    /**
     * Compiles an expression into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     expression: term (op term)*
     * </p>
     * <p>
     *     op: '+'|'-'|'*'|'/'|'&'|'|'|'<'|'>'|'='
     * </p>
     */
    public void compileExpression() throws Exception {

        compileTerm();
        while (Pattern.matches("[+\\-*/&|<>=]", jt.getToken())) { // (op term)*
            String op = eat("[+\\-*/&|<>=]"); // op
            compileTerm();
            switch (op) {
                case "+":
                    vmw.writeArithmetic("add");
                    break;
                case "-":
                    vmw.writeArithmetic("sub");
                    break;
                case "*":
                    vmw.writeCall("Math.multiply", 2);
                    break;
                case "/":
                    vmw.writeCall("Math.divide", 2);
                    break;
                case "&":
                    vmw.writeArithmetic("and");
                    break;
                case "|":
                    vmw.writeArithmetic("or");
                    break;
                case "<":
                    vmw.writeArithmetic("lt");
                    break;
                case ">":
                    vmw.writeArithmetic("gt");
                    break;
                case "=":
                    vmw.writeArithmetic("eq");
                    break;
            }
        }
    }

    /**
     * Compiles a term into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     term: integerConstant | stringConstant | keywordConstant |
     *     varName | varName '[' expression ']' | subroutineCall |
     *     '(' expression ')' | unaryOp term
     * </p>
     * <p>
     *     keywordConstant: 'true'|'false'|'null'|'this'
     * </p>
     * <p>
     *     subroutineCall: subroutineName '(' expressionList ')' | (className |
     *     varName) '.' subroutineName '(' expressionList ')'
     * </p>
     * <p>
     *     unaryOp: '-'|'~'
     * </p>
     * <p>
     *     varName: identifier
     * </p>
     */
    public void compileTerm() throws Exception {
        String token = jt.getToken();
        // token is int constant
        if (Pattern.matches(intConstantPattern, token)) {
            String intConst = eat(intConstantPattern);
            vmw.writePush("constant", Integer.parseInt(intConst));
        }
        // token is keyword const
        else if (Pattern.matches("true|false|null|this", token)) {
            String keyConst = eat("true|false|null|this");
            switch (keyConst) {
                case "true":
                    vmw.writePush("constant", 1);
                    vmw.writeArithmetic("neg");
                    break;
                case "false":
                case "null":
                    vmw.writePush("constant", 0);
                    break;
                case "this":
                    vmw.writePush("pointer", 0);
                    break;
            }
        }
        // token is str const
        else if (Pattern.matches(stringConstantPattern, token)) {
            String strConst = eat(stringConstantPattern);
            int strlen = strConst.length();
            vmw.writePush("constant", strlen);
            vmw.writeCall("String.new", 1);
            for (int i = 0; i < strlen; i++) {
                vmw.writePush("constant", (int) strConst.charAt(i));
                vmw.writeCall("String.appendChar", 2);
            }

        }
        // token is identifier...
        else if (Pattern.matches(identifierPattern, token)) {
            String identifier = eat(identifierPattern); // varName | subroutineName | className

            int expressionCount;
            switch (jt.getToken()) {
                // array val
                case "[": // check for '[' expression ']'
                    eat("\\["); // '['

                    compileExpression();
                    eat("\\]"); // ']'

                    vmw.writePush(st.segmentOf(identifier), st.indexOf(identifier));
                    vmw.writeArithmetic("add");
                    vmw.writePop("pointer", 1);
                    vmw.writePush("that", 0);
                    break;

                // method from this class
                case "(": // subroutineName '(' expressionList ')'
                    // push 'this' as first arg
                    vmw.writePush("pointer", 0);
                    eat("\\("); // '('

                    // get number of expressions compiled
                    expressionCount = compileExpressionList();
                    eat("\\)"); // ')'


                    vmw.writeCall(className + "." + identifier, expressionCount + 1);
                    break;

                // subroutine from another class
                case ".": // (className | varName) '.' subroutineName '(' expressionList ')'
                    // this is added to the function call; it's set to 1 when the subroutine is a method to account for the 'this' arg
                    int nArgsOffset = 0;
                    // method called on an object
                    if (st.indexOf(identifier) != -1) {
                        // push the object
                        vmw.writePush(st.segmentOf(identifier), st.indexOf(identifier));
                        // set identifier to object's class
                        identifier = st.typeOf(identifier);
                        nArgsOffset = 1;
                    }
                    // add .subroutineName to identifier
                    identifier += eat("\\.");
                    identifier += eat(identifierPattern);
                    eat("\\("); // '('

                    expressionCount = compileExpressionList();

                    eat("\\)"); // ')'


                    vmw.writeCall(identifier, expressionCount + nArgsOffset);
                    break;

                // varName
                default:
                    vmw.writePush(st.segmentOf(identifier), st.indexOf(identifier));
            }
        }
        // unaryOp term
        else if (Pattern.matches("[-~]", jt.getToken())) { // check for unaryOp term
            String unaryOp = eat("[-~]"); // unaryOp
            compileTerm();
            if (unaryOp.equals("-")) {
                vmw.writeArithmetic("neg");
            }
            else {
                vmw.writeArithmetic("not");
            }
        }
        // parenthesized term
        else if (Pattern.matches("\\(", jt.getToken())) { // check for '(' expression ')'
            eat("\\("); // '('
            compileExpression();
            eat("\\)"); // ')'
        }
    }

    /**
     * Compiles an expression list into VM code and writes it to the CompilationEngine's output file.
     * Uses the grammar:
     * <p>
     *     expressionList: (expression (',' expression)* )?
     * </p>
     * @return Number of expressions compiled
     */
    public int compileExpressionList() throws Exception {
        int expressionCount = 0;
        if (Pattern.matches(intConstantPattern + "|" + stringConstantPattern + "|" + keywordPattern + "|" + identifierPattern + "|\\(|-|~", jt.getToken())) { // matches start of term, which is start of expression
            compileExpression();
            expressionCount++;
            while (Pattern.matches(",", jt.getToken())) {
                eat(","); // ','
                compileExpression();
                expressionCount++;
            }
        }
        return expressionCount;
    }

    /**
     * Closes the VMWriter that the CompilationEngine is using to write VM code. This should be the last method called on the CompilationEngine.
     * @throws IOException Throws exception if the output file cannot be closed
     */
    public void close() throws IOException {
        vmw.close();
    }

    /**
     * This method checks the current JackTokenizer token against a provided regular expression, returning the token and advancing the tokenizer on a match.
     * @param regex The regex to match the current token to.
     * @return The current token, if it matched the regex.
     * @throws Exception Throws exception if the current token does not match the regex.
     */
    private String eat(String regex) throws Exception {
        // copy token and tokenType to local Strings to avoid excessive get calls, and to enable editing of the token
        String jtToken = jt.getToken();
        String jtTokenType = jt.getTokenType();

        if (jtTokenType.equals("stringConstant")) { // need to remove the quotes around string constants
            jtToken = jtToken.substring(1,jtToken.length()-1);
        }

        //throw an error if the token does not match the regex
        if (!Pattern.matches(regex, jt.getToken())) {
            jtToken = jt.getToken();
            jt.printFile();
            throw new Exception(String.format("Error: Token %s does not match pattern %s in class %s\n", jtToken, regex, className));
        }

        // advance at the end instead of the beginning so compile methods can check the next token when they need to determine the next bit of grammar
        if (jt.hasMoreTokens()) jt.advance();
        return jtToken;
    }

}
