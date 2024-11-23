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
package org.openhab.binding.hue.internal.handler.sensors;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;
import static org.openhab.binding.hue.internal.api.dto.clip1.FullSensor.STATE_PRESENCE;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.api.dto.clip1.FullSensor;
import org.openhab.binding.hue.internal.api.dto.clip1.SensorConfigUpdate;
import org.openhab.binding.hue.internal.handler.HueSensorHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Geofence Presence Sensor
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GeofencePresenceHandler extends HueSensorHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GEOFENCE_SENSOR);

    public GeofencePresenceHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        return new SensorConfigUpdate();
    }

    @Override
    protected void doSensorStateChanged(FullSensor sensor, Configuration config) {
        Object presence = sensor.getState().get(STATE_PRESENCE);
        if (presence != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(presence));
            updateState(CHANNEL_PRESENCE, OnOffType.from(value));
        }
    }
}
