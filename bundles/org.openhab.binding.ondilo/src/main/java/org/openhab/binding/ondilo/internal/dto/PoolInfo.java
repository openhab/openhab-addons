/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PoolInfo} DTO for representing Ondilo pool infos.
 *
 * @author MikeTheTux - Initial contribution
 */
public class PoolInfo {
    /*
     * Example JSON representation:
     * {
     * "uuid": "1234567890ABCDEF",
     * "serial_number": "SN00001",
     * "sw_version": "1.5.1-stable"
     * }
     */
    public String uuid;
    @SerializedName("serial_number")
    public String serialNumber;
    @SerializedName("sw_version")
    public String swVersion;
}
