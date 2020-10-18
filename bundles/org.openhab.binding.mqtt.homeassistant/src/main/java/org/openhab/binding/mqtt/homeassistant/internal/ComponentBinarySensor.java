/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;
import org.openhab.binding.mqtt.homeassistant.internal.listener.OffDelayUpdateStateListener;

/**
 * A MQTT BinarySensor, following the https://www.home-assistant.io/components/binary_sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentBinarySensor extends AbstractComponent<ComponentBinarySensor.ChannelConfiguration> {
    public static final String sensorChannelID = "sensor"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Binary Sensor");
        }

        protected @Nullable String device_class;
        protected boolean force_update = false;
        protected @Nullable Integer expire_after;
        protected @Nullable Integer off_delay;

        protected String state_topic = "";
        protected String payload_on = "ON";
        protected String payload_off = "OFF";

        protected @Nullable String json_attributes_topic;
        protected @Nullable String json_attributes_template;
        protected @Nullable List<String> json_attributes;
    }

    public ComponentBinarySensor(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        OnOffValue value = new OnOffValue(channelConfiguration.payload_on, channelConfiguration.payload_off);

        buildChannel(sensorChannelID, value, "value", getListener(componentConfiguration, value))
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template).build();
    }

    private ChannelStateUpdateListener getListener(CFactory.ComponentConfiguration componentConfiguration,
            Value value) {
        ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        if (channelConfiguration.expire_after != null) {
            updateListener = new ExpireUpdateStateListener(updateListener, channelConfiguration.expire_after, value,
                    componentConfiguration.getTracker(), componentConfiguration.getScheduler());
        }
        if (channelConfiguration.off_delay != null) {
            updateListener = new OffDelayUpdateStateListener(updateListener, channelConfiguration.off_delay, value,
                    componentConfiguration.getScheduler());
        }

        return updateListener;
    }
}
