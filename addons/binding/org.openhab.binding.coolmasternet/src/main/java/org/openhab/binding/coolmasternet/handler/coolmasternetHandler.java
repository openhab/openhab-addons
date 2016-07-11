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
        String uid = (String) this.getConfig().get(UID);

        if (!client.isConnected()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to CoolMasterNet");
        } else {
            String channel = channelUID.getId();

            if (channel.endsWith(ON) && command instanceof OnOffType) {
                OnOffType onoff = (OnOffType) command;
                client.sendCommand(String.format("%s %s", onoff == OnOffType.ON ? "on" : "off", uid));
            } else if (channel.endsWith(SET_TEMP) && command instanceof DecimalType) {
                DecimalType temp = (DecimalType) command;
                client.sendCommand(String.format("temp %s %s", uid, temp));
            } else if (channel.endsWith(CURRENT_TEMP) && command instanceof DecimalType) {
            }
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
        client = new CoolMasterNetClient(host, port);

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
                    if (client.isConnected()) {
                        updateStatus(ThingStatus.ONLINE);
                        ThingUID thinguid = getThing().getUID();

                        OnOffType on = "1".equals(query("o")) ? OnOffType.ON : OnOffType.OFF;
                        updateState(new ChannelUID(thinguid, ON), on);
                        updateState(new ChannelUID(thinguid, CURRENT_TEMP), new DecimalType(query("a")));
                        updateState(new ChannelUID(thinguid, SET_TEMP), new DecimalType(query("t")));
                        updateState(new ChannelUID(thinguid, MODE), new StringType(query("m")));
                        updateState(new ChannelUID(thinguid, LOUVRE), new StringType(query("s")));
                        updateState(new ChannelUID(thinguid, FAN), new StringType(query("f")));
                        // TODO: e == failure code
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Could not connect to CoolMasterNet");
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred while refreshing: {}", e.getMessage(), e);
                }
            }

            private String query(String query_char) {
                String cmn_uid = (String) getConfig().get(UID);
                return client.sendCommand(String.format("query %s %s", cmn_uid, query_char));
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

}
