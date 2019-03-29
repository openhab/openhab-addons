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
package org.openhab.binding.net.internal;

import static org.openhab.binding.net.internal.NetBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.openhab.binding.net.internal.handler.DataHandler;
import org.openhab.binding.net.internal.handler.HttpServerHandler;
import org.openhab.binding.net.internal.handler.TcpServerHandler;
import org.openhab.binding.net.internal.handler.UdpServerHandler;
import org.openhab.binding.net.internal.transformation.TransformationServiceProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.common.collect.Sets;

/**
 * The {@link NetBindingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.net")
@NonNullByDefault
public class NetBindingHandlerFactory extends BaseThingHandlerFactory implements TransformationServiceProvider {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(NetBindingConstants.SUPPORTED_THING_TYPES,
            NetBindingConstants.SUPPORTED_BRIDGE_TYPES);

    @Activate
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Deactivate
    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(BRIDGE_UDP_SERVER)) {
            return new UdpServerHandler((Bridge) thing);
        } else if (thingTypeUID.equals(BRIDGE_TCP_SERVER)) {
            return new TcpServerHandler((Bridge) thing);
        } else if (thingTypeUID.equals(BRIDGE_HTTP_SERVER)) {
            return new HttpServerHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_DATA_HANDLER)) {
            return new DataHandler(thing, this);
        }

        return null;
    }

    @Override
    public @Nullable TransformationService getTransformationService(String type) {
        return TransformationHelper.getTransformationService(bundleContext, type);
    }
}
