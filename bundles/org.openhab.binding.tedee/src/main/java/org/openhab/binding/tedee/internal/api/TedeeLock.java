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

package org.openhab.binding.tedee.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tedee.internal.api.TedeeLock.DeviceSettings;

/**
 * 
 * Represents the status information returned by a Tedee lock.
 *
 * @author Alex Goll - Initial contribution
 */
@NonNullByDefault
public class TedeeLock {
    public int type;
    public int id;
    public @Nullable Integer isConnected;
    public @Nullable Integer jammed;
    public @Nullable Integer doorState;
    public @Nullable Integer batteryLevel;
    public @Nullable Integer isCharging;
    public @Nullable DeviceSettings deviceSettings = new DeviceSettings();
    public int rssi;
    public int deviceRevision;
    public int state;

    public String name = "";
    public String serialNumber = "";
    public String version = "";



    public static class DeviceSettings {
        public @Nullable Integer autoLockEnabled;
        public @Nullable Integer autoLockDelay;
        public @Nullable Integer autoLockImplicitEnabled;
        public @Nullable Integer autoLockImplicitDelay;
        public @Nullable Integer pullSpringEnabled;
        public @Nullable Integer pullSpringDuration;
        public @Nullable Integer autoPullSpringEnabled;
        public @Nullable Integer postponedLockEnabled;
        public @Nullable Integer postponedLockDelay;
        public @Nullable Integer buttonLockEnabled;
        public @Nullable Integer buttonUnlockEnabled;
    }
}
