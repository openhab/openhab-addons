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
package org.openhab.binding.vesync.internal.dto.responses.devices.v2_2.airhumidifier;

import org.openhab.binding.vesync.internal.dto.responses.TransactionResp;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link V2StatusWrapper} class is used as a DTO to hold the Vesync's API's
 * response data from the bypass API about the result of a request for the status of an air humidifier, however for
 * reasons not understood it is double wrapped into this second wrapper.
 *
 * @author David Goodyear - Initial contribution
 */
public class V2StatusWrapper extends TransactionResp {

    @SerializedName("result")
    public V2StatusDetails result;
}
