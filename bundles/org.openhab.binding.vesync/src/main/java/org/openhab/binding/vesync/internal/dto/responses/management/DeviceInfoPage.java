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
package org.openhab.binding.vesync.internal.dto.responses.management;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DeviceInfoPage} class is a DTO that defines the information for a read page of information
 * about 1 or more devices.
 *
 * @author David Goodyear - Initial contribution
 */
public class DeviceInfoPage {
    @SerializedName("pageNo")
    public String pageNo;

    @SerializedName("total")
    public String total;

    @SerializedName("pageSize")
    public String pageSize;

    @SerializedName("list")
    public DeviceInfo[] list;

    public String getPageNo() {
        return pageNo;
    }

    public String getPageSize() {
        return pageSize;
    }

    public String getTotal() {
        return total;
    }
}
