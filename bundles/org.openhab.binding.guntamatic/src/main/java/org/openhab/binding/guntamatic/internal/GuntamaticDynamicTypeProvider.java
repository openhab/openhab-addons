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
package org.openhab.binding.guntamatic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.AbstractStorageBasedTypeProvider;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link GuntamaticDynamicTypeProvider} is an instance of a {@link AbstractStorageBasedTypeProvider} for the
 * Guntamatic Binding
 *
 * @author Weger Michael - Initial contribution
 */
@Component(service = { GuntamaticDynamicTypeProvider.class, ChannelTypeProvider.class })
@NonNullByDefault
public class GuntamaticDynamicTypeProvider extends AbstractStorageBasedTypeProvider {

    @Activate
    public GuntamaticDynamicTypeProvider(@Reference StorageService storageService) {
        super(storageService);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        String thingUid = uid.getAsString() + ":";
        getChannelTypes(null).stream().map(ChannelType::getUID).filter(c -> c.getAsString().startsWith(thingUid))
                .forEach(this::removeChannelType);
    }
}
