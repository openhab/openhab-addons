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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Device Trigger, following the https://www.home-assistant.io/integrations/device_trigger.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DeviceTrigger extends AbstractComponent<DeviceTrigger.ChannelConfiguration> {
    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Device Trigger");
        }

        @SerializedName("automation_type")
        protected String automationType = "trigger";
        protected String topic = "";
        protected String type = "";
        protected String subtype = "";

        protected @Nullable String payload;
    }

    public DeviceTrigger(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        if (!"trigger".equals(channelConfiguration.automationType)) {
            throw new ConfigurationException("Component:DeviceTrigger must have automation_type 'trigger'");
        }
        if (channelConfiguration.type.isBlank()) {
            throw new ConfigurationException("Component:DeviceTrigger must have a type");
        }
        if (channelConfiguration.subtype.isBlank()) {
            throw new ConfigurationException("Component:DeviceTrigger must have a subtype");
        }

        TextValue value;
        String payload = channelConfiguration.payload;
        if (payload != null) {
            value = new TextValue(new String[] { payload });
        } else {
            value = new TextValue();
        }

        buildChannel(channelConfiguration.type, value, channelConfiguration.getName(),
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.topic, channelConfiguration.getValueTemplate()).trigger(true).build();
    }
}
