/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1MonitorExpiredException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqDeviceV1OfflineException;
import org.openhab.binding.lgthinq.internal.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACTargetTmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQACApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQACApiV2ClientServiceImpl extends
        LGThinQAbstractApiV2ClientService<ACCapability, ACCanonicalSnapshot> implements LGThinQACApiClientService {
    private static final LGThinQACApiClientService instance;
    private static final Logger logger = LoggerFactory.getLogger(LGThinQACApiV2ClientServiceImpl.class);

    static {
        instance = new LGThinQACApiV2ClientServiceImpl(ACCapability.class, ACCanonicalSnapshot.class);
    }

    protected LGThinQACApiV2ClientServiceImpl(Class<ACCapability> capabilityClass,
            Class<ACCanonicalSnapshot> snapshotClass) {
        super(capabilityClass, snapshotClass);
    }

    public static LGThinQACApiClientService getInstance() {
        return instance;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Operation", "airState.operation",
                    newPowerState.commandValue());
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting device power", e);
        }
    }

    @Override
    public void turnCoolJetMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.wMode.jet", modeOnOff);
    }

    public void turnAirCleanMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.wMode.airClean", modeOnOff);
    }

    public void turnAutoDryMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.miscFuncState.autoDry", modeOnOff);
    }

    public void turnEnergySavingMode(String bridgeName, String deviceId, String modeOnOff) throws LGThinqApiException {
        turnGenericMode(bridgeName, deviceId, "airState.powerSave.basic", modeOnOff);
    }

    protected void turnGenericMode(String bridgeName, String deviceId, String modeName, String modeOnOff)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Operation", modeName,
                    Integer.parseInt(modeOnOff));
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting cool jet mode", e);
        }
    }

    @Override
    public void changeOperationMode(String bridgeName, String deviceId, int newOpMode) throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.opMode", newOpMode);
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeFanSpeed(String bridgeName, String deviceId, int newFanSpeed) throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.windStrength",
                    newFanSpeed);
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    @Override
    public void changeTargetTemperature(String bridgeName, String deviceId, ACTargetTmp newTargetTemp)
            throws LGThinqApiException {
        try {
            RestResult resp = sendBasicControlCommands(bridgeName, deviceId, "Set", "airState.tempState.target",
                    newTargetTemp.commandValue());
            handleGenericErrorResult(resp);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error adjusting operation mode", e);
        }
    }

    /**
     * Start monitor data form specific device. This is old one, <b>works only on V1 API supported devices</b>.
     * 
     * @param deviceId Device ID
     * @return Work1 to be uses to grab data during monitoring.
     * @throws LGThinqApiException If some communication error occur.
     */
    @Override
    public String startMonitor(String bridgeName, String deviceId)
            throws LGThinqApiException, LGThinqDeviceV1OfflineException, IOException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    public void stopMonitor(String bridgeName, String deviceId, String workId)
            throws LGThinqApiException, RefreshTokenException, IOException, LGThinqDeviceV1OfflineException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    public @Nullable ACCanonicalSnapshot getMonitorData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull String workId, DeviceTypes deviceType, @NonNull ACCapability deviceCapability)
            throws LGThinqApiException, LGThinqDeviceV1MonitorExpiredException, IOException {
        throw new UnsupportedOperationException("Not supported in V2 API.");
    }

    @Override
    public void initializeDevice(@NonNull String bridgeName, @NonNull String deviceId) throws LGThinqApiException {
        super.initializeDevice(bridgeName, deviceId);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId)
            throws LGThinqApiException {
        try {
            RestResult resp = sendControlCommands(bridgeName, deviceId, "control", "allEventEnable", "Set",
                    "airState.mon.timeout", "70");
            handleGenericErrorResult(resp);
        } catch (Exception e) {
            logger.debug("Can't execute Before Update command", e);
        }
    }

    @Override
    public double getInstantPowerConsumption(@NonNull String bridgeName, @NonNull String deviceId)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not supporte for this device");
    }
}
