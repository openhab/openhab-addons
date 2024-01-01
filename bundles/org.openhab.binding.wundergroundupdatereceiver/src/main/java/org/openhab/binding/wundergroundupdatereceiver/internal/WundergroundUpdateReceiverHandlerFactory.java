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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;

/**
 * The {@link WundergroundUpdateReceiverHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Demus - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wundergroundupdatereceiver", service = ThingHandlerFactory.class)
public class WundergroundUpdateReceiverHandlerFactory extends BaseThingHandlerFactory {

    private final WundergroundUpdateReceiverDiscoveryService discoveryService;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final WundergroundUpdateReceiverUnknownChannelTypeProvider channelTypeProvider;
    private final ManagedThingProvider managedThingProvider;
    private final WundergroundUpdateReceiverServlet wunderGroundUpdateReceiverServlet;

    @Activate
    public WundergroundUpdateReceiverHandlerFactory(@Reference HttpService httpService,
            @Reference WundergroundUpdateReceiverDiscoveryService discoveryService,
            @Reference WundergroundUpdateReceiverUnknownChannelTypeProvider channelTypeProvider,
            @Reference ChannelTypeRegistry channelTypeRegistry, @Reference ManagedThingProvider managedThingProvider,
            @Reference WundergroundUpdateReceiverServlet wunderGroundUpdateReceiverServlet) {
        this.discoveryService = discoveryService;
        this.channelTypeRegistry = channelTypeRegistry;
        this.channelTypeProvider = channelTypeProvider;
        this.managedThingProvider = managedThingProvider;
        this.wunderGroundUpdateReceiverServlet = wunderGroundUpdateReceiverServlet;
        this.discoveryService.servletControls = wunderGroundUpdateReceiverServlet;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return WundergroundUpdateReceiverBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_UPDATE_RECEIVER.equals(thingTypeUID)) {
            return new WundergroundUpdateReceiverHandler(thing, this.wunderGroundUpdateReceiverServlet,
                    this.discoveryService, this.channelTypeProvider, this.channelTypeRegistry,
                    this.managedThingProvider);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        this.wunderGroundUpdateReceiverServlet.disable();
        super.deactivate(componentContext);
    }
}
