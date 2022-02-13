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
package org.openhab.binding.lametrictime.internal.handler;

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.CHANNEL_APP_COMMAND;

import org.openhab.binding.lametrictime.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.api.model.CoreApps;
import org.openhab.binding.lametrictime.internal.config.LaMetricTimeAppConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link StopwatchAppHandler} represents an instance of the built-in stopwatch app.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class StopwatchAppHandler extends AbstractLaMetricTimeAppHandler {
    private static final String PACKAGE_NAME = "com.lametric.stopwatch";

    public static final String COMMAND_PAUSE = "pause";
    public static final String COMMAND_RESET = "reset";
    public static final String COMMAND_START = "start";

    private final Logger logger = LoggerFactory.getLogger(StopwatchAppHandler.class);

    public StopwatchAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleAppCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_APP_COMMAND:
                    handleCommandChannel(command);
                    updateState(channelUID, new StringType()); // clear state
                    updateActiveAppOnDevice();
                    break;
                default:
                    logger.debug("Channel '{}' not supported", channelUID);
                    break;
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Failed to perform action - taking app offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected String getPackageName(LaMetricTimeAppConfiguration config) {
        return PACKAGE_NAME;
    }

    private void handleCommandChannel(Command command) throws ApplicationActionException {
        String commandStr = command.toFullString();
        switch (commandStr) {
            case COMMAND_PAUSE:
                getDevice().doAction(getWidget(), CoreApps.stopwatch().pause());
                break;
            case COMMAND_RESET:
                getDevice().doAction(getWidget(), CoreApps.stopwatch().reset());
                break;
            case COMMAND_START:
                getDevice().doAction(getWidget(), CoreApps.stopwatch().start());
                break;
            default:
                logger.debug("Stopwatch app command '{}' not supported", commandStr);
                break;
        }
    }
}
