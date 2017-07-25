/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.s7.handler;

import static org.openhab.binding.s7.S7BindingConstants.*;

import java.math.BigDecimal;
import java.util.Dictionary;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import Moka7.S7;

/**
 * The {@link S7ThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Sibilla - Initial contribution
 */
public class S7ThingHandler extends S7BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(S7ThingHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_LIGHT, THING_TYPE_CONTACT,
            THING_TYPE_PUSHBUTTON, THING_TYPE_SWITCH);

    private static final long TAP_OFF_DELAY = 100;
    private static final long TAP_ON_DELAY = 250;

    private boolean lastState = false;
    private long lastUpdate = 0;
    private long updateTimeout = 5000;

    public S7ThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                final boolean setOn = ((OnOffType) command) == OnOffType.ON;

                if (setOn != lastState) {
                    lastUpdate = System.currentTimeMillis();
                    logger.debug("Prepare setting S7 lamp {} to {}.", getThing().getLabel(), setOn);

                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            logger.debug("Setting S7 lamp {} to {}.", getThing().getLabel(), setOn);

                            try {
                                S7BridgeHandler bridge = (S7BridgeHandler) getBridge().getHandler();
                                int Area = ((java.math.BigDecimal) getConfig().get(INPUT_DBAREA)).intValue();
                                int Address = ((java.math.BigDecimal) getConfig().get(INPUT_ADDRESS)).intValue();

                                switch ((String) getConfig().get(ACCESS_MODE)) {
                                    case MODE_TOGGLE:
                                        bridge.setBit(Area, Address, false);
                                        Thread.sleep(TAP_OFF_DELAY);
                                        bridge.setBit(Area, Address, true);
                                        Thread.sleep(TAP_ON_DELAY);
                                        bridge.setBit(Area, Address, false);
                                        break;
                                    case MODE_READ_WRITE:
                                        bridge.setBit(Area, Address, setOn);
                                        break;
                                    case MODE_PUSHBUTTON:
                                        bridge.setBit(Area, Address, true);
                                        updateState(CHANNEL_STATE, OnOffType.ON);
                                        Thread.sleep(TAP_ON_DELAY);
                                        bridge.setBit(Area, Address, false);
                                        updateState(CHANNEL_STATE, OnOffType.OFF);
                                        break;
                                }
                            } catch (InterruptedException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Could not toggle state of " + getThing().getLabel() + ".");
                                logger.warn("{}: {}", e.getMessage(), e.getStackTrace().toString());
                            }
                        }
                    };

                    super.scheduler.execute(t);
                }
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void processNewData(Dictionary<Integer, byte[]> data) {
        super.processNewData(data);

        if (this.getThing().getStatus() == ThingStatus.ONLINE
                && !this.getThing().getThingTypeUID().equals(THING_TYPE_PUSHBUTTON)
                && !getConfig().get(ACCESS_MODE).equals(MODE_PUSHBUTTON)) {
            int area = 0;
            int address = 0;
            byte[] buffer = null;

            BigDecimal value = (java.math.BigDecimal) getConfig().get(OUTPUT_DBAREA);
            if (value == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Cannot retrieve output DB Area for thing of access mode " + getConfig().get(ACCESS_MODE)
                                + ".");
                return;
            }
            area = value.intValue();

            value = (java.math.BigDecimal) getConfig().get(OUTPUT_ADDRESS);
            if (value == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Cannot retrieve output address.");
                return;
            }
            address = value.intValue();

            buffer = data.get(area);

            if (buffer != null) {
                if (address < 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Invalid output address : negative");
                } else if (buffer.length < address / 8) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Invalid output address : out of bound.");
                } else {
                    boolean isOn = S7.GetBitAt(buffer, address / 8, address % 8);

                    if (isOn != lastState || lastUpdate + this.updateTimeout < System.currentTimeMillis()) {
                        lastState = isOn;
                        lastUpdate = System.currentTimeMillis();

                        Channel channel = this.thing.getChannel(CHANNEL_STATE);

                        if (channel != null) {
                            switch (channel.getAcceptedItemType()) {
                                case "Switch":
                                    updateState(CHANNEL_STATE, isOn ? OnOffType.ON : OnOffType.OFF);
                                    break;
                                case "Contact":
                                    updateState(CHANNEL_STATE, isOn ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                                    break;
                                default:
                                    logger.warn("New state received but unknown channel type: {}",
                                            channel.getAcceptedItemType());
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }
}
