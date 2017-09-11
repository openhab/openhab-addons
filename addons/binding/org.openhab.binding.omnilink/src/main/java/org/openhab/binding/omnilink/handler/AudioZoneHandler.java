package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AudioZoneStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public class AudioZoneHandler extends AbstractOmnilinkHandler<AudioZoneStatus> implements ThingHandler {

    private Logger logger = LoggerFactory.getLogger(AudioZoneHandler.class);

    public AudioZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        int audioZoneID = getThingNumber();
        String channelID = channelUID.getId();
        try {
            switch (channelID) {
                case OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_POWER:
                    handlePowerCommand(command, audioZoneID);
                    break;
                case OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_MUTE:
                    handleMuteCommand(command, audioZoneID);
                    break;
                case OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_VOLUME:
                    handleVolumeCommand(command, audioZoneID);
                    break;
                case OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_SOURCE:
                    handleSourceCommand(command, audioZoneID);
                    break;
                default:
                    logger.warn("Channel ID ({}) not processed", channelID);
                    break;

            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("received exception handling command", e);
        }

    }

    private void handleSourceCommand(Command command, int audioZoneID)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        int source = ((DecimalType) command).intValue();
        getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_AUDIO_ZONE_SET_SOURCE.getNumber(), source,
                audioZoneID);
    }

    private void handleVolumeCommand(Command command, int audioZoneID)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        int volume = ((DecimalType) command).intValue();
        getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_AUDIO_ZONE_SET_VOLUME.getNumber(), volume,
                audioZoneID);
    }

    private void handleMuteCommand(Command command, int audioZoneID)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        /*
         * set audio zone P2 (0=all zones) to P1
         * 0 = off
         * 1 = on
         * 2 = mute off
         * 3 = mute on
         */

        int mode = ((OnOffType) command) == OnOffType.ON ? 3 : 2;
        getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_AUDIO_ZONE_SET_ON_MUTE.getNumber(), mode,
                audioZoneID);
    }

    private void handlePowerCommand(Command command, int audioZoneID)
            throws OmniInvalidResponseException, OmniUnknownMessageTypeException, BridgeOfflineException {
        /*
         * set audio zone P2 (0=all zones) to P1
         * 0 = off
         * 1 = on
         * 2 = mute off
         * 3 = mute on
         */
        int mode = ((OnOffType) command) == OnOffType.ON ? 1 : 0;
        getOmnilinkBridgeHander().sendOmnilinkCommand(OmniLinkCmd.CMD_AUDIO_ZONE_SET_ON_MUTE.getNumber(), mode,
                audioZoneID);
    }

    @Override
    public void updateChannels(AudioZoneStatus status) {
        logger.debug("Audio Zone Status {}", status);
        handlePowerState(status);
        handleMuteStatus(status);
        updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_VOLUME, new PercentType(status.getVolume()));
        updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_SOURCE, new DecimalType(status.getSource()));

    }

    private void handleMuteStatus(AudioZoneStatus status) {
        OnOffType muteState = status.isMute() ? OnOffType.ON : OnOffType.OFF;
        updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_MUTE, muteState);
    }

    private void handlePowerState(AudioZoneStatus status) {
        OnOffType powerState = status.isPower() ? OnOffType.ON : OnOffType.OFF;
        updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_ZONE_POWER, powerState);
    }

    @Override
    protected Optional<AudioZoneStatus> retrieveStatus() {
        try {
            int audioZoneID = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_AUDIO_ZONE,
                    audioZoneID, audioZoneID, true);
            return Optional.of((AudioZoneStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }
}
