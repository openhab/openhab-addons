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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.OpenCloseValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;
import org.openhab.binding.mqtt.homeassistant.internal.listener.OffDelayUpdateStateListener;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT BinarySensor, following the https://www.home-assistant.io/components/binary_sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Create Contact for certain device classes
 */
@NonNullByDefault
public class BinarySensor extends AbstractComponent<BinarySensor.ChannelConfiguration> {
    public static final String SENSOR_CHANNEL_ID = "sensor";

    private static final String DEVICE_CLASS_CARBON_MONOXIDE = "carbon_monoxide";
    private static final String DEVICE_CLASS_DOOR = "door";
    private static final String DEVICE_CLASS_GARAGE_DOOR = "garage_door";
    private static final String DEVICE_CLASS_GAS = "gas";
    private static final String DEVICE_CLASS_LOCK = "lock";
    private static final String DEVICE_CLASS_MOISTURE = "moisture";
    private static final String DEVICE_CLASS_MOTION = "motion";
    private static final String DEVICE_CLASS_MOVING = "moving";
    private static final String DEVICE_CLASS_OCCUPANCY = "occupancy";
    private static final String DEVICE_CLASS_OPENING = "opening";
    private static final String DEVICE_CLASS_PROBLEM = "problem";
    private static final String DEVICE_CLASS_SAFETY = "safety";
    private static final String DEVICE_CLASS_SMOKE = "smoke";
    private static final String DEVICE_CLASS_SOUND = "sound";
    private static final String DEVICE_CLASS_TAMPER = "tamper";
    private static final String DEVICE_CLASS_VIBRATION = "vibration";
    private static final String DEVICE_CLASS_WINDOW = "window";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Binary Sensor");
        }

        @SerializedName("device_class")
        protected @Nullable String deviceClass;
        @SerializedName("force_update")
        protected boolean forceUpdate = false;
        @SerializedName("expire_after")
        protected @Nullable Integer expireAfter;
        @SerializedName("off_delay")
        protected @Nullable Integer offDelay;

        @SerializedName("state_topic")
        protected String stateTopic = "";
        @SerializedName("payload_on")
        protected String payloadOn = "ON";
        @SerializedName("payload_off")
        protected String payloadOff = "OFF";

        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
        @SerializedName("json_attributes")
        protected @Nullable List<String> jsonAttributes;
    }

    public BinarySensor(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        Value value = null;
        String deviceClass = channelConfiguration.deviceClass;
        if (deviceClass != null) {
            // https://www.home-assistant.io/integrations/binary_sensor/#device-class
            // Device classes that are obviously open/closed or secure/insecure create a Contact channel instead
            switch (deviceClass) {
                case DEVICE_CLASS_CARBON_MONOXIDE, DEVICE_CLASS_DOOR, DEVICE_CLASS_GARAGE_DOOR, DEVICE_CLASS_GAS,
                        DEVICE_CLASS_LOCK, DEVICE_CLASS_MOISTURE, DEVICE_CLASS_MOTION, DEVICE_CLASS_MOVING,
                        DEVICE_CLASS_OCCUPANCY, DEVICE_CLASS_OPENING, DEVICE_CLASS_PROBLEM, DEVICE_CLASS_SAFETY,
                        DEVICE_CLASS_SMOKE, DEVICE_CLASS_SOUND, DEVICE_CLASS_TAMPER, DEVICE_CLASS_VIBRATION,
                        DEVICE_CLASS_WINDOW:
                    value = new OpenCloseValue(channelConfiguration.payloadOn, channelConfiguration.payloadOff);
                default:
            }
        }
        if (value == null) {
            value = new OnOffValue(channelConfiguration.payloadOn, channelConfiguration.payloadOff);
        }

        buildChannel(SENSOR_CHANNEL_ID, value, "value", getListener(componentConfiguration, value))
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate()).build();
    }

    private ChannelStateUpdateListener getListener(ComponentFactory.ComponentConfiguration componentConfiguration,
            Value value) {
        ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        if (channelConfiguration.expireAfter != null) {
            updateListener = new ExpireUpdateStateListener(updateListener, channelConfiguration.expireAfter, value,
                    componentConfiguration.getTracker(), componentConfiguration.getScheduler());
        }
        if (channelConfiguration.offDelay != null) {
            updateListener = new OffDelayUpdateStateListener(updateListener, channelConfiguration.offDelay, value,
                    componentConfiguration.getScheduler());
        }

        return updateListener;
    }
}
