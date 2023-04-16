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
package org.openhab.binding.lametrictime.internal.api.local.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.SortedMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.GsonProvider;
import org.openhab.binding.lametrictime.internal.api.authentication.HttpAuthenticationFeature;
import org.openhab.binding.lametrictime.internal.api.cloud.impl.LaMetricTimeCloudImpl;
import org.openhab.binding.lametrictime.internal.api.common.impl.AbstractClient;
import org.openhab.binding.lametrictime.internal.api.filter.LoggingFilter;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActivationException;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationNotFoundException;
import org.openhab.binding.lametrictime.internal.api.local.LaMetricTimeLocal;
import org.openhab.binding.lametrictime.internal.api.local.LocalConfiguration;
import org.openhab.binding.lametrictime.internal.api.local.NotificationCreationException;
import org.openhab.binding.lametrictime.internal.api.local.NotificationNotFoundException;
import org.openhab.binding.lametrictime.internal.api.local.UpdateException;
import org.openhab.binding.lametrictime.internal.api.local.dto.Api;
import org.openhab.binding.lametrictime.internal.api.local.dto.Application;
import org.openhab.binding.lametrictime.internal.api.local.dto.Audio;
import org.openhab.binding.lametrictime.internal.api.local.dto.AudioUpdateResult;
import org.openhab.binding.lametrictime.internal.api.local.dto.Bluetooth;
import org.openhab.binding.lametrictime.internal.api.local.dto.BluetoothUpdateResult;
import org.openhab.binding.lametrictime.internal.api.local.dto.Device;
import org.openhab.binding.lametrictime.internal.api.local.dto.Display;
import org.openhab.binding.lametrictime.internal.api.local.dto.DisplayUpdateResult;
import org.openhab.binding.lametrictime.internal.api.local.dto.Failure;
import org.openhab.binding.lametrictime.internal.api.local.dto.Notification;
import org.openhab.binding.lametrictime.internal.api.local.dto.NotificationResult;
import org.openhab.binding.lametrictime.internal.api.local.dto.UpdateAction;
import org.openhab.binding.lametrictime.internal.api.local.dto.WidgetUpdates;
import org.openhab.binding.lametrictime.internal.api.local.dto.Wifi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * Implementation class for LaMeticTimeLocal interface.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeLocalImpl extends AbstractClient implements LaMetricTimeLocal {
    private final Logger logger = LoggerFactory.getLogger(LaMetricTimeLocalImpl.class);

    private static final String HEADER_ACCESS_TOKEN = "X-Access-Token";

    private final LocalConfiguration config;

    @Nullable
    private volatile Api api;

    public LaMetricTimeLocalImpl(LocalConfiguration config) {
        this.config = config;
    }

    public LaMetricTimeLocalImpl(LocalConfiguration config, ClientBuilder clientBuilder) {
        super(clientBuilder);
        this.config = config;
    }

    @Override
    public Api getApi() {
        Api localApi = api;
        if (localApi == null) {
            synchronized (this) {
                localApi = getClient().target(config.getBaseUri()).request(MediaType.APPLICATION_JSON_TYPE)
                        .get(Api.class);
                // remove support for v2.0.0 which has several errors in returned endpoints
                if ("2.0.0".equals(localApi.getApiVersion())) {
                    throw new IllegalStateException(
                            "API version 2.0.0 detected, but 2.1.0 or greater is required. Please upgrade LaMetric Time firmware to version 1.7.7 or later. See http://lametric.com/firmware for more information.");
                }
                return localApi;
            }
        } else {
            return localApi;
        }
    }

    @Override
    public Device getDevice() {
        return getClient().target(getApi().getEndpoints().getDeviceUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .get(Device.class);
    }

    @Override
    public String createNotification(@Nullable Notification notification) throws NotificationCreationException {
        Response response = getClient().target(getApi().getEndpoints().getNotificationsUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(notification));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new NotificationCreationException(response.readEntity(Failure.class));
        }

        try {
            return response.readEntity(NotificationResult.class).getSuccess().getId();
        } catch (Exception e) {
            throw new NotificationCreationException("Invalid JSON returned from service", e);
        }
    }

    @Override
    public List<Notification> getNotifications() {
        Response response = getClient().target(getApi().getEndpoints().getNotificationsUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        Reader entity = new BufferedReader(
                new InputStreamReader((InputStream) response.getEntity(), StandardCharsets.UTF_8));

        // @formatter:off
        return getGson().fromJson(entity,  new TypeToken<List<Notification>>(){}.getType());
        // @formatter:on
    }

    @Override
    public @Nullable Notification getCurrentNotification() {
        Notification notification = getClient().target(getApi().getEndpoints().getCurrentNotificationUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).get(Notification.class);

        // when there is no current notification, return null
        if (notification.getId() == null) {
            return null;
        }

        return notification;
    }

    @Override
    public Notification getNotification(String id) throws NotificationNotFoundException {
        Response response = getClient()
                .target(getApi().getEndpoints().getConcreteNotificationUrl().replace("{:id}", id))
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new NotificationNotFoundException(response.readEntity(Failure.class));
        }

        return response.readEntity(Notification.class);
    }

    @Override
    public void deleteNotification(String id) throws NotificationNotFoundException {
        Response response = getClient()
                .target(getApi().getEndpoints().getConcreteNotificationUrl().replace("{:id}", id))
                .request(MediaType.APPLICATION_JSON_TYPE).delete();

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new NotificationNotFoundException(response.readEntity(Failure.class));
        }

        response.close();
    }

    @Override
    public Display getDisplay() {
        return getClient().target(getApi().getEndpoints().getDisplayUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .get(Display.class);
    }

    @Override
    public Display updateDisplay(Display display) throws UpdateException {
        Response response = getClient().target(getApi().getEndpoints().getDisplayUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(display));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new UpdateException(response.readEntity(Failure.class));
        }

        return response.readEntity(DisplayUpdateResult.class).getSuccess().getData();
    }

    @Override
    public Audio getAudio() {
        return getClient().target(getApi().getEndpoints().getAudioUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .get(Audio.class);
    }

    @Override
    public Audio updateAudio(Audio audio) throws UpdateException {
        Response response = getClient().target(getApi().getEndpoints().getAudioUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(audio));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new UpdateException(response.readEntity(Failure.class));
        }

        return response.readEntity(AudioUpdateResult.class).getSuccess().getData();
    }

    @Override
    public Bluetooth getBluetooth() {
        return getClient().target(getApi().getEndpoints().getBluetoothUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .get(Bluetooth.class);
    }

    @Override
    public Bluetooth updateBluetooth(Bluetooth bluetooth) throws UpdateException {
        Response response = getClient().target(getApi().getEndpoints().getBluetoothUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(bluetooth));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new UpdateException(response.readEntity(Failure.class));
        }

        return response.readEntity(BluetoothUpdateResult.class).getSuccess().getData();
    }

    @Override
    public Wifi getWifi() {
        return getClient().target(getApi().getEndpoints().getWifiUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .get(Wifi.class);
    }

    @Override
    public void updateApplication(String packageName, String accessToken, WidgetUpdates widgetUpdates)
            throws UpdateException {
        Response response = getClient()
                .target(getApi().getEndpoints().getWidgetUpdateUrl().replace("{:id}", packageName))
                .request(MediaType.APPLICATION_JSON_TYPE).header(HEADER_ACCESS_TOKEN, accessToken)
                .post(Entity.json(widgetUpdates));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new UpdateException(response.readEntity(Failure.class));
        }

        response.close();
    }

    @Override
    public SortedMap<String, Application> getApplications() {
        Response response = getClient().target(getApi().getEndpoints().getAppsListUrl())
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        Reader entity = new BufferedReader(
                new InputStreamReader((InputStream) response.getEntity(), StandardCharsets.UTF_8));

        // @formatter:off
        return getGson().fromJson(entity,
                                  new TypeToken<SortedMap<String, Application>>(){}.getType());
        // @formatter:on
    }

    @Override
    public @Nullable Application getApplication(@Nullable String packageName) throws ApplicationNotFoundException {
        if (packageName != null) {
            Response response = getClient()
                    .target(getApi().getEndpoints().getAppsGetUrl().replace("{:id}", packageName))
                    .request(MediaType.APPLICATION_JSON_TYPE).get();

            if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                throw new ApplicationNotFoundException(response.readEntity(Failure.class));
            }

            return response.readEntity(Application.class);
        } else {
            return null;
        }
    }

    @Override
    public void activatePreviousApplication() {
        getClient().target(getApi().getEndpoints().getAppsSwitchPrevUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.json(new Object()));
    }

    @Override
    public void activateNextApplication() {
        getClient().target(getApi().getEndpoints().getAppsSwitchNextUrl()).request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.json(new Object()));
    }

    @Override
    public void activateApplication(String packageName, String widgetId) throws ApplicationActivationException {
        Response response = getClient().target(getApi().getEndpoints().getAppsSwitchUrl().replace("{:id}", packageName)
                .replace("{:widget_id}", widgetId)).request(MediaType.APPLICATION_JSON_TYPE)
                .put(Entity.json(new Object()));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new ApplicationActivationException(response.readEntity(Failure.class));
        }

        response.close();
    }

    @Override
    public void doAction(String packageName, String widgetId, @Nullable UpdateAction action)
            throws ApplicationActionException {
        Response response = getClient().target(getApi().getEndpoints().getAppsActionUrl().replace("{:id}", packageName)
                .replace("{:widget_id}", widgetId)).request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(action));

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new ApplicationActionException(response.readEntity(Failure.class));
        }

        response.close();
    }

    @Override
    protected Client createClient() {
        ClientBuilder builder = clientBuilder;

        // setup Gson (de)serialization
        GsonProvider<Object> gsonProvider = new GsonProvider<>();
        builder.register(gsonProvider);

        if (config.isSecure()) {
            /*
             * The certificate presented by LaMetric time is self-signed.
             * Therefore, unless the user takes action by adding the certificate
             * chain to the Java keystore, HTTPS will fail.
             *
             * By setting the ignoreCertificateValidation configuration option
             * to true (default), HTTPS will be used and the connection will be
             * encrypted, but the validity of the certificate is not confirmed.
             */
            if (config.isIgnoreCertificateValidation()) {
                try {
                    SSLContext sslcontext = SSLContext.getInstance("TLS");
                    sslcontext.init(null, new TrustManager[] { new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate @Nullable [] arg0, @Nullable String arg1)
                                throws CertificateException {
                            // noop
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate @Nullable [] arg0, @Nullable String arg1)
                                throws CertificateException {
                            // noop
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    } }, new java.security.SecureRandom());
                    builder.sslContext(sslcontext);
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    logger.error("Failed to setup secure communication", e);
                }
            }

            /*
             * The self-signed certificate used by LaMetric time does not match
             * the host when configured on a network. This makes the HTTPS
             * handshake fail.
             *
             * By setting the ignoreHostnameValidation configuration option to
             * true (default), HTTPS will be used and the connection will be
             * encrypted, but the validity of the hostname in the certificate is
             * not confirmed.
             */
            if (config.isIgnoreHostnameValidation()) {
                builder.hostnameVerifier((host, session) -> true);
            }
        }

        // turn on logging if requested
        if (config.isLogging()) {
            builder.register(new LoggingFilter(
                    java.util.logging.Logger.getLogger(LaMetricTimeCloudImpl.class.getName()), config.getLogMax()));
        }

        // setup basic auth
        builder.register(HttpAuthenticationFeature.basic(config.getAuthUser(), config.getApiKey()));

        return builder.build();
    }
}
