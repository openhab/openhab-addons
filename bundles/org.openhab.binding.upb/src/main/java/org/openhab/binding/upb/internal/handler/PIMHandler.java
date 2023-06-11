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
package org.openhab.binding.upb.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upb.internal.Constants;
import org.openhab.binding.upb.internal.UPBController;
import org.openhab.binding.upb.internal.UPBDevice;
import org.openhab.binding.upb.internal.message.UPBMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Powerline Interface Module handlers.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public abstract class PIMHandler extends BaseBridgeHandler implements MessageListener, UPBIoHandler {

    private final Logger logger = LoggerFactory.getLogger(PIMHandler.class);

    // volatile to ensure visibility for callbacks from the serial I/O thread
    private volatile UPBController controller = new UPBController();

    public PIMHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing UPB PIM {}.", getThing().getUID());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, Constants.OFFLINE_CTLR_OFFLINE);
        controller = new UPBController();
    }

    @Override
    public void dispose() {
        logger.debug("UPB binding shutting down...");
        super.dispose();
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    @Override
    public void childHandlerInitialized(final ThingHandler childHandler, final Thing childThing) {
        logger.debug("child handler initialized: {}", childThing.getUID());
        controller.deviceAdded(childHandler, childThing);
        super.childHandlerInitialized(childHandler, childThing);
    }

    @Override
    public void childHandlerDisposed(final ThingHandler childHandler, final Thing childThing) {
        logger.debug("child handler disposed: {}", childThing.getUID());
        controller.deviceRemoved(childHandler, childThing);
        super.childHandlerDisposed(childHandler, childThing);
    }

    @Override
    public void incomingMessage(final UPBMessage msg) {
        updateStatus(ThingStatus.ONLINE);
        controller.incomingMessage(msg);
    }

    @Override
    public void onError(final Throwable t) {
        // Currently all PIM errors are unrecoverable, either a bug or
        // the serial thread had an I/O error.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, Constants.OFFLINE_COMM_ERROR);
    }

    public @Nullable UPBDevice getDevice(byte networkId, byte unitId) {
        return controller.getDevice(networkId, unitId);
    }
}
