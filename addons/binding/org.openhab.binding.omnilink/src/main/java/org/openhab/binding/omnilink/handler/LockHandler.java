/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AccessControlReaderLockStatus;

/**
 *
 * @author Brian O'Connell
 *
 */
public class LockHandler extends AbstractOmnilinkStatusHandler<AccessControlReaderLockStatus> {

    public LockHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(LockHandler.class);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for channel:{}, command:{}", channelUID, command);

        int lockID = getThingNumber();
        if (command instanceof OnOffType) {
            logger.debug("updating omnilink lock change: {}, command: {}", channelUID, command);

            OmniLinkCmd omniLinkCmd = OnOffType.ON.equals(command) ? OmniLinkCmd.CMD_LOCK_DOOR
                    : OmniLinkCmd.CMD_UNLOCK_DOOR;
            sendOmnilinkCommand(omniLinkCmd.getNumber(), 0, lockID);

        } else {
            logger.warn("Must handle command: {}", command);
        }
    }

    @Override
    public void updateChannels(AccessControlReaderLockStatus status) {
        logger.debug("Procesing status update {}", status);
        updateState(OmnilinkBindingConstants.CHANNEL_LOCK_SWITCH, status.isLocked() ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    protected Optional<AccessControlReaderLockStatus> retrieveStatus() {
        try {
            int lockID = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHandler().requestObjectStatus(Message.OBJ_TYPE_CONTROL_LOCK,
                    lockID, lockID, false);
            return Optional.of((AccessControlReaderLockStatus) objStatus.getStatuses()[0]);

        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }

}
