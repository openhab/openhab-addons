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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The {@link LGThinQWMApiV1ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQWMApiV1ClientServiceImpl
        extends LGThinQAbstractApiV1ClientService<WasherDryerCapability, WasherDryerSnapshot>
        implements LGThinQWMApiClientService {
    private final Logger logger = LoggerFactory.getLogger(LGThinQWMApiV1ClientServiceImpl.class);

    protected LGThinQWMApiV1ClientServiceImpl(HttpClient httpClient) {
        super(WasherDryerCapability.class, WasherDryerSnapshot.class, httpClient);
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
    public WasherDryerSnapshot getDeviceData(String bridgeName, String deviceId, CapabilityDefinition capDef) {
        throw new UnsupportedOperationException("Method not supported in V1 API device.");
    }

    @Override
    public void remoteStart(String bridgeName, WasherDryerCapability cap, String deviceId, Map<String, Object> data)
            throws LGThinqApiException {
        try {
            CommandDefinition cmdStartDef = cap.getCommandsDefinition().get(cap.getCommandRemoteStart());
            if (cmdStartDef == null) {
                logger.warn("No command definition found for remote start v1. Ignoring command");
                return;
            }
            Map<String, Object> cmdPayload = prepareCommandV1(cmdStartDef, data);
            logger.debug("token Payload:[{}]", cmdPayload);
            RestResult result = sendCommand(bridgeName, deviceId, cmdPayload);
            handleGenericErrorResult(result);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending remote start", e);
        }
    }

    @Override
    public void wakeUp(String bridgeName, String deviceId, Boolean wakeUp) throws LGThinqApiException {
        try {
            RestResult result = sendCommand(bridgeName, deviceId, "", "Control", "Operation", "", "WakeUp");
            handleGenericErrorResult(result);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending remote start", e);
        }
    }

    @Override
    protected Map<String, Object> prepareCommandV1(CommandDefinition cmdDef, Map<String, Object> snapData)
            throws JsonProcessingException {
        // expected map ordered here
        String dataStr = cmdDef.getDataTemplate();
        for (Map.Entry<String, Object> e : snapData.entrySet()) {
            String value = String.valueOf(e.getValue());
            if ("Start".equals(cmdDef.getCmdOptValue()) && e.getKey().equals("Option2")) {
                // For some reason, option2 fills only InitialBit with 1.
                value = "1";
            }
            dataStr = dataStr.replace("{{" + e.getKey() + "}}", value);
        }
        // Keep the order
        LinkedHashMap<String, Object> cmd = completeCommandDataNodeV1(cmdDef, dataStr);
        cmd.remove("encode");

        return cmd;
    }
}
