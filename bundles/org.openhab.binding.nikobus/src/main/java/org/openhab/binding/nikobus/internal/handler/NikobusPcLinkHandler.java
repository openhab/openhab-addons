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
package org.openhab.binding.nikobus.internal.handler;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.CONFIG_REFRESH_INTERVAL;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikobus.internal.NikobusBindingConstants;
import org.openhab.binding.nikobus.internal.discovery.NikobusDiscoveryService;
import org.openhab.binding.nikobus.internal.protocol.NikobusCommand;
import org.openhab.binding.nikobus.internal.protocol.NikobusConnection;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.openhab.core.io.transport.serial.SerialPortManager;
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
 * The {@link NikobusPcLinkHandler} is responsible for handling commands, which
 * are sent or received from the PC-Link Nikobus component.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusPcLinkHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(NikobusPcLinkHandler.class);
    private final Map<String, Runnable> commandListeners = Collections.synchronizedMap(new HashMap<>());
    private final LinkedList<NikobusCommand> pendingCommands = new LinkedList<>();
    private final StringBuilder stringBuilder = new StringBuilder();
    private final SerialPortManager serialPortManager;
    private @Nullable NikobusConnection connection;
    private @Nullable NikobusCommand currentCommand;
    private @Nullable ScheduledFuture<?> scheduledRefreshFuture;
    private @Nullable ScheduledFuture<?> scheduledSendCommandWatchdogFuture;
    private @Nullable String ack;
    private @Nullable Consumer<String> unhandledCommandsProcessor;
    private int refreshThingIndex = 0;

    public NikobusPcLinkHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        ack = null;
        stringBuilder.setLength(0);

        updateStatus(ThingStatus.UNKNOWN);

        String portName = (String) getConfig().get(NikobusBindingConstants.CONFIG_PORT_NAME);
        if (portName == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Port must be set!");
            return;
        }

        connection = new NikobusConnection(serialPortManager, portName, this::processReceivedValue);

        int refreshInterval = ((Number) getConfig().get(CONFIG_REFRESH_INTERVAL)).intValue();
        scheduledRefreshFuture = scheduler.scheduleWithFixedDelay(this::refresh, refreshInterval, refreshInterval,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();

        Utils.cancel(scheduledSendCommandWatchdogFuture);
        scheduledSendCommandWatchdogFuture = null;

        Utils.cancel(scheduledRefreshFuture);
        scheduledRefreshFuture = null;

        NikobusConnection connection = this.connection;
        this.connection = null;

        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Noop.
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(NikobusDiscoveryService.class);
    }

    private void processReceivedValue(byte value) {
        logger.trace("Received {}", value);

        if (value == 13) {
            String command = stringBuilder.toString();
            stringBuilder.setLength(0);

            logger.debug("Received command '{}', ack = '{}'", command, ack);

            try {
                if (command.startsWith("$")) {
                    String ack = this.ack;
                    this.ack = null;

                    processResponse(command, ack);
                } else {
                    Runnable listener = commandListeners.get(command);
                    if (listener != null) {
                        listener.run();
                    } else {
                        Consumer<String> processor = unhandledCommandsProcessor;
                        if (processor != null) {
                            processor.accept(command);
                        }
                    }
                }
            } catch (RuntimeException e) {
                logger.debug("Processing command '{}' failed due {}", command, e.getMessage(), e);
            }
        } else {
            stringBuilder.append((char) value);

            // Take ACK part, i.e. "$0512"
            if (stringBuilder.length() == 5) {
                String payload = stringBuilder.toString();
                if (payload.startsWith("$05")) {
                    ack = payload;
                    logger.debug("Received ack '{}'", ack);
                    stringBuilder.setLength(0);
                }
            } else if (stringBuilder.length() > 128) {
                // Fuse, if for some reason we don't receive \r don't fill buffer.
                stringBuilder.setLength(0);
                logger.warn("Resetting read buffer, should not happen, am I connected to Nikobus?");
            }
        }
    }

    public void addListener(String command, Runnable listener) {
        if (commandListeners.put(command, listener) != null) {
            logger.warn("Multiple registrations for '{}'", command);
        }
    }

    public void removeListener(String command) {
        commandListeners.remove(command);
    }

    public void setUnhandledCommandProcessor(Consumer<String> processor) {
        if (unhandledCommandsProcessor != null) {
            logger.debug("Unexpected override of unhandledCommandsProcessor");
        }
        unhandledCommandsProcessor = processor;
    }

    public void resetUnhandledCommandProcessor() {
        unhandledCommandsProcessor = null;
    }

    private void processResponse(String commandPayload, @Nullable String ack) {
        NikobusCommand command;
        synchronized (pendingCommands) {
            command = currentCommand;
        }

        if (command == null) {
            logger.debug("Processing response but no command pending");
            return;
        }

        NikobusCommand.ResponseHandler responseHandler = command.getResponseHandler();
        if (responseHandler == null) {
            logger.debug("No response expected for current command");
            return;
        }

        if (ack == null) {
            logger.debug("No ack received");
            return;
        }

        String requestCommandId = command.getPayload().substring(3, 5);
        String ackCommandId = ack.substring(3, 5);
        if (!ackCommandId.equals(requestCommandId)) {
            logger.debug("Unexpected command's ack '{}' != '{}'", requestCommandId, ackCommandId);
            return;
        }

        // Check if response has expected length.
        if (commandPayload.length() != responseHandler.getResponseLength()) {
            logger.debug("Unexpected response length");
            return;
        }

        if (!commandPayload.startsWith(responseHandler.getResponseCode())) {
            logger.debug("Unexpected response command code");
            return;
        }

        String requestCommandAddress = command.getPayload().substring(5, 9);
        String ackCommandAddress = commandPayload.substring(responseHandler.getAddressStart(),
                responseHandler.getAddressStart() + 4);
        if (!requestCommandAddress.equals(ackCommandAddress)) {
            logger.debug("Unexpected response address");
            return;
        }

        if (responseHandler.complete(commandPayload)) {
            resetProcessingAndProcessNext();
        }
    }

    public void sendCommand(NikobusCommand command) {
        synchronized (pendingCommands) {
            pendingCommands.addLast(command);
        }

        scheduler.submit(this::processCommand);
    }

    private void processCommand() {
        NikobusCommand command;
        synchronized (pendingCommands) {
            if (currentCommand != null) {
                return;
            }

            command = pendingCommands.pollFirst();
            if (command == null) {
                return;
            }

            currentCommand = command;
        }
        sendCommand(command, 3);
    }

    private void sendCommand(NikobusCommand command, int retry) {
        logger.debug("Sending retry = {}, command '{}'", retry, command.getPayload());

        NikobusConnection connection = this.connection;
        if (connection == null) {
            return;
        }

        try {
            connectIfNeeded(connection);

            OutputStream outputStream = connection.getOutputStream();
            if (outputStream == null) {
                return;
            }
            outputStream.write(command.getPayload().getBytes());
            outputStream.flush();
        } catch (IOException e) {
            logger.debug("Sending command failed due {}", e.getMessage(), e);
            connection.close();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            NikobusCommand.ResponseHandler responseHandler = command.getResponseHandler();
            if (responseHandler == null) {
                resetProcessingAndProcessNext();
            } else if (retry > 0) {
                scheduleSendCommandTimeout(() -> {
                    if (!responseHandler.isCompleted()) {
                        sendCommand(command, retry - 1);
                    }
                });
            } else {
                scheduleSendCommandTimeout(() -> processTimeout(responseHandler));
            }
        }
    }

    private void scheduleSendCommandTimeout(Runnable command) {
        scheduledSendCommandWatchdogFuture = scheduler.schedule(command, 2, TimeUnit.SECONDS);
    }

    private void processTimeout(NikobusCommand.ResponseHandler responseHandler) {
        if (responseHandler.completeExceptionally(new TimeoutException("Waiting for response timed-out."))) {
            resetProcessingAndProcessNext();
        }
    }

    private void resetProcessingAndProcessNext() {
        Utils.cancel(scheduledSendCommandWatchdogFuture);
        synchronized (pendingCommands) {
            currentCommand = null;
        }
        scheduler.submit(this::processCommand);
    }

    private void refresh() {
        List<Thing> things = getThing().getThings().stream()
                .filter(thing -> thing.getHandler() instanceof NikobusModuleHandler).collect(Collectors.toList());

        // if there are command listeners (buttons) then we need an open connection even if no modules exist
        if (!commandListeners.isEmpty()) {
            NikobusConnection connection = this.connection;
            if (connection == null) {
                return;
            }
            try {
                connectIfNeeded(connection);
            } catch (IOException e) {
                connection.close();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return;
            }
        }

        if (things.isEmpty()) {
            logger.debug("Nothing to refresh");
            return;
        }

        refreshThingIndex = (refreshThingIndex + 1) % things.size();

        ThingHandler thingHandler = things.get(refreshThingIndex).getHandler();
        if (thingHandler == null) {
            return;
        }

        NikobusModuleHandler handler = (NikobusModuleHandler) thingHandler;
        handler.refreshModule();
    }

    private synchronized void connectIfNeeded(NikobusConnection connection) throws IOException {
        if (!connection.isConnected()) {
            connection.connect();

            // Send connection sequence, mimicking the Nikobus software. If this is not
            // sent, PC-Link sometimes does not forward button presses via serial interface.
            Stream.of(new String[] { "++++", "ATH0", "ATZ", "$10110000B8CF9D", "#L0", "#E0", "#L0", "#E1" })
                    .map(NikobusCommand::new).forEach(this::sendCommand);

            updateStatus(ThingStatus.ONLINE);
        }
    }
}
