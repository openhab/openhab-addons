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
package org.openhab.binding.mielecloud.internal.webservice.request;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;

/**
 * Factory for {@link Request} objects.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public interface RequestFactory extends AutoCloseable {
    /**
     * Creates a GET {@link Request} for the given URL decorated with all required headers to interact with the Miele
     * cloud.
     *
     * @param url The URL to GET.
     * @param accessToken The OAuth2 access token for bearer authentication.
     * @return The {@link Request}.
     */
    Request createGetRequest(String url, String accessToken);

    /**
     * Creates a PUT {@link Request} for the given URL decorated with all required headers to interact with the Miele
     * cloud.
     *
     * @param url The URL to PUT.
     * @param accessToken The OAuth2 access token for bearer authentication.
     * @param jsonContent Json content to send in the body of the request.
     * @return The {@link Request}.
     */
    Request createPutRequest(String url, String accessToken, String jsonContent);

    /**
     * Creates a POST {@link Request} for the given URL decorated with all required headers to interact with the Miele
     * cloud.
     *
     * @param url The URL to POST.
     * @param accessToken The OAuth2 access token for bearer authentication.
     * @return The {@link Request}.
     */
    Request createPostRequest(String url, String accessToken);

    /**
     * Creates a GET request prepared for HTTP event stream data (also referred to as Server Sent Events, SSE).
     *
     * @param url The URL to subscribe to.
     * @param accessToken The OAuth2 access token for bearer authentication.
     * @return The {@link Request}.
     */
    Request createSseRequest(String url, String accessToken);
}
