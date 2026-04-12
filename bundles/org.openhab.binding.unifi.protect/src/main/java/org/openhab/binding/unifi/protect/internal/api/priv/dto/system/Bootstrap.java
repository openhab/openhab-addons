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
package org.openhab.binding.unifi.protect.internal.api.priv.dto.system;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.base.UniFiProtectObject;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.AiPort;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Bridge;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Camera;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Chime;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Light;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Sensor;
import org.openhab.binding.unifi.protect.internal.api.priv.dto.devices.Viewer;

/**
 * Bootstrap model - the main container for all UniFi Protect data
 * This is returned from /api/bootstrap and contains all devices, users, and configuration
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Bootstrap extends UniFiProtectObject {

    public String authUserId;
    public String accessKey;
    public Map<String, Camera> cameras;
    public Map<String, User> users;
    public Map<String, Group> groups;
    public Map<String, Liveview> liveviews;
    public Nvr nvr;
    public Map<String, Viewer> viewers;
    public Map<String, Light> lights;
    public Map<String, Bridge> bridges;
    public Map<String, Sensor> sensors;
    public Map<String, Doorlock> doorlocks;
    public Map<String, Chime> chimes;
    public Map<String, AiPort> aiports;
    public Map<String, Event> events;
    public String lastUpdateId;

    public @Nullable User getAuthUser() {
        return users != null ? users.get(authUserId) : null;
    }

    public @Nullable Camera getCamera(String id) {
        return cameras != null ? cameras.get(id) : null;
    }

    public @Nullable Light getLight(String id) {
        return lights != null ? lights.get(id) : null;
    }

    public @Nullable Sensor getSensor(String id) {
        return sensors != null ? sensors.get(id) : null;
    }

    public @Nullable Doorlock getDoorlock(String id) {
        return doorlocks != null ? doorlocks.get(id) : null;
    }
}
