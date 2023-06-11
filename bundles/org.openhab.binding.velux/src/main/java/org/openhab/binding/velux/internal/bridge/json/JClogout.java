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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.Logout;

/**
 * Specific bridge communication message supported by the Velux bridge.
 * <P>
 * Message semantic: Communication to deauthenticate itself, resulting in a return of current bridge state.
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
class JClogout extends Logout implements JsonBridgeCommunicationProtocol {

    private static final String URL = "/api/v1/auth";
    private static final String DESCRIPTION = "deauthenticate / logout";

    private Request request = new Request();
    private Response response = new Response();

    /**
     * Bridge I/O Request message used by {@link JsonVeluxBridge} for serializing.
     * <P>
     * Resulting JSON:
     *
     * <pre>
     * {"action":"logout","params":{}}
     * </pre>
     */
    private static class Request {

        @SuppressWarnings("unused")
        private String action;
        @SuppressWarnings("unused")
        private Map<String, String> params;

        public Request() {
            this.action = "logout";
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
     * '{"token": "PHPnfLda71xfGlxoYEOTGQ==", "result": true, "deviceStatus": "IDLE", "data": {}, "errors": [] }'
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

        public boolean getResult() {
            return result;
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
    public void setResponse(Object thisResponse) {
        response = (Response) thisResponse;
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
}
