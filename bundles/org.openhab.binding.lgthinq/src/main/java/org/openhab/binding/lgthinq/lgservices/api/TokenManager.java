/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.api;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.errors.AccountLoginException;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqGatewayException;
import org.openhab.binding.lgthinq.lgservices.errors.PreLoginException;
import org.openhab.binding.lgthinq.lgservices.errors.RefreshTokenException;
import org.openhab.binding.lgthinq.lgservices.errors.TokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link TokenManager} Principal facade to manage all token handles
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class TokenManager {
    private static final int EXPIRICY_TOLERANCE_SEC = 60;
    private final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    private final LGThinqOauthEmpAuthenticator authenticator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, TokenResult> tokenCached = new ConcurrentHashMap<>();

    public TokenManager(HttpClient httpClient) {
        authenticator = new LGThinqOauthEmpAuthenticator(httpClient);
    }

    public boolean isTokenExpired(TokenResult token) {
        Calendar c = Calendar.getInstance();
        c.setTime(token.getGeneratedTime());
        c.add(Calendar.SECOND, token.getExpiresIn() - EXPIRICY_TOLERANCE_SEC);
        Date expiricyDate = c.getTime();
        return expiricyDate.before(new Date());
    }

    public TokenResult refreshToken(String bridgeName, TokenResult currentToken) throws RefreshTokenException {
        try {
            TokenResult token = authenticator.doRefreshToken(currentToken);
            objectMapper.writeValue(new File(getConfigDataFileName(bridgeName)), token);
            return token;
        } catch (IOException e) {
            throw new RefreshTokenException("Error refreshing LGThinq token", e);
        }
    }

    private String getConfigDataFileName(String bridgeName) {
        return String.format(getThinqConnectionDataFile(), bridgeName);
    }

    public boolean isOauthTokenRegistered(String bridgeName) {
        File tokenFile = new File(getConfigDataFileName(bridgeName));
        return tokenFile.isFile();
    }

    private String getGatewayUrl(String alternativeGtwServer) {
        return alternativeGtwServer.isBlank() ? LG_API_GATEWAY_URL_V2
                : (alternativeGtwServer + LG_API_GATEWAY_SERVICE_PATH_V2);
    }

    public void oauthFirstRegistration(String bridgeName, String language, String country, String username,
            String password, String alternativeGtwServer)
            throws LGThinqGatewayException, PreLoginException, AccountLoginException, TokenException, IOException {
        LGThinqGateway gw;
        LGThinqOauthEmpAuthenticator.PreLoginResult preLogin;
        LGThinqOauthEmpAuthenticator.LoginAccountResult accountLogin;
        TokenResult token;
        UserInfo userInfo;
        try {
            gw = authenticator.discoverGatewayConfiguration(getGatewayUrl(alternativeGtwServer), language, country,
                    alternativeGtwServer);
        } catch (Exception ex) {
            throw new LGThinqGatewayException("Error trying to discover the LG Gateway Setting for the region informed",
                    ex);
        }

        try {
            preLogin = authenticator.preLoginUser(gw, username, password);
        } catch (Exception ex) {
            throw new PreLoginException("Error doing pre-login of the user in the Emp LG Server", ex);
        }
        try {
            accountLogin = authenticator.loginUser(gw, preLogin);
        } catch (Exception ex) {
            throw new AccountLoginException("Error doing user's account login on the Emp LG Server", ex);
        }
        try {
            token = authenticator.getToken(gw, accountLogin);
        } catch (Exception ex) {
            throw new TokenException("Error getting Token", ex);
        }
        try {
            userInfo = authenticator.getUserInfo(token);
            token.setUserInfo(userInfo);
            token.setGatewayInfo(gw);
        } catch (Exception ex) {
            throw new TokenException("Error getting UserInfo from Token", ex);
        }

        // persist the token information generated in file
        objectMapper.writeValue(new File(getConfigDataFileName(bridgeName)), token);
    }

    public TokenResult getValidRegisteredToken(String bridgeName) throws IOException, RefreshTokenException {
        TokenResult validToken;
        TokenResult bridgeToken = tokenCached.get(bridgeName);
        if (bridgeToken == null) {
            bridgeToken = Objects.requireNonNull(
                    objectMapper.readValue(new File(getConfigDataFileName(bridgeName)), TokenResult.class),
                    "Unexpected. Never null here");
        }

        if (!isValidToken(bridgeToken)) {
            throw new RefreshTokenException(
                    "Token is not valid. Try to delete token file and disable/enable bridge to restart authentication process");
        } else {
            tokenCached.put(bridgeName, bridgeToken);
        }

        validToken = Objects.requireNonNull(bridgeToken, "Unexpected. Never null here");
        if (isTokenExpired(validToken)) {
            validToken = refreshToken(bridgeName, validToken);
        }
        return validToken;
    }

    private boolean isValidToken(@Nullable TokenResult token) {
        return token != null && !token.getAccessToken().isBlank() && token.getExpiresIn() != 0
                && !token.getOauthBackendUrl().isBlank() && !token.getRefreshToken().isBlank();
    }

    /**
     * Remove the toke file registered for the bridge. Must be called only if the bridge is removed
     */
    public void cleanupTokenRegistry(String bridgeName) {
        File f = new File(getConfigDataFileName(bridgeName));
        if (f.isFile()) {
            if (!f.delete()) {
                logger.warn("Can't delete token registry file {}", f.getName());
            }
        }
    }
}
