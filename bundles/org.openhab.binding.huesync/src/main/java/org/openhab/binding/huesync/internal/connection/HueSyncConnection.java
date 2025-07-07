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
package org.openhab.binding.huesync.internal.connection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.huesync.internal.HueSyncConstants.ENDPOINTS;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.exceptions.HueSyncConnectionException;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author Patrik Gfeller - Initial Contribution
 * @author Patrik Gfeller - Issue #18376, Fix/improve log message and exception handling
 */
@NonNullByDefault
public class HueSyncConnection {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /**
     * Request format: The Sync Box API can be accessed locally via HTTPS on root
     * level (port 443, /api/v1), resource level /api/v1/<resource>
     * and in some cases sub-resource level /api/v1/<resource>/<sub-resource>.
     */
    private static final String REQUEST_FORMAT = "https://%s:%s/%s/%s";
    private static final String API = "api/v1";
    private final Logger logger = LoggerFactory.getLogger(HueSyncConnection.class);

    private final Integer port;
    private final String host;

    private final ServiceRegistration<?> tlsProviderService;
    private final HttpClient httpClient;
    private final URI deviceUri;

    private Optional<HueSyncAuthenticationResult> authentication = Optional.empty();

    private class Request {

        private final String endpoint;

        private HttpMethod method = HttpMethod.GET;
        private String payload = "";

        private Request(HttpMethod httpMethod, String endpoint, String payload) {
            this.method = httpMethod;
            this.endpoint = endpoint;
            this.payload = payload;
        }

        protected Request(String endpoint) {
            this.endpoint = endpoint;
        }

        private Request(HttpMethod httpMethod, String endpoint) {
            this.method = httpMethod;
            this.endpoint = endpoint;
        }

        protected ContentResponse execute() throws InterruptedException, ExecutionException, TimeoutException {
            String uri = String.format(REQUEST_FORMAT, host, port, API, endpoint);

            var request = httpClient.newRequest(uri).method(method).timeout(1, TimeUnit.SECONDS);
            if (!payload.isBlank()) {
                request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON_UTF_8.toString())
                        .content(new StringContentProvider(payload));
            }

            return request.send();
        }
    }

    protected String registrationId = "";

    public HueSyncConnection(HttpClient httpClient, HueSyncConfiguration configuration)
            throws CertificateException, IOException, URISyntaxException {
        this.host = configuration.host;
        this.port = configuration.port;

        this.deviceUri = new URI(String.format("https://%s:%s", this.host, this.port));

        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.host, this.port);
        BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

        this.tlsProviderService = context.registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider,
                null);
        this.httpClient = httpClient;
        this.updateAuthentication(configuration.registrationId, configuration.apiAccessToken);
    }

    public final void updateAuthentication(String id, String token) {
        this.removeAuthentication();

        if (!id.isBlank() && !token.isBlank()) {
            this.registrationId = id;

            this.authentication = Optional.of(new HueSyncAuthenticationResult(this.deviceUri, token));
            this.httpClient.getAuthenticationStore().addAuthenticationResult(this.authentication.get());
        }
    }

    // #region protected
    protected @Nullable <T> T executeRequest(HttpMethod method, String endpoint, String payload,
            @Nullable Class<T> type) throws HueSyncConnectionException {
        return this.executeRequest(new Request(method, endpoint, payload), type);
    }

    protected @Nullable <T> T executeRequest(HttpMethod httpMethod, String endpoint, @Nullable Class<T> type)
            throws HueSyncConnectionException {
        return this.executeRequest(new Request(httpMethod, endpoint), type);
    }

    protected @Nullable <T> T executeGetRequest(String endpoint, Class<T> type) throws HueSyncConnectionException {
        return this.executeRequest(new Request(endpoint), type);
    }

    protected boolean isRegistered() {
        return this.authentication.isPresent();
    }

    protected void unregisterDevice() throws HueSyncConnectionException {
        if (this.isRegistered()) {
            String endpoint = ENDPOINTS.REGISTRATIONS + "/" + this.registrationId;

            this.executeRequest(HttpMethod.DELETE, endpoint, null);
            this.removeAuthentication();
        }
    }

    protected void dispose() {
        this.tlsProviderService.unregister();
    }
    // #endregion

    // #region private

    private @Nullable <T> T executeRequest(Request request, @Nullable Class<T> type) throws HueSyncConnectionException {
        var message = "@text/connection.generic-error";

        try {
            var response = request.execute();

            /*
             * 400 Invalid State: Registration in progress
             * 
             * 401 Authentication failed: If credentials are missing or invalid, errors out.
             * If credentials are missing, continues on to GET only the Configuration
             * state when unauthenticated, to allow for device identification.
             * 
             * 404 Invalid URI Path: Accessing URI path which is not supported
             * 
             * 500 Internal: Internal errors like out of memory
             */
            switch (response.getStatus()) {
                case HttpStatus.OK_200:
                    return this.deserialize(response.getContentAsString(), type);
            }

            handleResponseStatus(response.getStatus(), new HttpResponseException(response.getReason(), response));
        } catch (ExecutionException e) {
            this.logger.trace("{}: {}", e.getMessage(), message);

            if (e.getCause() instanceof HttpResponseException httpResponseException) {
                handleResponseStatus(httpResponseException.getResponse().getStatus(), httpResponseException);
            }

            throw new HueSyncConnectionException(message, e);
        } catch (HttpResponseException e) {
            handleResponseStatus(e.getResponse().getStatus(), e);
        } catch (JsonProcessingException | InterruptedException | TimeoutException e) {
            this.logger.trace("{}: {}", e.getMessage(), message);

            throw new HueSyncConnectionException(message, e);
        }

        throw new HueSyncConnectionException(message);
    }

    private void handleResponseStatus(int status, Exception e) throws HueSyncConnectionException {
        var message = "@text/connection.generic-error";

        switch (status) {
            case HttpStatus.BAD_REQUEST_400:
            case HttpStatus.UNAUTHORIZED_401:
                message = "@text/connection.invalid-login";
                break;
            case HttpStatus.NOT_FOUND_404:
                message = "@text/connection.generic-error";
                break;
        }

        this.logger.trace("Status: {}, Message Key: {}", status, message);

        throw new HueSyncConnectionException(message, e);
    }

    private @Nullable <T> T deserialize(String json, @Nullable Class<T> type) throws JsonProcessingException {
        return type == null ? null : OBJECT_MAPPER.readValue(json, type);
    }

    private void removeAuthentication() {
        AuthenticationStore store = this.httpClient.getAuthenticationStore();
        store.clearAuthenticationResults();

        this.httpClient.setAuthenticationStore(store);

        this.registrationId = "";
        this.authentication = Optional.empty();
    }

    // #endregion
}
