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
package org.openhab.binding.vesync.internal.dto.requests.v2;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BypassDefinition} class is used as a DTO to hold the addressing information
 * that the command is for, and the relevant payload to send.
 *
 * @author David Goodyear - Initial contribution
 */
public class BypassDefinition {

    @SerializedName("method")
    public String method;

    @SerializedName("source")
    public String source = "APP";

    @SerializedName("data")
    public EmptyPayload data = new EmptyPayload();

    @SerializedName("subDeviceNo")
    public int subDeviceNo = 0;
}
