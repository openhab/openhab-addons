/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.folderwatcher.internal.api;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.*;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.folderwatcher.internal.api.auth.Azure4SignerForAuthorizationHeader;
import org.openhab.binding.folderwatcher.internal.api.exception.AuthException;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtilException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The {@link AzureActions} class contains AWS S3 API implementation.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class AzureActions {
    private final HttpClient httpClient;
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);
    private static final String CONTENT_TYPE = "application/xml";
    private URL containerUri;
    private String azureAccessKey;
    private String accountName;
    private String containerName;

    public AzureActions(HttpClientFactory httpClientFactory, String accountName, String containerName) {
        this(httpClientFactory, accountName, containerName, "");
    }

    public AzureActions(HttpClientFactory httpClientFactory, String accountName, String containerName,
            String azureAccessKey) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        try {
            this.containerUri = new URL("https://" + accountName + ".blob.core.windows.net/" + containerName);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to parse service endpoint: " + e.getMessage());
        }
        this.azureAccessKey = azureAccessKey;
        this.accountName = accountName;
        this.containerName = containerName;
    }

    public List<String> listContainer(String prefix) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> params = new HashMap<String, String>();
        return listBlob(prefix, headers, params);
    }

    public List<String> listBlob(String prefix, Map<String, String> headers, Map<String, String> params)
            throws Exception {
        List<String> returnList = new ArrayList<>();
        params.put("restype", "container");
        params.put("comp", "list");
        params.put("maxresults", "1000");
        params.put("prefix", prefix);
        headers.put(ACCEPT.toString(), CONTENT_TYPE);

        if (!azureAccessKey.isEmpty()) {
            Azure4SignerForAuthorizationHeader signer = new Azure4SignerForAuthorizationHeader("GET",
                    this.containerUri);
            String authorization;
            try {
                authorization = signer.computeSignature(headers, params, accountName, azureAccessKey, containerName);
            } catch (HttpUtilException e) {
                throw new AuthException(e);
            }
            headers.put("Authorization", authorization);
        }

        Request request = httpClient.newRequest(this.containerUri.toString()) //
                .method(GET) //
                .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS); //

        for (String headerKey : headers.keySet()) {
            request.header(headerKey, headers.get(headerKey));
        }
        for (String paramKey : params.keySet()) {
            request.param(paramKey, params.get(paramKey));
        }

        ContentResponse contentResponse = request.send();
        if (contentResponse.getStatus() != 200) {
            throw new Exception("HTTP Response is not 200");
        }

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        String sResponse = contentResponse.getContentAsString();
        Pattern pattern = Pattern.compile("<\\?xml.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sResponse);
        if (!matcher.find()) {
            return returnList;
        }
        sResponse = matcher.group();
        InputSource is = new InputSource(new StringReader(sResponse));
        Document doc = docBuilder.parse(is);
        NodeList nameNodesList = doc.getElementsByTagName("Blob");

        if (nameNodesList.getLength() == 0) {
            return returnList;
        }

        for (int i = 0; i < nameNodesList.getLength(); i++) {
            returnList.add(nameNodesList.item(i).getFirstChild().getTextContent());
        }

        nameNodesList = doc.getElementsByTagName("NextMarker");
        if (nameNodesList.getLength() > 0) {
            if (nameNodesList.item(0).getChildNodes().getLength() > 0) {
                String continueToken = nameNodesList.item(0).getFirstChild().getTextContent();
                params.clear();
                headers.clear();
                params.put("marker", continueToken);
                returnList.addAll(listBlob(prefix, headers, params));
            }
        }
        return returnList;
    }
}
