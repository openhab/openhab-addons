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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.BindingConstants.DEFAULT_TOKEN_VALIDITY_SEC;
import static org.openhab.binding.connectedcar.internal.CarUtils.*;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ApiToken} store the API token information.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ApiToken {
    protected String accessToken = "";
    protected String idToken = "";
    protected String securityToken = "";
    protected String refreshToken = "";
    protected String xcsrf = "";

    protected int authVersion = 1;
    protected int validity = -1;
    protected String service = "";
    protected Date creationTime = new Date();

    public static class JwtToken {
        /*
         * "at_hash":"9wYmNBTSKQ8bJV7F2f4otQ",
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

    public static class OAuthToken {
        // token API
        @SerializedName("token_type")
        public String authType = "";
        @SerializedName("access_token")
        public String accessToken = "";
        @SerializedName("id_token")
        public String idToken = "";
        @SerializedName("refresh_token")
        public String refreshToken = "";
        @SerializedName("securityToken")
        public String securityToken = "";

        @SerializedName("expires_in")
        public Integer validity = -1;

        // Login API
        @SerializedName("accessToken")
        public String accessToken2 = "";
        @SerializedName("idToken")
        public String idToken2 = "";
        @SerializedName("refreshToken")
        public String refreshToken2 = "";

        public String xcsrf = "";

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
        public ApiToken apiToken = new ApiToken();
        public ApiToken idToken = new ApiToken();
        public ApiHttpClient http = new ApiHttpClient();
    }

    public ApiToken() {
    }

    public ApiToken(String idToken, String accessToken, int validity) {
        this.idToken = idToken;
        this.accessToken = accessToken;
        setValidity(validity);
    }

    public ApiToken(OAuthToken token) {
        token.normalize();
        idToken = getString(token.idToken);
        accessToken = getString(token.accessToken);
        securityToken = getString(token.securityToken);
        refreshToken = getString(token.refreshToken);
        xcsrf = getString(token.xcsrf);

        setValidity(getInteger(token.validity));
    }

    public ApiToken updateToken(OAuthToken token) {
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
        double offset = value * 0.9;
        this.validity = value - (int) offset; // reduce by 20% treshhold
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

    /**
     * Check token validity
     *
     * @return true=token still valid, false=token has expired
     */
    public boolean isValid() {
        return (!accessToken.isEmpty() || !idToken.isEmpty() || !securityToken.isEmpty()) && (validity != -1);
    }

    /**
     * Make a token invalid by marking it expired
     */
    public void invalidate() {
        validity = -1;
    }

    @Override
    public String toString() {
        String token = !securityToken.isEmpty() ? securityToken
                : !idToken.isEmpty() ? idToken : !accessToken.isEmpty() ? accessToken : "NULL";
        return token + creationTime + ", V=" + validity;
    }
}
