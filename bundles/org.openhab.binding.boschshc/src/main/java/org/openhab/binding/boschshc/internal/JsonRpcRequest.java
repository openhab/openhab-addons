/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal;

/**
 * Payload as POST data for triggering a RPC call on the Bosch Smart Home Controller.
 *
 * @author Stefan Kästle - Initial contribution
 */
class JsonRpcRequest {

    public JsonRpcRequest(String jsonrpc, String method, String[] params) {

        this.jsonrpc = jsonrpc;
        this.method = method;
        this.params = params;
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

    public JsonRpcRequest() {
        this.jsonrpc = "";
        this.method = "";
        this.params = new String[0];
    }

    public String jsonrpc;
    public String method;
    public String[] params;

}
