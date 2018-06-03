/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all parsers with common methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class CommonRpcParser<M, R> implements RpcParser<M, R> {

    private final Logger logger = LoggerFactory.getLogger(CommonRpcParser.class);

    /**
     * Converts the object to a string.
     */
    protected String toString(Object object) {
        return StringUtils.trimToNull(ObjectUtils.toString(object));
    }

    /**
     * Converts the object to a integer.
     */
    protected Integer toInteger(Object object) {
        if (object == null || object instanceof Integer) {
            return (Integer) object;
        }
        try {
            return Double.valueOf(ObjectUtils.toString(object)).intValue();
        } catch (NumberFormatException ex) {
            logger.debug("Failed converting {} to a Double", object, ex);
            return null;
        }
    }

    /**
     * Converts the object to a double.
     */
    protected Double toDouble(Object object) {
        if (object == null || object instanceof Double) {
            return (Double) object;
        }
        try {
            return Double.valueOf(ObjectUtils.toString(object));
        } catch (NumberFormatException ex) {
            logger.debug("Failed converting {} to a Double", object, ex);
            return null;
        }
    }

    /**
     * Converts the object to a number.
     */
    protected Number toNumber(Object object) {
        if (object == null || object instanceof Number) {
            return (Number) object;
        }
        try {
            return NumberUtils.createNumber(ObjectUtils.toString(object));
        } catch (NumberFormatException ex) {
            logger.debug("Failed converting {} to a Number", object, ex);
            return null;
        }
    }

    /**
     * Converts the object to a boolean.
     */
    protected Boolean toBoolean(Object object) {
        if (object == null || object instanceof Boolean) {
            return (Boolean) object;
        }
        return BooleanUtils.toBoolean(ObjectUtils.toString(object));
    }

    /**
     * Converts the object to a string array.
     */
    protected String[] toOptionList(Object optionList) {
        if (optionList != null && optionList instanceof Object[]) {
            Object[] vl = (Object[]) optionList;
            String[] stringArray = new String[vl.length];
            for (int i = 0; i < vl.length; i++) {
                stringArray[i] = vl[i].toString();
            }
            return stringArray;
        }
        return null;
    }

    /**
     * Returns the address of a device, replacing group address identifier and illegal characters.
     */
    protected String getSanitizedAddress(Object object) {
        String address = StringUtils.trimToNull(StringUtils.replaceOnce(toString(object), "*", "T-"));
        return MiscUtils.validateCharacters(address, "Address", "_");
    }

    /**
     * Adjust uninitialized rssi values to zero.
     */
    protected void adjustRssiValue(HmDatapoint dp) {
        if (dp.getValue() != null && dp.getName().startsWith("RSSI_") && dp.isIntegerType()) {
            int rssiValue = ((Number) dp.getValue()).intValue();
            dp.setValue(getAdjustedRssiValue(rssiValue));
        }
    }

    /**
     * Adjust a rssi value if it is out of range.
     */
    protected Integer getAdjustedRssiValue(Integer rssiValue) {
        if (rssiValue == null || rssiValue >= 255 || rssiValue <= -255) {
            return 0;
        }
        return rssiValue;
    }

    /**
     * Converts the value to the correct type if necessary.
     */
    protected Object convertToType(HmDatapoint dp, Object value) {
        if (value == null) {
            return null;
        } else if (dp.isBooleanType()) {
            return toBoolean(value);
        } else if (dp.isIntegerType()) {
            return toInteger(value);
        } else if (dp.isFloatType()) {
            return toNumber(value);
        } else if (dp.isStringType()) {
            return toString(value);
        } else {
            return value;
        }
    }

    /**
     * Assembles a datapoint with the given parameters.
     */
    protected HmDatapoint assembleDatapoint(String name, String unit, String type, String[] options, Object min,
            Object max, Integer operations, Object defaultValue, HmParamsetType paramsetType, boolean isHmIpDevice)
            throws IOException {
        HmDatapoint dp = new HmDatapoint();
        dp.setName(name);
        dp.setDescription(name);
        dp.setUnit(StringUtils.replace(StringUtils.trimToNull(unit), "\ufffd", "°"));
        if (dp.getUnit() == null && StringUtils.startsWith(dp.getName(), "RSSI_")) {
            dp.setUnit("dBm");
        }

        HmValueType valueType = HmValueType.parse(type);
        if (valueType == null || valueType == HmValueType.UNKNOWN) {
            throw new IOException("Unknown datapoint type: " + type);
        }
        dp.setType(valueType);

        dp.setOptions(options);
        if (dp.isNumberType() || dp.isEnumType()) {
            if (isHmIpDevice && dp.isEnumType()) {
                dp.setMinValue(dp.getOptionIndex(toString(min)));
                dp.setMaxValue(dp.getOptionIndex(toString(max)));
            } else {
                dp.setMinValue(toNumber(min));
                dp.setMaxValue(toNumber(max));
            }
        }
        dp.setReadOnly((operations & 2) != 2);
        dp.setReadable((operations & 1) == 1);
        dp.setParamsetType(paramsetType);
        if (isHmIpDevice && dp.isEnumType()) {
            dp.setDefaultValue(dp.getOptionIndex(toString(defaultValue)));
        } else {
            dp.setDefaultValue(convertToType(dp, defaultValue));
        }
        dp.setValue(dp.getDefaultValue());
        return dp;
    }

    /**
     * Converts a string value to the type.
     */
    protected Object convertToType(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on")) {
            return (Boolean.TRUE);
        } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("off")) {
            return (Boolean.FALSE);
        } else if (value.matches("(-|\\+)?[0-9]+")) {
            return (Integer.valueOf(value));
        } else if (value.matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) {
            return (Double.valueOf(value));
        } else {
            return value;
        }
    }

}
