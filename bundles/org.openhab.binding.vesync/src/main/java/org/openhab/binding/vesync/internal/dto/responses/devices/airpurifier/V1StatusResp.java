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
 * The {@link V1StatusResp} class is used as a DTO to hold the Vesync's API's response,
 * to a status request
 *
 * @author David Goodyear - Initial contribution
 */
public class V1StatusResp extends TransactionResp {

    @SerializedName("result")
    public V1StatusWrapper result;
}
