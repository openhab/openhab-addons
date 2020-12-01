/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.transports;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterId;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This class represents authorization filter used to reauthorize our sony connection
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyAuthFilter implements ClientRequestFilter, ClientResponseFilter {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(SonyAuthFilter.class);

    /** The map of current cookies used for authentication */
    private final Map<String, NewCookie> cookies = new HashMap<>();

    /** The name of the authorization cookie */
    private static final String AUTHCOOKIENAME = "auth";

    /** The base URL of the access control service */
    private final URI baseUri;

    /** The function interface to determine whether we should automatically apply the auth logic */
    private final AutoAuth autoAuth;

    /**
     * A boolean to determine if we've already tried the authorization and failed (true to continue trying, false
     * otherwise)
     */
    private final AtomicBoolean tryAuth = new AtomicBoolean(true);

    /**
     * Instantiates a new sony auth filter from the device information
     *
     * @param baseUri the non-null, base URI for the access control service
     * @param autoAuth the non-null auto auth callback
     */
    public SonyAuthFilter(final URI baseUri, final AutoAuth autoAuth) {
        Objects.requireNonNull(baseUri, "baseUrl cannot be empty");
        Objects.requireNonNull(autoAuth, "autoAuth cannot be null");
        this.baseUri = baseUri;
        this.autoAuth = autoAuth;
    }

    @Override
    public void filter(final @Nullable ClientRequestContext requestCtx) throws IOException {
        Objects.requireNonNull(requestCtx, "requestCtx cannot be null");

        boolean authNeeded = true;

        // If we had a prior cookies, check to see if any expired
        if (!cookies.isEmpty()) {
            // Iterate the cookies
            for (final Iterator<Map.Entry<String, NewCookie>> it = cookies.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, NewCookie> entry = it.next();

                final String cookieName = entry.getKey();
                final NewCookie cookie = entry.getValue();

                // Has the cookie expired...
                final Date expiryDate = cookie.getExpiry();
                if (expiryDate != null && new Date().after(expiryDate)) {
                    it.remove();
                } else {
                    if (AUTHCOOKIENAME.equalsIgnoreCase(cookieName)) {
                        authNeeded = false;
                    }
                }
            }
        }

        if (authNeeded && tryAuth.get() && autoAuth.isAutoAuth()) {
            logger.debug("Trying to renew our authorization cookie");
            final Client client = ClientBuilder.newClient();
            // client.register(new LoggingFilter());

            final String actControlUrl = NetUtil.getSonyUri(baseUri, ScalarWebService.ACCESSCONTROL);

            final WebTarget target = client.target(actControlUrl);
            final Gson gson = GsonUtilities.getDefaultGson();

            final String json = gson.toJson(new ScalarWebRequest(ScalarWebMethod.ACTREGISTER, ScalarWebMethod.V1_0,
                    new ActRegisterId(), new Object[] { new ActRegisterOptions() }));

            try {
                final Response rsp = target.request().post(Entity.json(json));

                final Map<String, NewCookie> newCookies = rsp.getCookies();
                if (newCookies != null) {
                    final NewCookie authCookie = newCookies.get(AUTHCOOKIENAME);
                    if (authCookie != null) {
                        logger.debug("Authorization cookie was renewed");
                        cookies.put(AUTHCOOKIENAME, authCookie);
                    } else {
                        logger.debug("No authorization cookie was returned");
                    }
                } else {
                    logger.debug("No authorization cookie was returned");
                }
            } catch (final ProcessingException e) {
                if (e.getCause() instanceof ConnectException) {
                    tryAuth.set(false);
                }
                logger.debug("Could not renew authorization cookie: {}", e.getMessage());
            }
        }

        requestCtx.getHeaders().put("Cookie", new ArrayList<Object>(cookies.values()));
    }

    @Override
    public void filter(final @Nullable ClientRequestContext requestCtx,
            final @Nullable ClientResponseContext responseCtx) throws IOException {
        Objects.requireNonNull(responseCtx, "responseCtx cannot be null");

        // The response may included an auth cookie that we need to save
        final Map<String, NewCookie> newCookies = responseCtx.getCookies();
        if (newCookies != null && !newCookies.isEmpty()) {
            if (newCookies.containsKey(AUTHCOOKIENAME)) {
                logger.debug("Auth cookie found and saved");
            }
            cookies.clear();
            cookies.putAll(newCookies);
        }
    }

    /**
     * This is the functional interface to determing if auto auth is needed
     */
    @NonNullByDefault
    public interface AutoAuth {
        /**
         * Determines if auto auth is needed
         * 
         * @return true if needed, false otherwise
         */
        boolean isAutoAuth();
    }
}
