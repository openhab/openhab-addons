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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;

/**
 * The {@link AbstractOmnilinkStatusHandler} defines some methods that can be used across
 * the many different units exposed by the OmniLink protocol to retrive updated status information.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public abstract class AbstractOmnilinkStatusHandler<T extends Status> extends AbstractOmnilinkHandler {
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
        this.status = Optional.of(t);
        updateChannels(t);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        status.ifPresent(this::updateChannels);
    }

    private void updateHandlerStatus() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            retrieveStatus().ifPresentOrElse(this::updateChannels, () -> updateStatus(ThingStatus.OFFLINE,
                    ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Received null status update!"));
        }
    }
}
