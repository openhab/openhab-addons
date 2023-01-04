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
package org.openhab.binding.bosesoundtouch.internal;

import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bosesoundtouch.internal.handler.BoseSoundTouchHandler;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BoseSoundTouchHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.bosesoundtouch")
public class BoseSoundTouchHandlerFactory extends BaseThingHandlerFactory {

    private @Nullable StorageService storageService;
    private @Nullable BoseStateDescriptionOptionProvider stateOptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        StorageService localStorageService = storageService;
        if (localStorageService != null) {
            Storage<ContentItem> storage = localStorageService.getStorage(thing.getUID().toString(),
                    ContentItem.class.getClassLoader());
            BoseStateDescriptionOptionProvider localDescriptionOptionProvider = stateOptionProvider;
            if (localDescriptionOptionProvider != null) {
                BoseSoundTouchHandler handler = new BoseSoundTouchHandler(thing, new PresetContainer(storage),
                        localDescriptionOptionProvider);
                return handler;
            }
        }
        return null;
    }

    @Reference
    protected void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    protected void unsetStorageService(StorageService storageService) {
        this.storageService = null;
    }

    @Reference
    protected void setPresetChannelTypeProvider(BoseStateDescriptionOptionProvider stateOptionProvider) {
        this.stateOptionProvider = stateOptionProvider;
    }

    protected void unsetPresetChannelTypeProvider(BoseStateDescriptionOptionProvider stateOptionProvider) {
        this.stateOptionProvider = null;
    }
}
