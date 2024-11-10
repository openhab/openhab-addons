/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemensrds.points.BasePoint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link RdsHandler} is the OpenHab Handler for Siemens RDS smart
 * thermostats
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class RdsHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(RdsHandler.class);

    private @Nullable ScheduledFuture<?> lazyPollingScheduler = null;
    private @Nullable ScheduledFuture<?> fastPollingScheduler = null;

    private final AtomicInteger fastPollingCallsToGo = new AtomicInteger();

    private RdsDebouncer debouncer = new RdsDebouncer();

    private @Nullable RdsConfiguration config = null;

    private @Nullable RdsDataPoints points = null;

    public RdsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command != RefreshType.REFRESH) {
            doHandleCommand(channelUID.getId(), command);
        }
        startFastPollingBurst();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);

        RdsConfiguration config = this.config = getConfigAs(RdsConfiguration.class);

        if (config.plantId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing Plant Id");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING);

        try {
            RdsCloudHandler cloud = getCloudHandler();

            if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "cloud server offline");
                return;
            }

            initializePolling();
        } catch (RdsCloudException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing cloud server handler");
            return;
        }
    }

    public void initializePolling() {
        try {
            int pollInterval = getCloudHandler().getPollInterval();

            // create a "lazy" polling scheduler
            ScheduledFuture<?> lazyPollingScheduler = this.lazyPollingScheduler;
            if (lazyPollingScheduler == null || lazyPollingScheduler.isCancelled()) {
                this.lazyPollingScheduler = scheduler.scheduleWithFixedDelay(this::lazyPollingSchedulerExecute,
                        pollInterval, pollInterval, TimeUnit.SECONDS);
            }

            // create a "fast" polling scheduler
            fastPollingCallsToGo.set(FAST_POLL_CYCLES);
            ScheduledFuture<?> fastPollingScheduler = this.fastPollingScheduler;
            if (fastPollingScheduler == null || fastPollingScheduler.isCancelled()) {
                this.fastPollingScheduler = scheduler.scheduleWithFixedDelay(this::fastPollingSchedulerExecute,
                        FAST_POLL_INTERVAL, FAST_POLL_INTERVAL, TimeUnit.SECONDS);
            }

            startFastPollingBurst();
        } catch (RdsCloudException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.warn(LOG_SYSTEM_EXCEPTION, "initializePolling()", e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (fastPollingScheduler == null) {
                initializePolling();
            }
        }
    }

    @Override
    public void dispose() {
        // clean up the lazy polling scheduler
        ScheduledFuture<?> lazyPollingScheduler = this.lazyPollingScheduler;
        if (lazyPollingScheduler != null && !lazyPollingScheduler.isCancelled()) {
            lazyPollingScheduler.cancel(true);
            this.lazyPollingScheduler = null;
        }

        // clean up the fast polling scheduler
        ScheduledFuture<?> fastPollingScheduler = this.fastPollingScheduler;
        if (fastPollingScheduler != null && !fastPollingScheduler.isCancelled()) {
            fastPollingScheduler.cancel(true);
            this.fastPollingScheduler = null;
        }
    }

    /*
     * private method: initiate a burst of fast polling requests
     */
    public void startFastPollingBurst() {
        fastPollingCallsToGo.set(FAST_POLL_CYCLES);
    }

    /*
     * private method: this is the callback used by the lazy polling scheduler..
     * polls for the info for all points
     */
    private synchronized void lazyPollingSchedulerExecute() {
        doPollNow();
        if (fastPollingCallsToGo.get() > 0) {
            fastPollingCallsToGo.decrementAndGet();
        }
    }

    /*
     * private method: this is the callback used by the fast polling scheduler..
     * checks if a fast polling burst is scheduled, and if so calls
     * lazyPollingSchedulerExecute
     */
    private void fastPollingSchedulerExecute() {
        if (fastPollingCallsToGo.get() > 0) {
            lazyPollingSchedulerExecute();
        }
    }

    /*
     * private method: send request to the cloud server for a new list of data point
     * states
     */
    private void doPollNow() {
        try {
            RdsCloudHandler cloud = getCloudHandler();

            if (cloud.getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "cloud server offline");
                return;
            }

            RdsDataPoints points = this.points;
            if ((points == null || (!points.refresh(cloud.getApiKey(), cloud.getToken())))) {
                points = fetchPoints();
            }

            if (points == null) {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing data points");
                }
                throw new RdsCloudException("missing data points");
            }

            if (!points.isOnline()) {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "cloud server reports device offline");
                }
                return;
            }

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "server response ok");
            }

            for (ChannelMap channel : CHAN_MAP) {
                if (!debouncer.timeExpired(channel.id)) {
                    continue;
                }

                BasePoint point;
                try {
                    point = points.getPointByClass(channel.clazz);
                } catch (RdsCloudException e) {
                    logger.debug("{} \"{}\" not implemented; set state to UNDEF", channel.id, channel.clazz);
                    updateState(channel.id, UnDefType.UNDEF);
                    continue;
                }

                State state = null;

                switch (channel.id) {
                    case CHA_ROOM_TEMP:
                    case CHA_ROOM_HUMIDITY:
                    case CHA_OUTSIDE_TEMP:
                    case CHA_TARGET_TEMP: {
                        state = point.getState();
                        break;
                    }
                    case CHA_ROOM_AIR_QUALITY:
                    case CHA_ENERGY_SAVINGS_LEVEL: {
                        state = point.getEnum();
                        break;
                    }
                    case CHA_OUTPUT_STATE: {
                        state = point.getEnum();
                        // convert the state text "Neither" to the easier to understand word "Off"
                        if (STATE_NEITHER.equals(state.toString())) {
                            state = new StringType(STATE_OFF);
                        }
                        break;
                    }
                    case CHA_STAT_AUTO_MODE: {
                        state = OnOffType.from(point.getPresentPriority() > 13
                                || points.getPointByClass(HIE_STAT_OCC_MODE_PRESENT).asInt() == 2);
                        break;
                    }
                    case CHA_STAT_OCC_MODE_PRESENT: {
                        state = OnOffType.from(point.asInt() == 3);
                        break;
                    }
                    case CHA_DHW_AUTO_MODE: {
                        state = OnOffType.from(point.getPresentPriority() > 13);
                        break;
                    }
                    case CHA_DHW_OUTPUT_STATE: {
                        state = OnOffType.from(point.asInt() == 2);
                        break;
                    }
                }

                if (state != null) {
                    updateState(channel.id, state);
                }
            }
        } catch (RdsCloudException e) {
            logger.warn(LOG_SYSTEM_EXCEPTION, "doPollNow()", e.getClass().getName(), e.getMessage());
        }
    }

    /*
     * private method: sends a new channel value to the cloud server
     */
    private synchronized void doHandleCommand(String channelId, Command command) {
        RdsDataPoints points = this.points;
        try {
            RdsCloudHandler cloud = getCloudHandler();

            String apiKey = cloud.getApiKey();
            String token = cloud.getToken();

            if ((points == null || (!points.refresh(apiKey, token)))) {
                points = fetchPoints();
            }

            if (points == null) {
                throw new RdsCloudException("missing data points");
            }

            for (ChannelMap channel : CHAN_MAP) {
                if (channelId.equals(channel.id)) {
                    switch (channel.id) {
                        case CHA_TARGET_TEMP: {
                            double targetTemperature = Double.NaN;
                            if (command instanceof QuantityType<?> quantityCommand) {
                                Unit<?> unit = points.getPointByClass(channel.clazz).getUnit();
                                QuantityType<?> temp = quantityCommand.toUnit(unit);
                                if (temp != null) {
                                    targetTemperature = temp.doubleValue();
                                }
                            } else if (command instanceof DecimalType decimalCommand) {
                                targetTemperature = decimalCommand.doubleValue();
                            }
                            if (targetTemperature != Double.NaN) {
                                points.setValue(apiKey, token, channel.clazz,
                                        String.format("%.1f", Math.round(targetTemperature * 2) / 2.0));
                                debouncer.initialize(channelId);
                            }
                            break;
                        }
                        case CHA_STAT_AUTO_MODE: {
                            /*
                             * this command is particularly funky.. use Green Leaf = 5 to set to Auto, and
                             * use Comfort Button = 1 to set to Manual
                             */
                            if (command == OnOffType.ON) {
                                points.setValue(apiKey, token, HIE_ENERGY_SAVINGS_LEVEL, "5");
                            } else {
                                points.setValue(apiKey, token, HIE_STAT_CMF_BTN, "1");
                            }
                            debouncer.initialize(channelId);
                            break;
                        }
                        case CHA_STAT_OCC_MODE_PRESENT: {
                            points.setValue(apiKey, token, channel.clazz, command == OnOffType.OFF ? "2" : "3");
                            debouncer.initialize(channelId);
                            break;
                        }
                        case CHA_DHW_AUTO_MODE: {
                            if (command == OnOffType.ON) {
                                points.setValue(apiKey, token, channel.clazz, "0");
                            } else {
                                points.setValue(apiKey, token, channel.clazz,
                                        Integer.toString(points.getPointByClass(channel.clazz).asInt()));
                            }
                            debouncer.initialize(channelId);
                            break;
                        }
                        case CHA_DHW_OUTPUT_STATE: {
                            points.setValue(apiKey, token, channel.clazz, command == OnOffType.OFF ? "1" : "2");
                            debouncer.initialize(channelId);
                            break;
                        }
                        case CHA_ROOM_TEMP:
                        case CHA_ROOM_HUMIDITY:
                        case CHA_OUTSIDE_TEMP:
                        case CHA_ROOM_AIR_QUALITY:
                        case CHA_OUTPUT_STATE: {
                            logger.debug("error: unexpected command to channel {}", channel.id);
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (RdsCloudException e) {
            logger.warn(LOG_SYSTEM_EXCEPTION, "doHandleCommand()", e.getClass().getName(), e.getMessage());
        }
    }

    /*
     * private method: returns the cloud server handler
     */
    private RdsCloudHandler getCloudHandler() throws RdsCloudException {
        @Nullable
        Bridge b;

        if ((b = getBridge()) != null && (b.getHandler() instanceof RdsCloudHandler cloudHandler)) {
            return cloudHandler;
        }
        throw new RdsCloudException("no cloud handler found");
    }

    public @Nullable RdsDataPoints fetchPoints() {
        RdsConfiguration config = this.config;
        try {
            if (config == null) {
                throw new RdsCloudException("missing configuration");
            }

            String url = String.format(URL_POINTS, config.plantId);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_HTTP_COMMAND, HTTP_GET, url.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, url);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_HTTP_COMMAND_ABR, HTTP_GET, url.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_SENDING_MARK, url.substring(0, Math.min(url.length(), 30)));
            }

            RdsCloudHandler cloud = getCloudHandler();
            String apiKey = cloud.getApiKey();
            String token = cloud.getToken();

            String json = RdsDataPoints.httpGenericGetJson(apiKey, token, url);

            if (logger.isTraceEnabled()) {
                logger.trace(LOG_CONTENT_LENGTH, LOG_RECEIVED_MSG, json.length());
                logger.trace(LOG_PAYLOAD_FMT, LOG_RECEIVED_MARK, json);
            } else if (logger.isDebugEnabled()) {
                logger.debug(LOG_CONTENT_LENGTH_ABR, LOG_RECEIVED_MSG, json.length());
                logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_RECEIVED_MARK, json.substring(0, Math.min(json.length(), 30)));
            }

            return this.points = RdsDataPoints.createFromJson(json);
        } catch (RdsCloudException e) {
            logger.warn(LOG_SYSTEM_EXCEPTION, "fetchPoints()", e.getClass().getName(), e.getMessage());
        } catch (JsonParseException | IOException e) {
            logger.warn(LOG_RUNTIME_EXCEPTION, "fetchPoints()", e.getClass().getName(), e.getMessage());
        }
        return this.points = null;
    }
}
