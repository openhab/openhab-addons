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
package org.openhab.binding.homematic.internal.handler;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homematic.internal.type.HomematicChannelTypeProvider;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomematicThingHandlerFactory} is responsible for creating thing and bridge handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.homematic")
@NonNullByDefault
public class HomematicThingHandlerFactory extends BaseThingHandlerFactory {

    private final HomematicTypeGenerator typeGenerator;
    private final HomematicChannelTypeProvider channelTypeProvider;
    private final NetworkAddressService networkAddressService;
    private final HttpClient httpClient;

    @Activate
    public HomematicThingHandlerFactory(@Reference HomematicTypeGenerator typeGenerator,
            @Reference HomematicChannelTypeProvider channelTypeProvider,
            @Reference NetworkAddressService networkAddressService, @Reference HttpClientFactory httpClientFactory) {
        this.typeGenerator = typeGenerator;
        this.channelTypeProvider = channelTypeProvider;
        this.networkAddressService = networkAddressService;
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_BRIDGE.equals(thing.getThingTypeUID())) {
            return new HomematicBridgeHandler((Bridge) thing, typeGenerator,
                    networkAddressService.getPrimaryIpv4HostAddress(), httpClient);
        } else {
            return new HomematicThingHandler(thing, channelTypeProvider);
        }
    }
}
