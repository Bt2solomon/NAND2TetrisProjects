/**
 * The Code class contains methods to convert segments of assembly instructions to Hack binary.
 */
class Code {
    /**
     * Convert an assembly destination segment to Hack binary.
     * @param dest The assembly destination segment to be converted
     * @return The Hack binary translation of the given destination
     */
    public String dest(String dest) {
        if (dest == null) return "000";
        return switch (dest) {
            case "M" -> "001";
            case "D" -> "010";
            case "MD" -> "011";
            case "A" -> "100";
            case "AM" -> "101";
            case "AD" -> "110";
            case "AMD" -> "111";
            default -> null;
        };
    }
    /**
     * Convert an assembly computation instruction segment to Hack binary.
     * @param comp The assembly computation instruction segment to be converted
     * @return The Hack binary translation of the given computation instruction
     */
    public String comp(String comp) {
        return switch (comp) {
            case "0" -> "0101010";
            case "1" -> "0111111";
            case "-1" -> "0111010";
            case "D" -> "0001100";
            case "A" -> "0110000";
            case "!D" -> "0001101";
            case "!A" -> "0110001";
            case "-D" -> "0001111";
            case "-A" -> "0110011";
            case "D+1", "1+D" -> "0011111";
            case "A+1", "1+A" -> "0110111";
            case "D-1" -> "0001110";
            case "A-1" -> "0110010";
            case "D+A", "A+D" -> "0000010";
            case "D-A" -> "0010011";
            case "A-D" -> "0000111";
            case "D&A", "A&D" -> "0000000";
            case "D|A", "A|D" -> "0010101";

            case "M" -> "1110000";
            case "!M" -> "1110001";
            case "-M" -> "1110011";
            case "M+1", "1+M" -> "1110111";
            case "M-1" -> "1110010";
            case "D+M", "M+D" -> "1000010";
            case "D-M" -> "1010011";
            case "M-D" -> "1000111";
            case "D&M", "M&D" -> "1000000";
            case "D|M", "M|D" -> "1010101";
            default -> null;
        };
    }
    /**
     * Convert an assembly jump condition segment to Hack binary.
     * @param jump The assembly jump condition to be converted
     * @return The Hack binary translation of the given jump condition
     */
    public String jump(String jump) {
        if (jump == null) return "000";
        return switch (jump) {
            case "JGT" -> "001";
            case "JEQ" -> "010";
            case "JGE" -> "011";
            case "JLT" -> "100";
            case "JNE" -> "101";
            case "JLE" -> "110";
            case "JMP" -> "111";
            default -> null;
        };
    }

    /**
     * Attempts to convert an integer String to a binary String of at least a specified length, returning null on NumberFormatException
     * @param num String value to be converted
     * @param length Desired length of binary string (not used if the binary value is already at least this long)
     * @return A String containing binary value of the input String contents, or null if the String could not be converted to an int
     */
    public String convertToBinary(String num, int length) {
        try {
            int intNum = Integer.parseInt(num);
            return convertToBinary(intNum, length);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    /**
     * Attempts to convert an integer to a binary String of at least a specified length
     * @param num int value to be converted
     * @param length Desired length of binary string (not used if the binary value is already at least this long)
     * @return A String containing binary value of the input String contents
     */
    public String convertToBinary(int num, int length) {

        //convert  int to binary string(builder), then append 0s to front until it reaches the correct length
        StringBuilder symBinary = new StringBuilder(Integer.toBinaryString(num));
        while (symBinary.length() < length) {
            symBinary.insert(0, "0");
        }
        return symBinary.toString();
    }

}
