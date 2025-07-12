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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.ROConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;
import org.openhab.binding.mqtt.homeassistant.internal.listener.OffDelayUpdateStateListener;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * A MQTT BinarySensor, following the https://www.home-assistant.io/components/binary_sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BinarySensor extends AbstractComponent<BinarySensor.Configuration> {
    public static final String SENSOR_CHANNEL_ID = "sensor";

    public static class Configuration extends EntityConfiguration implements ROConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Binary Sensor");
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        String getPayloadOff() {
            return getString("payload_off");
        }

        @Nullable
        Integer getExpireAfter() {
            return getOptionalInt("expire_after");
        }

        @Nullable
        Integer getOffDelay() {
            return getOptionalInt("off_delay");
        }
    }

    public BinarySensor(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        OnOffValue value = new OnOffValue(config.getPayloadOn(), config.getPayloadOff());

        buildChannel(SENSOR_CHANNEL_ID, ComponentChannelType.SWITCH, value, "Sensor",
                getListener(componentContext, value)).stateTopic(config.getStateTopic(), config.getValueTemplate())
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();

        finalizeChannels();
    }

    private ChannelStateUpdateListener getListener(ComponentFactory.ComponentContext componentContext, Value value) {
        ChannelStateUpdateListener updateListener = componentContext.getUpdateListener();

        Integer expireAfter = config.getExpireAfter();
        if (expireAfter != null) {
            updateListener = new ExpireUpdateStateListener(updateListener, expireAfter, value,
                    componentContext.getTracker(), componentContext.getScheduler());
        }
        Integer offDelay = config.getOffDelay();
        if (offDelay != null) {
            updateListener = new OffDelayUpdateStateListener(updateListener, offDelay, value,
                    componentContext.getScheduler());
        }

        return updateListener;
    }
}
