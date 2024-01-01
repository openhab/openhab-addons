/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link TechnicianDTO} contains information pertaining to the technician
 * associated with a thermostat. The technician may not be modified through the API.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class TechnicianDTO {
    /*
     * The internal ecobee unique identifier for this contractor.
     */
    public String contractorRef;

    /*
     * The company name of the technician.
     */
    public String name;

    /*
     * The technician's contact phone number.
     */
    public String phone;

    /*
     * The technician's street address.
     */
    public String streetAddress;

    /*
     * The technician's city.
     */
    public String city;

    /*
     * The technician's State or Province.
     */
    public String provinceState;

    /*
     * The technician's country.
     */
    public String country;

    /*
     * The technician's ZIP or Postal Code.
     */
    public String postalCode;

    /*
     * The technician's email address.
     */
    public String email;

    /*
     * The technician's web site.
     */
    public String web;
}
