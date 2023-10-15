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
package org.openhab.binding.folderwatcher.internal.api;

import static org.eclipse.jetty.http.HttpHeader.*;
import static org.eclipse.jetty.http.HttpMethod.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.folderwatcher.internal.api.auth.AWS4SignerBase;
import org.openhab.binding.folderwatcher.internal.api.auth.AWS4SignerForAuthorizationHeader;
import org.openhab.binding.folderwatcher.internal.api.exception.APIException;
import org.openhab.binding.folderwatcher.internal.api.exception.AuthException;
import org.openhab.binding.folderwatcher.internal.api.util.HttpUtilException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link S3Actions} class contains AWS S3 API implementation.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class S3Actions {
    private final HttpClient httpClient;
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(1);
    private static final String CONTENT_TYPE = "application/xml";
    private URL bucketUri;
    private String region;
    private String awsAccessKey;
    private String awsSecretKey;

    public S3Actions(HttpClientFactory httpClientFactory, String bucketName, String region) throws APIException {
        this(httpClientFactory, bucketName, region, "", "");
    }

    public S3Actions(HttpClientFactory httpClientFactory, String bucketName, String region, String awsAccessKey,
            String awsSecretKey) throws APIException {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        try {
            this.bucketUri = new URL("http://" + bucketName + ".s3." + region + ".amazonaws.com");
        } catch (MalformedURLException e) {
            throw new APIException("Unable to parse service endpoint: " + e.getMessage());
        }
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }

    public List<String> listBucket(String prefix) throws APIException, AuthException {
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> params = new HashMap<String, String>();
        return listObjectsV2(prefix, headers, params);
    }

    private List<String> listObjectsV2(String prefix, Map<String, String> headers, Map<String, String> params)
            throws APIException, AuthException {
        params.put("list-type", "2");
        params.put("prefix", prefix);
        if (!awsAccessKey.isEmpty() || !awsSecretKey.isEmpty()) {
            headers.put("x-amz-content-sha256", AWS4SignerBase.EMPTY_BODY_SHA256);
            AWS4SignerForAuthorizationHeader signer = new AWS4SignerForAuthorizationHeader(this.bucketUri, "GET", "s3",
                    region);
            String authorization;
            try {
                authorization = signer.computeSignature(headers, params, AWS4SignerBase.EMPTY_BODY_SHA256, awsAccessKey,
                        awsSecretKey);
            } catch (HttpUtilException e) {
                throw new AuthException(e);
            }
            headers.put("Authorization", authorization);
        }

        headers.put(ACCEPT.toString(), CONTENT_TYPE);
        Request request = httpClient.newRequest(this.bucketUri.toString()) //
                .method(GET) //
                .timeout(REQUEST_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS); //

        for (String headerKey : headers.keySet()) {
            request.header(headerKey, headers.get(headerKey));
        }
        for (String paramKey : params.keySet()) {
            request.param(paramKey, params.get(paramKey));
        }

        ContentResponse contentResponse;
        try {
            contentResponse = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new APIException(e);
        }

        if (contentResponse.getStatus() != 200) {
            throw new APIException("HTTP Response is not 200");
        }

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new APIException(e);
        }
        InputSource is = new InputSource(new StringReader(contentResponse.getContentAsString()));
        Document doc;
        try {
            doc = docBuilder.parse(is);
        } catch (SAXException | IOException e) {
            throw new APIException(e);
        }
        NodeList nameNodesList = doc.getElementsByTagName("Key");
        List<String> returnList = new ArrayList<>();

        if (nameNodesList.getLength() == 0) {
            return returnList;
        }

        for (int i = 0; i < nameNodesList.getLength(); i++) {
            returnList.add(nameNodesList.item(i).getFirstChild().getTextContent());
        }

        nameNodesList = doc.getElementsByTagName("IsTruncated");
        if (nameNodesList.getLength() > 0) {
            if ("true".equals(nameNodesList.item(0).getFirstChild().getTextContent())) {
                nameNodesList = doc.getElementsByTagName("NextContinuationToken");
                if (nameNodesList.getLength() > 0) {
                    String continueToken = nameNodesList.item(0).getFirstChild().getTextContent();
                    params.clear();
                    headers.clear();
                    params.put("continuation-token", continueToken);
                    returnList.addAll(listObjectsV2(prefix, headers, params));
                }
            }
        }
        return returnList;
    }
}
