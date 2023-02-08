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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

/**
 * Response of the Controller for a Long Poll API call.
 *
 * The result field will contain the subscription ID needed for further API calls (e.g. the long polling call)
 *
 * @author Stefan Kästle - Initial contribution
 */
public class SubscribeResult {
    private String result;
    private String jsonrpc;

    public String getResult() {
        return this.result;
    }

    public String getJsonrpc() {
        return this.jsonrpc;
    }

    public static Boolean isValid(SubscribeResult obj) {
        return obj != null && obj.result != null && obj.jsonrpc != null;
    }
}
