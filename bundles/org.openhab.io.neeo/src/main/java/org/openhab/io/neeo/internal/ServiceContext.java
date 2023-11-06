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
package org.openhab.io.neeo.internal;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.addon.AddonInfoRegistry;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.io.transport.mdns.MDNSClient;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.util.StringUtils;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;

/**
 * Provides the services for a given context
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class ServiceContext {

    /** The component context provided by openHAB during # */
    private final ComponentContext componentContext;

    /** The http service. */
    private final HttpService httpService;

    /** The item registry. */
    private final ItemRegistry itemRegistry;

    /** The binding info registry. */
    private final AddonInfoRegistry addonInfoRegistry;

    /** The thing registry. */
    private final ThingRegistry thingRegistry;

    /** The thing type registry. */
    private final ThingTypeRegistry thingTypeRegistry;

    /** The item channel link registry. */
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    /** The channel type registry. */
    private final ChannelTypeRegistry channelTypeRegistry;

    /** The mdns client. */
    private final MDNSClient mdnsClient;

    /** The event publisher. */
    private final EventPublisher eventPublisher;

    /** The definitions. */
    private final NeeoDeviceDefinitions definitions;

    /** The network address service. */
    private final NetworkAddressService networkAddressService;

    /**
     * Creates teh service context from the variable services
     *
     * @param componentContext a non-null component context
     * @param httpService a non-null http service
     * @param itemRegistry a non-null item registry
     * @param addonInfoRegistry a non-null addon information registry
     * @param thingRegistry a non-null thing registry
     * @param thingTypeRegistry a non-null thing type registry
     * @param itemChannelLinkRegistry a non-null item channel link registry
     * @param channelTypeRegistry a non-null channel type registry
     * @param mdnsClient a non-null mdns client
     * @param eventPublisher a non-null event publisher
     * @param networkAddressService a non-null network address service
     */
    public ServiceContext(ComponentContext componentContext, HttpService httpService, ItemRegistry itemRegistry,
            AddonInfoRegistry addonInfoRegistry, ThingRegistry thingRegistry, ThingTypeRegistry thingTypeRegistry,
            ItemChannelLinkRegistry itemChannelLinkRegistry, ChannelTypeRegistry channelTypeRegistry,
            MDNSClient mdnsClient, EventPublisher eventPublisher, NetworkAddressService networkAddressService) {
        Objects.requireNonNull(componentContext, "componentContext cannot be null");
        Objects.requireNonNull(httpService, "httpService cannot be null");
        Objects.requireNonNull(itemRegistry, "itemRegistry cannot be null");
        Objects.requireNonNull(addonInfoRegistry, "addonInfoRegistry cannot be null");
        Objects.requireNonNull(thingRegistry, "thingRegistry cannot be null");
        Objects.requireNonNull(thingTypeRegistry, "thingTypeRegistry cannot be null");
        Objects.requireNonNull(itemChannelLinkRegistry, "itemChannelLinkRegistry cannot be null");
        Objects.requireNonNull(channelTypeRegistry, "channelTypeRegistry cannot be null");
        Objects.requireNonNull(mdnsClient, "mdnsClient cannot be null");
        Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
        Objects.requireNonNull(networkAddressService, "networkAddressService cannot be null");

        this.componentContext = componentContext;
        this.httpService = httpService;
        this.itemRegistry = itemRegistry;
        this.addonInfoRegistry = addonInfoRegistry;
        this.thingRegistry = thingRegistry;
        this.thingTypeRegistry = thingTypeRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.channelTypeRegistry = channelTypeRegistry;
        this.mdnsClient = mdnsClient;
        this.eventPublisher = eventPublisher;
        this.networkAddressService = networkAddressService;
        this.definitions = new NeeoDeviceDefinitions(this);
    }

    /**
     * Gets the definitions.
     *
     * @return the definitions
     */
    public NeeoDeviceDefinitions getDefinitions() {
        return definitions;
    }

    /**
     * Gets the component context.
     *
     * @return the component context
     */
    public ComponentContext getComponentContext() {
        return componentContext;
    }

    /**
     * Gets the http service.
     *
     * @return the http service
     */
    public HttpService getHttpService() {
        return httpService;
    }

    /**
     * Gets the item registry.
     *
     * @return the item registry
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Gets the binding info registry.
     *
     * @return the binding info registry
     */
    public AddonInfoRegistry getAddonInfoRegistry() {
        return addonInfoRegistry;
    }

    /**
     * Gets the thing registry.
     *
     * @return the thing registry
     */
    public ThingRegistry getThingRegistry() {
        return thingRegistry;
    }

    /**
     * Gets the thing type registry.
     *
     * @return the thing type registry
     */
    public ThingTypeRegistry getThingTypeRegistry() {
        return thingTypeRegistry;
    }

    /**
     * Gets the item channel link registry.
     *
     * @return the item channel link registry
     */
    public ItemChannelLinkRegistry getItemChannelLinkRegistry() {
        return itemChannelLinkRegistry;
    }

    /**
     * Gets the channel type registry.
     *
     * @return the channel type registry
     */
    public ChannelTypeRegistry getChannelTypeRegistry() {
        return channelTypeRegistry;
    }

    /**
     * Gets the mdns client.
     *
     * @return the mdns client
     */
    public MDNSClient getMdnsClient() {
        return mdnsClient;
    }

    /**
     * Gets the event publisher.
     *
     * @return the event publisher
     */
    public EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    /**
     * Gets the network address service
     *
     * @return the network address service
     */
    public NetworkAddressService getNetworkAddressService() {
        return networkAddressService;
    }

    /**
     * Helper utility to generate a random {@link NeeoThingUID}. Will try to generate a non-used random 8 digit alpha
     * numeric UID and if it fails after 100 attempts, will return one with a random UUID as the ID
     *
     * @param thingType a non-null thingType
     * @return a non-null {@link NeeoThingUID}
     */
    public NeeoThingUID generate(String thingType) {
        NeeoUtil.requireNotEmpty(thingType, "thingType cannot be null");

        for (int i = 0; i < 100; i++) {
            final String id = StringUtils.getRandomAlphanumeric(8);
            final NeeoThingUID uid = new NeeoThingUID(thingType, id);
            if (getThingRegistry().get(uid.asThingUID()) == null) {
                return uid;
            }
        }

        return new NeeoThingUID(thingType, UUID.randomUUID().toString());
    }

    /**
     * Helper method to determine if all things should be exposed by default
     *
     * @return true to expose all things, false otherwise
     */
    public boolean isExposeAllThings() {
        final Object cfgExposeAll = getComponentContext().getProperties().get(NeeoConstants.CFG_EXPOSE_ALL);
        return cfgExposeAll == null ? false : Boolean.parseBoolean(cfgExposeAll.toString());
    }

    /**
     * Helper method to determine if things from the NEEO binding should be exposed
     *
     * @return true to expose all things, false otherwise
     */
    public boolean isExposeNeeoBinding() {
        final Object cfgExpose = getComponentContext().getProperties().get(NeeoConstants.CFG_EXPOSENEEOBINDING);
        return cfgExpose == null ? true : Boolean.parseBoolean(cfgExpose.toString());
    }
}
