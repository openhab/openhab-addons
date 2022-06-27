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
package org.openhab.binding.jellyfin.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jellyfin.sdk.api.client.Response;
import org.jellyfin.sdk.api.client.exception.ApiClientException;

/**
 * The {@link SyncResponse} util to consume sdk api calls.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class SyncResponse<T> extends SyncCallback<Response<T>> {
    public Response<T> awaitResponse() throws ApiClientException, SyncCallbackError {
        try {
            return awaitResult();
        } catch (SyncCallbackError e) {
            var cause = e.getCause();
            if (cause instanceof ApiClientException) {
                throw (ApiClientException) cause;
            }
            throw e;
        }
    }

    public T awaitContent() throws ApiClientException, SyncCallbackError {
        var responseContent = awaitResponse().getContent();
        if (responseContent == null) {
            throw new SyncCallbackError("Missing content");
        }
        return responseContent;
    }
}
