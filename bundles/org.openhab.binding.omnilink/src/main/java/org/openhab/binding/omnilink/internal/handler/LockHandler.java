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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequest;
import org.openhab.binding.omnilink.internal.discovery.ObjectPropertyRequests;
import org.openhab.binding.omnilink.internal.exceptions.BridgeOfflineException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.properties.AccessControlReaderProperties;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.ExtendedAccessControlReaderLockStatus;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;

/**
 * The {@link LockHandler} defines some methods that are used to
 * interface with an OmniLink Lock. This by extension also defines the
 * Lock thing that openHAB will be able to pick up and interface with.
 *
 * @author Brian O'Connell - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class LockHandler extends AbstractOmnilinkStatusHandler<ExtendedAccessControlReaderLockStatus> {
    private final Logger logger = LoggerFactory.getLogger(LockHandler.class);
    private final int thingID = getThingNumber();
    public @Nullable String number;

    public LockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
        if (bridgeHandler != null) {
            updateLockProperties(bridgeHandler);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Received null bridge while initializing Lock!");
        }
    }

    private void updateLockProperties(OmnilinkBridgeHandler bridgeHandler) {
        ObjectPropertyRequest<AccessControlReaderProperties> objectPropertyRequest = ObjectPropertyRequest
                .builder(bridgeHandler, ObjectPropertyRequests.LOCK, thingID, 0).selectNamed().build();

        for (AccessControlReaderProperties lockProperties : objectPropertyRequest) {
            updateProperty(THING_PROPERTIES_NAME, lockProperties.getName());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_LOCK_SWITCH:
                if (command instanceof OnOffType) {
                    sendOmnilinkCommand(OnOffType.OFF.equals(command) ? CommandMessage.CMD_UNLOCK_DOOR
                            : CommandMessage.CMD_LOCK_DOOR, 0, thingID);
                } else {
                    logger.debug("Invalid command {}, must be OnOffType", command);
                }
                break;
            default:
                logger.warn("Unknown channel for Lock thing: {}", channelUID);
        }
    }

    @Override
    public void updateChannels(ExtendedAccessControlReaderLockStatus status) {
        logger.debug("updateChannels called for Lock status: {}", status);
        updateState(CHANNEL_LOCK_SWITCH, OnOffType.from(status.isLocked()));
    }

    @Override
    protected Optional<ExtendedAccessControlReaderLockStatus> retrieveStatus() {
        try {
            final OmnilinkBridgeHandler bridgeHandler = getOmnilinkBridgeHandler();
            if (bridgeHandler != null) {
                ObjectStatus objStatus = bridgeHandler.requestObjectStatus(Message.OBJ_TYPE_CONTROL_LOCK, thingID,
                        thingID, true);
                return Optional.of((ExtendedAccessControlReaderLockStatus) objStatus.getStatuses()[0]);
            } else {
                logger.debug("Received null bridge while updating Lock status!");
                return Optional.empty();
            }
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Received exception while refreshing Lock status: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
