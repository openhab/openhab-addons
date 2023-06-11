/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal;

import java.net.InetSocketAddress;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.fields.MACAddress;

/**
 * Configuration class for LIFX lights.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightConfig {

    private @Nullable String deviceId;
    private @Nullable String host;
    private long fadetime = 300; // milliseconds

    public @Nullable MACAddress getMACAddress() {
        String localDeviceId = deviceId;
        return localDeviceId == null ? null : new MACAddress(localDeviceId);
    }

    public @Nullable InetSocketAddress getHost() {
        return host == null ? null : new InetSocketAddress(host, LifxBindingConstants.UNICAST_PORT);
    }

    public Duration getFadeTime() {
        return Duration.ofMillis(fadetime);
    }
}
