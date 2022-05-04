/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.OnOffValue;

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

        protected String unit_of_measurement = "";
        protected @Nullable String device_class;
        protected boolean force_update = false;
        protected int expire_after = 0;

        protected String state_topic = "";
        protected String payload_on = "ON";
        protected String payload_off = "OFF";
    }

    public ComponentBinarySensor(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        if (channelConfiguration.force_update) {
            throw new UnsupportedOperationException("Component:Sensor does not support forced updates");
        }

        buildChannel(sensorChannelID, new OnOffValue(channelConfiguration.payload_on, channelConfiguration.payload_off),
                channelConfiguration.name, componentConfiguration.getUpdateListener())//
                        .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template)//
                        .unit(channelConfiguration.unit_of_measurement)//
                        .build();
    }
}
