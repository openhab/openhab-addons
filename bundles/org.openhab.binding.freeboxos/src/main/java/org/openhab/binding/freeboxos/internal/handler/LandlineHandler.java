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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.time.ZoneId;
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.phone.CallEntry;
import org.openhab.binding.freeboxos.internal.api.phone.CallEntry.CallType;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneConfig;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.phone.PhoneStatus;
import org.openhab.binding.freeboxos.internal.config.LandlineConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link LandlineHandler} is responsible for handling everything associated to
 * to the phone landline associated with the Freebox Server.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class LandlineHandler extends ApiConsumerHandler {
    private final static String LAST_CALL_TIMESTAMP = "last-call-timestamp";
    private final Logger logger = LoggerFactory.getLogger(LandlineHandler.class);
    private long lastCallTimestamp;
    private @NonNullByDefault({}) String localPN;

    public LandlineHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
        String lastCall = thing.getProperties().get(LAST_CALL_TIMESTAMP);
        if (lastCall != null) {
            lastCallTimestamp = Long.parseLong(lastCall) - 1;
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        Configuration test = getConfig();
        localPN = (String) test.get(LandlineConfiguration.PHONE_NUMBER);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        PhoneManager phoneManager = bridgeHandler.getPhoneManager();
        pollStatus(phoneManager);
        pollCalls(phoneManager);
        pollConfig(phoneManager);
    }

    private void pollCalls(PhoneManager phoneManager) throws FreeboxException {
        logger.debug("Polling phone calls since last...");

        phoneManager.getCallEntries(lastCallTimestamp).stream().sorted(Comparator.comparingLong(CallEntry::getDatetime))
                .filter(c -> c.getDatetime() > lastCallTimestamp).forEach(call -> {
                    if (call.getType() == CallType.INCOMING) {
                        triggerChannel(new ChannelUID(getThing().getUID(), STATE, PHONE_EVENT),
                                "incoming_call#" + call.getNumber());
                    } else {
                        updateChannels(call);
                        lastCallTimestamp = call.getDatetime();
                        updateProperty(LAST_CALL_TIMESTAMP, Long.toString(lastCallTimestamp));
                    }
                });
    }

    private void pollStatus(PhoneManager phoneManager) throws FreeboxException {
        logger.debug("Polling phone status...");

        PhoneStatus status = phoneManager.getStatus();
        updateChannelOnOff(STATE, ONHOOK, status.isOnHook());
        updateChannelOnOff(STATE, RINGING, status.isRinging());
    }

    private void pollConfig(PhoneManager phoneManager) throws FreeboxException {
        logger.debug("Polling phone status...");

        PhoneConfig config = phoneManager.getConfig();
        updateChannelOnOff(PHONE_MISC, ALTERNATE_RING, config.isDectRingOnOff());
        updateChannelOnOff(PHONE_MISC, DECT_ACTIVE, config.isDectEnabled());
    }

    private void updateChannels(CallEntry call) {
        String group = call.getType().name().toLowerCase();
        String phoneNumber = call.getNumber();

        ChannelUID id = new ChannelUID(getThing().getUID(), group, CALL_INFO);
        StringListType callType;
        if (call.getType() == CallType.OUTGOING) {
            callType = new StringListType(localPN, call.getNumber());
        } else {
            callType = new StringListType(call.getNumber(), localPN);
        }
        updateState(id, callType);
        updateChannelDateTimeState(group, CALL_TIMESTAMP, call.getDatetime());
        if (call.getType() != CallType.MISSED) { // Missed call have no duration by definition
            updateChannelQuantity(group, CALL_DURATION, call.getDuration(), Units.SECOND);
        }
        if (phoneNumber != null && !phoneNumber.contentEquals(call.getName())) {
            updateChannelString(group, CALL_NAME, call.getNumber());
        }
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        PhoneManager phoneManager = bridgeHandler.getPhoneManager();
        String target = channelUID.getIdWithoutGroup();
        if (command instanceof OnOffType) {
            boolean status = (OnOffType) command == OnOffType.ON;
            if (RINGING.equals(target)) {
                phoneManager.ring(status);
                return true;
            } else if (DECT_ACTIVE.equals(target)) {
                phoneManager.activateDect(status);
                return true;
            } else if (ALTERNATE_RING.equals(target)) {
                phoneManager.alternateRing(status);
                return true;
            }
        }
        return false;
    }
}
