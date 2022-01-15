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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openwebnet4j.OpenGateway;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereZigBee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetThingHandler} is responsible for handling commands for a OpenWebNet device.
 * It's the abstract class for all OpenWebNet things. It should be extended by each specific OpenWebNet category of
 * device (WHO).
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public abstract class OpenWebNetThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetThingHandler.class);

    protected @Nullable OpenWebNetBridgeHandler bridgeHandler;
    protected @Nullable String ownId; // OpenWebNet identifier for this device: WHO.WHERE
    protected @Nullable Where deviceWhere; // this device Where address

    protected @Nullable ScheduledFuture<?> refreshTimeout;

    public OpenWebNetThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            OpenWebNetBridgeHandler brH = (OpenWebNetBridgeHandler) bridge.getHandler();
            if (brH != null) {
                bridgeHandler = brH;

                final String configDeviceWhere = (String) getConfig().get(CONFIG_PROPERTY_WHERE);
                if (configDeviceWhere == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-where");
                } else {
                    Where w;
                    try {
                        if (brH.isBusGateway()) {
                            w = buildBusWhere(configDeviceWhere);
                        } else {
                            w = new WhereZigBee(configDeviceWhere);
                        }
                    } catch (IllegalArgumentException ia) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "@text/offline.conf-error-where");
                        return;
                    }
                    deviceWhere = w;
                    final String oid = brH.ownIdFromDeviceWhere(w, this);
                    ownId = oid;
                    Map<String, String> properties = editProperties();
                    properties.put(PROPERTY_OWNID, oid);
                    updateProperties(properties);
                    brH.registerDevice(oid, this);
                    logger.debug("associated thing to bridge with ownId={}", ownId);
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/unknown.waiting-state");
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-bridge");
        }
    }

    @Override
    public void handleCommand(ChannelUID channel, Command command) {
        logger.debug("handleCommand() (command={} - channel={})", command, channel);
        OpenWebNetBridgeHandler handler = bridgeHandler;
        if (handler != null) {
            OpenGateway gw = handler.gateway;
            if (gw != null && !gw.isConnected()) {
                logger.info("Cannot handle {} command for {}: gateway is not connected", command, getThing().getUID());
                return;
            }
            if (deviceWhere == null) {
                logger.info("Cannot handle {} command for {}: 'where' parameter is not configured or is invalid",
                        command, getThing().getUID());
                return;
            }
            if (command instanceof RefreshType) {
                requestChannelState(channel);
                // set a schedule to put device OFFLINE if no answer is received after THING_STATE_REQ_TIMEOUT_SEC
                refreshTimeout = scheduler.schedule(() -> {
                    if (thing.getStatus().equals(ThingStatus.UNKNOWN)) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Could not get channel state (timer expired)");
                    }
                }, THING_STATE_REQ_TIMEOUT_SEC, TimeUnit.SECONDS);
            } else {
                handleChannelCommand(channel, command);
            }
        } else {
            logger.debug("Thing {} is not associated to any gateway, skipping command", getThing().getUID());
        }
    }

    /**
     * Handles a command for the specific channel for this thing.
     * It must be implemented by each specific OpenWebNet category of device (WHO), based on channel
     *
     * @param channel specific ChannleUID
     * @param command the Command to be executed
     */
    protected abstract void handleChannelCommand(ChannelUID channel, Command command);

    /**
     * Handle incoming message from OWN network via bridge Thing, directed to this device. It should be further
     * implemented by each specific device handler.
     *
     * @param msg the message to handle
     */
    protected void handleMessage(BaseOpenMessage msg) {
        ThingStatus ts = getThing().getStatus();
        if (ThingStatus.ONLINE != ts && ThingStatus.REMOVING != ts && ThingStatus.REMOVED != ts) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Helper method to send OWN messages from ThingHandlers
     */
    public @Nullable Response send(OpenMessage msg) throws OWNException {
        OpenWebNetBridgeHandler bh = bridgeHandler;
        if (bh != null) {
            OpenGateway gw = bh.gateway;
            if (gw != null) {
                return gw.send(msg);
            }
        }
        logger.warn("Couldn't send message {}: handler or gateway is null", msg);
        return null;
    }

    /**
     * Helper method to send with high priority OWN messages from ThingsHandlers
     */
    protected @Nullable Response sendHighPriority(OpenMessage msg) throws OWNException {
        OpenWebNetBridgeHandler handler = bridgeHandler;
        if (handler != null) {
            OpenGateway gw = handler.gateway;
            if (gw != null) {
                return gw.sendHighPriority(msg);
            }
        }
        return null;
    }

    /**
     * Request the state for the specified channel
     *
     * @param channel the {@link ChannelUID} to request the state for
     */
    protected abstract void requestChannelState(ChannelUID channel);

    /**
     * Refresh the device
     *
     * @param refreshAll set true if all devices of the binding should be refreshed with one command, if possible
     */
    protected abstract void refreshDevice(boolean refreshAll);

    /**
     * Abstract builder for device Where address, to be implemented by each subclass to choose the right Where subclass
     * (the method is used only if the Thing is associated to a BUS gateway).
     *
     * @param wStr the WHERE string
     */
    protected abstract Where buildBusWhere(String wStr) throws IllegalArgumentException;

    @Override
    public void dispose() {
        OpenWebNetBridgeHandler bh = bridgeHandler;
        String oid = ownId;
        if (bh != null && oid != null) {
            bh.unregisterDevice(oid);
        }
        ScheduledFuture<?> sc = refreshTimeout;
        if (sc != null) {
            sc.cancel(true);
        }
        super.dispose();
    }

    /**
     * Helper method to return a Quantity from a Number value or UnDefType.NULL if value is null
     *
     * @param value to be used
     * @param unit to be used
     * @return Quantity
     */
    protected <U extends Quantity<U>> State getAsQuantityTypeOrNull(@Nullable Number value, Unit<U> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    /**
     * Returns a prefix String for ownId specific for each handler. To be implemented by sub-classes.
     *
     * @return
     */
    protected abstract String ownIdPrefix();
}
