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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncManagedDevicesPage} is a Java class used to hold the outcome dataset from the request
 * getting the devices data, known about on the VeSync service.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncManagedDevicePageOutcome {
    @SerializedName("pageNo")
    public String pageNo;

    @SerializedName("total")
    public String total;

    @SerializedName("pageSize")
    public String pageSize;

    @SerializedName("list")
    public VeSyncManagedDeviceBase[] list;

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
