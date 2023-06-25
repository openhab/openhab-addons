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

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

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
    private static final LGThinQWMApiClientService instance;
    static {
        instance = new LGThinQWMApiV1ClientServiceImpl(WasherDryerCapability.class, WasherDryerSnapshot.class);
    }

    protected LGThinQWMApiV1ClientServiceImpl(Class<WasherDryerCapability> capabilityClass,
            Class<WasherDryerSnapshot> snapshotClass) {
        super(capabilityClass, snapshotClass);
    }

    @Override
    protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) {
        // Nothing to do for V1 thinq
    }

    public static LGThinQWMApiClientService getInstance() {
        return instance;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState)
            throws LGThinqApiException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    @Nullable
    public WasherDryerSnapshot getDeviceData(@NonNull String bridgeName, @NonNull String deviceId,
            @NonNull CapabilityDefinition capDef) throws LGThinqApiException {
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

    private Map<String, Object> prepareCommandV1(CommandDefinition cmdDef, Map<String, Object> snapData)
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
        LinkedHashMap<String, Object> cmd = objectMapper.readValue(cmdDef.getRawCommand(), new TypeReference<>() {
        });
        cmd.remove("encode"); // remove encode node in the raw command to be similar to LG App.

        logger.debug("Prepare command v1: {}", dataStr);
        if (cmdDef.isBinary()) {
            cmd.put("format", "B64");
            List<Integer> list = objectMapper.readValue(dataStr, new TypeReference<>() {
            });
            // convert the list of integer to a bytearray
            byte[] bytes = ArrayUtils.toPrimitive(list.stream().map(Integer::byteValue).toArray(Byte[]::new));
            String str_data_encoded = new String(Base64.getEncoder().encode(bytes));
            cmd.put("data", str_data_encoded);
        } else {
            cmd.put("data", dataStr);
        }

        return cmd;
    }
}
