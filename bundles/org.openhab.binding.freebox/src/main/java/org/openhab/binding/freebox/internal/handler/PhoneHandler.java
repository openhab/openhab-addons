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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.CallEntry;
import org.openhab.binding.freebox.internal.api.model.CallEntry.CallType;
import org.openhab.binding.freebox.internal.api.model.PhoneStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link PhoneHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PhoneHandler extends APIConsumerHandler {
    private final static String LAST_CALL_TIMESTAMP = "last-call-timestamp";
    private final Logger logger = LoggerFactory.getLogger(PhoneHandler.class);
    private Long lastCallTimestamp;

    public PhoneHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
        lastCallTimestamp = thing.getProperties().containsKey(LAST_CALL_TIMESTAMP)
                ? Long.parseLong(thing.getProperties().get(LAST_CALL_TIMESTAMP)) - 1
                : 0L;
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling phone status...");
        pollPhoneStatus();
        pollCalls();
        // TODO : implement usage of this
        // PhoneConfig phoneConfig = getApiManager().execute(new APIRequests.PhoneConfig());
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
                    updateChannels(call, channelGroup);
                    updateChannels(call, ANY);
                    if (call.getType() != CallType.INCOMING) {
                        lastCallTimestamp = call.getDatetime();
                    }
                });
                updateProperty(LAST_CALL_TIMESTAMP, lastCallTimestamp.toString());
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
        updateChannelQuantity(group, CALL_DURATION, call.getDuration(), Units.SECOND);
        updateChannelDateTimeState(group, CALL_TIMESTAMP, call.getDatetime());
        if (!call.getNumber().equals(call.getName())) {
            updateChannelString(group, CALL_NAME, call.getNumber());
        }
        if (ANY.equals(group)) {
            updateChannelString(ANY, CALL_STATUS, call.getType().name());
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
