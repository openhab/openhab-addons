/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.internal.dto.thermostat;

/**
 * The {@link ElectricityTierDTO} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ElectricityTierDTO {

    /*
     * The tier name as defined by the Utility. May be an empty string if
     * the tier is undefined or the usage falls outside the defined tiers.
     */
    public String name;

    /*
     * The last daily consumption reading collected. The reading format and precision
     * is to three decimal places in kWh.
     */
    public String consumption;

    /*
     * The daily cumulative tier cost in dollars if defined by the Utility. May
     * be an empty string if undefined.
     */
    public String cost;
}
