/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.handler;

import static org.openhab.binding.homematic.HomematicBindingConstants.*;

import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomematicThingHandlerFactory} is responsible for creating thing and bridge handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.homematic", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class HomematicThingHandlerFactory extends BaseThingHandlerFactory {
    private HomematicTypeGenerator typeGenerator;
    private NetworkAddressService networkAddressService;

    @Reference
    protected void setTypeGenerator(HomematicTypeGenerator typeGenerator) {
        this.typeGenerator = typeGenerator;
    }

    protected void unsetTypeGenerator(HomematicTypeGenerator typeGenerator) {
        this.typeGenerator = null;
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_BRIDGE.equals(thing.getThingTypeUID())) {
            return new HomematicBridgeHandler((Bridge) thing, typeGenerator,
                    networkAddressService.getPrimaryIpv4HostAddress());
        } else {
            return new HomematicThingHandler(thing);
        }
    }

}
