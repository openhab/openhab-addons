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
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.huesync.internal.HueSyncConstants.ENDPOINTS;
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
 */
@NonNullByDefault
public class HueSyncConnection {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /**
     * Request format: The Sync Box API can be accessed locally via HTTPS on root level (port 443,
     * /api/v1), resource level /api/v1/<resource> and in some cases sub-resource level
     * /api/v1/<resource>/<sub-resource>.
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

    protected String registrationId = "";

    public HueSyncConnection(HttpClient httpClient, String host, Integer port)
            throws CertificateException, IOException, URISyntaxException {
        this.host = host;
        this.port = port;

        this.deviceUri = new URI(String.format("https://%s:%s", this.host, this.port));

        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.host, this.port);
        BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

        this.tlsProviderService = context.registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider,
                null);
        this.httpClient = httpClient;
    }

    public void updateAuthentication(String id, String token) {
        this.removeAuthentication();

        if (!id.isBlank() && !token.isBlank()) {
            this.registrationId = id;

            this.authentication = Optional.of(new HueSyncAuthenticationResult(this.deviceUri, token));
            this.httpClient.getAuthenticationStore().addAuthenticationResult(this.authentication.get());
        }
    }

    // #region protected
    protected @Nullable <T> T executeRequest(HttpMethod method, String endpoint, String payload,
            @Nullable Class<T> type) {
        try {
            return this.processedResponse(this.executeRequest(method, endpoint, payload), type);
        } catch (ExecutionException e) {
            this.handleExecutionException(e);
        } catch (InterruptedException | TimeoutException e) {
            this.logger.warn("{}", e.getMessage());
        }

        return null;
    }

    protected @Nullable <T> T executeGetRequest(String endpoint, Class<T> type) {
        try {
            return this.processedResponse(this.executeGetRequest(endpoint), type);
        } catch (ExecutionException e) {
            this.handleExecutionException(e);
        } catch (InterruptedException | TimeoutException e) {
            this.logger.warn("{}", e.getMessage());
        }

        return null;
    }

    protected boolean isRegistered() {
        return this.authentication.isPresent();
    }

    protected void unregisterDevice() {
        if (this.isRegistered()) {
            try {
                String endpoint = ENDPOINTS.REGISTRATIONS + "/" + this.registrationId;
                ContentResponse response = this.executeRequest(HttpMethod.DELETE, endpoint);

                if (response.getStatus() == HttpStatus.OK_200) {
                    this.removeAuthentication();
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                this.logger.warn("{}", e.getMessage());
            }
        }
    }

    protected void dispose() {
        this.tlsProviderService.unregister();
    }
    // #endregion

    // #region private
    private @Nullable <T> T processedResponse(Response response, @Nullable Class<T> type) {
        int status = response.getStatus();
        try {
            /*
             * 400 Invalid State: Registration in progress
             * 
             * 401 Authentication failed: If credentials are missing or invalid, errors out. If
             * credentials are missing, continues on to GET only the Configuration state when
             * unauthenticated, to allow for device identification.
             * 
             * 404 Invalid URI Path: Accessing URI path which is not supported
             * 
             * 500 Internal: Internal errors like out of memory
             */
            switch (status) {
                case HttpStatus.OK_200 -> {
                    return (type != null && (response instanceof ContentResponse))
                            ? this.deserialize(((ContentResponse) response).getContentAsString(), type)
                            : null;
                }
                case HttpStatus.BAD_REQUEST_400 -> this.logger.debug("registration in progress: no token received yet");
                case HttpStatus.UNAUTHORIZED_401 -> {
                    this.authentication = Optional.empty();
                    throw new HueSyncConnectionException("@text/connection.invalid-login");
                }
                case HttpStatus.NOT_FOUND_404 -> this.logger.warn("invalid device URI or API endpoint");
                case HttpStatus.INTERNAL_SERVER_ERROR_500 -> this.logger.warn("hue sync box server problem");
                default -> this.logger.warn("unexpected HTTP status: {}", status);
            }
        } catch (HueSyncConnectionException e) {
            this.logger.warn("{}", e.getMessage());
        }
        return null;
    }

    private @Nullable <T> T deserialize(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
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

        this.logger.trace("uri: {}", uri);
        this.logger.trace("method: {}", method);
        this.logger.trace("payload: {}", payload);

        if (!payload.isBlank()) {
            request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON_UTF_8.toString())
                    .content(new StringContentProvider(payload));
        }

        return request.send();
    }

    private void handleExecutionException(ExecutionException e) {
        this.logger.warn("{}", e.getMessage());

        Throwable cause = e.getCause();
        if (cause != null && cause instanceof HttpResponseException) {
            processedResponse(((HttpResponseException) cause).getResponse(), null);
        }
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
