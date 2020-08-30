package org.openhab.binding.modbus.helioseasycontrols.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a variable of the Helios modbus.
 *
 * @author Bernhard Bauer - Initial contribution
 * @version 2.0
 */
@NonNullByDefault
public class HeliosVariable implements Comparable<HeliosVariable> {

    /**
     * Read access
     */
    public static final String ACCESS_R = "R";

    /**
     * Write access
     */
    public static final String ACCESS_W = "W";

    /**
     * Read and write access
     */
    public static final String ACCESS_RW = "RW";

    /**
     * Integer type
     */
    public static final String TYPE_INTEGER = "int";

    /**
     * Float type
     */
    public static final String TYPE_FLOAT = "float";

    /**
     * String type
     */
    public static final String TYPE_STRING = "string";

    /**
     * Unit Volt
     */
    public static final String UNIT_VOLT = "V";

    /**
     * Unit %
     */
    public static final String UNIT_PERCENT = "%";

    /**
     * Unit ppm
     */
    public static final String UNIT_PPM = "ppm";

    /**
     * Unit degrees Celsius
     */
    public static final String UNIT_TEMP = "°C";

    /**
     * Unit day
     */
    public static final String UNIT_DAY = "d";

    /**
     * Unit hour
     */
    public static final String UNIT_HOUR = "h";

    /**
     * Unit minute
     */
    public static final String UNIT_MIN = "min";

    /**
     * Unit second
     */
    public static final String UNIT_SEC = "s";

    /**
     * The variable number
     */
    private int variable;

    /**
     * The variable name
     */
    private String name;

    /**
     * The variable group
     */
    private @Nullable String group;

    /**
     * The access to the variable
     */
    private String access;

    /**
     * The length of the variable (number of chars)
     */
    private int length;

    /**
     * The register count for this variable
     */
    private int count;

    /**
     * The variable type
     */
    private String type;

    /**
     * The variable's unit
     */
    private @Nullable String unit;

    /**
     * The minimal value (or null if not applicable)
     */
    private @Nullable Double minVal;

    /**
     * The maximum value (or null if not applicable)
     */
    private @Nullable Double maxVal;

