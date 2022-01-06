/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * The {@link SecuritySettingsDTO} defines the security settings which a thermostat
 * may have. Currently this object stores data specific to access control. If any of
 * the XXXAccess fields are not supplied they will default to false. So to set all
 * to false where previously some were set to true the caller can either pass all
 * the XXXAccess fields explicitly, or pass none and the default will be set for each.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SecuritySettingsDTO {

    /*
     * The 4-digit user access code for the thermostat. The code must be set when
     * enabling access control. See the callout above for more information.
     */
    public String userAccessCode;

    /*
     * The flag for determing whether there are any restrictions on the thermostat
     * regarding access control. Default value is false. If all other values are
     * true this value will default to true.
     */
    public Boolean allUserAccess;

    /*
     * The flag for determing whether there are any restrictions on the thermostat
     * regarding access control to the Thermostat Program. Default value is false,
     * unless allUserAccess is true.
     */
    public Boolean programAccess;

    /*
     * The flag for determing whether there are any restrictions on the thermostat
     * regarding access control to the Thermostat system and settings. Default value
     * is false, unless allUserAccess is true.
     */
    public Boolean detailsAccess;

    /*
     * The flag for determing whether there are any restrictions on the thermostat
     * regarding access control to the Thermostat quick save functionality. Default
     * value is false, unless allUserAccess is true.
     */
    public Boolean quickSaveAccess;

    /*
     * The flag for determing whether there are any restrictions on the thermostat
     * regarding access control to the Thermostat vacation functionality. Default
     * value is false, unless allUserAccess is true.
     */
    public Boolean vacationAccess;
}
