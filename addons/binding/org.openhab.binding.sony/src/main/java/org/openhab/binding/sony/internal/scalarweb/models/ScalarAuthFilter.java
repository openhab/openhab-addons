/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterId;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarAuthFilter.
 *
 * @author Tim Roberts - Initial contribution
 */
public class ScalarAuthFilter implements ClientRequestFilter, ClientResponseFilter {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ScalarAuthFilter.class);

    /** The device info. */
    private final ScalarWebDeviceInfo _deviceInfo;

    /** The cookies. */
    private final Map<String, NewCookie> _cookies = new HashMap<String, NewCookie>();

    /** The Constant AuthCookieName. */
    private static final String AuthCookieName = "auth";

    /**
     * Instantiates a new scalar auth filter.
     *
     * @param deviceInfo the device info
     */
    public ScalarAuthFilter(ScalarWebDeviceInfo deviceInfo) {
        _deviceInfo = deviceInfo;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.ws.rs.client.ClientResponseFilter#filter(javax.ws.rs.client.ClientRequestContext,
     * javax.ws.rs.client.ClientResponseContext)
     */
    @Override
    public void filter(ClientRequestContext requestCtx, ClientResponseContext responseCtx) throws IOException {
        // The response may included an auth cookie that we need to save
        final Map<String, NewCookie> newCookies = responseCtx.getCookies();
        if (newCookies != null && newCookies.size() > 0) {
            if (newCookies.containsKey(AuthCookieName)) {
                logger.debug("Auth cookie found and saved");
            }
            _cookies.clear();
            _cookies.putAll(newCookies);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.ws.rs.client.ClientRequestFilter#filter(javax.ws.rs.client.ClientRequestContext)
     */
    @Override
    public void filter(ClientRequestContext requestCtx) throws IOException {
        // If we had a prior cookies, check to see if any expired
        if (_cookies.size() > 0) {

            // Iterate the cookies
            for (final Iterator<Map.Entry<String, NewCookie>> it = _cookies.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<String, NewCookie> entry = it.next();

                final String cookieName = entry.getKey();
                final NewCookie cookie = entry.getValue();

                // Has the cookie expired...
                final Date expiryDate = cookie.getExpiry();
                if (expiryDate != null && new Date().after(expiryDate)) {

                    // was the cookie that expired the authorization cookie?
                    // if so, try to renew it via an out-of-band actRegister query
                    if (AuthCookieName.equalsIgnoreCase(cookieName)) {
                        final ScalarWebService acService = _deviceInfo.getService(ScalarWebService.AccessControl);
                        if (acService != null) {
                            logger.debug("Trying to renew our authorization cookie");
                            final Client client = ClientBuilder.newClient();
                            // client.register(new LoggingFilter());
                            final WebTarget target = client.target(acService.getBaseUri());
                            final Gson gson = new Gson();
                            final String version = acService.getVersion();

                            final String json = gson.toJson(new ScalarWebRequest(1, ScalarWebMethod.ActRegister,
                                    version, new ActRegisterId(), new Object[] { new ActRegisterOptions() }));
                            final Response rsp = target.request().post(Entity.json(json));

                            final Map<String, NewCookie> newCookies = rsp.getCookies();
                            if (newCookies != null) {
                                final NewCookie authCookie = newCookies.get(AuthCookieName);
                                if (authCookie != null) {
                                    logger.debug("Authorization cookie was renewed");
                                    entry.setValue(authCookie);
                                } else {
                                    logger.debug("No authorization cookie was returned");
                                    it.remove(); // and remove the cookie
                                }
                            } else {
                                logger.debug("No authorization cookie was returned");
                                it.remove(); // and remove the cookie
                            }
                        } else {
                            // this really shouldn't happen
                            logger.debug("No access control service found!");
                            it.remove(); // and remove the cookie
                        }
                    } else {
                        // remove the expired cookie
                        it.remove();
                    }
                }
            }

            requestCtx.getHeaders().put("Cookie", new ArrayList<Object>(_cookies.values()));
        }
    }

}
