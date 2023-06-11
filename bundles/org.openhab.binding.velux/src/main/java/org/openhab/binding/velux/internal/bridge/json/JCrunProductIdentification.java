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
import org.openhab.binding.velux.internal.bridge.common.RunProductIdentification;

/**
 * Specific bridge communication message supported by the Velux bridge.
 * <P>
 * Message semantic: Trigger action to identify a product, resulting in a return of current bridge state.
 * <P>
 *
 * It defines information how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the {@link JsonBridgeCommunicationProtocol}.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault

class JCrunProductIdentification extends RunProductIdentification implements JsonBridgeCommunicationProtocol {
    private static final int DEFAULT_IDENTIFY_TIME = 50;

    private static final String URL = "/api/v1/products";
    private static final String DESCRIPTION = "identify one product";

    private Request request = new Request();
    private Response response = new Response();

    private static int productId;
    private static int identifyTime = DEFAULT_IDENTIFY_TIME;

    /*
     * Message Objects
     */
    private static class ParamsIdentifyProduct {
        @SuppressWarnings("unused")
        private int id;
        @SuppressWarnings("unused")
        private int time;

        private ParamsIdentifyProduct(int id, int time) {
            this.id = id;
            this.time = time;
        }
    }

    /**
     * Bridge I/O Request message used by {@link JsonVeluxBridge} for serializing.
     * <P>
     * Resulting JSON (sample):
     *
     * <pre>
     * {"action":"identify","params":{"id":23,"time":254}}
     * </pre>
     */
    private static class Request {
        @SuppressWarnings("unused")
        private String action;
        @SuppressWarnings("unused")
        private ParamsIdentifyProduct params;

        public Request() {
            this.action = "identify";
            this.params = new ParamsIdentifyProduct(JCrunProductIdentification.productId,
                    JCrunProductIdentification.identifyTime);
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
     * "token": "NkR/AA5xXj7iL6NiIW8keA==",
     * "result": false,
     * "deviceStatus": "IDLE",
     * "data": {},
     * "errors": [ 104 ]
     * }
     * </pre>
     */
    private static class Response {
        @SuppressWarnings("unused")
        private String token = VeluxBindingConstants.UNKNOWN;
        private boolean result;
        private String deviceStatus = VeluxBindingConstants.UNKNOWN;
        @SuppressWarnings("unused")
        private @Nullable Object data;
        private String[] errors = {};

        public boolean getResult() {
            return result;
        }

        public String getDeviceStatus() {
            return deviceStatus;
        }

        public String[] getErrors() {
            return errors;
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
        return response.getDeviceStatus();
    }

    @Override
    public String[] getErrors() {
        return response.getErrors();
    }

    /*
     * Methods in addition to interface {@link BridgeCommunicationProtocol}.
     */
    @Override
    public JCrunProductIdentification setProductId(int id) {
        JCrunProductIdentification.productId = id;
        return this;
    }
}
