/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.models.ButtonInfo;
import org.openhab.io.neeo.internal.models.NeeoButtonGroup;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelKind;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelRange;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelText;
import org.openhab.io.neeo.internal.models.NeeoDeviceTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} that will serialize a {@link NeeoDevice} for communications going to the
 * NEEO Brain
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoBrainDeviceSerializer implements JsonSerializer<NeeoDevice> {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoBrainDeviceSerializer.class);

    @Override
    public JsonElement serialize(NeeoDevice device, Type deviceType, JsonSerializationContext jsonContext) {
        final JsonObject jsonObject = new JsonObject();

        final String adapterName = device.getUid().getNeeoUID();
        // jsonObject.addProperty("apiversion", "1.0"); // haven't decided if needed
        jsonObject.addProperty("adapterName", adapterName);
        jsonObject.addProperty("driverVersion", device.getDriverVersion());

        jsonObject.addProperty("type", device.getType().toString());
        jsonObject.addProperty("manufacturer", device.getManufacturer());
        jsonObject.addProperty("name", device.getName());
        jsonObject.addProperty("tokens", "");

        final NeeoDeviceTiming timing = device.getDeviceTiming();
        if (timing != null) {
            final JsonObject timingObj = new JsonObject();
            timingObj.addProperty("standbyCommandDelay", timing.getStandbyCommandDelay());
            timingObj.addProperty("sourceSwitchDelay", timing.getSourceSwitchDelay());
            timingObj.addProperty("shutdownDelay", timing.getShutdownDelay());
            jsonObject.add("timing", timingObj);
        }

        /**
         * Setup only really good for SDK discovery (which we don't do)
         * 'setup': { 'discovery': true,'registration': false,'introheader': 'header text','introtext': 'some hints'}
         */
        jsonObject.add("setup", new JsonObject());

        jsonObject.add("deviceCapabilities", jsonContext.serialize(device.getDeviceCapabilities()));

        final JsonObject deviceObj = new JsonObject();
        final String deviceName = device.getName();
        deviceObj.addProperty("name", deviceName);
        deviceObj.add("tokens", new JsonArray());
        jsonObject.add("device", deviceObj);

        final String specificName = device.getSpecificName();
        if (specificName != null && !specificName.isEmpty()) {
            deviceObj.addProperty("specificname", specificName);
            jsonObject.addProperty("specificname", specificName);
        } else if (!deviceName.isEmpty()) {
            deviceObj.addProperty("specificname", deviceName);
            jsonObject.addProperty("specificname", deviceName);
        }

        final String iconName = device.getIconName();
        if (iconName != null && !iconName.isEmpty()) {
            deviceObj.addProperty("icon", iconName);
            jsonObject.addProperty("icon", iconName);
        }

        final List<JsonObject> capabilities = new ArrayList<>();
        for (NeeoDeviceChannel channel : device.getExposedChannels()) {
            final NeeoCapabilityType capabilityType = channel.getType();

            final String compPath = NeeoConstants.CAPABILITY_PATH_PREFIX + "/" + adapterName + "/"
                    + channel.getItemName() + "/" + channel.getSubType() + "/" + channel.getChannelNbr();

            final String uniqueItemName = channel.getUniqueItemName();
            final String sensorItemName = uniqueItemName
                    + (uniqueItemName.toLowerCase().endsWith(NeeoConstants.NEEO_SENSOR_SUFFIX.toLowerCase()) ? ""
                            : NeeoConstants.NEEO_SENSOR_SUFFIX);

            if (capabilityType == NeeoCapabilityType.BUTTON) {
                final String name = channel.getLabel().isEmpty() ? uniqueItemName : channel.getLabel();

                if (channel.getKind() == NeeoDeviceChannelKind.TRIGGER) {
                    final String path = compPath + "/button/trigger";
                    capabilities.add(createBase(name, channel.getLabel(), capabilityType.toString(), path));
                } else {
                    final String value = channel.getValue();
                    final String path = compPath + "/button/"
                            + (value == null || value.isEmpty() ? "on" : NeeoUtil.encodeURIComponent(value.trim()));
                    capabilities.add(createBase(name, channel.getLabel(), capabilityType.toString(), path));
                }
            } else if (capabilityType == NeeoCapabilityType.SENSOR_POWER) {
                final JsonObject sensorTypeObj = new JsonObject();
                sensorTypeObj.addProperty("type", NeeoCapabilityType.SENSOR_POWER.toString());

                // power should NOT use the sensor suffix
                capabilities.add(createBase(uniqueItemName, channel.getLabel(), NeeoCapabilityType.SENSOR.toString(),
                        compPath + "/switch/power", sensorTypeObj));
            } else if (capabilityType == NeeoCapabilityType.SENSOR) {
                final JsonObject sensor = new JsonObject();
                sensor.addProperty("type", NeeoCapabilityType.SENSOR_RANGE.toString());

                final NeeoDeviceChannelRange channelRange = channel.getRange();
                final int[] range = new int[] { channelRange.getMinValue(), channelRange.getMaxValue() };
                sensor.add("range", jsonContext.serialize(range));
                sensor.addProperty("unit", channelRange.getUnit());

                capabilities.add(createBase(sensorItemName, channel.getLabel(), capabilityType.toString(),
                        compPath + "/sensor/sensor", sensor));
            } else if (capabilityType == NeeoCapabilityType.SLIDER) {
                final JsonObject sliderSensor = new JsonObject();
                sliderSensor.addProperty("type", NeeoCapabilityType.SENSOR_RANGE.toString());
                sliderSensor.addProperty("sensor", sensorItemName);

                final NeeoDeviceChannelRange channelRange = channel.getRange();
                final int[] range = new int[] { channelRange.getMinValue(), channelRange.getMaxValue() };
                sliderSensor.add("range", jsonContext.serialize(range));
                sliderSensor.addProperty("unit", channelRange.getUnit());
                capabilities.add(createBase(uniqueItemName, channel.getLabel(), capabilityType.toString(),
                        compPath + "/slider/actor", "slider", sliderSensor));

                final JsonObject sensorTypeObj = new JsonObject();
                sensorTypeObj.addProperty("type", NeeoCapabilityType.SENSOR_RANGE.toString());
                sensorTypeObj.add("range", jsonContext.serialize(range));
                sensorTypeObj.addProperty("unit", channelRange.getUnit());

                capabilities.add(createBase(sensorItemName, channel.getLabel(), NeeoCapabilityType.SENSOR.toString(),
                        compPath + "/slider/sensor", sensorTypeObj));
            } else if (capabilityType == NeeoCapabilityType.SWITCH) {
                final String label = channel.getLabel();

                final NeeoButtonGroup buttons = NeeoButtonGroup.parse(label);
                if (buttons == null) {
                    capabilities.add(createBase(uniqueItemName, channel.getLabel(), capabilityType.toString(),
                            compPath + "/switch/actor", new JsonPrimitive(sensorItemName)));

                    final JsonObject sensorTypeObj = new JsonObject();
                    sensorTypeObj.addProperty("type", NeeoCapabilityType.SENSOR_BINARY.toString());

                    capabilities.add(createBase(sensorItemName, channel.getLabel(),
                            NeeoCapabilityType.SENSOR.toString(), compPath + "/switch/sensor", sensorTypeObj));
                } else {
                    for (final ButtonInfo bi : buttons.getButtonInfos()) {
                        capabilities.add(createBase(bi.getLabel(), bi.getLabel(), NeeoCapabilityType.BUTTON.toString(),
                                compPath + "/button/" + bi.getSuffix()));

                    }
                }
            } else if (capabilityType == NeeoCapabilityType.IMAGEURL) {
                final String value = channel.getValue();
                final String size = (value == null || value.isEmpty() ? "large" : value.trim()).toLowerCase();

                final JsonObject jo = createBase(uniqueItemName, channel.getLabel(), capabilityType.toString(),
                        compPath + "/image/actor", "sensor", new JsonPrimitive(sensorItemName));
                jo.addProperty("size", size);
                capabilities.add(jo);

                final JsonObject sensorTypeObj = new JsonObject();
                sensorTypeObj.addProperty("type", NeeoCapabilityType.IMAGEURL.toString());

                capabilities.add(createBase(sensorItemName, channel.getLabel(), NeeoCapabilityType.SENSOR.toString(),
                        compPath + "/image/sensor", sensorTypeObj));
            } else if (capabilityType == NeeoCapabilityType.TEXTLABEL) {
                final JsonObject capObj = createBase(uniqueItemName, channel.getLabel(), capabilityType.toString(),
                        compPath + "/textlabel/actor", new JsonPrimitive(sensorItemName));

                capObj.addProperty("isLabelVisible",
                        channel instanceof NeeoDeviceChannelText ndct ? ndct.isLabelVisible() : true);

                capabilities.add(capObj);

                final JsonObject sensorTypeObj = new JsonObject();
                sensorTypeObj.addProperty("type", NeeoCapabilityType.SENSOR_CUSTOM.toString());

                capabilities.add(createBase(sensorItemName, channel.getLabel(), NeeoCapabilityType.SENSOR.toString(),
                        compPath + "/textlabel/sensor", sensorTypeObj));
            } else if (capabilityType == NeeoCapabilityType.DIRECTORY) {
                final JsonObject capObj = createBase(uniqueItemName, channel.getLabel(), capabilityType.toString(),
                        compPath + "/directory/actor");

                capabilities.add(capObj);
            } else {
                logger.debug("Unknown capability type: {} for channel {}", capabilityType, channel);
                continue;
            }

        }
        jsonObject.add("capabilities", jsonContext.serialize(capabilities));

        return jsonObject;
    }

    /**
     * Helper method to create a base element with the given name/label/type/path
     *
     * @param name the element name
     * @param label the element label
     * @param type the element type
     * @param path the element path
     * @return the json object representing the base element
     */
    private JsonObject createBase(String name, String label, String type, String path) {
        return createBase(name, label, type, path, null, null);
    }

    /**
     * Helper method to create a base element with the given name/label/type/path/sensor
     *
     * @param name the element name
     * @param label the element label
     * @param type the element type
     * @param path the element path
     * @param sensor the element sensor
     * @return the json object representing the base element
     */
    private JsonObject createBase(String name, String label, String type, String path, JsonElement sensor) {
        return createBase(name, label, type, path, "sensor", sensor);
    }

    /**
     * Helper method to create a base element with the given name/label/type/path/sensorname/sensor
     *
     * @param name the element name
     * @param label the element label
     * @param type the element type
     * @param path the element path
     * @param sensorName the element sensor name
     * @param sensor the element sensor
     * @return the json object representing the base element
     */
    private JsonObject createBase(String name, String label, String type, String path, @Nullable String sensorName,
            @Nullable JsonElement sensor) {
        final JsonObject compObj = new JsonObject();
        compObj.addProperty("name", NeeoUtil.encodeURIComponent(name));
        compObj.addProperty("label", label);
        compObj.addProperty("type", type);

        compObj.addProperty("path", NeeoUtil.encodeURIComponent(path));
        if (sensor != null && sensorName != null && !sensorName.isEmpty()) {
            if (sensor instanceof JsonPrimitive) {
                compObj.addProperty(sensorName, sensor.getAsString());
            } else {
                compObj.add(sensorName, sensor);
            }
        }
        return compObj;
    }
}
