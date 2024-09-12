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
package org.openhab.binding.ecovacs.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.util.HashUtil;

/**
 * @author Johannes Ptaszyk - Initial contribution
 */
@NonNullByDefault
public final class EcovacsApiConfiguration {
    private final String deviceId;
    private final String username;
    private final String password;
    private final String continent;
    private final String country;
    private final String language;
    private final String clientKey;
    private final String clientSecret;
    private final String authClientKey;
    private final String authClientSecret;
    private final String appKey;

    public EcovacsApiConfiguration(String deviceId, String username, String password, String continent, String country,
            String language, String clientKey, String clientSecret, String authClientKey, String authClientSecret,
            String appKey) {
        this.deviceId = HashUtil.getMD5Hash(deviceId);
        this.username = username;
        this.password = password;
        this.continent = continent;
        this.country = country;
        this.language = language;
        this.clientKey = clientKey;
        this.clientSecret = clientSecret;
        this.authClientKey = authClientKey;
        this.authClientSecret = authClientSecret;
        this.appKey = appKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getContinent() {
        return continent;
    }

    public String getCountry() {
        if ("gb".equalsIgnoreCase(country)) {
            // United Kingdom's ISO 3166 abbreviation is 'gb', but Ecovacs wants the TLD instead, which is 'uk' for
            // historical reasons
            return "uk";
        }
        return country.toLowerCase();
    }

    public String getLanguage() {
        return language;
    }

    public String getResource() {
        return deviceId.substring(0, 8);
    }

    public String getAuthOpenId() {
        return "global";
    }

    public String getTimeZone() {
        return "GMT-8";
    }

    public String getRealm() {
        return "ecouser.net";
    }

    public String getPortalAuthRequestWith() {
        return "users";
    }

    public String getOrg() {
        return "ECOWW";
    }

    public String getEdition() {
        return "ECOGLOBLE";
    }

    public String getBizType() {
        return "ECOVACS_IOT";
    }

    public String getChannel() {
        return "google_play";
    }

    public String getAppId() {
        return "ecovacs";
    }

    public String getAppPlatform() {
        return "android";
    }

    public String getAppCode() {
        return "global_e";
    }

    public String getAppVersion() {
        return "2.3.7";
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppUserAgent() {
        return "EcovacsHome/2.3.7 (Linux; U; Android 5.1.1; A5010 Build/LMY48Z)";
    }

    public String getDeviceType() {
        return "1";
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAuthClientKey() {
        return authClientKey;
    }

    public String getAuthClientSecret() {
        return authClientSecret;
    }
}
