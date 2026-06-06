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
package org.openhab.binding.sensorcommunity.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RadiationConfiguration} class for radiation measurement sensor
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RadiationConfiguration extends SensorCommunityConfiguration {

    // see https://community.openhab.org/t/adding-counts-per-minute-to-the-sensor-community-binding/168748/2
    public double conversionFactor = 0.00136;
}
