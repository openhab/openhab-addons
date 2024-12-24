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
 * The {@link LGThinQApiClientService} - defines the basic methods to manage devices in the LG Cloud
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQApiClientService<C extends CapabilityDefinition, S extends SnapshotDefinition> {
    /**
     * List all devices registers in the LG Account
     * 
     * @param bridgeName bridge name
     * @return return a List off all devices registered for the user account.
     * @throws LGThinqApiException if some error occur accessing LG API
     */
    List<LGDevice> listAccountDevices(String bridgeName) throws LGThinqApiException;

    /**
     * Get the LG device metadata about the settings and capabilities of the Device
     * 
     * @param bridgeName bridge name
     * @param deviceId LG Device ID
     * @return A map containing all the device settings.
     * @throws LGThinqApiException
     */
    Map<String, Object> getDeviceSettings(String bridgeName, String deviceId) throws LGThinqApiException;

    void initializeDevice(String bridgeName, String deviceId) throws LGThinqApiException;

    /**
     * Retrieve actual data from device (its sensors and points states).
     *
     * @param deviceId device number
     * @param capDef Capabilities definition/settings of the device
     * @return return snapshot state of the device sensors and features
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     */
    @Nullable
    S getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) throws LGThinqApiException;

    /**
     * Turn on/off the device
     * 
     * @param bridgeName bridge name
     * @param deviceId LG device ID
     * @param newPowerState new Power State
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     */
    void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) throws LGThinqApiException;

    /**
     * Start the device Monitor responsible to open a window of data collection. (only used for V1 protocol)
     * 
     * @param bridgeName bridge name
     * @param deviceId LG device ID
     * @return string with the monitor ID
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     * @throws IOException if some error occur opening device's configuration files.
     */
    String startMonitor(String bridgeName, String deviceId) throws LGThinqApiException, IOException;

    /**
     * Get the capabilities of the device (got from device settings)
     * 
     * @param deviceId The LG device ID
     * @param uri the URL containing the XML descriptor of the device
     * @param forceRecreate If you want to recreate the cached file of the XML descriptor
     * @return the capability object related to the device
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     */
    C getCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException;

    /**
     * Build a default snapshot data of the device when it's offline, junto to keep data integrity in the channels
     * 
     * @return Default snapshot.
     */
    S buildDefaultOfflineSnapshot();

    /**
     * Load device capabilities from the cached file.
     * 
     * @param deviceId LG Thinq Device ID
     * @param uri if the file doesn't exist, get the content from registered URI and save locally.
     * @param forceRecreate force to recreate the file even if was previously saved locally
     * @return File pointing to the capability file
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     */
    File loadDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException;

    /**
     * Stop the monitor of data collection
     * 
     * @param bridgeName Bridge name
     * @param deviceId LG Device ID
     * @param workId name of the monitor
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     * @throws IOException if some error occur opening device's configuration files.
     */
    void stopMonitor(String bridgeName, String deviceId, String workId) throws LGThinqException, IOException;

    /**
     * Get data collected by the monitor
     * 
     * @param bridgeName Bridge name
     * @param deviceId LG Device ID
     * @param workerId monitor ID
     * @param deviceType Device Type related to the data collected
     * @param deviceCapability capabilities of the device
     * @return Snapshot of the device collected from LG API
     * @throws LGThinqApiException if some error is returned from LG API
     * @throws LGThinqDeviceV1MonitorExpiredException if the monitor is not valid anymore
     * @throws IOException if some IO error happen when accessing token cache file.
     * @throws LGThinqUnmarshallException if some error happen reading data collected from LG API
     */
    @Nullable
    S getMonitorData(String bridgeName, String deviceId, String workerId, DeviceTypes deviceType, C deviceCapability)
            throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException, LGThinqUnmarshallException;
}
