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
package org.openhab.binding.solaredge.internal.command;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;

/**
 * checks validity of the token by accessing the webinterface
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class PrivateApiTokenCheck extends AbstractCommand implements SolarEdgeCommand {

    public PrivateApiTokenCheck(SolarEdgeHandler handler, StatusUpdateListener listener) {
        super(handler.getConfiguration(), listener);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        // as a token is used no real login is to be done here. It is just checked if a protected page can be retrieved
        // and therefore the token is valid.
        requestToPrepare.followRedirects(false);
        requestToPrepare.method(HttpMethod.GET);

        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        return PRIVATE_DATA_API_URL + config.getSolarId() + PRIVATE_DATA_API_URL_LIVE_DATA_SUFFIX;
    }

    @Override
    public void onComplete(@Nullable Result result) {
        updateListenerStatus();
    }
}
