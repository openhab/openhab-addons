/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet.handler;

import static org.openhab.binding.coolmasternet.config.coolmasternetConfiguration.*;
import static org.openhab.binding.coolmasternet.coolmasternetBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
import org.openhab.binding.coolmasternet.internal.CoolMasterNetClient;
import org.openhab.binding.coolmasternet.internal.CoolMasterNetClient.CoolMasterClientError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link coolmasternetHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Two Feathers Pty Ltd - Initial contribution
 */
public class coolmasternetHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(coolmasternetHandler.class);
    private CoolMasterNetClient client;
    private ScheduledFuture<?> refreshJob;

    public coolmasternetHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Configuration config = this.getConfig();
        String uid = (String) config.get(UID);
        String channel = channelUID.getId();

        try {
            if (!client.isConnected()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, String
                        .format("Could not connect to CoolMasterNet unit %s:%d", config.get(HOST), config.get(PORT)));
            } else {
                if (channel.endsWith(ON) && command instanceof OnOffType) {
                    OnOffType onoff = (OnOffType) command;
                    client.sendCommand(String.format("%s %s", onoff == OnOffType.ON ? "on" : "off", uid));
                } else if (channel.endsWith(SET_TEMP) && command instanceof DecimalType) {
                    DecimalType temp = (DecimalType) command;
                    client.sendCommand(String.format("temp %s %s", uid, temp));
                } else if (channel.endsWith(MODE) && command instanceof StringType) {
                    /* the mode value in the command is the actual coolmasternet protocol command */
                    client.sendCommand(String.format("%s %s", command, uid));
                } else if (channel.endsWith(FAN) && command instanceof StringType) {
                    client.sendCommand(String.format("fspeed %s %s", uid, command));
                } else if (channel.endsWith(LOUVRE) && command instanceof StringType) {
                    client.sendCommand(String.format("swing %s %s", uid, command));
                }
            }
        } catch (CoolMasterClientError e) {
            logger.error("Failed to set channel {} -> {}: {}", channel, command, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialising CoolMasterNet handler...");
        super.initialize();

        Configuration config = this.getConfig();
        String host = (String) config.get(HOST);
        int port = 10102;
        try {
            port = ((BigDecimal) config.get(PORT)).intValue();
        } catch (NullPointerException e) {
            // keep default
        }
        client = CoolMasterNetClient.getClient(host, port);

        int refresh = 5;
        try {
            refresh = ((BigDecimal) config.get(REFRESH)).intValue();
        } catch (NullPointerException e) {
            // keep default
        }
        startRefreshSchedule(refresh);
    }

    private void startRefreshSchedule(int refresh) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    client.checkConnection();
                    updateStatus(ThingStatus.ONLINE);
                    ThingUID thinguid = getThing().getUID();

                    OnOffType on = "1".equals(query("o")) ? OnOffType.ON : OnOffType.OFF;
                    updateState(new ChannelUID(thinguid, ON), on);
                    updateState(new ChannelUID(thinguid, CURRENT_TEMP), new DecimalType(query("a")));
                    updateState(new ChannelUID(thinguid, SET_TEMP), new DecimalType(query("t")));
                    String mode = modeNumToStr.getOrDefault(query("m"), null);
                    updateState(new ChannelUID(thinguid, MODE), new StringType(mode));
                    updateState(new ChannelUID(thinguid, LOUVRE), new StringType(query("s")));
                    String fan = fanNumToStr.getOrDefault(query("f"), null);
                    updateState(new ChannelUID(thinguid, FAN), new StringType(fan));
                } catch (CoolMasterClientError e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            String.format("Could not connect to CoolMasterNet: {}", e.getMessage()));
                }
            }

            private String query(String query_char) {
                String cmn_uid = (String) getConfig().get(UID);
                String command = String.format("query %s %s", cmn_uid, query_char);
                try {
                    return client.sendCommand(command);
                } catch (CoolMasterClientError e) {
                    logger.error("Query {} failed: {}", command, e.getMessage());
                    return null; /* passing back null sets an invalid value on the channel */
                }
            }
        };
        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        client.disconnect();
        super.dispose();
    }

    /*
     * The coolmasternet query command returns numbers 0-5 for operation modes,
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
     * The coolmasternet query command returns numbers 0-5 for fan speed,
     * but the fan command uses single-letter abbreviations. Yay consistency?
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
