/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv.handler;

import static org.openhab.binding.xmltv.XmlTVBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.xmltv.internal.configuration.XmlChannelConfiguration;
import org.openhab.binding.xmltv.internal.jaxb.Icon;
import org.openhab.binding.xmltv.internal.jaxb.MediaChannel;
import org.openhab.binding.xmltv.internal.jaxb.Programme;
import org.openhab.binding.xmltv.internal.jaxb.Tv;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelHandler} is responsible for handling informations
 * made available in regard of the channel and current programme
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ChannelHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(XmlTVHandler.class);
    private XmlTVHandler bridgeHandler;
    private XmlChannelConfiguration config;
    private ScheduledFuture<?> globalJob = null;
    private MediaChannel mediaChannel;
    private RawType mediaIcon = new RawType(new byte[0], RawType.DEFAULT_MIME_TYPE);
    private RawType programIcon = new RawType(new byte[0], RawType.DEFAULT_MIME_TYPE);

    @NonNull
    public List<Programme> programmes = new ArrayList<Programme>();

    public ChannelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(XmlChannelConfiguration.class);

        logger.debug("Initializing Broadcast Channel handler for uid '{}'", getThing().getUID());

        if (globalJob == null || globalJob.isCancelled()) {
            globalJob = scheduler.scheduleWithFixedDelay(globalRunnable, 3, config.refresh, TimeUnit.SECONDS);
        }
    }

    private Runnable globalRunnable = () -> {
        if (programmes.size() < 2 && getBridgeHandler() != null) {
            Tv tv = getBridgeHandler().getXmlFile();
            if (tv != null) {
                programmes.clear();

                if (mediaChannel == null) {
                    Optional<@NonNull MediaChannel> channel = tv.getMediaChannels().stream()
                            .filter(mediaChannel -> mediaChannel.getId().equals(config.channelId)).findFirst();
                    if (channel.isPresent()) {
                        setMediaChannel(channel.get());
                    }
                }

                tv.getProgrammes().stream().filter(
                        p -> p.getChannel().equals(config.channelId) && p.getProgrammeStop().isAfter(Instant.now()))
                        .forEach(p -> programmes.add(p));

                programIcon = downloadIcon(programmes.get(0).getIcons());

                updateStatus(ThingStatus.ONLINE);
            }
        }

        if (programmes.size() > 0 && Instant.now().isAfter(programmes.get(0).getProgrammeStop())) {
            programmes.remove(0);
            programIcon = downloadIcon(programmes.get(0).getIcons());
        }

        getThing().getChannels().forEach(channel -> updateChannel(channel.getUID()));
    };

    @Override
    public void dispose() {
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        // TODO Auto-generated method stub
    }

    protected XmlTVHandler getBridgeHandler() {
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
                bridgeHandler = (XmlTVHandler) bridge.getHandler();
            }
        }
        return bridgeHandler;
    }

    /**
     * Update the channel from the last OpenUV data retrieved
     *
     * @param channelUID the id identifying the channel to be updated
     *
     */
    private void updateChannel(@NonNull ChannelUID channelUID) {
        String[] uidElements = channelUID.getId().split("#");
        if (mediaChannel != null && uidElements.length == 2) {
            int target = GROUP_NEXT_PROGRAMME.equals(uidElements[0]) ? 1 : 0;
            Programme programme = programmes.get(target);

            switch (uidElements[1]) {
                case CHANNEL_ICON:
                    updateState(channelUID, GROUP_CHANNEL_PROPERTIES.equals(uidElements[0]) ? mediaIcon : programIcon);
                    break;
                case CHANNEL_CHANNEL_URL:
                    updateState(channelUID, new StringType(mediaChannel.getIcons().get(0).getSrc()));
                    break;
                case CHANNEL_PROGRAM_START:
                    Instant is = programme.getProgrammeStart();
                    ZonedDateTime zds = ZonedDateTime.ofInstant(is, ZoneId.systemDefault());
                    updateState(channelUID, new DateTimeType(zds));
                    break;
                case CHANNEL_PROGRAM_END:
                    ZonedDateTime zde = ZonedDateTime.ofInstant(programme.getProgrammeStop(), ZoneId.systemDefault());
                    updateState(channelUID, new DateTimeType(zde));
                    break;
                case CHANNEL_PROGRAM_TITLE:
                    updateState(channelUID, new StringType(programme.getTitles().get(0).getValue()));
                    break;
                case CHANNEL_PROGRAM_CATEGORY:
                    updateState(channelUID, new StringType(programme.getCategories().get(0).getValue()));
                    break;
                case CHANNEL_PROGRAM_ICON:
                    List<Icon> icons = programme.getIcons();
                    if (icons.size() > 0) {
                        updateState(channelUID, new StringType(icons.get(0).getSrc()));
                    } else {
                        updateState(channelUID, UnDefType.NULL);
                    }
                    break;
                case CHANNEL_PROGRAM_ELAPSED:
                    Duration elapsed = Duration.between(programme.getProgrammeStart(), Instant.now());
                    long secondsElapsed = elapsed.toMillis() / 1000;
                    updateState(channelUID, new QuantityType<>(secondsElapsed, SmartHomeUnits.SECOND));
                    break;
                case CHANNEL_PROGRAM_REMAINING:
                    Duration remaining = Duration.between(Instant.now(), programme.getProgrammeStop());
                    long secondsRemaining = remaining.toMillis() / 1000;
                    updateState(channelUID, new QuantityType<>(secondsRemaining, SmartHomeUnits.SECOND));
                    break;
                case CHANNEL_PROGRAM_TIMELEFT:
                    Duration remaining1 = Duration.between(Instant.now(), programme.getProgrammeStart());
                    long secondsRemaining1 = remaining1.toMillis() / 1000;
                    updateState(channelUID, new QuantityType<>(secondsRemaining1, SmartHomeUnits.SECOND));
                    break;
                case CHANNEL_PROGRAM_PROGRESS:
                    Duration totalLength = Duration.between(programme.getProgrammeStart(),
                            programme.getProgrammeStop());
                    Duration elapsed1 = Duration.between(programme.getProgrammeStart(), Instant.now());

                    long secondsElapsed1 = elapsed1.toMillis() / 1000;
                    long secondsLength = totalLength.toMillis() / 1000;

                    double progress = 100.0 * secondsElapsed1 / secondsLength;
                    if (progress > 100 || progress < 0) {
                        logger.warn("Outstanding process");
                    }
                    updateState(channelUID, new QuantityType<>(progress, SmartHomeUnits.PERCENT));

                    break;
            }
        }
    }

    private RawType downloadIcon(List<Icon> icons) {
        if (icons.size() > 0) {
            String url = icons.get(0).getSrc();
            return HttpUtil.downloadImage(url);
        }
        return new RawType(new byte[0], RawType.DEFAULT_MIME_TYPE);
    }

    private void setMediaChannel(MediaChannel mediaChannel) {
        this.mediaChannel = mediaChannel;
        mediaIcon = downloadIcon(mediaChannel.getIcons());
    }

}
