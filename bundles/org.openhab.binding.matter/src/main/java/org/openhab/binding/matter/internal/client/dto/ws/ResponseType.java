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
package org.openhab.binding.matter.internal.client.dto.ws;

import com.google.gson.annotations.SerializedName;

/**
 * Websocket message response types.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public enum ResponseType {

    @SerializedName("resultError")
    RESULT_ERROR("resultError"),

    @SerializedName("resultSuccess")
    RESULT_SUCCESS("resultSuccess");

    private final String value;

    ResponseType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
