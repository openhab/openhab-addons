/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fileregexparser.internal;

import static org.openhab.binding.fileregexparser.FileRegexParserBindingConstants.THING_TYPE_FILEREGEXPARSER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.fileregexparser.handler.FileRegexParserHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FileRegexParserHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author meju25 - Initial contribution
 */
public class FileRegexParserHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(FileRegexParserHandlerFactory.class);
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(THING_TYPE_FILEREGEXPARSER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_FILEREGEXPARSER)) {

            return new FileRegexParserHandler(thing);
        }

        return null;
    }

    protected ThingBuilder editThing(Thing thing) {
        return ThingBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration());
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {

        Thing myThing;
        String regEx;
        Pattern pattern;
        String groupConfig = "";
        String[] groupTypes = new String[0];
        int groupCount = 0;
        boolean groupsConfigured = false;
        ChannelTypeUID chStrMatchingGroup = new ChannelTypeUID("fileregexparser:matchingGroupStr");
        ChannelTypeUID chNumMatchingGroup = new ChannelTypeUID("fileregexparser:matchingGroupNum");
        myThing = super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        ThingBuilder myThingBuilder = editThing(myThing);
        Configuration config = myThing.getConfiguration();
        try {
            regEx = (String) config.get("regEx");
            pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher("");
            groupCount = matcher.groupCount();
            groupConfig = (String) config.get("matchingGroupTypes");

        } catch (Exception e) {
            logger.debug("Cannot set regEx parameter.", e);
        }
        if (groupConfig != null) {
            groupsConfigured = true;
            groupTypes = groupConfig.split(",");
            if (groupCount != groupTypes.length) {
                logger.error("Number of groups in matchingGroupTypes does not equal to the configured groups in regEx");
                return null;
            }
        }
        List<Channel> channels = new ArrayList<Channel>(myThing.getChannels());

        for (int i = 1; i <= groupCount; i++) {
            if (!groupsConfigured || groupTypes[i - 1].equals("str")) {
                Channel channel = ChannelBuilder.create(new ChannelUID(myThing.getUID(), "matchingGroup" + i), "String")
                        .withLabel("strMatchingGroup " + i).withType(chStrMatchingGroup).build();

                channels.add(channel);
            } else if (groupTypes[i - 1].equals("num")) {
                Channel channel = ChannelBuilder.create(new ChannelUID(myThing.getUID(), "matchingGroup" + i), "Number")
                        .withLabel("numMatchingGroup " + i).withType(chNumMatchingGroup).build();
                channels.add(channel);
            } else {
                logger.error(String.format("%s is not a valid type", groupTypes[i - 1]));
                return null;
            }
        }
        myThingBuilder.withChannels(channels);
        myThingBuilder.withLabel(thingUID.getId());
        myThing.setLabel(thingUID.getId());
        Thing myNewThing = myThingBuilder.build();
        return myNewThing;
    }

}
