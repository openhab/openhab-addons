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
package org.openhab.binding.livisismarthome.internal.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link URLCreator} is responsible for creating all required URLs.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public final class URLCreator {

    private URLCreator() {
    }

    public static String createTokenURL(String host) {
        return createAddress(host) + "/auth/token";
    }

    public static String createDevicesURL(String host) {
        return createAddress(host) + "/device";
    }

    public static String createDeviceStatesURL(String host) {
        return createAddress(host) + "/device/states";
    }

    public static String createDeviceURL(String host, String deviceId) {
        return createAddress(host) + "/device/" + deviceId;
    }

    public static String createDeviceStateURL(String host, String deviceId) {
        return createAddress(host) + "/device/" + deviceId + "/state";
    }

    public static String createDeviceCapabilitiesURL(String host, String deviceId) {
        return createAddress(host) + "/device/" + deviceId + "/capabilities";
    }

    public static String createCapabilityURL(String host) {
        return createAddress(host) + "/capability";
    }

    public static String createCapabilityStatesURL(String host) {
        return createAddress(host) + "/capability/states";
    }

    public static String createActionURL(String host) {
        return createAddress(host) + "/action";
    }

    public static String createStatusURL(String host) {
        return createAddress(host) + "/status";
    }

    public static String createLocationURL(String host) {
        return createAddress(host) + "/location";
    }

    public static String createMessageURL(String host) {
        return createAddress(host) + "/message";
    }

    public static String createEventsURL(String host, String token, boolean isClassicController) {
        final String tokenURLEncoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        final String webSocketPort;
        if (isClassicController) {
            webSocketPort = "8080";
        } else {
            webSocketPort = "9090";
        }
        return "ws://" + host + ':' + webSocketPort + "/events?token=" + tokenURLEncoded;
    }

    private static String createAddress(String host) {
        return "http://" + host + ":8080";
    }
}
