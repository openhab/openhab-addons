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
package org.openhab.binding.synopanalyzer.internal.stationdb;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Station} is a DTO for stations.json database.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class Station {
    public int idOmm;
    public String usualName = "";
    private double latitude;
    private double longitude;

    public String getLocation() {
        return Double.toString(latitude) + "," + Double.toString(longitude);
    }
}
