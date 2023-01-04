/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.discovery;

import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jellyfin.sdk.Jellyfin;
import org.jellyfin.sdk.JellyfinOptions;
import org.jellyfin.sdk.api.client.exception.ApiClientException;
import org.jellyfin.sdk.api.operations.SystemApi;
import org.jellyfin.sdk.compatibility.JavaFlow;
import org.jellyfin.sdk.compatibility.JavaFlow.FlowJob;
import org.jellyfin.sdk.model.ClientInfo;
import org.jellyfin.sdk.model.DeviceInfo;
import org.jellyfin.sdk.model.api.PublicSystemInfo;
import org.jellyfin.sdk.model.api.ServerDiscoveryInfo;
import org.openhab.binding.jellyfin.internal.util.SyncCallback;
import org.openhab.binding.jellyfin.internal.util.SyncResponse;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JellyfinServerDiscoveryService} discover Jellyfin servers in the network.
 *
 * @author Miguel Alvarez - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.jellyfin")
public class JellyfinServerDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(JellyfinServerDiscoveryService.class);
    @Nullable
    private FlowJob cancelDiscovery;

    public JellyfinServerDiscoveryService() throws IllegalArgumentException {
        super(Set.of(THING_TYPE_CLIENT), 60);
    }

    @Override
    protected void startScan() {
        var opts = new JellyfinOptions.Builder();
        opts.setClientInfo(new ClientInfo("openHAB", OpenHAB.getVersion()));
        opts.setDeviceInfo(new DeviceInfo("discovery", "openHAB"));
        var jellyfin = new Jellyfin(opts.build());
        var discoverySvc = new org.jellyfin.sdk.discovery.DiscoveryService(jellyfin);
        logger.debug("Starting search");
        cancelDiscovery = JavaFlow.collect(discoverySvc.discoverLocalServers(100, 10), null, (info) -> {
            if (info == null) {
                return;
            }
            logger.debug("Server found: [{}] {}", info.getId(), info.getName());
            processDiscoveryResult(jellyfin, info);
        }, (throwable) -> {
            if (throwable != null) {
                logger.warn("Discovery Error: {}", throwable.getMessage());
            } else {
                logger.debug("Discovery ends");
            }
        });
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        var cancelDiscovery = this.cancelDiscovery;
        if (cancelDiscovery != null) {
            cancelDiscovery.close();
            this.cancelDiscovery = null;
        }
    }

    private void processDiscoveryResult(Jellyfin jellyfin, ServerDiscoveryInfo info) {
        URI uri;
        try {
            uri = new URI(Objects.requireNonNull(info.getAddress()));
        } catch (URISyntaxException e) {
            logger.warn("Error parsing server url: {}", e.getMessage());
            return;
        }
        var jellyClient = jellyfin.createApi(info.getAddress());
        var asyncResponse = new SyncResponse<PublicSystemInfo>();
        new SystemApi(jellyClient).getPublicSystemInfo(asyncResponse);
        try {
            var publicSystemInfo = asyncResponse.awaitContent();
            discoverServer(uri.getHost(), uri.getPort(), uri.getScheme().equalsIgnoreCase("https"), uri.getPath(),
                    publicSystemInfo);
        } catch (SyncCallback.SyncCallbackError | ApiClientException e) {
            logger.warn("Discovery error: {}", e.getMessage());
        }
    }

    private void discoverServer(String hostname, int port, boolean ssl, String path,
            PublicSystemInfo publicSystemInfo) {
        logger.debug("Server discovered: [{}:{}] {}", hostname, port, publicSystemInfo.getServerName());
        var id = Objects.requireNonNull(publicSystemInfo.getId());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hostname", hostname);
        properties.put("port", port);
        properties.put("ssl", ssl);
        properties.put("path", path);
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        var productName = publicSystemInfo.getProductName();
        if (productName != null) {
            properties.put(Thing.PROPERTY_VENDOR, productName);
        }
        var version = publicSystemInfo.getVersion();
        if (version != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, version);
        }
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_SERVER, publicSystemInfo.getId()))
                .withTTL(DISCOVERY_RESULT_TTL_SEC).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                .withProperties(properties).withLabel(publicSystemInfo.getServerName()).build());
    }
}
