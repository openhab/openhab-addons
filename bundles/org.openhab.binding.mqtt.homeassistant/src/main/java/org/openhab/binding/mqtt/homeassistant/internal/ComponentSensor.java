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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;

/**
 * A MQTT sensor, following the https://www.home-assistant.io/components/sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentSensor extends AbstractComponent<ComponentSensor.ChannelConfiguration> {
    public static final String sensorChannelID = "sensor"; // Randomly chosen channel "ID"
    private static final Pattern triggerIcons = Pattern.compile("^mdi:(toggle|gesture).*$");

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Sensor");
        }

        protected @Nullable String unit_of_measurement;
        protected @Nullable String device_class;
        protected boolean force_update = false;
        protected @Nullable Integer expire_after;

        protected String state_topic = "";

        protected @Nullable String json_attributes_topic;
        protected @Nullable String json_attributes_template;
        protected @Nullable List<String> json_attributes;
    }

    public ComponentSensor(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        Value value;

        String uom = channelConfiguration.unit_of_measurement;

        if (uom != null && StringUtils.isNotBlank(uom)) {
            value = new NumberValue(null, null, null, uom);
        } else {
            value = new TextValue();
        }

        String icon = channelConfiguration.icon;

        boolean trigger = triggerIcons.matcher(icon).matches();

        buildChannel(sensorChannelID, value, channelConfiguration.name, getListener(componentConfiguration, value))
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template)//
                .trigger(trigger).build();
    }

    private ChannelStateUpdateListener getListener(CFactory.ComponentConfiguration componentConfiguration,
            Value value) {
        ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        if (channelConfiguration.expire_after != null) {
            updateListener = new ExpireUpdateStateListener(updateListener, channelConfiguration.expire_after, value,
                    componentConfiguration.getTracker(), componentConfiguration.getScheduler());
        }
        return updateListener;
    }
}
