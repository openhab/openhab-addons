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
 * The {@link UtilityDTO} contains the Utility information the Thermostat belongs to.
 * The utility may not be modified through the API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class UtilityDTO {
    /*
     * The Utility company name.
     */
    public String name;

    /*
     * The Utility company contact phone number.
     */
    public String phone;

    /*
     * The Utility company email address.
     */
    public String email;

    /*
     * The Utility company web site.
     */
    public String web;
}
