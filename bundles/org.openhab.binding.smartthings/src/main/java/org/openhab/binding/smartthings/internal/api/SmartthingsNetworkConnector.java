/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.smartthings.internal.type.SmartthingsException;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface SmartthingsNetworkConnector {

    public <T> T doRequest(Class<T> resultClass, String req, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws SmartthingsException;

    public @Nullable String doBasicRequest(String uri, @Nullable SmartthingsNetworkCallback callback,
            String accessToken, @Nullable String data, HttpMethod method) throws SmartthingsException;

    public void waitAllPendingRequest();

    public void waitNoNewRequest();

    public void onComplete(Request request);

    public void onError(Request request, SmartthingsNetworkCallback cb) throws Exception;
}
