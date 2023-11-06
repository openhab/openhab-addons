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

import java.util.List;

/**
 * The {@link ProgramDTO} is a container for the Schedule and its Climates. See Core Concepts for
 * details on how the program is structured. The schedule property is a two dimensional array
 * containing the climate names.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ProgramDTO {

    /*
     * The Schedule object defining the program schedule.
     */
    public List<List<String>> schedule;

    /*
     * The list of Climate objects defining all the climates in the program schedule.
     */
    public List<ClimateDTO> climates;

    /*
     * The currently active climate, identified by its ClimateRef.
     */
    public String currentClimateRef;
}
