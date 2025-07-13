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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.ROConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.listener.ExpireUpdateStateListener;
import org.openhab.core.types.util.UnitUtils;

/**
 * A MQTT sensor, following the https://www.home-assistant.io/components/sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Sensor extends AbstractComponent<Sensor.Configuration> {
    public static final String SENSOR_CHANNEL_ID = "sensor";

    /**
     * Configuration class for MQTT component
     */
    public static class Configuration extends EntityConfiguration implements ROConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Sensor");
        }

        @Nullable
        String getDeviceClass() {
            return getOptionalString("device_class");
        }

        @Nullable
        Integer getExpireAfter() {
            return getOptionalInt("expire_after");
        }

        boolean getForceUpdate() {
            return getBoolean("force_update");
        }

        @Nullable
        List<String> getOptions() {
            return getOptionalStringList("options");
        }

        @Nullable
        String getStateClass() {
            return getOptionalString("state_class");
        }

        @Nullable
        String getUnitOfMeasurement() {
            return getOptionalString("unit_of_measurement");
        }
    }

    public Sensor(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        Value value;
        String uom = config.getUnitOfMeasurement();
        String sc = config.getStateClass();
        ComponentChannelType type;

        if (uom != null && !uom.isBlank()) {
            value = new NumberValue(null, null, null, UnitUtils.parseUnit(uom));
            type = ComponentChannelType.NUMBER;
        } else if (sc != null && !sc.isBlank()) {
            // see state_class at https://developers.home-assistant.io/docs/core/entity/sensor#properties
            // > If not None, the sensor is assumed to be numerical
            value = new NumberValue(null, null, null, null);
            type = ComponentChannelType.NUMBER;
        } else {
            value = new TextValue();
            type = ComponentChannelType.STRING;
        }

        buildChannel(SENSOR_CHANNEL_ID, type, value, "Sensor", getListener(componentContext, value))
                .stateTopic(config.getStateTopic(), config.getValueTemplate()).build();

        finalizeChannels();
    }

    private ChannelStateUpdateListener getListener(ComponentFactory.ComponentContext componentContext, Value value) {
        ChannelStateUpdateListener updateListener = componentContext.getUpdateListener();

        Integer expireAfter = config.getExpireAfter();
        if (expireAfter != null) {
            updateListener = new ExpireUpdateStateListener(updateListener, expireAfter, value,
                    componentContext.getTracker(), componentContext.getScheduler());
        }
        return updateListener;
    }
}
