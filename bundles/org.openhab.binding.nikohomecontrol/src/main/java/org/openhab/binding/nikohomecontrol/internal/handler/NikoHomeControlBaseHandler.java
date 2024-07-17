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
package org.openhab.binding.nikohomecontrol.internal.handler;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcBaseEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlBaseHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NikoHomeControlBaseHandler extends BaseThingHandler implements NhcBaseEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlBaseHandler.class);

    volatile boolean initialized = false;

    String deviceId = "";
    @Nullable
    Future<?> commStartThread;

    public NikoHomeControlBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        if (commStartThread != null) {
            commStartThread.cancel(true);
            commStartThread = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        NikoHomeControlCommunication nhcComm = getCommunication(getBridgeHandler());
        if (nhcComm == null) {
            logger.debug("communication not up yet, cannot handle command {} for {}", command, channelUID);
            return;
        }

        // This can be expensive, therefore do it in a job.
        scheduler.submit(() -> {
            if (!nhcComm.communicationActive()) {
                restartCommunication(nhcComm);
            }

            if (nhcComm.communicationActive()) {
                handleCommandSelection(channelUID, command);
            }
        });
    }

    abstract void handleCommandSelection(ChannelUID channelUID, Command command);

    @Override
    public void deviceInitialized() {
        logger.debug("device initialized {}", deviceId);

        Bridge bridge = getBridge();
        if ((bridge != null) && (bridge.getStatus() == ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }

        refresh();
    }

    abstract void refresh();

    @Override
    public void deviceRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.configuration-error.deviceRemoved");
    }

    abstract void startCommunication();

    void restartCommunication(NikoHomeControlCommunication nhcComm) {
        // We lost connection but the connection object is there, so was correctly started.
        // Try to restart communication.
        nhcComm.scheduleRestartCommunication();
        // If still not active, take thing offline and return.
        if (!nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error");
            return;
        }
        // Also put the bridge back online
        NikoHomeControlBridgeHandler nhcBridgeHandler = getBridgeHandler();
        if (nhcBridgeHandler != null) {
            nhcBridgeHandler.bridgeOnline();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-unitialized");
        }
    }

    @Nullable
    NikoHomeControlCommunication getCommunication(@Nullable NikoHomeControlBridgeHandler nhcBridgeHandler) {
        return nhcBridgeHandler != null ? nhcBridgeHandler.getCommunication() : null;
    }

    @Nullable
    NikoHomeControlBridgeHandler getBridgeHandler() {
        Bridge nhcBridge = getBridge();
        return nhcBridge != null ? (NikoHomeControlBridgeHandler) nhcBridge.getHandler() : null;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        ThingStatus bridgeStatus = bridgeStatusInfo.getStatus();
        if (ThingStatus.ONLINE.equals(bridgeStatus)) {
            if (!initialized) {
                if (commStartThread != null) {
                    commStartThread.cancel(true);
                }
                commStartThread = scheduler.submit(this::startCommunication);
            } else {
                updateStatus(ThingStatus.ONLINE);
                refresh();
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }
}
