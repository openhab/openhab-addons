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
package org.openhab.binding.siemenshvac.internal.handler;

import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.discovery.SiemensHvacDeviceDiscoveryService;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensHvacBridgeBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution and API
 */
@NonNullByDefault
public class SiemensHvacBridgeThingHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacBridgeThingHandler.class);
    private @Nullable SiemensHvacDeviceDiscoveryService discoveryService;
    private final @Nullable HttpClientFactory httpClientFactory;
    private final SiemensHvacMetadataRegistry metaDataRegistry;
    private @Nullable SiemensHvacBridgeConfig config;
    private final TranslationProvider translationProvider;

    public SiemensHvacBridgeThingHandler(Bridge bridge, @Nullable NetworkAddressService networkAddressService,
            @Nullable HttpClientFactory httpClientFactory, SiemensHvacMetadataRegistry metaDataRegistry,
            TranslationProvider translationProvider) {
        super(bridge);
        SiemensHvacConnector lcConnector = null;
        this.httpClientFactory = httpClientFactory;
        this.metaDataRegistry = metaDataRegistry;
        this.translationProvider = translationProvider;

        lcConnector = this.metaDataRegistry.getSiemensHvacConnector();
        if (lcConnector != null) {
            lcConnector.setSiemensHvacBridgeBaseThingHandler(this);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        metaDataRegistry.invalidate();
    }

    @Override
    public void initialize() {
        SiemensHvacBridgeConfig lcConfig = getConfigAs(SiemensHvacBridgeConfig.class);
        String baseUrl = null;

        if (logger.isDebugEnabled()) {
            logger.debug("Initialize() bridge: {}", getBuildDate());
        }

        baseUrl = lcConfig.baseUrl;

        if (baseUrl.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.error-gateway-init");
            return;
        }

        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "http://" + baseUrl;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        config = lcConfig;

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/offline.waiting-bridge-initialization");

        // Will read metadata in background to not block initialize for a long period !
        scheduler.schedule(this::initializeCode, 1, TimeUnit.SECONDS);
    }

    protected String getBuildDate() {
        try {
            ClassLoader cl = getClass().getClassLoader();
            if (cl != null) {
                URL res = cl.getResource(getClass().getCanonicalName().replace('.', '/') + ".class");
                if (res != null) {
                    URLConnection cnx = res.openConnection();
                    LocalDate dt = LocalDate.ofEpochDay(cnx.getLastModified());
                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    return df.format(dt);
                }
            }

        } catch (Exception ex) {
        }
        return "unknown";
    }

    public static String getStackTrace(final Throwable throwable) {
        StringBuffer sb = new StringBuffer();

        Throwable current = throwable;
        while (current != null) {
            sb.append(current.getLocalizedMessage());
            sb.append(",\r\n");

            Throwable cause = throwable.getCause();
            if (cause != null) {
                if (!cause.equals(throwable)) {
                    current = current.getCause();
                } else {
                    current = null;
                }
            } else {
                current = null;
            }
        }
        return sb.toString();
    }

    private void initializeCode() {
        try {
            metaDataRegistry.readMeta();
            updateStatus(ThingStatus.ONLINE);
        } catch (SiemensHvacException ex) {
            Locale local = metaDataRegistry.getUserLocale();
            BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            String text = translationProvider.getText(bundleContext.getBundle(), "offline.error-gateway-init",
                    "DefaultValue", local);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    MessageFormat.format(text, ex.getMessage()));
        }
    }

    public @Nullable SiemensHvacBridgeConfig getBridgeConfiguration() {
        return config;
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public boolean registerDiscoveryListener(SiemensHvacDeviceDiscoveryService listener) {
        SiemensHvacDeviceDiscoveryService lcDiscoveryService = discoveryService;
        if (lcDiscoveryService == null) {
            lcDiscoveryService = listener;
            lcDiscoveryService.setSiemensHvacMetadataRegistry(metaDataRegistry);
            return true;
        }

        return false;
    }

    public boolean unregisterDiscoveryListener() {
        SiemensHvacDeviceDiscoveryService lcDiscoveryService = discoveryService;
        if (lcDiscoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    public @Nullable HttpClientFactory getHttpClientFactory() {
        return this.httpClientFactory;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SiemensHvacDeviceDiscoveryService.class);
    }
}
