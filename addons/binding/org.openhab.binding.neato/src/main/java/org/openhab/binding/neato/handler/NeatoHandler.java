/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.handler;

import static org.openhab.binding.neato.NeatoBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.neato.NeatoBindingConstants;
import org.openhab.binding.neato.internal.NeatoRobot;
import org.openhab.binding.neato.internal.exceptions.CouldNotFindRobotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeatoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NeatoHandler.class);

    private String vacuumSerialNumber;
    private String vacuumSecret;
    private String vacuumName;

    private NeatoRobot mrRobot;

    private int refreshTime;
    private ScheduledFuture<?> refreshTask;
    private static int DEFAULTREFRESHTIME = 60;

    private ThingStatus thisStatus;

    public NeatoHandler(Thing thing) {
        super(thing);
        thisStatus = ThingStatus.OFFLINE;
    }

    public ScheduledFuture<?> getRefreshTask() {
        return this.refreshTask;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(NeatoBindingConstants.COMMAND)) {
            logger.debug("Ok - will handle command for CHANNEL_COMMAND");

            try {
                mrRobot.sendCommand(command);
            } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
                logger.error("Error while processing command from openHAB. Error: {}", e.getMessage());
            }

        }
        this.refreshStateAndUpdate();
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        if (this.refreshTask != null && !this.refreshTask.isCancelled()) {
            this.refreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    @Override
    public void initialize() {

        logger.debug("Will boot up Neato Vacuum Cleaner binding!");

        updateStatus(ThingStatus.OFFLINE);
        thisStatus = ThingStatus.OFFLINE;

        Configuration config = getThing().getConfiguration();
        vacuumSecret = (String) config.get(CONFIG_SECRET);
        vacuumName = (String) config.get(CONFIG_NAME);
        vacuumSerialNumber = (String) config.get(CONFIG_SERIAL);

        logger.debug("Got config settings for {} with serial number: {}", vacuumName, vacuumSecret);

        super.initialize();

        refreshTime = ((BigDecimal) config.get(CONFIG_REFRESHTIME)).intValue();
        if (refreshTime < 30) {
            logger.warn("Refresh time [{}] is not valid. Refresh time must be more than (or equal to) 30 seconds.",
                    refreshTime);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Refresh time must be more than (or equal to) 30 seconds)");
        }

        mrRobot = new NeatoRobot(vacuumSerialNumber, vacuumSecret, vacuumName);
        this.startAutomaticRefresh();

    }

    public void refreshStateAndUpdate() {
        try {

            if (mrRobot.sendGetState()) {

                if (thisStatus != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                    thisStatus = ThingStatus.ONLINE;
                }

                mrRobot.sendGetGeneralInfo();

                List<Channel> channels = getThing().getChannels();

                for (Channel channel : channels) {
                    publishChannel(channel.getUID());
                }

            }
        } catch (IOException ioexc) {

            logger.error("Error when refreshing state. Error: {}", ioexc.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ioexc.getMessage());

        } catch (CouldNotFindRobotException | InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Error when refreshing state. Error: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            thisStatus = ThingStatus.OFFLINE;
        }
    }

    private void startAutomaticRefresh() {

        Runnable refresher = new Runnable() {
            @Override
            public void run() {
                refreshStateAndUpdate();
            }
        };

        this.refreshTask = scheduler.scheduleAtFixedRate(refresher, 0, refreshTime, TimeUnit.SECONDS);
        logger.debug("Start automatic refresh at {} seconds", refreshTime);
    }

    private void publishChannel(ChannelUID channelUID) {
        String channelID = channelUID.getId();
        logger.debug("Will publish changes to channel {}!", channelID);

        State state = null;
        switch (channelID) {
            case CHANNEL_BATTERY:
                state = new DecimalType(mrRobot.getState().getDetails().getCharge());
                break;
            case CHANNEL_STATE:
                state = new StringType(mrRobot.getState().getStateString().toString());
                break;
            case CHANNEL_VERSION:
                state = new StringType(mrRobot.getState().getVersion().toString());
                break;
            case CHANNEL_ERROR:
                state = new StringType(mrRobot.getState().getError().toString());
                break;
            case CHANNEL_MODELNAME:
                state = new StringType(mrRobot.getState().getMeta().getModelName().toString());
                break;
            case CHANNEL_FIRMWARE:
                state = new StringType(mrRobot.getState().getMeta().getFirmware().toString());
                break;
            case CHANNEL_ACTION:
                state = new StringType(mrRobot.getState().getActionString().toString());
                break;
            case CHANNEL_DOCKHASBEENSEEN:
                if (mrRobot.getState().getDetails().getDockHasBeenSeen()) {
                    state = OnOffType.ON;
                } else {
                    state = OnOffType.OFF;
                }
                break;
            case CHANNEL_ISCHARGING:
                if (mrRobot.getState().getDetails().getIsCharging()) {
                    state = OnOffType.ON;
                } else {
                    state = OnOffType.OFF;
                }

                break;
            case CHANNEL_ISSCHEDULED:
                if (mrRobot.getState().getDetails().getIsScheduleEnabled()) {
                    state = OnOffType.ON;
                } else {
                    state = OnOffType.OFF;
                }
                break;
            case CHANNEL_ISDOCKED:
                if (mrRobot.getState().getDetails().getIsDocked()) {
                    state = OnOffType.ON;
                } else {
                    state = OnOffType.OFF;
                }
                break;
            case CHANNEL_NAME:
                state = new StringType(mrRobot.getName());
                break;
            case CHANNEL_CLEANINGCATEGORY:
                state = new StringType(mrRobot.getState().getCleaning().getCategoryString());
                break;
            case CHANNEL_CLEANINGMODE:
                state = new StringType(mrRobot.getState().getCleaning().getModeString());
                break;
            case CHANNEL_CLEANINGMODIFIER:
                state = new StringType(mrRobot.getState().getCleaning().getModifierString());
                break;
            case CHANNEL_CLEANINGSPOTWIDTH:
                state = new DecimalType(mrRobot.getState().getCleaning().getSpotWidth());
                break;
            case CHANNEL_CLEANINGSPOTHEIGHT:
                state = new DecimalType(mrRobot.getState().getCleaning().getSpotHeight());
                break;
        }

        if (state != null) {
            updateState(channelID, state);
        } else {
            logger.debug("Can not update channel with ID : {} - channel name might be wrong!", channelID);
        }

    }
}
