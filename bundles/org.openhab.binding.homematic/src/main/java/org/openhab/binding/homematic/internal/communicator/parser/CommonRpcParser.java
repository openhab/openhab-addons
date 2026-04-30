/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public abstract class CommonRpcParser<M, R> implements RpcParser<M, R> {

    private final Logger logger = LoggerFactory.getLogger(CommonRpcParser.class);

    protected Object[] unWrapArray(Object @Nullable [] message) {
        if (message != null && message.length > 0 && message[0] instanceof Object[] innerMessage) {
            return innerMessage;
        }
        return new Object[0];
    }

    /**
     * Converts the object to a string.
     */
    protected @Nullable String toString(@Nullable Object object) {
        String value = MiscUtils.toStringOrEmptyIfNull(object).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * Converts the object to an integer.
     */
    protected @Nullable Integer toInteger(@Nullable Object object) {
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
    protected @Nullable Double toDouble(@Nullable Object object) {
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
    protected @Nullable Number toNumber(@Nullable Object object) {
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
    protected @Nullable Boolean toBoolean(@Nullable Object object) {
        if (object == null || object instanceof Boolean) {
            return (Boolean) object;
        }
        return "true".equals(object.toString().toLowerCase());
    }

    /**
     * Converts the object to a string array.
     */
    protected String @Nullable [] toOptionList(@Nullable Object optionList) {
        if (optionList != null && optionList instanceof Object[] vl) {
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
    protected String getSanitizedAddress(@Nullable Object object) {
        String address = MiscUtils.toStringOrEmptyIfNull(object).trim().replaceFirst("\\*", "T-");
        return MiscUtils.validateCharacters(address.isEmpty() ? null : address, "Address", "_");
    }

    /**
     * Adjust uninitialized rssi values to zero.
     */
    protected void adjustRssiValue(HmDatapoint dp) {
        Object dpValue = dp.getValue();
        if (dpValue != null && dp.getName().startsWith("RSSI_") && dp.isIntegerType()) {
            int rssiValue = ((Number) dpValue).intValue();
            dp.setValue(getAdjustedRssiValue(rssiValue));
        }
    }

    /**
     * Adjust a rssi value if it is out of range.
     */
    protected Integer getAdjustedRssiValue(@Nullable Integer rssiValue) {
        if (rssiValue == null || rssiValue >= 255 || rssiValue <= -255) {
            return 0;
        }
        return rssiValue;
    }

    /**
     * Converts the value to the correct type if necessary.
     */
    protected @Nullable Object convertToType(HmDatapoint dp, @Nullable Object value) {
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
    protected HmDatapoint assembleDatapoint(String name, @Nullable String unit, @Nullable String type,
            String @Nullable [] options, @Nullable Object min, @Nullable Object max, @Nullable Integer operations,
            @Nullable Object defaultValue, @Nullable Map<String, Number> specialValues, HmParamsetType paramsetType,
            boolean isHmIpDevice) throws IOException {
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
        if (dp.isNumberType() && specialValues != null) {
            dp.setSpecialValues(specialValues);
        }
        dp.setValue(dp.getDefaultValue());
        return dp;
    }

    /**
     * Converts a string value to the type.
     */
    protected @Nullable Object convertToType(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if ("true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value)) {
            return (Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value)) {
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
