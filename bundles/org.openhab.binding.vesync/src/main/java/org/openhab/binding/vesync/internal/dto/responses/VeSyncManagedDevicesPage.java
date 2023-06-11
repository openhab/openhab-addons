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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncManagedDevicesPage} is a Java class used as a DTO to hold the Vesync's API's response data to a
 * page of data requesting the manages devices.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncManagedDevicesPage extends VeSyncResponse {

    @SerializedName("result")
    public Outcome outcome;

    public class Outcome {
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
}
