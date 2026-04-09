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
package org.openhab.binding.vesync.internal.dto.responses.devices.airpurifier;

import org.openhab.binding.vesync.internal.dto.responses.TransactionResp;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link V1StatusWrapper} is a Java class used as a DTO to hold the Vesync's API's common
 * response data, with regard's to an air purifier device, this is a wrapper layer for the actual response data.
 *
 * @author David Goodyear - Initial contribution
 */
public class V1StatusWrapper extends TransactionResp {

    @SerializedName("result")
    public V1StatusDetails result;
}
