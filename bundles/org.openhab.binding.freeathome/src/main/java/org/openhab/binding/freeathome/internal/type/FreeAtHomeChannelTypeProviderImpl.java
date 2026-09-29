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
package org.openhab.binding.freeathome.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@Component(service = { FreeAtHomeChannelTypeProvider.class, ChannelTypeProvider.class })
@NonNullByDefault
public class FreeAtHomeChannelTypeProviderImpl extends AbstractStorageBasedTypeProvider
        implements FreeAtHomeChannelTypeProvider {

    @Activate
    public FreeAtHomeChannelTypeProviderImpl(@Reference StorageService storageService) {
        super(storageService);
    }

    @Override
    public void addChannelType(ChannelType channelType) {
        putChannelType(channelType);
    }
}
