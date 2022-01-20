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
import org.openhab.binding.xmltv.internal.configuration.XmlChannelConfiguration;
import org.openhab.binding.xmltv.internal.jaxb.Icon;
import org.openhab.binding.xmltv.internal.jaxb.MediaChannel;
import org.openhab.binding.xmltv.internal.jaxb.Programme;
import org.openhab.binding.xmltv.internal.jaxb.WithLangType;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    private @Nullable ScheduledFuture<?> globalJob;
    private @Nullable MediaChannel mediaChannel;
    private State mediaIcon = UnDefType.UNDEF;

    public final List<Programme> programmes = new ArrayList<>();
    private final ZoneId zoneId;

    public ChannelHandler(Thing thing, ZoneId zoneId) {
        super(thing);
        this.zoneId = zoneId;
    }

    @Override
    public void initialize() {
        XmlChannelConfiguration config = getConfigAs(XmlChannelConfiguration.class);

        logger.debug("Initializing Broadcast Channel handler for uid '{}'", getThing().getUID());

        ScheduledFuture<?> job = globalJob;
        if (job == null || job.isCancelled()) {
            globalJob = scheduler.scheduleWithFixedDelay(() -> {
                if (programmes.size() < 2) {
                    refreshProgramList();
                }
                if (programmes.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/no-more-programs");
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
                handler.getXmlFile().ifPresentOrElse(tv -> {
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
                }, () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/no-file-available"));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = globalJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
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
        // TODO : usage extraction of groupname
        String[] uidElements = channelUID.getId().split("#");
        if (uidElements.length == 2) {
            int target = GROUP_NEXT_PROGRAMME.equals(uidElements[0]) ? 1 : 0;
            if (programmes.size() > target) {
                Programme programme = programmes.get(target);

                switch (uidElements[1]) {
                    case CHANNEL_ICON:
                        State icon = GROUP_CHANNEL_PROPERTIES.equals(uidElements[0]) ? mediaIcon
                                : downloadIcon(programme.getIcons());
                        updateState(channelUID, icon);
                        break;
                    case CHANNEL_CHANNEL_URL:
                        MediaChannel channel = mediaChannel;
                        updateState(channelUID,
                                channel != null ? !channel.getIcons().isEmpty()
                                        ? new StringType(channel.getIcons().get(0).getSrc())
                                        : UnDefType.UNDEF : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_START:
                        updateDateTimeChannel(channelUID, programme.getProgrammeStart());
                        break;
                    case CHANNEL_PROGRAMME_END:
                        updateDateTimeChannel(channelUID, programme.getProgrammeStop());
                        break;
                    case CHANNEL_PROGRAMME_TITLE:
                        List<WithLangType> titles = programme.getTitles();
                        updateState(channelUID,
                                !titles.isEmpty() ? new StringType(programme.getTitles().get(0).getValue())
                                        : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_CATEGORY:
                        List<WithLangType> categories = programme.getCategories();
                        updateState(channelUID,
                                !categories.isEmpty() ? new StringType(programme.getCategories().get(0).getValue())
                                        : UnDefType.UNDEF);
                        break;
                    case CHANNEL_PROGRAMME_ICON:
                        List<Icon> icons = programme.getIcons();
                        updateState(channelUID,
                                !icons.isEmpty() ? new StringType(icons.get(0).getSrc()) : UnDefType.UNDEF);
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
                        long totalLength = Duration.between(programme.getProgrammeStart(), programme.getProgrammeStop())
                                .toSeconds();
                        long elapsed1 = Duration.between(programme.getProgrammeStart(), Instant.now()).toSeconds();

                        double progress = 100.0 * elapsed1 / totalLength;
                        if (progress > 100 || progress < 0) {
                            logger.debug("Outstanding process");
                        }
                        updateState(channelUID, new QuantityType<>((int) progress, Units.PERCENT));

                        break;
                }
            } else {
                logger.warn("Not enough programs in XML file, think to refresh it");
            }
        }
    }

    private void updateDateTimeChannel(ChannelUID channelUID, Instant instant) {
        ZonedDateTime zds = ZonedDateTime.ofInstant(instant, zoneId);
        updateState(channelUID, new DateTimeType(zds));
    }

    private QuantityType<?> getDurationInSeconds(Instant from, Instant to) {
        long elapsed = Duration.between(from, to).toSeconds();
        return new QuantityType<>(elapsed, Units.SECOND);
    }

    private State downloadIcon(List<Icon> icons) {
        if (!icons.isEmpty()) {
            String url = icons.get(0).getSrc();
            RawType result = HttpUtil.downloadImage(url);
            if (result != null) {
                return result;
            }
        }
        return UnDefType.NULL;
    }
}
