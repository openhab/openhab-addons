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
package org.openhab.binding.pentair.internal.handler;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.actions.PentairWriter;
import org.openhab.binding.pentair.internal.actions.PentairWriter.CallbackWriter;
import org.openhab.binding.pentair.internal.config.PentairBaseBridgeConfig;
import org.openhab.binding.pentair.internal.discovery.PentairDiscoveryService;
import org.openhab.binding.pentair.internal.parser.PentairIntelliChlorPacket;
import org.openhab.binding.pentair.internal.parser.PentairParser;
import org.openhab.binding.pentair.internal.parser.PentairParser.CallbackPentairParser;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
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
 * The {@link PentairBaseBridgeHandler } abstract class for all common functions for different bridge implementations.
 * Use as superclass for IPBridge and SerialBridge implementations.
 *
 * - Implements parsing of packets on Pentair bus and dispositions to appropriate Thing
 * - Periodically sends query to any {@link PentairIntelliFloHandler} things
 * - Provides function to write packets
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public abstract class PentairBaseBridgeHandler extends BaseBridgeHandler
        implements CallbackPentairParser, CallbackWriter {
    private final Logger logger = LoggerFactory.getLogger(PentairBaseBridgeHandler.class);

    private final PentairParser parser = new PentairParser();
    private final PentairWriter actions = new PentairWriter(this);

    // input/output stream must be assigned by sub-class in connect method
    @Nullable
    private BufferedInputStream inputStream;
    @Nullable
    private BufferedOutputStream outputStream;

    private @Nullable PentairDiscoveryService discoveryService;
    private @Nullable Thread parserThread;

    private @Nullable ScheduledFuture<?> monitorIOJob;

    private PentairBaseBridgeConfig config = new PentairBaseBridgeConfig();

    /** array to keep track of IDs seen on the Pentair bus that are not configured yet */
    private final Set<Integer> unregistered = new HashSet<Integer>();

    final Map<Integer, @Nullable PentairBaseThingHandler> equipment = new HashMap<>();

    // keep accessible a static reference to the bridge. This binding will only work with a single bridge
    @Nullable
    private static Bridge bridge;

    @Nullable
    public static Bridge getSingleBridge() {
        return bridge;
    }

    public void setDiscoveryService(PentairDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Gets pentair bus id
     *
     * @return id
     */
    public int getId() {
        return config.id;
    }

    public PentairWriter getBaseActions() {
        return actions;
    }

    PentairBaseBridgeHandler(Bridge bridge) {
        super(bridge);
        parser.setCallback(this);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(PentairDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(PentairBaseBridgeConfig.class);

        if (PentairBaseBridgeHandler.bridge != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-duplicate");
            return;
        }

        PentairBaseBridgeHandler.bridge = this.getThing();

        updateStatus(ThingStatus.UNKNOWN);

        this.monitorIOJob = scheduler.scheduleWithFixedDelay(this::monitorIO, 60, 30, TimeUnit.SECONDS);

        baseConnect();
    }

    @Override
    public void dispose() {
        PentairBaseBridgeHandler.bridge = null;

        ScheduledFuture<?> monitorIOJob = this.monitorIOJob;
        if (monitorIOJob != null) {
            monitorIOJob.cancel(true);
        }

        baseDisconnect();
    }

    /*
     * Custom function to call during initialization to notify the bridge. childHandlerInitialized is not called
     * until the child thing actually goes to the ONLINE status.
     */
    public void childHandlerInitializing(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PentairBaseThingHandler baseThingHandler) {
            equipment.put(baseThingHandler.getPentairID(), baseThingHandler);
            unregistered.remove(baseThingHandler.getPentairID());
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PentairBaseThingHandler baseThingHandler) {
            equipment.remove(baseThingHandler.getPentairID());
        }
    }

    /**
     * Abstract method for creating connection. Must be implemented in subclass.
     * Return 0 if all goes well. Must call setInputStream and setOutputStream before exciting.
     */
    protected abstract boolean connect();

    protected abstract void disconnect();

    private void baseConnect() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            return;
        }

        // montiorIOJob will only start after a successful connection
        if (monitorIOJob == null) {
            monitorIOJob = scheduler.scheduleWithFixedDelay(this::monitorIO, 60, 30, TimeUnit.SECONDS);
        }

        if (!connect()) {
            // if connect() sets to offline, preserve the StatusDetail\
            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE);
            }
            return;
        }

        Thread parserThread = new Thread(parser, "OH-pentair-" + this.getThing().getUID() + "-parser");
        this.parserThread = parserThread;

        parserThread.setDaemon(true);
        parserThread.start();

        if (inputStream == null || outputStream == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.iostream-error ");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void baseDisconnect() {
        // Preserve OFFLINE status detail if already OFFLINE
        if (getThing().getStatus() != ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        Thread parserThread = this.parserThread;
        if (parserThread != null) {
            try {
                parserThread.interrupt();
                parserThread.join(3000); // wait for thread to complete
            } catch (InterruptedException e) {
                // do nothing
            }
            parserThread = null;
        }

        BufferedInputStream reader = this.inputStream;
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.debug("setInputStream: Exception error while closing: {}", e.getMessage());
            }
        }

        BufferedOutputStream writer = this.outputStream;
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.debug("setOutputStream: Exception error while closing: {}", e.getMessage());
            }
        }

        disconnect();
    }

    public void setInputStream(InputStream inputStream) {
        BufferedInputStream reader = this.inputStream;
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.trace("setInputStream: Exception error while closing: {}", e.getMessage());
            }
        }

        this.inputStream = new BufferedInputStream(inputStream);
        parser.setInputStream(inputStream);
    }

    public void setOutputStream(OutputStream outputStream) {
        BufferedOutputStream writer = this.outputStream;
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.trace("setOutputStream: Exception error while closing: {}", e.getMessage());
            }
        }

        writer = new BufferedOutputStream(outputStream);
        this.outputStream = writer;

        actions.initialize(writer, getId());
    }

    // method to poll to try and reconnect upon being disconnected. Note this should only be started on an initial
    private void monitorIO() {
        ThingStatus thingStatus = getThing().getStatus();

        if (thingStatus == ThingStatus.ONLINE) {
            // Check if parser thread has terminated and if it has reconnect. This will take down the interface and
            // restart the interface.
            Thread parserThread = Objects.requireNonNull(this.parserThread);
            if (!parserThread.isAlive()) {
                baseDisconnect();
                baseConnect();
            }
        } else if (thingStatus == ThingStatus.OFFLINE) {
            baseConnect();
        }
    }

    /**
     * Helper function to find a Thing assigned to this bridge with a specific pentair bus id.
     *
     * @param id Pentair bus id
     * @return Thing object. null if id is not found.
     */
    public @Nullable Thing findThing(int id) {
        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            PentairBaseThingHandler handler = (PentairBaseThingHandler) t.getHandler();

            if (handler != null && handler.getPentairID() == id) {
                return t;
            }
        }

        return null;
    }

    public @Nullable PentairControllerHandler findController() {
        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            PentairBaseThingHandler handler = (PentairBaseThingHandler) t.getHandler();

            if (handler instanceof PentairControllerHandler controllerHandler) {
                return controllerHandler;
            }
        }

        return null;
    }

    public @Nullable PentairIntelliChlorHandler findIntellichlor() {
        List<Thing> things = getThing().getThings();

        for (Thing t : things) {
            PentairBaseThingHandler handler = (PentairBaseThingHandler) t.getHandler();

            if (handler instanceof PentairIntelliChlorHandler intelliChlorHandler) {
                return intelliChlorHandler;
            }
        }

        return null;
    }

    @Override
    public void onPentairPacket(PentairStandardPacket p) {
        PentairBaseThingHandler thinghandler;

        int source = p.getSource();
        thinghandler = equipment.get(source);

        if (thinghandler == null) {
            int sourceType = (source >> 4);

            if (sourceType == 0x02) { // control panels are 0x2*, don't treat as an
                                      // unregistered device
                logger.debug("[{}] Command from control panel device: {}", source, p);
            } else if (!unregistered.contains(source)) { // if not yet seen discover
                PentairDiscoveryService discoveryService = this.discoveryService;
                if (discoveryService != null) {
                    if (sourceType == 0x01) { // controller
                        PentairControllerHandler handler = this.findController();
                        if (handler == null) { // only register one controller
                            if (config.discovery) {
                                discoveryService.notifyDiscoveredThing(CONTROLLER_THING_TYPE, source, CONTROLLER);
                            }
                        }
                    } else if (sourceType == 0x06) {
                        if (config.discovery) {
                            int pumpid = (source & 0x04) + 1;
                            discoveryService.notifyDiscoveredThing(INTELLIFLO_THING_TYPE, source, "pump" + pumpid);
                        }
                    } else if (sourceType == 0x09) {
                        if (config.discovery) {
                            discoveryService.notifyDiscoveredThing(INTELLICHEM_THING_TYPE, source, INTELLICHEM);
                        }
                    }

                    logger.debug("[{}] First command from unregistered device: {}", source, p);
                    unregistered.add(source);
                }
            } else {
                logger.debug("[{}] Subsequent command from unregistered device: {}", source, p);
            }
        } else {
            logger.trace("[{}] Received pentair command: {}", source, p);

            thinghandler.processPacketFrom(p);
            actions.ackResponse(p.getAction());
        }
    }

    @Override
    public void onIntelliChlorPacket(PentairIntelliChlorPacket p) {
        PentairBaseThingHandler thinghandler;

        thinghandler = equipment.get(0);

        if (thinghandler == null) {
            // Only register if the packet is sent from chlorinator (i.e. action=0x12)
            int dest = p.getByte(PentairIntelliChlorPacket.DEST);
            if (!unregistered.contains(0) && p.getByte(PentairIntelliChlorPacket.ACTION) == 0x12) {
                PentairDiscoveryService discoveryService = this.discoveryService;

                if (config.discovery && discoveryService != null) {
                    discoveryService.notifyDiscoveredThing(INTELLICHLOR_THING_TYPE, 0, INTELLICHLOR);

                    logger.debug("[{}] First command from unregistered Intellichlor: {}", dest, p);
                    unregistered.add(0);
                }
            } else {
                logger.debug("[{}] Subsequent command from unregistered Intellichlor: {}", dest, p);
            }
            return;
        }

        thinghandler.processPacketFrom(p);
    }

    @Override
    public void writerFailureCallback() {
        baseDisconnect();
    }

    @Override
    public void parserFailureCallback() {
        baseDisconnect();
    }
}
