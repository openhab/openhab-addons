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
package org.openhab.binding.vesync.internal.dto.requests.v2_2;

import org.openhab.binding.vesync.internal.dto.requests.login.AuthenticatedReq;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ManagedDeviceBypassReq} class is used as a DTO to hold the Vesync's API's common
 * request data for V2 ByPass payloads.
 *
 * @author David Goodyear - Initial contribution
 */
public class ManagedDeviceBypassReq extends AuthenticatedReq {

    @SerializedName("deviceRegion")
    public String deviceRegion = "";

    @SerializedName("debugMode")
    public boolean debugMode = false;

    @SerializedName("cid")
    public String cid = "";

    @SerializedName("configModule")
    public String configModule = "";

    @SerializedName("configModel")
    public String configModel = "";

    @SerializedName("payload")
    public BypassDefinition payload = new BypassDefinition();

    public ManagedDeviceBypassReq() {
        method = "bypassV2";
    }
}
