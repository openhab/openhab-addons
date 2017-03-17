/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.api.manager;

import java.io.IOException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.mihome.internal.rest.RestClient;

import com.google.gson.JsonObject;

/**
 * An interface for handling failed request of the {@link MiHomeApiManager}.
 *
 * @author Mihaela Memova
 *
 */
public interface FailingRequestHandler {

    /**
     * Handles HTTP request (with status different than 200 OK)
     *
     * @param response - response of the request
     */
    void handleFailingHttpRequest(ContentResponse response);

    /**
     * Handles HTTP requests that doesn't contains JSON as response content or the JSON content is invalid
     * <p>
     * See the {@link MiHomeApiManager} methods
     *
     * @param jsonResponse
     */
    void handleFailingJsonRequest(JsonObject jsonResponse);

    /**
     * Handles exceptions during execution of the request that are thrown from
     * {@link RestClient#sendRequest(String, org.eclipse.jetty.http.HttpMethod, java.util.Properties, java.io.InputStream, String)}
     * method
     *
     * @param failedUrl
     * @param e1
     */
    void handleIOException(String failedUrl, IOException e1);
}
