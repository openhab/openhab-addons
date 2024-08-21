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
package org.openhab.binding.semsportal.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * POJO containing (a small subset of) the data received from the portal when issuing a {@link StatusRequest}
 *
 * @author Iwan Bron - Initial contribution
 */
public class StatusResponse extends BaseResponse {

    @SerializedName("data")
    private StationStatus status;

    public StationStatus getStatus() {
        return status;
    }
}
