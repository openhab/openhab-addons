/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.openhab.binding.lgthinq.errors.LGApiException;
import org.openhab.binding.lgthinq.errors.LGDeviceV1OfflineException;
import org.openhab.binding.lgthinq.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgapi.model.*;

/**
 * The {@link LGApiClientService}
 *
 * @author Nemer Daud - Initial contribution
 */
public interface LGApiClientService {

    public List<LGDevice> listAccountDevices() throws LGApiException;

    public Map<String, Object> getDeviceSettings(String deviceId) throws LGApiException;

    /**
     * Retrieve actual data from device (its sensors and points states).
     * 
     * @param deviceId device number
     * @return return snapshot state of the device
     * @throws LGApiException if some error interacting with LG API Server occur.
     */
    public ACSnapShot getAcDeviceData(String deviceId) throws LGApiException;

    public boolean turnDevicePower(String deviceId, DevicePowerState newPowerState) throws LGApiException;

    public boolean changeOperationMode(String deviceId, ACOpMode newOpMode) throws LGApiException;

    public boolean changeFanSpeed(String deviceId, ACFanSpeed newFanSpeed) throws LGApiException;

    public boolean changeTargetTemperature(String deviceId, ACTargetTmp newTargetTemp) throws LGApiException;

    public String startMonitor(String deviceId) throws LGApiException, LGDeviceV1OfflineException, IOException;

    public void stopMonitor(String deviceId, String workId)
            throws LGThinqException, LGDeviceV1OfflineException, IOException;

    public ACSnapShot getMonitorData(String deviceId, String workerId) throws LGApiException, IOException;
}
