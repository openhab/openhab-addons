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
package org.openhab.binding.hue.internal.factory;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.HueBindingConstants;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
import org.openhab.binding.hue.internal.handler.Clip2StateDescriptionProvider;
import org.openhab.binding.hue.internal.handler.Clip2ThingHandler;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.binding.hue.internal.handler.HueGroupHandler;
import org.openhab.binding.hue.internal.handler.HueLightHandler;
import org.openhab.binding.hue.internal.handler.HueStateDescriptionProvider;
import org.openhab.binding.hue.internal.handler.sensors.ClipHandler;
import org.openhab.binding.hue.internal.handler.sensors.DimmerSwitchHandler;
import org.openhab.binding.hue.internal.handler.sensors.GeofencePresenceHandler;
import org.openhab.binding.hue.internal.handler.sensors.LightLevelHandler;
import org.openhab.binding.hue.internal.handler.sensors.PresenceHandler;
import org.openhab.binding.hue.internal.handler.sensors.TapSwitchHandler;
import org.openhab.binding.hue.internal.handler.sensors.TemperatureHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The factory for all varieties of Hue thing handlers.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThingType method
 * @author Andre Fuechsel - implemented to use one discovery service per bridge
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 * @author Laurent Garnier - Added support for groups
 * @author Andrew Fiddian-Green - Added support for CLIP 2 things
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hue")
public class HueThingHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(Clip2BridgeHandler.SUPPORTED_THING_TYPES.stream(), Clip2ThingHandler.SUPPORTED_THING_TYPES.stream(),
                    HueBridgeHandler.SUPPORTED_THING_TYPES.stream(), HueLightHandler.SUPPORTED_THING_TYPES.stream(),
                    DimmerSwitchHandler.SUPPORTED_THING_TYPES.stream(), TapSwitchHandler.SUPPORTED_THING_TYPES.stream(),
                    PresenceHandler.SUPPORTED_THING_TYPES.stream(),
                    GeofencePresenceHandler.SUPPORTED_THING_TYPES.stream(),
                    TemperatureHandler.SUPPORTED_THING_TYPES.stream(), LightLevelHandler.SUPPORTED_THING_TYPES.stream(),
                    ClipHandler.SUPPORTED_THING_TYPES.stream(), HueGroupHandler.SUPPORTED_THING_TYPES.stream())
            .flatMap(i -> i).collect(Collectors.toUnmodifiableSet());

    private final HttpClientFactory httpClientFactory;
    private final HueStateDescriptionProvider stateDescriptionProvider;
    private final Clip2StateDescriptionProvider clip2StateDescriptionProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final ThingRegistry thingRegistry;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Activate
    public HueThingHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference HueStateDescriptionProvider stateDescriptionProvider,
            final @Reference Clip2StateDescriptionProvider clip2StateDescriptionProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider,
            final @Reference ThingRegistry thingRegistry,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.httpClientFactory = httpClientFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.clip2StateDescriptionProvider = clip2StateDescriptionProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.thingRegistry = thingRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (HueBindingConstants.THING_TYPE_BRIDGE_API2.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (Clip2ThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID clip2ThingUID = getClip2ThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, clip2ThingUID, bridgeUID);
        } else if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueLightUID = getLightUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueLightUID, bridgeUID);
        } else if (DimmerSwitchHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || TapSwitchHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || PresenceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || GeofencePresenceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || TemperatureHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || LightLevelHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                || ClipHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueSensorUID = getSensorUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueSensorUID, bridgeUID);
        } else if (HueGroupHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID hueGroupUID = getGroupUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, hueGroupUID, bridgeUID);
        }

        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the Hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private ThingUID getClip2ThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        return thingUID != null ? thingUID
                : getThingUID(thingTypeUID, configuration.get(PROPERTY_RESOURCE_ID).toString(), bridgeUID);
    }

    private ThingUID getLightUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            return getThingUID(thingTypeUID, configuration.get(LIGHT_ID).toString(), bridgeUID);
        }
    }

    private ThingUID getSensorUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            return getThingUID(thingTypeUID, configuration.get(SENSOR_ID).toString(), bridgeUID);
        }
    }

    private ThingUID getGroupUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID, Configuration configuration,
            @Nullable ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        } else {
            return getThingUID(thingTypeUID, configuration.get(GROUP_ID).toString(), bridgeUID);
        }
    }

    private ThingUID getThingUID(ThingTypeUID thingTypeUID, String id, @Nullable ThingUID bridgeUID) {
        if (bridgeUID != null) {
            return new ThingUID(thingTypeUID, id, bridgeUID.getId());
        } else {
            return new ThingUID(thingTypeUID, id);
        }
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (HueBindingConstants.THING_TYPE_BRIDGE_API2.equals(thingTypeUID)) {
            return new Clip2BridgeHandler((Bridge) thing, httpClientFactory, thingRegistry, localeProvider,
                    i18nProvider);
        } else if (Clip2ThingHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new Clip2ThingHandler(thing, clip2StateDescriptionProvider, thingRegistry, itemChannelLinkRegistry);
        } else if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new HueBridgeHandler((Bridge) thing, httpClientFactory.getCommonHttpClient(),
                    stateDescriptionProvider, i18nProvider, localeProvider);
        } else if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new HueLightHandler(thing, stateDescriptionProvider);
        } else if (DimmerSwitchHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new DimmerSwitchHandler(thing);
        } else if (TapSwitchHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new TapSwitchHandler(thing);
        } else if (PresenceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new PresenceHandler(thing);
        } else if (GeofencePresenceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new GeofencePresenceHandler(thing);
        } else if (TemperatureHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new TemperatureHandler(thing);
        } else if (LightLevelHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new LightLevelHandler(thing);
        } else if (ClipHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new ClipHandler(thing);
        } else if (HueGroupHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new HueGroupHandler(thing, stateDescriptionProvider);
        } else {
            return null;
        }
    }
}
