/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.SolarEdgeBindingConstants.*;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;

/**
 * implements the login to the webinterface
 *
 * @author afriese
 *
 */
public class Login extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;

    public Login(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare, CookieStore cookieStore) {

        // as a token is used no real login is to be done here. IT is just checked if a protected page can be retrieved
        // and therefore the token is valid.
        HttpCookie c = new HttpCookie(TOKEN_COOKIE_NAME, config.getToken());
        c.setDomain(TOKEN_COOKIE_DOMAIN);
        c.setPath(TOKEN_COOKIE_PATH);
        cookieStore.add(URI.create(getURL()), c);

        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return DATA_API_URL + config.getSolarId() + DATA_API_URL_LIVE_DATA_SUFFIX;
    }

    @Override
    public void onComplete(Result result) {
        getListener().update(getCommunicationStatus());
    }

}
