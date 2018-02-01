/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 * implements the login to the webinterface - second step: retrieval of the security token which is needed for all subsequent requests
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class PostLoginGetSpringSecurityToken extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;

    public PostLoginGetSpringSecurityToken(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {

        Fields fields = new Fields();
        fields.add(LOGIN_USERNAME_FIELD, handler.getConfiguration().getUsername());
        fields.add(LOGIN_PASSWORD_FIELD, handler.getConfiguration().getPassword());
        FormContentProvider cp = new FormContentProvider(fields);

        requestToPrepare.content(cp);
        requestToPrepare.followRedirects(true);
        requestToPrepare.method(HttpMethod.POST);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return POST_LOGIN_SESSION_TOKEN_URL;
    }

    @Override
    public void onComplete(Result result) {
        getListener().update(getCommunicationStatus());

    }

}
