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
package org.openhab.binding.lcn.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.openhab.binding.lcn.internal.common.LcnAddrMod;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnDefs.OutputPortDimMode;
import org.openhab.binding.lcn.internal.common.LcnException;
import org.openhab.binding.lcn.internal.connection.Connection;
import org.openhab.binding.lcn.internal.connection.ConnectionCallback;
import org.openhab.binding.lcn.internal.connection.ConnectionSettings;
import org.openhab.binding.lcn.internal.connection.ModInfo;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PckGatewayHandler} is responsible for the communication via a PCK gateway.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class PckGatewayHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PckGatewayHandler.class);
    private @Nullable Connection connection;
    private Optional<Consumer<String>> pckListener = Optional.empty();
    private @Nullable PckGatewayConfiguration config;

    public PckGatewayHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing
    }

    @Override
    public synchronized void initialize() {
        PckGatewayConfiguration localConfig = config = getConfigAs(PckGatewayConfiguration.class);

        String errorMessage = "Could not connect to LCN-PCHK/VISU: " + localConfig.getHostname() + ": ";

        try {
            OutputPortDimMode dimMode;
            String mode = localConfig.getMode();
            if (LcnDefs.OutputPortDimMode.NATIVE50.name().equalsIgnoreCase(mode)) {
                dimMode = LcnDefs.OutputPortDimMode.NATIVE50;
            } else if (LcnDefs.OutputPortDimMode.NATIVE200.name().equalsIgnoreCase(mode)) {
                dimMode = LcnDefs.OutputPortDimMode.NATIVE200;
            } else {
                throw new LcnException("DimMode " + mode + " is not supported");
            }

            ConnectionSettings settings = new ConnectionSettings("0", localConfig.getHostname(), localConfig.getPort(),
                    localConfig.getUsername(), localConfig.getPassword(), dimMode, LcnDefs.OutputPortStatusMode.PERCENT,
                    localConfig.getTimeoutMs());

            connection = new Connection(settings, scheduler, new ConnectionCallback() {
                @Override
                public void onOnline() {
                    updateStatus(ThingStatus.ONLINE);
                }

                @Override
                public void onOffline(@Nullable String errorMessage) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage + ".");
                }

                @Override
                public void onPckMessageReceived(String message) {
                    pckListener.ifPresent(l -> l.accept(message));
                    getThing().getThings().stream().filter(t -> t.getStatus() == ThingStatus.ONLINE).map(t -> {
                        LcnModuleHandler handler = (LcnModuleHandler) t.getHandler();
                        if (handler == null) {
                            logger.warn("Failed to process PCK message: Handler not set");
                        }
                        return handler;
                    }).filter(h -> h != null).forEach(h -> h.handleStatusMessage(message));
                }
            });

            updateStatus(ThingStatus.UNKNOWN);
        } catch (LcnException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage + e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LcnModuleDiscoveryService.class);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childThing.getThingTypeUID().equals(LcnBindingConstants.THING_TYPE_MODULE)
                || childThing.getThingTypeUID().equals(LcnBindingConstants.THING_TYPE_GROUP)) {
            try {
                LcnAddr addr = getLcnAddrFromThing(childThing);
                Connection localConnection = connection;
                if (localConnection != null) {
                    localConnection.removeLcnModule(addr);
                }
            } catch (LcnException e) {
                logger.warn("Failed to read configuration: {}", e.getMessage());
            }
        }
    }

    private LcnAddr getLcnAddrFromThing(Thing childThing) throws LcnException {
        LcnModuleHandler lcnModuleHandler = (LcnModuleHandler) childThing.getHandler();
        if (lcnModuleHandler != null) {
            return lcnModuleHandler.getCommandAddress();
        } else {
            throw new LcnException("Could not get module handler");
        }
    }

    /**
     * Enqueues a PCK (String) command to be sent to an LCN module.
     *
     * @param addr the modules address
     * @param wantsAck true, if the module shall send an ACK upon successful processing
     * @param pck the command to send
     */
    public void queue(LcnAddr addr, boolean wantsAck, String pck) {
        Connection localConnection = connection;
        if (localConnection != null) {
            localConnection.queue(addr, wantsAck, pck);
        } else {
            logger.warn("Dropped PCK command: {}", pck);
        }
    }

    /**
     * Enqueues a PCK (ByteBuffer) command to be sent to an LCN module.
     *
     * @param addr the modules address
     * @param wantsAck true, if the module shall send an ACK upon successful processing
     * @param pck the command to send
     */
    public void queue(LcnAddr addr, boolean wantsAck, byte[] pck) {
        Connection localConnection = connection;
        if (localConnection != null) {
            localConnection.queue(addr, wantsAck, pck);
        } else {
            logger.warn("Dropped PCK command of length: {}", pck.length);
        }
    }

    /**
     * Sends a broadcast message to all LCN modules: All LCN modules are requested to answer with an Ack.
     */
    void sendModuleDiscoveryCommand() {
        Connection localConnection = connection;
        if (localConnection != null) {
            localConnection.sendModuleDiscoveryCommand();
        }
    }

    /**
     * Send a request to an LCN module to respond with its serial number and firmware version.
     *
     * @param addr the module's address
     */
    void sendSerialNumberRequest(LcnAddrMod addr) {
        Connection localConnection = connection;
        if (localConnection != null) {
            localConnection.sendSerialNumberRequest(addr);
        }
    }

    /**
     * Send a request to an LCN module to respond with its configured name.
     *
     * @param addr the module's address
     */
    void sendModuleNameRequest(LcnAddrMod addr) {
        Connection localConnection = connection;
        if (localConnection != null) {
            localConnection.sendModuleNameRequest(addr);
        }
    }

    /**
     * Returns the ModInfo to a given module. Will be created if it doesn't exist,yet.
     *
     * @param addr the module's address
     * @return the ModInfo
     * @throws LcnException when this handler is not initialized, yet
     */
    ModInfo getModInfo(LcnAddrMod addr) throws LcnException {
        Connection localConnection = connection;
        if (localConnection != null) {
            return localConnection.updateModuleData(addr);
        } else {
            throw new LcnException("Connection is null");
        }
    }

    /**
     * Registers a listener to receive all PCK messages from this PCK gateway.
     *
     * @param listener the listener to add
     */
    void registerPckListener(Consumer<String> listener) {
        this.pckListener = Optional.of(listener);
    }

    /**
     * Removes all listeners for PCK messages from this PCK gateway.
     */
    void removeAllPckListeners() {
        this.pckListener = Optional.empty();
    }

    /**
     * Gets the Connection for this handler.
     *
     * @return the Connection
     */
    @Nullable
    public Connection getConnection() {
        return connection;
    }

    /**
     * Gets the local segment ID. When no segments are used, the value is 0.
     *
     * @return the local segment ID
     */
    public int getLocalSegmentId() {
        Connection localConnection = connection;
        if (localConnection != null) {
            return localConnection.getLocalSegId();
        } else {
            return 0;
        }
    }

    /**
     * Translates the given physical segment ID (0 or 4 if local segment) to the logical segment ID (local segment ID).
     *
     * @param physicalSegmentId the segment ID to convert
     * @return the converted segment ID
     */
    public int toLogicalSegmentId(int physicalSegmentId) {
        int localSegmentId = getLocalSegmentId();
        if ((physicalSegmentId == 0 || physicalSegmentId == 4) && localSegmentId != -1) {
            // PCK message came from local segment
            // physicalSegmentId == 0 => Module is programmed to send status messages to local segment only
            // physicalSegmentId == 4 => Module is programmed to send status messages globally (to all segments)
            // or segment coupler scan did not finish, yet (-1). Assume local segment, then.
            return localSegmentId;
        } else {
            return physicalSegmentId;
        }
    }

    @Override
    public void dispose() {
        Connection localConnection = connection;
        if (localConnection != null) {
            localConnection.shutdown();
        }
    }

    /**
     * Gets the configured connection timeout for the PCK gateway.
     *
     * @return the timeout in ms
     */
    public long getTimeoutMs() {
        PckGatewayConfiguration localConfig = config;
        return localConfig != null ? localConfig.getTimeoutMs() : 3500;
    }
}
