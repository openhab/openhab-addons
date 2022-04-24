/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.handler.EaseeHandler;
import org.openhab.binding.easee.internal.model.GenericErrorResponse;
import org.openhab.binding.easee.internal.model.account.AuthenticationDataResponse;
import org.openhab.binding.easee.internal.model.account.AuthenticationResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implements the login to the webinterface
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class Login extends AbstractCommand implements EaseeCommand {
    private final Logger logger = LoggerFactory.getLogger(Login.class);

    class LoginData {
        final String userName;
        final String password;

        public LoginData(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }

    private final LoginData loginData;

    public Login(EaseeHandler handler) {
        super(handler);
        loginData = new LoginData(handler.getConfiguration().getUsername(), handler.getConfiguration().getPassword());
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
        logger.debug("onComplete()");

        String json = getContentAsString(StandardCharsets.UTF_8);
        AuthenticationResultData data = new AuthenticationResultData();

        switch (getCommunicationStatus().getHttpCode()) {
            case OK:
                AuthenticationDataResponse successResponse = gson.fromJson(json, AuthenticationDataResponse.class);
                data.setSuccessResponse(successResponse);
                break;
            case BAD_REQUEST:
            case UNAUTHORIZED:
            case FORBIDDEN:
                GenericErrorResponse errorResponse = gson.fromJson(json, GenericErrorResponse.class);
                data.setErrorResponse(errorResponse);
                break;
            default:
                break;
        }

        updateListenerStatus(data);
    }

    @Override
    protected String getChannelGroup() {
        return CHANNEL_GROUP_NONE;
    }
}
