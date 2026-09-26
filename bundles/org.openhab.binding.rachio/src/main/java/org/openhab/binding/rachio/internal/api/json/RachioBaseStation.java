/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api.json;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.firstNonBlank;
import static org.openhab.binding.rachio.internal.RachioUtils.putIfNotBlank;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.core.thing.Thing;

/**
 * Smart Hose base-station response.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioBaseStation {
    public String id = "";
    public String name = "";
    public String displayName = "";
    public String nickname = "";
    public String serialNumber = "";
    public String model = "";
    public String firmwareVersion = "";
    public String hardwareVersion = "";
    public String status = "";
    public @Nullable Boolean online;
    public @Nullable Boolean connected;

    public static RachioBaseStation fromJson(String json) {
        RachioBaseStation baseStation = RachioSmartHoseJsonParser.parseObject(json, RachioBaseStation.class,
                "baseStation", "data", "result");
        return baseStation != null ? baseStation : new RachioBaseStation();
    }

    public String getThingID() {
        return firstNonBlank(id, serialNumber, getThingName());
    }

    public String getThingName() {
        return firstNonBlank(name, displayName, nickname, "Rachio BaseStation");
    }

    public boolean isOnline() {
        Boolean online = this.online;
        if (online != null) {
            return online.booleanValue();
        }
        Boolean connected = this.connected;
        if (connected != null) {
            return connected.booleanValue();
        }
        String status = firstNonBlank(this.status);
        return "ONLINE".equalsIgnoreCase(status) || "CONNECTED".equalsIgnoreCase(status);
    }

    public boolean hasOnlineState() {
        return online != null || connected != null || !firstNonBlank(status).isBlank();
    }

    public Map<String, String> fillProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
        properties.put(PROPERTY_BASE_STATION_ID, firstNonBlank(id));
        putIfNotBlank(properties, PROPERTY_NAME, getThingName());
        putIfNotBlank(properties, Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        putIfNotBlank(properties, PROPERTY_MODEL, model);
        putIfNotBlank(properties, Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        putIfNotBlank(properties, Thing.PROPERTY_HARDWARE_VERSION, hardwareVersion);
        return properties;
    }
}
