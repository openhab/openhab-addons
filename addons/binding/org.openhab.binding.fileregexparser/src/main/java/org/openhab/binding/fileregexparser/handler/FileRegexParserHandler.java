/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fileregexparser.handler;

import static org.openhab.binding.fileregexparser.FileRegexParserBindingConstants.CHANNEL_GROUPCOUNT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.fileregexparser.internal.FileRegexParserWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author meju25 - Initial contribution
 */
public class FileRegexParserHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(FileRegexParserHandler.class);
    private String fileName;
    private String regEx;
    private Pattern pattern;
    private Matcher matcher;
    private FileRegexParserWorker myWorker = new FileRegexParserWorker(this);

    public FileRegexParserHandler(Thing thing) {
        super(thing);
    }

    public void updateStateReceived(String channel, String state) {
        ChannelUID curChannel = new ChannelUID(getThing().getUID(), channel);
        String itemType = thing.getChannel(channel).getAcceptedItemType();
        if (itemType.equals("String")) {
            updateState(curChannel, new StringType(state));
        } else if (itemType.equals("Number")) {
            updateState(curChannel, new DecimalType(state));
        } else {
            logger.error("unsupported itemType: " + itemType);
        }
    }

    public void updateStateReceived(String channel, Number state) {
        updateState(new ChannelUID(getThing().getUID(), channel), new DecimalType((BigDecimal) state));
    }

    @Override
    public void initialize() {
        logger.debug("Initializing FileRegexParser handler.");
        super.initialize();

        Configuration config = getThing().getConfiguration();

        try {
            fileName = (String) config.get("fileName");
        } catch (Exception e) {
            logger.error("Cannot set fileName parameter.", e);
        }
        try {
            regEx = (String) config.get("regEx");
            pattern = Pattern.compile(regEx);
        } catch (Exception e) {
            logger.error("Cannot set regEx parameter.", e);
        }
        matcher = pattern.matcher("");
        matcher.groupCount();
        logger.debug(getThing().getUID() + ": updating groupCount to: " + matcher.groupCount());
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_GROUPCOUNT), new DecimalType(matcher.groupCount()));
        updateStatus(ThingStatus.ONLINE);
        myWorker.startWorker(fileName, regEx);
    }

    protected void thingStructureChanged() {
        String regEx;
        Pattern pattern;
        String groupConfig = "";
        String[] groupTypes = new String[0];
        int groupCount = 0;
        boolean groupsConfigured = false;
        ChannelTypeUID chStrCapturingGroup = new ChannelTypeUID("fileregexparser:capturingGroupStr");
        ChannelTypeUID chNumCapturingGroup = new ChannelTypeUID("fileregexparser:capturingGroupNum");
        ThingBuilder myThingBuilder = editThing();
        Configuration config = thing.getConfiguration();
        try {
            fileName = (String) config.get("fileName");
        } catch (Exception e) {
            logger.error("Cannot set fileName parameter.", e);
        }
        try {
            regEx = (String) config.get("regEx");
            pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher("");
            groupCount = matcher.groupCount();
            groupConfig = (String) config.get("capturingGroupTypes");
        } catch (Exception e) {
            logger.debug("Cannot set regEx parameter.", e);
        }
        if (groupConfig != null) {
            groupsConfigured = true;
            groupTypes = groupConfig.split(",");
            if (groupCount != groupTypes.length) {
                logger.error(
                        "Number of groups in capturingGroupTypes does not equal to the configured groups in regEx");
                return;
            }
        }
        List<Channel> channels = new ArrayList<Channel>(thing.getChannels());

        for (int i = 1; i <= groupCount; i++) {
            if (!groupsConfigured || groupTypes[i - 1].trim().equals("str")) {
                Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), "capturingGroup" + i), "String")
                        .withLabel("strCapturingGroup " + i).withType(chStrCapturingGroup).build();
                channels.add(channel);
            } else if (groupTypes[i - 1].trim().equals("num")) {
                Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), "capturingGroup" + i), "Number")
                        .withLabel("numCapturingGroup " + i).withType(chNumCapturingGroup).build();
                channels.add(channel);
            } else {
                logger.error(String.format("%s is not a valid type", groupTypes[i - 1]));
                return;
            }
        }
        myThingBuilder.withChannels(channels);
        myThingBuilder.withLabel(thing.getUID().getId());
        thing.setLabel(thing.getUID().getId());
        updateThing(myThingBuilder.build());
    }

    @Override
    public void dispose() {
        myWorker.stopWorker();
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("Thing updated: " + thing.getUID());
        dispose();
        this.thing = thing;
        initialize();
        this.thingStructureChanged();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        validateConfigurationParameters(configurationParameters);
        logger.debug("Thing ConfigUpdate: " + thing.getUID());
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        if (isInitialized()) {
            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
            this.thingStructureChanged();
        } else {
            // persist new configuration and notify Thing Manager
            updateConfiguration(configuration);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }
}
