/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;

import com.google.gson.Gson;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface SmartThingsNetworkConnector {

    public <T> T doRequest(Class<T> resultClass, String req, @Nullable SmartThingsNetworkCallback<T> callback,
            String accessToken, @Nullable String data, HttpMethod method) throws SmartThingsException;

    public <T> @Nullable String doBasicRequest(Class<T> resultClass, String uri,
            @Nullable SmartThingsNetworkCallback<T> callback, String accessToken, @Nullable String data,
            HttpMethod method) throws SmartThingsException;

    public void waitAllPendingRequest();

    public void waitNoNewRequest();

    public void onComplete(Request request);

    public <T> void onError(Request request, SmartThingsNetworkCallback<T> cb) throws Exception;

    public Gson getGson();
}
