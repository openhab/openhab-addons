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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO;
import org.openhab.binding.verisure.internal.dto.VerisureEventLogDTO.EventLog;

/**
 * Handler for the Event Log thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureEventLogThingHandler extends VerisureThingHandler<VerisureEventLogDTO> {

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
        updateStatus(ThingStatus.ONLINE);
        updateEventLogState(thing);
    }

    private void updateEventLogState(VerisureEventLogDTO eventLogJSON) {
        EventLog eventLog = eventLogJSON.getData().getInstallation().getEventLog();
        if (eventLog.getPagedList().size() > 0) {
            getThing().getChannels().stream().map(Channel::getUID)
                    .filter(channelUID -> isLinked(channelUID) && !channelUID.getId().equals("timestamp"))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), eventLogJSON, eventLog);
                        updateState(channelUID, state);
                    });

            updateTimeStamp(eventLogJSON.getData().getInstallation().getEventLog().getPagedList().get(0).getEventTime(),
                    CHANNEL_LAST_EVENT_TIME);
        } else {
            logger.debug("Empty event log.");
        }

        super.update(eventLogJSON);
    }

    public State getValue(String channelId, VerisureEventLogDTO verisureEventLog, EventLog eventLog) {
        switch (channelId) {
            case CHANNEL_LAST_EVENT_LOCATION:
                String lastEventLocation = eventLog.getPagedList().get(0).getDevice().getArea();
                return lastEventLocation != null ? new StringType(lastEventLocation) : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_DEVICE_ID:
                String lastEventDeviceId = eventLog.getPagedList().get(0).getDevice().getDeviceLabel();
                return lastEventDeviceId != null ? new StringType(lastEventDeviceId) : UnDefType.NULL;
            case CHANNEL_LAST_EVENT_DEVICE_TYPE:
                String lastEventDeviceType = eventLog.getPagedList().get(0).getDevice().getGui().getLabel();
                return lastEventDeviceType != null ? new StringType(lastEventDeviceType) : UnDefType.NULL;
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
}
