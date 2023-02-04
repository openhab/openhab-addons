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
package org.openhab.binding.siemenshvac.internal.handler;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.discovery.SiemensHvacDeviceDiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts a a
 * conduit for other {@link DeviceThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - Refactoring & cleanup
 */
@NonNullByDefault
public class SiemensHvacOZW672BridgeThingHandler extends SiemensHvacBridgeBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacOZW672BridgeThingHandler.class);

    @Activate
    public SiemensHvacOZW672BridgeThingHandler(Bridge bridge, @Nullable NetworkAddressService networkAddressService,
            @Nullable HttpClientFactory httpClientFactory, SiemensHvacMetadataRegistry metaDataRegistry) {
        super(bridge, networkAddressService, httpClientFactory, metaDataRegistry);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize() bridge");

        super.initialize();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SiemensHvacDeviceDiscoveryService.class);
    }
}
