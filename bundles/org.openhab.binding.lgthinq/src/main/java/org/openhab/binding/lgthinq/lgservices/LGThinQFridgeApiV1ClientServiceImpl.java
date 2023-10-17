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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.FR_SET_CONTROL_COMMAND_NAME_V1;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
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
    private static final Logger logger = LoggerFactory.getLogger(LGThinQFridgeApiV1ClientServiceImpl.class);

    protected LGThinQFridgeApiV1ClientServiceImpl(HttpClient httpClient) {
        super(FridgeCapability.class, FridgeCanonicalSnapshot.class, httpClient);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // Nothing to do for V1 thinq
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    @Nullable
    public FridgeCanonicalSnapshot getDeviceData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull CapabilityDefinition capDef) throws LGThinqApiException {
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }

    @Override
    public void setFridgeTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException {
        assert snapCmdData != null;
        snapCmdData.put("TempRefrigerator", targetTemperatureIndex);
        setTemperature(bridgeId, deviceId, fridgeCapability, snapCmdData);
    }

    @Override
    public void setFreezerTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            Integer targetTemperatureIndex, String tempUnit, @Nullable Map<String, Object> snapCmdData)
            throws LGThinqApiException {
        assert snapCmdData != null;
        snapCmdData.put("TempFreezer", targetTemperatureIndex);
        setTemperature(bridgeId, deviceId, fridgeCapability, snapCmdData);
    }

    private void setTemperature(String bridgeId, String deviceId, FridgeCapability fridgeCapability,
            @Nullable Map<String, Object> snapCmdData) throws LGThinqApiException {
        try {
            CommandDefinition cmdSetControlDef = fridgeCapability.getCommandsDefinition()
                    .get(FR_SET_CONTROL_COMMAND_NAME_V1);
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
