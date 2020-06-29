/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.dto.VerisureBaseThingDTO.Device;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO.EventLog;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO.PagedList;

/**
 * Handler for the Event Log thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureEventLogThingHandler extends VerisureThingHandler<VerisureEventLogDTO> {

    private BigDecimal lastEventId = BigDecimal.ZERO;
    private long lastEventTime = 0;
    private final static BigInteger TWO = BigInteger.valueOf(2);
    private final static BigDecimal MINUS_ONE = new BigDecimal(-1);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_EVENT_LOG);

    public VerisureEventLogThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<VerisureEventLogDTO> getVerisureThingClass() {
        return VerisureEventLogDTO.class;
    }

    @Override
    public synchronized void update(VerisureEventLogDTO thing) {
        logger.debug("update on thing: {}", thing);
        updateEventLogState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void initialize() {
        logger.debug("initialize on thing: {}", thing);
        VerisureSession session = getSession();
        config = getConfigAs(VerisureThingConfiguration.class);
        if (session != null) {
            logger.debug("Set number of events to fetch from API to {} for thing {}", config.getNumberOfEvents(),
                    thing);
            session.setNumberOfEvents(config.getNumberOfEvents());
        }
        super.initialize();
    }

    private void updateEventLogState(VerisureEventLogDTO eventLogJSON) {
        EventLog eventLog = eventLogJSON.getData().getInstallation().getEventLog();
        if (eventLog.getPagedList().size() > 0) {
            getThing().getChannels().stream().map(Channel::getUID).filter(channelUID -> isLinked(channelUID))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), eventLogJSON, eventLog);
                        updateState(channelUID, state);
                    });
            updateInstallationChannels(eventLogJSON);
            String eventTime = eventLogJSON.getData().getInstallation().getEventLog().getPagedList().get(0)
                    .getEventTime();
            if (eventTime != null) {
                updateTimeStamp(eventTime, CHANNEL_LAST_EVENT_TIME);
                lastEventTime = ZonedDateTime.parse(eventTime).toEpochSecond();
            }
        } else {
            logger.debug("Empty event log.");
        }
    }

    public static BigDecimal toBigDecimal(String hex) {
        // handle leading sign
        BigDecimal sign = null;
        if (hex.startsWith("-")) {
            hex = hex.substring(1);
            sign = MINUS_ONE;
        } else if (hex.startsWith("+")) {
            hex = hex.substring(1);
        }

        // constant must start with 0x or 0X
        if (!(hex.startsWith("0x") || hex.startsWith("0X"))) {
            throw new NumberFormatException("not a hexadecimal floating point constant: " + hex);
        }
        hex = hex.substring(2);

        // ... and end in 'p' or 'P' and an exponent
        int p = hex.indexOf("p");
        if (p < 0) {
            p = hex.indexOf("P");
        }
        if (p < 0) {
            throw new NumberFormatException("not a hexadecimal floating point constant");
        }
        String mantissa = hex.substring(0, p);
        String exponent = hex.substring(p + 1);

        // find the hexadecimal point, if any
        int hexadecimalPoint = mantissa.indexOf(".");
        int hexadecimalPlaces = 0;
        if (hexadecimalPoint >= 0) {
            hexadecimalPlaces = mantissa.length() - 1 - hexadecimalPoint;
            mantissa = mantissa.substring(0, hexadecimalPoint) + mantissa.substring(hexadecimalPoint + 1);
        }

        // reduce the exponent by 4 for every hexadecimal place
        int binaryExponent = Integer.valueOf(exponent) - (hexadecimalPlaces * 4);
        boolean positive = true;
        if (binaryExponent < 0) {
            binaryExponent = -binaryExponent;
            positive = false;
        }

        BigDecimal base = new BigDecimal(new BigInteger(mantissa, 16));
        BigDecimal factor = new BigDecimal(TWO.pow(binaryExponent));
        BigDecimal value = positive ? base.multiply(factor) : base.divide(factor);
        if (sign != null) {
            value = value.multiply(sign);
        }

        return value;
    }

    public State getValue(String channelId, VerisureEventLogDTO verisureEventLog, EventLog eventLog) {
        Device device = eventLog.getPagedList().get(0).getDevice();

        switch (channelId) {
            case CHANNEL_LAST_EVENT_LOCATION:
                return device != null && device.getArea() != null ? new StringType(device.getArea()) : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_DEVICE_ID:
                return device != null && device.getDeviceLabel() != null ? new StringType(device.getDeviceLabel())
                        : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_ID:
                String eventId = eventLog.getPagedList().get(0).getEventId();
                if (eventId != null) {
                    if (eventId.contains("-")) {
                        eventId = eventId.replace("-", "");
                        eventId = "0x" + eventId;
                        eventId = eventId + "p+0";
                        lastEventId = toBigDecimal(eventId);
                    } else {
                        lastEventId = new BigDecimal(eventId);
                    }
                    return new DecimalType(lastEventId);
                } else {
                    return UnDefType.NULL;
                }
            case CHANNEL_LAST_EVENT_TIME:
                if (lastEventTime != 0) {
                    triggerEventChannels(eventLog);
                }
            case CHANNEL_LAST_EVENT_DEVICE_TYPE:
                return device != null && device.getGui().getLabel() != null ? new StringType(device.getGui().getLabel())
                        : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_TYPE:
                String lastEventType = eventLog.getPagedList().get(0).getEventType();
                return lastEventType != null ? new StringType(lastEventType) : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_CATEGORY:
                String lastEventCategory = eventLog.getPagedList().get(0).getEventCategory();
                return lastEventCategory != null ? new StringType(lastEventCategory) : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_USER_NAME:
                String lastEventUserName = eventLog.getPagedList().get(0).getUserName();
                return lastEventUserName != null ? new StringType(lastEventUserName) : UnDefType.NULL;
            case CHANNEL_EVENT_LOG:
                String eventLogJSON = gson.toJson(eventLog);
                return eventLogJSON != null ? new StringType(eventLogJSON) : UnDefType.NULL;
        }
        return UnDefType.UNDEF;
    }

    private void triggerEventChannels(EventLog eventLog) {
        List<PagedList> newEventList = eventLog.getPagedList().stream().collect(Collectors.toList());
        Collections.reverse(newEventList);
        ArrayList<Event> events = new ArrayList<>();
        for (PagedList newEvent : newEventList) {
            long eventTime = ZonedDateTime.parse(newEvent.getEventTime()).toEpochSecond();
            logger.trace("Event time: {} Last Event time: {}", eventTime, lastEventTime);
            if (eventTime > lastEventTime) {
                logger.debug("Create event {} for event time {}", newEvent.getEventType(), eventTime);
                Event event;
                Device device = newEvent.getDevice();
                if (device != null) {
                    event = new Event(device.getDeviceLabel(), newEvent.getEventType(), newEvent.getEventCategory());
                } else {
                    event = new Event("NA", newEvent.getEventType(), newEvent.getEventCategory());
                }
                events.add(event);
            }
        }
        updateTriggerChannel(events);
    }

    @Override
    public void updateTriggerChannel(String event) {
        // TODO Auto-generated method stub

    }
}
