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
package org.openhab.binding.easee.internal.command.account;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.REFRESH_TOKEN_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeBridgeHandler;

/**
 * implements the refresh of the access token.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class RefreshToken extends Login {

    class RefreshData {
        final String accessToken;
        final String refreshToken;

        public RefreshData(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    private final RefreshData refreshData;

    public RefreshToken(EaseeBridgeHandler handler, String accessToken, String refreshToken,
            JsonResultProcessor resultProcessor) {
        super(handler, resultProcessor);
        refreshData = new RefreshData(accessToken, refreshToken);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        StringContentProvider cp = new StringContentProvider(gson.toJson(refreshData));
        requestToPrepare.content(cp);
        requestToPrepare.method(HttpMethod.POST);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return REFRESH_TOKEN_URL;
    }
}
