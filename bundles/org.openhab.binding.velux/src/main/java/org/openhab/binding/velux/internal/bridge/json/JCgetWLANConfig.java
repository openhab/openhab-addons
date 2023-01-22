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
package org.openhab.binding.velux.internal.bridge.json;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.GetWLANConfig;
import org.openhab.binding.velux.internal.things.VeluxGwWLAN;

/**
 * Specific bridge communication message supported by the Velux bridge.
 * <P>
 * Message semantic: Retrieval of WLAN configuration.
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
class JCgetWLANConfig extends GetWLANConfig implements JsonBridgeCommunicationProtocol {

    private static final String URL = "/api/v1/settings";
    private static final String DESCRIPTION = "get WLAN configuration";

    private Request request = new Request();
    private Response response = new Response();

    /*
     * Message Objects
     */

    /**
     * Bridge I/O Request message used by {@link org.openhab.binding.velux.internal.bridge.json.JsonVeluxBridge
     * JsonVeluxBridge}
     * for serializing.
     * <P>
     * Resulting JSON:
     *
     * <pre>
     * {"action":"wifi","params":{}}
     * </pre>
     */
    private static class Request {

        @SuppressWarnings("unused")
        private String action;

        @SuppressWarnings("unused")
        private Map<String, String> params;

        public Request() {
            this.action = "wifi";
            this.params = new HashMap<>();
        }
    }

    /**
     * Bridge Communication Structure containing the version of the firmware.
     * <P>
     * Used within structure {@link JCgetWLANConfig} to describe the network connectivity of the Bridge.
     *
     * <PRE>
     * {"password":"Esf56mxqFY","name":"VELUX_KLF_847C"}
     * </PRE>
     */
    private static class BCWLANConfig {

        private String password = VeluxBindingConstants.UNKNOWN;
        private String name = VeluxBindingConstants.UNKNOWN;

        @Override
        public String toString() {
            return String.format("SSID=%s,password=********", this.name);
        }
    }

    /**
     * Bridge I/O Response message used by {@link JsonBridgeCommunicationProtocol} for deserialization with including
     * component access
     * methods
     * <P>
     * Expected JSON (sample):
     *
     * <pre>
     * {
     *  "token":"RHIKGlJyZhidI/JSK0a2RQ==",
     *  "result":true,
     *  "deviceStatus":"IDLE",
     *  "data":{"password":"Esf56mxqFY","name":"VELUX_KLF_847C"},
     *  "errors":[]
     * }
     * </pre>
     */
    private static class Response {
        @SuppressWarnings("unused")
        private String token = VeluxBindingConstants.UNKNOWN;
        private boolean result;
        private String deviceStatus = VeluxBindingConstants.UNKNOWN;
        private BCWLANConfig data = new BCWLANConfig();
        private String[] errors = {};

        public boolean getResult() {
            return result;
        }

        @Override
        public String toString() {
            return data.toString();
        }
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
    public void setResponse(Object response) {
        this.response = (Response) response;
    }

    @Override
    public boolean isCommunicationSuccessful() {
        return response.getResult();
    }

    @Override
    public String getDeviceStatus() {
        return response.deviceStatus;
    }

    @Override
    public String[] getErrors() {
        return response.errors;
    }

    /**
     * Methods in addition to interface {@link JsonBridgeCommunicationProtocol}.
     */
    @Override
    public VeluxGwWLAN getWLANConfig() {
        VeluxGwWLAN gwWLAN = new VeluxGwWLAN(response.data.name, response.data.password);
        return gwWLAN;
    }
}
