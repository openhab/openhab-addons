/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private void onRSSIUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Short rssi) {
            notifyListeners(new RssiEvent(dbusPath, rssi));
        }
    }

    private void onDiscoveringUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Boolean discovered) {
            notifyListeners(new AdapterDiscoveringChangedEvent(dbusPath, discovered));
        }
    }

    private void onPoweredUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Boolean powered) {
            notifyListeners(new AdapterPoweredChangedEvent(dbusPath, powered));
        }
    }

    private void onServicesResolved(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Boolean resolved) {
            notifyListeners(new ServicesResolvedEvent(dbusPath, resolved));
        }
    }

    private void onNameUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof String name) {
            notifyListeners(new NameEvent(dbusPath, name));
        }
    }

    private void onTXPowerUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Short txPower) {
            notifyListeners(new TXPowerEvent(dbusPath, txPower));
        }
    }

    private void onConnectedUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Boolean connected) {
            notifyListeners(new ConnectedEvent(dbusPath, connected));
        }
    }

    private void onManufacturerDataUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Map<?, ?> map) {
            Map<Short, byte[]> eventData = new HashMap<>();

            map.forEach((key, value) -> {
                if (key instanceof UInt16 iKey && value instanceof Variant<?> vValue
                        && vValue.getValue() instanceof List<?> byteList && !byteList.isEmpty()
                        && byteList.get(0) instanceof Byte) {
                    eventData.put(iKey.shortValue(), toByteArray(byteList));
                }
            });

            if (!eventData.isEmpty()) {
                notifyListeners(new ManufacturerDataEvent(dbusPath, eventData));
            }
        }
    }

    private void onServiceDataUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof Map<?, ?> map) {
            Map<String, byte[]> serviceData = new HashMap<>();

            map.forEach((key, value) -> {
                if (key instanceof String sKey && value instanceof Variant<?> vValue
                        && vValue.getValue() instanceof List<?> byteList && !byteList.isEmpty()
                        && byteList.get(0) instanceof Byte) {
                    serviceData.put(sKey, toByteArray(byteList));
                }
            });

            if (!serviceData.isEmpty()) {
                notifyListeners(new ServiceDataEvent(dbusPath, serviceData));
            }
        }
    }

    private void onValueUpdate(String dbusPath, Variant<?> variant) {
        if (variant.getValue() instanceof List<?> byteList && !byteList.isEmpty() && byteList.get(0) instanceof Byte) {
            notifyListeners(new CharacteristicUpdateEvent(dbusPath, toByteArray(byteList)));
        }
    }

    private static byte[] toByteArray(List<?> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            bytes[i] = (element instanceof Byte b) ? b : 0;
        }
        return bytes;
    }
}
