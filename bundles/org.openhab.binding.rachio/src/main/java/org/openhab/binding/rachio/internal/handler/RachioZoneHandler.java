/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioZoneHandler} is responsible for handling commands, which are
 * sent to one of the zone channels.
 *
 * @author Markus Michels - Initial contribution
 */

@NonNullByDefault
public class RachioZoneHandler extends AbstractRachioThingHandler {
    private static final int MAX_ZONE_IMAGE_SIZE_BYTES = 5_000_000;

    private final Logger logger = LoggerFactory.getLogger(RachioZoneHandler.class);
    private OnOffType zoneRunState = OnOffType.OFF;
    private @Nullable RachioDevice dev;
    private @Nullable RachioZone zone;
    private String cachedImageUrl = "";
    private @Nullable RawType cachedImage;
    private String failedImageUrl = "";

    public RachioZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        String configuredZoneId = getThingConfigurationString(PROPERTY_ZONE_ID);
        logger.debug("Zone initialize entered: thingUid={}, bridgeUid={}, configured zoneId='{}'", getThing().getUID(),
                getThing().getBridgeUID(), configuredZoneId);

        try {
            if (initializeCloudHandler()) {
                RachioBridgeHandler handler = cloudHandler;
                Bridge currentBridge = bridge;
                logger.debug("Zone parent bridge resolved: thingUid={}, bridgeUid={}, bridgeOnline={}",
                        getThing().getUID(), currentBridge != null ? currentBridge.getUID() : null, isBridgeOnline());
                zone = handler != null ? handler.getZoneByThing(this.getThing()) : null;
                RachioZone z = zone;
                if (z != null && handler != null) {
                    z.setThingHandler(this);
                    dev = handler.getDevForZone(z);
                    RachioDevice d = dev;
                    if (d != null) {
                        thingId = d.name + "[" + z.zoneNumber + "]";
                        logger.debug(
                                "Zone model lookup succeeded: thingUid={}, zoneId='{}', zoneName='{}', controllerId='{}'",
                                getThing().getUID(), z.id, z.name, d.id);
                    } else {
                        logger.debug("Zone model lookup found zoneId='{}' for Thing '{}' but no parent controller",
                                z.id, getThing().getUID());
                    }
                } else {
                    logger.debug("Zone model lookup failed: thingUid={}, configured zoneId='{}'", getThing().getUID(),
                            configuredZoneId);
                }
            } else {
                logger.debug("Zone parent bridge is not available: thingUid={}, bridgeUid={}", getThing().getUID(),
                        getThing().getBridgeUID());
            }
            if (bridge == null || cloudHandler == null || dev == null || zone == null) {
                String errorMessage = buildZoneResolutionError(configuredZoneId);
                logger.debug("{}: {}", thingId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
                return;
            }

            // listen to bridge events
            registerStatusListener();
            if (!isBridgeOnline()) {
                logger.debug("{}: Bridge is offline!", thingId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                logger.debug("{}: Zone status set ONLINE", thingId);
                goOnline();
                return;
            }
        } catch (RuntimeException e) {
            logger.debug("{}: Initialization failed", thingId, e);
        }

        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*
         * Note: if communication with thing fails for some reason,
         * indicate that by setting the status with detail information
         * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
         */
        String channel = channelUID.getId();
        logger.debug("Handle command {} for {}", command.toString(), channelUID.getAsString());
        RachioBridgeHandler handler = cloudHandler;
        RachioZone currentZone = zone;
        RachioDevice currentDev = dev;
        if (handler == null || currentZone == null || currentDev == null) {
            logger.debug("{}: Cloud handler or device not initialized!", thingId);
            return;
        }

        String errorMessage = "";
        try {
            if (command == RefreshType.REFRESH) {
                if (handleRefreshCommand(channel)) {
                    logger.debug("{}: Return cached data for channel {}: {}", thingId, channel,
                            channelData.get(channel));
                }
                return;
            }

            if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_ENABLED)) {
                if (command instanceof OnOffType) {
                    boolean enabled = command == OnOffType.ON;
                    logger.info("{} zone '{} [{}]'", enabled ? "Enabling" : "Disabling", currentZone.name,
                            currentZone.zoneNumber);
                    handler.setZoneEnabled(currentZone.id, enabled);
                    currentZone.setEnabled(enabled);
                    updateChannel(CHANNEL_ZONE_ENABLED, currentZone.getEnabled());
                    updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_RUN)) {
                if (command == OnOffType.ON) {
                    int runtime = currentZone.getStartRunTime();
                    logger.debug("{}: Starting zone {} for {} sec", thingId, currentZone.name, runtime);
                    if (runtime == 0) {
                        runtime = handler.getDefaultRuntime();
                        logger.debug("{}: Starting zone {} with default runtime ({} sec);", thingId, currentZone.name,
                                runtime);
                    }
                    handler.startZone(currentZone.id, runtime);
                } else {
                    logger.debug("{}: Stop watering for the device", thingId);
                    handler.stopWatering(currentDev.id);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_RUNTIME)) {
                RachioQuantityTypes.durationSeconds(command).ifPresentOrElse(runtime -> {
                    logger.debug("{}: Zone {} will start for {} sec", thingId, currentZone.name, runtime);
                    currentZone.setStartRunTime(runtime);
                }, () -> logger.debug("{}: Zone runtime command value is not a duration: {}", thingId, command));
            } else if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_MOISTURE_LEVEL)) {
                OptionalDouble moistureLevel = RachioQuantityTypes.lengthMillimeters(command);
                if (moistureLevel.isPresent()) {
                    double level = moistureLevel.getAsDouble();
                    logger.debug("{}: Updating zone '{}' moisture level to {}", thingId, currentZone.name, level);
                    handler.setZoneMoistureLevel(currentZone.id, level);
                    currentZone.setMoistureLevel(level);
                    updateChannel(CHANNEL_ZONE_MOISTURE_LEVEL, RachioQuantityTypes.millimetersOrUndef(level));
                    updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
                } else {
                    logger.debug("{}: Moisture level command value is not a length: {}", thingId, command);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_MOISTURE_PERCENT)) {
                OptionalDouble moisturePercent = RachioQuantityTypes.dimensionless(command);
                if (moisturePercent.isPresent()) {
                    double percent = moisturePercent.getAsDouble();
                    if (percent < 0 || percent > 1) {
                        logger.debug("{}: Invalid moisture percent {}; expected range is 0..1", thingId, percent);
                        return;
                    }
                    logger.debug("{}: Updating zone '{}' moisture percent to {}", thingId, currentZone.name, percent);
                    handler.setZoneMoisturePercent(currentZone.id, percent);
                    currentZone.setMoisturePercent(percent);
                    updateChannel(CHANNEL_ZONE_MOISTURE_PERCENT, RachioQuantityTypes.fractionOrUndef(percent));
                    updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
                } else {
                    logger.debug("{}: Moisture percent command value is not dimensionless: {}", thingId, command);
                }
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            errorMessage = message != null ? message : e.toString();
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.warn("{}: Zone command failed: {}", thingId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    private String buildZoneResolutionError(String configuredZoneId) {
        if (configuredZoneId.isBlank()) {
            return "Unable to resolve Rachio zone for Thing '" + getThing().getUID()
                    + "': no zoneId is configured and no legacy UID/property mapping matched. The zoneId must be the Rachio zone UUID.";
        }
        return "Unable to resolve Rachio zone for Thing '" + getThing().getUID() + "' using configured zoneId '"
                + configuredZoneId + "'.";
    }

    private boolean rebindToCurrentBridgeModel(String reason) {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null && !initializeCloudHandler()) {
            return false;
        }
        handler = cloudHandler;
        if (handler == null) {
            return false;
        }
        RachioZone currentZone = handler.getZoneByThing(getThing());
        if (currentZone == null) {
            return false;
        }
        RachioDevice currentDev = handler.getDevForZone(currentZone);
        if (currentDev == null) {
            return false;
        }
        return bindResolvedModel(currentDev, currentZone, reason);
    }

    boolean rebindToCurrentModel(RachioDevice currentDev, RachioZone currentZone, String reason) {
        if (!handlesZone(currentZone)) {
            return false;
        }
        return bindResolvedModel(currentDev, currentZone, reason);
    }

    boolean handlesZone(RachioZone candidateZone) {
        RachioZone currentZone = zone;
        if (currentZone != null && currentZone.id.equalsIgnoreCase(candidateZone.id)) {
            return true;
        }
        try {
            String configuredZoneId = getThingConfigurationOrPropertyString(PROPERTY_ZONE_ID);
            return !configuredZoneId.isBlank() && configuredZoneId.equalsIgnoreCase(candidateZone.id);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private boolean bindResolvedModel(RachioDevice currentDev, RachioZone currentZone, String reason) {
        RachioDevice previousDev = dev;
        RachioZone previousZone = zone;
        boolean changed = !Objects.equals(previousDev, currentDev) || !Objects.equals(previousZone, currentZone)
                || !Objects.equals(currentZone.getThingHandler(), this);

        currentZone.setThingHandler(this);
        dev = currentDev;
        zone = currentZone;
        thingId = currentDev.name + "[" + currentZone.zoneNumber + "]";
        if (changed) {
            logger.debug(
                    "{}: Rebound zone Thing '{}' to current bridge model for zoneId='{}' after {}; direct webhook handling can use this handler",
                    thingId, getThing().getUID(), currentZone.id, reason);
        }
        return true;
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        RachioZone z = zone;
        if (updatedZone != null && (z == null || !z.id.equals(updatedZone.id)) && handlesZone(updatedZone)) {
            rebindToCurrentBridgeModel("zone state update");
            z = zone;
        }
        if (updatedZone != null && z != null && z.id.equals(updatedZone.id)) {
            logger.debug("{}: Update for zone {} received.", thingId, z.name);
            z.update(updatedZone);
            updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
            postChannelData();
            updateResolvedZoneThingStatusAfterSuccessfulCommunication();
            return true;
        }
        return false;
    }

    void refreshThingStatusAfterSuccessfulCommunication() {
        rebindToCurrentBridgeModel("successful cloud poll");
        RachioDevice d = dev;
        RachioZone z = zone;
        if (!isBridgeOnline() || d == null || z == null) {
            return;
        }
        logger.debug("{}: Refreshing resolved zone Thing status after successful cloud poll, zoneId='{}'", thingId,
                z.id);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void onConfigurationUpdated() {
        if (rebindToCurrentBridgeModel("bridge configuration update") && isBridgeOnline()) {
            goOnline();
        }
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        boolean update = false;
        RachioZone z = zone;
        if (z == null) {
            return false;
        }
        try {
            String zoneName = getZoneDisplayName(event, z);
            String zoneLogSubject = getZoneLogSubject(zoneName);
            String evt = event.subType.isEmpty() ? event.type : event.subType;
            z.setEvent(evt, getTimestamp()); // and funnel all zone events to the device
            if ("ZONE_STATUS".equals(event.type)) {
                String state = event.getZoneRunStateForWebhookHandling();
                if ("ZONE_STARTED".equals(state)) {
                    logger.info("{}: {} STARTED watering ({}).", thingId, zoneLogSubject, event.timestamp);
                    zoneRunState = OnOffType.ON;
                    updateChannel(CHANNEL_ZONE_RUN, zoneRunState);
                } else if ("ZONE_STOPPED".equals(state) || "ZONE_COMPLETED".equals(state)) {
                    logger.info(
                            "{}: {} STOPPED watering (timestamp={}, current={}, duration={}sec/{}min, flowVolume={}).",
                            thingId, zoneLogSubject, event.timestamp, event.zoneCurrent, event.duration,
                            event.durationInMinutes, event.flowVolume);
                    zoneRunState = OnOffType.OFF;
                    long lastWateredDate = firstTimestampMillis(event.endTime, event.timestamp);
                    if (lastWateredDate >= 0) {
                        z.lastWateredDate = lastWateredDate;
                    }
                    updateChannel(CHANNEL_ZONE_RUN, zoneRunState);
                } else {
                    logger.info("{}: Event for zone {}: {} (status={}, duration = {}sec)", thingId, zoneName,
                            event.summary, state, event.duration);
                }
                update = true;
            } else if ("ZONE_DELTA".equals(event.subType)) {
                logger.info("{}: DELTA Event for zone {}: {}.{}", thingId, z.name, event.category, event.action);
                update = true;
            } else {
                logger.debug("{}: Unhandled event type {}.{} for zone {}", thingId, event.type, event.subType,
                        zoneName);
            }

            if (update) {
                updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
                postChannelData();
            }
        } catch (RuntimeException e) {
            logger.debug("{}: Unable to process event", thingId, e);
        }

        return update;
    }

    private String getZoneDisplayName(RachioEventGsonDTO event, RachioZone zone) {
        if (!event.zoneName.isBlank()) {
            return event.zoneName;
        }
        if (!zone.name.isBlank()) {
            return zone.name;
        }
        int zoneNumber = event.getZoneNumberForWebhookHandling();
        if (zoneNumber <= 0) {
            zoneNumber = zone.zoneNumber;
        }
        return zoneNumber > 0 ? Integer.toString(zoneNumber) : "unknown";
    }

    private String getZoneLogSubject(String zoneName) {
        if (zoneName.toLowerCase(Locale.ROOT).startsWith("zone ")) {
            return zoneName;
        }
        return "Zone " + zoneName;
    }

    private long firstTimestampMillis(String... values) {
        for (String value : values) {
            if (!value.isBlank()) {
                try {
                    return Instant.parse(value).toEpochMilli();
                } catch (RuntimeException e) {
                    logger.trace("{}: Ignoring unparsable zone webhook timestamp '{}'", thingId, value);
                }
            }
        }
        return -1;
    }

    @Override
    protected void postChannelData() {
        RachioZone z = zone;
        if (z != null) {
            updateChannel(CHANNEL_ZONE_NAME, new StringType(z.name));
            updateChannel(CHANNEL_ZONE_NUMBER, new DecimalType(new BigDecimal(z.zoneNumber).toString()));
            updateChannel(CHANNEL_ZONE_ENABLED, z.getEnabled());
            updateChannel(CHANNEL_ZONE_RUN, zoneRunState);
            updateChannel(CHANNEL_ZONE_RUNTIME, RachioQuantityTypes.seconds(z.getStartRunTime()));
            updateChannel(CHANNEL_ZONE_RUN_TOTAL, RachioQuantityTypes.seconds(z.runtime));
            updateChannel(CHANNEL_ZONE_AVAILABLE_WATER, RachioQuantityTypes.inchesOrNull(z.availableWater));
            updateChannel(CHANNEL_ZONE_IMAGEURL, new StringType(z.imageUrl));
            updateZoneImageChannel(z);
            updateChannel(CHANNEL_ZONE_DEPTH_OF_WATER, RachioQuantityTypes.inchesOrNull(z.depthOfWater));
            updateChannel(CHANNEL_ZONE_SATURATED_DEPTH_OF_WATER,
                    RachioQuantityTypes.inchesOrNull(z.saturatedDepthOfWater));
            updateChannel(CHANNEL_ZONE_MANAGEMENT_ALLOWED_DEPLETION,
                    RachioQuantityTypes.fractionOrNull(z.managementAllowedDepletion));
            updateChannel(CHANNEL_ZONE_ROOT_ZONE_DEPTH, RachioQuantityTypes.inchesOrNull(z.rootZoneDepth));
            updateChannel(CHANNEL_ZONE_EFFICIENCY, RachioQuantityTypes.fractionOrNull(z.efficiency));
            updateChannel(CHANNEL_ZONE_YARD_AREA_SQUARE_FEET, RachioQuantityTypes.squareFeet(z.yardAreaSquareFeet));
            updateChannel(CHANNEL_ZONE_LAST_WATERED_DATE, epochMillisOrNull(z.lastWateredDate));
            updateChannel(CHANNEL_ZONE_FIXED_RUNTIME, RachioQuantityTypes.seconds(z.fixedRuntime));
            updateChannel(CHANNEL_ZONE_MAX_RUNTIME, RachioQuantityTypes.seconds(z.maxRuntime));
            updateChannel(CHANNEL_ZONE_RUNTIME_NO_MULTIPLIER, RachioQuantityTypes.seconds(z.runtimeNoMultiplier));
            updateChannel(CHANNEL_ZONE_SCHEDULE_DATA_MODIFIED, z.scheduleDataModified ? OnOffType.ON : OnOffType.OFF);
            if (!Double.isNaN(z.getMoistureLevel())) {
                updateChannel(CHANNEL_ZONE_MOISTURE_LEVEL,
                        RachioQuantityTypes.millimetersOrUndef(z.getMoistureLevel()));
            }
            if (!Double.isNaN(z.getMoisturePercent())) {
                updateChannel(CHANNEL_ZONE_MOISTURE_PERCENT,
                        RachioQuantityTypes.fractionOrUndef(z.getMoisturePercent()));
            }
            updateChannel(CHANNEL_LAST_EVENT, new StringType(z.getEvent()));
            DateTimeType ts = z.getEventTime();
            updateChannel(RachioBindingConstants.CHANNEL_LAST_EVENTTS, ts != null ? ts : UnDefType.UNDEF);
        }
    }

    private void updateZoneImageChannel(RachioZone z) {
        String imageUrl = z.getImageDownloadUrl();
        if (imageUrl.isBlank()) {
            cachedImageUrl = "";
            cachedImage = null;
            failedImageUrl = "";
            updateChannel(CHANNEL_ZONE_IMAGE, UnDefType.NULL);
            return;
        }

        RawType image = cachedImage;
        if (imageUrl.equals(cachedImageUrl) && image != null) {
            updateChannel(CHANNEL_ZONE_IMAGE, image);
            return;
        }
        if (imageUrl.equals(failedImageUrl)) {
            return;
        }

        try {
            image = downloadZoneImage(imageUrl);
            if (image == null) {
                failedImageUrl = imageUrl;
                if (cachedImage == null) {
                    updateChannel(CHANNEL_ZONE_IMAGE, UnDefType.NULL);
                }
                logger.debug("{}: Unable to download image for zone '{}'", thingId, z.name);
                return;
            }
            cachedImageUrl = imageUrl;
            cachedImage = image;
            failedImageUrl = "";
            updateChannel(CHANNEL_ZONE_IMAGE, image);
        } catch (RuntimeException e) {
            failedImageUrl = imageUrl;
            if (cachedImage == null) {
                updateChannel(CHANNEL_ZONE_IMAGE, UnDefType.NULL);
            }
            logger.debug("{}: Unable to update image for zone '{}': {}", thingId, z.name, e.getMessage());
        }
    }

    protected @Nullable RawType downloadZoneImage(String imageUrl) {
        return HttpUtil.downloadImage(imageUrl, true, MAX_ZONE_IMAGE_SIZE_BYTES);
    }

    static State epochMillisOrNull(long epochMillis) {
        if (epochMillis <= 0) {
            return UnDefType.NULL;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()));
    }

    @Override
    protected void goOnline() {
        updateProperties();
        updateResolvedZoneThingStatusAfterSuccessfulCommunication();
        postChannelData();
    }

    private void updateResolvedZoneThingStatusAfterSuccessfulCommunication() {
        RachioDevice d = dev;
        RachioZone z = zone;
        if (isBridgeOnline() && d != null && z != null) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    protected void onBridgeOnline() {
        if (!rebindToCurrentBridgeModel("bridge online")) {
            logger.debug("Bridge is ONLINE; retrying zone initialization for '{}'", getThing().getUID());
            initialize();
        } else {
            goOnline();
        }
    }

    private void updateProperties() {
        RachioZone z = zone;
        if (cloudHandler != null && z != null) {
            logger.trace("Updating Rachio zone properties");
            Map<String, String> properties = z.fillProperties();
            updateProperties(properties);
        }
    }
}
