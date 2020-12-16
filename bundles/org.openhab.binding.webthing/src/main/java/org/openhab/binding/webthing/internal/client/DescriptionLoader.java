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
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class to load the WebThing description (meta data). Refer https://iot.mozilla.org/wot/#web-thing-description
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class DescriptionLoader {
    private final HttpClient httpClient;

    /**
     * constructor
     *
     * @param httpClient the http client to use
     */
    public DescriptionLoader(HttpClient httpClient) {
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
            var response = httpClient.newRequest(webthingURI).timeout(30, TimeUnit.SECONDS).send();
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw new IOException(
                        "could not read resource description " + webthingURI + ". Got " + response.getStatus());
            }
            var body = response.getContentAsString();
            var description = new Gson().fromJson(body, WebThingDescription.class);
            if ((description.properties != null) && (description.properties.size() > 0)) {
                return description;
            } else {
                throw new IOException("description does not include properties");
            }
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("error occurred by querying WebThing", e);
        } catch (JsonSyntaxException se) {
            throw new IOException("resource seems not to be a WebThing. Typo?");
        } catch (InterruptedException ie) {
            throw new IOException(ie.getMessage());
        }
    }
}
