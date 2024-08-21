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
package org.openhab.binding.boschindego.internal.dto.response;

import com.google.gson.annotations.SerializedName;

/**
 * Mower serial number and status.
 * 
 * @author Jacob Laursen - Initial contribution
 */
public class Mower {

    @SerializedName("alm_sn")
    public String serialNumber;

    @SerializedName("alm_status")
    public int status;
}
