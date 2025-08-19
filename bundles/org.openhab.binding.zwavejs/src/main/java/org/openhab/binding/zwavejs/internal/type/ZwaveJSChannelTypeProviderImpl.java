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
package org.openhab.binding.zwavejs.internal.type;

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
 * Implementation of the {@link ZwaveJSChannelTypeProvider} interface that provides
 * channel types for Z-Wave JS devices. This class extends the {@link AbstractStorageBasedTypeProvider}
 * to leverage storage-based channel type management.
 *
 * <p>
 * This class is registered as an OSGi component and provides services for the
 * {@link ZwaveJSChannelTypeProvider} interface.
 *
 * @see ZwaveJSChannelTypeProvider
 * @see ChannelTypeProvider
 * @see AbstractStorageBasedTypeProvider
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
@Component(service = { ZwaveJSChannelTypeProvider.class, ChannelTypeProvider.class })
public class ZwaveJSChannelTypeProviderImpl extends AbstractStorageBasedTypeProvider
        implements ZwaveJSChannelTypeProvider {

    @Activate
    public ZwaveJSChannelTypeProviderImpl(@Reference StorageService storageService) {
        super(storageService);
    }

    /*
     * Removes all channel types associated with the specified ThingUID.
     *
     * @param uid the ThingUID for which the channel types should be removed
     */
    public void removeChannelTypesForThing(ThingUID uid) {
        String thingUid = uid.getAsString() + ":";
        getChannelTypes(null).stream().map(ChannelType::getUID).filter(c -> c.getAsString().startsWith(thingUid))
                .forEach(this::removeChannelType);
    }

    @Override
    public void addChannelType(ChannelType channelType) {
        this.putChannelType(channelType);
    }
}
