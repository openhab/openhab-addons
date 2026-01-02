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
package org.openhab.binding.blink.internal.dto;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link BlinkAccount} class is the DTO for the login api call.
 *
 * @author Matthias Oesterheld - Initial contribution
 * @author Robert T. Brown (-rb) - support Blink Authentication changes in 2025 (OAUTHv2)
 */
public class BlinkAccount {

    public Account account;
    public Auth auth;
    public Instant lastTokenRefresh = Instant.EPOCH;
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());
    static DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    // This inner class is a DTO suitable for GSON-construction from the Blink authentication API.
    public static class Auth {
        public String access_token;
        public String refresh_token;
        public Integer expires_in;
        public Instant tokenExpiresAt = Instant.EPOCH;

        public Auth() {
        }

        public Auth(Auth that) {
            this.access_token = that.access_token;
            this.refresh_token = that.refresh_token;
            this.expires_in = that.expires_in;
            this.tokenExpiresAt = that.tokenExpiresAt;
        }

        @Override
        public String toString() {
            String shortAccessToken = "(empty)";
            String shortRefreshToken = "(empty)";
            if (access_token != null) {
                shortAccessToken = access_token.substring(0, 4) + "____REDACTED____"
                        + access_token.substring(access_token.length() - 4);
            }
            if (refresh_token != null) {
                shortRefreshToken = refresh_token.substring(0, 4) + "____REDACTED____"
                        + refresh_token.substring(refresh_token.length() - 4);
            }

            return "Auth{" + "access_token='" + shortAccessToken + "', refresh_token='" + shortRefreshToken
                    + "', tokenExpiresAt=" + dateTimeFormatter.format(tokenExpiresAt) + "}";
        }
    }

    public static class Account {
        public String account_id;
        public String user_id;
        public String tier;
        public String hardware_id;

        public Account() {
        }

        public Account(Account that) {
            this.account_id = that.account_id;
            this.user_id = that.user_id;
            this.tier = that.tier;
            this.hardware_id = that.hardware_id;
        }

        @Override
        public String toString() {
            return "Account{" + "account_id=" + account_id + ", user_id=" + user_id + ", tier='" + tier + '\''
                    + ", hardware_id=" + hardware_id + "}";
        }
    }

    public BlinkAccount() {
        this.auth = new Auth();
        this.account = new Account();
    }

    public BlinkAccount(BlinkAccount that) {
        this.account = new Account(that.account);
        this.auth = new Auth(that.auth);
        this.lastTokenRefresh = that.lastTokenRefresh;
    }

    @Override
    public String toString() {
        return "BlinkAccount{" + "account=" + account + ", auth=" + auth + '}';
    }

    public Map<String, String> toAccountProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(PROPERTY_ACCOUNT_ID, account.account_id);
        props.put(PROPERTY_USER_ID, account.user_id);
        props.put(PROPERTY_TIER, account.tier);
        props.put(PROPERTY_LAST_TOKEN_REFRESH, shortFormatter.format(lastTokenRefresh));
        props.put(PROPERTY_TOKEN_EXPIRES, shortFormatter.format(auth.tokenExpiresAt));
        props.put(PROPERTY_HARDWARE_ID, account.hardware_id);
        return props;
    }
}
