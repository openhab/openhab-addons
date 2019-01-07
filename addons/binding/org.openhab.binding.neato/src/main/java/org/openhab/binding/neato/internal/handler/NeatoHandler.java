/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.handler;

import static org.openhab.binding.neato.internal.NeatoBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.neato.internal.CouldNotFindRobotException;
import org.openhab.binding.neato.internal.NeatoCommunicationException;
import org.openhab.binding.neato.internal.NeatoRobot;
import org.openhab.binding.neato.internal.classes.Cleaning;
import org.openhab.binding.neato.internal.classes.Details;
import org.openhab.binding.neato.internal.classes.NeatoState;
import org.openhab.binding.neato.internal.config.NeatoRobotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NeatoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Patrik Wimnell - Initial contribution
 * @author Jeff Lauterbach - Code Cleanup and Refactor
 */
public class NeatoHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NeatoHandler.class);

    private static final long INITIAL_DELAY_IN_SECONDS = 15;

    private NeatoRobot mrRobot;

    private int refreshTime;
    private ScheduledFuture<?> refreshTask;

    public NeatoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.schedule(this::refreshStateAndUpdate, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        } else if (channelUID.getId().equals(COMMAND)) {
            sendCommandToRobot(command);
        } else {
            logger.debug("Command {} is not supported for channel: {}.", command, channelUID.getId());
        }
    }

    private void sendCommandToRobot(Command command) {
        logger.debug("Ok - will handle command for CHANNEL_COMMAND");

        try {
            mrRobot.sendCommand(command.toString());
        } catch (NeatoCommunicationException e) {
            logger.debug("Error while processing command from openHAB.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        this.refreshStateAndUpdate();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Will boot up Neato Vacuum Cleaner binding!");

        NeatoRobotConfig config = getConfigAs(NeatoRobotConfig.class);

        logger.debug("Neato Robot Config: {}", config);

        refreshTime = config.getRefresh();
        if (refreshTime < 30) {
            logger.warn(
                    "Refresh time [{}] is not valid. Refresh time must be at least 30 seconds.  Setting to minimum of 30 sec",
                    refreshTime);
            config.setRefresh(30);
        }

        mrRobot = new NeatoRobot(config);
        startAutomaticRefresh();
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Neato handler '{}'.", getThing().getUID());
        if (refreshTask != null && !refreshTask.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (refreshTask.cancel(true)) {
                refreshTask = null;
            }
        }
    }

    public void refreshStateAndUpdate() {
        if (refreshTask == null || refreshTask.isCancelled()) {
            try {
                mrRobot.sendGetState();
                updateStatus(ThingStatus.ONLINE);

                mrRobot.sendGetGeneralInfo();

                publishChannels();
            } catch (NeatoCommunicationException | CouldNotFindRobotException e) {
                logger.debug("Error when refreshing state.", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    private void startAutomaticRefresh() {
        Runnable refresher = () -> refreshStateAndUpdate();

        refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, refreshTime, TimeUnit.SECONDS);
        logger.debug("Start automatic refresh at {} seconds", refreshTime);
    }

    private void publishChannels() {
        logger.debug("Updating Channels");

        NeatoState neatoState = mrRobot.getState();
        if (neatoState == null) {
            return;
        }

        updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, neatoState.getMeta().getFirmware());
        updateProperty(Thing.PROPERTY_MODEL_ID, neatoState.getMeta().getModelName());

        updateState(CHANNEL_STATE, new StringType(neatoState.getRobotState().name()));
        updateState(CHANNEL_ERROR, new StringType((String) ObjectUtils.defaultIfNull(neatoState.getError(), "")));
        updateState(CHANNEL_ACTION, new StringType(neatoState.getRobotAction().name()));

        Details details = neatoState.getDetails();
        if (details != null) {
            updateState(CHANNEL_BATTERY, new DecimalType(details.getCharge()));
            updateState(CHANNEL_DOCKHASBEENSEEN, details.getDockHasBeenSeen() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_ISCHARGING, details.getIsCharging() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_ISSCHEDULED, details.getIsScheduleEnabled() ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_ISDOCKED, details.getIsDocked() ? OnOffType.ON : OnOffType.OFF);
        }

        Cleaning cleaning = neatoState.getCleaning();
        if (cleaning != null) {
            updateState(CHANNEL_CLEANINGCATEGORY, new StringType(cleaning.getCategory().name()));
            updateState(CHANNEL_CLEANINGMODE, new StringType(cleaning.getMode().name()));
            updateState(CHANNEL_CLEANINGMODIFIER, new StringType(cleaning.getModifier().name()));
            updateState(CHANNEL_CLEANINGSPOTWIDTH, new DecimalType(cleaning.getSpotWidth()));
            updateState(CHANNEL_CLEANINGSPOTHEIGHT, new DecimalType(cleaning.getSpotHeight()));
        }
    }
}
