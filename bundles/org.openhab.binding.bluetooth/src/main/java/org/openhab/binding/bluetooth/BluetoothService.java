/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link BluetoothCharacteristic} class defines the BLE Service.
 * <p>
 * Services are collections of characteristics and relationships to other services that encapsulate the behavior of part
 * of a device.
 * <p>
 * https://www.bluetooth.com/specifications/gatt/services
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Cleaned up code
 */
public class BluetoothService {

    // The service UUID
    private final UUID uuid;

    /**
     * The start handle for this service
     */
    private final int handleStart;

    /**
     * The end handle for this service
     */
    private final int handleEnd;

    protected int instanceId;

    /**
     * Indicates if this is a primary service (true) or secondary service (false)
     */
    protected boolean primaryService;

    /**
     * Map of {@link BluetoothCharacteristic}s supported in this service
     */
    protected final Map<UUID, BluetoothCharacteristic> supportedCharacteristics = new ConcurrentHashMap<>();

    /**
     * Constructor
     *
     * @param uuid the uuid of the service
     */
    public BluetoothService(UUID uuid) {
        this(uuid, true, 0, 0);
    }

    /**
     * Constructor
     *
     * @param uuid the uuid of the service
     * @param primaryService true, if this service is a primary service
     */
    public BluetoothService(UUID uuid, boolean primaryService) {
        this(uuid, primaryService, 0, 0);
    }

    /**
     * Constructor
     *
     * @param uuid the uuid of the service
     * @param primaryService true, if this service is a primary service
     * @param handleStart id of the lowest handle
     * @param handleEnd id of the highest handle
     */
    public BluetoothService(UUID uuid, boolean primaryService, int handleStart, int handleEnd) {
        this.uuid = uuid;
        this.primaryService = primaryService;
        this.handleStart = handleStart;
        this.handleEnd = handleEnd;
    }

    /**
     * Get characteristic based on {@link UUID}
     *
     * @return the {@link BluetoothCharacteristic} with the requested {@link UUID}
     */
    public BluetoothCharacteristic getCharacteristic(UUID uuid) {
        return supportedCharacteristics.get(uuid);
    }

    /**
     * Get list of characteristics of the service
     *
     * @return the list of {@link BluetoothCharacteristic}s
     */
    public List<BluetoothCharacteristic> getCharacteristics() {
        return new ArrayList<>(supportedCharacteristics.values());
    }

    /**
     * Return the UUID of this service
     *
     * @return the {@link UUID} of the service
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the starting handle for this service
     *
     * @return the start handle
     */
    public int getHandleStart() {
        return handleStart;
    }

    /**
     * Gets the end handle for this service
     *
     * @return the end handle
     */
    public int getHandleEnd() {
        return handleEnd;
    }

    /**
     * Get the type of this service (primary/secondary)
     *
     * @return true if this is a primary service
     */
    public boolean isPrimary() {
        return primaryService;
    }

    /**
     * Returns the instance ID for this service
     *
     * @return Instance ID of this service
     */
    public int getInstanceId() {
        return instanceId;
    }

    /**
     * Checks if the service provides a specific characteristic
     *
     * @return true if the characteristic is provided in this service
     */
    public boolean providesCharacteristic(UUID uuid) {
        return supportedCharacteristics.containsKey(uuid);
    }

    /**
     * Add a characteristic to this service
     *
     * @param characteristic The characteristics to be added
     * @return true, if the characteristic was added to the service
     */
    public boolean addCharacteristic(BluetoothCharacteristic characteristic) {
        if (supportedCharacteristics.get(characteristic.getUuid()) != null) {
            return false;
        }

        supportedCharacteristics.put(characteristic.getUuid(), characteristic);
        characteristic.setService(this);
        return true;
    }

    /**
     * Gets a characteristic by the handle
     *
     * @param handle the handle of the characteristic to return
     * @return return the {@link BluetoothCharacteristic} or null if not found
     */
    public BluetoothCharacteristic getCharacteristicByHandle(int handle) {
        synchronized (supportedCharacteristics) {
            for (BluetoothCharacteristic characteristic : supportedCharacteristics.values()) {
                if (characteristic.getHandle() == handle) {
                    return characteristic;
                }
            }
        }
        return null;
    }

    /**
     * Gets the {@link GattService} for this service. This is an enum defining the available GATT services.
     *
     * @return the {@link GattService} relating to this service
     */
    public GattService getService() {
        return GattService.getService(uuid);
    }

    public enum GattService {

        // List of GATT Services
        ALERT_NOTIFICATION_SERVICE(0x1811),
        AUTOMATION_IO(0x1815),
        BATTERY_SERVICE(0x180F),
        BLOOD_PRESSURE(0x1810),
        BODY_COMPOSITION(0x181B),
        BOND_MANAGEMENT(0x181E),
        CONTINUOUS_GLUCOSE_MONITORING(0x181F),
        CURRENT_TIME_SERVICE(0x1805),
        CYCLING_POWER(0x1818),
        CYCLING_SPEED_AND_CADENCE(0x1816),
        DEVICE_INFORMATION(0x180A),
        ENVIRONMENTAL_SENSING(0x181A),
        GENERIC_ACCESS(0x1800),
        GENERIC_ATTRIBUTE(0x1801),
        GLUCOSE(0x1808),
        HEALTH_THERMOMETER(0x1809),
        HEART_RATE(0x180D),
        HTTP_PROXY(0x1823),
        HUMAN_INTERFACE_DEVICE(0x1812),
        IMMEDIATE_ALERT(0x1802),
        INDOOR_POSITIONING(0x1821),
        INTERNET_PROTOCOL_SUPPORT(0x1820),
        LINK_LOSS(0x1803L),
        LOCATION_AND_NAVIGATION(0x1819),
        NEXT_DST_CHANGE_SERVICE(0x1807),
        PHONE_ALERT_STATUS_SERVICE(0x180E),
        REFERENCE_TIME_UPDATE_SERVICE(0x1806),
        RUNNING_SPEED_AND_CADENCE(0x1814),
        SCAN_PARAMETERS(0x1813),
        TX_POWER(0x1804),
        USER_DATA(0x181C),
        WEIGHT_SCALE(0x181D);

        private static Map<UUID, GattService> uuidToServiceMapping;

        private UUID uuid;

        private GattService(long key) {
            this.uuid = BluetoothBindingConstants.createBluetoothUUID(key);
        }

        private static void initMapping() {
            uuidToServiceMapping = new HashMap<>();
            for (GattService s : values()) {
                uuidToServiceMapping.put(s.uuid, s);
            }
        }

        public static GattService getService(UUID uuid) {
            if (uuidToServiceMapping == null) {
                initMapping();
            }
            return uuidToServiceMapping.get(uuid);
        }

        /**
         * @return the key
         */
        public UUID getUUID() {
            return uuid;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BluetoothService [uuid=");
        builder.append(uuid);
        builder.append(", handleStart=");
        builder.append(handleStart);
        builder.append(", handleEnd=");
        builder.append(handleEnd);
        builder.append(']');
        return builder.toString();
    }
}
