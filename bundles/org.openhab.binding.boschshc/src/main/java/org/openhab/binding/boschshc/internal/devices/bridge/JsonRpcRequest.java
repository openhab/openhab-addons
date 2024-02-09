/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Payload as POST data for triggering a RPC call on the Bosch Smart Home Controller.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
class JsonRpcRequest {

    public String jsonrpc;
    public String method;
    public String[] params;

    public JsonRpcRequest(String jsonrpc, String method, String[] params) {
        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = params;
    }

    public JsonRpcRequest() {
        this("", "", new String[0]);
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}
