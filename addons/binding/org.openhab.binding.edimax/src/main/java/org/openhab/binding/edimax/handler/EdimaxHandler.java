/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.edimax.EdimaxBindingConstants;
import org.openhab.binding.edimax.configuration.EdimaxConfiguration;
import org.openhab.binding.edimax.internal.ConnectionInformation;
import org.openhab.binding.edimax.internal.commands.GetState;
import org.openhab.binding.edimax.internal.commands.SetState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EdimaxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxHandler extends BaseThingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdimaxHandler.class);

    private static final int PORT = 10000;

    /**
     * Connection Information to the device.
     */
    protected ConnectionInformation ci;

    private ScheduledFuture<?> pollingJob;

    /**
     * Constructor for the edimax things.
     *
     * @param thing The thing
     */
    public EdimaxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        LOGGER.debug("command for {}: {}", channelUID.getAsString(), command.toString());
        try {
            if (channelUID.getId().equals(EdimaxBindingConstants.SWITCH)) {
                if (command instanceof RefreshType) {
                    LOGGER.debug("State: {}", getState().toString());
                    if (getState()) {
                        updateState(channelUID, OnOffType.ON);
                    } else {
                        updateState(channelUID, OnOffType.OFF);
                    }
                }
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        switchState(Boolean.TRUE);
                    } else {
                        switchState(Boolean.FALSE);
                    }
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void initialize() {
        LOGGER.debug("Initializing Things");
        final EdimaxConfiguration configuration = this.getConfigAs(EdimaxConfiguration.class);
        final String completeUrl = completeURL(configuration.getIpAddress());
        ci = new ConnectionInformation(configuration.getUsername(), configuration.getPassword(), completeUrl, PORT);
        updateStatus(ThingStatus.ONLINE);

        final Runnable runnable = () -> handleCommand(getThing().getChannel(EdimaxBindingConstants.SWITCH).getUID(),
                RefreshType.REFRESH);
        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
    }

    private static String completeURL(String anIp) {
        return "http://" + anIp;
    }

    /**
     * Returns state for device.
     *
     * @return The on/off state of the thing
     * @throws IOException if the communication fails
     */
    private Boolean getState() throws IOException {
        final GetState getS = new GetState();
        return getS.executeCommand(ci);
    }

    /**
     * Switch to.
     *
     * @param newState new state for the thing
     * @return True if device is turned on. Otherwise false.
     * @throws IOException if the communication fails
     */
    public Boolean switchState(Boolean newState) throws IOException {
        final SetState setS = new SetState(newState);
        return setS.executeCommand(ci);
    }
}