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
package org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class PortalCleanLogsRequest {

    @SerializedName("auth")
    final PortalAuthRequestParameter auth;

    @SerializedName("td")
    final String commandName = "GetCleanLogs";

    @SerializedName("did")
    final String targetDeviceId;

    @SerializedName("resource")
    final String targetResource;

    public PortalCleanLogsRequest(PortalAuthRequestParameter auth, String targetDeviceId, String targetResource) {
        this.auth = auth;
        this.targetDeviceId = targetDeviceId;
        this.targetResource = targetResource;
    }
}
