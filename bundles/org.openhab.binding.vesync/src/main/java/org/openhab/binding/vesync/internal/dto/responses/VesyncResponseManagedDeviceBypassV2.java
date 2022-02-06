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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VesyncResponseManagedDeviceBypassV2} is a Java class used as a DTO to hold the Vesync's API's common
 * response data.
 *
 * @author David Goodyear - Initial contribution
 */
public class VesyncResponseManagedDeviceBypassV2 extends VesyncResponse {

    @SerializedName("result")
    public ManagedDeviceByPassV2Payload result;

    public class ManagedDeviceByPassV2Payload extends VesyncResponse {

    }
}
