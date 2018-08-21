/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal;

import static org.openhab.binding.ihc2.Ihc2BindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.ihc2.handler.Ihc2ContactThingHandler;
import org.openhab.binding.ihc2.handler.Ihc2ControllerBridgeHandler;
import org.openhab.binding.ihc2.handler.Ihc2DateTimeThingHandler;
import org.openhab.binding.ihc2.handler.Ihc2DimmerThingHandler;
import org.openhab.binding.ihc2.handler.Ihc2EnumThingHandler;
import org.openhab.binding.ihc2.handler.Ihc2MultiChannelThingHandler;
import org.openhab.binding.ihc2.handler.Ihc2NumberThingHandler;
import org.openhab.binding.ihc2.handler.Ihc2SwitchThingHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link Ihc2HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.ihc2")
@NonNullByDefault
public class Ihc2HandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(Ihc2HandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_IHCCONTROLLER,
            THING_TYPE_SWITCH, THING_TYPE_CONTACT, THING_TYPE_NUMBER, THING_TYPE_DATETIME, THING_TYPE_DIMMER,
            THING_TYPE_STRING, THING_TYPE_MULTI_CHANNEL);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.debug("supportsThingType(): {}", thingTypeUID.getAsString());
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    @Nullable
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, @Nullable ThingUID thingUID,
            @Nullable ThingUID bridgeUID) {
        logger.debug("createThing(): {}", thingTypeUID.getAsString());

        // Hard code the Controller Thing to ensure there is only one THING_TYPE_IHCCONTROLLER
        if (thingTypeUID.equals(THING_TYPE_IHCCONTROLLER)) {
            return super.createThing(thingTypeUID, configuration, THE_IHC_CONTROLLER_UID, bridgeUID);
        }

        // if (thingTypeUID.equals(THING_TYPE_MULTI_CHANNEL)) {
        // List<Channel> newChannels = new ArrayList<Channel>();
        // Map<String, Object> properties = configuration.getProperties();
        //
        // String numChannels = properties.get("numberOfChannels").toString();
        //
        // for (int i = 0; i < Integer.valueOf(numChannels); i++) {
        // String resourceIdKey = String.format("channel%dResourceId", i + 1);
        // String resourceTypeKey = String.format("channel%dResourceType", i + 1);
        // String readonlyKey = String.format("channel%dReadonly", i + 1);
        // String pulsTimeKey = String.format("channel%dPulseTime", i + 1);
        //
        // String resourceId = properties.get(resourceIdKey).toString();
        // String resourceType = properties.get(resourceTypeKey).toString();
        // String readonly = properties.get(readonlyKey).toString();
        // String pulsTime = properties.get(pulsTimeKey).toString();
        //
        // ChannelTypeUID channelTypeUID = null;
        // String channelValueName = "";
        // String channelLabel = "";
        // String channelDescription = "";
        // String acceptedItemType = "";
        //
        // Map<String, String> channelProperties = new HashMap<String, String>();
        //
        // channelProperties.put("channelNumber", String.valueOf(i + 1));
        //
        // switch (resourceType) {
        // case CHANNEL_TYPE_PERCENT:
        // channelTypeUID = new ChannelTypeUID(thingUID.getBindingId(), CHANNEL_TYPE_PERCENT);
        // channelValueName = CHANNEL_PERCENT;
        // channelLabel = "Light Level";
        // acceptedItemType = "Level";
        // // channelProperties.put("resourceId", resourceId);
        // // channelProperties.put("readonly", readonly);
        // break;
        // case CHANNEL_TYPE_SWITCH:
        // channelTypeUID = new ChannelTypeUID(thingUID.getBindingId(), CHANNEL_TYPE_SWITCH);
        // channelValueName = CHANNEL_SWITCH;
        // channelLabel = "Switch State";
        // acceptedItemType = "Switch";
        // channelDescription = "On/Off status of the switch";
        // // channelProperties.put("resourceId", resourceId);
        // // channelProperties.put("readonly", readonly);
        // // channelProperties.put("pulseTime", pulsTime);
        // break;
        // case CHANNEL_TYPE_CONTACT:
        // channelTypeUID = new ChannelTypeUID(thingUID.getBindingId(), CHANNEL_TYPE_CONTACT);
        // channelValueName = CHANNEL_CONTACT;
        // channelLabel = "Contact State";
        // acceptedItemType = "Contact";
        // channelDescription = "Open/Close status of the contact";
        // // channelProperties.put("resourceId", resourceId);
        // // channelProperties.put("readonly", readonly);
        // break;
        // case CHANNEL_TYPE_DATETIME:
        // channelTypeUID = new ChannelTypeUID(thingUID.getBindingId(), CHANNEL_TYPE_DATETIME);
        // channelValueName = CHANNEL_DATETIME;
        // channelLabel = "Date Time";
        // acceptedItemType = "DateTime";
        // channelDescription = "Time and Date";
        // // channelProperties.put("resourceId", resourceId);
        // // channelProperties.put("readonly", readonly);
        // break;
        // case CHANNEL_TYPE_NUMBER:
        // channelTypeUID = new ChannelTypeUID(thingUID.getBindingId(), CHANNEL_TYPE_NUMBER);
        // channelValueName = CHANNEL_NUMBER;
        // channelLabel = "Number";
        // acceptedItemType = "Number";
        // channelDescription = "Number";
        // // channelProperties.put("resourceId", resourceId);
        // // channelProperties.put("readonly", readonly);
        // break;
        // case CHANNEL_TYPE_STRING:
        // channelTypeUID = new ChannelTypeUID(thingUID.getBindingId(), CHANNEL_TYPE_STRING);
        // channelValueName = CHANNEL_STRING;
        // channelLabel = "String";
        // acceptedItemType = "String";
        // channelDescription = "String Value";
        // // channelProperties.put("resourceId", resourceId);
        // // channelProperties.put("readonly", readonly);
        // break;
        // }
        //
        // ChannelBuilder channelBuilder = ChannelBuilder.create(
        // new ChannelUID(thingUID, String.format("channel%d", i + 1), channelValueName),
        // acceptedItemType);
        // channelBuilder.withType(channelTypeUID);
        // channelBuilder.withProperties(channelProperties);
        // channelBuilder.withLabel(channelLabel);
        // channelBuilder.withDescription(channelDescription);
        // newChannels.add(channelBuilder.build());
        // }
        //
        // if (thingUID != null) {
        // ThingBuilder thingBuilder = ThingBuilder.create(thingTypeUID, thingUID);
        // thingBuilder.withChannels(newChannels);
        // thingBuilder.withBridge(bridgeUID);
        // thingBuilder.withConfiguration(configuration);
        //
        // return thingBuilder.build();
        // }
        // return null;
        // }
        return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.info("createHandler() for {}", thing.getUID().getAsString());
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Hmm - these thing handlers turned out to be pretty identical
        // perhaps they should be merged into one thing handler
        if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new Ihc2SwitchThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_DIMMER)) {
            return new Ihc2DimmerThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_CONTACT)) {
            return new Ihc2ContactThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_STRING)) {
            return new Ihc2EnumThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_DATETIME)) {
            return new Ihc2DateTimeThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_NUMBER)) {
            return new Ihc2NumberThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_MULTI_CHANNEL)) {
            return new Ihc2MultiChannelThingHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_IHCCONTROLLER)) {
            String s = thing.getClass().toString();
            if (s.endsWith("BridgeImpl")) { // Sometimes I get a ThingImpl and the cast to Bridge fails. Duno why. npe.
                Bridge b = (Bridge) thing;
                return new Ihc2ControllerBridgeHandler(b);
            }
        }

        return null;
    }
}
