/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OctopusEnergyConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class OctopusEnergyConfiguration {

    /**
     * The Octopus Energy account number.
     */
    public String accountNumber = "";

    /**
     * The Octopus Energy API key.
     */
    public String apiKey = "";

    /**
     * The Octopus Energy query interval (in minutes) for consumption and price updates.
     */
    public long refreshInterval = OctopusEnergyBindingConstants.DEFAULT_REFRESH_INTERVAL_MINS;
}
