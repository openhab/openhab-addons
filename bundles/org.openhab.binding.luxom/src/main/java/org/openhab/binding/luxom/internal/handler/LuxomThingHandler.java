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
package org.openhab.binding.luxom.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxom.internal.protocol.LuxomAction;
import org.openhab.binding.luxom.internal.protocol.LuxomCommand;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base type for all Luxom thing handlers.
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public abstract class LuxomThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LuxomThingHandler.class);

    private String address = "";

    @Override
    public void initialize() {
        String id = (String) getConfig().get("address");
        if (id == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.thing-address-missing");
            address = "noaddress";
            return;
        }
        address = id;
    }

    public LuxomThingHandler(Thing thing) {
        super(thing);
    }

    public abstract void handleCommandComingFromBridge(LuxomCommand command);

    public final String getAddress() {
        return address;
    }

    /**
     * Queries for any device state needed at initialization time or after losing connectivity to the bridge, and
     * updates device status. Will be called when bridge status changes to ONLINE and thing has status
     * OFFLINE:BRIDGE_OFFLINE.
     */
    protected abstract void initDeviceState();

    /**
     * Called when changing thing status to offline. Subclasses may override to take any needed actions.
     */
    protected void thingOfflineNotify() {
    }

    protected @Nullable LuxomBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();

        return bridge == null ? null : (LuxomBridgeHandler) bridge.getHandler();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for luxom device handler {}", bridgeStatusInfo.getStatus(),
                getAddress());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE) {
            initDeviceState();

        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            thingOfflineNotify();
        }
    }

    protected void sendCommands(List<CommandExecutionSpecification> commands) {
        @Nullable
        LuxomBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR,
                    "@text/status.bridge-handler-missing");
            thingOfflineNotify();
        } else {
            bridgeHandler.sendCommands(commands);
        }
    }

    /**
     * example : *P,0,1,21;
     */
    protected void ping() {
        sendCommands(List.of(new CommandExecutionSpecification(LuxomAction.PING.getCommand() + ",0," + getAddress())));
    }

    /**
     * example : *S,0,1,21;
     */
    protected void set() {
        sendCommands(List.of(new CommandExecutionSpecification(LuxomAction.SET.getCommand() + ",0," + getAddress())));
    }

    /**
     * example : *C,0,1,21;
     */
    protected void clear() {
        sendCommands(List.of(new CommandExecutionSpecification(LuxomAction.CLEAR.getCommand() + ",0," + getAddress())));
    }
}
