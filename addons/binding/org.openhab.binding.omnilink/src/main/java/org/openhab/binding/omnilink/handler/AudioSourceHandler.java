package org.openhab.binding.omnilink.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.AudioSourceStatus;

public class AudioSourceHandler extends AbstractOmnilinkHandler {

    private final static Logger logger = LoggerFactory.getLogger(AudioSourceHandler.class);

    private final static long POLL_DELAY = 5; // 5 Second polling

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> scheduledPolling = null;

    public synchronized static void shutdownExecutor() {
        logger.debug("Shutting down audio polling executor service");
        executorService.shutdownNow();
    }

    public synchronized static void startExecutor() {
        if (executorService.isShutdown()) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
    }

    public AudioSourceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (scheduledPolling != null) {
            scheduledPolling.cancel(false);
        }
        scheduledPolling = executorService.scheduleWithFixedDelay(new PollAudioSource(getThingNumber()), POLL_DELAY,
                POLL_DELAY, TimeUnit.SECONDS);
        super.initialize();
    }

    @Override
    public void dispose() {
        if (scheduledPolling != null) {
            scheduledPolling.cancel(false);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("No commands are available for Audio Sources");
    }

    private class PollAudioSource implements Runnable {

        private final int sourceNumber;

        private PollAudioSource(int sourceNumber) {
            this.sourceNumber = sourceNumber;
        }

        @Override
        public void run() {
            logger.debug("Polling Audio Source {} Status", sourceNumber);
            try {
                int position = 0;
                Message message;
                while ((message = getOmnilinkBridgeHander().requestAudioSourceStatus(sourceNumber, position))
                        .getMessageType() == Message.MESG_TYPE_AUDIO_SOURCE_STATUS) {
                    AudioSourceStatus audioSourceStatus = (AudioSourceStatus) message;
                    position = audioSourceStatus.getPosition();
                    switch (position) {
                        case 1:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT1,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 2:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT2,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 3:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT3,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 4:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT4,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 5:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT5,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                        case 6:
                            updateState(OmnilinkBindingConstants.CHANNEL_AUDIO_SOURCE_TEXT6,
                                    new StringType(audioSourceStatus.getSourceData()));
                            break;
                    }

                }
            } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
                logger.warn("Exception Polling Audio Status", e);
            }

        }

    }

}
