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
package org.openhab.binding.bondhome.internal;

import static org.openhab.binding.bondhome.internal.BondHomeBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bondhome.internal.handler.BondBridgeHandler;
import org.openhab.binding.bondhome.internal.handler.BondDeviceHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BondHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bondhome", service = ThingHandlerFactory.class)
public class BondHomeHandlerFactory extends BaseThingHandlerFactory {
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final HttpClientFactory httpClientFactory;

    @Activate
    public BondHomeHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_BRIDGE_TYPES.contains(thingTypeUID) || SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BOND_BRIDGE.equals(thingTypeUID)) {
            return new BondBridgeHandler((Bridge) thing, httpClientFactory);
        } else if (SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new BondDeviceHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof BondBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }
}
