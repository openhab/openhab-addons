/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.bluez.internal.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 *
 * @author Benjamin Lafois - Initial Contribution
 *
 */
@NonNullByDefault
public class BlueZEvent {

    private static final Pattern PATTERN_ADAPTER_MAC = Pattern
            .compile("/org/bluez/(?<adapterName>[^/]+)(/dev_(?<deviceMac>[^/]+).*)?");

    public enum EventType {
        RSSI_UPDATE,
        CHARACTERISTIC_NOTIFY,
        MANUFACTURER_DATA,
        CONNECTED,
        TXPOWER,
        NAME,
        SERVICES_RESOLVED,
        ADAPTER_POWERED_CHANGED,
        ADAPTER_DISCOVERING_CHANGED
    }

    private String dbusPath;
    private EventType eventType;

    private @Nullable BluetoothAddress device;
    private @Nullable String adapterName;

    public BlueZEvent(String dbusPath, EventType eventType) {
        this.dbusPath = dbusPath;
        this.eventType = eventType;

        Matcher matcher = PATTERN_ADAPTER_MAC.matcher(dbusPath);
        if (matcher.find()) {
            this.adapterName = matcher.group("adapterName");

            String mac = matcher.group("deviceMac");
            if (mac != null) {
                this.device = new BluetoothAddress(mac.replace('_', ':'));
            }
        }
    }

    public String getDbusPath() {
        return dbusPath;
    }

    public EventType getEventType() {
        return eventType;
    }

    public @Nullable BluetoothAddress getDevice() {
        return device;
    }

    public @Nullable String getAdapterName() {
        return adapterName;
    }

    @Override
    public String toString() {
        return "EventType: " + eventType + ", Device: " + device;
    }
}
