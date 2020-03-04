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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.CallEntry;
import org.openhab.binding.freebox.internal.api.model.CallEntry.CallType;
import org.openhab.binding.freebox.internal.api.model.CallEntryResponse;
import org.openhab.binding.freebox.internal.api.model.PhoneActionResponse;
import org.openhab.binding.freebox.internal.api.model.PhoneStatus;
import org.openhab.binding.freebox.internal.api.model.PhoneStatusResponse;
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
    private final static String LAST_CALL_TIMESTAMP = "lastCallTimestamp";
    private final Logger logger = LoggerFactory.getLogger(PhoneHandler.class);
    private Long lastCallTimestamp = 0L;

    public PhoneHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
        if (thing.getProperties().containsKey(LAST_CALL_TIMESTAMP)) {
            lastCallTimestamp = Long.parseLong(thing.getProperties().get(LAST_CALL_TIMESTAMP)) - 1;
        }
    }

    @Override
    protected void internalPoll() {
        try {
            logger.debug("Polling phone status...");
            PhoneStatus phoneStatus = bridgeHandler.executeGet(PhoneStatusResponse.class, null).get(0);
            updateChannelSwitchState(STATE, ONHOOK, phoneStatus.isOnHook());
            updateChannelSwitchState(STATE, RINGING, phoneStatus.isRinging());

            logger.debug("Polling phone calls since last...");
            List<CallEntry> callEntries = bridgeHandler.executeGet(CallEntryResponse.class,
                    "_dc=" + lastCallTimestamp.toString());
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
            updateStatus(ThingStatus.ONLINE);

        } catch (FreeboxException e) {
            handleFreeboxException(e);
        }
    }

    private void updateChannels(CallEntry call, String group) {
        updateChannelStringState(group, CALLNUMBER, call.getNumber());
        updateChannelQuantityType(group, CALLDURATION, new QuantityType<>(call.getDuration(), Units.SECOND));
        updateChannelDateTimeState(group, CALLTIMESTAMP, call.getDatetime());
        updateState(new ChannelUID(getThing().getUID(), group, CALLNAME),
                call.getNumber().equals(call.getName()) ? UnDefType.UNDEF : new StringType(call.getName()));
        if (ANY.equals(group)) {
            updateChannelStringState(ANY, CALLSTATUS, call.getType().name());
        }
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) {
        if (RINGING.equals(channelUID.getIdWithoutGroup()) && command instanceof OnOffType) {
            String request = "fxs_ring_" + (((OnOffType) command) == OnOffType.ON ? "start" : "stop") + "/";
            try {
                bridgeHandler.executePost(PhoneActionResponse.class, request, null);
                return true;
            } catch (FreeboxException e) {
                handleFreeboxException(e);
            }
        }
        return false;
    }

}
