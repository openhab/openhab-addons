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
        Map<String, Variant<?>> changedProperties = properties.getPropertiesChanged();
        if (changedProperties == null) {
            logger.debug("Null properties changed. Skipping.");
            return;
        }

        // do this asynchronously so that we don't slow things down for the dbus event dispatcher
        scheduler.execute(() -> {

            String dbusPath = properties.getPath();
            changedProperties.forEach((key, variant) -> {
                switch (key) {
                    case "RSSI":
                        // Signal Update
                        notifyListeners(new RssiEvent(dbusPath, (Short) variant.getValue()));
                        break;
                    case "TxPower":
                        // TxPower
                        notifyListeners(new TXPowerEvent(dbusPath, (Short) variant.getValue()));
                        break;
                    case "Value":
                        // Characteristc value updated
                        notifyListeners(new CharacteristicUpdateEvent(dbusPath, (byte[]) variant.getValue()));
                        break;
                    case "Connected":
                        notifyListeners(new ConnectedEvent(dbusPath, (boolean) variant.getValue()));
                        break;
                    case "Name":
                        notifyListeners(new NameEvent(dbusPath, (String) variant.getValue()));
                        break;
                    case "Alias":
                        // TODO
                        break;
                    case "ManufacturerData":
                        notifyListeners(new ManufacturerDataEvent(dbusPath, getManufacturerData(variant)));
                        break;
                    case "Powered":
                        notifyListeners(new AdapterPoweredChangedEvent(dbusPath, (boolean) variant.getValue()));
                        break;
                    case "Discovering":
                        notifyListeners(new AdapterDiscoveringChangedEvent(dbusPath, (boolean) variant.getValue()));
                        break;
                    case "ServicesResolved":
                        notifyListeners(new ServicesResolvedEvent(dbusPath, (boolean) variant.getValue()));
                        break;
                }
            });

            logger.debug("PropertiesPath: {}", dbusPath);
            logger.debug("PropertiesChanged: {}", changedProperties);
        });
    }

    @SuppressWarnings("unchecked")
    private Map<Short, byte[]> getManufacturerData(Variant<?> v) {
        Map<Short, byte[]> eventData = new HashMap<>();
        DBusMap<UInt16, Variant<?>> dbm = (DBusMap<UInt16, Variant<?>>) v.getValue();
        for (Map.Entry<UInt16, Variant<?>> entry : dbm.entrySet()) {
            byte[] bytes = (byte[]) entry.getValue().getValue();
            eventData.put(entry.getKey().shortValue(), bytes);
        }
        return eventData;
    }
}
