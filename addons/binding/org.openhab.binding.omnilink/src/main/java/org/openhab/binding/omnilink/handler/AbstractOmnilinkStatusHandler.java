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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;

/**
 *
 * @author Craig Hamilton
 *
 */
public abstract class AbstractOmnilinkStatusHandler<T extends Status> extends AbstractOmnilinkHandler {

    private static Logger logger = LoggerFactory.getLogger(AbstractOmnilinkStatusHandler.class);

    public AbstractOmnilinkStatusHandler(Thing thing) {
        super(thing);
    }

    private volatile Optional<T> status = Optional.empty();

    @Override
    public void initialize() {
        updateHandlerStatus();
    }

    /**
     * Attempt to retrieve an updated status for this handler type.
     *
     * @return Optional with updated status if possible, empty optional otherwise.
     */
    protected abstract Optional<T> retrieveStatus();

    /**
     * Update channels associated with handler
     *
     * @param t Status object to update channels with
     */
    protected abstract void updateChannels(T t);

    /**
     * Process a status update for this handler. This will dispatch updateChannels where appropriate.
     *
     * @param t Status to process.
     */
    public void handleStatus(T t) {
        if (t != null) {
            this.status = Optional.of(t);
            updateChannels(t);
        } else {
            logger.warn("Received null status update");
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channel linked: {} for zone {}", channelUID, getThingNumber());
        if (status.isPresent()) {
            updateChannels(status.get());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        updateHandlerStatus();
    }

    private void updateHandlerStatus() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            Optional<T> status = retrieveStatus();
            handleStatus(status.orElse(null)); // handle status will process null.
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
