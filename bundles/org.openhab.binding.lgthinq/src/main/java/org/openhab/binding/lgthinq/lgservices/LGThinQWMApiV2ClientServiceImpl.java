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

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.WMD_COMMAND_REMOTE_START_V2;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The {@link LGThinQWMApiV2ClientServiceImpl}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQWMApiV2ClientServiceImpl
        extends LGThinQAbstractApiV2ClientService<WasherDryerCapability, WasherDryerSnapshot>
        implements LGThinQWMApiClientService {

    protected LGThinQWMApiV2ClientServiceImpl(HttpClient httpClient) {
        super(WasherDryerCapability.class, WasherDryerSnapshot.class, httpClient);
    }

    @Override
    protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
        return false;
    }

    @Override
    public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void remoteStart(String bridgeName, WasherDryerCapability cap, String deviceId, Map<String, Object> data)
            throws LGThinqApiException {
        try {
            ObjectNode dataSetList = JsonNodeFactory.instance.objectNode();
            ObjectNode nodeData = dataSetList.putObject("dataSetList").putObject("washerDryer");
            // 1 - mount nodeData template
            CommandDefinition cdStart = cap.getCommandsDefinition().get(WMD_COMMAND_REMOTE_START_V2);
            if (cdStart == null) {
                throw new LGThinqApiException(
                        "Command WMStart doesn't defined in cap. Do the Device support Remote Start ?");
            }
            // remove data values (based on command template values) that it's not the real name
            data.remove("course");
            data.remove("SmartCourse");
            for (Map.Entry<String, Object> value : data.entrySet()) {
                Object v = value.getValue();
                if (v instanceof Double d) {
                    nodeData.put(value.getKey(), d);
                } else if (v instanceof Integer i) {
                    nodeData.put(value.getKey(), i);
                } else {
                    nodeData.put(value.getKey(), value.getValue().toString());
                }
            }

            RestResult result = sendCommand(bridgeName, deviceId, "control-sync", WMD_COMMAND_REMOTE_START_V2, "Set",
                    null, null, dataSetList);
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
            ObjectNode dataSetList = JsonNodeFactory.instance.objectNode();
            dataSetList.putObject("dataSetList").putObject("washerDryer").put("controlDataType", "WAKEUP")
                    .put("controlDataValueLength", wakeUp ? "1" : "0");

            RestResult result = sendCommand(bridgeName, deviceId, "control-sync", "WMWakeup", "Set", null, null,
                    dataSetList);
            handleGenericErrorResult(result);
        } catch (LGThinqApiException e) {
            throw e;
        } catch (Exception e) {
            throw new LGThinqApiException("Error sending remote start", e);
        }
    }
}
