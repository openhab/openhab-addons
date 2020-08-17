/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BimmerConstants} This class holds the important constants for the BMW Connected Drive Authorization. They
 * are taken from the Bimmercode from github {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BimmerConstants {

    public final static String SERVER_NORTH_AMERICA = "b2vapi.bmwgroup.us";
    public final static String SERVER_CHINA = "b2vapi.bmwgroup.cn:859";
    public final static String SERVER_ROW = "b2vapi.bmwgroup.com";
    public final static Map<String, String> SERVER_MAP = new HashMap<String, String>() {
        {
            put("NORTH_AMERICA", SERVER_NORTH_AMERICA);
            put("CHINA", SERVER_CHINA);
            put("ROW", SERVER_ROW);
        }
    };

    public final static String AUTHORIZATION_VALUE = "Basic blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanliTEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==";
    public final static String CREDENTIAL_VALUES = "nQv6CqtxJuXWP74xf3CJwUEP:1zDHx6un4cDjybLENN3kyfumX2kEYigWPcQpdvDRpIBk7rOJ";
    public final static String CLIENT_ID_VALUE = "dbf0a542-ebd1-4ff0-a9a7-55172fbfce35";
    public final static String REDIRECT_URI_VALUE = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html";
    public final static String SCOPE_VALUES = "authenticate_user vehicle_data remote_services";
}
