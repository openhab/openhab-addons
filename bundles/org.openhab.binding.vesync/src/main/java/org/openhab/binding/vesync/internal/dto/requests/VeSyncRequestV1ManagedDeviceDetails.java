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

import org.openhab.binding.vesync.internal.dto.responses.VeSyncUserSession;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncRequestV1ManagedDeviceDetails} is the Java class as a DTO to request the managed device details for
 * the Vesync API.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncRequestV1ManagedDeviceDetails extends VeSyncAuthenticatedRequest {

    @SerializedName("mobileId")
    public String mobileId = "1234567890123456";

    @SerializedName("uuid")
    public String uuid = null;

    public VeSyncRequestV1ManagedDeviceDetails(final String deviceUuid) {
        uuid = deviceUuid;
        method = "deviceDetail";
    }

    public VeSyncRequestV1ManagedDeviceDetails(final VeSyncUserSession user) throws AuthenticationException {
        super(user);
        method = "deviceDetail";
    }

    public VeSyncRequestV1ManagedDeviceDetails(final VeSyncUserSession user, String deviceUuid)
            throws AuthenticationException {
        this(user);
        uuid = deviceUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMobileId() {
        return mobileId;
    }
}
