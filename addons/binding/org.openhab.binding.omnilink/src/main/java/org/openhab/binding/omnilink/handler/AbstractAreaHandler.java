/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.openhab.binding.omnilink.OmnilinkBindingConstants.AreaAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;
import com.digitaldan.jomnilinkII.MessageTypes.systemevents.AllOnOffEvent;

/**
 *
 * @author Craig Hamilton
 *
 */
public abstract class AbstractAreaHandler extends AbstractOmnilinkStatusHandler<AreaStatus> {
    private Logger logger = LoggerFactory.getLogger(AbstractAreaHandler.class);

    public AbstractAreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand: {}, command: {}", channelUID, command);

        switch (channelUID.getId()) {
            case OmnilinkBindingConstants.CHANNEL_AREA_ACTIVATE_KEYPAD_EMERGENCY:
                handleKeypadEmergency(channelUID, command);
                break;
            default:
                handleSecurityMode(channelUID, command);
                break;
        }

    }

    private void handleSecurityMode(ChannelUID channelUID, Command command) {
        int mode = getMode(channelUID);
        int areaNumber = getThingNumber();

        if (!(command instanceof StringType)) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Command {} is not valid for channel {}, only StringType are accepted",
                            command, channelUID.getId()));
        }

        logger.debug("Received mode {} on area {}", mode, areaNumber);

        char[] code = command.toFullString().toCharArray();
        if (code.length != 4) {
            logger.error("Invalid code length, code must be 4 digits");
        } else {
            // mode, codeNum, areaNum
            try {
                SecurityCodeValidation codeValidation = getOmnilinkBridgeHandler().reqSecurityCodeValidation(areaNumber,
                        Character.getNumericValue(code[0]), Character.getNumericValue(code[1]),
                        Character.getNumericValue(code[2]), Character.getNumericValue(code[3]));
                /*
                 * 0 Invalid code
                 * 1 Master
                 * 2 Manager
                 * 3 User
                 */
                logger.debug("User code number:{} level:{}", codeValidation.getCodeNumber(),
                        codeValidation.getAuthorityLevel());

                /*
                 * Valid user code number are 1-99, 251 is duress code, 0 means code does not exist
                 */
                if ((codeValidation.getCodeNumber() > 0 && codeValidation.getCodeNumber() <= 99)
                        && codeValidation.getAuthorityLevel() > 0) {
                    sendOmnilinkCommand(mode, codeValidation.getCodeNumber(), areaNumber);
                } else {
                    logger.error("System reported an invalid code");
                }
            } catch (OmniInvalidResponseException e) {
                logger.debug("Could not arm area, are all zones closed?", e);
            } catch (OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.error("Could not send area command", e);
            }
        }
        // this is a send only channel, so don't store the user code
        updateState(channelUID, UnDefType.UNDEF);
    }

    /**
     * Get the specific mode for the omni type
     *
     * @param channelUID Channel that maps to a mode
     * @return Omni representation of mode.
     */
    protected abstract int getMode(ChannelUID channelUID);

    /**
     * Get the set of alarms supported by this area handler.
     *
     * @return Set of alarms for this handler.
     */
    protected abstract EnumSet<AreaAlarm> getAlarms();

    private void handleKeypadEmergency(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType) {
            try {
                int areaNumber = getThingNumber();
                getOmnilinkBridgeHandler().activateKeypadEmergency(areaNumber, ((DecimalType) command).intValue());
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.debug("Could not send command to omnilink: {}", e);
            }
        } else {
            throw new IllegalArgumentException(
                    MessageFormat.format("Command {} is not valid for channel {}, only DecimalTypes are accepted",
                            command, channelUID.getId()));
        }
    }

    @Override
    public void updateChannels(AreaStatus status) {
        logger.debug("handle area event: mode:{} alarms:{} entryTimer:{} exitTimer:{}", status.getMode(),
                status.getAlarms(), status.getEntryTimer(), status.getExitTimer());

        /*
         * According to the spec, if the 3rd bit is set on a area mode, then that mode is in a delayed state.
         * Unfortunately, this is not the case, but we can fix that by looking to see if the exit timer
         * is set and do this manually.
         */

        int mode = status.getExitTimer() > 0 ? status.getMode() | 1 << 3 : status.getMode();
        updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_MODE), new DecimalType(mode));

        /*
         * Alarm status is actually 8 status, packed into each bit, so we loop through to see if a bit is set, note that
         * this means you can have multiple alarms set at once
         */
        BigInteger alarmBits = BigInteger.valueOf(status.getAlarms());
        for (AreaAlarm alarm : getAlarms()) {
            OnOffType alarmState = alarm.isSet(alarmBits) ? OnOffType.ON : OnOffType.OFF;
            updateState(new ChannelUID(thing.getUID(), alarm.getChannelUID()), alarmState);
        }

    }

    public void handleAllOnOffEvent(AllOnOffEvent event) {
        ChannelUID activateChannel = new ChannelUID(getThing().getUID(),
                OmnilinkBindingConstants.TRIGGER_CHANNEL_AREA_ALL_ON_OFF_EVENT);
        triggerChannel(activateChannel, event.isOn() ? "ON" : "OFF");
    }

    @Override
    protected Optional<AreaStatus> retrieveStatus() {
        try {
            int areaId = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHandler().requestObjectStatus(Message.OBJ_TYPE_AREA, areaId,
                    areaId, false);
            return Optional.of((AreaStatus) objStatus.getStatuses()[0]);
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing area: ", e);
            return Optional.empty();
        }
    }

}
