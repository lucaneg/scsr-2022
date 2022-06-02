package it.unive.scsr;

import java.math.BigInteger;
import java.util.Objects;

public class ModularBinaryString implements Comparable<ModularBinaryString>{
    private final String binaryString;
    private final String overflowAt;

    public static ModularBinaryString fromLongs(long value, long overflowAt) throws SumOverflowException {
        assert overflowAt > 0 && value < overflowAt;
        if (value >= 0)
            return new ModularBinaryString(Long.toBinaryString(value % overflowAt), Long.toBinaryString(overflowAt));
        else{
            String asPosAligned = align(Long.toBinaryString((value % overflowAt) * - 1), reduced(Long.toBinaryString(overflowAt - 1)))[0];
            return new ModularBinaryString(twoComplement(asPosAligned), Long.toBinaryString(overflowAt));
        }
    }

    public static ModularBinaryString fromStrings(String value, String overflowAt) throws SumOverflowException {
        String[] aligned = align(value, overflowAt);
        value = aligned[0];
        overflowAt = aligned[1];
        if(value.compareTo(overflowAt) < 0){
            value = binaryModule(value, overflowAt);
        }
        return new ModularBinaryString(value, overflowAt); //TODO: VERY IMPORTANT if binaryValue is greater than perform module
    }

    private ModularBinaryString(String binaryValue, String overflowAt) {
        this.binaryString = binaryValue;
        this.overflowAt = overflowAt;
    }


    public String getBinaryString() {
        return binaryString;
    }

    public String getOverflowAt() {
        return overflowAt;
    }

    public static ModularBinaryString getOne(String maxValue){
        return new ModularBinaryString ("1", maxValue);
    }

    public long asUnsigned() {
        return Long.parseUnsignedLong(this.binaryString, 2);
    }

    public long asSigned() throws SumOverflowException {
        if (align(getBinaryString(), getOverflowAt())[0].charAt(1) == '0')
            return asUnsigned();
        else
            return -1 * Long.parseLong(twoComplement(getBinaryString()), 2);
    }

    public ModularBinaryString modularSum(ModularBinaryString b) throws SumOverflowException {
        assert reduced(getOverflowAt()).equals(reduced(b.getOverflowAt())); //Sum can only be performed over values with same maxValue
        String sum = binarySum(getBinaryString(), b.getBinaryString(), true);
        return new ModularBinaryString(binaryModule(sum, getOverflowAt()), getOverflowAt());
    }

    public ModularBinaryString modularSubtraction(ModularBinaryString b) throws SumOverflowException {
        assert reduced(getOverflowAt()).equals(reduced(b.getOverflowAt())); //Subtraction can only be performed over values with same maxValue
        String subtraction = binarySubtraction(getBinaryString(), b.getBinaryString(), true);
        return new ModularBinaryString(binaryModule(subtraction, getOverflowAt()), getOverflowAt());
    }

    public ModularBinaryString modularMultiplication(ModularBinaryString b) throws SumOverflowException {
        assert reduced(getOverflowAt()).equals(reduced(b.getOverflowAt())); //Product can only be performed over values with same maxValue
        String counter = "0".repeat(getBinaryString().length());
        String sum = "0".repeat(2 * getBinaryString().length());
        while (!reduced(counter).equals(reduced(b.getBinaryString()))) {
            sum = binarySum(sum, getBinaryString(), false);
            counter = binarySum(counter, "1", false);
        }
        return new ModularBinaryString(reduced(binaryModule(sum, getOverflowAt())), getOverflowAt());
    }

    //Delete prepending zeros
    public static String reduced(String a) {
        if (a.charAt(0) == '1')
            return a;
        if (a.indexOf('1') == -1)
            return "0";
        return a.substring(a.indexOf('1'));
    }

    public ModularBinaryString editMaxSigned(String maxValue) throws SumOverflowException {
        return ModularBinaryString.fromStrings(this.binaryString, maxValue);
    }

    /**********************************************************/

