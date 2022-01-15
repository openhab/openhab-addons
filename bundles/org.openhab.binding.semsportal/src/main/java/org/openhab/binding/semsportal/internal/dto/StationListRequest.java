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
package org.openhab.binding.semsportal.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Request to list all available power stations in an account. Answer can be deserialized in a
 * {@link StationListResponse}
 *
 * @author Iwan Bron - Initial contribution
 *
 */
@NonNullByDefault
public class StationListRequest {
    // Properties are private but used by Gson to construct the request
    @SerializedName("page_size")
    private int pageSize = 5;
    @SerializedName("page_index")
    private int pageIndex = 1;
    @SerializedName("order_by")
    private String orderBy = "";
    @SerializedName("powerstation_status")
    private String powerstationStatus = "";
    // @SerializedName("key")
    // private String key = "";
    @SerializedName("powerstation_id")
    private String powerstationId = "";
    @SerializedName("powerstation_type")
    private String powerstationType = "";
}
