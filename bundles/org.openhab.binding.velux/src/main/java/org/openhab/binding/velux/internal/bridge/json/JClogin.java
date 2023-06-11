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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific bridge communication message supported by the Velux bridge.
 * <P>
 * Message semantic: Communication to authenticate itself, resulting in a return of current bridge state.
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
class JClogin extends Login implements JsonBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(JClogin.class);

    private static final String URL = "/api/v1/auth";
    private static final String DESCRIPTION = "authenticate / login";

    private Request request = new Request();
    private Response response = new Response();

    /*
     * Message Objects
     */
    private static class ParamsLogin {
        @SuppressWarnings("unused")
        private String password = VeluxBindingConstants.UNKNOWN;
    }

    /**
     * Bridge I/O Request message used by {@link JsonVeluxBridge}
     * for serializing.
     * <P>
     * Resulting JSON:
     *
     * <pre>
     * {"action":"login","params":{"password":"PASSWORD"}}
     * </pre>
     */
    private static class Request {

        @SuppressWarnings("unused")
        private final String action = "login";
        private ParamsLogin params;

        public Request() {
            this.params = new ParamsLogin();
        }
    }

    /**
     * Bridge I/O Response message used by {@link JsonVeluxBridge} for deserializing with including component access
     * methods
     * <P>
     * <B>Expected JSON (sample):</B>
     *
     * <pre>
     * '{"token": "PHPnfLda71xfGlxoYEOTGQ==", "result": true, "deviceStatus": "IDLE", "data": {}, "errors": [] }'
     * </pre>
     */
    private static class Response {
        private String token = VeluxBindingConstants.UNKNOWN;
        private boolean result;
        private String deviceStatus = VeluxBindingConstants.UNKNOWN;
        @SuppressWarnings("unused")
        private @Nullable Object data;
        private String[] errors = {};

        public String getToken() {
            return token;
        }

        public boolean getResult() {
            return result;
        }
    }

    /*
     * Constructor Method
     */

    public JClogin() {
        logger.trace("JClogin(constructor) called.");
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

    /*
     * Methods in addition to interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public void setPassword(String thisPassword) {
        logger.trace("setPassword() called.");
        request.params.password = thisPassword;
    }

    @Override
    public String getAuthToken() {
        return response.getToken();
    }
}
