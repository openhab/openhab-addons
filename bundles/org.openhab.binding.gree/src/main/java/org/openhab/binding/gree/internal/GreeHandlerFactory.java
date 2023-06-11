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
package org.openhab.binding.gree.internal;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gree.internal.discovery.GreeDeviceFinder;
import org.openhab.binding.gree.internal.handler.GreeHandler;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link GreeHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
@Component(configurationPid = "binding." + BINDING_ID, service = ThingHandlerFactory.class)
public class GreeHandlerFactory extends BaseThingHandlerFactory {
    private final GreeTranslationProvider messages;
    private final GreeDeviceFinder deviceFinder;

    @Activate
    public GreeHandlerFactory(@Reference NetworkAddressService networkAddressService,
            @Reference GreeDeviceFinder deviceFinder, @Reference GreeTranslationProvider translationProvider,
            ComponentContext componentContext, Map<String, Object> configProperties) {
        super.activate(componentContext);
        this.messages = translationProvider;
        this.deviceFinder = deviceFinder;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_GREEAIRCON.equals(thing.getThingTypeUID())) {
            return new GreeHandler(thing, messages, deviceFinder);
        }
        return null;
    }
}
