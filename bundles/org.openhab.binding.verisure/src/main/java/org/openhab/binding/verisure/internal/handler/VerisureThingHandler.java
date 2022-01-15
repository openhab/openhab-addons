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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.verisure.internal.DeviceStatusListener;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.dto.VerisureThingDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Base class and handler for some of the different thing types that Verisure provides.
 *
 * @author Jarle Hjortland - Initial contribution
 * @author Jan Gustafsson - Further development
 *
 */
@NonNullByDefault
public abstract class VerisureThingHandler<T extends VerisureThingDTO> extends BaseThingHandler
        implements DeviceStatusListener<T> {

    protected final Logger logger = LoggerFactory.getLogger(VerisureThingHandler.class);
    protected final Gson gson = new Gson();
    protected VerisureThingConfiguration config = new VerisureThingConfiguration();

    public VerisureThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("VerisureThingHandler handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler bridgeHandler = bridge.getHandler();
                if (bridgeHandler != null) {
                    bridgeHandler.handleCommand(channelUID, command);
                    String deviceId = config.getDeviceId();
                    VerisureSession session = getSession();
                    if (session != null) {
                        @Nullable
                        T thing = session.getVerisureThing(deviceId, getVerisureThingClass());
                        if (thing != null) {
                            update(thing);
                        } else {
                            logger.trace("Thing is null!");
                        }
                    } else {
                        logger.debug("Session is null!");
                    }
                } else {
                    logger.debug("BridgeHandler is null!");
                }
            } else {
                logger.warn("Bridge is null!");
            }
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    @Override
    public void initialize() {
        // Do not go online
        config = getConfigAs(VerisureThingConfiguration.class);
        // Set status to UNKNOWN and let background task set correct status
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge != null) {
            this.bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void dispose() {
        logger.debug("dispose on thing: {}", thing);
        VerisureSession session = getSession();
        if (session != null) {
            session.unregisterDeviceStatusListener(this);
            session.removeVerisureThingHandler(config.getDeviceId());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            VerisureSession session = getSession();
            if (session != null) {
                String deviceId = config.getDeviceId();
                @Nullable
                T thing = session.getVerisureThing(deviceId, getVerisureThingClass());
                if (thing != null) {
                    update(thing);
                } else {
                    logger.warn("Please check that you have configured correct deviceId for thing!");
                }
                session.registerDeviceStatusListener(this);
                session.setVerisureThingHandler(this, config.getDeviceId());
            }
        }
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void onDeviceStateChanged(T thing) {
        String deviceId = thing.getDeviceId();
        // Make sure device id is normalized
        if (config.getDeviceId().equalsIgnoreCase((VerisureThingConfiguration.normalizeDeviceId(deviceId)))) {
            update(thing);
        }
    }

    public abstract void update(T thing);

    public abstract void updateTriggerChannel(String event);

    protected void updateInstallationChannels(T thing) {
        BigDecimal siteId = thing.getSiteId();
        updateState(CHANNEL_INSTALLATION_ID, new DecimalType(siteId.longValue()));
        updateState(CHANNEL_INSTALLATION_NAME, new StringType(thing.getSiteName()));
    }

    protected void updateTriggerChannel(ArrayList<Event> newEvents) {
        VerisureSession session = getSession();
        int delay = 1;
        for (Event newEvent : newEvents) {
            String deviceId = newEvent.getDeviceId();
            String eventType = newEvent.getEventType();
            String eventCategory = newEvent.getEventCategory();
            logger.debug("Trigger event type {}, event category {} for thing {}", eventType, eventCategory, deviceId);
            if (session != null && eventType != null && deviceId != null) {
                String deviceIdTransformed = VerisureThingConfiguration.normalizeDeviceId(deviceId);
                @Nullable
                T thing = session.getVerisureThing(deviceIdTransformed);
                if (thing != null) {
                    logger.debug("Trigger event {} on deviceId {} on  thing {}", eventType, deviceIdTransformed, thing);
                    VerisureThingHandler<?> vth = session.getVerisureThinghandler(deviceIdTransformed);
                    if (vth == null) {
                        logger.debug("No VerisureThingHandler found for thing {}", thing);
                        return;
                    }
                    String eventTranslation = "UNKNOWN_EVENT_TYPE";
                    switch (eventType) {
                        case "BA":
                            eventTranslation = TRIGGER_EVENT_INSTRUSION;
                            break;
                        case "FA":
                            eventTranslation = TRIGGER_EVENT_FIRE;
                            break;
                        case "XT":
                            eventTranslation = TRIGGER_EVENT_BATTERY_LOW;
                            break;
                        case "XR":
                            eventTranslation = TRIGGER_EVENT_BATTERY_RESTORED;
                            break;
                        case "SB":
                        case "BP":
                            eventTranslation = TRIGGER_EVENT_COM_TEST;
                            break;
                        case "YC":
                            eventTranslation = TRIGGER_EVENT_COM_FAILURE;
                            break;
                        case "YK":
                            eventTranslation = TRIGGER_EVENT_COM_RESTORED;
                            break;
                        case "TA":
                            eventTranslation = TRIGGER_EVENT_SABOTAGE_ALARM;
                            break;
                        case "TR":
                            eventTranslation = TRIGGER_EVENT_SABOTAGE_RESTORED;
                            break;
                        case "CO":
                        case "CL":
                        case "CT":
                            eventTranslation = TRIGGER_EVENT_ARM;
                            break;
                        case "OP":
                        case "OO":
                        case "OT":
                        case "OH":
                            eventTranslation = TRIGGER_EVENT_DISARM;
                            break;
                        case "LM":
                        case "LO":
                        case "LC":
                        case "LD":
                            eventTranslation = TRIGGER_EVENT_LOCK;
                            break;
                        case "FK":
                            eventTranslation = TRIGGER_EVENT_LOCK_FAILURE;
                            break;
                        case "UA":
                        case "DC":
                        case "DO":
                        case "DK":
                            eventTranslation = TRIGGER_EVENT_UNLOCK;
                            break;
                        case "WA":
                            eventTranslation = TRIGGER_EVENT_WATER;
                            break;
                        case "IA":
                            eventTranslation = TRIGGER_EVENT_MICE;
                            break;
                        case "DOORWINDOW_STATE_CHANGE_OPENED":
                            eventTranslation = TRIGGER_EVENT_DOORWINDOW_OPENED;
                            break;
                        case "DOORWINDOW_STATE_CHANGE_CLOSED":
                            eventTranslation = TRIGGER_EVENT_DOORWINDOW_CLOSED;
                            break;
                        case "LOCATION_HOME":
                            eventTranslation = TRIGGER_EVENT_LOCATION_HOME;
                            break;
                        case "LOCATION_AWAY":
                            eventTranslation = TRIGGER_EVENT_LOCATION_AWAY;
                            break;
                        default:
                            logger.debug("Unhandled event type: {}, event category: {}", eventType, eventCategory);
                    }
                    logger.debug("Schedule vth {} and event {} with delay {}", vth, eventTranslation, delay);
                    scheduler.schedule(new EventTrigger(vth, eventTranslation), delay, TimeUnit.MILLISECONDS);
                    delay = delay + config.getEventTriggerDelay();
                } else {
                    logger.debug("Thing is null!");
                }
            }
        }
    }

    protected void updateTimeStamp(@Nullable String lastUpdatedTimeStamp) {
        updateTimeStamp(lastUpdatedTimeStamp, CHANNEL_TIMESTAMP);
    }

    protected void updateTimeStamp(@Nullable String lastUpdatedTimeStamp, ChannelUID cuid) {
        if (lastUpdatedTimeStamp != null) {
            try {
                logger.trace("Parsing date {} for channel {}", lastUpdatedTimeStamp, cuid);
                ZonedDateTime zdt = ZonedDateTime.parse(lastUpdatedTimeStamp);
                ZonedDateTime zdtLocal = zdt.withZoneSameInstant(ZoneId.systemDefault());
                logger.trace("Parsing datetime successful. Using date. {}", new DateTimeType(zdtLocal));
                updateState(cuid, new DateTimeType(zdtLocal));
            } catch (IllegalArgumentException e) {
                logger.warn("Parsing date failed: {}.", e.getMessage(), e);
            }
        } else {
            logger.debug("Timestamp is null!");
        }
    }

    protected void updateTimeStamp(@Nullable String lastUpdatedTimeStamp, String channel) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), channel);
        updateTimeStamp(lastUpdatedTimeStamp, cuid);
    }

    protected @Nullable VerisureSession getSession() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            VerisureBridgeHandler vbh = (VerisureBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                return vbh.getSession();
            }
        }
        return null;
    }

    protected void scheduleImmediateRefresh(int refreshDelay) {
        logger.debug("scheduleImmediateRefresh on thing: {}", thing);
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            VerisureBridgeHandler vbh = (VerisureBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                vbh.scheduleImmediateRefresh(refreshDelay);
            }
        }
    }

    private class EventTrigger implements Runnable {
        private @Nullable VerisureThingHandler<?> vth;
        private @Nullable String event;

        public EventTrigger(@Nullable VerisureThingHandler<?> vth, @Nullable String event) {
            this.vth = vth;
            this.event = event;
        }

        @Override
        public void run() {
            logger.debug("Trigger Event {} on {} at time {}", event, vth, ZonedDateTime.now());
            String localEvent = event;
            if (vth != null && localEvent != null) {
                vth.updateTriggerChannel(localEvent);
            }
        }
    }

    protected class Event {
        private @Nullable String deviceId;
        private @Nullable String eventType;
        private @Nullable String eventCategory;

        public Event(@Nullable String deviceId, @Nullable String eventType, @Nullable String eventCategory) {
            this.deviceId = deviceId;
            this.eventType = eventType;
            this.eventCategory = eventCategory;
        }

        public @Nullable String getDeviceId() {
            return deviceId;
        }

        public @Nullable String getEventType() {
            return eventType;
        }

        public @Nullable String getEventCategory() {
            return eventCategory;
        }
    }
}
