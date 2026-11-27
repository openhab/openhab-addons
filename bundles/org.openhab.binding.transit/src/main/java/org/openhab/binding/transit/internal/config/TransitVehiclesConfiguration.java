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
package org.openhab.binding.transit.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TransitVehiclesConfiguration} holds the configuration of a vehicles Thing.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitVehiclesConfiguration {
    public static final int DIRECTION_BOTH = -1;

    public String routeId = "";
    // 0 = outbound, 1 = inbound, -1 = both directions
    public int directionId = DIRECTION_BOTH;
    public int refreshInterval = 300;
}
