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
package org.openhab.binding.freebox.internal.handler;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.CallEntry;
import org.openhab.binding.freebox.internal.api.model.CallEntry.CallType;
import org.openhab.binding.freebox.internal.api.model.PhoneStatus;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link PhoneHandler} is responsible for handling everything associated to
 * to the phone landline associated with the Freebox Server.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PhoneHandler extends APIConsumerHandler {
    private final static String LAST_CALL_TIMESTAMP = "last-call-timestamp";
    private final Logger logger = LoggerFactory.getLogger(PhoneHandler.class);
    private long lastCallTimestamp;

    public PhoneHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
        lastCallTimestamp = thing.getProperties().containsKey(LAST_CALL_TIMESTAMP)
                ? Long.parseLong(thing.getProperties().get(LAST_CALL_TIMESTAMP)) - 1
                : 0;
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling phone status...");
        pollPhoneStatus();
        pollCalls();
    }

    private void pollCalls() throws FreeboxException {
        logger.debug("Polling phone calls since last...");
        List<CallEntry> callEntries = getApiManager().execute(new APIRequests.CallEntries(lastCallTimestamp));
        if (callEntries != null) {
            callEntries = callEntries.stream().sorted(Comparator.comparingLong(CallEntry::getDatetime))
                    .filter(c -> c.getDatetime() > lastCallTimestamp).collect(Collectors.toList());
            if (callEntries.size() > 0) {
                callEntries.forEach(call -> {
                    String channelGroup = call.getType().name().toLowerCase();
                    if (call.getType() != CallType.INCOMING) {
                        updateChannels(call, channelGroup);
                        lastCallTimestamp = call.getDatetime();
                    } else {
                        triggerChannel(new ChannelUID(getThing().getUID(), STATE, PHONE_EVENT),
                                "incoming_call#" + call.getNumber());
                    }
                });
                updateProperty(LAST_CALL_TIMESTAMP, Long.toString(lastCallTimestamp));
            }
        }
    }

    private void pollPhoneStatus() throws FreeboxException {
        List<PhoneStatus> phoneStatus = getApiManager().execute(new APIRequests.PhoneStatus());
        if (!phoneStatus.isEmpty()) {
            updateChannelOnOff(STATE, ONHOOK, phoneStatus.get(0).isOnHook());
            updateChannelOnOff(STATE, RINGING, phoneStatus.get(0).isRinging());
        }
    }

    private void updateChannels(CallEntry call, String group) {
        updateChannelString(group, CALL_NUMBER, call.getNumber());
        updateChannelDateTimeState(group, CALL_TIMESTAMP, call.getDatetime());
        if (call.getType() != CallType.MISSED) { // Missed call have no duration by definition
            updateChannelQuantity(group, CALL_DURATION, call.getDuration(), Units.SECOND);
        }
        if (!call.getNumber().equals(call.getName())) {
            updateChannelString(group, CALL_NAME, call.getNumber());
        }
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        if (RINGING.equals(channelUID.getIdWithoutGroup()) && command instanceof OnOffType) {
            getApiManager().execute(new APIRequests.RingPhone((OnOffType) command == OnOffType.ON));
            return true;
        }
        return false;
    }
}
