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
package org.openhab.binding.peblar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class PeblarEvInterfaceDTO {

    /** IEC 61851 CP state, e.g. "State A", "State B", "State C" */
    @SerializedName("CpState")
    public @Nullable String cpState;

    @SerializedName("LockState")
    public @Nullable Boolean lockState;

    /** Charge current limit in milliamperes */
    @SerializedName("ChargeCurrentLimit")
    public @Nullable Long chargeCurrentLimit;

    @SerializedName("ChargeCurrentLimitSource")
    public @Nullable String chargeCurrentLimitSource;

    /** Actual applied charge current limit in milliamperes */
    @SerializedName("ChargeCurrentLimitActual")
    public @Nullable Long chargeCurrentLimitActual;

    @SerializedName("Force1Phase")
    public @Nullable Boolean force1Phase;
}
