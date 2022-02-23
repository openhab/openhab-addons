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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.call.CallEntry;
import org.openhab.binding.freeboxos.internal.api.call.CallEntry.CallType;
import org.openhab.binding.freeboxos.internal.api.call.CallManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneConfig;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LandlineHandler} is responsible for handling everything associated to
 * to the phone landline associated with the Freebox Server.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LandlineHandler extends ApiConsumerHandler {
    private static final String LAST_CALL_TIMESTAMP = "last-call-timestamp";

    private final Logger logger = LoggerFactory.getLogger(LandlineHandler.class);

    private ZonedDateTime lastCallTimestamp = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);

    public LandlineHandler(Thing thing) {
        super(thing);
        String lastCall = thing.getProperties().get(LAST_CALL_TIMESTAMP);
        if (lastCall != null) {
            lastCallTimestamp = ZonedDateTime.parse(lastCall).minusSeconds(1);
        }
    }

    @Override
    void internalGetProperties(Map<String, String> properties) throws FreeboxException {
        PhoneManager phoneManager = getManager(PhoneManager.class);
        List<PhoneStatus> phones = phoneManager.getPhoneStatuses();
        phones.stream().filter(phone -> phone.getId() == getConfigAs(ClientConfiguration.class).id).findFirst()
                .ifPresent(config -> properties.put(PHONE_TYPE, config.getType().name()));
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        pollStatus();
        pollCalls();
        updateStatus(ThingStatus.ONLINE);
    }

    private void pollCalls() throws FreeboxException {
        logger.debug("Polling phone calls since last...");
        CallManager callManager = getManager(CallManager.class);

        callManager.getCallEntries().stream().filter(entry -> entry.getDatetime() != null)
                .sorted(Comparator.comparing(CallEntry::getDatetime))
                .filter(c -> lastCallTimestamp.isBefore(c.getDatetime())).forEach(call -> {
                    if (call.getType() == CallType.INCOMING) {
                        triggerChannel(new ChannelUID(getThing().getUID(), STATE, PHONE_EVENT),
                                "incoming_call#" + call.getPhoneNumber());
                    } else {
                        updateCallChannels(call);
                        ZonedDateTime timeStamp = call.getDatetime();
                        if (timeStamp != null) {
                            lastCallTimestamp = timeStamp;
                            updateProperty(LAST_CALL_TIMESTAMP, timeStamp.toString());
                        }
                    }
                });
    }

    private void pollStatus() throws FreeboxException {
        logger.debug("Polling phone status...");
        PhoneManager phoneManager = getManager(PhoneManager.class);

        phoneManager.getStatus(getConfigAs(ClientConfiguration.class).id).ifPresent(status -> {
            updateChannelOnOff(STATE, ONHOOK, status.isOnHook());
            updateChannelOnOff(STATE, RINGING, status.isRinging());
        });

        PhoneConfig config = phoneManager.getConfig();
        updateChannelOnOff(PHONE_MISC, ALTERNATE_RING, config.isDectRingOnOff());
        updateChannelOnOff(PHONE_MISC, DECT_ACTIVE, config.isEnabled());
    }

    private void updateCallChannels(CallEntry call) {
        String group = call.getType().name().toLowerCase();
        String phoneNumber = call.getPhoneNumber();

        ChannelUID id = new ChannelUID(getThing().getUID(), group, NUMBER);
        updateState(id, new StringType(call.getPhoneNumber()));
        updateChannelDateTimeState(group, TIMESTAMP, call.getDatetime());
        if (call.getType() != CallType.MISSED) { // Missed call have no duration by definition
            updateChannelQuantity(group, DURATION, call.getDuration(), Units.SECOND);
        }
        if (phoneNumber != null && !phoneNumber.equals(call.getName())) {
            updateChannelString(group, NAME, call.getPhoneNumber());
        }
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        PhoneManager phoneManager = getManager(PhoneManager.class);
        String target = channelUID.getIdWithoutGroup();
        if (command instanceof OnOffType) {
            boolean status = (OnOffType) command == OnOffType.ON;
            if (RINGING.equals(target)) {
                phoneManager.ring(status);
                return true;
            } else if (DECT_ACTIVE.equals(target)) {
                phoneManager.setStatus(status);
                return true;
            } else if (ALTERNATE_RING.equals(target)) {
                phoneManager.alternateRing(status);
                return true;
            }
        }
        return super.internalHandleCommand(channelUID, command);
    }
}
