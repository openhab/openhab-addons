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
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDetailedDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequest;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the connection to a Hue HDMI Sync Box using the official API.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncConnection {
    /**
     * Request format: The Sync Box API can be accessed locally via HTTPS on root
     * level (port 443, /api/v1), resource level /api/v1/<resource> and in some
     * cases sub-resource level /api/v1/<resource>/<sub-resource>.
     */
    private static final String REQUEST_FORMAT = "https://%s:%s/%s/%s";
    private static final String API = "api/v1";
    private static final Integer TIMEOUT_MILLISECONDS = 500;

    private static class ENDPOINTS {
        public static final String DEVICE = "device";
        public static final String REGISTRATIONS = "registrations";
    }

    private static final ObjectMapper ObjectMapper = new ObjectMapper();

    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncConnection.class);

    private HttpClient httpClient;
    private String host;
    private Integer port;

    private @Nullable String apiAccessToken;
    private @Nullable String registrationId;

    private ServiceRegistration<?> tlsProviderService;

    public HueSyncConnection(HttpClient httpClient, String host, Integer port, String apiAccessToken,
            String registrationId) throws CertificateException, IOException {

        this.host = host;
        this.port = port;
        this.apiAccessToken = apiAccessToken;
        this.registrationId = registrationId;

        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.host, this.port);
        BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

        this.tlsProviderService = context.registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider,
                null);
        this.httpClient = httpClient;
    }

    @SuppressWarnings("null")
    public @Nullable HueSyncDeviceInfo getDeviceInfo() {
        HueSyncDeviceInfo deviceInfo = null;

        ContentResponse response = this.executeGetRequest(ENDPOINTS.DEVICE);

        if (this.responseReceived(response)) {
            deviceInfo = this.deserialize(response.getContentAsString(), HueSyncDeviceInfo.class);
        }

        return deviceInfo;
    }

    @SuppressWarnings("null")
    public @Nullable HueSyncDetailedDeviceInfo getDetailedDeviceInfo() {
        if (!this.isRegistered())
            return null;

        HueSyncDetailedDeviceInfo deviceInfo = null;

        ContentResponse response = this.executeGetRequest(ENDPOINTS.DEVICE);

        if (this.responseReceived(response)) {
            deviceInfo = this.deserialize(response.getContentAsString(), HueSyncDetailedDeviceInfo.class);
        }

        return deviceInfo;
    }

    public @Nullable HueSyncRegistration registerDevice(@Nullable String id) {
        HueSyncRegistration registration = null;

        try {
            if (id != null) {
                HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();

                dto.appName = HueSyncConstants.APPLICATION_NAME;
                dto.instanceName = id;

                String json = ObjectMapper.writeValueAsString(dto);

                ContentResponse response = this.executeRequest(HttpMethod.POST, ENDPOINTS.REGISTRATIONS, json);

                if (this.responseReceived(response)) {
                    registration = this.deserialize(response.getContentAsString(), HueSyncRegistration.class);

                    if (registration != null) {
                        this.apiAccessToken = registration.accessToken;
                        this.registrationId = registration.registrationId;
                    }
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonProcessingException e) {
            this.logger.error("{}", e.getMessage());
        }

        return registration;
    }

    public boolean unregisterDevice() {
        if (this.isRegistered()) {
            try {
                String endpoint = ENDPOINTS.REGISTRATIONS + "/" + this.registrationId;
                ContentResponse response = this.executeRequest(HttpMethod.DELETE, endpoint);

                return response.getStatus() == HttpStatus.OK_200;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                this.logger.error("{}", e.getMessage());
            }
        }
        return false;
    }

    public void stop() {
        this.tlsProviderService.unregister();
    }

    @SuppressWarnings("null")
    public boolean isRegistered() {
        return this.apiAccessToken != null && this.registrationId != null && !this.apiAccessToken.isBlank()
                && !this.registrationId.isBlank();
    }

    // #region - private

    private boolean responseReceived(@Nullable ContentResponse response) {
        if (response != null && response.getStatus() == HttpStatus.OK_200) {
            return true;
        } else {
            // TODO: responseReceived - log error details ...
            return false;
        }
    }

    private @Nullable <T> T deserialize(String json, Class<T> type) {
        try {
            return ObjectMapper.readValue(json, type);
        } catch (JsonProcessingException | NoClassDefFoundError e) {
            this.logger.error("{}", e.getMessage());

            return null;
        }
    }

    private @Nullable ContentResponse executeGetRequest(String endpoint) {
        String uri = String.format(REQUEST_FORMAT, this.host, this.port, API, endpoint);

        try {
            return httpClient.GET(uri);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            this.logger.error("Endpoint: {} - {}", endpoint, e.getMessage());

            return null;
        }
    }

    private ContentResponse executeRequest(HttpMethod method, String endpoint)
            throws InterruptedException, TimeoutException, ExecutionException {
        return this.executeRequest(method, endpoint, "");
    }

    private ContentResponse executeRequest(HttpMethod method, String endpoint, String payload)
            throws InterruptedException, TimeoutException, ExecutionException {

        String uri = String.format(REQUEST_FORMAT, this.host, this.port, API, endpoint);

        Request request = this.httpClient.newRequest(uri).method(method);

        if (!payload.isBlank()) {
            request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                    .content(new StringContentProvider(payload));
        }

        if (this.apiAccessToken != null && !this.apiAccessToken.isBlank()) {
            // TODO: Check if we can use httpClient.setAuthenticationStore ...
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.apiAccessToken);
        }

        return request.timeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS).send();
    }

    // #endregion
}
