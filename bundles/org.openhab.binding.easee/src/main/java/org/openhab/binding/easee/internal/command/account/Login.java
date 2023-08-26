/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal.command.account;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeBridgeHandler;

import com.google.gson.JsonObject;

/**
 * implements the login to the webinterface
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class Login extends AbstractCommand {

    class LoginData {
        final String userName;
        final String password;

        public LoginData(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }

    private final LoginData loginData;

    public Login(EaseeBridgeHandler handler, JsonResultProcessor resultProcessor) {
        // flags do not matter as "onComplete" is overwritten in this class.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.NO, resultProcessor);
        loginData = new LoginData(handler.getBridgeConfiguration().getUsername(),
                handler.getBridgeConfiguration().getPassword());
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        StringContentProvider cp = new StringContentProvider(gson.toJson(loginData));
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
        return CHANNEL_GROUP_NONE;
    }
}
