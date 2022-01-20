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
package org.openhab.binding.blink.internal.dto;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link BlinkAccount} class is the DTO for the login api call.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
public class BlinkAccount {

    public Account account;
    public Auth auth;

    public static class Auth {
        public String token;

        @Override
        public String toString() {
            return "Auth{" + "token='" + token + '\'' + '}';
        }
    }

    public static class Account {

        public Long account_id;
        public Long user_id;
        public Long client_id;
        public String tier;
        public boolean client_verification_required = true;

        @Override
        public String toString() {
            return "Account{" + "account_id=" + account_id + ", user_id=" + user_id + ", client_id=" + client_id
                    + ", tier='" + tier + '\'' + ", client_verification_required=" + client_verification_required + '}';
        }
    }

    @Override
    public String toString() {
        return "BlinkAccount{" + "account=" + account + ", auth=" + auth + '}';
    }

    public Map<String, String> toAccountProperties() {
        Map<String, String> props = new HashMap<>();
        props.put(PROPERTY_ACCOUNT_ID, Long.toString(account.account_id));
        props.put(PROPERTY_CLIENT_ID, Long.toString(account.client_id));
        props.put(PROPERTY_TIER, account.tier);
        props.put(PROPERTY_TOKEN, auth.token);
        return props;
    }
}
