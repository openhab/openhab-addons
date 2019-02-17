/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chamberlainmyq.internal;

import static org.openhab.binding.chamberlainmyq.ChamberlainMyQBindingConstants.USERAGENT;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Need to Subclass to override the USERAGENT ID to spoof the MyQ App
 * Some common methods to be used in both HTTP-In-Binding and HTTP-Out-Binding
 *
 * @author Scott Hanson - Initial contribution
 * @author Thomas Eichstaedt-Engelen
 * @author Kai Kreuzer - Initial contribution and API
 * @author Svilen Valkanov - replaced Apache HttpClient with Jetty
 */
public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static HttpClient client = new HttpClient(new SslContextFactory());

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>.
     * Furthermore the <code>http.proxyXXX</code> System variables are read and
     * set into the {@link HttpClient}.
     *
     * @param httpMethod the HTTP method to use
     * @param url        the url to execute (in milliseconds)
     * @param timeout    the socket timeout to wait for data
     *
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, int timeout) throws IOException {
        return executeUrl(httpMethod, url, null, null, timeout);
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>.
     * Furthermore the <code>http.proxyXXX</code> System variables are read and
     * set into the {@link HttpClient}.
     *
     * @param httpMethod  the HTTP method to use
     * @param url         the url to execute (in milliseconds)
     * @param content     the content to be send to the given <code>url</code> or <code>null</code> if no content should
     *                        be
     *                        send.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout     the socket timeout to wait for data
     *
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, InputStream content, String contentType, int timeout)
            throws IOException {
        return executeUrl(httpMethod, url, null, content, contentType, timeout);
    }

    /**
     * Executes the given <code>url</code> with the given <code>httpMethod</code>
     *
     * @param httpMethod  the HTTP method to use
     * @param url         the url to execute (in milliseconds)
     * @param httpHeaders optional HTTP headers which has to be set on request
     * @param content     the content to be send to the given <code>url</code> or <code>null</code> if no content should
     *                        be
     *                        send.
     * @param contentType the content type of the given <code>content</code>
     * @param timeout     the socket timeout to wait for data
     * @return the response body or <code>NULL</code> when the request went wrong
     * @throws IOException when the request execution failed, timed out or it was interrupted
     */
    public static String executeUrl(String httpMethod, String url, Properties httpHeaders, InputStream content,
            String contentType, int timeout) throws IOException {
        startHttpClient(client);

        client.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, USERAGENT));

        HttpMethod method = HttpUtil.createHttpMethod(httpMethod);

        Request request = client.newRequest(url).method(method).timeout(timeout, TimeUnit.MILLISECONDS);

        if (httpHeaders != null) {
            for (String httpHeaderKey : httpHeaders.stringPropertyNames()) {
                request.header(httpHeaderKey, httpHeaders.getProperty(httpHeaderKey));
            }
        }

        // add basic auth header, if url contains user info
        try {
            URI uri = new URI(url);
            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");

                String user = userInfo[0];
                String password = userInfo[1];

                String basicAuthentication = "Basic " + B64Code.encode(user + ":" + password, StringUtil.__ISO_8859_1);
                request.header(HttpHeader.AUTHORIZATION, basicAuthentication);
            }
        } catch (URISyntaxException e) {
            logger.debug("String {} can not be parsed as URI reference", url);
        }

        // add content if a valid method is given ...
        if (content != null && (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT))) {
            request.content(new InputStreamContentProvider(content), contentType);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("About to execute {}", request.getURI());
        }

        try {
            ContentResponse response = request.send();
            int statusCode = response.getStatus();
            if (statusCode >= HttpStatus.BAD_REQUEST_400) {
                String statusLine = statusCode + " " + response.getReason();
                logger.debug("Method failed: {}", statusLine);
            }

            byte[] rawResponse = response.getContent();
            String encoding = response.getEncoding() != null ? response.getEncoding().replaceAll("\"", "").trim()
                    : "UTF-8";
            String responseBody = new String(rawResponse, encoding);
            if (!responseBody.isEmpty()) {
                logger.trace("Http Response {}.", responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
        }
    }

    /**
     * Factory method to create a {@link HttpMethod}-object according to the
     * given String <code>httpMethodString</code>
     *
     * @param httpMethodString the name of the {@link HttpMethod} to create
     *
     * @throws IllegalArgumentException if <code>httpMethod</code> is none of <code>GET</code>, <code>PUT</code>,
     *                                      <code>POST</POST> or <code>DELETE</code>
     */
    public static HttpMethod createHttpMethod(String httpMethodString) {
        if ("GET".equals(httpMethodString)) {
            return HttpMethod.GET;
        } else if ("PUT".equals(httpMethodString)) {
            return HttpMethod.PUT;
        } else if ("POST".equals(httpMethodString)) {
            return HttpMethod.POST;
        } else if ("DELETE".equals(httpMethodString)) {
            return HttpMethod.DELETE;
        } else {
            throw new IllegalArgumentException("given httpMethod '" + httpMethodString + "' is unknown");
        }
    }

    private static void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                logger.warn("Cannot start HttpClient!", e);
            }
        }
    }

}
