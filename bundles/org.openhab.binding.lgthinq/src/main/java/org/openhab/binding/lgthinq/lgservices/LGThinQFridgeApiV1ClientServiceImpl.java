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

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.RE_SET_CONTROL_COMMAND_NAME_V1;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.fridge.FridgeCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQFridgeApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQFridgeApiV1ClientServiceImpl
        extends LGThinQAbstractApiV1ClientService<FridgeCapability, FridgeCanonicalSnapshot>
        implements LGThinQFridgeApiClientService {
    private final Logger logger = LoggerFactory.getLogger(LGThinQFridgeApiV1ClientServiceImpl.class);

    protected LGThinQFridgeApiV1ClientServiceImpl(HttpClient httpClient) {
        super(FridgeCapability.class, FridgeCanonicalSnapshot.class, httpClient);
    }

    @Override
    protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
        // there's no before settings to send command
        return false;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    @Nullable
    public FridgeCanonicalSnapshot getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) {
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }

    @Override
    public void setFridgeTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException {
        if (snapCmdData != null) {
            snapCmdData.put("TempRefrigerator", targetTemperatureIndex);
            setControlCommand(bridgeId, deviceId, fridgeCapability, snapCmdData);
        } else {
            logger.warn("Snapshot Command Data is null");
        }
    }

    @Override
    public void setFreezerTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException {
        if (snapCmdData != null) {
            snapCmdData.put("TempFreezer", targetTemperatureIndex);
            setControlCommand(bridgeId, deviceId, fridgeCapability, snapCmdData);
        } else {
            logger.warn("Snapshot command is null");
        }
    }

    @Override
    public void setExpressMode(String bridgeId, String deviceId, String expressModeIndex) {
        throw new UnsupportedOperationException("V1 Fridge doesn't support ExpressMode feature. It mostly like a bug");
    }

    @Override
    public void setExpressCoolMode(String bridgeId, String deviceId, boolean trueOnFalseOff) {
        throw new UnsupportedOperationException(
                "V1 Fridge doesn't support ExpressCoolMode feature. It mostly like a bug");
    }

    @Override
    public void setEcoFriendlyMode(String bridgeId, String deviceId, boolean trueOnFalseOff) {
        throw new UnsupportedOperationException(
                "V1 Fridge doesn't support ExpressCoolMode feature. It mostly like a bug");
    }

    @Override
    public void setIcePlus(String bridgeId, String deviceId, FridgeCapability fridgeCapability, boolean trueOnFalseOff,
            Map<String, Object> snapCmdData) throws LGThinqApiException {
        snapCmdData.put("IcePlus", trueOnFalseOff ? 1 : 0);
        setControlCommand(bridgeId, deviceId, fridgeCapability, snapCmdData);
    }

    private void setControlCommand(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            @Nullable Map<String, Object> snapCmdData) throws LGThinqApiException {
        try {
            CommandDefinition cmdSetControlDef = fridgeCapability.getCommandsDefinition()
                    .get(RE_SET_CONTROL_COMMAND_NAME_V1);
            if (cmdSetControlDef == null) {
                logger.warn("No command definition found for set control command. Ignoring command");
                return;
            }
            if (snapCmdData == null) {
                logger.error("Snapshot to complete command was not send. It's mostly like a bug");
                return;
            }
            Map<String, Object> cmdPayload = prepareCommandV1(cmdSetControlDef, snapCmdData);
            logger.debug("setControl Payload:[{}]", cmdPayload);
            RestResult result = sendCommand(bridgeId, deviceId, cmdPayload);
            handleGenericErrorResult(result);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending remote start", e);
        }
    }
}
