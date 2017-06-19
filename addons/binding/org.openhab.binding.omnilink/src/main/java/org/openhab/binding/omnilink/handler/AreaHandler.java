package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.SecurityCodeValidation;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AreaStatus;

public class AreaHandler extends AbstractOmnilinkHandler {
    private Logger logger = LoggerFactory.getLogger(AreaHandler.class);

    public AreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand: {}, command: {}", channelUID, command);

        if (!(command instanceof DecimalType)) {
            logger.debug("Command {} is not valid, only DecimalTypes are accepted", command);
            return;
        }

        int areaNumber = (int) getThing().getConfiguration().get(OmnilinkBindingConstants.THING_PROPERTIES_NUMBER);

        // keypad command
        if (OmnilinkBindingConstants.CHANNEL_AREA_ACTIVATE_KEYPAD_EMERGENCY.equals(channelUID.getId())) {
            try {
                getOmnilinkBridgeHander().activateKeypadEmergency(areaNumber, ((DecimalType) command).intValue());
            } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                    | BridgeOfflineException e) {
                logger.debug("Could not send command to omnilink: {}", e);
            }
            return;
        }

        // security mode commands;
        int mode = -1;
        switch (channelUID.getId()) {
            case "disarm":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_DISARM.getNumber();
                break;
            case "day":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_DAY_MODE.getNumber();
                break;
            case "night":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_NIGHT_MODE.getNumber();
                break;
            case "away":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_AWAY_MODE.getNumber();
                break;
            case "vacation":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_VACATION_MODE.getNumber();
                break;
            case "day_instant":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_DAY_INSTANCE_MODE.getNumber();
                break;
            case "night_delayed":
                mode = OmniLinkCmd.CMD_SECURITY_OMNI_NIGHT_DELAYED_MODE.getNumber();
                break;
            default:
                logger.debug("Unknown channel {}", channelUID.getId());
                return;
        }

        logger.debug("Receievd mode {} on area {}", mode, areaNumber);

        char[] code = command.toFullString().toCharArray();
        if (code.length != 4) {
            logger.error("Invalid code length, code must be 4 digits");
        } else {
            // mode, codeNum, areaNum
            try {
                SecurityCodeValidation codeValidation = getOmnilinkBridgeHander().reqSecurityCodeValidation(areaNumber,
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
                    getOmnilinkBridgeHander().sendOmnilinkCommand(mode, codeValidation.getCodeNumber(), areaNumber);
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

    public void handleAreaEvent(AreaStatus status) {
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
        for (int i = 0; i < OmnilinkBindingConstants.CHANNEL_AREA_ALARMS.length; i++) {
            if (((status.getAlarms() >> i) & 1) > 0) {
                updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_ALARMS[i]),
                        OnOffType.ON);
            } else {
                updateState(new ChannelUID(thing.getUID(), OmnilinkBindingConstants.CHANNEL_AREA_ALARMS[i]),
                        OnOffType.OFF);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked: {}", channelUID);
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        int areaId = Integer.parseInt(channelParts[channelParts.length - 2]);
        ObjectStatus objStatus;
        try {
            objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_AREA, areaId, areaId, false);
            handleAreaEvent((AreaStatus) objStatus.getStatuses()[0]);
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing area:", e);
        }
    }
}
