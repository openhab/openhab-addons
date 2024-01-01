/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.somfymylink.internal;

import static org.openhab.binding.somfymylink.internal.SomfyMyLinkBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.somfymylink.internal.handler.SomfyMyLinkBridgeHandler;
import org.openhab.binding.somfymylink.internal.handler.SomfyMyLinkStateDescriptionOptionsProvider;
import org.openhab.binding.somfymylink.internal.handler.SomfySceneHandler;
import org.openhab.binding.somfymylink.internal.handler.SomfyShadeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Chris Johnson - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.somfymylink", service = ThingHandlerFactory.class)
public class SomfyMyLinkHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_MYLINK, THING_TYPE_SHADE, THING_TYPE_SCENE));

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_SHADE, THING_TYPE_SCENE));

    private @Nullable SomfyMyLinkStateDescriptionOptionsProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_MYLINK)) {
            return new SomfyMyLinkBridgeHandler((Bridge) thing, stateDescriptionProvider);
        }
        if (THING_TYPE_SHADE.equals(thingTypeUID)) {
            return new SomfyShadeHandler(thing);
        }
        if (THING_TYPE_SCENE.equals(thingTypeUID)) {
            return new SomfySceneHandler(thing);
        }

        return null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(SomfyMyLinkStateDescriptionOptionsProvider provider) {
        this.stateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(SomfyMyLinkStateDescriptionOptionsProvider provider) {
        this.stateDescriptionProvider = null;
    }
}
