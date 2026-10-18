/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DriveState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class DriveState {

    // Only reported while a route is active in the navigation, otherwise null
    @SerializedName("active_route_destination")
    public String activeRouteDestination;
    @SerializedName("active_route_latitude")
    public Double activeRouteLatitude;
    @SerializedName("active_route_longitude")
    public Double activeRouteLongitude;
    @SerializedName("active_route_miles_to_arrival")
    public Double activeRouteMilesToArrival;
    @SerializedName("active_route_minutes_to_arrival")
    public Double activeRouteMinutesToArrival;
    @SerializedName("active_route_traffic_minutes_delay")
    public Double activeRouteTrafficMinutesDelay;
    @SerializedName("gps_as_of")
    public int gpsAsOf;
    @SerializedName("heading")
    public int heading;
    @SerializedName("latitude")
    public double latitude;
    @SerializedName("longitude")
    public double longitude;
    @SerializedName("native_latitude")
    public double nativeLatitude;
    @SerializedName("native_location_supported")
    public int nativeLocationSupported;
    @SerializedName("native_longitude")
    public double nativeLongitude;
    @SerializedName("native_type")
    public String nativeType;
    @SerializedName("power")
    public int power;
    @SerializedName("shift_state")
    public String shiftState;
    @SerializedName("speed")
    public String speed;
    @SerializedName("timestamp")
    public long timestamp;

    DriveState() {
    }
}
