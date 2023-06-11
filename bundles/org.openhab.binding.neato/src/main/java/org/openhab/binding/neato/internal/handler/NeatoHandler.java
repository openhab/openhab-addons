/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neato.internal.handler;

import static org.openhab.binding.neato.internal.NeatoBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.neato.internal.CouldNotFindRobotException;
import org.openhab.binding.neato.internal.NeatoBindingConstants;
import org.openhab.binding.neato.internal.NeatoCommunicationException;
import org.openhab.binding.neato.internal.NeatoRobot;
import org.openhab.binding.neato.internal.classes.Cleaning;
import org.openhab.binding.neato.internal.classes.Details;
import org.openhab.binding.neato.internal.classes.NeatoState;
import org.openhab.binding.neato.internal.config.NeatoRobotConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private NeatoRobot mrRobot;

    private int refreshTime;
    private ScheduledFuture<?> refreshTask;

    public NeatoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshStateAndUpdate();
        } else if (channelUID.getId().equals(NeatoBindingConstants.COMMAND)) {
            sendCommandToRobot(command);
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
    public void dispose() {
        logger.debug("Running dispose()");
        if (this.refreshTask != null) {
            this.refreshTask.cancel(true);
            this.refreshTask = null;
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("Will boot up Neato Vacuum Cleaner binding!");

        NeatoRobotConfig config = getThing().getConfiguration().as(NeatoRobotConfig.class);

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

    public void refreshStateAndUpdate() {
        if (mrRobot != null) {
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

        this.refreshTask = scheduler.scheduleWithFixedDelay(refresher, 0, refreshTime, TimeUnit.SECONDS);
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
