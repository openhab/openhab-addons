/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smhi.provider;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smhi.internal.Util;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link ChannelTypeProvider}.
 * Handles dynamic creation of channel types, and stores parameter
 * metadata.
 *
 * @author Anders Alfredsson - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, SmhiChannelTypeProvider.class })
@NonNullByDefault
public class SmhiChannelTypeProvider extends AbstractStorageBasedTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(SmhiChannelTypeProvider.class);

    private final Map<String, ParameterMetadata> parameterMetadata;
    private ZonedDateTime lastChannelUpdate = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private final ChannelTypeRegistry channelTypeRegistry;

    @Activate
    public SmhiChannelTypeProvider(@Reference StorageService storageService,
            @Reference ChannelTypeRegistry channelTypeRegistry) {
        super(storageService);
        this.channelTypeRegistry = channelTypeRegistry;
        this.parameterMetadata = new LinkedHashMap<>();
    }

    public synchronized void putParameterMetadata(ParameterMetadata metadata) {
        logger.trace("Adding parameter metadata for {}", metadata.name());
        parameterMetadata.put(metadata.name(), metadata);
        ChannelType channelType = channelTypeRegistry.getChannelType(new ChannelTypeUID(BINDING_ID, metadata.name()),
                null);
        if (channelType == null) {
            logger.debug("Adding new channel: {}", metadata.name());
            putChannelType(Util.createChannelTypeFromMetadata(metadata));
            lastChannelUpdate = ZonedDateTime.now();
        }
    }

    public synchronized Collection<ParameterMetadata> getAllParameterMetadata() {
        return List.copyOf(parameterMetadata.values());
    }

    public synchronized @Nullable ParameterMetadata getParameterMetadata(String name) {
        return parameterMetadata.get(name);
    }

    public synchronized boolean channelsUpdatedSince(Duration duration) {
        return ZonedDateTime.now().minus(duration).isBefore(lastChannelUpdate);
    }
}
