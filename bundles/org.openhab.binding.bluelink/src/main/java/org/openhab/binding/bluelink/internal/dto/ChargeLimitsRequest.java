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
package org.openhab.binding.bluelink.internal.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Charge limits request for the Bluelink API.
 *
 * @author Marcus Better - Initial contribution
 */
public record ChargeLimitsRequest(@SerializedName("targetSOClist") List<TargetSOC> targetSOCList) {

    public static final int PLUG_TYPE_DC = 0;
    public static final int PLUG_TYPE_AC = 1;

    public record TargetSOC(int plugType, @SerializedName("targetSOClevel") int targetSOCLevel) {
    }
}
