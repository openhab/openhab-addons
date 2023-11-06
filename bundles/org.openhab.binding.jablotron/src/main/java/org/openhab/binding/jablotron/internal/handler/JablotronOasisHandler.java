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
package org.openhab.binding.jablotron.internal.handler;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jablotron.internal.model.JablotronControlResponse;
import org.openhab.binding.jablotron.internal.model.JablotronDataUpdateResponse;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetailSegment;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JablotronOasisHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronOasisHandler extends JablotronAlarmHandler {

    private final Logger logger = LoggerFactory.getLogger(JablotronOasisHandler.class);

    public JablotronOasisHandler(Thing thing, String alarmName) {
        super(thing, alarmName);
        dataCache = new ExpiringCache<>(CACHE_TIMEOUT_MS, this::sendGetStatusRequest);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            logger.debug("refreshing channel: {}", channelUID.getId());
            updateChannel(channelUID.getId());
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_COMMAND:
                    if (command instanceof StringType) {
                        scheduler.execute(() -> {
                            sendCommand(command.toString());
                        });
                    }
                    break;
                case CHANNEL_STATUS_PGX:
                    if (command instanceof OnOffType) {
                        scheduler.execute(() -> {
                            controlSection("PGM_1", command.equals(OnOffType.ON) ? "set" : "unset");
                        });
                    }
                    break;
                case CHANNEL_STATUS_PGY:
                    if (command instanceof OnOffType) {
                        scheduler.execute(() -> {
                            controlSection("PGM_2", command.equals(OnOffType.ON) ? "set" : "unset");
                        });
                    }
                    break;
            }
        }
    }

    private void updateChannel(String channel) {
        ExpiringCache<JablotronDataUpdateResponse> localDataCache = dataCache;
        if (localDataCache != null) {
            switch (channel) {
                case CHANNEL_STATUS_A:
                    updateSegmentStatus("STATE_1", localDataCache.getValue());
                    break;
                case CHANNEL_STATUS_B:
                    updateSegmentStatus("STATE_2", localDataCache.getValue());
                    break;
                case CHANNEL_STATUS_ABC:
                    updateSegmentStatus("STATE_3", localDataCache.getValue());
                    break;
                case CHANNEL_STATUS_PGX:
                    updateSegmentStatus("PGM_1", localDataCache.getValue());
                    break;
                case CHANNEL_STATUS_PGY:
                    updateSegmentStatus("PGM_2", localDataCache.getValue());
                    break;
                case CHANNEL_LAST_CHECK_TIME:
                    // not updating
                    break;
                default:
                    updateEventChannel(channel);
            }
        }
    }

    @Override
    protected void updateSegmentStatus(JablotronServiceDetailSegment segment) {
        logger.debug("Segment id: {} and status: {}", segment.getSegmentId(), segment.getSegmentState());
        State newState = "unset".equals(segment.getSegmentState()) ? OnOffType.OFF : OnOffType.ON;
        switch (segment.getSegmentId()) {
            case "STATE_1":
                updateState(CHANNEL_STATUS_A, newState);
                break;
            case "STATE_2":
                updateState(CHANNEL_STATUS_B, newState);
                break;
            case "STATE_3":
                updateState(CHANNEL_STATUS_ABC, newState);
                break;
            case "PGM_1":
                updateState(CHANNEL_STATUS_PGX, newState);
                break;
            case "PGM_2":
                updateState(CHANNEL_STATUS_PGY, newState);
                break;
            default:
                logger.debug("Unknown segment received: {} with state: {}", segment.getSegmentId(),
                        segment.getSegmentState());
        }
    }

    public synchronized void controlSection(String section, String status) {
        logger.debug("Controlling section: {} with status: {}", section, status);
        JablotronControlResponse response = sendUserCode(section, section.toLowerCase(), status, "");

        updateAlarmStatus();
        if (response == null) {
            logger.debug("null response/status received during the control of section: {}", section);
        }
    }

    public synchronized void sendCommand(String code) {
        JablotronControlResponse response = sendUserCode(code);
        scheduler.schedule(this::updateAlarmStatus, 1, TimeUnit.SECONDS);

        if (response == null) {
            logger.debug("null response/status received during sending a code");
        }
    }

    private synchronized @Nullable JablotronControlResponse sendUserCode(String code) {
        return sendUserCode("sections", "button_1", "partialSet", code);
    }
}