    /**
     * Constructor to set the member variables
     *
     * @param variable The variable's number
     * @param name The variable's name
     * @param group The variable's group
     * @param access Access possibilities
     * @param length Number of expected characters when writing to / reading from Modbus
     * @param count Exact number of characters to write to Modbus
     * @param type Variable type (string, integer or float)
     * @param unit Variable's unit
     * @param minVal Minimum value (only applicable for numeric values)
     * @param maxVal Maximum value (only applicable for numeric values)
     */
    public HeliosVariable(int variable, String name, @Nullable String group, String access, int length, int count,
            String type, @Nullable String unit, @Nullable Double minVal, @Nullable Double maxVal) {
        this.variable = variable;
        this.name = name;
        this.group = group;
        this.access = access;
        this.length = length;
        this.count = count;
        this.type = type;
        this.unit = unit;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    /**
     * Constructor to set the member variables
     *
     * @param variable The variable's number
     * @param name The variable's name
     * @param group The variable's group
     * @param access Access possibilities
     * @param length Number of expected characters when writing to / reading from Modbus
     * @param count Exact number of characters to write to Modbus
     * @param type Variable type (string, integer or float)
     * @param unit Variable's unit
     */
    public HeliosVariable(int variable, String name, @Nullable String group, String access, int length, int count,
            String type, String unit) {
        this(variable, name, group, access, length, count, type, unit, null, null);
    }

    /**
     * Constructor to set the member variables
     *
     * @param variable The variable's number
     * @param name The variable's name
     * @param group The variable's group
     * @param access Access possibilities
     * @param length Number of expected characters when writing to / reading from Modbus
     * @param count Exact number of characters to write to Modbus
     * @param type Variable type (string, integer or float)
     */
    public HeliosVariable(int variable, String name, @Nullable String group, String access, int length, int count,
            String type) {
        this(variable, name, group, access, length, count, type, null, null, null);
    }

    /**
     * Getter for variable
     *
     * @return variable
     */
    public int getVariable() {
        return this.variable;
    }

    /**
     * Returns a formatted string representation for the variable
     *
     * @return String The string representation for the variable (e.g. 'v00020' for variable number 20)
     */
    public String getVariableString() {
        String v = Integer.toString(this.variable);
        while (v.length() < 5) {
            v = '0' + v;
        }
        v = 'v' + v;
        return v;
    }

    /**
     * Setter for name
     *
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for name
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for group
     *
     * @return group
     */
    public @Nullable String getGroup() {
        return this.group;
    }

    /**
     * Getter for access
     *
     * @return access
     */
    public String getAccess() {
        return this.access;
    }

    /**
     * Getter for length
     *
     * @return length
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Getter for count
     *
     * @return count
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Getter for the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Getter for the unit
     */
    public @Nullable String getUnit() {
        return this.unit;
    }

    /**
     * Getter for minimum value
     *
     * @return minimum value
     */
    public @Nullable Double getMinVal() {
        return this.minVal;
    }

    /**
     * Getter for maximum value
     *
     * @return maximum value
     */
    public @Nullable Double getMaxVal() {
        return this.maxVal;
    }

    /**
     * Returns the variable's name and prefixes the group, separated by a # if available
     *
     * @return the variable's name and prefixes the group, separated by a # if available
     */
    public String getGroupAndName() {
        return this.group != null ? this.group + "#" + this.name : this.name;
    }

    /**
     * Checks if the variable's data are consistent
     *
     * @return true if the variable contains consistent data
     */
    public boolean isOk() {
        boolean check;

        // this.access has one of the allowed values
        check = (this.access.equals(HeliosVariable.ACCESS_R)) || (this.access.equals(HeliosVariable.ACCESS_W))
                || (this.access.equals(HeliosVariable.ACCESS_RW));

        // this.type has one of the allowed values
        check = check && ((this.type.equals(HeliosVariable.TYPE_STRING))
                || (this.type.equals(HeliosVariable.TYPE_INTEGER)) || (this.type.equals(HeliosVariable.TYPE_FLOAT)));

        // this.minValue and this.maxValue are either not set or minValue is less than maxValue
        check = check && (((this.minVal == null) && (this.maxVal == null)) || (this.minVal <= this.maxVal));

        // length is set
        check = check && (this.length > 0);

        // count is set
        check = check && (this.count > 0);

        return check;
    }

    /**
     * Checks if the variable has write access
     *
     * @return true if the variable has write access
     */
    public boolean hasWriteAccess() {
        return (this.access.equals(HeliosVariable.ACCESS_W)) || (this.access.equals(HeliosVariable.ACCESS_RW));
    }

    /**
     * Checks if the variable has read access
     *
     * @return true if the variable has read access
     */
    public boolean hasReadAccess() {
        return (this.access.equals(HeliosVariable.ACCESS_R)) || (this.access.equals(HeliosVariable.ACCESS_RW));
    }

    /**
     * Checks if the provided value is within the accepted range
     *
     * @param value The value as a string
     * @return true if the value is within the accepted range
     */
    public boolean isInAllowedRange(String value) {
        if ((this.getMinVal() != null) && (this.getMaxVal() != null)) { // min and max value are set
            try {
                if (this.type.equals(HeliosVariable.TYPE_INTEGER)) {
                    // using long becuase some variable are specified with a max of 2^32-1
                    // parsing double to allow floating point values to be processed as well
                    Long l = new Double(value).longValue();
                    return (this.getMinVal().longValue() <= l) && (this.getMaxVal().longValue() >= l);
                } else if (this.type.equals(HeliosVariable.TYPE_FLOAT)) {
                    Double d = Double.parseDouble(value);
                    return (this.getMinVal() <= d) && (this.getMaxVal() >= d);
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            return true; // no range to check
        }
        return false;
    }

    @Override
    public int compareTo(HeliosVariable v) {
        if (this.getVariable() < v.getVariable()) {
            return -1;
        } else if (this.getVariable() == v.getVariable()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return this.getVariableString() + ": " + this.getName() + " (" + this.getAccess() + ", " + this.getType()
                + (this.getMinVal() != null ? "[" + this.getMinVal() + "," + this.getMaxVal() + "]" : ")");
    }
}
