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
package org.openhab.binding.panamaxfurman.internal;

import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionActiveEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectionBrokenEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectivityEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanConnectivityListener;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedEvent;
import org.openhab.binding.panamaxfurman.internal.protocol.event.PanamaxFurmanInformationReceivedListener;
import org.openhab.binding.panamaxfurman.internal.transport.PanamaxFurmanTransport;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanamaxFurmanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public abstract class PanamaxFurmanAbstractHandler extends BaseThingHandler
        implements PanamaxFurmanConnectivityListener, PanamaxFurmanInformationReceivedListener {

    private final Logger logger = LoggerFactory.getLogger(PanamaxFurmanAbstractHandler.class);
    private final PanamaxFurmanTransport transport;

    public PanamaxFurmanAbstractHandler(Thing thing) {
        super(thing);
        this.transport = createTransport(getConfig());

        this.transport.addConnectivityListener(this);
        this.transport.addInformationReceivedListener(this);
    }

    protected abstract PanamaxFurmanTransport createTransport(Configuration genericConfig);

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        // Initialize in the background
        scheduler.execute(() -> {
            // If the power query request has not been sent, the connection to the
            // PM has failed. So update its status to OFFLINE.
            if (transport.requestStatusOf("outlet1#power")) {
                // Since the power query was sent successfully, the PC is ONLINE.
                updateStatus(ThingStatus.ONLINE);
                // Then send a power query for the rest of the outlets
                for (int i = 2; i < PanamaxFurmanConstants.MAX_DEVICE_OUTLET_COUNT; i++) {
                    transport.requestStatusOf("outlet" + i + "#power");
                }
            }
        });
    }

    /**
     * Invoked when the user wants to send a command to the Power Conditioner
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            transport.requestStatusOf(channelUID);
        } else {
            transport.sendUpdateCommand(channelUID, command);
        }
    }

    @Override
    public void onConnectivityEvent(PanamaxFurmanConnectivityEvent event) {
        // Only invoke updateStatus if it has actually changed
        if (event instanceof PanamaxFurmanConnectionActiveEvent) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else if (event instanceof PanamaxFurmanConnectionBrokenEvent
                && getThing().getStatus() != ThingStatus.OFFLINE) {
            String errorDetail = ((PanamaxFurmanConnectionBrokenEvent) event).getErrorDetail();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorDetail);
        } else {
            logger.error("Unhandled PanamaxFurmanConnectivityEvent of {}", event.getClass());
        }
    }

    @Override
    public void onInformationReceived(PanamaxFurmanInformationReceivedEvent event) {
        State state = event.getState();
        if (state != null) {
            updateState(event.getChannelString(), state);
        }
    }

    @Override
    public void dispose() {
        transport.shutdown();
        super.dispose();
    }

    /**
     * Build the channelUID from the channel name and the outlet number.
     */
    public static String getChannelUID(String channelName, int outlet) {
        return String.format(PanamaxFurmanConstants.GROUP_CHANNEL_PATTERN, outlet, channelName);
    }

    /**
     * @return the outlet number or null if the outlet was not present or could not be extracted from the channelUID.
     */
    public static @Nullable Integer getOutletFromChannelUID(ChannelUID channelUID) {
        return getOutletFromChannelUID(channelUID.getId());
    }

    public static @Nullable Integer getOutletFromChannelUID(String channelString) {
        Integer outletNumber = null;

        Matcher matcher = PanamaxFurmanConstants.GROUP_CHANNEL_OUTLET_PATTERN.matcher(channelString);
        if (matcher.find()) {
            try {
                outletNumber = Integer.valueOf(matcher.group(1));
            } catch (NumberFormatException e) {
                LoggerFactory.getLogger(PanamaxFurmanAbstractHandler.class).warn(
                        "Caught exception trying to parse outlet number from '{}' {}", channelString, e.getMessage());
            }
            if (outletNumber == null) {
                LoggerFactory.getLogger(PanamaxFurmanAbstractHandler.class)
                        .warn("Could not parse outlet number from '{}'", channelString);
            }
        }
        return outletNumber;
    }
}
