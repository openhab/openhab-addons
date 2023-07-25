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
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * HTTP client using own context with private & Bosch Certs
 * to pair and connect to the Bosch Smart Home Controller.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
public class BoschHttpClient extends HttpClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String ipAddress;
    private final String systemPassword;

    public BoschHttpClient(String ipAddress, String systemPassword, SslContextFactory sslContextFactory) {
        super(sslContextFactory);
        this.ipAddress = ipAddress;
        this.systemPassword = systemPassword;
    }

    /**
     * Returns the public information URL for the Bosch SHC client addressed with the given IP address, using port 8446
     * See https://github.com/BoschSmartHome/bosch-shc-api-docs/blob/master/postman/README.md
     *
     * @return URL for public information
     */
    public static String getPublicInformationUrl(String ipAddress) {
        return String.format("https://%s:8446/smarthome/public/information", ipAddress);
    }

    /**
     * Returns the public information URL for the current Bosch SHC client.
     *
     * @return URL for public information
     */
    public String getPublicInformationUrl() {
        return getPublicInformationUrl(this.ipAddress);
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
     * @param endpoint an endpoint, see https://apidocs.bosch-smarthome.com/local/index.html
     * @return Bosch SHC URL for passed endpoint
     */
    public String getBoschShcUrl(String endpoint) {
        return String.format("https://%s:8444/%s", this.ipAddress, endpoint);
    }

    /**
     * Returns a SmartHome URL for the endpoint - shortcut of {@link BoschSslUtil::getBoschShcUrl()}
     *
     * @param endpoint an endpoint, see https://apidocs.bosch-smarthome.com/local/index.html
     * @return SmartHome URL for passed endpoint
     */
    public String getBoschSmartHomeUrl(String endpoint) {
        return this.getBoschShcUrl(String.format("smarthome/%s", endpoint));
    }

    /**
     * Returns a URL to get or put a service state.
     * <p>
     * Example:
     *
     * <pre>
     * https://localhost:8444/smarthome/devices/hdm:ZigBee:000d6f0016d1cdae/services/AirQualityLevel/state
     * </pre>
     *
     * see https://apidocs.bosch-smarthome.com/local/index.html
     *
     * @param serviceName the name of the service
     * @param deviceId the device identifier
     * @return a URL to get or put a service state
     */
    public String getServiceStateUrl(String serviceName, String deviceId) {
        return this.getBoschSmartHomeUrl(String.format("devices/%s/services/%s/state", deviceId, serviceName));
    }

    /**
     * Returns a URL to get general information about a service.
     * <p>
     * Example:
     *
     * <pre>
     * https://localhost:8444/smarthome/devices/hdm:ZigBee:000d6f0016d1cdae/services/BatteryLevel
     * </pre>
     *
     * In some cases this URL has to be used to get the service state, for example for battery levels.
     *
     * @param serviceName the name of the service
     * @param deviceId the device identifier
     * @return a URL to retrieve general service information
     */
    public String getServiceUrl(String serviceName, String deviceId) {
        return this.getBoschSmartHomeUrl(String.format("devices/%s/services/%s", deviceId, serviceName));
    }

    /**
     * Checks if the Bosch SHC is online.
     *
     * The HTTP server could be offline (Timeout of request).
     * Or during boot-up the server can response e.g. with SERVICE_UNAVAILABLE_503
     *
     * Will return true, if the server responds with the "public information".
     *
     *
     * @return true if HTTP server is online
     * @throws InterruptedException in case of an interrupt
     */
    public boolean isOnline() throws InterruptedException {
        try {
            String url = this.getPublicInformationUrl();
            Request request = this.createRequest(url, GET);
            ContentResponse contentResponse = request.send();
            if (HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                String content = contentResponse.getContentAsString();
                logger.debug("Online check completed with success: {} - status code: {}", content,
                        contentResponse.getStatus());
                return true;
            } else {
                logger.debug("Online check failed with status code: {}", contentResponse.getStatus());
                return false;
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("Online check failed because of {}!", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if the Bosch SHC can be accessed.
     *
     * @return true if HTTP access to SHC devices was successful
     * @throws InterruptedException in case of an interrupt
     */
    public boolean isAccessPossible() throws InterruptedException {
        try {
            String url = this.getBoschSmartHomeUrl("devices");
            Request request = this.createRequest(url, GET);
            ContentResponse contentResponse = request.send();
            if (HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                String content = contentResponse.getContentAsString();
                logger.debug("Access check completed with success: {} - status code: {}", content,
                        contentResponse.getStatus());
                return true;
            } else {
                logger.debug("Access check failed with status code: {}", contentResponse.getStatus());
                return false;
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.debug("Access check failed because of {}!", e.getMessage(), e);
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
        } catch (TimeoutException | CertificateEncodingException | KeyStoreException | RuntimeException e) {
            logger.warn("Pairing failed with exception {}", e.getMessage(), e);
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
        logger.trace("Create request for http client {}", this);

        Request request = this.newRequest(url).method(method).header("Content-Type", "application/json")
                .header("api-version", "3.2") // see https://github.com/BoschSmartHome/bosch-shc-api-docs/issues/80
                .timeout(10, TimeUnit.SECONDS); // Set default timeout

        if (content != null) {
            String body = GsonUtils.DEFAULT_GSON_INSTANCE.toJson(content);
            logger.trace("create request for {} and content {}", url, content);
            request = request.content(new StringContentProvider(body));
        } else {
            logger.trace("create request for {}", url);
        }

        return request;
    }

    /**
     * Sends a request and expects a response of the specified type.
     *
     * @param request Request to send
     * @param responseContentClass Type of expected response
     * @param contentValidator Checks if the parsed response is valid
     * @param errorResponseHandler Optional ustom error response handling. If not provided a generic exception is thrown
     * @throws ExecutionException in case of invalid HTTP request result
     * @throws TimeoutException in case of an HTTP request timeout
     * @throws InterruptedException in case of an interrupt
     * @throws BoschSHCException in case of a custom handled error response
     */
    public <TContent> TContent sendRequest(Request request, Class<TContent> responseContentClass,
            Predicate<TContent> contentValidator,
            @Nullable BiFunction<Integer, String, BoschSHCException> errorResponseHandler)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        logger.trace("Send request: {}", request);

        ContentResponse contentResponse = request.send();

        String textContent = contentResponse.getContentAsString();

        Integer statusCode = contentResponse.getStatus();
        if (!HttpStatus.getCode(statusCode).isSuccess()) {
            if (errorResponseHandler != null) {
                throw errorResponseHandler.apply(statusCode, textContent);
            } else {
                throw new ExecutionException(String.format("Send request failed with status code %s", statusCode),
                        null);
            }
        }

        logger.debug("Send request completed with success: {} - status code: {}", textContent, statusCode);

        try {
            @Nullable
            TContent content = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(textContent, responseContentClass);
            if (content == null) {
                throw new ExecutionException(String.format("Received no content in response, expected type %s",
                        responseContentClass.getName()), null);
            }
            if (!contentValidator.test(content)) {
                throw new ExecutionException(String.format("Received invalid content for type %s: %s",
                        responseContentClass.getName(), content), null);
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
