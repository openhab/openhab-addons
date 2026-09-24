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
package org.openhab.binding.tedee.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

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
    public int isConnected;
    public int rssi;
    public int deviceRevision;
    public int state;
    public int jammed;
    public int doorState;
    public int batteryLevel;
    public int isCharging;

    public String name = "";
    public String serialNumber = "";
    public String version = "";

    public DeviceSettings deviceSettings = new DeviceSettings();

    public static class DeviceSettings {
        public int autoLockEnabled;
        public int autoLockDelay;
        public int autoLockImplicitEnabled;
        public int autoLockImplicitDelay;
        public int pullSpringEnabled;
        public int pullSpringDuration;
        public int autoPullSpringEnabled;
        public int postponedLockEnabled;
        public int postponedLockDelay;
        public int buttonLockEnabled;
        public int buttonUnlockEnabled;
    }
}
