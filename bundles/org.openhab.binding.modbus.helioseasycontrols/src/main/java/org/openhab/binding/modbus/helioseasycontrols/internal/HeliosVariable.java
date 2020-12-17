/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
        return String.format("v%05d", variable);
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
        Double minVal = this.getMinVal();
        Double maxVal = this.getMaxVal();
        check = check && (((minVal == null) && (maxVal == null))
                || ((minVal != null) && (maxVal != null) && (minVal <= maxVal)));

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
        Double minVal = this.getMinVal();
        Double maxVal = this.getMaxVal();
        if ((minVal != null) && (maxVal != null)) { // min and max value are set
            try {
                if (this.type.equals(HeliosVariable.TYPE_INTEGER)) {
                    // using long becuase some variable are specified with a max of 2^32-1
                    // parsing double to allow floating point values to be processed as well
                    long l = new Double(value).longValue();
                    return (minVal.longValue() <= l) && (maxVal.longValue() >= l);
                } else if (this.type.equals(HeliosVariable.TYPE_FLOAT)) {
                    double d = Double.parseDouble(value);
                    return (minVal <= d) && (maxVal >= d);
                }
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            return true; // no range to check
        }
        return false;
    }

    /**
     * Depending on the type of variable this method formats the provided value according to the interpretation of its
     * value
     *
     * @param value The value to format
     * @return The formatted value
     * @throws HeliosException if the provided value doesn't fit to the variable
     */
    public String formatPropertyValue(String value) throws HeliosException {
        switch (this.getName()) {
            case HeliosEasyControlsBindingConstants.DATE_FORMAT:
                switch (value) {
                    case "0":
                        return "dd.mm.yyyy";
                    case "1":
                        return "mm.dd.yyyy";
                    case "2":
                        return "yyyy.mm.dd";
                }
                throw new HeliosException(this.createErrorMessage(value));
            case HeliosEasyControlsBindingConstants.UNIT_CONFIG:
                return value.equals("1") ? "DIBt" : "PHI";
            case HeliosEasyControlsBindingConstants.KWL_BE:
            case HeliosEasyControlsBindingConstants.KWL_BEC:
                return value.equals("1") ? "On" : "Off";
            case HeliosEasyControlsBindingConstants.EXTERNAL_CONTACT:
            case HeliosEasyControlsBindingConstants.FUNCTION_TYPE_KWL_EM:
                return "Function " + value;
            case HeliosEasyControlsBindingConstants.HEAT_EXCHANGER_TYPE:
                switch (value) {
                    case "1":
                        return "Plastic";
                    case "2":
                        return "Aluminium";
                    case "3":
                        return "Enthalpy";
                }
                throw new HeliosException(this.createErrorMessage(value));
            case HeliosEasyControlsBindingConstants.OFFSET_EXTRACT_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_1_EXTRACT_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_2_EXTRACT_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_3_EXTRACT_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_4_EXTRACT_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_1_SUPPLY_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_2_SUPPLY_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_3_SUPPLY_AIR:
            case HeliosEasyControlsBindingConstants.VOLTAGE_FAN_STAGE_4_SUPPLY_AIR:
                return this.getUnit() != null ? value + this.getUnit() : value;
            case HeliosEasyControlsBindingConstants.ASSIGNMENT_FAN_STAGES:
                return value.equals("0") ? "0...10V" : "stepped";
            case HeliosEasyControlsBindingConstants.FAN_STAGE_STEPPED_0TO2V:
            case HeliosEasyControlsBindingConstants.FAN_STAGE_STEPPED_2TO4V:
            case HeliosEasyControlsBindingConstants.FAN_STAGE_STEPPED_4TO6V:
            case HeliosEasyControlsBindingConstants.FAN_STAGE_STEPPED_6TO8V:
            case HeliosEasyControlsBindingConstants.FAN_STAGE_STEPPED_8TO10V:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_1:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_2:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_3:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_4:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_5:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_6:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_7:
            case HeliosEasyControlsBindingConstants.SENSOR_NAME_HUMIDITY_AND_TEMP_8:
                return value;
            case HeliosEasyControlsBindingConstants.VHZ_TYPE:
                switch (value) {
                    case "1":
                        return "EH-basis";
                    case "2":
                        return "EH-EWR";
                    case "3":
                        return "SEWT";
                    case "4":
                        return "LEWT";
                }
                throw new HeliosException(this.createErrorMessage(value));
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_0:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_1:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_2:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_3:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_4:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_5:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_6:
            case HeliosEasyControlsBindingConstants.KWL_FTF_CONFIG_7:
                switch (value) {
                    case "1":
                        return "Relative Humidity";
                    case "2":
                        return "Temperature";
                    case "3":
                        return "Combined";
                }
                throw new HeliosException(this.createErrorMessage(value));
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_1:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_2:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_3:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_4:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_5:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_6:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_7:
            case HeliosEasyControlsBindingConstants.SENSOR_CONFIG_KWL_FTF_8:
                return value.equals("0") ? "No sensor" : "Sensor installed";
            case HeliosEasyControlsBindingConstants.HUMIDITY_CONTROL_STATUS:
            case HeliosEasyControlsBindingConstants.CO2_CONTROL_STATUS:
            case HeliosEasyControlsBindingConstants.VOC_CONTROL_STATUS:
                switch (value) {
                    case "0":
                        return "Off";
                    case "1":
                        return "Stepped";
                    case "2":
                        return "Continuous";
                }
                throw new HeliosException(this.createErrorMessage(value));
            default:
                return value;
        }
    }

    private String createErrorMessage(String value) {
        return "Illegal value for variable " + this.getName() + ": " + value;
    }

    @Override
    public int compareTo(HeliosVariable v) {
        return getVariable() - v.getVariable();
    }

    @Override
    public String toString() {
        return this.getVariableString() + ": " + this.getName() + " (" + this.getAccess() + ", " + this.getType()
                + (this.getMinVal() != null ? "[" + this.getMinVal() + "," + this.getMaxVal() + "]" : ")");
    }
}