    //Zerofills to obtain equal length
    private static String[] align(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == a.length())
            b = "0".repeat(maxLength - b.length()) + b;
        else
            a = "0".repeat(maxLength - a.length()) + a;
        return new String[]{a, b};
    }

    //Binary sum over strings with extension of smaller value to match length of bigger. Method des not perform boundaries check
    private static String binarySum(String a, String b, boolean ignoreOverflow) throws SumOverflowException {
        //Create a copy of object
        String[] aligned = align(a, b);
        String aCopy = aligned[0];
        String bCopy = aligned[1];
        StringBuilder result = new StringBuilder("0".repeat(aCopy.length()));
        boolean carry = false, lastCarry = false;
        for (int i = aCopy.length() - 1; i >= 0; i--) {
            lastCarry = carry;
            if (aCopy.charAt(i) == '0' && bCopy.charAt(i) == '0') {
                if (carry)
                    result.setCharAt(i, '1');
                else
                    result.setCharAt(i, '0');
                carry = false;
            }
            if ((aCopy.charAt(i) == '1' && bCopy.charAt(i) == '0')
                    || (aCopy.charAt(i) == '0' && bCopy.charAt(i) == '1')) {
                if (carry)
                    result.setCharAt(i, '0');
                else
                    result.setCharAt(i, '1');
            }
            if (aCopy.charAt(i) == '1' && bCopy.charAt(i) == '1') {
                if (carry)
                    result.setCharAt(i, '1');
                else
                    result.setCharAt(i, '0');
                carry = true;
            }
        }
        if (lastCarry != carry) {  //If last two carrys are not equal than overflow happened!
            if (ignoreOverflow)
                return result.toString(); //If requested to ignore overflow that return truncated result
            throw new SumOverflowException("Overflow happened while performing " + a + " + " + b);
        } else        //No overflow or ignore
            return result.toString();
    }

    //Performs two complement of a given string
    private static String twoComplement(String a) throws SumOverflowException {
        StringBuilder result = new StringBuilder(a);
        //Bits inversion
        for (int i = 0; i < a.length(); i++)
            result.setCharAt(i, a.charAt(i) == '0' ? '1' : '0');
        //Adding 1
        return binarySum(result.toString(), "1", true);
    }

    //Performs binary subtraction of a given string
    private static String binarySubtraction(String a, String b, boolean ignoreOverflow) throws SumOverflowException {
        //Adding two complement of b
        return binarySum(a, twoComplement(b), ignoreOverflow);
    }

    //Performs module operation between a and m
    private static String binaryModule(String a, String m) throws SumOverflowException {
        String[] aligned = align(a, m);
        a = aligned[0];
        m = aligned[1];
        //a - m until a <= m
        while (a.compareTo(m) >= 0) {
            a = "0" + aligned[0];
            m = "0" + aligned[1];
            a = binarySubtraction(a, m, false);
            aligned = align(a, m);
        }
        return a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModularBinaryString that = (ModularBinaryString) o;
        return Objects.equals(reduced(binaryString), reduced(that.binaryString))
                && Objects.equals(reduced(overflowAt), reduced(that.overflowAt));
    }

    @Override
    public int compareTo(ModularBinaryString toCompare){
        String [] aligned = align(getBinaryString(), toCompare.getBinaryString());
        return aligned[0].compareTo(aligned[1]);
    }

    public int modularCompareTo(ModularBinaryString toCompare, String a) throws SumOverflowException {
        String [] aligned = align(getBinaryString(), toCompare.getBinaryString());
        a = align(aligned[0], a)[1];
        String thisMinusA = binarySubtraction(aligned[0], a, true);
        String toCompareMinusA = binarySubtraction(aligned[1], a, true);
        return thisMinusA.compareTo(toCompareMinusA); //TODO: check if align is required
    }

    @Override
    public int hashCode() {
        return Objects.hash(binaryString, overflowAt);
    }

    @Override
    public String toString() {
        return getBinaryString();
    }

    public static class SumOverflowException extends Exception {
        public SumOverflowException(String message) {
            super(message);
        }
    }
}
