/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.model;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.internal.misc.MiscUtils;

/**
 * Object that holds the metadata and values for a datapoint.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmDatapoint implements Cloneable {

    private HmChannel channel;
    private String name;
    private String description;
    private Object value;
    private Object previousValue;
    private Object defaultValue;
    private HmValueType type;
    private HmParamsetType paramsetType;
    private Number minValue;
    private Number maxValue;
    private String[] options;
    private boolean readOnly;
    private boolean readable;
    private String info;
    private String unit;
    private boolean virtual;
    private boolean trigger;

    public HmDatapoint() {
    }

    public HmDatapoint(String name, String description, HmValueType type, Object value, boolean readOnly,
            HmParamsetType paramsetType) {
        this.description = description;
        this.type = type;
        this.readOnly = readOnly;
        this.paramsetType = paramsetType;
        this.value = value;
        setName(name);
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     */
    public void setName(String name) {
        this.name = MiscUtils.validateCharacters(name, "Datapoint name", "_");
    }

    /**
     * Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the channel of the datapoint.
     */
    public HmChannel getChannel() {
        return channel;
    }

    /**
     * Sets the channel of the datapoint.
     */
    public void setChannel(HmChannel channel) {
        this.channel = channel;
    }

    /**
     * Returns the value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the previous value.
     */
    public Object getPreviousValue() {
        return previousValue;
    }

    /**
     * Sets the value.
     */
    public void setValue(Object value) {
        previousValue = this.value;
        this.value = value;
    }

    /**
     * Returns the option list.
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Sets the option list.
     */
    public void setOptions(String[] options) {
        this.options = options;
    }

    /**
     * Returns the index of the value in a option list.
     */
    public int getOptionIndex(String option) {
        if (options != null && option != null) {
            for (int i = 0; i < options.length; i++) {
                String value = options[i];
                if (option.equalsIgnoreCase(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the value of a option list.
     */
    public @Nullable String getOptionValue() {
        Integer idx = getIntegerValue();
        if (options != null && idx != null && idx < options.length) {
            return options[idx];
        }
        return null;
    }

    public @Nullable Integer getIntegerValue() {
        if (value instanceof Integer) {
            return (int) value;
        } else if (value != null) {
            return Integer.parseInt(value.toString());
        } else {
            return null;
        }
    }

    public @Nullable Double getDoubleValue() {
        if (value instanceof Double) {
            return (double) value;
        } else if (value != null) {
            return Double.parseDouble(value.toString());
        } else {
            return null;
        }
    }

    /**
     * Returns the max value.
     */
    public Number getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the max value.
     */
    public void setMaxValue(Number maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Returns the min value.
     */
    public Number getMinValue() {
        return minValue;
    }

    /**
     * Sets the min value.
     */
    public void setMinValue(Number minValue) {
        this.minValue = minValue;
    }

    /**
     * Returns true, if the datapoint is readOnly.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the readOnly flag.
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Returns true, if the datapoint is readable.
     */
    public boolean isReadable() {
        return readable;
    }

    /**
     * Sets the readable flag.
     */
    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    /**
     * Returns extra infos for this datapoint.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets extra infos for this datapoint.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Returns the unit.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Returns the type.
     */
    public HmValueType getType() {
        return type;
    }

    /**
     * Sets the type.
     */
    public void setType(HmValueType type) {
        this.type = type;
    }

    /**
     * Returns the paramset type.
     */
    public HmParamsetType getParamsetType() {
        return paramsetType;
    }

    /**
     * Sets the paramset type.
     */
    public void setParamsetType(HmParamsetType paramsetType) {
        this.paramsetType = paramsetType;
    }

    /**
     * Returns the default value.
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value.
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns true, if the datapoint is a virtual datapoint.
     */
    public boolean isVirtual() {
        return virtual;
    }

    /**
     * Marks the datapoint as a virtual datapoint.
     */
    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    /**
     * Returns true, if the datapoint is a action.
     */
    public boolean isActionType() {
        return type == HmValueType.ACTION;
    }

    /**
     * Returns true, if the datapoint is a boolean.
     */
    public boolean isBooleanType() {
        return type == HmValueType.BOOL || type == HmValueType.ACTION;
    }

    /**
     * Returns true, if the datapoint is a float.
     */
    public boolean isFloatType() {
        return type == HmValueType.FLOAT;
    }

    /**
     * Returns true, if the datapoint is a integer.
     */
    public boolean isIntegerType() {
        return type == HmValueType.INTEGER;
    }

    /**
     * Returns true, if the datapoint is a number.
     */
    public boolean isNumberType() {
        return isIntegerType() || isFloatType();
    }

    /**
     * Returns true, if the datapoint is a string.
     */
    public boolean isStringType() {
        return type == HmValueType.STRING;
    }

    /**
     * Returns true, if the datapoint is a enum.
     */
    public boolean isEnumType() {
        return type == HmValueType.ENUM;
    }

    /**
     * Returns true, if the datapoint is a datetime (only for virtual datapoints).
     */
    public boolean isDateTimeType() {
        return type == HmValueType.DATETIME;
    }

    /**
     * Returns true, if the datapoint is a variable.
     */
    public boolean isVariable() {
        return channel.isGatewayVariable();
    }

    /**
     * Returns true, if the datapoint is a program.
     */
    public boolean isScript() {
        return channel.isGatewayScript();
    }

    /**
     * Returns true, if the name of the datapoint starts with PRESS_.
     */
    public boolean isPressDatapoint() {
        return name != null && name.startsWith("PRESS_");
    }

    /**
     * Sets the trigger flag.
     */
    public void setTrigger(boolean trigger) {
        this.trigger = trigger;
    }

    /**
     * Returns true, if the datapoint should be handled as a trigger.
     */
    public boolean isTrigger() {
        return trigger;
    }

    @Override
    public HmDatapoint clone() {
        HmDatapoint dp = new HmDatapoint(name, description, type, value, readOnly, paramsetType);
        dp.setChannel(channel);
        dp.setMinValue(minValue);
        dp.setMaxValue(maxValue);
        dp.setOptions(options);
        dp.setInfo(info);
        dp.setUnit(unit);
        dp.setVirtual(virtual);
        dp.setReadable(readable);
        dp.setTrigger(trigger);
        dp.setDefaultValue(defaultValue);
        return dp;
    }

    @Override
    public String toString() {
        return String.format("%s[name=%s,value=%s,defaultValue=%s,type=%s,minValue=%s,maxValue=%s,options=%s,"
                + "readOnly=%b,readable=%b,unit=%s,description=%s,info=%s,paramsetType=%s,virtual=%b,trigger=%b]",
                getClass().getSimpleName(), name, value, defaultValue, type, minValue, maxValue,
                (options == null ? null : String.join(";", options)), readOnly, readable, unit, description, info,
                paramsetType, virtual, trigger);
    }
}
