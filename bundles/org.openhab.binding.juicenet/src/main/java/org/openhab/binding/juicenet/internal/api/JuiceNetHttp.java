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
package org.openhab.binding.juicenet.internal.api;

import static java.net.HttpURLConnection.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JuiceNetHttp} implements the http-based REST API to access the JuiceNet Cloud
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetHttp {
    private final Logger logger = LoggerFactory.getLogger(JuiceNetHttp.class);

    private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(20)).build();

    private int apiCalls = 0;

    private String authorization = "";

    /**
     * Constructor for the JuiceNet API class to create a connection to the JuiceNet cloud service.
     *
     * @param key JuiceNet API Access token (see Web UI)
     * @throws Exception
     */
    public JuiceNetHttp() throws JuiceNetApiException {
    }

    void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return JuiceNetApiResult including GET response, http code etc.
     * @throws InterruptedException
     * @throws IOException
     * @throws Exception
     */
    public HttpResponse<String> httpGet(String url, @Nullable Map<String, Object> params)
            throws IOException, InterruptedException {

        String paramString = "";

        if (params != null) {
            paramString = paramsToUrl(params);
        }

        return httpGet(url, paramString);
    }

    public HttpResponse<String> httpGet(String url, @Nullable String paramString)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder(URI.create(url + paramString)).GET().build();
        // .header("Authorization", "Bearer " + this.authorization).GET().build();

        return httpClient.send(request, BodyHandlers.ofString());
    }

    /**
     * Given a URL and a set parameters, send a HTTP POST request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a POST request to.
     * @param urlParameters List of parameters to use in the URL for the POST request. Null if no parameters.
     * @return JuiceNetApiResult including GET response, http code etc.
     * @throws Exception
     */
    public boolean httpPut(String url, String putData) throws JuiceNetApiException {
        return httpRequest(HttpMethod.PUT, url, null, putData);
    }

    /**
     * Given a URL and a set parameters, send a HTTP POST request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a POST request to.
     * @param postData List of parameters to use in the URL for the POST request. Null if no parameters.
     * @return JuiceNetApiResult including GET response, http code etc.
     * @throws InterruptedException
     * @throws IOException
     * @throws Exception
     */
    public HttpResponse<String> httpPost(String url, @Nullable Map<String, Object> params)
            throws IOException, InterruptedException {
        String paramString = "";

        if (params != null) {
            paramString = paramsToBody(params);
        }

        return httpPost(url, paramString);
    }

    public HttpResponse<String> httpPost(String url, String postData) throws IOException, InterruptedException {
        logger.trace(postData);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(postData)).build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        return response;
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return JuiceNetApiResult including GET response, http code etc.
     * @throws Exception if something went wrong (e.g. unable to connect)
     */
    public boolean httpDelete(String url, @Nullable String urlParameters) throws JuiceNetApiException {
        return httpRequest(HttpMethod.DELETE, url, urlParameters, null);
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return JuiceNetApiResult including GET response, http code etc.
     * @throws Exception
     */
    protected boolean httpRequest(String method, String url, @Nullable String urlParameters, @Nullable String reqDatas)
            throws JuiceNetApiException {

        int responseCode;
        String resultString;

        try {
            apiCalls++;

            URL location = null;
            if (urlParameters != null) {
                location = new URL(url + "?" + urlParameters);
            } else {
                location = new URL(url);
            }

            HttpURLConnection request = (HttpURLConnection) location.openConnection();
            /*
             * if (apikey != null) {
             * request.setRequestProperty("Authorization", "Bearer " + apikey);
             * result.apikey = apikey;
             * }
             */
            request.setRequestMethod(method);
            /*
             * request.setRequestProperty("User-Agent", SERVLET_WEBHOOK_USER_AGENT);
             * request.setRequestProperty("Content-Type", SERVLET_WEBHOOK_APPLICATION_JSON);
             */
            logger.trace("JuiceNetHttp[Call #{}]: Call JuiceNet cloud service: {} '{}')", apiCalls,
                    request.getRequestMethod(), location);
            if (method.equals(HttpMethod.PUT) || method.equals(HttpMethod.POST)) {
                request.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(request.getOutputStream());
                wr.writeBytes(reqDatas);
                wr.flush();
                wr.close();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuilder response = new StringBuilder();

            responseCode = request.getResponseCode();

            if ((responseCode != HTTP_OK) && ((responseCode != HTTP_NO_CONTENT)
                    || (!method.equals(HttpMethod.PUT) && !method.equals(HttpMethod.DELETE)))) {
                String message = MessageFormat.format(
                        "JuiceNetHttp: Error sending HTTP {0} request to {2} - http response code={2}", method, url,
                        responseCode);
                throw new JuiceNetApiException(message);
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            resultString = response.toString();
            logger.trace("JuiceNetHttp: {} {} - Response='{}'", method, url, resultString);

            return true;
        } catch (RuntimeException | IOException e) {
            throw new JuiceNetApiException(e.toString());
        }
    }

    public String paramsToUrl(Map<String, Object> params) {
        return params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&", "?", ""));
    }

    public String paramsToBody(Map<String, Object> params) {
        return params.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }
}
