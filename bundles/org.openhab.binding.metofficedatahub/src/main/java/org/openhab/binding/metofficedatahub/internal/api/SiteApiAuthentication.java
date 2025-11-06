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
package org.openhab.binding.metofficedatahub.internal.api;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.GET_FORECAST_API_KEY_HEADER;
import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.GSON;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * This handles the authentication aspects of the Site API
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class SiteApiAuthentication {

    private final Object isAuthenticatedWriteLock = new Object();

    private final Logger logger = LoggerFactory.getLogger(SiteApiAuthentication.class);

    private Boolean isAuthenticated = false;
    private String apiKey = "";

    public SiteApiAuthentication() {
    }

    public void dispose() {
    }

    public void setApiKey(final String newApiKey, final boolean validate) throws AuthTokenException {
        this.apiKey = "";

        // Perform some basic token checks, as data that isn't even JWT formatted will give a
        // 500 response from the Met Office servers rather than a 401 response.
        try {
            final String[] chunks = newApiKey.split("\\.");
            if (chunks.length != 3) {
                logger.trace("API JWT Invalid due to missing header / payload / signature");
                throw new AuthTokenException();
            }
            final Base64.Decoder decoder = Base64.getUrlDecoder();
            try {
                final JwtTokenHeader headers = GSON
                        .fromJson(new String(decoder.decode(chunks[0]), StandardCharsets.UTF_8), JwtTokenHeader.class);
                if (headers == null || !headers.isValid()) {
                    logger.trace("API JWT Invalid due to headers");
                    throw new AuthTokenException();
                }

                final JwtTokenPayload payload = GSON
                        .fromJson(new String(decoder.decode(chunks[1]), StandardCharsets.UTF_8), JwtTokenPayload.class);
                if (payload == null || !payload.isValid()) {
                    logger.trace("API JWT Invalid due to payload");
                    throw new AuthTokenException();
                }

                decoder.decode(chunks[2]); // check base64 encoding of signature
            } catch (JsonSyntaxException | IllegalArgumentException e) {
                throw new AuthTokenException();
            }
        } catch (AuthTokenException ate) {
            if (validate) {
                throw ate;
            }
        }

        if (validate) {
            logger.trace("Validated API JWT token successfully");
        } else {
            logger.trace("API JWT token accepted without validation");
        }
        this.apiKey = newApiKey;
    }

    /**
     * Adds the required data to the Jetty Request, for the authentication of the request
     * 
     * @param req is the request to have the relevant headers, etc added
     * @return Request is the passed to Request arg to allow chained call's.
     */
    public Request addAuthentication(final Request req) throws AuthTokenException {
        if (apiKey.isBlank()) {
            throw new AuthTokenException();
        }
        return req.header(GET_FORECAST_API_KEY_HEADER, apiKey);
    }

    /**
     * Process the given Result from a Jetty Client request, and returns true if the Result
     * indicates that there was not a authentication issue.
     * 
     * @param result is the Jetty Client Result to check for authentication issues
     * @return Result is the passed to Result arg to allow chained call's.
     */
    public Result processResult(final Result result) {
        if (result.isSucceeded()) {
            switch (result.getResponse().getStatus()) {
                case HttpStatus.FORBIDDEN_403:
                    setIsAuthenticated(false);
                    break;
                case HttpStatus.OK_200:
                    setIsAuthenticated(true);
                    break;
            }
        }
        return result;
    }

    /**
     * Return whether the current key has been validated as authenticated
     *
     * @return true if the last result processed indicated a validated api key.
     */
    public boolean getIsAuthenticated() {
        return this.isAuthenticated;
    }

    /**
     * Set's the state of the isAuthenticated state, using COW principles.
     */
    private void setIsAuthenticated(final boolean isAuthenticated) {
        synchronized (isAuthenticatedWriteLock) {
            this.isAuthenticated = Boolean.valueOf(isAuthenticated);
        }
    }
}
