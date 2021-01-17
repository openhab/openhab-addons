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
package org.openhab.binding.haassohnpelletoven.internal;

import static org.openhab.binding.haassohnpelletoven.internal.HaassohnpelletstoveBindingConstants.*;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.haassohnpelletoven.communication.HaassohnpelletstoveJSONCommunication;
import org.openhab.binding.haassohnpelletoven.data.HaassohnpelletstoveJsonData;
import org.openhab.binding.haassohnpelletoven.helper.Helper;
import org.openhab.binding.haassohnpelletoven.validation.IpAddressValidator;
import org.openhab.binding.haassohnpelletoven.validation.PinValidator;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HaassohnpelletstoveHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class HaassohnpelletstoveHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HaassohnpelletstoveHandler.class);

    private @Nullable ScheduledFuture<?> refreshJob;

    private @Nullable HaassohnpelletstoveConfiguration config;
    boolean resultOk = false;

    private HaassohnpelletstoveJSONCommunication serviceCommunication;

    private boolean automaticRefreshing = false;

    private HashMap<String, Boolean> linkedChannels = new HashMap<String, Boolean>();

    public HaassohnpelletstoveHandler(Thing thing) {
        super(thing);
        serviceCommunication = new HaassohnpelletstoveJSONCommunication();
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (channelUID.getId().equals(CHANNEL_prg)) {

            String postData = null;
            if (command.equals(OnOffType.ON)) {
                postData = "{\"prg\":true}";
            } else if (command.equals(OnOffType.OFF)) {
                postData = "{\"prg\":false}";
            }

            if (postData != null) {
                logger.debug("Executing {} command", CHANNEL_prg);
                updateOvenData(postData);
            }
        } else if (channelUID.getId().equals(CHANNEL_spTemp)) {
            if (command instanceof QuantityType<?>) {
                QuantityType<?> value = null;
                try {
                    value = (QuantityType<?>) command;
                    double a = value.doubleValue();

                    String postdata = "{\"sp_temp\":" + Double.toString(a) + "}";
                    logger.debug("Executing {} command", CHANNEL_spTemp);
                    updateOvenData(postdata);

                } catch (Exception e) {
                    logger.debug("Error by parsing value: {} Details:{}", value.toString(), e.getMessage());
                }
            } else {
                logger.debug("Error. Command is the wrong type: {}", command.toString());
            }
        }
    }

    /**
     * Calls the service to update the oven data
     *
     * @param postdata
     */
    private boolean updateOvenData(@Nullable String postdata) {

        Helper message = new Helper();
        if (serviceCommunication.updateOvenData(postdata, message, this.getThing().getUID().toString())) {
            updateStatus(ThingStatus.ONLINE);
            return true;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    message.getStatusDesription());
            return false;
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing haassohnpelletstove handler for thing {}", getThing().getUID());
        config = getConfigAs(HaassohnpelletstoveConfiguration.class);

        boolean validConfig = true;
        String errors = "";
        String statusDescr = null;

        if (config == null) {
            validConfig = false;
        } else {

            if (config.refreshRate < 0 && config.refreshRate > 999) {
                errors += " Parameter 'refresh Rate' greater then 0 and less then 1000.";
                statusDescr = "Parameter 'refresh Rate' greater then 0 and less then 1000.";
                validConfig = false;
            }

            if (config.hostIP == null) {
                errors += " Parameter 'hostIP' must be configured.";
                statusDescr = "IP Address must be configured!";
                validConfig = false;
            }

            if (!new IpAddressValidator().isValid(config.hostIP)) {
                errors += " 'hostIP' is no valid IP address.";
                logger.debug("{} is no valid IP address.", config.hostIP);
                statusDescr = "No valid IP-Adress configured.";
                validConfig = false;
            }

            if (config.hostPIN == null) {
                errors += " Parameter 'hostPin' must be configured.";
                statusDescr = "PIN must be configured!";
                validConfig = false;
            }

            if (!new PinValidator().isValid(config.hostPIN)) {
                errors += " 'hostPIN' is no valid PIN. PIN consists of 4-digit numbers.";
                logger.debug("{} is no valid PIN. PIN consists of 4-digit numbers.", config.hostPIN);
                statusDescr = "No valid PIN configure. A valid PIN consists of 4-digit numbers.";
                validConfig = false;
            }

            errors = errors.trim();
        }

        Helper message = new Helper();
        message.setStatusDescription(statusDescr);
        if (validConfig) {
            serviceCommunication.setConfig(config);
            if (serviceCommunication.refreshOvenConnection(message, this.getThing().getUID().toString())) {
                if (updateOvenData(null)) {
                    updateStatus(ThingStatus.ONLINE);
                    updateLinkedChannels();
                }
            } else {

                logger.debug("Setting thing '{}' to OFFLINE: {}", getThing().getUID(), errors);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message.getStatusDesription());
            }

        } else {
            logger.debug("Setting thing '{}' to OFFLINE: {}", getThing().getUID(), errors);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message.getStatusDesription());
        }
    }

    private void updateLinkedChannels() {

        verifyLinkedChannel(CHANNEL_isTemp);
        verifyLinkedChannel(CHANNEL_mode);
        verifyLinkedChannel(CHANNEL_prg);
        verifyLinkedChannel(CHANNEL_spTemp);

        if (!linkedChannels.isEmpty()) {
            logger.debug("Start automatic refreshing");

            updateOvenData(null);

            for (Channel channel : getThing().getChannels()) {
                updateChannel(channel.getUID().getId());
            }
            startAutomaticRefresh();
            automaticRefreshing = true;
        }
    }

    private void verifyLinkedChannel(String channelID) {
        if (isLinked(channelID) && !linkedChannels.containsKey(channelID)) {
            linkedChannels.put(channelID, true);
        }
    }

    @Override
    public void dispose() {

        this.dispose();
        logger.debug("Disposing Haas and Sohn Pellet stove handler.");
        stopScheduler();
    }

    private void stopScheduler() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    /**
     * Start the job refreshing the oven status
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {

                        updateOvenData(null);

                        for (Channel channel : getThing().getChannels()) {
                            updateChannel(channel.getUID().getId());
                        }
                    } catch (Exception e) {
                        logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            @SuppressWarnings("null")
            int period = config.refreshRate;
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, period, TimeUnit.SECONDS);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {

        if (!automaticRefreshing) {
            logger.debug("Start automatic refreshing");
            startAutomaticRefresh();
            automaticRefreshing = true;
        }

        verifyLinkedChannel(channelUID.getId());
        updateChannel(channelUID.getId());
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {

        if (linkedChannels.containsKey(channelUID.getId())) {

            linkedChannels.remove(channelUID.getId());
        }

        if (linkedChannels.isEmpty()) {
            automaticRefreshing = false;
            stopScheduler();
            logger.debug("Stop automatic refreshing");
        }
    }

    private void updateChannel(String channelId) {
        if (isLinked(channelId)) {

            State state = null;
            HaassohnpelletstoveJsonData data = serviceCommunication.getOvenData();

            if (data != null) {
                switch (channelId) {
                    case CHANNEL_isTemp:
                        state = new StringType(data.getisTemp());
                        update(state, channelId);
                        break;
                    case CHANNEL_mode:
                        state = new StringType(data.getMode());
                        update(state, channelId);
                        break;
                    case CHANNEL_prg:
                        boolean prg = data.getPrg();
                        if (prg) {
                            state = OnOffType.ON;
                        } else {
                            state = OnOffType.OFF;
                        }
                        update(state, channelId);
                        break;
                    case CHANNEL_spTemp:
                        state = new StringType(data.getspTemp());
                        update(state, channelId);
                        break;

                }

            }
        }
    }

    /**
     * Updates the State of the given channel
     *
     * @param state
     * @param channelId
     */
    private void update(@Nullable State state, String channelId) {
        logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());

        if (state != null) {
            updateState(channelId, state);

        } else {
            updateState(channelId, UnDefType.NULL);
        }
    }
}
