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
 * Smart Hose valve response.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValve {
    public String id = "";
    public String baseStationId = "";
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
    public @Nullable Double batteryLevel;
    public @Nullable Integer defaultRuntimeSeconds;
    public @Nullable RachioValveState state;
    public @Nullable RachioValveState valveState;

    public static RachioValve fromJson(String json) {
        RachioValve valve = RachioSmartHoseJsonParser.parseObject(json, RachioValve.class, "valve", "data", "result");
        return valve != null ? valve : new RachioValve();
    }

    public String getThingID() {
        return firstNonBlank(id, serialNumber, getThingName());
    }

    public String getThingName() {
        return firstNonBlank(name, displayName, nickname, "Rachio Valve");
    }

    public RachioValveState getState() {
        RachioValveState valveState = this.valveState;
        if (valveState != null) {
            return valveState;
        }
        RachioValveState state = this.state;
        return state != null ? state : new RachioValveState();
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
        RachioValveState state = getState();
        Boolean stateOnline = state.online;
        if (stateOnline != null) {
            return stateOnline.booleanValue();
        }
        Boolean stateConnected = state.connected;
        if (stateConnected != null) {
            return stateConnected.booleanValue();
        }
        String status = firstNonBlank(this.status);
        return "ONLINE".equalsIgnoreCase(status) || "CONNECTED".equalsIgnoreCase(status);
    }

    public boolean hasOnlineState() {
        RachioValveState state = getState();
        return online != null || connected != null || state.online != null || state.connected != null
                || !firstNonBlank(status).isBlank();
    }

    public boolean stateMatches() {
        Boolean matches = getState().matches;
        return matches != null && matches.booleanValue();
    }

    public boolean hasStateMatches() {
        return getState().matches != null;
    }

    public boolean flowDetected() {
        return getState().getFlowDetected();
    }

    public boolean hasFlowDetected() {
        return getState().hasFlowDetected();
    }

    public int getDefaultRuntimeSeconds() {
        Integer defaultRuntimeSeconds = this.defaultRuntimeSeconds;
        if (defaultRuntimeSeconds != null && defaultRuntimeSeconds.intValue() > 0) {
            return defaultRuntimeSeconds.intValue();
        }
        Integer runtime = getState().defaultRuntimeSeconds;
        return runtime != null ? Math.max(0, runtime.intValue()) : 0;
    }

    public Map<String, String> fillProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, RachioBindingConstants.BINDING_VENDOR);
        properties.put(PROPERTY_VALVE_ID, firstNonBlank(id));
        putIfNotBlank(properties, PROPERTY_BASE_STATION_ID, baseStationId);
        putIfNotBlank(properties, PROPERTY_NAME, getThingName());
        putIfNotBlank(properties, Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        putIfNotBlank(properties, PROPERTY_MODEL, model);
        putIfNotBlank(properties, Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
        putIfNotBlank(properties, Thing.PROPERTY_HARDWARE_VERSION, hardwareVersion);
        return properties;
    }
}
