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
 * The {@link ManagementDTO} contains information about the management company
 * the thermostat belongs to. The Management object is read-only, it may be
 * modified in the web portal.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ManagementDTO {
    /*
     * The administrative contact name.
     */
    public String administrativeContact;

    /*
     * The billing contact name.
     */
    public String billingContact;

    /*
     * The company name.
     */
    public String name;

    /*
     * The phone number.
     */
    public String phone;

    /*
     * The contact email address.
     */
    public String email;

    /*
     * The company web site.
     */
    public String web;

    /*
     * Whether to show management alerts on the thermostat.
     */
    public Boolean showAlertIdt;

    /*
     * Whether to show management alerts in the web portal.
     */
    public Boolean showAlertWeb;
}
