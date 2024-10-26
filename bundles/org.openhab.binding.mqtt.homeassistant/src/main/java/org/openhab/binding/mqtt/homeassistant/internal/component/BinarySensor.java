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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;
import org.openhab.binding.mqtt.homeassistant.internal.listener.OffDelayUpdateStateListener;
import org.openhab.core.thing.type.AutoUpdatePolicy;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT BinarySensor, following the https://www.home-assistant.io/components/binary_sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BinarySensor extends AbstractComponent<BinarySensor.ChannelConfiguration> {
    public static final String SENSOR_CHANNEL_ID = "sensor";

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
    }

    public BinarySensor(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        OnOffValue value = new OnOffValue(channelConfiguration.payloadOn, channelConfiguration.payloadOff);

        buildChannel(SENSOR_CHANNEL_ID, ComponentChannelType.SWITCH, value, getName(),
                getListener(componentConfiguration, value))
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();

        finalizeChannels();
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
