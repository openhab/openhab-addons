/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.CHANNEL_APP_CONTROL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApps;
import org.openhab.binding.lametrictime.internal.api.local.ApplicationActionException;
import org.openhab.binding.lametrictime.internal.config.LaMetricTimeAppConfiguration;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RadioAppHandler} represents an instance of the built-in radio app.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class RadioAppHandler extends AbstractLaMetricTimeAppHandler {
    private static final String PACKAGE_NAME = "com.lametric.radio";

    private final Logger logger = LoggerFactory.getLogger(RadioAppHandler.class);

    public RadioAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleAppCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_APP_CONTROL:
                    handleControl(command);
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

    private void handleControl(final Command command) throws ApplicationActionException {
        if (command instanceof PlayPauseType playCommand) {
            switch (playCommand) {
                case PLAY:
                    play();
                    return;
                case PAUSE:
                    stop();
                    return;
                default:
                    logger.debug("{} command not supported by LaMetric Time Radio App", command);
                    return;
            }
        }

        if (command instanceof NextPreviousType nextCommand) {
            switch (nextCommand) {
                case NEXT:
                    next();
                    return;
                case PREVIOUS:
                    previous();
                    return;
                default:
                    logger.debug("{} command not supported by LaMetric Time Radio App", command);
                    return;
            }
        }
    }

    private void next() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().next());
    }

    private void play() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().play());
    }

    private void previous() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().previous());
    }

    private void stop() throws ApplicationActionException {
        getDevice().doAction(getWidget(), CoreApps.radio().stop());
    }

    @Override
    protected @Nullable String getPackageName(LaMetricTimeAppConfiguration config) {
        return PACKAGE_NAME;
    }
}
