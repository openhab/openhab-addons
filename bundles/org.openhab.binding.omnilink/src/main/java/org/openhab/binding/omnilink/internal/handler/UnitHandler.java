/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.properties.UnitProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedUnitStatus;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.SwitchPressEvent;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link AbstractOmnilinkHandler} defines some methods that can be used across
 * the many different Units exposed by the OmniLink protocol
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class UnitHandler extends AbstractOmnilinkStatusHandler<ExtendedUnitStatus> {
    private final Logger logger = LoggerFactory.getLogger(UnitHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public UnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateUnitProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Unit!");
        }
    }

    private void updateUnitProperties(OmnilinkBridgeHandler bridgeHandler) {
        final List<AreaProperties> areas = getAreaProperties();
        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                int areaFilter = bitFilterForArea(areaProperties);

                ObjectPropertyRequest<UnitProperties> objectPropertyRequest = ObjectPropertyRequest
                        .builder(bridgeHandler, ObjectPropertyRequests.UNIT, thingID, 0).selectNamed()
                        .areaFilter(areaFilter).selectAnyLoad().build();

                for (UnitProperties unitProperties : objectPropertyRequest) {
                    Map<String, String> properties = editProperties();
                    properties.put(THING_PROPERTIES_NAME, unitProperties.getName());
                    properties.put(THING_PROPERTIES_AREA, Integer.toString(areaProperties.getNumber()));
                    updateProperties(properties);
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_UNIT_LEVEL:
            case CHANNEL_UNIT_SWITCH:
                if (command instanceof OnOffType onOffCommand) {
                    handleOnOff(channelUID, onOffCommand);
                } else {
                    logger.debug("Invalid command: {}, must be OnOffType", command);
                }
                break;
            case CHANNEL_UNIT_ON_FOR_SECONDS:
            case CHANNEL_UNIT_OFF_FOR_SECONDS:
            case CHANNEL_UNIT_ON_FOR_MINUTES:
            case CHANNEL_UNIT_OFF_FOR_MINUTES:
            case CHANNEL_UNIT_ON_FOR_HOURS:
            case CHANNEL_UNIT_OFF_FOR_HOURS:
                if (command instanceof DecimalType decimalCommand) {
                    handleUnitDuration(channelUID, decimalCommand);
                } else {
                    logger.debug("Invalid command: {}, must be DecimalType", command);
                }
                break;
            default:
                logger.warn("Unknown channel for Unit thing: {}", channelUID);
        }
    }

    private void handleUnitDuration(ChannelUID channelUID, DecimalType command) {
        logger.debug("handleUnitDuration called for channel: {}, command: {}", channelUID, command);
        final String channelID = channelUID.getId();

        int duration;
        if (channelID.endsWith("seconds")) {
            duration = command.intValue();
        } else if (channelID.endsWith("minutes")) {
            duration = command.intValue() + 100;
        } else if (channelID.endsWith("hours")) {
            duration = command.intValue() + 200;
        } else {
            logger.warn("Unknown channel for Unit duration: {}", channelUID);
            return;
        }

        sendOmnilinkCommand(channelID.startsWith("on") ? CommandMessage.CMD_UNIT_ON : CommandMessage.CMD_UNIT_OFF,
                duration, thingID);
    }

    protected void handleOnOff(ChannelUID channelUID, OnOffType command) {
        logger.debug("handleOnOff called for channel: {}, command: {}", channelUID, command);
        sendOmnilinkCommand(OnOffType.ON.equals(command) ? CommandMessage.CMD_UNIT_ON : CommandMessage.CMD_UNIT_OFF, 0,
                thingID);
    }

    @Override
    public void updateChannels(ExtendedUnitStatus status) {
        logger.debug("updateChannels called for Unit status: {}", status);
        int unitStatus = status.getStatus();
        int level = 0;

        if (unitStatus == Status.UNIT_OFF) {
            level = 0;
        } else if (unitStatus == Status.UNIT_ON) {
            level = 100;
        } else if ((unitStatus >= Status.UNIT_SCENE_A) && (unitStatus <= Status.UNIT_SCENE_L)) {
            level = 100;
        } else if ((unitStatus >= Status.UNIT_LEVEL_0) && (unitStatus <= Status.UNIT_LEVEL_100)) {
            level = unitStatus - Status.UNIT_LEVEL_0;
        }

        updateState(CHANNEL_UNIT_LEVEL, PercentType.valueOf(Integer.toString(level)));
        updateState(CHANNEL_UNIT_SWITCH, OnOffType.from(level != 0));
    }

    @Override
    protected Optional<ExtendedUnitStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
            if (bridgeHandler != null) {
                ObjectStatus objectStatus = bridgeHandler.requestObjectStatus(Message.OBJ_TYPE_UNIT, thingID, thingID,
                        true);
                return Optional.of((ExtendedUnitStatus) objectStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Unit status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Unit status: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static class Status {
        private static final int UNIT_OFF = 0;
        private static final int UNIT_ON = 1;
        private static final int UNIT_SCENE_A = 2;
        private static final int UNIT_SCENE_L = 13;
        private static final int UNIT_LEVEL_0 = 100;
        private static final int UNIT_LEVEL_100 = 200;
    }

    /**
     * Handle a switch press event by triggering the appropriate channel.
     *
     * @param switchPressEvent
     */
    public void handleSwitchPressEvent(SwitchPressEvent switchPressEvent) {
        ChannelUID activateChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_SWITCH_PRESS_EVENT);
        triggerChannel(activateChannel, Integer.toString(switchPressEvent.getSwitchValue()));
    }
}
