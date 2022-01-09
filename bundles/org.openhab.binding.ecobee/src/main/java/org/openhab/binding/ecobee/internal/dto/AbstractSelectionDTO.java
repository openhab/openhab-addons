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
package org.openhab.binding.ecobee.internal.dto;

/**
 * The {@link AbstractSelectionDTO} represents the common objects included in
 * all requests.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AbstractSelectionDTO {

    /*
     * The type of match data supplied: Values: thermostats, registered, managementSet.
     */
    public String selectionType;

    /*
     * The match data based on selectionType (e.g. a comma-separated list of thermostat
     * idendifiers in the case of a selectionType of thermostats)
     */
    public String selectionMatch;
}
