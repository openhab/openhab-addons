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
package org.openhab.binding.dreamscreen.internal;

import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.THING_DREAMSCREEN;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link DreamScreenHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.dreamscreen", service = ThingHandlerFactory.class)
public class DreamScreenHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_DREAMSCREEN);

    private final DreamScreenDatagramServer server = new DreamScreenDatagramServer();
    private final DreamScreenDynamicStateDescriptionProvider descriptionProvider;

    @Activate
    public DreamScreenHandlerFactory(@Reference DreamScreenDynamicStateDescriptionProvider provider,
            @Reference NetworkAddressService networkAddressService) {
        this.descriptionProvider = provider;
        server.setHostAddress(networkAddressService.getPrimaryIpv4HostAddress());
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        server.shutdown();
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_DREAMSCREEN.equals(thingTypeUID)) {
            return new DreamScreenHandler(thing, server, descriptionProvider);
        }

        return null;
    }
}
