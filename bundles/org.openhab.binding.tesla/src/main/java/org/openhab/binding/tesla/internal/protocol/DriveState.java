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
package org.openhab.binding.tesla.internal.protocol;

/**
 * The {@link DriveState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class DriveState {

    public String active_route_destination;
    public double active_route_latitude;
    public double active_route_longitude;
    public double active_route_miles_to_arrival;
    public double active_route_minutes_to_arrival;
    public double active_route_traffic_minutes_delay;
    public double latitude;
    public double longitude;
    public double native_latitude;
    public double native_longitude;
    public int gps_as_of;
    public int heading;
    public int native_location_supported;
    public String native_type;
    public String shift_state;
    public String speed;

    DriveState() {
    }
}
