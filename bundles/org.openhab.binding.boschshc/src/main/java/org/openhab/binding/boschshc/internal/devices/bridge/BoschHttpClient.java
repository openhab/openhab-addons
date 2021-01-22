/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import com.google.gson.JsonSyntaxException;

/**
 * HTTP client using own context with private & Bosch Certs
 * to pair and connect to the Bosch Smart Home Controller.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
public class BoschHttpClient extends HttpClient {
    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(BoschHttpClient.class);

    private final String ipAddress;
    private final String systemPassword;

    public BoschHttpClient(String ipAddress, String systemPassword, SslContextFactory sslContextFactory) {
        super(sslContextFactory);
        this.ipAddress = ipAddress;
        this.systemPassword = systemPassword;
    }

    /**
     * Returns the pairing URL for the Bosch SHC clients, using port 8443.
     * See https://github.com/BoschSmartHome/bosch-shc-api-docs/blob/master/postman/README.md
     * 
     * @return URL for pairing
     */
    public String getPairingUrl() {
        return String.format("https://%s:8443/smarthome/clients", this.ipAddress);
    }

    /**
     * Returns a Bosch SHC URL for the endpoint, using port 8444.
     * 
     * @param endpoint a endpoint, see https://apidocs.bosch-smarthome.com/local/index.html
     * @return Bosch SHC URL for passed endpoint
     */
    public String getBoschShcUrl(String endpoint) {
        return String.format("https://%s:8444/%s", this.ipAddress, endpoint);
    }

    /**
     * Returns a SmartHome URL for the endpoint - shortcut of {@link BoschSslUtil::getBoschShcUrl()}
     * 
     * @param endpoint a endpoint, see https://apidocs.bosch-smarthome.com/local/index.html
     * @return SmartHome URL for passed endpoint
     */
    public String getBoschSmartHomeUrl(String endpoint) {
        return this.getBoschShcUrl(String.format("smarthome/%s", endpoint));
    }

    /**
     * Returns a device & service URL.
     * see https://apidocs.bosch-smarthome.com/local/index.html
     * 
     * @param serviceName the name of the service
     * @param deviceId the device identifier
     * @return SmartHome URL for passed endpoint
     */
    public String getServiceUrl(String serviceName, String deviceId) {
        return this.getBoschSmartHomeUrl(String.format("devices/%s/services/%s/state", deviceId, serviceName));
    }

    /**
     * Checks if the Bosch SHC can be accessed.
     * 
     * @return true if HTTP access was successful
     * @throws InterruptedException in case of an interrupt
     */
    public boolean isAccessPossible() throws InterruptedException {
        try {
            String url = this.getBoschSmartHomeUrl("devices");
            Request request = this.createRequest(url, GET);
            ContentResponse contentResponse = request.send();
            String content = contentResponse.getContentAsString();
            logger.debug("Access check response complete: {} - return code: {}", content, contentResponse.getStatus());
            return true;
        } catch (TimeoutException | ExecutionException | NullPointerException e) {
            logger.debug("Access check response failed because of {}!", e.getMessage());
            return false;
        }
    }

    /**
     * Pairs this client with the Bosch SHC.
     * Press pairing button on the Bosch Smart Home Controller!
     * 
     * @return true if pairing was successful, otherwise false
     * @throws InterruptedException in case of an interrupt
     */
    public boolean doPairing() throws InterruptedException {
        logger.trace("Starting pairing openHAB Client with Bosch Smart Home Controller!");
        logger.trace("Please press the Bosch Smart Home Controller button until LED starts blinking");

        ContentResponse contentResponse;
        try {
            String publicCert = getCertFromSslContextFactory();
            logger.trace("Pairing with SHC {}", ipAddress);

            // JSON Rest content
            Map<String, String> items = new HashMap<>();
            items.put("@type", "client");
            items.put("id", BoschSslUtil.getBoschShcClientId()); // Client Id contains the unique OpenHab instance Id
            items.put("name", "oss_OpenHAB_Binding"); // Client name according to
                                                      // https://github.com/BoschSmartHome/bosch-shc-api-docs#terms-and-conditions
            items.put("primaryRole", "ROLE_RESTRICTED_CLIENT");
            items.put("certificate", "-----BEGIN CERTIFICATE-----\r" + publicCert + "\r-----END CERTIFICATE-----");

            String url = this.getPairingUrl();
            Request request = this.createRequest(url, HttpMethod.POST, items).header("Systempassword",
                    Base64.getEncoder().encodeToString(this.systemPassword.getBytes(StandardCharsets.UTF_8)));

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
        } catch (TimeoutException | CertificateEncodingException | KeyStoreException | NullPointerException e) {
            logger.warn("Pairing failed with exception {}", e.getMessage());
            return false;
        } catch (ExecutionException e) {
            // javax.net.ssl.SSLHandshakeException: General SSLEngine problem
            // => usually the pairing failed, because hardware button was not pressed.
            logger.trace("Pairing failed - Details: {}", e.getMessage());
            logger.warn("Pairing failed. Was the Bosch Smart Home Controller button pressed?");
            return false;
        }
    }

    /**
     * Creates a HTTP request.
     * 
     * @param url for the HTTP request
     * @param method for the HTTP request
     * @return created HTTP request instance
     */
    public Request createRequest(String url, HttpMethod method) {
        return this.createRequest(url, method, null);
    }

    /**
     * Creates a HTTP request.
     * 
     * @param url for the HTTP request
     * @param method for the HTTP request
     * @param content for the HTTP request
     * @return created HTTP request instance
     */
    public Request createRequest(String url, HttpMethod method, @Nullable Object content) {
        logger.trace(String.format("Create request for http client %s", this.toString()));

        Request request = this.newRequest(url).method(method).header("Content-Type", "application/json");
        if (content != null) {
            String body = GSON.toJson(content);
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
     * @throws ExecutionException in case of invalid HTTP request result
     * @throws TimeoutException in case of an HTTP request timeout
     * @throws InterruptedException in case of an interrupt
     */
    public <TContent> TContent sendRequest(Request request, Class<TContent> responseContentClass)
            throws InterruptedException, TimeoutException, ExecutionException {
        logger.trace("Send request: {}", request.toString());

        ContentResponse contentResponse = request.send();

        logger.debug("Received response: {} - status: {}", contentResponse.getContentAsString(),
                contentResponse.getStatus());

        try {
            @Nullable
            TContent content = GSON.fromJson(contentResponse.getContentAsString(), responseContentClass);
            if (content == null) {
                throw new ExecutionException(String.format("Received no content in response, expected type %s",
                        responseContentClass.getName()), null);
            }
            return content;
        } catch (JsonSyntaxException e) {
            throw new ExecutionException(String.format("Received invalid content in response, expected type %s: %s",
                    responseContentClass.getName(), e.getMessage()), e);
        }
    }

    private String getCertFromSslContextFactory() throws KeyStoreException, CertificateEncodingException {
        Certificate cert = this.getSslContextFactory().getKeyStore()
                .getCertificate(BoschSslUtil.getBoschShcServerId(ipAddress));
        return Base64.getEncoder().encodeToString(cert.getEncoded());
    }
}
