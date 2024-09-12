/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.CallActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.CallManager;
import org.openhab.binding.freeboxos.internal.api.rest.CallManager.Call;
import org.openhab.binding.freeboxos.internal.api.rest.CallManager.Type;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
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
    private final Map<Type, ZonedDateTime> lastCalls = new HashMap<>(Type.values().length);

    public CallHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        // nothing to do here
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling phone calls ...");

        lastCalls.clear();

        List<Call> entries = getManager(CallManager.class).getCallEntries();
        Arrays.stream(Type.values()).forEach(callType -> entries.stream().filter(call -> call.type().equals(callType))
                .reduce((first, second) -> second).ifPresent(this::updateCallChannels));

        // Clear incoming call if the youngest is not an incoming call
        lastCalls.entrySet().stream().sorted(Map.Entry.comparingByValue()).reduce((first, second) -> second)
                .map(entry -> entry.getKey()).filter(type -> !Type.INCOMING.equals(type)).ifPresent(type -> {
                    String groupName = Type.INCOMING.name().toLowerCase();
                    getThing().getChannelsOfGroup(groupName).stream().map(Channel::getUID).filter(uid -> isLinked(uid))
                            .forEach(uid -> updateState(uid, UnDefType.NULL));
                });

        updateStatus(ThingStatus.ONLINE);
    }

    private void updateCallChannels(Call call) {
        Type lastType = call.type();
        lastCalls.put(lastType, call.datetime());
        String group = lastType.name().toLowerCase();
        String phoneNumber = call.number();

        updateChannelString(group, NUMBER, phoneNumber);
        updateChannelString(group, NAME, call.name());
        updateChannelDateTimeState(group, TIMESTAMP, call.datetime());

        // Do not consider duration for Missed & incoming calls
        if (lastType == Type.ACCEPTED || lastType == Type.OUTGOING) {
            updateChannelQuantity(group, DURATION, call.duration(), Units.SECOND);
        }
    }

    public void emptyQueue() {
        try {
            getManager(CallManager.class).emptyQueue();
        } catch (FreeboxException e) {
            logger.warn("Error clearing call logs: {}", e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(CallActions.class);
    }
}
