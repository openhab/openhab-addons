/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.test.internal;

import static org.openhab.binding.test.internal.TestBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TestHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
public class TestHandler extends BaseThingHandler {
    // private final Map<String, ChannelType> channelTypes = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TestHandler.class);
    private BasicChannelTypeProvider basicChannelTypeProvider;
    // private @Nullable TestConfiguration config;

    public TestHandler(Thing thing, BasicChannelTypeProvider basicChannelTypeProvider) {
        super(thing);
        this.basicChannelTypeProvider = basicChannelTypeProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_1.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // config = getConfigAs(TestConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        logger.warn("Thing UID: {}", thing.getUID());
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, "sample-channel");
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), "samplechannel"), "Number:Temperature")
                .withType(channelTypeUID).withKind(ChannelKind.STATE).withLabel("label").build();

        ChannelTypeUID channelTypeUID2 = new ChannelTypeUID(BINDING_ID, "test-channel");
        basicChannelTypeProvider.addChannelType(channelTypeUID2, "label", "String", "Description", true);
        Channel channel2 = ChannelBuilder.create(new ChannelUID(thing.getUID(), "testchannel"), "String")
                .withType(channelTypeUID2).withKind(ChannelKind.STATE).withLabel("label2").build();

        ChannelTypeUID channelTypeUID3 = new ChannelTypeUID(BINDING_ID, "test-channel3");
        basicChannelTypeProvider.addChannelType(channelTypeUID3, "label3", "Number", "Description3", false);
        Channel channel3 = ChannelBuilder.create(new ChannelUID(thing.getUID(), "testchannel3"))
                .withType(channelTypeUID3).withKind(ChannelKind.STATE).build();

        ChannelTypeUID channelTypeUIDx = new ChannelTypeUID(BINDING_ID, "test-channelx");
        basicChannelTypeProvider.addChannelType(channelTypeUIDx, "labelx", "String", "Descriptionx", true);
        Channel channelx = ChannelBuilder.create(new ChannelUID(thing.getUID(), "testchannelx"), "String")
                .withType(channelTypeUID2).withKind(ChannelKind.STATE).withLabel("labelx").build();

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannel(channel.getUID());
        thingBuilder.withoutChannel(channel2.getUID());
        thingBuilder.withoutChannel(channel3.getUID());
        thingBuilder.withoutChannel(channelx.getUID());
        logger.warn("Channel UID: {}", channel.getUID());
        logger.warn("Channel2 UID: {}", channel2.getUID());
        logger.warn("channelTypeUID2: {}", channelTypeUID2.getAsString());
        thingBuilder.withChannel(channel);
        thingBuilder.withChannel(channel2);
        thingBuilder.withChannel(channel3);
        thingBuilder.withChannel(channelx);
        updateThing(thingBuilder.build());

        updateStatus(ThingStatus.ONLINE);
    }
    /*
     * @Override
     * public void thingUpdated(Thing thing) {
     * // dispose();
     * this.thing = thing;
     * updateStatus(ThingStatus.ONLINE);
     * // initialize();
     * }
     */
}
