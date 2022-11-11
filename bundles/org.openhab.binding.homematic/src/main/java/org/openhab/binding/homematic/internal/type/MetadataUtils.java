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
package org.openhab.binding.homematic.internal.type;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.*;
import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for generating the openHAB metadata.
 *
 * @author Gerhard Riegler - Initial contribution
 * @author Michael Reitler - QuantityType support
 */

public class MetadataUtils {
    private static final Logger logger = LoggerFactory.getLogger(MetadataUtils.class);
    private static ResourceBundle descriptionsBundle;
    private static Map<String, String> descriptions = new HashMap<>();
    private static Map<String, Set<String>> standardDatapoints = new HashMap<>();

    protected static void initialize() {
        // loads all Homematic device names
        loadBundle("homematic/generated-descriptions");
        loadBundle("homematic/extra-descriptions");
        loadStandardDatapoints();
    }

    private static void loadBundle(String filename) {
        descriptionsBundle = ResourceBundle.getBundle(filename, Locale.getDefault());
        for (String key : descriptionsBundle.keySet()) {
            descriptions.put(key.toUpperCase(), descriptionsBundle.getString(key));
        }
        ResourceBundle.clearCache();
        descriptionsBundle = null;
    }

    /**
     * Loads the standard datapoints for channel metadata generation.
     */
    private static void loadStandardDatapoints() {
        Bundle bundle = FrameworkUtil.getBundle(MetadataUtils.class);
        try (InputStream stream = bundle.getResource("homematic/standard-datapoints.properties").openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split("\\|");
                    String channelType = null;
                    String datapointName = null;
                    if (parts.length > 0) {
                        channelType = parts[0].trim();
                        if (parts.length > 1) {
                            datapointName = parts[1].trim();
                        }
                    }

                    Set<String> channelDatapoints = standardDatapoints.get(channelType);
                    if (channelDatapoints == null) {
                        channelDatapoints = new HashSet<>();
                        standardDatapoints.put(channelType, channelDatapoints);
                    }

                    channelDatapoints.add(datapointName);
                }
            }
        } catch (IllegalStateException | IOException e) {
            logger.warn("Can't load standard-datapoints.properties file!", e);
        }
    }

    public interface OptionsBuilder<T> {
        public T createOption(String value, String description);
    }

    /**
     * Creates channel and config description metadata options for the given Datapoint.
     */
    public static <T> List<T> generateOptions(HmDatapoint dp, OptionsBuilder<T> optionsBuilder) {
        List<T> options = null;
        if (dp.getOptions() == null) {
            logger.warn("No options for ENUM datapoint {}", dp);
        } else {
            options = new ArrayList<>();
            for (int i = 0; i < dp.getOptions().length; i++) {
                String description = null;
                if (!dp.isVariable() && !dp.isScript()) {
                    description = getDescription(dp.getChannel().getType(), dp.getName(), dp.getOptions()[i]);
                }
                if (description == null) {
                    description = dp.getOptions()[i];
                }
                options.add(optionsBuilder.createOption(dp.getOptions()[i], description));
            }
        }
        return options;
    }

    /**
     * Returns the ConfigDescriptionParameter type for the given Datapoint.
     */
    public static Type getConfigDescriptionParameterType(HmDatapoint dp) {
        if (dp.isBooleanType()) {
            return Type.BOOLEAN;
        } else if (dp.isIntegerType()) {
            return Type.INTEGER;
        } else if (dp.isFloatType()) {
            return Type.DECIMAL;
        } else {
            return Type.TEXT;
        }
    }

    /**
     * Returns the unit metadata string for the given Datapoint.
     */
    public static String getUnit(HmDatapoint dp) {
        if (dp.getUnit() != null) {
            return dp.getUnit().replace("100%", "%").replace("%", "%%");
        }
        return null;
    }

    /**
     * Returns the pattern metadata string for the given Datapoint.
     */
    public static String getPattern(HmDatapoint dp) {
        if (dp.isFloatType()) {
            return "%.2f";
        } else if (dp.isNumberType()) {
            return "%d";
        } else {
            return null;
        }
    }

    /**
     * Returns the state pattern metadata string with unit for the given Datapoint.
     */
    public static String getStatePattern(HmDatapoint dp) {
        String unit = getUnit(dp);
        if ("%%".equals(unit)) {
            return "%d %%";
        }
        if (unit != null && !unit.isEmpty()) {
            String pattern = getPattern(dp);
            if (pattern != null) {
                return String.format("%s %s", pattern, "%unit%");
            }
        }
        return null;
    }

    /**
     * Returns the label string for the given Datapoint.
     */
    public static String getLabel(HmDatapoint dp) {
        return MiscUtils.capitalize(dp.getName().replace("_", " "));
    }

    /**
     * Returns the parameter name for the specified Datapoint.
     */
    public static String getParameterName(HmDatapoint dp) {
        return String.format("HMP_%d_%s", dp.getChannel().getNumber(), dp.getName());
    }

    /**
     * Returns the description for the given keys.
     */
    public static String getDescription(String... keys) {
        StringBuilder sb = new StringBuilder();
        for (int startIdx = 0; startIdx < keys.length; startIdx++) {
            String key = String.join("|", Arrays.copyOfRange(keys, startIdx, keys.length));
            if (key.endsWith("|")) {
                key = key.substring(0, key.length() - 1);
            }
            String description = descriptions.get(key.toUpperCase());
            if (description != null) {
                return description;
            }
            sb.append(key).append(", ");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Description not found for: {}", sb.toString().substring(0, sb.length() - 2));
        }
        return null;
    }

    /**
     * Returns the device name for the given device type.
     */
    public static String getDeviceName(HmDevice device) {
        if (device.isGatewayExtras()) {
            return getDescription(HmDevice.TYPE_GATEWAY_EXTRAS);
        }

        String deviceDescription = null;
        boolean isTeam = device.getType().endsWith("-Team");
        String type = isTeam ? device.getType().replace("-Team", "") : device.getType();
        deviceDescription = getDescription(type);
        if (deviceDescription != null && isTeam) {
            deviceDescription += " Team";
        }

        return deviceDescription == null ? "No Description" : deviceDescription;
    }

    /**
     * Returns the description for the given datapoint.
     */
    public static String getDatapointDescription(HmDatapoint dp) {
        if (dp.isVariable() || dp.isScript()) {
            return null;
        }
        return getDescription(dp.getChannel().getType(), dp.getName());
    }

    /**
     * Returns true, if the given datapoint is a standard datapoint.
     */
    public static boolean isStandard(HmDatapoint dp) {
        Set<String> channelDatapoints = standardDatapoints.get(dp.getChannel().getType());
        if (channelDatapoints == null) {
            return true;
        }

        return channelDatapoints.contains(dp.getName());
    }

    /**
     * Helper method for creating a BigDecimal.
     */
    public static BigDecimal createBigDecimal(Number number) {
        if (number == null) {
            return null;
        }
        try {
            return new BigDecimal(number.toString());
        } catch (Exception ex) {
            logger.warn("Can't create BigDecimal for number: {}", number.toString());
            return null;
        }
    }

    /**
     * Determines the itemType for the given Datapoint.
     */
    public static String getItemType(HmDatapoint dp) {
        String dpName = dp.getName();
        String channelType = dp.getChannel().getType();
        if (channelType == null) {
            channelType = "";
        }

        if (dp.isBooleanType()) {
            if (((dpName.equals(DATAPOINT_NAME_STATE) || dpName.equals(VIRTUAL_DATAPOINT_NAME_STATE_CONTACT))
                    && (channelType.equals(CHANNEL_TYPE_SHUTTER_CONTACT)
                            || channelType.contentEquals(CHANNEL_TYPE_TILT_SENSOR)))
                    || (dpName.equals(DATAPOINT_NAME_SENSOR) && channelType.equals(CHANNEL_TYPE_SENSOR))) {
                return ITEM_TYPE_CONTACT;
            } else {
                return ITEM_TYPE_SWITCH;
            }
        } else if (dp.isNumberType()) {
            if (dpName.startsWith(DATAPOINT_NAME_LEVEL) && isRollerShutter(dp)) {
                return ITEM_TYPE_ROLLERSHUTTER;
            } else if (dpName.startsWith(DATAPOINT_NAME_LEVEL) && !channelType.equals(CHANNEL_TYPE_WINMATIC)
                    && !channelType.equals(CHANNEL_TYPE_AKKU)) {
                return ITEM_TYPE_DIMMER;
            } else {
                // determine QuantityType
                String unit = dp.getUnit() != null ? dp.getUnit() : "";
                switch (unit) {
                    case "Â°C":
                    case "°C":
                        return ITEM_TYPE_NUMBER + ":Temperature";
                    case "V":
                        return ITEM_TYPE_NUMBER + ":ElectricPotential";
                    case "100%":
                    case "% rH":
                    case "% rF":
                    case "%":
                        return ITEM_TYPE_NUMBER + ":Dimensionless";
                    case "mHz":
                    case "Hz":
                        return ITEM_TYPE_NUMBER + ":Frequency";
                    case "hPa":
                        return ITEM_TYPE_NUMBER + ":Pressure";
                    case "Lux":
                        return ITEM_TYPE_NUMBER + ":Illuminance";
                    case "degree":
                        return ITEM_TYPE_NUMBER + ":Angle";
                    case "km/h":
                        return ITEM_TYPE_NUMBER + ":Speed";
                    case "mm":
                        return ITEM_TYPE_NUMBER + ":Length";
                    case "W":
                        return ITEM_TYPE_NUMBER + ":Power";
                    case "Wh":
                        return ITEM_TYPE_NUMBER + ":Energy";
                    case "m3":
                        return ITEM_TYPE_NUMBER + ":Volume";
                    case "":
                        if (dpName.startsWith(DATAPOINT_NAME_OPERATING_VOLTAGE)) {
                            return ITEM_TYPE_NUMBER + ":ElectricPotential";
                        }
                    case "s":
                    case "min":
                    case "minutes":
                    case "day":
                    case "month":
                    case "year":
                    default:
                        return ITEM_TYPE_NUMBER;
                }
            }
        } else if (dp.isDateTimeType()) {
            return ITEM_TYPE_DATETIME;
        } else {
            return ITEM_TYPE_STRING;
        }
    }

    /**
     * Returns true, if the device of the datapoint is a rollershutter.
     */
    public static boolean isRollerShutter(HmDatapoint dp) {
        String channelType = dp.getChannel().getType();
        return channelType.equals(CHANNEL_TYPE_BLIND) || channelType.equals(CHANNEL_TYPE_JALOUSIE)
                || channelType.equals(CHANNEL_TYPE_BLIND_TRANSMITTER)
                || channelType.equals(CHANNEL_TYPE_SHUTTER_TRANSMITTER)
                || channelType.equals(CHANNEL_TYPE_SHUTTER_VIRTUAL_RECEIVER)
                || channelType.contentEquals(CHANNEL_TYPE_BLIND_VIRTUAL_RECEIVER);
    }

    /**
     * Determines the category for the given Datapoint.
     */
    public static String getCategory(HmDatapoint dp, String itemType) {
        String dpName = dp.getName();
        String channelType = dp.getChannel().getType();
        if (channelType == null) {
            channelType = "";
        }

        if (dpName.equals(DATAPOINT_NAME_BATTERY_TYPE) || dpName.equals(DATAPOINT_NAME_LOWBAT)
                || dpName.equals(DATAPOINT_NAME_LOWBAT_IP)) {
            return CATEGORY_BATTERY;
        } else if (dpName.equals(DATAPOINT_NAME_STATE) && channelType.equals(CHANNEL_TYPE_ALARMACTUATOR)) {
            return CATEGORY_ALARM;
        } else if (dpName.equals(DATAPOINT_NAME_HUMIDITY)) {
            return CATEGORY_HUMIDITY;
        } else if (dpName.contains(DATAPOINT_NAME_TEMPERATURE)) {
            return CATEGORY_TEMPERATURE;
        } else if (dpName.equals(DATAPOINT_NAME_MOTION)) {
            return CATEGORY_MOTION;
        } else if (dpName.equals(DATAPOINT_NAME_AIR_PRESSURE)) {
            return CATEGORY_PRESSURE;
        } else if (dpName.equals(DATAPOINT_NAME_STATE) && channelType.equals(CHANNEL_TYPE_SMOKE_DETECTOR)) {
            return CATEGORY_SMOKE;
        } else if (dpName.equals(DATAPOINT_NAME_STATE) && channelType.equals(CHANNEL_TYPE_WATERDETECTIONSENSOR)) {
            return CATEGORY_WATER;
        } else if (dpName.equals(DATAPOINT_NAME_WIND_SPEED)) {
            return CATEGORY_WIND;
        } else if (dpName.startsWith(DATAPOINT_NAME_RAIN)
                || dpName.equals(DATAPOINT_NAME_STATE) && channelType.equals(CHANNEL_TYPE_RAINDETECTOR)) {
            return CATEGORY_RAIN;
        } else if (channelType.equals(CHANNEL_TYPE_POWERMETER) && !dpName.equals(DATAPOINT_NAME_BOOT)
                && !dpName.equals(DATAPOINT_NAME_FREQUENCY)) {
            return CATEGORY_ENERGY;
        } else if (itemType.equals(ITEM_TYPE_ROLLERSHUTTER)) {
            return CATEGORY_BLINDS;
        } else if (itemType.equals(ITEM_TYPE_CONTACT)) {
            return CATEGORY_CONTACT;
        } else if (itemType.equals(ITEM_TYPE_DIMMER)) {
            return "";
        } else if (itemType.equals(ITEM_TYPE_SWITCH)) {
            return CATEGORY_SWITCH;
        } else {
            return null;
        }
    }
}
