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
package org.openhab.binding.fronius.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PowerFlowRealtimeResponse} is responsible for storing
 * the response from the powerflowrealtime api
 *
 * @author Thomas Rokohl - Initial contribution
 */
public class PowerFlowRealtimeResponse extends BaseFroniusResponse {
    @SerializedName("Body")
    private PowerFlowRealtimeBody body;

    public PowerFlowRealtimeBody getBody() {
        if (body == null) {
            body = new PowerFlowRealtimeBody();
        }
        return body;
    }

    public void setBody(PowerFlowRealtimeBody body) {
        this.body = body;
    }
}
