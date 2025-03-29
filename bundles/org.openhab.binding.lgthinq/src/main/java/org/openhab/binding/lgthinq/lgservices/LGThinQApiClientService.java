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
package org.openhab.binding.lgthinq.lgservices;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqUnmarshallException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.SnapshotDefinition;

/**
 * The {@link LGThinQApiClientService} interface defines the core methods for managing LG ThinQ devices
 * via the LG Cloud API. It provides functionalities for retrieving device metadata, controlling power states,
 * monitoring device status, and handling device capabilities.
 *
 * @param <C> The type representing the capability definition for a device.
 * @param <S> The type representing a snapshot definition of device data.
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQApiClientService<C extends CapabilityDefinition, S extends SnapshotDefinition> {

    /**
     * Retrieves a list of all devices registered under the LG account.
     *
     * @param bridgeName The name of the bridge managing the devices.
     * @return A list of {@link LGDevice} representing the registered devices.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    List<LGDevice> listAccountDevices(String bridgeName) throws LGThinqApiException;

    /**
     * Retrieves device metadata, including settings and capabilities.
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @return A map containing device settings and metadata.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    Map<String, Object> getDeviceSettings(String bridgeName, String deviceId) throws LGThinqApiException;

    /**
     * Initializes a device, preparing it for interaction.
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    void initializeDevice(String bridgeName, String deviceId) throws LGThinqApiException;

    /**
     * Retrieves the latest data snapshot from the device, including sensor readings and state values.
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @param capDef The capability definition of the device.
     * @return A snapshot containing the device's current data.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    @Nullable
    S getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) throws LGThinqApiException;

    /**
     * Toggles the power state of the device (on/off).
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @param newPowerState The desired power state.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) throws LGThinqApiException;

    /**
     * Starts a monitoring session for data collection (only applicable for protocol V1).
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @return The monitor session ID.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     * @throws IOException If an error occurs while handling device configuration files.
     */
    String startMonitor(String bridgeName, String deviceId) throws LGThinqApiException, IOException;

    /**
     * Retrieves the capability definition of the device.
     *
     * @param deviceId The unique ID of the LG ThinQ device.
     * @param uri The URI containing the XML descriptor of the device.
     * @param forceRecreate Whether to force recreation of the cached capability file.
     * @return The capability definition of the device.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    C getCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException;

    /**
     * Builds a default snapshot to maintain data integrity when the device is offline.
     *
     * @return A default snapshot representing offline device data.
     */
    S buildDefaultOfflineSnapshot();

    /**
     * Loads device capabilities from a cached file or retrieves them from the API if necessary.
     *
     * @param deviceId The unique ID of the LG ThinQ device.
     * @param uri The URI used to retrieve capability data if the file is missing.
     * @param forceRecreate Whether to force recreation of the cached file.
     * @return A file containing the device capability data.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     */
    File loadDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException;

    /**
     * Stops a previously started monitoring session.
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @param workId The monitor session ID.
     * @throws LGThinqException If an error occurs while stopping the monitor.
     * @throws IOException If an error occurs while handling device configuration files.
     */
    void stopMonitor(String bridgeName, String deviceId, String workId) throws LGThinqException, IOException;

    /**
     * Retrieves data collected from an active monitoring session.
     *
     * @param bridgeName The name of the bridge managing the device.
     * @param deviceId The unique ID of the LG ThinQ device.
     * @param workerId The monitoring session ID.
     * @param deviceType The type of device being monitored.
     * @param deviceCapability The capability definition of the device.
     * @return A snapshot containing the collected data.
     * @throws LGThinqApiException If an error occurs while accessing the LG API.
     * @throws LGThinqDeviceV1MonitorExpiredException If the monitoring session has expired.
     * @throws IOException If an error occurs while accessing cached token files.
     * @throws LGThinqUnmarshallException If an error occurs while parsing collected data.
     */
    @Nullable
    S getMonitorData(String bridgeName, String deviceId, String workerId, DeviceTypes deviceType, C deviceCapability)
            throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException, LGThinqUnmarshallException;
}
