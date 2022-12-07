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
package org.openhab.binding.velux.internal.bridge.json;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.GetDeviceStatus;
import org.openhab.binding.velux.internal.things.VeluxGwState;
import org.openhab.binding.velux.internal.things.VeluxGwState.VeluxGatewayState;
import org.openhab.binding.velux.internal.things.VeluxGwState.VeluxGatewaySubState;

/**
 * Specific bridge communication message supported by the Velux bridge.
 * <P>
 * Message semantic: Retrieval of current bridge state.
 * <P>
 *
 * It defines information how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the {@link org.openhab.binding.velux.internal.bridge.json.JsonBridgeCommunicationProtocol
 * BridgeCommunicationProtocol}.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class JCgetDeviceStatus extends GetDeviceStatus implements JsonBridgeCommunicationProtocol {

    private static final String URL = "/api/v1/device";
    private static final String DESCRIPTION = "get device status";

    private Request request = new Request();
    private Response response = new Response();

    /*
     * Message Objects
     */

    /**
     * Bridge I/O Request message used by {@link org.openhab.binding.velux.internal.bridge.json.JsonVeluxBridge
     * JsonVeluxBridge} for serializing.
     *
     * Resulting JSON:
     *
     * <pre>
     * {"action":"getDeviceStatus","params":{}}
     * </pre>
     *
     * NOTE: the gateway software is extremely sensitive to this exact JSON structure.
     * Any modifications (like omitting empty params) will lead to a gateway error.
     */
    private static class Request {

        @SuppressWarnings("unused")
        private String action;

        @SuppressWarnings("unused")
        private Map<String, String> params;

        public Request() {
            this.action = "getDeviceStatus";
            this.params = new HashMap<>();
        }
    }

    /**
     * Bridge I/O Response message used by {@link JsonVeluxBridge} for deserializing with including component access
     * methods
     * <P>
     * Expected JSON (sample):
     *
     * <pre>
     * {
     *  "token":"RHIKGlJyZhidI/JSK0a2RQ==",
     *  "result":true,
     *  "deviceStatus":"discovering",       or "IDLE"
     *  "data":{},
     *  "errors":[]
     * }
     * </pre>
     */
    private static class Response {
        @SuppressWarnings("unused")
        private String token = VeluxBindingConstants.UNKNOWN;
        private boolean result;
        private String deviceStatus = VeluxBindingConstants.UNKNOWN;
        @SuppressWarnings("unused")
        private @Nullable Object data = null;
        private String[] errors = {};
    }

    /*
     * Methods required for interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public String getURL() {
        return URL;
    }

    @Override
    public Object getObjectOfRequest() {
        return request;
    }

    @Override
    public Class<Response> getClassOfResponse() {
        return Response.class;
    }

    @Override
    public void setResponse(Object thisResponse) {
        response = (Response) thisResponse;
    }

    @Override
    public boolean isCommunicationSuccessful() {
        return response.result;
    }

    @Override
    public String getDeviceStatus() {
        return response.deviceStatus;
    }

    @Override
    public String[] getErrors() {
        return response.errors;
    }

    /*
     * Methods in addition to interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public VeluxGwState getState() {
        String deviceStatus = this.getDeviceStatus();
        byte stateValue = (byte) VeluxGatewayState.GW_S_GWM.getStateValue();
        byte subStateValue;
        if ("discovering".equals(deviceStatus)) {
            subStateValue = (byte) VeluxGatewaySubState.GW_SS_P1.getStateValue();
        } else if ("IDLE".equals(deviceStatus)) {
            subStateValue = (byte) VeluxGatewaySubState.GW_SS_IDLE.getStateValue();
        } else {
            subStateValue = (byte) VeluxGatewaySubState.GW_SS_P2.getStateValue();
        }
        return new VeluxGwState(stateValue, subStateValue);
    }
}
