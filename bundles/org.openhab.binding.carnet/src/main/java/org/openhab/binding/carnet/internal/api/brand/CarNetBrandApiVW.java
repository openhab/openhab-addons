/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api.brand;

import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetBrandAuthenticator;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetBrandApiVW} provides the VW specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandApiVW extends CarNetApiBase implements CarNetBrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(CarNetBrandApiVW.class);

    public CarNetBrandApiVW(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        super(httpClient, tokenManager);
    }

    @Override
    public CarNetApiProperties getProperties() {
        CarNetApiProperties properties = new CarNetApiProperties();
        properties.brand = CNAPI_BRAND_VW;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://msg.volkswagen.de/fs-car";
        properties.clientId = "9496332b-ea03-4091-a224-8c746b885068@apps_vw-dilab_com";
        properties.xClientId = "38761134-34d0-41f3-9a73-c4be88d7d337";
        properties.authScope = "openid profile mbb email cars birthdate badge address vin";
        properties.redirect_uri = "carnet://identity-kit/Flogin";
        properties.xrequest = "de.volkswagen.carnet.eu.eremote";
        properties.responseType = "id_token token code";
        properties.xappName = "eRemote";
        properties.xappVersion = "5.1.2";
        return properties;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws CarNetException {
        return url; // + "&code_challenge=" + codeChallenge + "&code_challenge_method=S256";
    }

    @Override
    public @Nullable String getPersonalData() throws CarNetException {
        /*
         * url: "https://customer-profile.apps.emea.vwapps.io/v1/customers/" + this.config.userid + "/personalData",
         * headers: {
         * "user-agent": "okhttp/3.7.0",
         * "X-App-version": this.xappversion,
         * "X-App-name": this.xappname,
         * authorization: "Bearer " + this.config.atoken,
         * accept: "application/json",
         * Host: "customer-profile.apps.emea.vwapps.io",
         * },
         */
        String json = "{}";
        try {
            String url = "https://customer-profile.apps.emea.vwapps.io/v1/customers/"
                    + UrlEncoded.encodeString(config.account.user) + "/personalData";
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
            headers.put(CNAPI_HEADER_APP, config.api.xappName);
            headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
            headers.put(HttpHeader.AUTHORIZATION.toString(), createVwToken());
            headers.put(HttpHeader.ACCEPT.toString(), CNAPI_ACCEPTT_JSON);
            headers.put(HttpHeader.HOST.toString(), "customer-profile.apps.emea.vwapps.io");
            json = http.get(url, headers, createVwToken());
            return json;
        } catch (CarNetException e) {
            logger.debug("{}: API call getPersonalData failed: {}", config.vehicle.vin, e.toString());
        } catch (RuntimeException e) {
            logger.debug("{}: API call getPersonalData failed", config.vehicle.vin, e);
        }
        return null;
    }

    /*
     * private String[] getCodeChallenge() {
     * String hash = "";
     * String result = "";
     * while (hash === "" || hash.indexOf("+") !== -1 || hash.indexOf("/") !== -1 || hash.indexOf("=") !== -1 ||
     * result.indexOf("+") !== -1 || result.indexOf("/") !== -1) {
     * String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
     * result = "";
     * for (let i = 64; i > 0; --i) {
     * result += chars[Math.floor(Math.random() * chars.length)];
     * }
     * result = Buffer.from(result).toString("base64");
     * result = result.replace(/=/g, "");
     * hash = crypto.createHash("sha256").update(result).digest("base64");
     * hash = hash.slice(0, hash.length - 1);
     * }
     * return [result, hash];
     * }
     */
}
