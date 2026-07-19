import java.util.Collection;
import java.util.HashMap;

/**
 * SymbolTable objects track variable names and their type and kind, and assigns them an index.
 * The class is capable of separating class and subroutine variables based on their kind
 * (static and field variables are class level, arg and var are subroutine level).
 * The subroutine segment of the SymbolTable can be reset without losing the class segment.
 */
public class SymbolTable {

    // separate HashMaps for class and subroutine level
    HashMap<String, SymbolValue> classTable;
    HashMap<String, SymbolValue> subTable;
    // index counters for each kind to track what index to assign new variables
    int staticIndex;
    int fieldIndex;
    int argIndex;
    int varIndex;

    /**
     * Constructs a new empty SymbolTable.
     */
    public SymbolTable() {
        classTable = new HashMap<>();
        subTable = new HashMap<>();
        staticIndex = 0;
        fieldIndex = 0;
        argIndex = 0;
        varIndex = 0;
    }

    /**
     * Clears the subroutine variables currently stored in the SymbolTable. Should be called when starting a new subroutine scope.
     */
    public void startSubroutine() {
        // reset subroutine table and index
        subTable.clear();
        argIndex = 0;
        varIndex = 0;
    }

    /**
     * Defines a new variable in the SymbolTable and assigns it an index. Prints a warning and does not add variable if a variable with the provided name already exists in the relevant scope, or if an invalid kind is passed.
     * @param name The name of the variable
     * @param type The type of the variable
     * @param kind The kind of the variable. Should be "static", "field", "arg", or "var"
     */
    public void define(String name, String type, String kind) {
        /* adds a key-value pair to the correct HashMap.
         * key = name
         * val = SymbolValue where:
         * val.type = type
         * val.kind = kind
         * val.index = index
         */
        // if variable is static or field, it has class scope
        if (kind.equals("static") || kind.equals("field")) {
            // make sure the variable doesn't already exist
            if (classTable.containsKey(name)) {
                System.out.println("WARNING: variable " + name + " already defined in class scope!\n");
            }
            else {
                // set key to name, and value to new SymbolValue containing type, kind, and index
                if (kind.equals("static")) {
                    classTable.put(name, new SymbolValue(type, kind, staticIndex));
                    staticIndex++;
                }
                else {
                    classTable.put(name, new SymbolValue(type, kind, fieldIndex));
                    fieldIndex++;
                }
            }
        }
        // if variable is arg or var, it has subroutine scope
        else if (kind.equals("arg") || kind.equals("var")) {
            if (subTable.containsKey(name)) {
                System.out.println("WARNING: variable " + name + " already defined in subroutine scope!\n");
            }
            else {
                if (kind.equals("arg")) {
                    subTable.put(name, new SymbolValue(type, kind, argIndex));
                    argIndex++;
                }
                else {
                    subTable.put(name, new SymbolValue(type, kind, varIndex));
                    varIndex++;
                }

            }
        }
        // this section is only reached when an invalid kind is passed to the method
        else {
            System.out.println("WARNING: variable " + name + " has invalid kind " + kind + "!\n");
        }
    }

    /**
     * Counts how many variables of a given kind exist in the SymbolTable.
     * @param kind The kind of variable to count. Should be "static", "field", "arg", or "var"
     * @return The number of variables in the SymbolTable with the given kind, or -1 if an invalid kind is passed in.
     */
    public int varCount(String kind) {
        int count = 0;
        // create an empty collection of arrays
        Collection<SymbolValue> values;
        // if kind is class-level, get all value arrays from classTable
        if (kind.equals("static") || kind.equals("field")) {
            values = classTable.values();
        }
        // if kind is subroutine-level, get all value arrays from subTable
        else if (kind.equals("arg") || kind.equals("var")) {
            values = subTable.values();
        }
        // if kind is invalid, return -1
        else return -1;
        // count all values that contain a matching kind
        for (SymbolValue value : values) {
            if (value.kind().equals(kind)) count++;
        }
        return count;
    }

    /**
     * Gets the type of the given variable.
     * @param name The name of the variable
     * @return The type of the variable, or null if the variable could not be found.
     */
    public String typeOf(String name) {
        if (subTable.containsKey(name)) {
            return subTable.get(name).type();
        }
        if (classTable.containsKey(name)) {
            return classTable.get(name).type();
        }
        return null;

    }

    /**
     * Gets the kind of the given variable.
     * @param name The name of the variable
     * @return The kind of the variable, or null if the variable could not be found.
     */
    public String kindOf(String name) {
        if (subTable.containsKey(name)) {
            return subTable.get(name).kind();
        }
        if (classTable.containsKey(name)) {
            return classTable.get(name).kind();
        }
        return null;
    }

    /**
     * Gets the memory segment corresponding to the kind of the given variable.
     * A variable of kind "static" returns "static", "field" returns "this", "arg" returns "argument", and "var" returns "local".
     * @param name The name of the variable
     * @return The memory segment corresponding to the variable's kind, or null if the variable could not be found.
     */
    public String segmentOf(String name) {
        String kind = kindOf(name);
        switch(kind) {
            case "static":
                return "static";
            case "field":
                return "this";
            case "arg":
                return "argument";
            case "var":
                return "local";
            default:
                return null;
        }
    }

    /**
     * Gets the index of the given variable.
     * @param name The name of the variable
     * @return The index of the variable, or -1 if the variable could not be found.
     */
    public int indexOf(String name) {
        if (subTable.containsKey(name)) {
            return subTable.get(name).index();
        }
        if (classTable.containsKey(name)) {
            return classTable.get(name).index();
        }
        return -1;

    }
}

/**
 * Simple class used by SymbolTable to keep track of variable type, kind, and index in one object so they can all be stored in a single HashMap value.
 */
class SymbolValue { // this could be a record class but the project needs to be compiled with Java 8
    private final String type;
    private final String kind;
    private final int index;

    /**
     * Creates a new SymbolValue and sets its properties
     * @param type The type of the variable
     * @param kind The kind of the variable
     * @param index The index of the variable
     */
    public SymbolValue(String type, String kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    /**
     * Gets the SymbolValue's type
     * @return The type of the SymbolValue
     */
    public String type() {
        return this.type;
    }
    /**
     * Gets the SymbolValue's kind
     * @return The kind of the SymbolValue
     */
    public String kind() {
        return this.kind;
    }
    /**
     * Gets the SymbolValue's index
     * @return The index of the SymbolValue
     */
    public int index() {
        return this.index;
    }
}
