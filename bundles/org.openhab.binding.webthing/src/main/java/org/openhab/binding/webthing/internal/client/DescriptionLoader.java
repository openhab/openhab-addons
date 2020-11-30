/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthing.internal.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class to load the WebThing description (meta data). Refer https://iot.mozilla.org/wot/#web-thing-description
 */
@NonNullByDefault
public class DescriptionLoader {
    private final HttpClient httpClient;

    /**
     * constructor
     */
    public DescriptionLoader() {
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build());
    }

    /**
     * constructor
     *
     * @param httpClient the http client to use
     */
    DescriptionLoader(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * loads the WebThing meta data
     *
     * @param webthingURI the WebThing URI
     * @param timeout the timeout
     * @return the Webthing description
     * @throws IOException if the WebThing can not be connected
     */
    public WebThingDescription loadWebthingDescription(URI webthingURI, Duration timeout) throws IOException {
        try {
            var request = HttpRequest.newBuilder().timeout(timeout).GET().uri(webthingURI).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException(
                        "could not read resource description " + webthingURI + ". Got " + response.statusCode());
            }
            var description = new Gson().fromJson(response.body(), WebThingDescription.class);
            if (description.properties.size() > 0) {
                return description;
            } else {
                throw new IOException("description does not include properties");
            }
        } catch (JsonSyntaxException se) {
            throw new IOException("resource seems not to be a WebThing. Typo?");
        } catch (InterruptedException ie) {
            throw new IOException(ie.getMessage());
        }
    }
}
