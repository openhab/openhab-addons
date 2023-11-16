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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.omnilink.internal.AreaAlarm;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AreaProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAreaStatus;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.AllOnOffEvent;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link AbstractAreaHandler} defines some methods that can be used across
 * the many different areas defined in an OmniLink Controller.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public abstract class AbstractAreaHandler extends AbstractOmnilinkStatusHandler<ExtendedAreaStatus> {
    private final Logger logger = LoggerFactory.getLogger(AbstractAreaHandler.class);
    private final int thingID = getThingNumber();

    public AbstractAreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();

        super.initialize();
        if (bridgeHandler != null) {
            updateAreaProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Area!");
        }
    }

    private void updateAreaProperties(OmnilinkBridgeHandler bridgeHandler) {
        final List<AreaProperties> areas = getAreaProperties();
        if (areas != null) {
            for (AreaProperties areaProperties : areas) {
                String thingName = areaProperties.getName();
                if (areaProperties.getNumber() == 1 && "".equals(thingName)) {
                    thingName = "Main Area";
                }
                Map<String, String> properties = editProperties();
                properties.put(THING_PROPERTIES_NAME, thingName);
                updateProperties(properties);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_AREA_ACTIVATE_KEYPAD_EMERGENCY:
                handleKeypadEmergency(channelUID, command);
                break;
            default:
                handleSecurityMode(channelUID, command);
                break;
        }
    }

    private void handleSecurityMode(ChannelUID channelUID, Command command) {
        int mode = getMode(channelUID);

        if (!(command instanceof StringType)) {
            logger.debug("Invalid command: {}, must be StringType", command);
            return;
        }

        logger.debug("Received mode: {}, on area: {}", mode, thingID);

        char[] code = command.toFullString().toCharArray();
        if (code.length != 4) {
            logger.warn("Invalid code length, code must be 4 digits");
        } else {
            // mode, codeNum, areaNum
            try {
                final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
                if (bridge != null) {
                    SecurityCodeValidation codeValidation = bridge.reqSecurityCodeValidation(thingID,
                            Character.getNumericValue(code[0]), Character.getNumericValue(code[1]),
                            Character.getNumericValue(code[2]), Character.getNumericValue(code[3]));
                    /*
                     * 0 Invalid code
                     * 1 Master
                     * 2 Manager
                     * 3 User
                     */
                    logger.debug("User code number: {}, level: {}", codeValidation.getCodeNumber(),
                            codeValidation.getAuthorityLevel());

                    /*
                     * Valid user code number are 1-99, 251 is duress code, 0 means code does not exist
                     */
                    if ((codeValidation.getCodeNumber() > 0 && codeValidation.getCodeNumber() <= 99)
                            && codeValidation.getAuthorityLevel() > 0) {
                        sendOmnilinkCommand(mode, codeValidation.getCodeNumber(), thingID);
                    } else {
                        logger.warn("System reported an invalid code");
                    }
                } else {
                    logger.debug("Received null bridge while sending area command!");
                }
            } catch (OmniInvalidResponseException e) {
                logger.debug("Could not arm area: {}, are all zones closed?", e.getMessage());
            } catch (OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("Could not send area command: {}", e.getMessage());
            }
        }
        // This is a send only channel, so don't store the user code
        updateState(channelUID, UnDefType.UNDEF);
    }

    /**
     * Get the specific mode for the OmniLink type
     *
     * @param channelUID Channel that maps to a mode
     * @return OmniLink representation of mode.
     */
    protected abstract int getMode(ChannelUID channelUID);

    /**
     * Get the set of alarms supported by this area handler.
     *
     * @return Set of alarms for this handler.
     */
    protected abstract EnumSet<AreaAlarm> getAlarms();

    private void handleKeypadEmergency(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType decimalCommand) {
            try {
                final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
                if (bridge != null) {
                    bridge.activateKeypadEmergency(thingID, decimalCommand.intValue());
                } else {
                    logger.debug("Received null bridge while sending Keypad Emergency command!");
                }
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("Received exception while sending command to OmniLink Controller: {}", e.getMessage());
            }
        } else {
            logger.debug("Invalid command: {}, must be DecimalType", command);
        }
    }

    @Override
    public void updateChannels(ExtendedAreaStatus status) {
        logger.debug("Handle area event: mode: {}, alarms: {}, entryTimer: {}, exitTimer: {}", status.getMode(),
                status.getAlarms(), status.getEntryTimer(), status.getExitTimer());

        /*
         * According to the specification, if the 3rd bit is set on an area mode, then that mode is in a delayed state.
         * Unfortunately, this is not the case, but we can fix that by looking to see if the exit timer
         * is set and do this manually.
         */
        int mode = status.getExitTimer() > 0 ? status.getMode() | 1 << 3 : status.getMode();
        updateState(new ChannelUID(thing.getUID(), CHANNEL_AREA_MODE), new DecimalType(mode));

        /*
         * Alarm status is actually 8 status, packed into each bit, so we loop through to see if a bit is set, note that
         * this means you can have multiple alarms set at once
         */
        BigInteger alarmBits = BigInteger.valueOf(status.getAlarms());
        for (AreaAlarm alarm : getAlarms()) {
            OnOffType alarmState = OnOffType.from(alarm.isSet(alarmBits));
            updateState(new ChannelUID(thing.getUID(), alarm.getChannelUID()), alarmState);
        }
    }

    public void handleAllOnOffEvent(AllOnOffEvent event) {
        ChannelUID activateChannel = new ChannelUID(getThing().getUID(), TRIGGER_CHANNEL_AREA_ALL_ON_OFF_EVENT);
        triggerChannel(activateChannel, event.isOn() ? "ON" : "OFF");
    }

    @Override
    protected Optional<ExtendedAreaStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridge = getOmnilinkBridgeHandler();
            if (bridge != null) {
                ObjectStatus objStatus = bridge.requestObjectStatus(Message.OBJ_TYPE_AREA, thingID, thingID, true);
                return Optional.of((ExtendedAreaStatus) objStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Area status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Area status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
