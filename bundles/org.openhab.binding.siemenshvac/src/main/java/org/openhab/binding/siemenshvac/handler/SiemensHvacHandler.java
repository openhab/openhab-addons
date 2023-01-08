/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.siemenshvac.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.siemenshvac.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.siemensHvacConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensHvacHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class SiemensHvacHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SiemensHvacHandler.class);
    private siemensHvacConnector hvacConnector;

    public SiemensHvacHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SiemensHvacBindingConstants.TEMPERATURE)) {

        }
    }

    @Override
    public void initialize() {
        List<Channel> channels = new ArrayList<>(1);

        ThingBuilder thingBuilder = ThingBuilder.create(thing.getThingTypeUID(), thing.getUID())
                .withBridge(thing.getBridgeUID()).withChannels(channels).withConfiguration(thing.getConfiguration());

        ChannelUID channelUID = new ChannelUID(thing.getUID(), "0");
        Channel channel = ChannelBuilder.create(channelUID, "switch").build();
        thingBuilder.withChannel(channel);

        // ChannelUID channelUID2 = new ChannelUID(thing.getUID(), "1");
        // Channel channel2 = ChannelBuilder.create(channelUID2, "switch").build();
        // thingBuilder.withChannel(channel2);

        updateThing(thingBuilder.build());

        updateStatus(ThingStatus.ONLINE);
        hvacConnector = createConnection();
    }

    protected siemensHvacConnector createConnection() {
        String host = (String) this.getConfig().get(SiemensHvacBindingConstants.HOST_PARAMETER);
        String user = (String) this.getConfig().get(SiemensHvacBindingConstants.USER_PARAMETER);
        String password = (String) this.getConfig().get(SiemensHvacBindingConstants.PASSWORD_PARAMETER);

        return new siemensHvacConnector(host, user, password);
    }

}
