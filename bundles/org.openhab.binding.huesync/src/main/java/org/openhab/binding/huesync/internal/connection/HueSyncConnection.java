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
     * 
     * @author Patrik Gfeller - Initial Contribution
     */
    protected class HueSyncConnectionHelper {
        private static class ENDPOINTS {
            public static final String DEVICE = "device";
            public static final String REGISTRATIONS = "registrations";
        }

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

        protected static final ObjectMapper ObjectMapper = new ObjectMapper();

        protected String apiAccessToken;
        protected String registrationId;

        public HueSyncConnectionHelper(HttpClient httpClient, String host, Integer port, String apiAccessToken,
                String registrationId) throws CertificateException, IOException {

            this.host = host;
            this.port = port;
            this.apiAccessToken = apiAccessToken;
            this.registrationId = registrationId;

            HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.host, this.port);
            BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

            this.tlsProviderService = context.registerService(TlsTrustManagerProvider.class.getName(),
                    trustManagerProvider, null);
            this.httpClient = httpClient;
        }

        protected @Nullable <T> T executeRequest(HttpMethod method, String endpoint, String payload, Class<T> type) {
            try {
                ContentResponse response = this.executeRequest(method, endpoint, payload);

                if (response != null) {
                    return processedResponse(response, type);
                }

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                this.logger.error("{}", e.getMessage());
            }
            return null;
        }

        protected @Nullable <T> T executeGetRequest(String endpoint, Class<T> type) {
            try {
                ContentResponse response = this.executeGetRequest(endpoint);

                if (response != null) {
                    return processedResponse(response, type);
                }

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                this.logger.error("{}", e.getMessage());
            }

            return null;
        }

        protected boolean isRegistered() {
            if (this.apiAccessToken.isBlank() || this.registrationId.isBlank()) {
                return false;
            }

            return true;
        }

        protected boolean unregisterDevice() {
            if (this.isRegistered()) {
                try {
                    String endpoint = ENDPOINTS.REGISTRATIONS + "/" + this.registrationId;
                    ContentResponse response = this.executeRequest(HttpMethod.DELETE, endpoint);

                    if (response.getStatus() == HttpStatus.OK_200) {
                        this.registrationId = "";
                        this.apiAccessToken = "";

                        return true;
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    this.logger.error("{}", e.getMessage());
                }
            }
            return false;
        }

        protected void stop() {
            this.tlsProviderService.unregister();
        }

        private @Nullable <T> T processedResponse(ContentResponse response, Class<T> type) {
            switch (response.getStatus()) {
                case HttpStatus.OK_200:
                    return this.deserialize(response.getContentAsString(), type);
                default:
                    logger.warn("HTTP Status: {}", response.getStatus());
                    return null;
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

            if (this.isRegistered()) {
                request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.apiAccessToken);
            }

            return request.send();
        }
    };

    private HueSyncConnectionHelper helper;
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncConnection.class);

    public HueSyncConnection(HttpClient httpClient, String host, Integer port, String apiAccessToken,
            String registrationId) throws CertificateException, IOException {

        this.helper = new HueSyncConnectionHelper(httpClient, host, port, apiAccessToken, registrationId);
    }

    public @Nullable HueSyncDeviceInfo getDeviceInfo() {
        return this.helper.executeGetRequest(HueSyncConnectionHelper.ENDPOINTS.DEVICE, HueSyncDeviceInfo.class);
    }

    public @Nullable HueSyncRegistration registerDevice(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();

        dto.appName = HueSyncConstants.APPLICATION_NAME;
        dto.instanceName = id;

        try {
            String json = HueSyncConnectionHelper.ObjectMapper.writeValueAsString(dto);
            HueSyncRegistration registration = this.helper.executeRequest(HttpMethod.POST,
                    HueSyncConnectionHelper.ENDPOINTS.REGISTRATIONS, json, HueSyncRegistration.class);

            if (registration != null) {
                this.helper.registrationId = registration.registrationId != null ? registration.registrationId : "";
                this.helper.apiAccessToken = registration.accessToken != null ? registration.accessToken : "";
            }

            return registration;
        } catch (JsonProcessingException e) {
            this.logger.error("{}", e.getMessage());
        }

        return null;
    }

    public @Nullable HueSyncDetailedDeviceInfo getDetailedDeviceInfo() {
        if (this.helper.isRegistered()) {
            return this.helper.executeRequest(HttpMethod.GET, HueSyncConnectionHelper.ENDPOINTS.DEVICE, "",
                    HueSyncDetailedDeviceInfo.class);
        }

        return null;
    }

    public boolean isRegistered() {
        return this.helper.isRegistered();
    }

    public boolean unregisterDevice() {
        return this.helper.unregisterDevice();
    }

    public void stop() {
        this.helper.stop();
    }
}
