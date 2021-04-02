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
import org.openhab.binding.rachio.internal.api.json.RachioApiGsonDTO.RachioZoneStatus;
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
 * The {@link RachioDeviceHandler} is responsible for handling commands, which are
 * sent to one of the device channels.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioDeviceHandler extends BaseThingHandler implements RachioStatusListener {
    private final Logger logger = LoggerFactory.getLogger(RachioDeviceHandler.class);
    private String thingId = "";

    @Nullable
    Bridge bridge;
    @Nullable
    RachioBridgeHandler cloudHandler;
    @Nullable
    RachioDevice dev;
    private Map<String, State> channelData = new HashMap<>();

    public RachioDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        logger.debug("Initializing Rachio Thing '{}'.", thingId);

        String errorMessage = "";
        try {
            bridge = getBridge();
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();
                if ((handler != null) && (handler instanceof RachioBridgeHandler)) {
                    cloudHandler = (RachioBridgeHandler) handler;
                    dev = cloudHandler.getDevByUID(this.getThing().getUID());
                    if (dev != null) {
                        thingId = dev.name;
                        dev.setThingHandler(this);
                        ((RachioBridgeHandler) handler).registerStatusListener(this);
                        ((RachioBridgeHandler) handler).registerWebHook(dev.id);
                        if (bridge.getStatus() != ThingStatus.ONLINE) {
                            logger.debug("{}: Rachio Bridge is offline!", thingId);
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                        } else {
                            updateProperties();
                            postChannelData();
                            updateStatus(dev.getStatus());
                            logger.debug("{}: Device {} initialized.", thingId, dev.name);
                            return;
                        }
                    }
                }
            }
            errorMessage = "Initialisation failed";
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            if (e.getMessage() != null) {
                errorMessage = e.getMessage();
            }
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.warn("{}: ERROR: {}", thingId, errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        logger.debug("{}: Handle Command {} for channel {}", thingId, command, channel);

        RachioDevice d = dev;
        if ((cloudHandler == null) || (d == null)) {
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

            RachioBridgeHandler handler = cloudHandler;
            if (handler == null) {
                return;
            }
            if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_ACTIVE)) {
                if (command instanceof OnOffType) {
                    if (command == OnOffType.OFF) {
                        logger.debug("{}: Pause device {} (disable watering, schedules etc.)", thingId, d.name);
                        handler.disableDevice(d.id);
                    } else {
                        logger.debug("{}: Resume device {} (enable watering, schedules etc.)", thingId, d.name);
                        handler.enableDevice(d.id);
                    }
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RUN_TIME)) {
                if (command instanceof DecimalType) {
                    int runtime = ((DecimalType) command).intValue();
                    logger.debug("Default Runtime for zones set to {} sec", runtime);
                    dev.setRunTime(runtime);
                } else {
                    logger.debug("Command value is no DecimalType: {}", command);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RUN_ZONES)) {
                if (command instanceof StringType) {
                    logger.debug("Run multiple zones: '{}' ('' = ALL)", command.toString());
                    dev.setRunZones(command.toString());
                } else {
                    logger.debug("Command value is no StringType: {}", command);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RUN)) {
                if (command == OnOffType.ON) {
                    logger.debug("START watering zones '{}' ('' = ALL)", dev.getRunZones());
                    handler.runMultipleZones(dev.getAllRunZonesJson(handler.getDefaultRuntime()));
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_STOP)) {
                if (command == OnOffType.ON) {
                    logger.info("STOP watering for device '{}'", dev.name);
                    handler.stopWatering(dev.id);
                    updateState(RachioBindingConstants.CHANNEL_DEVICE_STOP, OnOffType.OFF);
                }
            } else if (channel.equals(RachioBindingConstants.CHANNEL_DEVICE_RAIN_DELAY)) {
                if (command instanceof DecimalType) {
                    logger.info("Start rain delay cycle for {} sec", command.toString());
                    dev.setRainDelayTime(((DecimalType) command).intValue());
                    handler.startRainDelay(dev.id, ((DecimalType) command).intValue());
                } else {
                    logger.debug("Command value is no DecimalType: {}", command);
                }
            }
        } catch (RachioApiException e) {
            errorMessage = e.toString();
        } catch (RuntimeException e) {
            errorMessage = e.getMessage();
        } finally {
            if (!errorMessage.isEmpty()) {
                logger.debug("ERROR: {}", errorMessage);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
            }
        }
    }

    private void postChannelData() {
        RachioDevice d = dev;
        if (d != null) {
            logger.debug("Updating  status");
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_NAME, new StringType(d.getThingName()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_ONLINE, d.getOnline());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_ACTIVE, d.getEnabled());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_PAUSED, d.getSleepMode());
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RUN_ZONES, new StringType(d.getRunZones()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RUN_TIME, new DecimalType(d.getRunTime()));
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RAIN_DELAY,
                    d.rainSensorTripped ? OnOffType.ON : OnOffType.OFF);
            updateChannel(RachioBindingConstants.CHANNEL_DEVICE_RAIN_STRIPPED, new DecimalType(d.rainDelay));
            updateChannel(RachioBindingConstants.CHANNEL_LAST_EVENT, new StringType(d.getEvent()));
            DateTimeType ts = d.getEventTime();
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
    public void onConfigurationUpdated() {
        try {
            if (cloudHandler != null) {
                cloudHandler.registerWebHook(dev.id);
            }
        } catch (RachioApiException e) {
            logger.debug("{}: Unable to renew webhook registration: {}", thingId, e.toString());
        }
    }

    @Override
    public boolean onThingStateChangedl(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        RachioDevice d = dev;
        if ((updatedDev != null) && (d != null) && d.id.equals(updatedDev.id)) {
            logger.debug("Update for device '{}' received.", d.id);
            dev.update(updatedDev);
            updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
            postChannelData();
            updateStatus(d.getStatus());
            return true;
        }
        return false;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        logger.debug("Rachio Bridge Status changed to {}", bridgeStatusInfo.getStatus());
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateProperties();
            postChannelData();
            if (dev != null) {
                updateStatus(dev.getStatus());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void shutdown() {
        if (dev != null) {
            dev.setStatus("OFFLINE");
        }
        updateStatus(ThingStatus.OFFLINE);
    }

    public boolean webhookEvent(RachioEventGsonDTO event) {
        boolean update = true;
        RachioDevice d = dev;
        if (d == null) {
            return false;
        }

        try {
            String etype = event.type;
            RachioZone zone = null;
            if (etype.equals("ZONE_STATUS")) {
                RachioZoneStatus runStatus = event.zoneRunStatus;
                if (runStatus != null) {
                    zone = d.getZoneByNumber(runStatus.zoneNumber);
                }
            } else if (event.subType.equals("ZONE_DELTA")) {
                zone = d.getZoneById(event.zoneId);
            }
            if (zone != null) {
                RachioZoneHandler handler = zone.getThingHandler();
                if (handler != null) {
                    return handler.webhookEvent(event);
                }
            }

            String evt = event.subType.isEmpty() ? event.type : event.subType;
            dev.setEvent(evt, getTimestamp()); // and funnel all zone events to the device
            if (etype.equals("DEVICE_STATUS")) {
                // sub types:
                // COLD_REBOOT, ONLINE, OFFLINE, OFFLINE_NOTIFICATION, SLEEP_MODE_ON, SLEEP_MODE_OFF, BROWNOUT_VALVE
                // RAIN_SENSOR_DETECTION_ON, RAIN_SENSOR_DETECTION_OFF, RAIN_DELAY_ON, RAIN_DELAY_OFF
                logger.debug("Device {} ('{}') changed to status '{}'.", d.name, d.id, event.subType);
                if (event.subType.equals("COLD_REBOOT")) {
                    logger.info("{}: Device {}  was restarted, ip={}/{}, gw={}, dns={}/{}, wifi rssi={}.", thingId,
                            d.name, d.network.ip, d.network.nm, d.network.gw, d.network.dns1, d.network.dns2,
                            d.network.rssi);
                    if (event.network != null) {
                        dev.setNetwork(event.network);
                    }
                } else if (event.subType.equals("ONLINE")) {
                    logger.info("{}: Device is ONLINE.", thingId);
                    dev.setStatus(event.subType);
                } else if (event.subType.equals("OFFLINE") || event.subType.equals("OFFLINE_NOTIFICATION")) {
                    logger.info("{}: Device is OFFLINE (subType = '{}').", thingId, event.subType);
                    dev.setStatus(event.subType);
                } else if (event.subType.equals("SLEEP_MODE_ON")) {
                    logger.info("{}: Device switch to sleep mode.", thingId);
                    dev.setSleepMode(event.subType);
                } else if (event.subType.equals("SLEEP_MODE_OFF")) {
                    logger.info("{}: Device was resumed (exit from sleep mode).", thingId);
                    dev.setSleepMode(event.subType);
                } else if (event.subType.equals("RAIN_DELAY_ON")) {
                    logger.info("{}: Device reporterd Rain Delay ON.", thingId);
                    update = false; // details missing
                } else if (event.subType.equals("RAIN_DELAY_OFF")) {
                    logger.info("{}: Device reporterd Rain Delay OFF.", thingId);
                    update = false; // details missing
                } else if (event.subType.equals("RAIN_SENSOR_DETECTION_ON")) {
                    logger.info("{}: Device reporterd a Rain Sensor ON.", thingId);
                    update = false; // details missing
                } else if (event.subType.equals("RAIN_SENSOR_DETECTION_ON")) {
                    logger.info("{}: Device reporterd Rain Sensor OFF.", thingId);
                    update = false; // details missing
                } else {
                    update = false; // details missing
                }
            } else if (event.type.equals("SCHEDULE_STATUS")) {
                logger.info("{}: Status {} for schedule {}: {} (start={}, end={}, duration={}min)", thingId,
                        event.subType, event.scheduleName, event.summary, event.startTime, event.endTime,
                        event.durationInMinutes);
                updateChannel(CHANNEL_SCHED_NAME, new StringType(event.scheduleName));
                updateChannel(CHANNEL_SCHED_INFO, new StringType(event.summary));
                updateChannel(CHANNEL_SCHED_START, new DateTimeType(event.startTime));
                updateChannel(CHANNEL_SCHED_END, new DateTimeType(event.endTime));
            } else {
                update = false; // unknown event
            }

            if (update) {
                postChannelData();
                updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
                return true;
            }
            logger.debug("{}: Unhandled event {}.{} for device {} ({}): {}", thingId, event.type, event.subType, d.name,
                    d.id, event.summary);
            return false;
        } catch (RuntimeException e) {
            logger.debug("{}: Unable to process event {}.{} - {}", thingId, event.type, event.subType, event.summary,
                    e);
            return false;
        }
    }

    private void updateProperties() {
        RachioDevice d = dev;
        if (d != null) {
            logger.trace("{}: Updating device properties", thingId);
            updateProperties(d.fillProperties());
        }
    }
}
