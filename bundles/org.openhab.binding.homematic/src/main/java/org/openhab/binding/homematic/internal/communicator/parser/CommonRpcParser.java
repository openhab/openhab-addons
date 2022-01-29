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
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.homematic.internal.misc.HomematicConstants;
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
        String value = Objects.toString(object, "").trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Converts the object to a integer.
     */
    protected Integer toInteger(Object object) {
        if (object == null || object instanceof Integer) {
            return (Integer) object;
        }
        try {
            return Double.valueOf(object.toString()).intValue();
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
            return Double.valueOf(object.toString());
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
            String value = object.toString();
            if (value.contains(".")) {
                return Float.parseFloat(value);
            } else {
                return Integer.parseInt(value);
            }
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
        return "true".equals(object.toString().toLowerCase());
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
    @NonNull
    protected String getSanitizedAddress(Object object) {
        String address = Objects.toString(object, "").trim().replaceFirst("\\*", "T-");
        return MiscUtils.validateCharacters(address.isEmpty() ? null : address, "Address", "_");
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
        if (unit != null) {
            unit = unit.trim().replace("\ufffd", "°");
        }
        dp.setUnit(unit == null || unit.isEmpty() ? null : unit);

        // Bypass: For several devices the CCU does not send a unit together with the value in the data point definition
        if (dp.getUnit() == null && dp.getName() != null) {
            if (dp.getName().startsWith("RSSI_")) {
                dp.setUnit("dBm");
            } else if (dp.getName().startsWith(HomematicConstants.DATAPOINT_NAME_OPERATING_VOLTAGE)) {
                dp.setUnit("V");
            } else if (HomematicConstants.DATAPOINT_NAME_HUMIDITY.equals(dp.getName())) {
                dp.setUnit("%");
            } else if (HomematicConstants.DATAPOINT_NAME_LEVEL.equals(dp.getName())) {
                dp.setUnit("100%");
            }
        }

        HmValueType valueType = HmValueType.parse(type);
        if (valueType == null || valueType == HmValueType.UNKNOWN) {
            throw new IOException("Unknown datapoint type: " + type);
        } else if (valueType == HmValueType.FLOAT && dp.getUnit() == null
                && dp.getName().matches("\\w*_TEMPERATURE(_\\w.*|$)")) {
            logger.debug("No unit information found for temperature datapoint {}, assuming Number:Temperature",
                    dp.getName());
            dp.setUnit("°C"); // Bypass for a problem with HMIP devices where unit of temperature channels is sometimes
                              // empty
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
        if (value == null || value.isBlank()) {
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
