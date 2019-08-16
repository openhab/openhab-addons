/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.xmltv.internal.handler;

import static org.openhab.binding.xmltv.internal.XmlTVBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.xmltv.internal.configuration.XmlChannelConfiguration;
import org.openhab.binding.xmltv.internal.jaxb.Icon;
import org.openhab.binding.xmltv.internal.jaxb.MediaChannel;
import org.openhab.binding.xmltv.internal.jaxb.Programme;
import org.openhab.binding.xmltv.internal.jaxb.Tv;
import org.openhab.binding.xmltv.internal.jaxb.WithLangType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ChannelHandler} is responsible for handling information
 * made available in regard of the channel and current program
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ChannelHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ChannelHandler.class);

    private @NonNullByDefault({}) ScheduledFuture<?> globalJob;
    private @Nullable MediaChannel mediaChannel;
    private @Nullable RawType mediaIcon = new RawType(new byte[0], RawType.DEFAULT_MIME_TYPE);

    public final List<Programme> programmes = new ArrayList<>();

    public ChannelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        XmlChannelConfiguration config = getConfigAs(XmlChannelConfiguration.class);

        logger.debug("Initializing Broadcast Channel handler for uid '{}'", getThing().getUID());

        if (globalJob == null || globalJob.isCancelled()) {
            globalJob = scheduler.scheduleWithFixedDelay(() -> {
                if (programmes.size() < 2) {
                    refreshProgramList();
                }
                if (programmes.size() == 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "No programmes to come in the current XML file for this channel");
                } else if (Instant.now().isAfter(programmes.get(0).getProgrammeStop())) {
                    programmes.remove(0);
                }

                getThing().getChannels().forEach(channel -> updateChannel(channel.getUID()));

            }, 3, config.refresh, TimeUnit.SECONDS);
        }
    }

    private void refreshProgramList() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            XmlTVHandler handler = (XmlTVHandler) bridge.getHandler();
            if (handler != null) {
                Tv tv = handler.getXmlFile();
                if (tv != null) {
                    String channelId = (String) getConfig().get(XmlChannelConfiguration.CHANNEL_ID);

                    if (mediaChannel == null) {
                        Optional<MediaChannel> channel = tv.getMediaChannels().stream()
                                .filter(mediaChannel -> mediaChannel.getId().equals(channelId)).findFirst();
                        if (channel.isPresent()) {
                            mediaChannel = channel.get();
                            mediaIcon = downloadIcon(mediaChannel.getIcons());
                        }
                    }

                    programmes.clear();
                    tv.getProgrammes().stream().filter(
                            p -> p.getChannel().equals(channelId) && p.getProgrammeStop().isAfter(Instant.now()))
                            .forEach(p -> programmes.add(p));

                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No file available");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} for {}", command, channelUID);
        if (command == RefreshType.REFRESH) {
            refreshProgramList();
        }
    }

    /**
     * Update the channel from the last OpenUV data retrieved
     *
     * @param channelUID the id identifying the channel to be updated
     *
     */
    private void updateChannel(ChannelUID channelUID) {
        String[] uidElements = channelUID.getId().split("#");
        if (uidElements.length == 2) {
            int target = GROUP_NEXT_PROGRAMME.equals(uidElements[0]) ? 1 : 0;
            if (programmes.size() > target) {
                Programme programme = programmes.get(target);

                switch (uidElements[1]) {
                    case CHANNEL_ICON:
                        State icon = null;
                        if (GROUP_CHANNEL_PROPERTIES.equals(uidElements[0])) {
                            icon = mediaIcon;
                        } else {
                            icon = downloadIcon(programme.getIcons());
                        }
                        updateState(channelUID, icon != null ? icon : UnDefType.UNDEF);
                        break;
                    case CHANNEL_CHANNEL_URL:
                        updateState(channelUID,
                                mediaChannel != null ? mediaChannel.getIcons().size() > 0
                                        ? new StringType(mediaChannel.getIcons().get(0).getSrc())
                                        : UnDefType.UNDEF : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_START:
                        Instant is = programme.getProgrammeStart();
                        ZonedDateTime zds = ZonedDateTime.ofInstant(is, ZoneId.systemDefault());
                        updateState(channelUID, new DateTimeType(zds));
                        break;
                    case CHANNEL_PROGRAMME_END:
                        ZonedDateTime zde = ZonedDateTime.ofInstant(programme.getProgrammeStop(),
                                ZoneId.systemDefault());
                        updateState(channelUID, new DateTimeType(zde));
                        break;
                    case CHANNEL_PROGRAMME_TITLE:
                        List<WithLangType> titles = programme.getTitles();
                        updateState(channelUID,
                                titles.size() > 0 ? new StringType(programme.getTitles().get(0).getValue())
                                        : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_CATEGORY:
                        List<WithLangType> categories = programme.getCategories();
                        updateState(channelUID,
                                categories.size() > 0 ? new StringType(programme.getCategories().get(0).getValue())
                                        : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_ICON:
                        List<Icon> icons = programme.getIcons();
                        updateState(channelUID,
                                icons.size() > 0 ? new StringType(icons.get(0).getSrc()) : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_ELAPSED:
                        updateState(channelUID, getDurationInSeconds(programme.getProgrammeStart(), Instant.now()));
                        break;
                    case CHANNEL_PROGRAMME_REMAINING:
                        updateState(channelUID, getDurationInSeconds(Instant.now(), programme.getProgrammeStop()));
                        break;
                    case CHANNEL_PROGRAMME_TIMELEFT:
                        updateState(channelUID, getDurationInSeconds(Instant.now(), programme.getProgrammeStart()));
                        break;
                    case CHANNEL_PROGRAMME_PROGRESS:
                        Duration totalLength = Duration.between(programme.getProgrammeStart(),
                                programme.getProgrammeStop());
                        Duration elapsed1 = Duration.between(programme.getProgrammeStart(), Instant.now());

                        long secondsElapsed1 = elapsed1.toMillis() / 1000;
                        long secondsLength = totalLength.toMillis() / 1000;

                        double progress = 100.0 * secondsElapsed1 / secondsLength;
                        if (progress > 100 || progress < 0) {
                            logger.debug("Outstanding process");
                        }
                        updateState(channelUID, new QuantityType<>(progress, SmartHomeUnits.PERCENT));

                        break;
                }
            } else {
                logger.warn("Not enough programs in XML file, think to refresh it");
            }
        }
    }

    private QuantityType<?> getDurationInSeconds(Instant from, Instant to) {
        Duration elapsed = Duration.between(from, to);
        long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(elapsed.toMillis());
        return new QuantityType<>(secondsElapsed, SmartHomeUnits.SECOND);
    }

    private @Nullable RawType downloadIcon(List<Icon> icons) {
        if (icons.size() > 0) {
            String url = icons.get(0).getSrc();
            return HttpUtil.downloadImage(url);
        }
        return null;
    }

}
