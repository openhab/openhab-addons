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

import org.openhab.binding.vesync.internal.dto.responses.VeSyncResponse;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DevicesInfoPageResp} class is used as a DTO to hold the Vesync's API's response data to a
 * response of a {@link org.openhab.binding.vesync.internal.dto.requests.management.DevicesInfoPageReq} request.
 *
 * @author David Goodyear - Initial contribution
 */
public class DevicesInfoPageResp extends VeSyncResponse {

    @SerializedName("result")
    public DeviceInfoPage outcome;
}
