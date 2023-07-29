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

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.folderwatcher.internal.api.auth.AWS4SignerBase;
import org.openhab.binding.folderwatcher.internal.api.auth.AWS4SignerForAuthorizationHeader;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

    public S3Actions(HttpClientFactory httpClientFactory, String bucketName, String region) {
        this(httpClientFactory, bucketName, region, "", "");
    }

    public S3Actions(HttpClientFactory httpClientFactory, String bucketName, String region, String awsAccessKey,
            String awsSecretKey) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        try {
            this.bucketUri = new URL("http://" + bucketName + ".s3." + region + ".amazonaws.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to parse service endpoint: " + e.getMessage());
        }
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }

    public List<String> listBucket(String prefix) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> params = new HashMap<String, String>();
        return listObjectsV2(prefix, headers, params);
    }

    private List<String> listObjectsV2(String prefix, Map<String, String> headers, Map<String, String> params)
            throws Exception {
        params.put("list-type", "2");
        params.put("prefix", prefix);
        if (!awsAccessKey.isEmpty() || !awsSecretKey.isEmpty()) {
            headers.put("x-amz-content-sha256", AWS4SignerBase.EMPTY_BODY_SHA256);
            AWS4SignerForAuthorizationHeader signer = new AWS4SignerForAuthorizationHeader(this.bucketUri, "GET", "s3",
                    region);
            String authorization = signer.computeSignature(headers, params, AWS4SignerBase.EMPTY_BODY_SHA256,
                    awsAccessKey, awsSecretKey);
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

        ContentResponse contentResponse = request.send();
        if (contentResponse.getStatus() != 200) {
            throw new Exception("HTTP Response is not 200");
        }

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(contentResponse.getContentAsString()));
        Document doc = docBuilder.parse(is);
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
            if (nameNodesList.item(0).getFirstChild().getTextContent().equals("true")) {
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
