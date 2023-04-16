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
package org.openhab.binding.evohome.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * List of evohome API constants
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
@NonNullByDefault
public class EvohomeApiConstants {
    public static final String URL_V2_AUTH = "https://tccna.honeywell.com/Auth/OAuth/Token";

    public static final String URL_V2_BASE = "https://tccna.honeywell.com/WebAPI/emea/api/v1/";

    public static final String URL_V2_ACCOUNT = "userAccount";
    public static final String URL_V2_INSTALLATION_INFO = "location/installationInfo?userId=%s&includeTemperatureControlSystems=True";// {userId}
    public static final String URL_V2_LOCATION = "location/%s/installationInfo?includeTemperatureControlSystems=True"; // {locationId}
    public static final String URL_V2_GATEWAY = "gateway";
    public static final String URL_V2_HOT_WATER = "domesticHotWater/%s/state"; // {hardwareId}
    public static final String URL_V2_SCHEDULE = "%s/%s/schedule"; // {zone_type}, {zoneId}
    public static final String URL_V2_HEAT_SETPOINT = "temperatureZone/%s/heatSetpoint"; // {zoneId}
    public static final String URL_V2_LOCATION_STATUS = "location/%s/status?includeTemperatureControlSystems=True"; // {locationId}
    public static final String URL_V2_MODE = "temperatureControlSystem/%s/mode"; // {systemId}
}
