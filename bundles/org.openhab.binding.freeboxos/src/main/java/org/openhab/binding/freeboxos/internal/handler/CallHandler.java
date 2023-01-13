/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.CallActions;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.CallType;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.call.CallEntry;
import org.openhab.binding.freeboxos.internal.api.call.CallManager;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CallHandler} is responsible for handling everything associated to the phone calls received on the box line
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CallHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(CallHandler.class);
    private ZonedDateTime lastTimestamp = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    private CallType lastType = CallType.UNKNOWN;

    public CallHandler(Thing thing) {
        super(thing);
        String lastCall = thing.getProperties().get(LAST_CALL_TIMESTAMP);
        if (lastCall != null) {
            lastTimestamp = ZonedDateTime.parse(lastCall).minusSeconds(1);
        }
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        // nothing to do here
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling phone calls ...");

        getManager(CallManager.class).getCallEntries().filter(c -> lastTimestamp.isBefore(c.getDatetime()))
                .forEach(call -> {
                    updateCallChannels(call);
                    lastTimestamp = call.getDatetime().minusSeconds(1);
                    lastType = call.getType();
                });

        updateProperty(LAST_CALL_TIMESTAMP, lastTimestamp.toString());

        if (lastType != CallType.INCOMING) {
            updateIfActive("incoming", NUMBER, UnDefType.NULL);
            updateIfActive("incoming", TIMESTAMP, UnDefType.NULL);
            updateIfActive("incoming", NAME, UnDefType.NULL);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void updateCallChannels(CallEntry call) {
        String group = call.getType().name().toLowerCase();
        String phoneNumber = call.getPhoneNumber();

        updateChannelString(group, NUMBER, phoneNumber);
        updateChannelDateTimeState(group, TIMESTAMP, call.getDatetime());

        // Missed & incoming call have no duration by definition
        if (call.getType() == CallType.ACCEPTED || call.getType() == CallType.OUTGOING) {
            updateChannelQuantity(group, DURATION, call.getDuration(), Units.SECOND);
        }

        if (!phoneNumber.equals(call.getName())) {
            updateChannelString(group, NAME, phoneNumber);
        }
    }

    public void emptyQueue() {
        try {
            getManager(CallManager.class).emptyQueue();
            logger.info("Call log succesfully cleared");
        } catch (FreeboxException e) {
            logger.warn("Error clearing call logs : {}", e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(CallActions.class);
    }

}
