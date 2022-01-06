/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link HueThingHandlerFactory} is a factory for {@link HueBridgeHandler}s.
 *
 * @author Dennis Nobel - Initial contribution of hue binding
 * @author Kai Kreuzer - added supportsThingType method
 * @author Andre Fuechsel - implemented to use one discovery service per bridge
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 * @author Laurent Garnier - Added support for groups
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hue")
public class HueThingHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.unmodifiableSet(Stream
            .of(HueBridgeHandler.SUPPORTED_THING_TYPES.stream(), HueLightHandler.SUPPORTED_THING_TYPES.stream(),
                    DimmerSwitchHandler.SUPPORTED_THING_TYPES.stream(), TapSwitchHandler.SUPPORTED_THING_TYPES.stream(),
                    PresenceHandler.SUPPORTED_THING_TYPES.stream(),
                    GeofencePresenceHandler.SUPPORTED_THING_TYPES.stream(),
                    TemperatureHandler.SUPPORTED_THING_TYPES.stream(), LightLevelHandler.SUPPORTED_THING_TYPES.stream(),
                    ClipHandler.SUPPORTED_THING_TYPES.stream(), HueGroupHandler.SUPPORTED_THING_TYPES.stream())
            .flatMap(i -> i).collect(Collectors.toSet()));

    private final HueStateDescriptionProvider stateDescriptionProvider;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public HueThingHandlerFactory(final @Reference HueStateDescriptionProvider stateDescriptionProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
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

        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the hue binding.");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
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
        if (HueBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueBridgeHandler((Bridge) thing, stateDescriptionProvider, i18nProvider, localeProvider);
        } else if (HueLightHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueLightHandler(thing, stateDescriptionProvider);
        } else if (DimmerSwitchHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new DimmerSwitchHandler(thing);
        } else if (TapSwitchHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new TapSwitchHandler(thing);
        } else if (PresenceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new PresenceHandler(thing);
        } else if (GeofencePresenceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new GeofencePresenceHandler(thing);
        } else if (TemperatureHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new TemperatureHandler(thing);
        } else if (LightLevelHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new LightLevelHandler(thing);
        } else if (ClipHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new ClipHandler(thing);
        } else if (HueGroupHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new HueGroupHandler(thing, stateDescriptionProvider);
        } else {
            return null;
        }
    }
}
