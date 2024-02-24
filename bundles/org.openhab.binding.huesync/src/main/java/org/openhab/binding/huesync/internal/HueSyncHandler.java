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
package org.openhab.binding.huesync.internal;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.connection.HueSyncConnection;
import org.openhab.binding.huesync.internal.connection.HueSyncTrustManagerProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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
 * @author Marco Kawon - Initial contribution
 * @author Patrik Gfeller - Integration into official repository, update to 4.x
 *         infrastructure
 */
@NonNullByDefault
public class HueSyncHandler extends BaseThingHandler {

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

        @SuppressWarnings("null")
        HueSyncTrustManagerProvider trustManagerProvider = new HueSyncTrustManagerProvider(this.config.host,
                this.config.port);

        this.serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
                .registerService(TlsTrustManagerProvider.class.getName(), trustManagerProvider, null);

        SslContextFactory context = new Client.Client();

        this.logger.debug("SSL context - Alias: {}", context.getCertAlias());
        this.logger.debug("Context: ", context.dumpSelf());

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
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.firmwareVersion);

                updateProperties(properties);

                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                this.logger.error("Unable to initialize handler for {}({}): {}", this.thing.getLabel(),
                        this.thing.getUID(), e);

                updateStatus(ThingStatus.OFFLINE);
            }
        });
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
