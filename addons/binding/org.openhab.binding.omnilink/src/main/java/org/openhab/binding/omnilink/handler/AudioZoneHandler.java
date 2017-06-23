package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AudioZoneStatus;

public class AudioZoneHandler extends AbstractOmnilinkHandler implements ThingHandler {

    private Logger logger = LoggerFactory.getLogger(AudioZoneHandler.class);

    public AudioZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    public void handleAudioZoneStatus(AudioZoneStatus status) {
        logger.debug("Audio Zone Status {}", status);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked: {}", channelUID);
        try {
            int audioZoneID = getThingID();
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_AUDIO_ZONE,
                    audioZoneID, audioZoneID, true);
            handleAudioZoneStatus((AudioZoneStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
        }
    }

}
