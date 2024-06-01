/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.command.account;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.myuplink.internal.command.AbstractCommand;
import org.openhab.binding.myuplink.internal.command.JsonResultProcessor;
import org.openhab.binding.myuplink.internal.handler.MyUplinkBridgeHandler;

import com.google.gson.JsonObject;

/**
 * implements the login to the webinterface
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class Login extends AbstractCommand {

    private final String encodedLogin;

    public Login(MyUplinkBridgeHandler handler, JsonResultProcessor resultProcessor) {
        // flags do not matter as "onComplete" is overwritten in this class.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.NO, resultProcessor);

        String login = handler.getBridgeConfiguration().getClientId() + ":"
                + handler.getBridgeConfiguration().getClientSecret();
        encodedLogin = Base64.getEncoder().encodeToString(login.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        Fields fields = new Fields();
        fields.add(LOGIN_FIELD_GRANT_TYPE_KEY, LOGIN_FIELD_GRANT_TYPE_VALUE);
        fields.add(LOGIN_FIELD_SCOPE_KEY, LOGIN_FIELD_SCOPE_VALUE);
        FormContentProvider cp = new FormContentProvider(fields, StandardCharsets.UTF_8);

        requestToPrepare.header(HttpHeader.AUTHORIZATION, LOGIN_BASIC_AUTH_PREFIX + encodedLogin);
        requestToPrepare.content(cp);
        requestToPrepare.method(HttpMethod.POST);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return LOGIN_URL;
    }

    @Override
    public void onComplete(@Nullable Result result) {
        String json = getContentAsString(StandardCharsets.UTF_8);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if (jsonObject != null) {
            processResult(jsonObject);
        }
    }

    @Override
    protected String getChannelGroup() {
        return EMPTY;
    }
}
