/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Number, following the https://www.home-assistant.io/components/number.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Number extends AbstractComponent<Number.ChannelConfiguration> {
    public static final String NUMBER_CHANNEL_ID = "number"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Number");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("unit_of_measurement")
        protected @Nullable String unitOfMeasurement;
        @SerializedName("device_class")
        protected @Nullable String deviceClass;

        @SerializedName("command_template")
        protected @Nullable String commandTemplate;
        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("state_topic")
        protected String stateTopic = "";

        protected BigDecimal min = new BigDecimal(1);
        protected BigDecimal max = new BigDecimal(100);
        protected BigDecimal step = new BigDecimal(1);

        @SerializedName("payload_reset")
        protected String payloadReset = "None";

        protected String mode = "auto";

        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
    }

    public Number(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        boolean optimistic = channelConfiguration.optimistic != null ? channelConfiguration.optimistic
                : channelConfiguration.stateTopic.isBlank();

        if (optimistic && !channelConfiguration.stateTopic.isBlank()) {
            throw new ConfigurationException("Component:Number does not support forced optimistic mode");
        }

        NumberValue value = new NumberValue(channelConfiguration.min, channelConfiguration.max,
                channelConfiguration.step, UnitUtils.parseUnit(channelConfiguration.unitOfMeasurement));

        buildChannel(NUMBER_CHANNEL_ID, value, getName(), componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                .build();
    }
}
