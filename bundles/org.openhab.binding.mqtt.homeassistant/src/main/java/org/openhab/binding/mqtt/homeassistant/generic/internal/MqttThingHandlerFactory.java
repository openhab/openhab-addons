/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.generic.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantPythonBridge;
import org.openhab.binding.mqtt.homeassistant.internal.HomeAssistantStateDescriptionProvider;
import org.openhab.binding.mqtt.homeassistant.internal.handler.HomeAssistantThingHandler;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link MqttThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class MqttThingHandlerFactory extends BaseThingHandlerFactory {
    private final MqttChannelTypeProvider typeProvider;
    private final MqttChannelStateDescriptionProvider stateDescriptionProvider;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final UnitProvider unitProvider;
    private final Gson gson;
    private final HomeAssistantPythonBridge python;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(MqttBindingConstants.HOMEASSISTANT_MQTT_THING).collect(Collectors.toSet());

    @Activate
    public MqttThingHandlerFactory(final @Reference MqttChannelTypeProvider typeProvider,
            final @Reference HomeAssistantStateDescriptionProvider stateDescriptionProvider,
            final @Reference ChannelTypeRegistry channelTypeRegistry, final @Reference UnitProvider unitProvider,
            final @Reference HomeAssistantPythonBridge python) {
        this.typeProvider = typeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.channelTypeRegistry = channelTypeRegistry;
        this.unitProvider = unitProvider;
        this.gson = new Gson();
        this.python = python;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID) || isHomeassistantDynamicType(thingTypeUID);
    }

    private boolean isHomeassistantDynamicType(ThingTypeUID thingTypeUID) {
        return MqttBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId())
                && thingTypeUID.getId().startsWith(MqttBindingConstants.HOMEASSISTANT_MQTT_THING.getId());
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (supportsThingType(thingTypeUID)) {
            return new HomeAssistantThingHandler(thing, this, typeProvider, stateDescriptionProvider,
                    channelTypeRegistry, gson, python, unitProvider, 10000, 2000);
        }
        return null;
    }

    public HomeAssistantPythonBridge getPython() {
        return python;
    }
}
