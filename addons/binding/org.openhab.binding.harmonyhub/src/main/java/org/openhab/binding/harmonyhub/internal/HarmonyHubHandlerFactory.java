/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.harmonyhub.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.ChannelGroupType;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.harmonyhub.HarmonyHubBindingConstants;
import org.openhab.binding.harmonyhub.handler.HarmonyDeviceHandler;
import org.openhab.binding.harmonyhub.handler.HarmonyHubHandler;
import org.openhab.binding.harmonyhub.internal.discovery.HarmonyDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Sets;

/**
 * The {@link HarmonyHubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class,
        ChannelTypeProvider.class }, immediate = true, configurationPid = "binding.harmonyhub")
public class HarmonyHubHandlerFactory extends BaseThingHandlerFactory implements ChannelTypeProvider {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .union(HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS, HarmonyDeviceHandler.SUPPORTED_THING_TYPES_UIDS);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private List<ChannelType> channelTypes = new CopyOnWriteArrayList<ChannelType>();
    private List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<ChannelGroupType>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE)) {
            HarmonyHubHandler harmonyHubHandler = new HarmonyHubHandler((Bridge) thing, this);
            registerHarmonyDeviceDiscoveryService(harmonyHubHandler);
            return harmonyHubHandler;
        }

        if (thingTypeUID.equals(HarmonyHubBindingConstants.HARMONY_DEVICE_THING_TYPE)) {
            return new HarmonyDeviceHandler(thing, this);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HarmonyHubHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    /**
     * Adds HarmonyHubHandler to the discovery service to find Harmony Devices
     *
     * @param harmonyHubHandler
     */
    private synchronized void registerHarmonyDeviceDiscoveryService(HarmonyHubHandler harmonyHubHandler) {
        HarmonyDeviceDiscoveryService discoveryService = new HarmonyDeviceDiscoveryService(harmonyHubHandler);
        this.discoveryServiceRegs.put(harmonyHubHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(Locale locale) {
        return channelTypes;
    }

    @Override
    public ChannelType getChannelType(ChannelTypeUID channelTypeUID, Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
    }

    @Override
    public ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID, Locale locale) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                return channelGroupType;
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(Locale locale) {
        return channelGroupTypes;
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public void removeChannelType(ChannelType type) {
        channelTypes.remove(type);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        List<ChannelType> removes = new ArrayList<ChannelType>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(uid.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }

}
