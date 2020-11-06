/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.panasonictv.internal;

import static org.openhab.binding.panasonictv.internal.PanasonicTvBindingConstants.THING_TYPE_PANASONICTV;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.openhab.binding.panasonictv.internal.event.PanasonicEventListenerService;
import org.openhab.binding.panasonictv.internal.handler.PanasonicTvHandler;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link PanasonicTvHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.panasonictv")
public class PanasonicTvHandlerFactory extends BaseThingHandlerFactory {
    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(THING_TYPE_PANASONICTV);

    private final UpnpIOService upnpIOService;
    private final UpnpService upnpService;
    private final PanasonicEventListenerService panasonicEventListenerService;

    @Activate
    public PanasonicTvHandlerFactory(@Reference UpnpIOService upnpIOService, @Reference UpnpService upnpService,
            @Reference PanasonicEventListenerService panasonicEventListenerService) {
        this.upnpIOService = upnpIOService;
        this.upnpService = upnpService;
        this.panasonicEventListenerService = panasonicEventListenerService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_PANASONICTV)) {
            return new PanasonicTvHandler(thing, upnpIOService, upnpService, panasonicEventListenerService);
        }

        return null;
    }
}
