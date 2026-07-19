import java.util.HashMap;

/**
 * The SymbolTable is a table that stores symbol/address pairs. It can also generate an address when adding a new symbol to the table.
 * The SymbolTable behaves like a HashMap where the symbol is the key and the address is the value.
 */
class SymbolTable {

    HashMap<String, Integer> st;
    int currentAddr;

    /**
     * Constructs a new SymbolTable and initializes default symbols.
     * @param startingAddr The address to assign the first new symbol to.
     */
    public SymbolTable(int startingAddr) {
        st = new HashMap<String, Integer>();
        initializeTable();
        currentAddr = startingAddr;
    }
    /**
     * Constructs a new SymbolTable with a starting address of 16 and initializes default symbols.
     */
    public SymbolTable() {
        this(16);
    }

    /**
     * Initializes default symbols per Hack specification.
     */
    private void initializeTable() {
        for (int i = 0; i < 16; i++) {
            addEntry("R"+ i, i);
        }
        addEntry("SP", 0);
        addEntry("LCL", 1);
        addEntry("ARG", 2);
        addEntry("THIS", 3);
        addEntry("THAT", 4);
        addEntry("SCREEN", 16384);
        addEntry("KBD", 24576);
    }

    /**
     * Adds an entry to the SymbolTable with a given address.
     * @param symbol The symbol to be added to the SymbolTable
     * @param address The address to associate with the given symbol
     */
    public void addEntry(String symbol, int address) {
        if (!st.containsKey(symbol)) {
            st.put(symbol, address);
        }
    }

    /**
     * Adds an entry to the SymbolTable at an automatically generated address.
     * @param symbol The symbol to be added to the SymbolTable
     * @return The address assigned to the symbol
     */
    public int addEntry(String symbol) {
        addEntry(symbol, currentAddr);
        currentAddr++;
        return currentAddr - 1;
    }

    /**
     * Checks if a given symbol exists in the SymbolTable
     * @param symbol The symbol to check for the presence of
     * @return True if the symbol is in the SymbolTable, false otherwise
     */
    public boolean contains(String symbol) {
        return st.containsKey(symbol);
    }

    /**
     * Gets the address associated with a given symbol.
     * @param symbol The symbol to get the address of
     * @return The address of the given symbol
     */
    public int getAddress(String symbol) {
        return st.get(symbol);
    }
}
