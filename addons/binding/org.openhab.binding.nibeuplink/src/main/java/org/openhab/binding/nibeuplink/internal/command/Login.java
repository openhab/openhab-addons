/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.command;

import static org.openhab.binding.nibeuplink.NibeUplinkBindingConstants.*;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.nibeuplink.handler.NibeUplinkHandler;
import org.openhab.binding.nibeuplink.internal.callback.AbstractUplinkCommandCallback;
import org.openhab.binding.nibeuplink.internal.connector.StatusUpdateListener;

/**
 * implements the login to the webinterface
 *
 * @author Alexander Friese - initial contribution
 */
public class Login extends AbstractUplinkCommandCallback implements NibeUplinkCommand {

    public Login(NibeUplinkHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {

        Fields fields = new Fields();
        fields.add(LOGIN_FIELD_EMAIL, config.getUser());
        fields.add(LOGIN_FIELD_PASSWORD, config.getPassword());
        fields.add(LOGIN_FIELD_RETURN_URL, "");
        FormContentProvider cp = new FormContentProvider(fields);

        requestToPrepare.content(cp);
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.POST);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return LOGIN_URL;
    }

    @Override
    public void onComplete(Result result) {
        StatusUpdateListener listener = getListener();
        if (listener != null) {
            listener.update(getCommunicationStatus());
        }
    }
}
