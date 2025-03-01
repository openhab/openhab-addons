/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.handler;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jablotron.internal.config.JablotronDeviceConfig;
import org.openhab.binding.jablotron.internal.model.JablotronControlResponse;
import org.openhab.binding.jablotron.internal.model.JablotronDataUpdateResponse;
import org.openhab.binding.jablotron.internal.model.JablotronDiscoveredService;
import org.openhab.binding.jablotron.internal.model.JablotronHistoryDataEvent;
import org.openhab.binding.jablotron.internal.model.JablotronService;
import org.openhab.binding.jablotron.internal.model.JablotronServiceData;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetail;
import org.openhab.binding.jablotron.internal.model.JablotronServiceDetailSegment;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link JablotronAlarmHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public abstract class JablotronAlarmHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Gson gson = new Gson();

    protected JablotronDeviceConfig thingConfig = new JablotronDeviceConfig();

    private String lastWarningTime = "";

    protected String alarmName = "";

    private boolean inService = false;

    protected @Nullable ScheduledFuture<?> future = null;

    protected @Nullable ExpiringCache<JablotronDataUpdateResponse> dataCache;
    protected ExpiringCache<JablotronHistoryDataEvent> eventCache;

    public JablotronAlarmHandler(Thing thing, String alarmName) {
        super(thing);
        this.alarmName = alarmName;
        eventCache = new ExpiringCache<>(CACHE_TIMEOUT_MS, this::sendGetEventHistory);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (ThingStatus.OFFLINE == bridgeStatusInfo.getStatus()
                || ThingStatus.UNINITIALIZED == bridgeStatusInfo.getStatus()) {
            cleanup();
        }
        if (ThingStatus.ONLINE == bridgeStatusInfo.getStatus()) {
            initialize();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        cleanup();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the alarm: {}", getThing().getUID());
        thingConfig = getConfigAs(JablotronDeviceConfig.class);
        future = scheduler.scheduleWithFixedDelay(this::updateAlarmStatus, 1, thingConfig.getRefresh(),
                TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
    }

    public boolean isInService() {
        return inService;
    }

    public String getAlarmName() {
        return alarmName;
    }

    protected abstract void updateSegmentStatus(JablotronServiceDetailSegment segment);

    protected void updateSegmentStatus(String segmentName, @Nullable JablotronDataUpdateResponse dataUpdate) {
        if (dataUpdate == null || !dataUpdate.isStatus()) {
            return;
        }
        List<JablotronServiceData> serviceData = dataUpdate.getData().getServiceData();
        for (JablotronServiceData data : serviceData) {
            if (!thingConfig.getServiceId().equals(data.getServiceId())) {
                continue;
            }
            List<JablotronService> services = data.getData();
            for (JablotronService service : services) {
                JablotronServiceDetail detail = service.getData();
                for (JablotronServiceDetailSegment segment : detail.getSegments()) {
                    if (segmentName.toUpperCase().equals(segment.getSegmentId())) {
                        updateSegmentStatus(segment);
                    }
                }
            }
        }
    }

    private void cleanup() {
        logger.debug("doing cleanup...");
        ScheduledFuture<?> localFuture = future;
        if (localFuture != null) {
            localFuture.cancel(true);
        }
    }

    protected State getCheckTime() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        return new DateTimeType(zdt);
    }

    protected synchronized @Nullable JablotronDataUpdateResponse sendGetStatusRequest() {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            return handler.sendGetStatusRequest(getThing());
        }
        return null;
    }

    protected synchronized boolean updateAlarmStatus() {
        logger.debug("Updating status of alarm: {}", getThing().getUID());
        JablotronDataUpdateResponse dataUpdate = sendGetStatusRequest();
        if (dataUpdate == null) {
            return false;
        }

        if (dataUpdate.isStatus()) {
            updateState(CHANNEL_LAST_CHECK_TIME, getCheckTime());
            List<JablotronServiceData> serviceData = dataUpdate.getData().getServiceData();
            for (JablotronServiceData data : serviceData) {
                if (!thingConfig.getServiceId().equals(data.getServiceId())) {
                    continue;
                }
                List<JablotronService> services = data.getData();
                for (JablotronService service : services) {
                    JablotronServiceDetail detail = service.getData();
                    for (JablotronServiceDetailSegment segment : detail.getSegments()) {
                        updateSegmentStatus(segment);
                    }
                }

            }
        } else {
            logger.debug("Error during alarm status update: {}", dataUpdate.getErrorMessage());
        }

        JablotronHistoryDataEvent event = sendGetEventHistory();
        if (event != null) {
            updateLastEvent(event);
        }

        return true;
    }

    protected @Nullable JablotronHistoryDataEvent sendGetEventHistory() {
        return sendGetEventHistory(alarmName);
    }

    private @Nullable JablotronHistoryDataEvent sendGetEventHistory(String alarm) {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            return handler.sendGetEventHistory(getThing(), alarm);
        }
        return null;
    }

    protected void updateLastEvent(JablotronHistoryDataEvent event) {
        updateState(CHANNEL_LAST_EVENT_TIME, new DateTimeType(Instant.parse(event.getDate())));
        updateState(CHANNEL_LAST_EVENT, new StringType(event.getEventText()));
        updateState(CHANNEL_LAST_EVENT_CLASS, new StringType(event.getIconType()));
        updateState(CHANNEL_LAST_EVENT_INVOKER, new StringType(event.getInvokerName()));

        // oasis does not have sections
        if (getThing().getChannel(CHANNEL_LAST_EVENT_SECTION) != null) {
            updateState(CHANNEL_LAST_EVENT_SECTION, new StringType(event.getSectionName()));
        }
    }

    protected void updateEventChannel(String channel) {
        JablotronHistoryDataEvent event = eventCache.getValue();
        if (event != null) {
            switch (channel) {
                case CHANNEL_LAST_EVENT_TIME:
                    updateState(CHANNEL_LAST_EVENT_TIME, new DateTimeType(Instant.parse(event.getDate())));
                    break;
                case CHANNEL_LAST_EVENT:
                    updateState(CHANNEL_LAST_EVENT, new StringType(event.getEventText()));
                    break;
                case CHANNEL_LAST_EVENT_CLASS:
                    updateState(CHANNEL_LAST_EVENT_CLASS, new StringType(event.getIconType()));
                    break;
                case CHANNEL_LAST_EVENT_INVOKER:
                    updateState(CHANNEL_LAST_EVENT_INVOKER, new StringType(event.getInvokerName()));
                    break;
                case CHANNEL_LAST_EVENT_SECTION:
                    updateState(CHANNEL_LAST_EVENT_SECTION, new StringType(event.getSectionName()));
                    break;
            }
        }
    }

    protected @Nullable JablotronControlResponse sendUserCode(String section, String key, String status, String code) {
        JablotronBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            return handler.sendUserCode(getThing(), section, key, status, code);
        }
        return null;
    }

    protected @Nullable JablotronBridgeHandler getBridgeHandler() {
        Bridge br = getBridge();
        if (br != null && br.getHandler() != null) {
            return (JablotronBridgeHandler) br.getHandler();
        }
        return null;
    }

    public void setStatus(ThingStatus status, ThingStatusDetail detail, String message) {
        updateStatus(status, detail, message);
    }

    public void triggerAlarm(JablotronDiscoveredService service) {
        if (!service.getWarningTime().equals(lastWarningTime)) {
            logger.debug("Service id: {} is triggering an alarm: {}", thing.getUID().getId(), service.getWarning());
            lastWarningTime = service.getWarningTime();
            triggerChannel(CHANNEL_ALARM, service.getWarning());
        }
    }

    public void setInService(boolean inService) {
        this.inService = inService;
    }
}
