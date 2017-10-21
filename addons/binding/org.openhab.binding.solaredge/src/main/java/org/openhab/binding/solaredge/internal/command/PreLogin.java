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

import java.nio.charset.Charset;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.solaredge.handler.SolarEdgeHandler;
import org.openhab.binding.solaredge.internal.callback.AbstractCommandCallback;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.model.PreLoginResponse;

/**
 * implements the login to the webinterface
 *
 * @author Alexander Friese - initial contribution
 *
 */
public class PreLogin extends AbstractCommandCallback implements SolarEdgeCommand {

    private final SolarEdgeHandler handler;

    public PreLogin(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
        this.handler = handler;
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {

        Fields fields = new Fields();
        fields.add(LOGIN_COMMAND_FIELD, LOGIN_COMMAND_VALUE);
        fields.add(LOGIN_DEMO_FIELD, LOGIN_DEMO_VALUE);
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
        return PRE_LOGIN_URL;
    }

    @Override
    public void onComplete(Result result) {
        logger.debug("onComplete()");

        String json = getContentAsString(Charset.forName("UTF-8"));
        if (json != null) {
            PreLoginResponse jsonObject = convertJson(json, PreLoginResponse.class);
            if (jsonObject != null) {
                String failure = jsonObject.getFailure();
                if (failure != null && !failure.isEmpty()) {
                    // this should not happen!
                    if (getCommunicationStatus().getHttpCode().equals(HttpStatus.Code.OK)) {
                        getCommunicationStatus().setHttpCode(Code.INTERNAL_SERVER_ERROR);
                    }

                    getCommunicationStatus().setError(new RuntimeException(failure));

                }
            }
        }

        if (getListener() != null) {
            getListener().update(getCommunicationStatus());
        }
    }

}
