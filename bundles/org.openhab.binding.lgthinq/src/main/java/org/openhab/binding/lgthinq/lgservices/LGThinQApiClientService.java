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
 * The {@link LGThinQApiClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGThinQApiClientService<C extends CapabilityDefinition, S extends SnapshotDefinition> {

    List<LGDevice> listAccountDevices(String bridgeName) throws LGThinqApiException;

    Map<String, Object> getDeviceSettings(String bridgeName, String deviceId) throws LGThinqApiException;

    void initializeDevice(String bridgeName, String deviceId) throws LGThinqApiException;

    /**
     * Retrieve actual data from device (its sensors and points states).
     *
     * @param deviceId device number
     * @param capDef
     * @return return snapshot state of the device
     * @throws LGThinqApiException if some error interacting with LG API Server occur.
     */
    @Nullable
    S getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) throws LGThinqApiException;

    void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) throws LGThinqApiException;

    String startMonitor(String bridgeName, String deviceId) throws LGThinqApiException, IOException;

    C getCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException;

    S buildDefaultOfflineSnapshot();

    File loadDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGThinqApiException;

    void stopMonitor(String bridgeName, String deviceId, String workId) throws LGThinqException, IOException;

    @Nullable
    S getMonitorData(String bridgeName, String deviceId, String workerId, DeviceTypes deviceType, C deviceCapability)
            throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException, LGThinqUnmarshallException;
}
