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
package org.openhab.binding.bluetooth.bluez.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.Variant;
import org.openhab.binding.bluetooth.bluez.internal.events.AdapterDiscoveringChangedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.AdapterPoweredChangedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.BlueZEventListener;
import org.openhab.binding.bluetooth.bluez.internal.events.CharacteristicUpdateEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ConnectedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ManufacturerDataEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.NameEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.RssiEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ServiceDataEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.ServicesResolvedEvent;
import org.openhab.binding.bluetooth.bluez.internal.events.TXPowerEvent;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the PropertiesChangedHandler subclass used by the binding to handle/dispatch property change events
 * from bluez.
 *
 * @author Benjamin Lafois - Initial contribution and API
 * @author Connor Petty - Code cleanup
 * @author Peter Rosenberg - Add support for ServiceData
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
            event.dispatch(listener);
        }
    }

    @Override
    public void handle(@Nullable PropertiesChanged properties) {
        if (properties == null || properties.getPropertiesChanged() == null) {
            logger.debug("Null properties. Skipping.");
            return;
        }
        Map<@Nullable String, @Nullable Variant<?>> changedProperties = properties.getPropertiesChanged();
        if (changedProperties == null) {
            logger.debug("Null properties changed. Skipping.");
            return;
        }

        // do this asynchronously so that we don't slow things down for the dbus event dispatcher
        scheduler.execute(() -> {

            String dbusPath = properties.getPath();
            changedProperties.forEach((key, variant) -> {
                if (key == null || variant == null) {
                    return;
                }
                switch (key.toLowerCase()) {
                    case "rssi":
                        // Signal Update
                        onRSSIUpdate(dbusPath, variant);
                        break;
                    case "txpower":
                        // TxPower
                        onTXPowerUpdate(dbusPath, variant);
                        break;
                    case "value":
                        // Characteristc value updated
                        onValueUpdate(dbusPath, variant);
                        break;
                    case "connected":
                        onConnectedUpdate(dbusPath, variant);
                        break;
                    case "name":
                        onNameUpdate(dbusPath, variant);
                        break;
                    case "alias":
                        // TODO
                        break;
                    case "manufacturerdata":
                        onManufacturerDataUpdate(dbusPath, variant);
                        break;
                    case "servicedata":
                        onServiceDataUpdate(dbusPath, variant);
                        break;
                    case "powered":
                        onPoweredUpdate(dbusPath, variant);
                        break;
                    case "discovering":
                        onDiscoveringUpdate(dbusPath, variant);
                        break;
                    case "servicesresolved":
                        onServicesResolved(dbusPath, variant);
                        break;
                }
            });

            logger.debug("PropertiesPath: {}", dbusPath);
            logger.debug("PropertiesChanged: {}", changedProperties);
        });
    }

    private void onDiscoveringUpdate(String dbusPath, Variant<?> variant) {
        Object discovered = variant.getValue();
        if (discovered instanceof Boolean) {
            notifyListeners(new AdapterDiscoveringChangedEvent(dbusPath, (boolean) discovered));
        }
    }

    private void onPoweredUpdate(String dbusPath, Variant<?> variant) {
        Object powered = variant.getValue();
        if (powered instanceof Boolean) {
            notifyListeners(new AdapterPoweredChangedEvent(dbusPath, (boolean) powered));
        }
    }

    private void onServicesResolved(String dbusPath, Variant<?> variant) {
        Object resolved = variant.getValue();
        if (resolved instanceof Boolean) {
            notifyListeners(new ServicesResolvedEvent(dbusPath, (boolean) resolved));
        }
    }

    private void onNameUpdate(String dbusPath, Variant<?> variant) {
        Object name = variant.getValue();
        if (name instanceof String) {
            notifyListeners(new NameEvent(dbusPath, (String) name));
        }
    }

    private void onTXPowerUpdate(String dbusPath, Variant<?> variant) {
        Object txPower = variant.getValue();
        if (txPower instanceof Short) {
            notifyListeners(new TXPowerEvent(dbusPath, (short) txPower));
        }
    }

    private void onConnectedUpdate(String dbusPath, Variant<?> variant) {
        Object connected = variant.getValue();
        if (connected instanceof Boolean) {
            notifyListeners(new ConnectedEvent(dbusPath, (boolean) connected));
        }
    }

    private void onManufacturerDataUpdate(String dbusPath, Variant<?> variant) {
        Map<Short, byte[]> eventData = new HashMap<>();

        Object map = variant.getValue();
        if (map instanceof DBusMap) {
            DBusMap<?, ?> dbm = (DBusMap<?, ?>) map;
            for (Map.Entry<?, ?> entry : dbm.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key instanceof UInt16 && value instanceof Variant<?>) {
                    value = ((Variant<?>) value).getValue();
                    if (value instanceof byte[]) {
                        eventData.put(((UInt16) key).shortValue(), ((byte[]) value));
                    }
                }
            }
        }
        if (!eventData.isEmpty()) {
            notifyListeners(new ManufacturerDataEvent(dbusPath, eventData));
        }
    }

    private void onServiceDataUpdate(String dbusPath, Variant<?> variant) {
        Map<String, byte[]> serviceData = new HashMap<>();

        Object map = variant.getValue();
        if (map instanceof DBusMap) {
            DBusMap<?, ?> dbm = (DBusMap<?, ?>) map;
            for (Map.Entry<?, ?> entry : dbm.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key instanceof String && value instanceof Variant<?>) {
                    value = ((Variant<?>) value).getValue();
                    if (value instanceof byte[]) {
                        serviceData.put(((String) key), ((byte[]) value));
                    }
                }
            }
        }
        if (!serviceData.isEmpty()) {
            notifyListeners(new ServiceDataEvent(dbusPath, serviceData));
        }
    }

    private void onValueUpdate(String dbusPath, Variant<?> variant) {
        Object value = variant.getValue();
        if (value instanceof byte[]) {
            notifyListeners(new CharacteristicUpdateEvent(dbusPath, (byte[]) value));
        }
    }

    private void onRSSIUpdate(String dbusPath, Variant<?> variant) {
        Object rssi = variant.getValue();
        if (rssi instanceof Short) {
            notifyListeners(new RssiEvent(dbusPath, (short) rssi));
        }
    }
}
