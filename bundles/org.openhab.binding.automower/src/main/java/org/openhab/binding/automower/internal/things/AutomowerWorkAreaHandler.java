/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.things;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.WorkArea;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerWorkAreaHandler} represents a WorkArea of an automower as thing.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class AutomowerWorkAreaHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_WORKAREA);

    private final Logger logger = LoggerFactory.getLogger(AutomowerWorkAreaHandler.class);
    private final String thingId;

    public AutomowerWorkAreaHandler(Thing thing) {
        super(thing);
        this.thingId = this.getThing().getUID().getId();

        logger.trace("AutomowerWorkAreaHandler created for thingId {}", this.thingId);
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        // REFRESH is not implemented as it would causes >100 channel updates in a row during setup (performance, API
        // rate limit)
        if (RefreshType.REFRESH != command) {
            AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
            if (automowerBridgeHandler != null) {
                AutomowerHandler handler = automowerBridgeHandler.getAutomowerHandlerByWorkAreaId(this.thingId);
                if (handler != null) {
                    if (CHANNEL_WORKAREA_ENABLED.equals(channelUID.getIdWithoutGroup())) {
                        if (command instanceof OnOffType cmd) {
                            handler.sendAutomowerWorkAreaEnable(this.thingId, ((cmd == OnOffType.ON) ? true : false));
                        } else {
                            logger.warn("Command {} not supported for channel {}", command, channelUID);
                        }
                    } else if (CHANNEL_WORKAREA_CUTTING_HEIGHT.equals(channelUID.getIdWithoutGroup())) {
                        if (command instanceof QuantityType cmd) {
                            cmd = cmd.toUnit("%");
                            if (cmd != null) {
                                handler.sendAutomowerWorkAreaCuttingHeight(this.thingId, cmd.byteValue());
                            }
                        } else if (command instanceof DecimalType cmd) {
                            handler.sendAutomowerWorkAreaCuttingHeight(this.thingId, cmd.byteValue());
                        } else {
                            logger.warn("Command {} not supported for channel {}", command, channelUID);
                        }
                    }
                } else {
                    logger.warn("No AutomowerHandler found for areaId {}", this.thingId);
                }
            } else {
                logger.warn("No AutomowerBridgeHandler found for thingId {}", this.thingId);
            }
        } else {
            logger.warn("Command {} not supported for channel {}", command, channelUID);
        }
    }

    @Override
    public void initialize() {
        // Adding handler to map of handlers
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            automowerBridgeHandler.registerAutomowerWorkAreaHandler(this.thingId, this);
        } else {
            logger.warn("No AutomowerBridgeHandler found for thingId {}", this.thingId);
        }
        updateStatus(ThingStatus.ONLINE);
        logger.trace("AutomowerWorkAreaHandler initialized for thingId {}", this.thingId);
    }

    @Nullable
    private AutomowerBridgeHandler getAutomowerBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            automowerBridgeHandler.unregisterAutomowerWorkAreaHandler(this.thingId);
        }
    }

    public void updateWorkAreaChannels(WorkArea workArea, AutomowerHandler mowerHandler) {
        if (workArea.getWorkAreaId() == 0L && workArea.getName().isBlank()) {
            updateState(CHANNEL_WORKAREA_NAME, new StringType("main area"));
        } else {
            updateState(CHANNEL_WORKAREA_NAME, new StringType(workArea.getName()));
        }
        updateState(CHANNEL_WORKAREA_CUTTING_HEIGHT, new QuantityType<>(workArea.getCuttingHeight(), Units.PERCENT));
        updateState(CHANNEL_WORKAREA_ENABLED, OnOffType.from(workArea.isEnabled()));
        if (workArea.getProgress() != null) {
            updateState(CHANNEL_WORKAREA_PROGRESS, new QuantityType<>(workArea.getProgress(), Units.PERCENT));
        } else {
            updateState(CHANNEL_WORKAREA_PROGRESS, UnDefType.NULL);
        }

        // lastTimeCompleted is in seconds, convert it to milliseconds
        Long lastTimeCompleted = workArea.getLastTimeCompleted();
        // If lastTimeCompleted is 0 it means the work area has never been completed
        if (lastTimeCompleted != null && lastTimeCompleted != 0L) {
            updateState(CHANNEL_WORKAREA_LAST_TIME_COMPLETED, new DateTimeType(mowerHandler
                    .toZonedDateTime(TimeUnit.SECONDS.toMillis(lastTimeCompleted), mowerHandler.getMowerZoneId())));
        } else {
            updateState(CHANNEL_WORKAREA_LAST_TIME_COMPLETED, UnDefType.NULL);
        }
    }
}
