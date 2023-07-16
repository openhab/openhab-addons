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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.BindingConstants.DEFAULT_TOKEN_VALIDITY_SEC;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ApiIdentity} stores the API token information.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiIdentity {
    protected String accessToken = "";
    protected String wcAccessToken = ""; // WeCharge / WeConnect
    protected String idToken = "";
    protected String securityToken = "";
    protected String refreshToken = "";

    protected int authVersion = 1;
    protected int validity = -1;
    protected String service = "";
    protected Date creationTime = new Date();

    public static class JwtToken {
        /*
         * "at_hash":"9wYmNBTSKQ8bJVXXXXXXXX",
         * "sub":"c3ab56e9-XXXX-41c8-XXXX-XXXXXXXX",
         * "email_verified":true,
         * "cor":"DE",
         * "iss":"https:\/\/identity.vwgroup.io",
         * "jtt":"id_token",
         * "type":"identity",
         * "nonce":"MTYyMjMxNzA0MTQ5OA==",
         * "lee":[
         * "AUDI"
         * ],
         * "aud":[
         * "09b6cbec-cd19-4589-82fd-363dfa8c24da@apps_vw-dilab_com",
         * "VWGMBB01DELIV1",
         * "https:\/\/api.vas.eu.dp15.vwg-connect.com",
         * "https:\/\/api.vas.eu.wcardp.io"
         * ],
         * "acr":"https:\/\/identity.vwgroup.io\/assurance\/loa-2",
         * "updated_at":1617052457793,
         * "aat":"identitykit",
         * "exp":1622320642,
         * "iat":1622317042,
         * "jti":"1cb4abb3-497d-4f46-a300-669223f830ee",
         * "email":"user@me.com"
         *
         */
        public String sub = "";
        public Boolean email_verified = false;
        public String cor = "";
        public String type = "";
        public String nonce = "";
    }

    /* Consolidated OAuth token - maps multiple formats into a unified one */
    public static class OAuthToken {
        // token API
        @SerializedName("token_type")
        public String authType = "";
        @SerializedName("access_token")
        public String accessToken = "";
        @SerializedName("wc_access_token") // WeCharge
        public String wcAccessToken = "";
        @SerializedName("id_token")
        public String idToken = "";
        @SerializedName("refresh_token")
        public String refreshToken = "";
        @SerializedName("securityToken")
        public String securityToken = "";

        @SerializedName("expires_in")
        public Integer validity = -1;

        // Login API
        @SerializedName("accessToken") // WeConnect
        public String accessToken2 = "";
        @SerializedName("idToken") // WeConnect
        public String idToken2 = "";
        @SerializedName("refreshToken")
        public String refreshToken2 = "";

        public OAuthToken normalize() {
            // Map We Connect format to generic one
            if (accessToken.isEmpty() && !accessToken2.isEmpty()) {
                accessToken = accessToken2;
            }
            if (idToken.isEmpty() && !idToken2.isEmpty()) {
                idToken = idToken2;
            }
            if (refreshToken.isEmpty() && !refreshToken2.isEmpty()) {
                refreshToken = refreshToken2;
            }
            return this;
        }
    }

    public static class TokenSet {
        public ApiIdentity apiToken = new ApiIdentity();
        public ApiIdentity idToken = new ApiIdentity();
        public ApiHttpClient http = new ApiHttpClient();
    }

    public ApiIdentity() {
    }

    public ApiIdentity(String idToken, String accessToken, int validity) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        setValidity(validity);
    }

    public ApiIdentity(OAuthToken token) {
        token.normalize();
        idToken = getString(token.idToken);
        accessToken = getString(token.accessToken);
        securityToken = getString(token.securityToken);
        wcAccessToken = getString(token.wcAccessToken);
        refreshToken = getString(token.refreshToken);

        setValidity(getInteger(token.validity));
    }

    public ApiIdentity updateToken(OAuthToken token) {
        if (!getString(token.idToken).isEmpty()) {
            this.idToken = getString(token.idToken);
        }
        if (!getString(token.accessToken).isEmpty()) {
            this.accessToken = getString(token.accessToken);
        }
        if (!getString(token.refreshToken).isEmpty()) {
            this.refreshToken = getString(token.refreshToken);
        }
        if (!getString(token.securityToken).isEmpty()) {
            this.securityToken = getString(token.securityToken);
        }
        if (token.validity != -1) {
            setValidity(token.validity);
        }
        return this;
    }

    public void setValidity(int expiresIn) {
        creationTime = new Date();
        int value = expiresIn <= 0 ? DEFAULT_TOKEN_VALIDITY_SEC : expiresIn;
        double offset = value * 0.2; // reduce by 20% treshhold
        this.validity = value - (int) offset;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Check token validity
     *
     * @return true=token still valid, false=token has expired
     */
    public boolean isValid() {
        return (!accessToken.isEmpty() || !idToken.isEmpty() || !securityToken.isEmpty()) && (validity != -1);
    }

    /**
     * Check if access token is still valid
     *
     * @return false: token invalid or expired
     */
    public Boolean isExpired() {
        if (!isValid()) {
            return true;
        }
        Date currentTime = new Date();
        long diff = currentTime.getTime() - creationTime.getTime();
        return (diff / 1000) > validity;
    }

    public void invalidate() {
        validity = -1; // Make a token invalid by marking it expired
    }
}
