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
package org.openhab.binding.matter.internal.bridge.devices;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.MetadataRegistry;

/**
 * The {@link DeviceRegistry} is a registry of device types that are supported by the Matter Bridge service.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DeviceRegistry {
    private static final Map<String, Class<? extends BaseDevice>> DEVICE_TYPES = new HashMap<>();

    static {
        registerDevice("OnOffLight", OnOffLightDevice.class);
        registerDevice("OnOffPlugInUnit", OnOffPlugInUnitDevice.class);
        registerDevice("DimmableLight", DimmableLightDevice.class);
        registerDevice("Thermostat", ThermostatDevice.class);
        registerDevice("WindowCovering", WindowCoveringDevice.class);
        registerDevice("DoorLock", DoorLockDevice.class);
        registerDevice("TemperatureSensor", TemperatureSensorDevice.class);
        registerDevice("HumiditySensor", HumiditySensorDevice.class);
        registerDevice("OccupancySensor", OccupancySensorDevice.class);
        registerDevice("ContactSensor", ContactSensorDevice.class);
        registerDevice("ColorLight", ColorDevice.class);
        registerDevice("Fan", FanDevice.class);
        registerDevice("ModeSelect", ModeSelectDevice.class);
    }

    private static void registerDevice(String deviceType, Class<? extends BaseDevice> device) {
        DEVICE_TYPES.put(deviceType, device);
    }

    public static @Nullable BaseDevice createDevice(String deviceType, MetadataRegistry metadataRegistry,
            MatterBridgeClient client, GenericItem item) {
        Class<? extends BaseDevice> clazz = DEVICE_TYPES.get(deviceType);
        if (clazz != null) {
            try {
                Class<?>[] constructorParameterTypes = new Class<?>[] { MetadataRegistry.class,
                        MatterBridgeClient.class, GenericItem.class };
                Constructor<? extends BaseDevice> constructor = clazz.getConstructor(constructorParameterTypes);
                return constructor.newInstance(metadataRegistry, client, item);
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }
}
