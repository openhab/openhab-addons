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
import org.openhab.core.thing.ThingStatusInfo;
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
 * The {@link OpenWebNetThingHandler} is responsible for handling commands for an OpenWebNet device.
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
    protected @Nullable Where deviceWhere; // this device WHERE address

    protected @Nullable ScheduledFuture<?> requestChannelStateTimeout;
    protected @Nullable ScheduledFuture<?> refreshTimeout;

    private static final int ALL_DEVICES_REFRESH_INTERVAL_MSEC = 60_000; // interval before sending another
                                                                         // refreshAllDevices request

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
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/unknown.waiting-state");
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channel, Command command) {
        logger.debug("handleCommand() (command={} - channel={})", command, channel);
        OpenWebNetBridgeHandler handler = bridgeHandler;
        if (handler != null) {
            OpenGateway gw = handler.gateway;
            if (gw != null && !gw.isConnected()) {
                logger.info("Cannot handle {} command for {}: gateway is not connected", command, thing.getUID());
                return;
            }
            if (deviceWhere == null) {
                logger.info("Cannot handle {} command for {}: 'where' parameter is not configured or is invalid",
                        command, thing.getUID());
                return;
            }
            if (command instanceof RefreshType) {
                requestChannelState(channel);
            } else {
                handleChannelCommand(channel, command);
            }
        } else {
            logger.debug("Thing {} is not associated to any gateway, skipping command", getThing().getUID());
        }
    }

    /**
     * Handles a command for the specific channel for this thing.
     * It must be further implemented by each specific device handler.
     *
     * @param channel the {@link ChannelUID}
     * @param command the Command to be executed
     */
    protected abstract void handleChannelCommand(ChannelUID channel, Command command);

    /**
     * Handle incoming message from OWN network via bridge Thing, directed to this device.
     * It should be further implemented by each specific device handler.
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
     * Helper method to send OWN messages from handler.
     *
     * @param msg the OpenMessage to be sent
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
     * Helper method to send with high priority OWN messages from handler.
     *
     * @param msg the OpenMessage to be sent
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
     * Request the state for the specified channel. If no answer is received within THING_STATE_REQ_TIMEOUT_SEC, it is
     * put OFFLINE.
     * The method must be further implemented by each specific handler.
     *
     * @param channel the {@link ChannelUID} to request the state for
     */
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("requestChannelState() {}", channel);
        Where w = deviceWhere;
        if (w == null) {
            logger.warn("Could not requestChannelState(): deviceWhere is null for thing {}", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.conf-error-where");
            return;
        }
        // set a schedule to put device OFFLINE if no answer is received after THING_STATE_REQ_TIMEOUT_SEC
        requestChannelStateTimeout = scheduler.schedule(() -> {
            if (thing.getStatus().equals(ThingStatus.UNKNOWN)) {
                logger.debug("requestChannelState() TIMEOUT for thing {}", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-state");
            }
        }, THING_STATE_REQ_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    /**
     * Refresh a device, possibly using a single OWN command if refreshAll=true and if supported.
     * The method must be further implemented by each specific handler.
     *
     * @param refreshAll true if all devices for this handler must be refreshed with a single OWN command, if supported,
     *            otherwise just refresh the single device.
     */
    protected abstract void refreshDevice(boolean refreshAll);

    /**
     * If the subclass supports refreshing all devices with a single OWN command, returns the last TS when a refreshAll
     * was requested, or 0 if not requested yet. If not supported return -1 (default).
     * It must be implemented by each subclass that supports all devices refresh.
     *
     * @return timestamp when last refreshAll command was sent, 0 if not requested yet, or -1 if it's not supported by
     *         subclass.
     */
    protected long getRefreshAllLastTS() {
        return -1;
    };

    /**
     * Refresh all devices for this handler
     */
    protected void refreshAllDevices() {
        logger.debug("--- refreshAllDevices() for device {}", thing.getUID());
        OpenWebNetBridgeHandler brH = bridgeHandler;
        if (brH != null) {
            long refAllTS = getRefreshAllLastTS();
            logger.debug("{} support = {}", thing.getUID(), refAllTS >= 0);
            if (brH.isBusGateway() && refAllTS >= 0) {
                long now = System.currentTimeMillis();
                if (now - refAllTS > ALL_DEVICES_REFRESH_INTERVAL_MSEC) {
                    logger.debug("--- refreshAllDevices() : refreshing ALL devices... ({})", thing.getUID());
                    refreshDevice(true);
                } else {
                    logger.debug("--- refreshAllDevices() : refresh all devices sent {}msec ago, skipping... ({})",
                            ALL_DEVICES_REFRESH_INTERVAL_MSEC, thing.getUID());
                }
                // sometimes GENERAL (e.g. #*1*0##) refresh requests do not return state for all devices, so let's
                // schedule another single refresh device, just in case
                refreshTimeout = scheduler.schedule(() -> {
                    if (thing.getStatus().equals(ThingStatus.UNKNOWN)) {
                        logger.debug(
                                "--- refreshAllDevices() : schedule expired: --UNKNOWN-- status for {}. Refreshing it...",
                                thing.getUID());
                        refreshDevice(false);
                    } else {
                        logger.debug("--- refreshAllDevices() : schedule expired: ONLINE status for {}",
                                thing.getUID());
                    }
                }, THING_STATE_REQ_TIMEOUT_SEC, TimeUnit.SECONDS);
            } else { // USB device or AllDevicesRefresh not supported
                refreshDevice(false);
            }
        }
    }

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
        ScheduledFuture<?> rcst = requestChannelStateTimeout;
        if (rcst != null) {
            rcst.cancel(true);
        }
        ScheduledFuture<?> rt = refreshTimeout;
        if (rt != null) {
            rt.cancel(true);
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
