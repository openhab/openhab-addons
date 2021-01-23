/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vdr.internal;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vdr.internal.svdrp.SVDRPChannel;
import org.openhab.binding.vdr.internal.svdrp.SVDRPClient;
import org.openhab.binding.vdr.internal.svdrp.SVDRPClientImpl;
import org.openhab.binding.vdr.internal.svdrp.SVDRPConnectionException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPDiskStatus;
import org.openhab.binding.vdr.internal.svdrp.SVDRPEpgEvent;
import org.openhab.binding.vdr.internal.svdrp.SVDRPException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPParseResponseException;
import org.openhab.binding.vdr.internal.svdrp.SVDRPVolume;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
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
 * The {@link VDRHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class VDRHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VDRHandler.class);

    private VDRConfiguration config = new VDRConfiguration();

    private @Nullable ScheduledFuture<?> refreshThreadFuture = null;

    public VDRHandler(Thing thing) {
        super(thing);
    }

    /**
     * when disposing refresh thread has to be cancelled
     */
    @Override
    public void dispose() {
        stopRefreshThread(true);
    }

    /**
     * Initialization of {@link VDRHandler}
     */
    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        config = getConfigAs(VDRConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        // background initialization:
        scheduler.execute(() -> {
            stopRefreshThread(true);

            SVDRPClient con = new SVDRPClientImpl(config.getHost(), config.getPort());
            try {
                con.openConnection();
                updateStatus(ThingStatus.ONLINE);
                updateProperties(con);
                con.closeConnection();
            } catch (SVDRPException se) {
                logger.trace("VDR not online: Thing {}, Message: {}", this.getThing().getUID(), se.getMessage());
                // also update power channel when thing is initially offline
                thing.getChannels().stream().map(c -> c.getUID()).filter(this::isLinked).forEach(channelUID -> {
                    try {
                        if ("power".equals(channelUID.getIdWithoutGroup())) {
                            updateState(channelUID, OnOffType.OFF);
                        }
                    } catch (Exception e) {
                        logger.trace("VDR Power Update for Thing {}, ChannelUID {} failed. ErrorMessage: {}",
                                this.getThing().getUID(), channelUID, e.getMessage());
                    }
                });
                updateStatus(ThingStatus.OFFLINE);
            }

            scheduleRefreshThread();
        });

        logger.debug("Finished initializing!");
    }

    /**
     * Update Thing's properties (e. g. VDR Version)
     *
     * @param client the {@link SVDRPClient} to be used for properties update
     */
    public void updateProperties(SVDRPClient client) {
        Map<String, String> properties = editProperties();
        // set vdr version to properties of thing
        String version = client.getSVDRPVersion();
        properties.put("version", version.toString());

        // persist changes only if there are any changes.
        if (!editProperties().equals(properties)) {
            updateProperties(properties);
        }
    }

    /**
     * Handling Commands for {@link VDRHandler}
     *
     * @param channelUID channel command was executed for
     * @param command command to execute
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        SVDRPClient con = new SVDRPClientImpl(config.getHost(), config.getPort());
        try {
            con.openConnection();

            if (command instanceof RefreshType) {
                this.onVDRRefresh();
            } else {
                State result = UnDefType.NULL;
                String cmd = command.toString();
                switch (channelUID.getId()) {
                    case "message":
                        con.sendSVDRPMessage(cmd);
                        break;
                    case "power":
                        con.sendSVDRPKey(cmd);
                        break;
                    case "channel":
                        SVDRPChannel channel = con.setSVDRPChannel(Integer.parseInt(cmd));
                        result = new DecimalType(channel.getNumber());
                        updateState(channelUID, result);
                        break;
                    case "volume":
                        SVDRPVolume volume = con.setSVDRPVolume(Integer.parseInt(cmd));
                        result = new PercentType(volume.getVolume());
                        updateState(channelUID, result);
                        break;
                    case "keyCode":
                        con.sendSVDRPKey(cmd.trim());
                        break;
                }
            }
        } catch (SVDRPParseResponseException e) {
            logger.trace("VDR handleCommand for Thing {}, ChannelUID {}, Parse Response failed. Message: {}",
                    this.getThing().getUID(), channelUID, e.getMessage());
        } catch (SVDRPConnectionException e) {
            logger.debug("VDR handleCommand for Thing {}, ChannelUID {}, Connection failed. Message: {}",
                    this.getThing().getUID(), channelUID, e.getMessage());
        } catch (

        Exception ex) {
            logger.error("There is something wrong with your thing '{}', please check or recreate it: {}",
                    thing.getUID(), ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
        } finally {
            try {
                con.closeConnection();
            } catch (SVDRPException ex) {
                logger.trace("Error on VDR handleCommand while closing SVDRP Connection for Thing : {} with message {}",
                        this.getThing().getUID(), ex.getMessage());
            }
        }
    }

    /**
     * Refresh data from SVDRPClient (Polling)
     */
    private void onVDRRefresh() {
        SVDRPClient con = new SVDRPClientImpl(config.getHost(), config.getPort());
        Thing thing = getThing();
        try {
            con.openConnection();
            if (thing.getStatus() == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.ONLINE);
                updateProperties(con);
            }
            thing.getChannels().stream().map(c -> c.getUID()).filter(this::isLinked).forEach(channelUID -> {
                try {
                    logger.trace("updateChannel: {}", channelUID);

                    SVDRPEpgEvent entry;
                    State result = UnDefType.NULL;

                    switch (channelUID.getId()) {
                        case "recording":
                            boolean isRecording = con.isRecordingActive();
                            if (isRecording) {
                                result = OnOffType.ON;
                            } else {
                                result = OnOffType.OFF;
                            }
                            break;
                        case "volume":
                            SVDRPVolume volume = con.getSVDRPVolume();
                            result = new PercentType(volume.getVolume());
                            break;
                        case "channel":
                            SVDRPChannel channel = con.getCurrentSVDRPChannel();
                            result = new DecimalType(channel.getNumber());
                            break;
                        case "channelName":
                            SVDRPChannel svdrpChannel = con.getCurrentSVDRPChannel();
                            result = new StringType(svdrpChannel.getName());
                            break;
                        case "power":
                            SVDRPDiskStatus status = con.getDiskStatus();
                            if (status.getPercentUsed() >= 0) {
                                result = OnOffType.ON;
                            } else {
                                result = OnOffType.OFF;
                            }
                            break;
                        case "diskUsage":
                            SVDRPDiskStatus diskStatus = con.getDiskStatus();
                            result = new DecimalType(diskStatus.getPercentUsed());
                            break;
                        case "currentEventTitle":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NOW);
                            result = new StringType(entry.getTitle());
                            break;
                        case "currentEventSubTitle":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NOW);
                            result = new StringType(entry.getSubtitle());
                            break;
                        case "currentEventDuration":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NOW);
                            result = new DecimalType(entry.getDuration());
                            break;
                        case "currentEventBegin":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NOW);
                            result = new DateTimeType(LocalDateTime
                                    .ofInstant(entry.getBegin().toInstant(), TimeZone.getDefault().toZoneId())
                                    .atZone(TimeZone.getDefault().toZoneId()));
                            break;
                        case "currentEventEnd":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NOW);
                            result = new DateTimeType(LocalDateTime
                                    .ofInstant(entry.getEnd().toInstant(), TimeZone.getDefault().toZoneId())
                                    .atZone(TimeZone.getDefault().toZoneId()));
                            break;
                        case "nextEventTitle":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NEXT);
                            result = new StringType(entry.getTitle());
                            break;
                        case "nextEventSubTitle":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NEXT);
                            result = new StringType(entry.getSubtitle());
                            break;
                        case "nextEventDuration":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NEXT);
                            result = new DecimalType(entry.getDuration());
                            break;
                        case "nextEventBegin":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NEXT);
                            result = new DateTimeType(LocalDateTime
                                    .ofInstant(entry.getBegin().toInstant(), TimeZone.getDefault().toZoneId())
                                    .atZone(TimeZone.getDefault().toZoneId()));
                            break;
                        case "nextEventEnd":
                            entry = con.getEpgEvent(SVDRPEpgEvent.TYPE.NEXT);
                            result = new DateTimeType(LocalDateTime
                                    .ofInstant(entry.getEnd().toInstant(), TimeZone.getDefault().toZoneId())
                                    .atZone(TimeZone.getDefault().toZoneId()));
                            break;

                    }
                    updateState(channelUID, result);

                } catch (SVDRPParseResponseException e) {
                    logger.trace("VDR Refresh for Thing {}, ChannelUID {}, Parse Response failed. Message: {}",
                            this.getThing().getUID(), channelUID, e.getMessage());
                    updateState(channelUID, UnDefType.UNDEF);
                } catch (SVDRPConnectionException e) {
                    logger.debug("VDR Refresh for Thing {}, ChannelUID {}, Connection failed. Message: {}",
                            this.getThing().getUID(), channelUID, e.getMessage());
                }

            });

        } catch (SVDRPException ce) {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                // also update power channel when thing is offline before setting it offline
                thing.getChannels().stream().map(c -> c.getUID()).filter(this::isLinked).forEach(channelUID -> {
                    try {
                        if ("power".equals(channelUID.getIdWithoutGroup())) {
                            updateState(channelUID, OnOffType.OFF);
                        }
                    } catch (Exception e) {
                        logger.warn("VDR Refresh for Thing {}, ChannelUID {} failed. ErrorMessage: {}",
                                this.getThing().getUID(), channelUID, e.getMessage());
                    }
                });
            }
            updateStatus(ThingStatus.OFFLINE);

        } catch (Exception ex) {
            logger.error("There is something wrong with your thing '{}', please check or recreate it: {}",
                    thing.getUID(), ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
        } finally {
            try {
                con.closeConnection();
            } catch (SVDRPException e) {
                logger.trace("Error on VDR Refresh while closing SVDRP Connection for Thing : {} with message {}",
                        this.getThing().getUID(), e.getMessage());
            }
        }
    }

    /**
     * Schedules the refresh thread
     */
    private void scheduleRefreshThread() {
        refreshThreadFuture = scheduler.scheduleWithFixedDelay(this::onVDRRefresh, 3, config.getRefresh(),
                TimeUnit.SECONDS);
    }

    /**
     * Stops the refresh thread.
     *
     * @param force if set to true thread cancellation will be forced
     */
    @SuppressWarnings({ "null" })
    private void stopRefreshThread(boolean force) {
        if (refreshThreadFuture != null) {
            refreshThreadFuture.cancel(force);
        }
    }
}
