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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.eclipse.jetty.http.HttpMethod.GET;

import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * HTTP client using own context with private & Bosch Certs
 * to pair and connect to the Bosch Smart Home Controller.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
public class BoschHttpClient extends HttpClient {
    private final Logger logger = LoggerFactory.getLogger(BoschHttpClient.class);

    private String ipAddress;
    private String systempassword;

    private Gson gson = new Gson();

    public BoschHttpClient(String ipAddress, String systempassword, SslContextFactory sslContextFactory) {
        super(sslContextFactory);
        this.ipAddress = ipAddress;
        this.systempassword = systempassword;
    }

    private String getCertFromSslContextFactory() throws KeyStoreException, CertificateEncodingException {
        Certificate cert = this.getSslContextFactory().getKeyStore().getCertificate(BoschSslUtil.getBoschSHCId());
        return Base64.getEncoder().encodeToString(cert.getEncoded());
    }

    public boolean isAccessPossible() {
        try {
            String url = this.createSmartHomeUrl("devices");
            Request request = this.createRequest(url, GET);
            ContentResponse contentResponse = request.send();
            String content = contentResponse.getContentAsString();
            logger.debug("Access check response complete: {} - return code: {}", content, contentResponse.getStatus());
            return true;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Access check response failed because of {}!", e.getMessage());
            return false;
        }
    }

    public boolean doPairing() {
        logger.trace("Starting pairing openHAB Client with Bosch SmartHomeController!");
        logger.trace("Please press the Bosch SHC button until LED starts blinking");

        ContentResponse contentResponse;
        try {
            String publicCert = getCertFromSslContextFactory();
            logger.trace("Pairing this Client '{}' with SHC {}", BoschSslUtil.getBoschSHCId(), ipAddress);

            // JSON Rest content
            Map<String, String> items = new HashMap<>();
            items.put("@type", "client");
            items.put("id", BoschSslUtil.getBoschSHCId()); // Client Id contains the unique OpenHab instance Id
            items.put("name", "oss_OpenHAB_Binding"); // Client name according to
                                                      // https://github.com/BoschSmartHome/bosch-shc-api-docs#terms-and-conditions
            items.put("primaryRole", "ROLE_RESTRICTED_CLIENT");
            items.put("certificate", "-----BEGIN CERTIFICATE-----\r" + publicCert + "\r-----END CERTIFICATE-----");

            String url = this.createPairingUrl();
            Request request = this.createRequest(url, HttpMethod.POST, items).header("Systempassword",
                    Base64.getEncoder().encodeToString(systempassword.getBytes(StandardCharsets.UTF_8)));

            contentResponse = request.send();

            logger.trace("Pairing response complete: {} - return code: {}", contentResponse.getContentAsString(),
                    contentResponse.getStatus());
            if (201 == contentResponse.getStatus()) {
                logger.debug("Pairing successful.");
                return true;
            } else {
                logger.info("Pairing failed with response status {}.", contentResponse.getStatus());
                return false;
            }
        } catch (InterruptedException | TimeoutException | CertificateEncodingException | KeyStoreException e) {
            logger.warn("Pairing failed with exception {}", e.getMessage());
            return false;
        } catch (ExecutionException e) {
            // javax.net.ssl.SSLHandshakeException: General SSLEngine problem
            // => usually the pairing failed, because hardware button was not pressed.
            logger.trace("Pairing failed - Details: {}", e.getMessage());
            logger.warn("Pairing failed. Was the Bosch SHC button pressed?");
            return false;
        }
    }

    public String createPairingUrl() {
        return String.format("https://%s:8443/smarthome/clients", this.ipAddress);
    }

    public String createUrl(String endpoint) {
        return String.format("https://%s:8444/%s", this.ipAddress, endpoint);
    }

    public String createSmartHomeUrl(String endpoint) {
        return this.createUrl(String.format("smarthome/%s", endpoint));
    }

    public String createServiceUrl(String serviceName, String deviceId) {
        return this.createSmartHomeUrl(String.format("devices/%s/services/%s/state", deviceId, serviceName));
    }

    public Request createRequest(String url, HttpMethod method) {
        return this.createRequest(url, method, null);
    }

    public Request createRequest(String url, HttpMethod method, @Nullable Object content) {
        Request request = this.newRequest(url).method(method).header("Content-Type", "application/json");
        if (content != null) {
            String body = gson.toJson(content);
            logger.trace("create request for {} and content {}", url, body);
            request = request.content(new StringContentProvider(body));
        } else {
            logger.trace("create request for {}", url);
        }

        // Set default timeout
        request.timeout(10, TimeUnit.SECONDS);

        return request;
    }

    /**
     * Sends a request and expects a response of the specified type.
     * 
     * @param request Request to send
     * @param responseContentClass Type of expected response
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public <TContent> TContent sendRequest(Request request, Class<TContent> responseContentClass)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse contentResponse = request.send();

        logger.debug("BoschHttpClient: response complete: {} - return code: {}", contentResponse.getContentAsString(),
                contentResponse.getStatus());

        TContent content = gson.fromJson(contentResponse.getContentAsString(), responseContentClass);

        return content;
    }
}
