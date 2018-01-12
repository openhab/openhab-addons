/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet.handler;

import static org.openhab.binding.coolmasternet.CoolMasterNetBindingConstants.*;
import static org.openhab.binding.coolmasternet.internal.config.CoolMasterNetConfiguration.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.coolmasternet.internal.ControllerHandler;
import org.openhab.binding.coolmasternet.internal.ControllerHandler.CoolMasterClientError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HVACHandler} is responsible for handling commands for a single
 * HVAC unit (a single UID on a CoolMasterNet controller.)
 *
 * @author Angus Gratton
 */
public class HVACHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(HVACHandler.class);
    private ControllerHandler controller;

    public HVACHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Configuration config = this.getConfig();
        String uid = (String) config.get(UID);
        String channel = channelUID.getId();

        try {
            if (!controller.isConnected()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String
                        .format("Could not connect to CoolMasterNet unit %s:%d", config.get(HOST), config.get(PORT)));
            } else {
                if (channel.endsWith(ON) && command instanceof OnOffType) {
                    OnOffType onoff = (OnOffType) command;
                    controller.sendCommand(String.format("%s %s", onoff == OnOffType.ON ? "on" : "off", uid));
                } else if (channel.endsWith(SET_TEMP) && command instanceof DecimalType) {
                    DecimalType temp = (DecimalType) command;
                    controller.sendCommand(String.format("temp %s %s", uid, temp));
                } else if (channel.endsWith(MODE) && command instanceof StringType) {
                    /* the mode value in the command is the actual CoolMasterNet protocol command */
                    controller.sendCommand(String.format("%s %s", command, uid));
                } else if (channel.endsWith(FAN) && command instanceof StringType) {
                    controller.sendCommand(String.format("fspeed %s %s", uid, command));
                } else if (channel.endsWith(LOUVRE) && command instanceof StringType) {
                    controller.sendCommand(String.format("swing %s %s", uid, command));
                } else if (command instanceof RefreshType) {
                    refresh();
                }
            }
        } catch (CoolMasterClientError e) {
            logger.error("Failed to set channel {} -> {}: {}", channel, command, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialising CoolMasterNet HVAC handler...");
        updateStatus(ThingStatus.ONLINE);
        controller = (ControllerHandler) getBridge().getHandler();
    }

    /* Update this HVAC unit's properties from the controller */
    public void refresh() {
        ThingUID thinguid = getThing().getUID();

        String on = query("o");
        if (on != null) {
            updateState(new ChannelUID(thinguid, ON), "1".equals(on) ? OnOffType.ON : OnOffType.OFF);
        }
        updateState(new ChannelUID(thinguid, CURRENT_TEMP), new DecimalType(query("a")));
        updateState(new ChannelUID(thinguid, SET_TEMP), new DecimalType(query("t")));
        String mode = modeNumToStr.get(query("m"));
        if (mode != null) {
            updateState(new ChannelUID(thinguid, MODE), new StringType(mode));
        }
        String louvre = query("s");
        if (louvre != null) {
            updateState(new ChannelUID(thinguid, LOUVRE), new StringType(louvre));
        }
        String fan = fanNumToStr.get(query("f"));
        if (fan != null) {
            updateState(new ChannelUID(thinguid, FAN), new StringType(fan));
        }
    }

    private String query(String query_char) {
        String cmn_uid = (String) getConfig().get(UID);
        String command = String.format("query %s %s", cmn_uid, query_char);
        try {
            return controller.sendCommand(command);
        } catch (CoolMasterClientError e) {
            logger.error("Query {} failed: {}", command, e.getMessage());
            return null; /* passing back null sets an invalid value on the channel */
        }
    }

    /*
     * The CoolMasterNet query command returns numbers 0-5 for operation modes,
     * but these don't map to any mode you can set on the device, so we use this
     * lookup table.
     */
    private static final Map<String, String> modeNumToStr;
    static {
        modeNumToStr = new HashMap<>();
        modeNumToStr.put("0", "cool");
        modeNumToStr.put("1", "heat");
        modeNumToStr.put("2", "auto");
        modeNumToStr.put("3", "dry");
        /* 4=='haux' but this mode doesn't have an equivalent command to set it! */
        modeNumToStr.put("4", "heat");
        modeNumToStr.put("5", "fan");
    }

    /*
     * The CoolMasterNet protocol's query command returns numbers 0-5
     * for fan speed, but the protocol's fan command (& matching
     * binding command) use single-letter abbreviations.
     */
    private static final Map<String, String> fanNumToStr;
    static {
        fanNumToStr = new HashMap<>();
        fanNumToStr.put("0", "l"); /* Low */
        fanNumToStr.put("1", "m"); /* Medium */
        fanNumToStr.put("2", "h"); /* High */
        fanNumToStr.put("3", "a"); /* Auto */
        fanNumToStr.put("4", "t"); /* Top */
    }
}
