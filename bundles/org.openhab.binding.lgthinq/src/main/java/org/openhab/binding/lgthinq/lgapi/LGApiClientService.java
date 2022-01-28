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
package org.openhab.binding.lgthinq.lgapi;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.errors.LGApiException;
import org.openhab.binding.lgthinq.internal.errors.LGDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.internal.errors.LGDeviceV1OfflineException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgapi.model.*;

/**
 * The {@link LGApiClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface LGApiClientService {

    List<LGDevice> listAccountDevices(String bridgeName) throws LGApiException;

    Map<String, Object> getDeviceSettings(String bridgeName, String deviceId) throws LGApiException;

    /**
     * Retrieve actual data from device (its sensors and points states).
     * 
     * @param deviceId device number
     * @return return snapshot state of the device
     * @throws LGApiException if some error interacting with LG API Server occur.
     */
    @Nullable
    ACSnapShot getAcDeviceData(@NonNull String bridgeName, @NonNull String deviceId) throws LGApiException;

    void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) throws LGApiException;

    void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGApiException;

    void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGApiException;

    void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp) throws LGApiException;

    String startMonitor(String bridgeName, String deviceId)
            throws LGApiException, LGDeviceV1OfflineException, IOException;

    ACCapability getDeviceCapability(String deviceId, String uri, boolean forceRecreate) throws LGApiException;

    void stopMonitor(String bridgeName, String deviceId, String workId) throws LGThinqException, IOException;

    @Nullable
    ACSnapShot getMonitorData(@NonNull String bridgeName, @NonNull String deviceId, @NonNull String workerId)
            throws LGApiException, LGDeviceV1MonitorExpiredException, IOException;
}
