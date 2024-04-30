/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.connection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.huesync.internal.HueSyncConstants.ENDPOINTS;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncConnection {
    public static final ObjectMapper ObjectMapper = new ObjectMapper();
    /**
     * Request format: The Sync Box API can be accessed locally via HTTPS on root
     * level (port 443, /api/v1), resource level /api/v1/<resource> and in some
     * cases sub-resource level /api/v1/<resource>/<sub-resource>.
     */
    private static final String REQUEST_FORMAT = "https://%s:%s/%s/%s";
    private static final String API = "api/v1";
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncConnection.class);

    private Integer port;
    private String host;

    private ServiceRegistration<?> tlsProviderService;
    private HttpClient httpClient;
    private URI uri;

    @Nullable
    private HueSyncAuthenticationResult authentication;

    protected String registrationId = "";

    public HueSyncConnection(HttpClient httpClient, String host, Integer port)
            throws CertificateException, IOException, URISyntaxException {

        this.host = host;
        this.port = port;

        this.uri = new URI(String.format("https://%s:%s", this.host, this.port));

        // this.registrationId = registrationId;

        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.host, this.port);
        BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

        this.tlsProviderService = context.registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider,
                null);
        this.httpClient = httpClient;
    }

    protected @Nullable <T> T executeRequest(HttpMethod method, String endpoint, String payload, Class<T> type) {
        try {
            ContentResponse response = this.executeRequest(method, endpoint, payload);

            return processedResponse(response, type);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.logger.error("{}", e.getMessage());
        }
        return null;
    }

    protected @Nullable <T> T executeGetRequest(String endpoint, Class<T> type) {
        try {
            ContentResponse response = this.executeGetRequest(endpoint);

            return processedResponse(response, type);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.logger.error("{}", e.getMessage());
        }

        return null;
    }

    protected boolean isRegistered() {
        return Optional.ofNullable(this.authentication).isPresent();
    }

    protected void setAuthentication(String token) {
        if (!token.isBlank()) {
            this.unsetAuthentication();

            this.authentication = new HueSyncAuthenticationResult(this.uri, token);
            this.httpClient.getAuthenticationStore().addAuthenticationResult(this.authentication);
        }
    }

    protected void unsetAuthentication() {
        if (isRegistered()) {
            this.httpClient.getAuthenticationStore().removeAuthenticationResult(this.authentication);

            this.registrationId = "";
            this.authentication = null;
        }
    }

    protected boolean unregisterDevice() {
        if (this.isRegistered()) {
            try {
                String endpoint = ENDPOINTS.REGISTRATIONS + "/" + this.registrationId;
                ContentResponse response = this.executeRequest(HttpMethod.DELETE, endpoint);

                if (response.getStatus() == HttpStatus.OK_200) {
                    this.unsetAuthentication();
                    return true;
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                this.logger.error("{}", e.getMessage());
            }
        }
        return false;
    }

    protected void dispose() {
        this.tlsProviderService.unregister();
    }

    private @Nullable <T> T processedResponse(ContentResponse response, Class<T> type) {
        int status = response.getStatus();
        /*
         * 400 Invalid State:
         * Registration in progress
         * 
         * 401 Authentication failed:
         * If credentials are missing or invalid, errors out. If credentials are missing, continues on to GET only the
         * Configuration state when unauthenticated, to allow for device identification.
         * 
         * 404 Invalid URI Path:
         * Accessing URI path which is not supported
         * 
         * 500 Internal:
         * Internal errors like out of memory
         */
        switch (status) {
            case HttpStatus.OK_200:
                return this.deserialize(response.getContentAsString(), type);
            case HttpStatus.BAD_REQUEST_400:
                logger.info("registration in progress: no token received yet");
                break;
            case HttpStatus.UNAUTHORIZED_401:
                logger.error("credentials missing or invalid");
                break;
            case HttpStatus.NOT_FOUND_404:
                logger.error("invalid device URI or API endpoint");
                break;
            case HttpStatus.INTERNAL_SERVER_ERROR_500:
                logger.error("hue sync box server problem");
                break;
            default:
                logger.warn("unexpected HTTP status: {}", status);
        }
        return null;
    }

    private @Nullable <T> T deserialize(String json, Class<T> type) {
        try {
            return ObjectMapper.readValue(json, type);
        } catch (JsonProcessingException | NoClassDefFoundError e) {
            this.logger.error("{}", e.getMessage());

            return null;
        }
    }

    private ContentResponse executeRequest(HttpMethod method, String endpoint)
            throws InterruptedException, TimeoutException, ExecutionException {
        return this.executeRequest(method, endpoint, "");
    }

    private ContentResponse executeGetRequest(String endpoint)
            throws InterruptedException, ExecutionException, TimeoutException {
        String uri = String.format(REQUEST_FORMAT, this.host, this.port, API, endpoint);

        return httpClient.GET(uri);
    }

    private ContentResponse executeRequest(HttpMethod method, String endpoint, String payload)
            throws InterruptedException, TimeoutException, ExecutionException {

        String uri = String.format(REQUEST_FORMAT, this.host, this.port, API, endpoint);

        Request request = this.httpClient.newRequest(uri).method(method);

        if (!payload.isBlank()) {
            request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                    .content(new StringContentProvider(payload));
        }

        return request.send();
    }

    public void updateConfig(HueSyncConfiguration config) {
        if (!config.apiAccessToken.isBlank()) {
            this.registrationId = config.registrationId;
            this.setAuthentication(config.apiAccessToken);
        }
    }
}
