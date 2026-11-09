/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.hw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the bridges to a legacy HomeWorks processor. The processor speaks the same L232 command set over its
 * RS232 ports and over its Ethernet (telnet) port, so the handling of incoming messages is shared. Subclasses provide
 * the transport.
 *
 * @author Andrew Shilliday - Initial contribution of the serial bridge handler this class was extracted from
 * @author Christopher Smith - Extracted shared processor message handling for the telnet bridge
 */
@NonNullByDefault
public abstract class HwBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(HwBridgeHandler.class);

    private final List<Consumer<String>> lineListeners = new CopyOnWriteArrayList<>();

    private @Nullable HwDiscoveryService discoveryService;

    protected HwBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /**
     * Sends a command to the processor. The command is a single L232 command line without a line ending.
     */
    public abstract void sendCommand(String command);

    /**
     * Sends the commands that put the processor in the state the binding expects: no prompts, and monitoring of
     * dimmer levels, keypad buttons and keypad LEDs.
     */
    protected void sendStartupCommands() {
        logger.debug("Sending monitoring commands.");
        sendCommand("PROMPTOFF");
        sendCommand("KBMON"); // Keypad button events, and dimmer tap events
        sendCommand("KLMON"); // Keypad LED states
        sendCommand("GSMOFF");
        sendCommand("DLMON"); // Turn on dimmer monitoring
    }

    public void setDiscoveryService(HwDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Registers a listener that receives every message line from the processor, whether or not the binding handles it.
     * Used by the console command to show what the processor sends.
     */
    public void addLineListener(Consumer<String> listener) {
        lineListeners.add(listener);
    }

    public void removeLineListener(Consumer<String> listener) {
        lineListeners.remove(listener);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HwDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Unexpected command for HomeWorks Bridge: {} - {}", channelUID, command);
    }

    protected void handleIncomingMessage(@Nullable String line) {
        if (line == null || line.isEmpty()) {
            return;
        }

        logger.debug("Received message from HomeWorks processor: {}", line);
        for (Consumer<String> listener : lineListeners) {
            try {
                listener.accept(line);
            } catch (RuntimeException e) {
                logger.debug("Line listener failed: {}", e.getMessage(), e);
            }
        }

        String[] data = line.replaceAll("\\s", "").toUpperCase().split(",");
        try {
            switch (data[0]) {
                case "DL" -> handleDimmerLevel(data);
                case "KBP", "KBR", "KBH", "KBDT" -> handleKeypadButton(data);
                case "KLS" -> handleKeypadLeds(data);
                case "DBP", "DBR", "DBH", "DBDT" -> handleDimmerTap(data);
                default -> {
                    // SVS and SVB messages of Sivoia shades are not supported
                }
            }
        } catch (RuntimeException e) {
            logger.debug("Could not parse message '{}' from HomeWorks processor: {}", line, e.getMessage());
        }
    }

    private void handleDimmerLevel(String[] data) {
        String address = HwAddress.normalize(data[1]);
        // Levels are usually whole numbers, but some devices report decimals, such as 100.00
        BigDecimal reported = new BigDecimal(data[2]);
        Integer level = Math.max(0, Math.min(100, reported.setScale(0, RoundingMode.HALF_UP).intValue()));
        HwDimmerHandler handler = findHandler(HwDimmerHandler.class, address, HwDimmerHandler::getAddress);
        if (handler == null) {
            HwDiscoveryService service = discoveryService;
            if (HwAddress.isQedShade(address)) {
                logger.debug("Not discovering {} as a dimmer, it can only be a QED shade", address);
            } else if (service != null) {
                service.declareUnknownDimmer(address);
            }
        } else {
            handler.handleLevelChange(level);
        }
    }

    private void handleKeypadButton(String[] data) {
        String address = HwAddress.normalize(data[1]);
        int button = Integer.parseInt(data[2]);
        HwKeypadHandler handler = findHandler(HwKeypadHandler.class, address, HwKeypadHandler::getAddress);
        if (handler == null) {
            declareUnknownKeypad(address);
        } else {
            handler.handleButton(data[0], button);
        }
    }

    private void handleKeypadLeds(String[] data) {
        String address = HwAddress.normalize(data[1]);
        HwKeypadHandler handler = findHandler(HwKeypadHandler.class, address, HwKeypadHandler::getAddress);
        if (handler == null) {
            declareUnknownKeypad(address);
        } else {
            handler.handleLedStates(data[2]);
        }
    }

    private void handleDimmerTap(String[] data) {
        String address = HwAddress.normalize(data[1]);
        HwDimmerHandler handler = findHandler(HwDimmerHandler.class, address, HwDimmerHandler::getAddress);
        if (handler != null) {
            handler.handleTap(data[0].replace("DB", "KB"));
        }
    }

    private void declareUnknownKeypad(String address) {
        HwDiscoveryService service = discoveryService;
        if (service != null) {
            service.declareUnknownKeypad(address);
        }
    }

    private <T extends ThingHandler> @Nullable T findHandler(Class<T> type, String address,
            Function<T, @Nullable String> addressOf) {
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (type.isInstance(handler)) {
                T typed = type.cast(handler);
                String handlerAddress = addressOf.apply(typed);
                if (handlerAddress != null && address.equals(HwAddress.normalize(handlerAddress))) {
                    return typed;
                }
            }
        }
        return null;
    }
}
