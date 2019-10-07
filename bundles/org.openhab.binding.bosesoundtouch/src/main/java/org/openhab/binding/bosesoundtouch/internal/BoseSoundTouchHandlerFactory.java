/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.storage.Storage;
import org.eclipse.smarthome.core.storage.StorageService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.bosesoundtouch.internal.handler.BoseSoundTouchHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BoseSoundTouchHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Niessner - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.bosesoundtouch")
public class BoseSoundTouchHandlerFactory extends BaseThingHandlerFactory {

    private StorageService storageService;
    private BoseStateDescriptionOptionProvider stateOptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        Storage<ContentItem> storage = storageService.getStorage(thing.getUID().toString(),
                ContentItem.class.getClassLoader());
        BoseSoundTouchHandler handler = new BoseSoundTouchHandler(thing, new PresetContainer(storage),
                stateOptionProvider);
        return handler;
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
