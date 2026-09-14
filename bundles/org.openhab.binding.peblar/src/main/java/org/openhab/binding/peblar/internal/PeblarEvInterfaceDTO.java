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

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class PeblarEvInterfaceDTO {

    /** IEC 61851 CP state, e.g. "State A", "State B", "State C" */
    public @Nullable String cpState;

    public @Nullable Boolean lockState;

    /** Charge current limit in milliamperes */
    public @Nullable Long chargeCurrentLimit;

    public @Nullable String chargeCurrentLimitSource;

    /** Actual applied charge current limit in milliamperes */
    public @Nullable Long chargeCurrentLimitActual;

    public @Nullable Boolean force1Phase;
}
