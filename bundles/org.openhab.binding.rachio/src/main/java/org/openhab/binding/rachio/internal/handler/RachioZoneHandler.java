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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioBindingConstants;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
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
public class RachioZoneHandler extends BaseThingHandler implements RachioStatusListener {
    private final Logger logger = LoggerFactory.getLogger(RachioZoneHandler.class);
    private String thingId = "";
    private Map<String, State> channelData = new HashMap<>();
    @Nullable
    private RachioBridgeHandler cloudHandler;
    @Nullable
    Bridge bridge;
    @Nullable
    private RachioDevice dev;
    @Nullable
    private RachioZone zone;

    public RachioZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing zone '{}'", this.getThing().getUID().toString());

        try {
            // initialize class objects
            bridge = getBridge();
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();
                if ((handler != null) && (handler instanceof RachioBridgeHandler)) {
                    cloudHandler = (RachioBridgeHandler) handler;
                    zone = cloudHandler.getZoneByUID(this.getThing().getUID());
                    if (zone != null) {
                        zone.setThingHandler(this);
                        dev = ((RachioBridgeHandler) handler).getDevByUID(zone.getDevUID());
                        thingId = dev.name + "[" + zone.zoneNumber + "]";
                    }
                }
            }
            if ((bridge == null) || (cloudHandler == null) || (dev == null) || (zone == null)) {
                logger.debug("{}: Thing initialisation failed!", thingId);
                return;
            }

            // listen to bridge events
            cloudHandler.registerStatusListener(this);
            if (bridge.getStatus() != ThingStatus.ONLINE) {
                logger.debug("{}: Bridge is offline!", thingId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                updateProperties();
                postChannelData();
                updateStatus(dev.getStatus());
                return;
            }
        } catch (RuntimeException e) {
            logger.debug("{}: Initialisation failed", thingId, e);
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
        if ((cloudHandler == null) || (zone == null)) {
            logger.debug("{}: Cloud handler or device not initialized!", thingId);
            return;
        }

        String errorMessage = "";
        try {
            if (command == RefreshType.REFRESH) {
                if (channelData.containsKey(channel)) {
                    State state = channelData.get(channel);
                    if (state != null) {
                        updateState(channel, state);
                        logger.debug("{}: Return cached data for channel {}: {}", thingId, channel, state);
                    }
                } else {
                    postChannelData();
                }
                return;
            }

            if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_ENABLED)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        logger.debug("Enabling zone '{} [{}]'", zone.name, zone.zoneNumber);
                    }
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_RUN)) {
                if (command == OnOffType.ON) {
                    int runtime = zone.getStartRunTime();
                    logger.debug("{}: Starting zone {} for {} secs", thingId, zone.name, runtime);
                    if (runtime == 0) {
                        runtime = cloudHandler.getDefaultRuntime();
                        logger.debug("{}: Starting zone {} with default runtime ({} secs);", thingId, zone.name,
                                runtime);
                    }
                    cloudHandler.startZone(zone.id, runtime);
                } else {
                    logger.debug("{}: Stop watering for the device", thingId);
                    cloudHandler.stopWatering(dev.id);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_ZONE_RUN_TIME)) {
                if (command instanceof DecimalType) {
                    int runtime = ((DecimalType) command).intValue();
                    logger.debug("{}: Zone {} will start for {} sec", thingId, zone.name, runtime);
                    zone.setStartRunTime(runtime);
                }
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = e.toString();
            }
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.debug("{}: {}", thingId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    @Override
    public boolean onThingStateChangedl(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        RachioZone z = zone;
        if ((updatedZone != null) && (zone != null) && zone.id.equals(updatedZone.id)) {
            logger.debug("{}: Update for zone {} received.", thingId, zone.name);
            zone.update(updatedZone);
            updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
            postChannelData();
            updateStatus(dev.getStatus());
            return true;
        }
        return false;
    }

    @Override
    public void onConfigurationUpdated() {
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        boolean update = false;
        try {

            String zoneName = event.zoneName;
            String evt = event.subType.isEmpty() ? event.type : event.subType;
            zone.setEvent(evt, getTimestamp()); // and funnel all zone events to the device
            if (event.type.equals("ZONE_STATUS")) {
                if (event.zoneRunStatus.state.equals("ZONE_STARTED")) {
                    logger.info("{}: Zone {} STARTED watering ({}).", thingId, zoneName, event.timestamp);
                    updateChannel(CHANNEL_ZONE_RUN, OnOffType.ON);
                } else if (event.subType.equals("ZONE_STOPPED") || event.subType.equals("ZONE_COMPLETED")) {
                    logger.info(
                            "{}: Zoned {} STOPPED watering (timestamp={}, current={}, duration={}sec/{}min, flowVolume={}).",
                            thingId, zoneName, event.timestamp, event.zoneCurrent, event.duration,
                            event.durationInMinutes, event.flowVolume);
                    updateChannel(CHANNEL_ZONE_RUN, OnOffType.OFF);
                } else {
                    logger.info("{}: Event for zone {}: {} (status={}, duration = {}sec)", thingId, event.zoneName,
                            event.summary, event.zoneRunStatus.state, event.duration);
                }
                update = true;
            } else if (event.subType.equals("ZONE_DELTA")) {
                logger.info("{}: DELTA Event for zone {}: {}.{}", thingId, zone.name, event.category, event.action);
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

    public void postChannelData() {
        RachioZone z = zone;
        if (z != null) {
            updateChannel(CHANNEL_ZONE_NAME, new StringType(z.name));
            updateChannel(CHANNEL_ZONE_NUMBER, new DecimalType(z.zoneNumber));
            updateChannel(CHANNEL_ZONE_ENABLED, z.getEnabled());
            updateChannel(CHANNEL_ZONE_RUN, OnOffType.OFF);
            updateChannel(CHANNEL_ZONE_RUN_TIME, new DecimalType(z.getStartRunTime()));
            updateChannel(CHANNEL_ZONE_RUN_TOTAL, new DecimalType(z.runtime));
            updateChannel(CHANNEL_ZONE_IMAGEURL, new StringType(z.imageUrl));
            updateChannel(CHANNEL_LAST_EVENT, new StringType(z.getEvent()));
            DateTimeType ts = z.getEventTime();
            updateChannel(RachioBindingConstants.CHANNEL_LAST_EVENTTS, ts != null ? ts : UnDefType.UNDEF);
        }
    }

    private boolean updateChannel(String channelName, State newValue) {
        State currentValue = channelData.get(channelName);
        if ((currentValue != null) && currentValue.equals(newValue)) {
            // no update required
            return false;
        }

        if (currentValue == null) {
            // new value -> update
            channelData.put(channelName, newValue);
        } else {
            // value changed -> update
            channelData.replace(channelName, newValue);
        }

        updateState(channelName, newValue);
        return true;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        logger.trace("{}: Bridge Status changed to {}", thingId, bridgeStatusInfo.getStatus());
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateProperties();
            updateStatus(dev.getStatus());
            postChannelData();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void shutdown() {
        updateStatus(ThingStatus.OFFLINE);
    }

    private void updateProperties() {
        if ((cloudHandler != null) && (zone != null)) {
            logger.trace("Updating Rachio zone properties");
            Map<String, String> prop = zone.fillProperties();
            if (prop != null) {
                updateProperties(prop);
            } else {
                logger.debug("{}: Unable to update properties for Thing!", thingId);
                return;
            }
        }
    }
}
