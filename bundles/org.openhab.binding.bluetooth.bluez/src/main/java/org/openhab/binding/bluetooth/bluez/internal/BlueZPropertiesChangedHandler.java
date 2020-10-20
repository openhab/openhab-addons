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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.Variant;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.bluez.internal.events.AdapterDiscoveringChangedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.AdapterPoweredChangedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEventListener;
import org.openhab.binding.bluetooth.bluez.internal.events.CharacteristicUpdateEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ConnectedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ManufacturerDataEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.NameEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.RssiEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ServicesResolvedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.TXPowerEvent;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Benjamin Lafois - Initial contribution and API
 *
 */
@NonNullByDefault

public class BlueZPropertiesChangedHandler extends AbstractPropertiesChangedHandler {

    private final Logger logger = LoggerFactory.getLogger(BlueZPropertiesChangedHandler.class);

    private final Set<BlueZEventListener> listeners = new CopyOnWriteArraySet<>();

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("bluetooth");

    public void addListener(BlueZEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(BlueZEventListener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListeners(BlueZEvent event) {
        for (BlueZEventListener listener : this.listeners) {
            listener.onDBusBlueZEvent(event);
        }
    }

    @Override
    public void handle(@Nullable PropertiesChanged properties) {
        if (properties == null || properties.getPropertiesChanged() == null) {
            logger.debug("Null properties. Skipping.");
            return;
        }
        Map<String, Variant<?>> changedProperties = properties.getPropertiesChanged();
        if (changedProperties == null) {
            logger.debug("Null properties changed. Skipping.");
            return;
        }

        // do this asynchronously so that we don't slow things down for the dbus event dispatcher
        scheduler.execute(() -> {

            changedProperties.forEach((key, variant) -> {
                switch (key) {
                    case "RSSI":
                        // Signal Update
                        onRSSIUpdate(properties.getPath(), (@NonNull Short) variant.getValue());
                        break;
                    case "TxPower":
                        // TxPower
                        onTXPowerUpdate(properties.getPath(), (@NonNull Short) variant.getValue());
                        break;
                    case "Value":
                        // Characteristc value updated
                        onValueUpdate(properties.getPath(), (byte @NonNull []) variant.getValue());
                        break;
                    case "Connected":
                        onConnectedUpdate(properties.getPath(), ((@NonNull Boolean) variant.getValue()).booleanValue());
                        break;
                    case "Name":
                        onNameUpdate(properties.getPath(), (@NonNull String) variant.getValue());
                        break;
                    case "Alias":
                        // TODO
                        break;
                    case "ManufacturerData":
                        onManufacturerDataUpdate(properties.getPath(), variant);
                        break;
                    case "Powered":
                        onPoweredUpdate(properties.getPath(), ((@NonNull Boolean) variant.getValue()).booleanValue());
                        break;
                    case "Discovering":
                        onDiscoveringUpdate(properties.getPath(),
                                ((@NonNull Boolean) variant.getValue()).booleanValue());
                        break;
                    case "ServicesResolved":
                        onServicesResolved(properties.getPath(),
                                ((@NonNull Boolean) variant.getValue()).booleanValue());
                        break;
                }
            });

            logger.debug("PropertiesPath: {}", properties.getPath());
            logger.debug("PropertiesChanged: {}", properties.getPropertiesChanged());
        });
    }

    private void onDiscoveringUpdate(String dbusPath, boolean discovering) {
        AdapterDiscoveringChangedEvent event = new AdapterDiscoveringChangedEvent(dbusPath, discovering);
        String adapter = event.getAdapterName();
        if (adapter != null) {
            notifyListeners(event);
        }
    }

    private void onPoweredUpdate(String dbusPath, boolean powered) {
        AdapterPoweredChangedEvent event = new AdapterPoweredChangedEvent(dbusPath, powered);
        String adapter = event.getAdapterName();
        if (adapter != null) {
            notifyListeners(event);
        }
    }

    private void onServicesResolved(String dbusPath, boolean resolved) {
        ServicesResolvedEvent event = new ServicesResolvedEvent(dbusPath, resolved);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }

    private void onNameUpdate(String dbusPath, String value) {
        NameEvent event = new NameEvent(dbusPath, value);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }

    private void onTXPowerUpdate(String dbusPath, Short txPower) {
        TXPowerEvent event = new TXPowerEvent(dbusPath, txPower);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }

    private void onConnectedUpdate(String dbusPath, boolean connected) {
        ConnectedEvent event = new ConnectedEvent(dbusPath, connected);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }

    @SuppressWarnings("unchecked")
    private void onManufacturerDataUpdate(String dbusPath, Variant<?> v) {
        Map<Short, byte[]> eventData = new HashMap<>();

        DBusMap<UInt16, Variant<?>> dbm = (@NonNull DBusMap<UInt16, Variant<?>>) v.getValue();

        for (Map.Entry<UInt16, Variant<?>> entry : dbm.entrySet()) {
            byte @NonNull [] bytes = (byte @NonNull []) entry.getValue().getValue();
            eventData.put(entry.getKey().shortValue(), bytes);
        }
        ManufacturerDataEvent event = new ManufacturerDataEvent(dbusPath, eventData);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }

    private void onValueUpdate(String dbusPath, byte[] value) {
        CharacteristicUpdateEvent event = new CharacteristicUpdateEvent(dbusPath, value);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }

    private void onRSSIUpdate(String dbusPath, Short rssi) {
        RssiEvent event = new RssiEvent(dbusPath, rssi);
        BluetoothAddress addr = event.getDevice();
        if (addr != null) {
            notifyListeners(event);
        }
    }
}
