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
package org.openhab.binding.webthing.internal.client;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.webthing.internal.client.dto.WebThingDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Utility class to load the WebThing description (meta data). Refer https://iot.mozilla.org/wot/#web-thing-description
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
public class DescriptionLoader {
    private final Logger logger = LoggerFactory.getLogger(DescriptionLoader.class);
    private final Gson gson = new Gson();
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
            var response = httpClient.newRequest(webthingURI).timeout(30, TimeUnit.SECONDS).accept("application/json")
                    .send();
            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                throw new IOException(
                        "could not read resource description " + webthingURI + ". Got " + response.getStatus());
            }
            var body = response.getContentAsString();
            var description = gson.fromJson(body, WebThingDescription.class);
            if ((description != null) && (description.properties != null) && (description.properties.size() > 0)) {
                if ((description.contextKeyword == null) || description.contextKeyword.trim().length() == 0) {
                    description.contextKeyword = "https://webthings.io/schemas";
                }
                var schema = description.contextKeyword.replaceFirst("/$", "").toLowerCase(Locale.US).trim();

                // currently, the old and new location of the WebThings schema are supported only.
                // In the future, other schemas such as http://iotschema.org/docs/full.html may be supported
                if (schema.equals("https://webthings.io/schemas") || schema.equals("https://iot.mozilla.org/schemas")) {
                    return description;
                }
                logger.debug(
                        "WebThing {} detected with unsupported schema {} (Supported schemas are https://webthings.io/schemas and https://iot.mozilla.org/schemas)",
                        webthingURI, description.contextKeyword);
                throw new IOException("unsupported schema (@context parameter) " + description.contextKeyword
                        + " (Supported schemas are https://webthings.io/schemas and https://iot.mozilla.org/schemas)");
            } else {
                throw new IOException("description does not include properties");
            }
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("error occurred by calling WebThing", e);
        } catch (JsonSyntaxException se) {
            throw new IOException("resource seems not to be a WebThing. Typo?");
        } catch (InterruptedException ie) {
            throw new IOException("resource seems not to be reachable");
        }
    }
}
