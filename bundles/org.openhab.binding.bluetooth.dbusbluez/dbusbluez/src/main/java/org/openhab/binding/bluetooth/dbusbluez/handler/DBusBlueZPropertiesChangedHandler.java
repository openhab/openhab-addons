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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.Variant;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.CharacteristicUpdateEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.ConnectedEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.ManufacturerDataEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.NameEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.RssiEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.ServicesResolvedEvent;
import org.openhab.binding.bluetooth.dbusbluez.handler.events.TXPowerEvent;
import org.openhab.binding.bluetooth.dbusbluez.internal.DBusBlueZUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Benjamin Lafois - Initial contribution and API
 *
 */
public class DBusBlueZPropertiesChangedHandler extends AbstractPropertiesChangedHandler {

    private final Logger logger = LoggerFactory.getLogger(DBusBlueZPropertiesChangedHandler.class);

    private final List<DBusBlueZEventListener> listeners = new CopyOnWriteArrayList<>();

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void addListener(DBusBlueZEventListener listener) {
        this.listeners.add(listener);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handle(PropertiesChanged properties) {

        try {
            if (properties.getPropertiesChanged() == null) {
                logger.debug("Null properties. Skipping.");
                return;
            }

            if (properties.getPropertiesChanged().containsKey("RSSI")) {
                // Signal Update
                onRSSIUpdate(properties.getPath(), (Short) properties.getPropertiesChanged().get("RSSI").getValue());
            }

            if (properties.getPropertiesChanged().containsKey("TxPower")) {
                // TxPower
                onTXPowerUpdate(properties.getPath(),
                        (Short) properties.getPropertiesChanged().get("TxPower").getValue());
            }

            if (properties.getPropertiesChanged().containsKey("Value")) {
                // Characteristc value updated
                onValueUpdate(properties.getPath(), (byte[]) properties.getPropertiesChanged().get("Value").getValue());
            }

            if (properties.getPropertiesChanged().containsKey("Connected")) {
                onConnectedUpdate(properties.getPath(),
                        (boolean) properties.getPropertiesChanged().get("Connected").getValue());
            }

            if (properties.getPropertiesChanged().containsKey("Name")) {
                onNameUpdate(properties.getPath(), (String) properties.getPropertiesChanged().get("Name").getValue());
            }

            if (properties.getPropertiesChanged().containsKey("Alias")) {
                // TODO
            }

            if (properties.getPropertiesChanged().containsKey("ManufacturerData")) {
                onManufacturerDataUpdate(properties.getPath(),
                        properties.getPropertiesChanged().get("ManufacturerData"));
            }

            if (properties.getPropertiesChanged().containsKey("Powered")) {
                // TODO
            }

            if (properties.getPropertiesChanged().containsKey("Discovering")) {
                // TODO
            }

            if (properties.getPropertiesChanged().containsKey("ServicesResolved")) {
                onServicesResolved(properties.getPath(),
                        (boolean) properties.getPropertiesChanged().get("ServicesResolved").getValue());
            }

            logger.debug("PropertiesPath: {}", properties.getPath());
            logger.debug("PropertiesChanged: {}", properties.getPropertiesChanged());

        } catch (Exception e) {
            logger.error("Error occured in DBus Handler", e);
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onServicesResolved(String dbusPath, boolean resolved) {
        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new ServicesResolvedEvent(addr, resolved));
    }

    private void onNameUpdate(String dbusPath, String value) {
        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new NameEvent(addr, value));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onTXPowerUpdate(String dbusPath, Short txPower) {
        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new TXPowerEvent(addr, txPower));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onConnectedUpdate(String dbusPath, boolean connected) {
        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new ConnectedEvent(addr, connected));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void onManufacturerDataUpdate(String dbusPath, Variant v) {
        Map<Short, byte[]> eventData = new HashMap<>();

        DBusMap<UInt16, Variant> dbm = (DBusMap<UInt16, Variant>) v.getValue();

        for (Map.Entry<UInt16, Variant> entry : dbm.entrySet()) {
            byte[] bytes = (byte[]) entry.getValue().getValue();
            eventData.put(Short.valueOf(entry.getKey().shortValue()), bytes);
        }

        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new ManufacturerDataEvent(addr, eventData));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onValueUpdate(String dbusPath, byte[] value) {
        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new CharacteristicUpdateEvent(addr, dbusPath, value));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void onRSSIUpdate(String dbusPath, Short rssi) {
        BluetoothAddress addr = DBusBlueZUtils.dbusPathToMac(dbusPath);
        notifyListeners(new RssiEvent(addr, rssi));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void notifyListeners(DBusBlueZEvent event) {
        for (DBusBlueZEventListener listener : this.listeners) {
            if (listener.getAddress() != null && listener.getAddress().equals(event.getDevice())) {
                listener.onDBusBlueZEvent(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

}
