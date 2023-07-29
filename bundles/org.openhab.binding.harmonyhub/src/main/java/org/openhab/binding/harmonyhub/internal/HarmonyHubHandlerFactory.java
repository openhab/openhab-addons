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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.harmonyhub.internal.discovery.HarmonyDeviceDiscoveryService;
import org.openhab.binding.harmonyhub.internal.handler.HarmonyDeviceHandler;
import org.openhab.binding.harmonyhub.internal.handler.HarmonyHubHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HarmonyHubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, ChannelTypeProvider.class,
        ChannelGroupTypeProvider.class }, configurationPid = "binding.harmonyhub")
public class HarmonyHubHandlerFactory extends BaseThingHandlerFactory
        implements ChannelTypeProvider, ChannelGroupTypeProvider {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS.stream(),
                    HarmonyDeviceHandler.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final HttpClient httpClient;

    private final List<ChannelType> channelTypes = new CopyOnWriteArrayList<>();
    private final List<ChannelGroupType> channelGroupTypes = new CopyOnWriteArrayList<>();

    @Activate
    public HarmonyHubHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
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
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
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
        this.discoveryServiceRegs.put(harmonyHubHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        for (ChannelType channelType : channelTypes) {
            if (channelType.getUID().equals(channelTypeUID)) {
                return channelType;
            }
        }
        return null;
    }

    @Override
    public @Nullable ChannelGroupType getChannelGroupType(ChannelGroupTypeUID channelGroupTypeUID,
            @Nullable Locale locale) {
        for (ChannelGroupType channelGroupType : channelGroupTypes) {
            if (channelGroupType.getUID().equals(channelGroupTypeUID)) {
                return channelGroupType;
            }
        }
        return null;
    }

    @Override
    public Collection<ChannelGroupType> getChannelGroupTypes(@Nullable Locale locale) {
        return channelGroupTypes;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public void addChannelType(ChannelType type) {
        channelTypes.add(type);
    }

    public void removeChannelType(ChannelType type) {
        channelTypes.remove(type);
    }

    public void removeChannelTypesForThing(ThingUID uid) {
        List<ChannelType> removes = new ArrayList<>();
        for (ChannelType c : channelTypes) {
            if (c.getUID().getAsString().startsWith(uid.getAsString())) {
                removes.add(c);
            }
        }
        channelTypes.removeAll(removes);
    }
}
