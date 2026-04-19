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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.base;

import java.time.Duration;
import java.time.Instant;

/**
 * Base class for all UniFi Protect devices
 * Includes common device properties like MAC, host, firmware, etc.
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class UniFiProtectDevice extends UniFiProtectModel {

    public String name;
    public String type;
    public String mac;
    public String host;
    public Instant upSince;
    public Duration uptime;
    public Instant lastSeen;
    public String hardwareRevision;
    public String firmwareVersion;
    public Boolean isUpdating;
    public Boolean isSshEnabled;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', name='" + name + "', mac='" + mac + "'}";
    }
}
