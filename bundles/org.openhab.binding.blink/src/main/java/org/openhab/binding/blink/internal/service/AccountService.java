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
package org.openhab.binding.blink.internal.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.blink.internal.config.AccountConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.binding.blink.internal.dto.BlinkValidation;

import com.google.gson.Gson;

/**
 * The {@link AccountService} class handles all communication with account related blink apis.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class AccountService extends BaseBlinkApiService {

    public AccountService(HttpClient httpClient, Gson gson) {
        super(httpClient, gson);
    }

    /**
     * Login and get an authorisation token.
     * An additional 2FA verification step could be necessary for previously unregistered clients.
     *
     * @param configuration AccountConfiguration containing login data
     * @return login result containing authorisation information
     */
    public BlinkAccount login(@Nullable AccountConfiguration configuration, String generatedClientId, boolean start2FA)
            throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("Cannot authenticate without configuration");
        }
        String uri = "/api/v5/account/login";
        Map<String, String> params = new HashMap<>();
        params.put("email", configuration.email);
        params.put("password", configuration.password);
        params.put("unique_id", generatedClientId);
        if (!start2FA) {
            params.put("reauth", "true");
        }
        BlinkAccount login = apiRequest("prod", uri, HttpMethod.POST, null, params, BlinkAccount.class);
        if (login.account == null || login.auth == null)
            throw new IOException("Did not receive valid account or token from API.");
        login.generatedClientId = generatedClientId;
        return login;
    }

    /**
     * Verifies the 2FA pin for the client sent to the account using SMS or email.
     * This should only be necessary once for each clientId.
     *
     * @param account Login result from login API call
     * @param pin 2FA pin sent to the account
     * @return result of verification
     */
    public boolean verifyPin(@Nullable BlinkAccount account, String pin) throws IOException {
        if (account == null || account.account == null)
            throw new IllegalArgumentException("Trying to do 2FA without a login");
        String uri = "/api/v4/account/" + account.account.account_id + "/client/" + account.account.client_id
                + "/pin/verify";
        Map<String, String> params = new HashMap<>();
        params.put("pin", pin);
        BlinkValidation validation = apiRequest(account.account.tier, uri, HttpMethod.POST, account.auth.token, params,
                BlinkValidation.class);
        if (validation == null)
            return false;
        return validation.valid;
    }

    public BlinkHomescreen getDevices(@Nullable BlinkAccount account) throws IOException {
        if (account == null || account.account == null)
            throw new IllegalArgumentException("Cannot call Blink API without account");
        String uri = "/api/v3/accounts/" + account.account.account_id + "/homescreen";
        return apiRequest(account.account.tier, uri, HttpMethod.GET, account.auth.token, null, BlinkHomescreen.class);
    }

    /**
     * Generates a client ID in the format used by the Blink API.
     * This should be in the format as seen in
     * <a href="https://www.drdsnell.com/projects/hubitat/drivers/BlinkAPI.groovy"/>BlinkAPI.groovy</a>
     *
     * @return random client id in Blink API format
     */
    public String generateClientId() {
        return "BlinkCamera_" + randomNumber(4) + "-" + randomNumber(2) + "-" + randomNumber(2) + "-" + randomNumber(2)
                + "-" + randomNumber(6);
    }

    /**
     * Generates a random number with the given number of digits, with leading zeros.
     * Number of digits must be less than 10 (that way the String could be parsed to int precision).
     *
     * @param digits number of digits to generate (between 1 and 9)
     * @return formatted string of generated int with possible leading zeros.
     */
    String randomNumber(int digits) {
        if (digits < 1 || digits > 9)
            throw new IllegalArgumentException("Number of digits must be between 1 and 9.");
        Random random = new Random();
        StringBuilder b = new StringBuilder(digits);
        IntStream.range(0, digits).forEach(i -> b.append(random.nextInt(10)));
        return b.toString();
    }
}
