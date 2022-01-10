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
package org.openhab.binding.http.internal;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpField;

/**
 * The {@link Util} is a utility class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {

    /**
     * create a log string from a {@link org.eclipse.jetty.client.api.Request}
     *
     * @param request the request to log
     * @return the string representing the request
     */
    public static String requestToLogString(Request request) {
        ContentProvider contentProvider = request.getContent();
        String contentString = contentProvider == null ? "null"
                : StreamSupport.stream(contentProvider.spliterator(), false)
                        .map(b -> StandardCharsets.UTF_8.decode(b).toString()).collect(Collectors.joining(", "));
        String logString = "Method = {" + request.getMethod() + "}, Headers = {"
                + request.getHeaders().stream().map(HttpField::toString).collect(Collectors.joining(", "))
                + "}, Content = {" + contentString + "}";

        return logString;
    }

    /**
     * create an URI from a string, escaping all necessary characters
     *
     * @param s the URI as unescaped string
     * @return URI correspondign to the input string
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public static URI uriFromString(String s) throws MalformedURLException, URISyntaxException {
        URL url = new URL(s);
        return new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(),
                url.getQuery(), url.getRef());
    }
}
