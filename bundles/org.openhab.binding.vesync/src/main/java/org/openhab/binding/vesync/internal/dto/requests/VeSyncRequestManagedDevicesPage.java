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
package org.openhab.binding.vesync.internal.dto.requests;

import org.openhab.binding.vesync.internal.dto.responses.VeSyncUserSession;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncRequestManagedDevicesPage} is the Java class as a DTO to hold login credentials for the Vesync
 * API.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncRequestManagedDevicesPage extends VeSyncAuthenticatedRequest {

    @SerializedName("pageNo")
    public String pageNo;

    @SerializedName("pageSize")
    public String pageSize;

    public VeSyncRequestManagedDevicesPage(final VeSyncUserSession user) throws AuthenticationException {
        super(user);
        method = "devices";
    }

    public VeSyncRequestManagedDevicesPage(final VeSyncUserSession user, int pageNo, int pageSize)
            throws AuthenticationException {
        this(user);
        this.pageNo = String.valueOf(pageNo);
        this.pageSize = String.valueOf(pageSize);
    }

    public String getPageNo() {
        return pageNo;
    }

    public String getPageSize() {
        return pageSize;
    }
}
