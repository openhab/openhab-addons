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
package org.openhab.binding.bluetooth.dbusbluez.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAddress;

/**
 *
 * @author Benjamin Lafois
 *
 */
@NonNullByDefault
public class DBusBlueZEvent {

    public enum EventType {
        RSSI_UPDATE,
        CHARACTERISTIC_NOTIFY,
        MANUFACTURER_DATA,
        CONNECTED,
        TXPOWER,
        NAME,
        SERVICES_RESOLVED,
        ADAPTER_POWERED_CHANGED
    }

    private EventType eventType;

    private @Nullable BluetoothAddress device;
    private @Nullable String adapter;

    public DBusBlueZEvent(EventType eventType, BluetoothAddress device) {
        this.eventType = eventType;
        this.device = device;
    }

    public DBusBlueZEvent(EventType eventType, String adapter) {
        this.eventType = eventType;
        this.adapter = adapter;
    }

    public EventType getEventType() {
        return eventType;
    }

    public @Nullable BluetoothAddress getDevice() {
        return device;
    }

    public @Nullable String getAdapter() {
        return adapter;
    }

    @Override
    public String toString() {
        return "EventType: " + eventType + ", Device: " + device;
    }

}
