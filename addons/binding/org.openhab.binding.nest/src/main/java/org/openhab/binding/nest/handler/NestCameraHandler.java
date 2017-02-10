package org.openhab.binding.nest.handler;

import static org.openhab.binding.nest.NestBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nest.internal.data.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NestCameraHandler extends BaseNestHandler {

    private Logger logger = LoggerFactory.getLogger(NestCameraHandler.class);
    private Camera lastData;

    public NestCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void updateCamera(Camera camera) {
        logger.debug("Updating camera " + camera.getDeviceId());
        if (lastData == null || !lastData.equals(camera)) {
            Channel chan = getThing().getChannel(CHANNEL_STREAMING);
            updateState(chan.getUID(), camera.isStreaming() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel(CHANNEL_VIDEO_HISTORY_ENABLED);
            updateState(chan.getUID(), camera.isVideoHistoryEnabled() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel(CHANNEL_AUDIO_INPUT_ENABLED);
            updateState(chan.getUID(), camera.isAudioInputEnabled() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel(CHANNEL_PUBLIC_SHARE_ENABLED);
            updateState(chan.getUID(), camera.isPublicShareEnabled() ? OnOffType.ON : OnOffType.OFF);
            chan = getThing().getChannel(CHANNEL_PUBLIC_SHARE_URL);
            updateState(chan.getUID(), new StringType(camera.getPublicShareUrl()));
            chan = getThing().getChannel(CHANNEL_WEB_URL);
            updateState(chan.getUID(), new StringType(camera.getWebUrl()));
            chan = getThing().getChannel(CHANNEL_APP_URL);
            updateState(chan.getUID(), new StringType(camera.getAppUrl()));
            chan = getThing().getChannel(CHANNEL_SNAPSHOT_URL);
            updateState(chan.getUID(), new StringType(camera.getSnapshotUrl()));

            if (camera.isOnline()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }

            // Setup the properties for this device.
            updateProperty(PROPERTY_ID, camera.getDeviceId());
            updateProperty(PROPERTY_FIRMWARE_VERSION, camera.getSoftwareVersion());
        } else {
            logger.debug("Nothing to update, same as before.");
        }
    }
}
