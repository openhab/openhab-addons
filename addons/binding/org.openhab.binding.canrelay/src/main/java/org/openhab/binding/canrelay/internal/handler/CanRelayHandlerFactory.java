/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.handler;

import static org.openhab.binding.canrelay.internal.CanRelayBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.canrelay.internal.protocol.CanRelayAccess;
import org.openhab.binding.canrelay.internal.runtime.Runtime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CanRelayHandlerFactory} is responsible for creating bridge and thing
 * handlers.
 *
 * @author Lubos Housa - Initial contribution
 */
@Component(configurationPid = "binding.canrelay", service = ThingHandlerFactory.class)
public class CanRelayHandlerFactory extends BaseThingHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(CanRelayHandlerFactory.class);

    private CanRelayAccess canRelayAccess;
    private Runtime runtime;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HW_BRIDGE.equals(thingTypeUID)) {
            logger.debug("Creating bridge type handler for CanRelay...");
            return new CanRelayBridgeHandler((Bridge) thing, canRelayAccess, runtime);
        }

        if (THING_TYPE_LIGHT.equals(thingTypeUID)) {
            logger.debug("Creating light type handler for CanRelsay...");
            return new CanRelayLightSwitchHandler(thing);
        }
        return null;
    }

    @Reference
    public void setCanRelayAccess(CanRelayAccess canRelayAccess) {
        this.canRelayAccess = canRelayAccess;
    }

    public void unsetCanRelayAccess(CanRelayAccess canRelayAccess) {
        this.canRelayAccess = null;
    }

    @Reference
    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    public void unsetRuntime(Runtime runtime) {
        this.runtime = null;
    }
}
