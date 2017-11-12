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

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;

/**
 * implements the login to the webinterface - retrieval of client cookie: needed for some requests such as the legacy-live-data retrieval
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class PostLoginGetClientCookie extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;

    public PostLoginGetClientCookie(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {

        Fields fields = new Fields();
        fields.add(POST_LOGIN_CLIENT_CMD_FIELD, POST_LOGIN_CLIENT_CMD_VALUE);
        fields.add(POST_LOGIN_CLIENT_TARGET_FIELD,
                POST_LOGIN_CLIENT_TARGET_VALUE + handler.getConfiguration().getSolarId());
        fields.add(POST_LOGIN_CLIENT_CLIENT_FIELD, POST_LOGIN_CLIENT_CLIENT_VALUE);
        FormContentProvider cp = new FormContentProvider(fields);
        requestToPrepare.content(cp);

        // requestToPrepare.param(POST_LOGIN_CLIENT_CMD_FIELD, POST_LOGIN_CLIENT_CMD_VALUE);
        // requestToPrepare.param(POST_LOGIN_CLIENT_TARGET_FIELD,
        // POST_LOGIN_CLIENT_TARGET_VALUE + handler.getConfiguration().getSolarId());
        // requestToPrepare.param(POST_LOGIN_CLIENT_CLIENT_FIELD, POST_LOGIN_CLIENT_CLIENT_VALUE);

        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.POST);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return POST_LOGIN_CLIENT_COOKIE_URL;
    }

    @Override
    public void onComplete(Result result) {
        getListener().update(getCommunicationStatus());

    }

}
