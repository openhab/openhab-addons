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
package org.openhab.binding.vesync.internal.dto.requests;

import org.openhab.binding.vesync.internal.dto.responses.VesyncLoginResponse;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VesyncRequestManagedDevicesPage} is the Java class as a DTO to hold login credentials for the Vesync
 * API.
 *
 * @author David Goodyear - Initial contribution
 */
public class VesyncRequestManagedDevicesPage extends VesyncAuthenticatedRequest {

    @SerializedName("pageNo")
    public String pageNo;

    public String getPageNo() {
        return pageNo;
    }

    public String getPageSize() {
        return pageSize;
    }

    @SerializedName("pageSize")
    public String pageSize;

    public VesyncRequestManagedDevicesPage(final VesyncLoginResponse.VesyncUserSession user)
            throws AuthenticationException {
        super(user);
        method = "devices";
    }

    public VesyncRequestManagedDevicesPage(final VesyncLoginResponse.VesyncUserSession user, int pageNo, int pageSize)
            throws AuthenticationException {
        this(user);
        this.pageNo = String.valueOf(pageNo);
        this.pageSize = String.valueOf(pageSize);
    }
}
