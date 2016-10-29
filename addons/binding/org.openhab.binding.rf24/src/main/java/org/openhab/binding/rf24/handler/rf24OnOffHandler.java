/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rf24.handler;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.grzeslowski.smarthome.proto.common.Basic.BasicMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffCommandMessage;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.OnOffCommandMessage.OnOff;
import pl.grzeslowski.smarthome.proto.sensor.Sensor.SensorCommandMessage;

/**
 * The {@link rf24OnOffHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class rf24OnOffHandler extends rf24BaseHandler {

    private static final int DEVICE_ID = 1;
    private static final AtomicInteger MESSAGE_ID_SUPPLIER = new AtomicInteger();
    private Logger logger = LoggerFactory.getLogger(rf24OnOffHandler.class);

    public rf24OnOffHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(rf24BindingConstants.RF24_ON_OFF_CHANNEL)) {
            // getting OnOff command
            OnOffType onOff = (OnOffType) command;
            SensorCommandMessage cmdToSend = build(onOff);
            wifi.write(pipe, cmdToSend);
        } else if (channelUID.getId().equals(rf24BindingConstants.RF24_DEVICE_ID_CHANNEL)) {
            // Getting device Id
        }
    }

    private SensorCommandMessage build(OnOffType cmd) {
        BasicMessage basic = BasicMessage.newBuilder().setDeviceId(DEVICE_ID).setLinuxTimestamp(new Date().getTime())
                .setMessageId(MESSAGE_ID_SUPPLIER.incrementAndGet()).build();

        OnOff onOff;
        switch (cmd) {
            case ON:
                onOff = OnOff.ON;
                break;

            case OFF:
                onOff = OnOff.OFF;
                break;
            default:
                throw new RuntimeException("This should never happened [" + cmd + "]!");
        }

        OnOffCommandMessage onOffCommand = OnOffCommandMessage.newBuilder().setCommand(onOff).build();

        return SensorCommandMessage.newBuilder().setBasic(basic).setOnOffCommand(onOffCommand).build();
    }

    @Override
    public void initialize() {
        super.initialize();
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
