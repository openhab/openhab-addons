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
package org.openhab.binding.ambientweather.internal.model;

/**
 * The {@link StationInfoJson} is the JSON object
 * returned by the Ambient Weather API that describes the
 * user-provided name and location of the weather station.
 *
 * @author Mark Hilbush - Initial Contribution
 */
public class StationInfoJson {
    /*
     * The name given to the station by the user
     */
    public String name;

    /*
     * The location given to the station by the user
     */
    public String location;
}
