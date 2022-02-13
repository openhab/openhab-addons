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

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.*;

import java.time.LocalTime;

import org.openhab.binding.lametrictime.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.api.model.CoreApps;
import org.openhab.binding.lametrictime.internal.config.LaMetricTimeAppConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClockAppHandler} represents an instance of the built-in clock app.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class ClockAppHandler extends AbstractLaMetricTimeAppHandler {
    private static final String PACKAGE_NAME = "com.lametric.clock";

    public static final String COMMAND_DISABLE_ALARM = "disableAlarm";

    private final Logger logger = LoggerFactory.getLogger(ClockAppHandler.class);

    public ClockAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleAppCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_APP_SET_ALARM: {
                    LocalTime time = ((DateTimeType) command).getZonedDateTime().toLocalTime();
                    getDevice().doAction(getWidget(), CoreApps.clock().setAlarm(true, time, null));
                    updateActiveAppOnDevice();
                    break;
                }
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
            case COMMAND_DISABLE_ALARM:
                getDevice().doAction(getWidget(), CoreApps.clock().stopAlarm());
                break;
            default:
                logger.debug("Clock app command '{}' not supported", commandStr);
                break;
        }
    }
}
