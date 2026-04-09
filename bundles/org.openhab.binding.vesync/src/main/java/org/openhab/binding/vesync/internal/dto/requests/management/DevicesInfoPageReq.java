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
package org.openhab.binding.vesync.internal.dto.requests.management;

import org.openhab.binding.vesync.internal.dto.requests.login.AuthenticatedReq;
import org.openhab.binding.vesync.internal.dto.responses.UserSession;
import org.openhab.binding.vesync.internal.exceptions.AuthenticationException;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DevicesInfoPageReq} class is a DTO to get a specific page of information
 * about devices known managed by the Vesync API, and their associated current status as
 * well as key information required to address them in requests.
 *
 * @author David Goodyear - Initial contribution
 */
public class DevicesInfoPageReq extends AuthenticatedReq {

    @SerializedName("pageNo")
    public String pageNo;

    @SerializedName("pageSize")
    public String pageSize;

    public DevicesInfoPageReq(final UserSession user) throws AuthenticationException {
        super(user);
        method = "devices";
    }

    public DevicesInfoPageReq(final UserSession user, int pageNo, int pageSize) throws AuthenticationException {
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
