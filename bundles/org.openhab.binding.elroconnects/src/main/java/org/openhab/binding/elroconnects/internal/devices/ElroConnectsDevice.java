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
package org.openhab.binding.elroconnects.internal.devices;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceStatus;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceType;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.binding.elroconnects.internal.util.ElroConnectsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsDevice} is an abstract class representing all basic properties for ELRO Connects devices.
 * Concrete subclasses will contain specific logic for each device type.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public abstract class ElroConnectsDevice {

    private final Logger logger = LoggerFactory.getLogger(ElroConnectsDevice.class);

    // minimum data to create an instance of the class
    protected int deviceId;
    protected ElroConnectsBridgeHandler bridge;

    protected volatile String deviceName = "";
    protected volatile String deviceType = "";
    protected volatile String deviceStatus = "";

    protected volatile Map<String, ElroDeviceStatus> statusMap = Map.of();

    /**
     * Create a new instance of a subclass of {@link ElroConnectsDevice}. These instances get created by an instance
     * {@link ElroConnectsBridgeHandler}. The deviceId will be set on creation. Other fields will be set as and when the
     * information is received from the K1 hub.
     *
     * @param deviceId
     * @param bridge
     */
    public ElroConnectsDevice(int deviceId, ElroConnectsBridgeHandler bridge) {
        this.deviceId = deviceId;
        this.bridge = bridge;
    }

    /**
     * Get the current status of the device.
     *
     * @return status
     */
    protected ElroDeviceStatus getStatus() {
        String deviceStatus = this.deviceStatus;
        ElroDeviceStatus elroStatus = ElroDeviceStatus.UNDEF;

        if (deviceStatus.length() >= 6) {
            elroStatus = statusMap.getOrDefault(deviceStatus.substring(4, 6), ElroDeviceStatus.UNDEF);
        }

        return elroStatus;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void updateDeviceName(String deviceName) {
        try {
            if (!ElroConnectsUtil.equals(getDeviceName(), deviceName, 15)) {
                bridge.renameDevice(deviceId, deviceName);
                setDeviceName(deviceName);
            }
        } catch (IOException e) {
            logger.debug("Failed to update device name: {}", e.getMessage());
        }
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDeviceName() {
        String typeName = null;
        ElroDeviceType type = TYPE_MAP.get(getDeviceType());
        if (type != null) {
            typeName = TYPE_NAMES.get(type);
        }
        if (typeName == null) {
            typeName = getDeviceType();
        }

        return deviceName.isEmpty() ? typeName + "-" + deviceId : deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Retrieve the {@link ElroConnectsDeviceHandler} for device.
     *
     * @return handler for the device.
     */
    protected @Nullable ElroConnectsDeviceHandler getHandler() {
        return bridge.getDeviceHandler(deviceId);
    }

    /**
     * Update all {@link ElroConnectsDeviceHandler} channel states with information received from the device. This
     * method needs to be implemented in the concrete subclass when any state updates are received from the device.
     */
    public abstract void updateState();

    /**
     * Send alarm test message to the device. This method is called from the {@link ElroConnectsDeviceHandler}. The
     * method needs to be implemented in the concrete subclass when test alarms are supported.
     */
    public abstract void testAlarm();

    /**
     * Send alarm mute message to the device. This method is called from the {@link ElroConnectsDeviceHandler}. The
     * method needs to be implemented in the concrete subclass when alarm muting is supported.
     */
    public abstract void muteAlarm();

    /**
     * Send state switch message to the device. This method is called from the {@link ElroConnectsDeviceHandler}. The
     * method needs to be implemented in the concrete subclass when switching the state on/off is supported.
     */
    public abstract void switchState(boolean state);
}
