/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.jablotron.internal.model.JablotronControlResponse;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetailSegment;
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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_COMMAND) && command instanceof StringType) {
            scheduler.execute(() -> {
                sendCommand(command.toString());
            });
        }

        if (channelUID.getId().equals(CHANNEL_STATUS_PGX) && command instanceof OnOffType) {
            scheduler.execute(() -> {
                controlSection("PGM_1", command.equals(OnOffType.ON) ? "set" : "unset");
            });
        }

        if (channelUID.getId().equals(CHANNEL_STATUS_PGY) && command instanceof OnOffType) {
            scheduler.execute(() -> {
                controlSection("PGM_2", command.equals(OnOffType.ON) ? "set" : "unset");
            });
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
                logger.debug("Unknown segment received: {} with state: {}", segment.getSegmentId(), segment.getSegmentState());
        }
    }

    public synchronized void controlSection(String section, String status) {
        logger.debug("Controlling section: {} with status: {}", section, status);
        JablotronControlResponse response = sendUserCode(section, section.toLowerCase(), status, "");

        updateAlarmStatus();
        if (response == null) {
            logger.debug("null response/status received");
        }
    }

    public synchronized void sendCommand(String code) {
        try {
            JablotronControlResponse response = sendUserCode(code);
            scheduler.schedule(this::updateAlarmStatus, 1, TimeUnit.SECONDS);

            if (response == null) {
                logger.debug("null response/status received");
            }
        } catch (Exception e) {
            logger.debug("internalReceiveCommand exception", e);
        }
    }

    private synchronized @Nullable JablotronControlResponse sendUserCode(String code) {
        return sendUserCode("sections", "button_1", "partialSet", code);
    }
}
