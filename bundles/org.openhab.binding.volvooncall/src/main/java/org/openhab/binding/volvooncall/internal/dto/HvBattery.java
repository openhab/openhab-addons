/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.dto;

import static org.openhab.binding.volvooncall.internal.VolvoOnCallBindingConstants.UNDEFINED;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HvBattery} is responsible for storing
 * PHEV Battery information returned by vehicle status rest answer
 *
 * @author Arie van der Lee - Initial contribution
 */
@NonNullByDefault
public class HvBattery {
    public int hvBatteryLevel = UNDEFINED;
    public int distanceToHVBatteryEmpty = UNDEFINED;
    public @NonNullByDefault({}) String hvBatteryChargeStatusDerived;
    public int timeToHVBatteryFullyCharged = UNDEFINED;
    /*
     * Currently unused in the binding, maybe interesting in the future
     * private ZonedDateTime timestamp;
     */
}
