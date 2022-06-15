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
package org.openhab.binding.mercedesme.internal;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Constants {
    public static String MB_AUTH_URL = "https://id.mercedes-benz.com/as/authorization.oauth2";
    public static String MB_TOKEN_URL = "https://id.mercedes-benz.com/as/token.oauth2";
    public static String CALLBACK = "/mb-callback";
    public static String OAUTH_CLIENT_NAME = "#byocar";

    // https://developer.mercedes-benz.com/products/electric_vehicle_status/docs
    public static String SCOPE_EV = "mb:vehicle:mbdata:evstatus";
    // https://developer.mercedes-benz.com/products/fuel_status/docs
    public static String SCOPE_FUEL = "mb:vehicle:mbdata:fuelstatus";
    // https://developer.mercedes-benz.com/products/pay_as_you_drive_insurance/docs
    public static String SCOPE_ODO = "mb:vehicle:mbdata:payasyoudrive";
    // https://developer.mercedes-benz.com/products/vehicle_lock_status/docs
    public static String SCOPE_LOCK = "mb:vehicle:mbdata:vehiclelock";
    // https://developer.mercedes-benz.com/products/vehicle_status/docs
    public static String SCOPE_STATUS = "mb:vehicle:mbdata:vehiclestatus";
    public static String SCOPE_OFFLINE = "offline_access";

    public static String SPACE = "";
}
