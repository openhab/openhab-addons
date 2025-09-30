/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.persistence;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomekitTypeProvider} is responsible for loading and storing HomeKit specific channel and
 * channel group types.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = { ChannelTypeProvider.class, ChannelGroupTypeProvider.class, HomekitTypeProvider.class })
public class HomekitTypeProvider extends AbstractStorageBasedTypeProvider {

    /**
     * Creates a HomekitTypeProvider which uses the given {@link StorageService} to persist the types. It forces
     * that OSGI loads {@link StorageService}, {@link ChannelTypeRegistry}, and {@link ChannelGroupTypeRegistry}
     * before this component gets loaded. Which ensures this component is active before the handler factory gets
     * loaded, and therefore before any thing handlers are created and could start creating channels.
     */
    @Activate
    public HomekitTypeProvider(@Reference StorageService storageService,
            @Reference ChannelTypeRegistry channelTypeRegistry,
            @Reference ChannelGroupTypeRegistry channelGroupTypeRegistry) {
        super(storageService);
    }
}
