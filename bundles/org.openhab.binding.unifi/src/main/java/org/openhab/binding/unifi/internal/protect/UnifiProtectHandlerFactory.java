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
package org.openhab.binding.unifi.internal.protect;

import static org.openhab.binding.unifi.internal.protect.UnifiProtectBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectCameraHandler;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectChimeHandler;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectDoorlockHandler;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectLightHandler;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectNVRHandler;
import org.openhab.binding.unifi.internal.protect.handler.UnifiProtectSensorHandler;
import org.openhab.binding.unifi.internal.protect.media.UnifiMediaService;
import org.openhab.binding.unifi.internal.protect.util.TranslationService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link UnifiProtectHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.unifiprotect", service = ThingHandlerFactory.class)
public class UnifiProtectHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_NVR, THING_TYPE_CAMERA,
            THING_TYPE_LIGHT, THING_TYPE_SENSOR, THING_TYPE_DOORLOCK, THING_TYPE_CHIME, THING_TYPE_NVR_LEGACY,
            THING_TYPE_CAMERA_LEGACY, THING_TYPE_LIGHT_LEGACY, THING_TYPE_SENSOR_LEGACY, THING_TYPE_DOORLOCK_LEGACY,
            THING_TYPE_CHIME_LEGACY);
    private final TranslationService translationService;
    private final ThingTypeRegistry thingTypeRegistry;
    private UnifiMediaService media;

    @Activate
    public UnifiProtectHandlerFactory(@Reference UnifiMediaService media,
            @Reference TranslationService translationService, @Reference ThingTypeRegistry thingTypeRegistry) {
        this.media = media;
        this.translationService = translationService;
        this.thingTypeRegistry = thingTypeRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        // Dispatch on the thing-type id so one handler serves both the canonical unifi:*
        // and the legacy unifiprotect:* binding IDs.
        switch (thing.getThingTypeUID().getId()) {
            case "nvr":
                return new UnifiProtectNVRHandler(thing, thingTypeRegistry);
            case "camera":
                return new UnifiProtectCameraHandler(thing, media, translationService, thingTypeRegistry);
            case "light":
                return new UnifiProtectLightHandler(thing, thingTypeRegistry);
            case "sensor":
                return new UnifiProtectSensorHandler(thing, thingTypeRegistry);
            case "doorlock":
                return new UnifiProtectDoorlockHandler(thing, thingTypeRegistry);
            case "chime":
                return new UnifiProtectChimeHandler(thing, thingTypeRegistry);
            default:
                return null;
        }
    }
}
