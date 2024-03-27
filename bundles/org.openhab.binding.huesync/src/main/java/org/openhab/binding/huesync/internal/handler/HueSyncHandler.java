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
package org.openhab.binding.huesync.internal.handler;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncConnection;
import org.openhab.binding.huesync.internal.connection.HueSyncTrustManagerProvider;
import org.openhab.binding.huesync.internal.exceptions.HueSyncApiException;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;

/**
 * The {@link HueSyncHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {
    /** the device registration id */
    static final String REGISTRATION_ID = "registrationId";
    static final String API_TOKEN = "apiAccessToken";

    /** the key for the api version property */
    static final String PROPERTY_API_VERSION = "apiVersion";
    /** the ky for the network state property */
    static final String PROPERTY_NETWORK_STATE = "networkState";

    @SuppressWarnings("null")
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncHandler.class);

    private HueSyncConfiguration config;

    private @Nullable BundleContext context;
    private @Nullable ScheduledFuture<?> deviceRegistrationTask;

    private @Nullable HueSyncConnection connection;
    private @Nullable HueSyncDeviceInfo deviceInfo;
    private @Nullable ServiceRegistration<?> serviceRegistration;

    private HttpClient httpClient;

    @SuppressWarnings("null")
    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory)
            throws CertificateException, IOException, Exception {
        super(thing);

        this.config = getConfigAs(HueSyncConfiguration.class);

        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.config.host,
                this.config.port);

        this.context = FrameworkUtil.getBundle(getClass()).getBundleContext();
        this.serviceRegistration = this.context.registerService(TlsTrustManagerProvider.class.getName(),
                trustManagerProvider, null);

        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: Implementation ...
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                this.connection = new HueSyncConnection(this.httpClient, this.config);

                this.deviceInfo = this.connection.getDeviceInfo();

                Map<String, String> properties = this.editProperties();

                properties.put(Thing.PROPERTY_SERIAL_NUMBER, this.deviceInfo.uniqueId);
                properties.put(Thing.PROPERTY_MODEL_ID, this.deviceInfo.deviceType);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, this.deviceInfo.firmwareVersion);

                properties.put(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", this.deviceInfo.apiLevel));
                properties.put(HueSyncHandler.PROPERTY_NETWORK_STATE, this.deviceInfo.wifiState);

                this.updateProperties(properties);

                this.checkCompatibility();
                this.checkRegistration();

                if (this.thing.getStatus() == ThingStatus.OFFLINE) {
                    this.startDeviceRegistrationTask();
                }
            } catch (HueSyncApiException e) {
                this.logger.error("{}", e.getLogMessage());
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (Exception e) {
                this.logger.error("{}", e.getMessage());
                this.updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @SuppressWarnings("null")
    private void startDeviceRegistrationTask() {
        if (this.deviceRegistrationTask != null) {
            return;
        }

        this.logger.info("Starting registration job for {} {}:{}", this.deviceInfo.name, this.deviceInfo.deviceType,
                this.deviceInfo.uniqueId);

        this.deviceRegistrationTask = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (this.thing.getStatus() == ThingStatus.OFFLINE) {
                    HueSyncRegistration registration = this.connection.registerDevice();

                    if (registration != null) {
                        Map<String, String> properties = this.editProperties();

                        properties.put(HueSyncHandler.PROPERTY_API_VERSION,
                                String.format("%d", this.deviceInfo.apiLevel));
                        properties.put(HueSyncHandler.REGISTRATION_ID, registration.registrationId);

                        this.config.registrationId = registration.registrationId;
                        this.config.apiAccessToken = registration.accessToken;

                        Configuration configuration = this.editConfiguration();

                        configuration.put(HueSyncHandler.REGISTRATION_ID, this.config.registrationId);
                        configuration.put(HueSyncHandler.API_TOKEN, this.config.apiAccessToken);

                        this.updateConfiguration(configuration);
                        this.updateProperties(properties);
                        this.updateStatus(ThingStatus.ONLINE);

                        this.deviceRegistrationTask.cancel(false);

                        this.logger.info("Device registration for {} complete - Id: {}", this.deviceInfo.name,
                                registration.registrationId);
                    }
                }
            } catch (Exception e) {
                this.logger.debug("{}", e.getMessage());
            }
        }, HueSyncConstants.REGISTRATION_INITIAL_DELAY, HueSyncConstants.REGISTRATION_DELAY, TimeUnit.SECONDS);
    }

    private void checkRegistration() {
        if (this.config.apiAccessToken.isEmpty() || this.config.apiAccessToken.isBlank()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/thing.config.huesync.box.registration");
        } else {
            this.updateStatus(ThingStatus.ONLINE);
        }
    }

    private void checkCompatibility() throws HueSyncApiException {
        if (this.deviceInfo != null && this.deviceInfo.apiLevel < HueSyncConstants.MINIMAL_API_VERSION) {
            throw new HueSyncApiException("@text/api.minimal-version");
        }
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        super.dispose();

        try {
            if (this.deviceRegistrationTask != null && !this.deviceRegistrationTask.isDone()) {
                this.deviceRegistrationTask.cancel(true);
            }

            if (this.serviceRegistration != null) {
                this.serviceRegistration.unregister();
            }

        } catch (Exception e) {
            this.logger.error("{}", e.getMessage());
        } finally {
            this.deviceRegistrationTask = null;
            this.serviceRegistration = null;

            this.logger.info("Thing {} ({}) disposed.", this.thing.getLabel(), this.thing.getUID());
        }
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();

        if (this.connection != null && !this.connection.unregisterDevice()) {
            this.logger.error(
                    "It was not possible to unregister {} ({}). You may use id: {} Key: {} to manually re-configure the thing, or to manually remove the device via API.",
                    this.thing.getLabel(), this.thing.getUID(), this.config.registrationId, this.config.apiAccessToken);
        }
    }
}
