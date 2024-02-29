/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.connection.HueSyncConnection;
import org.openhab.binding.huesync.internal.connection.HueSyncTrustManagerProvider;
import org.openhab.binding.huesync.internal.exceptions.HueSyncApiException;
import org.openhab.binding.huesync.internal.util.HueSyncLogLocalizer;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueSyncHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {

    /** the key for the api version property */
    static final String PROPERTY_API_VERSION = "apiVersion";
    /** the ky for the network state property */
    static final String PROPERTY_NETWORK_STATE = "networkState";

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(HueSyncHandler.class);

    private HueSyncConfiguration config;

    private @Nullable HueSyncConnection connection;
    private @Nullable HueSyncDeviceInfo deviceInfo;
    private @Nullable ServiceRegistration<?> serviceRegistration;

    private HttpClient httpClient;

    public HueSyncHandler(Thing thing, HttpClientFactory httpClientFactory)
            throws CertificateException, IOException, Exception {
        super(thing);

        this.config = getConfigAs(HueSyncConfiguration.class);

        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.config.host,
                this.config.port);

        this.serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
                .registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider, null);

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

                Map<String, String> properties = editProperties();

                properties.put(Thing.PROPERTY_SERIAL_NUMBER, this.deviceInfo.uniqueId);
                properties.put(Thing.PROPERTY_MODEL_ID, this.deviceInfo.deviceType);
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, this.deviceInfo.firmwareVersion);

                properties.put(HueSyncHandler.PROPERTY_API_VERSION, String.format("%d", this.deviceInfo.apiLevel));
                properties.put(HueSyncHandler.PROPERTY_NETWORK_STATE, this.deviceInfo.wifiState);

                this.updateProperties(properties);

                this.checkCompatibility();

                if (this.config.apiAccessToken.isEmpty() || this.config.apiAccessToken.isBlank()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (HueSyncApiException e) {
                // TODO: Refacture - simplify use of logger ...
                this.logger.error(HueSyncLogLocalizer.getResourceString("@text/logger.initialization-problem"),
                        this.thing.getLabel(), this.thing.getUID(),
                        HueSyncLogLocalizer.getResourceString(e.getMessage()));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (Exception e) {
                this.logger.error(HueSyncLogLocalizer.getResourceString("@text/logger.initialization-problem"),
                        this.thing.getLabel(), this.thing.getUID(),
                        HueSyncLogLocalizer.getResourceString(e.getMessage()));

                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    private void checkCompatibility() throws HueSyncApiException {
        throw new HueSyncApiException("@text/api.minimal-version");

        // if (this.deviceInfo != null && this.deviceInfo.apiLevel <
        // HueSyncBindingConstants.MINIMAL_API_VERSION) {
        // throw new HueSyncApiException("@text/api.minimal-version");
        // }
    }

    @Override
    public void dispose() {
        super.dispose();

        try {
            if (this.serviceRegistration != null) {
                this.serviceRegistration.unregister();
                this.serviceRegistration = null;
            }
        } catch (Exception e) {
            this.logger.error("Unable to properly dispose handler for {}({}): {}", this.thing.getLabel(),
                    this.thing.getUID(), e);
        }
    }
}
