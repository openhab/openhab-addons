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
package org.openhab.binding.revogismartstripcontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.revogismartstripcontrol.internal.api.StatusService;
import org.openhab.binding.revogismartstripcontrol.internal.api.SwitchService;
import org.openhab.binding.revogismartstripcontrol.internal.udp.DatagramSocketWrapper;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static org.openhab.binding.revogismartstripcontrol.internal.RevogiSmartStripControlBindingConstants.SMART_STRIP_THING_TYPE;

/**
 * The {@link RevogiSmartStripControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.revogismartstripcontrol", service = ThingHandlerFactory.class)
public class RevogiSmartStripControlHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(SMART_STRIP_THING_TYPE);
    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SMART_STRIP_THING_TYPE.equals(thingTypeUID)) {
            UdpSenderService udpSenderService = new UdpSenderService(new DatagramSocketWrapper());
            StatusService statusService = new StatusService(udpSenderService);
            SwitchService switchService = new SwitchService(udpSenderService);
            RevogiSmartStripControlHandler revogiSmartStripControlHandler = new RevogiSmartStripControlHandler(thing, statusService, switchService);
            registerSmartStripDiscoveryService(revogiSmartStripControlHandler, udpSenderService);
            return revogiSmartStripControlHandler;
        }

        return null;
    }

    private void registerSmartStripDiscoveryService(RevogiSmartStripControlHandler revogiSmartStripControlHandler, UdpSenderService udpSenderService) {
        RevogiSmartStripDiscoveryService discoveryService = new RevogiSmartStripDiscoveryService(udpSenderService, revogiSmartStripControlHandler);
        this.discoveryServiceRegs.put(revogiSmartStripControlHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

    }
}
