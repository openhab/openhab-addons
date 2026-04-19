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
package org.openhab.binding.http.internal;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public static final Pattern FORMAT_REPLACE_PATTERN = Pattern.compile("%\\d\\$[^%]+");

    /**
     * Create a log string from a {@link org.eclipse.jetty.client.api.Request}
     *
     * @param request the request to log
     * @return the string representing the request
     */
    public static String requestToLogString(Request request) {
        ContentProvider contentProvider = request.getContent();
        String contentString = contentProvider == null ? "null"
                : StreamSupport.stream(contentProvider.spliterator(), false)
                        .map(b -> StandardCharsets.UTF_8.decode(b).toString()).collect(Collectors.joining(", "));
        return "Method = {" + request.getMethod() + "}, Headers = {"
                + request.getHeaders().stream().map(HttpField::toString).collect(Collectors.joining(", "))
                + "}, Content = {" + contentString + "}";
    }

    /**
     * Create a URI from a string, escaping all necessary characters
     *
     * @param s the URI as unescaped string
     * @return URI corresponding to the input string
     * @throws MalformedURLException if parameter is not a URL
     * @throws URISyntaxException if parameter could not be converted to a URI
     */
    public static URI uriFromString(String s) throws MalformedURLException, URISyntaxException {
        URI uri = parse(s);
        return URI.create(uri.toASCIIString().replace("+", "%2B").replace("%25%25", "%"));
    }

    private static final Pattern URL_PATTERN = Pattern.compile("^(?:(?<scheme>[a-zA-Z][a-zA-Z0-9+.-]*):)?//"
            + "(?:(?<userinfo>[^@/?#]*)@)?" + "(?<host>(?:\\[[a-fA-F0-9:]+\\])|(?:[^:/?#]*))(?::(?<port>\\d+))?"
            + "(?<path>/[^?#]*)?" + "(?:\\?(?<query>[^#]*))?" + "(?:#(?<fragment>.*))?");

    /**
     * Parses a URL string and returns a {@link URI} object, converting Unicode hostnames to ASCII as needed.
     *
     * @param url the URL string to parse
     * @return a {@link URI} representing the parsed URL
     * @throws URISyntaxException if the input string is not a valid URL
     */
    public static URI parse(String url) throws URISyntaxException {
        Matcher m = URL_PATTERN.matcher(url.trim());
        if (!m.matches()) {
            throw new URISyntaxException(url,
                    "Invalid URL. Expected format: [scheme]://[host][:port][path][?query][#fragment]. Input: '" + url
                            + "'");
        }

        String host = m.group("host");
        if (host != null) {
            host = IDN.toASCII(host); // <-- convert Unicode to ASCII
        }

        String portStr = m.group("port");
        int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : -1;

        return new URI(m.group("scheme"), m.group("userinfo"), host, port, m.group("path"), m.group("query"),
                m.group("fragment"));
    }

    /**
     * Format a string using {@link String#format(String, Object...)} but allow non-format percent characters
     *
     * The {@param inputString} is checked for format patterns ({@code %<index>$<format>}) and passes only those to the
     * {@link String#format(String, Object...)} method. This avoids format errors due to other percent characters in the
     * string.
     *
     * @param inputString the input string, potentially containing format instructions
     * @param params an array of parameters to be passed to the splitted input string
     * @return the formatted string
     */
    public static String wrappedStringFormat(String inputString, Object... params) {
        Matcher replaceMatcher = FORMAT_REPLACE_PATTERN.matcher(inputString);
        return replaceMatcher.replaceAll(matchResult -> String.format(matchResult.group(), params));
    }
}
