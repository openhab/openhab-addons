package org.openhab.binding.blink.internal.dto;

import java.util.HashMap;
import java.util.Map;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.*;

public class BlinkAccount {

    public Account account;
    public Auth auth;
    public String generatedClientId;

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
            return "Account{" + "account_id=" + account_id + ", user_id=" + user_id + ", client_id=" + client_id + ", tier='" + tier + '\'' + ", client_verification_required=" + client_verification_required + '}';
        }
    }

    @Override
    public String toString() {
        return "BlinkAccount{" + "account=" + account + ", auth=" + auth + ", generatedClientId='" + generatedClientId + '\'' + '}';
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
