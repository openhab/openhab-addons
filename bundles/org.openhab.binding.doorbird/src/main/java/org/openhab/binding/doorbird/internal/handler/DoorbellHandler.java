/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.doorbird.internal.handler;

import static org.openhab.binding.doorbird.internal.DoorbirdBindingConstants.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.doorbird.internal.action.DoorbirdActions;
import org.openhab.binding.doorbird.internal.api.DoorbirdAPI;
import org.openhab.binding.doorbird.internal.api.DoorbirdImage;
import org.openhab.binding.doorbird.internal.api.SipStatus;
import org.openhab.binding.doorbird.internal.config.DoorbellConfiguration;
import org.openhab.binding.doorbird.internal.listener.DoorbirdUdpListener;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorbellHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbellHandler extends BaseThingHandler {
    private static final long MONTAGE_UPDATE_DELAY_SECONDS = 5L;

    // Maximum number of doorbell and motion history images stored on Doorbird backend
    private static final int MAX_HISTORY_IMAGES = 50;

    private final Logger logger = LoggerFactory.getLogger(DoorbellHandler.class);

    // Get a dedicated threadpool for the long-running listener thread
    private final ScheduledExecutorService doorbirdScheduler = ThreadPoolManager
            .getScheduledPool("doorbirdListener" + "-" + thing.getUID().getId());
    private @Nullable ScheduledFuture<?> listenerJob;
    private final DoorbirdUdpListener udpListener;

    private @Nullable ScheduledFuture<?> imageRefreshJob;
    private @Nullable ScheduledFuture<?> doorbellOffJob;
    private @Nullable ScheduledFuture<?> motionOffJob;

    private @NonNullByDefault({}) DoorbellConfiguration config;

    private DoorbirdAPI api = new DoorbirdAPI();

    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;

    public DoorbellHandler(Thing thing, TimeZoneProvider timeZoneProvider, HttpClient httpClient) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.httpClient = httpClient;
        udpListener = new DoorbirdUdpListener(this);
    }

    @Override
    public void initialize() {
        config = getConfigAs(DoorbellConfiguration.class);
        String host = config.doorbirdHost;
        if (host == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Doorbird host not provided");
            return;
        }
        String user = config.userId;
        if (user == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User ID not provided");
            return;
        }
        String password = config.userPassword;
        if (password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User password not provided");
            return;
        }
        api.setAuthorization(host, user, password);
        api.setHttpClient(httpClient);
        startImageRefreshJob();
        startUDPListenerJob();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        stopUDPListenerJob();
        stopImageRefreshJob();
        stopDoorbellOffJob();
        stopMotionOffJob();
        super.dispose();
    }

    // Callback used by listener to get Doorbird host name
    public @Nullable String getDoorbirdHost() {
        return config.doorbirdHost;
    }

    // Callback used by listener to get Doorbird password
    public @Nullable String getUserId() {
        return config.userId;
    }

    // Callback used by listener to get Doorbird password
    public @Nullable String getUserPassword() {
        return config.userPassword;
    }

    // Callback used by listener to update doorbell channel
    public void updateDoorbellChannel(long timestamp) {
        logger.debug("Handler: Update DOORBELL channels for thing {}", getThing().getUID());
        DoorbirdImage dbImage = api.downloadCurrentImage();
        if (dbImage != null) {
            RawType image = dbImage.getImage();
            updateState(CHANNEL_DOORBELL_IMAGE, image != null ? image : UnDefType.UNDEF);
            updateState(CHANNEL_DOORBELL_TIMESTAMP, getLocalDateTimeType(dbImage.getTimestamp()));
        }
        triggerChannel(CHANNEL_DOORBELL, CommonTriggerEvents.PRESSED);
        startDoorbellOffJob();
        updateDoorbellMontage();
    }

    // Callback used by listener to update motion channel
    public void updateMotionChannel(long timestamp) {
        logger.debug("Handler: Update MOTION channels for thing {}", getThing().getUID());
        DoorbirdImage dbImage = api.downloadCurrentImage();
        if (dbImage != null) {
            RawType image = dbImage.getImage();
            updateState(CHANNEL_MOTION_IMAGE, image != null ? image : UnDefType.UNDEF);
            updateState(CHANNEL_MOTION_TIMESTAMP, getLocalDateTimeType(dbImage.getTimestamp()));
        }
        updateState(CHANNEL_MOTION, OnOffType.ON);
        startMotionOffJob();
        updateMotionMontage();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Got command {} for channel {} of thing {}", command, channelUID, getThing().getUID());

        switch (channelUID.getId()) {
            case CHANNEL_DOORBELL_IMAGE:
                if (command instanceof RefreshType) {
                    refreshDoorbellImageFromHistory();
                }
                break;
            case CHANNEL_MOTION_IMAGE:
                if (command instanceof RefreshType) {
                    refreshMotionImageFromHistory();
                }
                break;
            case CHANNEL_LIGHT:
                handleLight(command);
                break;
            case CHANNEL_OPENDOOR1:
                handleOpenDoor(command, "1");
                break;
            case CHANNEL_OPENDOOR2:
                handleOpenDoor(command, "2");
                break;
            case CHANNEL_IMAGE:
                if (command instanceof RefreshType) {
                    handleGetImage();
                }
                break;
            case CHANNEL_DOORBELL_HISTORY_INDEX:
            case CHANNEL_MOTION_HISTORY_INDEX:
                if (command instanceof RefreshType) {
                    // On REFRESH, get the first history image
                    handleHistoryImage(channelUID, new DecimalType(1));
                } else {
                    // Get the history image specified in the command
                    handleHistoryImage(channelUID, command);
                }
                break;
            case CHANNEL_DOORBELL_IMAGE_MONTAGE:
                if (command instanceof RefreshType) {
                    updateDoorbellMontage();
                }
                break;
            case CHANNEL_MOTION_IMAGE_MONTAGE:
                if (command instanceof RefreshType) {
                    updateMotionMontage();
                }
                break;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(DoorbirdActions.class);
    }

    public void actionRestart() {
        api.restart();
    }

    public void actionSIPHangup() {
        api.sipHangup();
    }

    public String actionGetRingTimeLimit() {
        return getSipStatusValue(SipStatus::getRingTimeLimit);
    }

    public String actionGetCallTimeLimit() {
        return getSipStatusValue(SipStatus::getCallTimeLimit);
    }

    public String actionGetLastErrorCode() {
        return getSipStatusValue(SipStatus::getLastErrorCode);
    }

    public String actionGetLastErrorText() {
        return getSipStatusValue(SipStatus::getLastErrorText);
    }

    private String getSipStatusValue(Function<SipStatus, String> function) {
        String value = "";
        SipStatus sipStatus = api.getSipStatus();
        if (sipStatus != null) {
            value = function.apply(sipStatus);
        }
        return value;
    }

    private void refreshDoorbellImageFromHistory() {
        logger.debug("Handler: REFRESH doorbell image channel using most recent doorbell history image");
        scheduler.execute(() -> {
            DoorbirdImage dbImage = api.downloadDoorbellHistoryImage("1");
            if (dbImage != null) {
                RawType image = dbImage.getImage();
                updateState(CHANNEL_DOORBELL_IMAGE, image != null ? image : UnDefType.UNDEF);
                updateState(CHANNEL_DOORBELL_TIMESTAMP, getLocalDateTimeType(dbImage.getTimestamp()));
            }
            updateState(CHANNEL_DOORBELL, OnOffType.OFF);
        });
    }

    private void refreshMotionImageFromHistory() {
        logger.debug("Handler: REFRESH motion image channel using most recent motion history image");
        scheduler.execute(() -> {
            DoorbirdImage dbImage = api.downloadMotionHistoryImage("1");
            if (dbImage != null) {
                RawType image = dbImage.getImage();
                updateState(CHANNEL_MOTION_IMAGE, image != null ? image : UnDefType.UNDEF);
                updateState(CHANNEL_MOTION_TIMESTAMP, getLocalDateTimeType(dbImage.getTimestamp()));
            }
            updateState(CHANNEL_MOTION, OnOffType.OFF);
        });
    }

    private void handleLight(Command command) {
        // It's only possible to energize the light relay
        if (command.equals(OnOffType.ON)) {
            api.lightOn();
        }
    }

    private void handleOpenDoor(Command command, String doorNumber) {
        // It's only possible to energize the open door relay
        if (command.equals(OnOffType.ON)) {
            api.openDoorDoorbell(doorNumber);
        }
    }

    private void handleGetImage() {
        scheduler.execute(this::updateImageAndTimestamp);
    }

    private void handleHistoryImage(ChannelUID channelUID, Command command) {
        if (!(command instanceof DecimalType)) {
            logger.debug("History index must be of type DecimalType");
            return;
        }
        int value = ((DecimalType) command).intValue();
        if (value < 0 || value > MAX_HISTORY_IMAGES) {
            logger.debug("History index must be in range 1 to {}", MAX_HISTORY_IMAGES);
            return;
        }
        boolean isDoorbell = CHANNEL_DOORBELL_HISTORY_INDEX.equals(channelUID.getId());
        String imageChannelId = isDoorbell ? CHANNEL_DOORBELL_HISTORY_IMAGE : CHANNEL_MOTION_HISTORY_IMAGE;
        String timestampChannelId = isDoorbell ? CHANNEL_DOORBELL_HISTORY_TIMESTAMP : CHANNEL_MOTION_HISTORY_TIMESTAMP;

        DoorbirdImage dbImage = isDoorbell ? api.downloadDoorbellHistoryImage(command.toString())
                : api.downloadMotionHistoryImage(command.toString());
        if (dbImage != null) {
            RawType image = dbImage.getImage();
            updateState(imageChannelId, image != null ? image : UnDefType.UNDEF);
            updateState(timestampChannelId, getLocalDateTimeType(dbImage.getTimestamp()));
        }
    }

    private void startImageRefreshJob() {
        Integer imageRefreshRate = config.imageRefreshRate;
        if (imageRefreshRate != null) {
            imageRefreshJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    updateImageAndTimestamp();
                } catch (RuntimeException e) {
                    logger.debug("Refresh image job got unhandled exception: {}", e.getMessage(), e);
                }
            }, 8L, imageRefreshRate, TimeUnit.SECONDS);
            logger.debug("Scheduled job to refresh image channel every {} seconds", imageRefreshRate);
        }
    }

    private void stopImageRefreshJob() {
        if (imageRefreshJob != null) {
            imageRefreshJob.cancel(true);
            imageRefreshJob = null;
            logger.debug("Canceling image refresh job");
        }
    }

    private void startUDPListenerJob() {
        logger.debug("Listener job is scheduled to start in 5 seconds");
        listenerJob = doorbirdScheduler.schedule(udpListener, 5, TimeUnit.SECONDS);
    }

    private void stopUDPListenerJob() {
        if (listenerJob != null) {
            listenerJob.cancel(true);
            udpListener.shutdown();
            logger.debug("Canceling listener job");
        }
    }

    private void startDoorbellOffJob() {
        Integer offDelay = config.doorbellOffDelay;
        if (offDelay == null) {
            return;
        }
        if (doorbellOffJob != null) {
            doorbellOffJob.cancel(true);
        }
        doorbellOffJob = scheduler.schedule(() -> {
            logger.debug("Update channel 'doorbell' to OFF for thing {}", getThing().getUID());
            triggerChannel(CHANNEL_DOORBELL, CommonTriggerEvents.RELEASED);
        }, offDelay, TimeUnit.SECONDS);
    }

    private void stopDoorbellOffJob() {
        if (doorbellOffJob != null) {
            doorbellOffJob.cancel(true);
            doorbellOffJob = null;
            logger.debug("Canceling doorbell off job");
        }
    }

    private void startMotionOffJob() {
        Integer offDelay = config.motionOffDelay;
        if (offDelay == null) {
            return;
        }
        if (motionOffJob != null) {
            motionOffJob.cancel(true);
        }
        motionOffJob = scheduler.schedule(() -> {
            logger.debug("Update channel 'motion' to OFF for thing {}", getThing().getUID());
            updateState(CHANNEL_MOTION, OnOffType.OFF);
        }, offDelay, TimeUnit.SECONDS);
    }

    private void stopMotionOffJob() {
        if (motionOffJob != null) {
            motionOffJob.cancel(true);
            motionOffJob = null;
            logger.debug("Canceling motion off job");
        }
    }

    private void updateDoorbellMontage() {
        if (config.montageNumImages == 0) {
            return;
        }
        logger.debug("Scheduling DOORBELL montage update to run in {} seconds", MONTAGE_UPDATE_DELAY_SECONDS);
        scheduler.schedule(() -> {
            updateMontage(CHANNEL_DOORBELL_IMAGE_MONTAGE);
        }, MONTAGE_UPDATE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void updateMotionMontage() {
        if (config.montageNumImages == 0) {
            return;
        }
        logger.debug("Scheduling MOTION montage update to run in {} seconds", MONTAGE_UPDATE_DELAY_SECONDS);
        scheduler.schedule(() -> {
            updateMontage(CHANNEL_MOTION_IMAGE_MONTAGE);
        }, MONTAGE_UPDATE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void updateMontage(String channelId) {
        logger.debug("Update montage for channel '{}'", channelId);
        ArrayList<BufferedImage> images = getImages(channelId);
        if (!images.isEmpty()) {
            State state = createMontage(images);
            if (state != null) {
                logger.debug("Got a montage. Updating channel '{}' with image montage", channelId);
                updateState(channelId, state);
                return;
            }
        }
        logger.debug("Updating channel '{}' with NULL image montage", channelId);
        updateState(channelId, UnDefType.NULL);
    }

    // Get an array list of history images
    private ArrayList<BufferedImage> getImages(String channelId) {
        ArrayList<BufferedImage> images = new ArrayList<>();
        Integer numberOfImages = config.montageNumImages;
        if (numberOfImages != null) {
            for (int imageNumber = 1; imageNumber <= numberOfImages; imageNumber++) {
                logger.trace("Downloading montage image {} for channel '{}'", imageNumber, channelId);
                DoorbirdImage historyImage = CHANNEL_DOORBELL_IMAGE_MONTAGE.equals(channelId)
                        ? api.downloadDoorbellHistoryImage(String.valueOf(imageNumber))
                        : api.downloadMotionHistoryImage(String.valueOf(imageNumber));
                if (historyImage != null) {
                    RawType image = historyImage.getImage();
                    if (image != null) {
                        try {
                            BufferedImage i = ImageIO.read(new ByteArrayInputStream(image.getBytes()));
                            images.add(i);
                        } catch (IOException e) {
                            logger.debug("IOException creating BufferedImage from downloaded image: {}",
                                    e.getMessage());
                        }
                    }
                }
            }
            if (images.size() < numberOfImages) {
                logger.debug("Some images could not be downloaded: wanted={}, actual={}", numberOfImages,
                        images.size());
            }
        }
        return images;
    }

    // Assemble the array of images into a single scaled image
    private @Nullable State createMontage(ArrayList<BufferedImage> images) {
        State state = null;
        Integer montageScaleFactor = config.montageScaleFactor;
        if (montageScaleFactor != null) {
            // Assume all images are the same size, as the Doorbird image resolution cannot
            // be changed by the user
            int height = (int) (images.get(0).getHeight() * (montageScaleFactor / 100.0));
            int width = (int) (images.get(0).getWidth() * (montageScaleFactor / 100.0));
            int widthTotal = width * images.size();
            logger.debug("Dimensions of final montage image: w={}, h={}", widthTotal, height);

            // Create concatenated image
            int currentWidth = 0;
            BufferedImage concatImage = new BufferedImage(widthTotal, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = concatImage.createGraphics();
            logger.debug("Concatenating images array into single image");
            for (int j = 0; j < images.size(); j++) {
                g2d.drawImage(images.get(j), currentWidth, 0, width, height, null);
                currentWidth += width;
            }
            g2d.dispose();

            // Convert image to a state
            logger.debug("Rendering image to byte array and converting to RawType state");
            byte[] imageBytes = convertImageToByteArray(concatImage);
            if (imageBytes != null) {
                state = new RawType(imageBytes, "image/png");
            }
        }
        return state;
    }

    private byte @Nullable [] convertImageToByteArray(BufferedImage image) {
        byte[] data = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            data = out.toByteArray();
        } catch (IOException ioe) {
            logger.debug("IOException occurred converting image to byte array", ioe);
        }
        return data;
    }

    private void updateImageAndTimestamp() {
        DoorbirdImage dbImage = api.downloadCurrentImage();
        if (dbImage != null) {
            RawType image = dbImage.getImage();
            updateState(CHANNEL_IMAGE, image != null ? image : UnDefType.UNDEF);
            updateState(CHANNEL_IMAGE_TIMESTAMP, getLocalDateTimeType(dbImage.getTimestamp()));
        }
    }

    private DateTimeType getLocalDateTimeType(long dateTimeSeconds) {
        return new DateTimeType(Instant.ofEpochSecond(dateTimeSeconds).atZone(timeZoneProvider.getTimeZone()));
    }
}
