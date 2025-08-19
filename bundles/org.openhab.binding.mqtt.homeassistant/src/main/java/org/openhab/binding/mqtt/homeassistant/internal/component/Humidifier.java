/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.RWConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

/**
 * A MQTT Humidifier, following the https://www.home-assistant.io/integrations/humidifier.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Humidifier extends AbstractComponent<Humidifier.Configuration> {
    public static final String ACTION_CHANNEL_ID = "action";
    public static final String CURRENT_HUMIDITY_CHANNEL_ID = "current-humidity";
    public static final String DEVICE_CLASS_CHANNEL_ID = "device-class";
    public static final String MODE_CHANNEL_ID = "mode";
    public static final String STATE_CHANNEL_ID = "state";
    public static final String TARGET_HUMIDITY_CHANNEL_ID = "target-humidity";

    public static final String[] ACTIONS = new String[] { "off", "humidifying", "drying", "idle" };

    public static class Configuration extends EntityConfiguration implements RWConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Humidifier");
        }

        @Nullable
        Value getActionTemplate() {
            return getOptionalValue("action_template");
        }

        @Nullable
        String getActionTopic() {
            return getOptionalString("action_topic");
        }

        List<String> getModes() {
            return getStringList("modes");
        }

        @Nullable
        String getModeCommandTopic() {
            return getOptionalString("mode_command_topic");
        }

        @Nullable
        Value getCommandTemplate() {
            return getOptionalValue("command_template");
        }

        @Nullable
        Value getCurrentHumidityTemplate() {
            return getOptionalValue("current_humidity_template");
        }

        @Nullable
        String getCurrentHumidityTopic() {
            return getOptionalString("current_humidity_topic");
        }

        @Nullable
        String getDeviceClass() {
            return getOptionalString("device_class");
        }

        @Nullable
        Value getModeCommandTemplate() {
            return getOptionalValue("mode_command_template");
        }

        @Nullable
        String getModeStateTopic() {
            return getOptionalString("mode_state_topic");
        }

        @Nullable
        Value getModeStateTemplate() {
            return getOptionalValue("mode_state_template");
        }

        String getPayloadOff() {
            return getString("payload_off");
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        @Nullable
        Value getStateValueTemplate() {
            return getOptionalValue("state_value_template");
        }

        String getTargetHumidityCommandTopic() {
            return getString("target_humidity_command_topic");
        }

        @Nullable
        Value getTargetHumidityCommandTemplate() {
            return getOptionalValue("target_humidity_command_template");
        }

        double getMaxHumidity() {
            return getDouble("max_humidity");
        }

        double getMinHumidity() {
            return getDouble("min_humidity");
        }

        @Nullable
        Value getTargetHumidityStateTemplate() {
            return getOptionalValue("target_humidity_state_template");
        }

        @Nullable
        String getTargetHumidityStateTopic() {
            return getOptionalString("target_humidity_state_topic");
        }

        String getPayloadResetHumidity() {
            return getString("payload_reset_humidity");
        }

        String getPayloadResetMode() {
            return getString("payload_reset_mode");
        }
    }

    public Humidifier(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        boolean optimistic = config.isOptimistic();

        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.SWITCH,
                new OnOffValue(config.getPayloadOn(), config.getPayloadOff()), "State",
                componentContext.getUpdateListener()).stateTopic(config.getStateTopic(), config.getStateValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos(), config.getCommandTemplate())
                .inferOptimistic(optimistic).build();

        buildChannel(TARGET_HUMIDITY_CHANNEL_ID, ComponentChannelType.HUMIDITY,
                new NumberValue(BigDecimal.valueOf(config.getMinHumidity()),
                        BigDecimal.valueOf(config.getMaxHumidity()), null, Units.PERCENT),
                "Target Humidity", componentContext.getUpdateListener())
                .stateTopic(config.getTargetHumidityStateTopic(), config.getTargetHumidityStateTemplate())
                .commandTopic(config.getTargetHumidityCommandTopic(), config.isRetain(), config.getQos(),
                        config.getTargetHumidityCommandTemplate())
                .inferOptimistic(optimistic).build();

        String actionTopic = config.getActionTopic();
        if (actionTopic != null) {
            buildChannel(ACTION_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(ACTIONS), "Action",
                    componentContext.getUpdateListener()).stateTopic(actionTopic, config.getActionTemplate()).build();
        }

        String modeCommandTopic = config.getModeCommandTopic();
        if (modeCommandTopic != null) {
            TextValue modeValue = new TextValue(config.getModes().toArray(new String[0]));
            modeValue.setNullValue(config.getPayloadResetMode());
            buildChannel(MODE_CHANNEL_ID, ComponentChannelType.STRING, modeValue, "Mode",
                    componentContext.getUpdateListener())
                    .stateTopic(config.getModeStateTopic(), config.getModeStateTemplate())
                    .commandTopic(modeCommandTopic, config.isRetain(), config.getQos(), config.getModeCommandTemplate())
                    .inferOptimistic(optimistic).build();
        }

        String currentHumidityTopic = config.getCurrentHumidityTopic();
        if (currentHumidityTopic != null) {
            buildChannel(CURRENT_HUMIDITY_CHANNEL_ID, ComponentChannelType.HUMIDITY,
                    new NumberValue(null, null, null, Units.PERCENT), "Current Humidity",
                    componentContext.getUpdateListener())
                    .stateTopic(currentHumidityTopic, config.getCurrentHumidityTemplate()).build();
        }

        String deviceClass = config.getDeviceClass();
        if (deviceClass != null) {
            TextValue deviceClassValue = new TextValue();
            deviceClassValue.update(new StringType(deviceClass));
            buildChannel(DEVICE_CLASS_CHANNEL_ID, ComponentChannelType.STRING, deviceClassValue, "Device Class",
                    componentContext.getUpdateListener()).build();
        }

        finalizeChannels();
    }
}
