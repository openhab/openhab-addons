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
package org.openhab.binding.hue.internal.handler.sensors;

import static org.openhab.binding.hue.internal.FullSensor.STATE_LAST_UPDATED;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.SensorConfigUpdate;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.binding.hue.internal.handler.HueSensorHandler;

/**
 * Hue Dimmer Switch
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class DimmerSwitchHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DIMMER_SWITCH);

    private long refreshIntervalInNanos = TimeUnit.MILLISECONDS.toNanos(1000);

    public DimmerSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof HueBridgeHandler) {
                refreshIntervalInNanos = TimeUnit.MILLISECONDS
                        .toNanos(((HueBridgeHandler) bridgeHandler).getSensorPollingInterval() * 2);
            }
        }
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        return new SensorConfigUpdate();
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId), timestamp = now;

        Object lastUpdated = sensor.getState().get(STATE_LAST_UPDATED);
        if (lastUpdated != null) {
            try {
                timestamp = ZonedDateTime.ofInstant(
                        LocalDateTime.parse(String.valueOf(lastUpdated), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        ZoneOffset.UTC, zoneId);
            } catch (DateTimeParseException e) {
                // do nothing
            }
        }

        Object buttonState = sensor.getState().get(FullSensor.STATE_BUTTON_EVENT);
        if (buttonState != null) {
            String value = String.valueOf(buttonState);
            updateState(CHANNEL_DIMMER_SWITCH, new DecimalType(value));
            Instant then = timestamp.toInstant();
            Instant someSecondsEarlier = now.minusNanos(refreshIntervalInNanos).toInstant();
            if (then.isAfter(someSecondsEarlier) && then.isBefore(now.toInstant())) {
                triggerChannel(EVENT_DIMMER_SWITCH, value);
            }
        }
    }
}
