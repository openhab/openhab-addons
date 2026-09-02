/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.websocket.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * WebSocket access model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public enum Access {
    READ,
    @SerializedName("READWRITE")
    READ_WRITE,
    @SerializedName("READSTATIC")
    READ_STATIC,
    @SerializedName("WRITEONLY")
    WRITE_ONLY,
    NONE
}
