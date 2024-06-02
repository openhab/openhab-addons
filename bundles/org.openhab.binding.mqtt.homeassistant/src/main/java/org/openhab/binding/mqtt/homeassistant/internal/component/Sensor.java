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

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT sensor, following the https://www.home-assistant.io/components/sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Sensor extends AbstractComponent<Sensor.ChannelConfiguration> {
    public static final String SENSOR_CHANNEL_ID = "sensor"; // Randomly chosen channel "ID"
    private static final Pattern TRIGGER_ICONS = Pattern.compile("^mdi:(toggle|gesture).*$");

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Sensor");
        }

        @SerializedName("unit_of_measurement")
        protected @Nullable String unitOfMeasurement;
        @SerializedName("device_class")
        protected @Nullable String deviceClass;
        @SerializedName("state_class")
        protected @Nullable String stateClass;
        @SerializedName("force_update")
        protected boolean forceUpdate = false;
        @SerializedName("expire_after")
        protected @Nullable Integer expireAfter;

        @SerializedName("state_topic")
        protected String stateTopic = "";

        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
        @SerializedName("json_attributes")
        protected @Nullable List<String> jsonAttributes;
    }

    public Sensor(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        Value value;
        String uom = channelConfiguration.unitOfMeasurement;
        String sc = channelConfiguration.stateClass;

        if (uom != null && !uom.isBlank()) {
            value = new NumberValue(null, null, null, UnitUtils.parseUnit(uom));
        } else if (sc != null && !sc.isBlank()) {
            // see state_class at https://developers.home-assistant.io/docs/core/entity/sensor#properties
            // > If not None, the sensor is assumed to be numerical
            value = new NumberValue(null, null, null, null);
        } else {
            value = new TextValue();
        }

        String icon = channelConfiguration.getIcon();

        boolean trigger = TRIGGER_ICONS.matcher(icon).matches();

        buildChannel(SENSOR_CHANNEL_ID, value, getName(), getListener(componentConfiguration, value))
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())//
                .trigger(trigger).build();
    }

    private ChannelStateUpdateListener getListener(ComponentFactory.ComponentConfiguration componentConfiguration,
            Value value) {
        ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        if (channelConfiguration.expireAfter != null) {
            updateListener = new ExpireUpdateStateListener(updateListener, channelConfiguration.expireAfter, value,
                    componentConfiguration.getTracker(), componentConfiguration.getScheduler());
        }
        return updateListener;
    }
}
