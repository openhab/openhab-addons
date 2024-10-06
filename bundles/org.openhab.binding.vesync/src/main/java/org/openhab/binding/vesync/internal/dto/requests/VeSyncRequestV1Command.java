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
package org.openhab.binding.vesync.internal.dto.requests;

import org.eclipse.jetty.http.HttpMethod;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncRequestV1Command} is the Java class as a DTO to define the base implementation of a V1 command for
 * the Vesync API.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncRequestV1Command extends VeSyncAuthenticatedRequest {

    @SerializedName("uuid")
    public String uuid = null;

    public VeSyncRequestV1Command(final String deviceUuid) {
        // Exclude fields that shouldn't be there by setting to null
        super.phoneOS = null;
        super.phoneBrand = null;
        super.method = null;
        super.appVersion = null;
        super.httpMethod = HttpMethod.PUT;
        // Set the required payload parameters
        uuid = deviceUuid;
    }

    public String getUuid() {
        return uuid;
    }
}
