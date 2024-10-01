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
package org.openhab.binding.siemenshvac.internal.network;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeConfig;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeThingHandler;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface SiemensHvacConnector {

    @Nullable
    String doBasicRequest(String uri) throws SiemensHvacException;

    @Nullable
    JsonObject doRequest(String req);

    @Nullable
    JsonObject doRequest(String req, @Nullable SiemensHvacCallback callback);

    void waitAllPendingRequest();

    void waitNoNewRequest();

    void onComplete(Request request, SiemensHvacRequestHandler reqListener) throws SiemensHvacException;

    void onError(Request request, SiemensHvacRequestHandler reqListener,
            SiemensHvacRequestListener.ErrorSource errorSource, boolean mayRetry) throws SiemensHvacException;

    void setSiemensHvacBridgeBaseThingHandler(SiemensHvacBridgeThingHandler hvacBridgeBaseThingHandler);

    @Nullable
    SiemensHvacBridgeConfig getBridgeConfiguration();

    void resetSessionId(@Nullable String sessionIdToInvalidate, boolean web);

    void displayRequestStats();

    Gson getGson();

    Gson getGsonWithAdapter();

    int getRequestCount();

    int getErrorCount();

    SiemensHvacRequestListener.ErrorSource getErrorSource();

    void invalidate();

    void setTimeOut(int timeout);
}
