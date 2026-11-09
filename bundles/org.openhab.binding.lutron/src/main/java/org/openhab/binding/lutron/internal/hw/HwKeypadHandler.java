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

import static org.openhab.binding.lutron.internal.hw.HwConstants.CHANNEL_PRESS;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.KeypadComponent;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigHw;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a keypad of a legacy HomeWorks processor.
 *
 * Button events arrive as {@code KBP}, {@code KBR}, {@code KBH} and {@code KBDT} messages, and the state of all LEDs
 * of a keypad as a {@code KLS} message. The processor reports what a keypad does, so every message is passed on as it
 * arrives.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class HwKeypadHandler extends BaseThingHandler {
    private static final ChannelTypeUID BUTTON_CHANNEL_TYPE = new ChannelTypeUID("lutron", "buttonTrigger");
    private static final ChannelTypeUID LED_CHANNEL_TYPE = new ChannelTypeUID("lutron", "hwLedState");
    private static final ChannelTypeUID PRESS_CHANNEL_TYPE = new ChannelTypeUID("lutron", "hwKeypadPress");

    // The index of a state is the digit used for it by KLS and SETLED
    private static final List<String> LED_STATES = List.of("OFF", "ON", "FLASH1", "FLASH2");

    private final Logger logger = LoggerFactory.getLogger(HwKeypadHandler.class);

    private final KeypadConfigHw keypadConfig = new KeypadConfigHw();
    private final KeypadChannelSupport support = new KeypadChannelSupport(new SupportHost(), BUTTON_CHANNEL_TYPE,
            LED_CHANNEL_TYPE, "String");

    private volatile @Nullable String address;

    public HwKeypadHandler(Thing thing) {
        super(thing);
    }

    private class SupportHost implements KeypadChannelSupport.Host {
        @Override
        public Thing getThing() {
            return HwKeypadHandler.this.getThing();
        }

        @Override
        public ThingBuilder editThing() {
            return HwKeypadHandler.this.editThing();
        }

        @Override
        public void updateThing(Thing thing) {
            HwKeypadHandler.this.updateThing(thing);
        }

        @Override
        public void triggerChannel(ChannelUID channelUID, String event) {
            HwKeypadHandler.this.triggerChannel(channelUID, event);
        }

        @Override
        public void updateState(ChannelUID channelUID, State state) {
            HwKeypadHandler.this.updateState(channelUID, state);
        }
    }

    @Override
    public void initialize() {
        HwKeypadConfig config = getConfigAs(HwKeypadConfig.class);

        if (config.address.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Address not set");
            return;
        }
        String model = config.model.toUpperCase(Locale.ROOT);
        List<KeypadComponent> components = keypadConfig.getComponents(model);
        if (components.isEmpty()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "Unknown model " + config.model);
            return;
        }
        address = HwAddress.normalize(config.address);

        Channel press = ChannelBuilder.create(new ChannelUID(thing.getUID(), CHANNEL_PRESS), "String")
                .withType(PRESS_CHANNEL_TYPE).build();
        support.configure(components, List.of(press));

        Bridge bridge = getBridge();
        if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        queryLeds();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && address != null) {
            // The query sent during initialization is lost if the bridge was not connected yet, and the LEDs can
            // have changed while the connection was down.
            queryLeds();
        }
    }

    public @Nullable String getAddress() {
        return address;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_PRESS.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                return;
            }
            handlePress(command.toString());
            return;
        }
        Integer id = support.componentFor(channelUID);
        if (id == null || !keypadConfig.isLed(id)) {
            return;
        }
        if (command instanceof RefreshType) {
            queryLeds();
            return;
        }
        int state = LED_STATES.indexOf(command.toString().toUpperCase(Locale.ROOT));
        if (state < 0) {
            logger.debug("Unsupported LED state '{}' for {}", command, channelUID);
            return;
        }
        send(String.format("SETLED, %s, %d, %d", address, id - KeypadConfigHw.ledId(0), state));
    }

    private void handlePress(String value) {
        int button;
        try {
            button = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            button = -1;
        }
        if (!keypadConfig.isButton(KeypadConfigHw.buttonId(button))) {
            logger.debug("Ignoring press of '{}', it is not a button number between 1 and {}", value,
                    KeypadConfigHw.MAX_BUTTONS);
            return;
        }
        send(String.format("KBP, %s, %d", address, button));
        send(String.format("KBR, %s, %d", address, button));
    }

    private void queryLeds() {
        if (address != null) {
            send("RKLS, " + address);
        }
    }

    private void send(String command) {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HwBridgeHandler bridgeHandler) {
            bridgeHandler.sendCommand(command);
        } else {
            logger.debug("Bridge unavailable while sending command for Thing {}: {}", thing.getUID(), command);
        }
    }

    /**
     * Passes a button message of the processor on to the trigger channel of the button.
     *
     * @param action the L232 message name: KBP, KBR, KBH or KBDT
     * @param button the button number
     */
    void handleButton(String action, int button) {
        String event = switch (action) {
            case "KBP" -> CommonTriggerEvents.PRESSED;
            case "KBR" -> CommonTriggerEvents.RELEASED;
            case "KBH" -> CommonTriggerEvents.LONG_PRESSED;
            case "KBDT" -> CommonTriggerEvents.DOUBLE_PRESSED;
            default -> null;
        };
        if (event != null && !support.trigger(KeypadConfigHw.buttonId(button), event)) {
            logger.debug("Keypad {} has no button {}", address, button);
        }
    }

    /**
     * Updates the LED channels from a KLS message.
     *
     * @param states one digit per LED, starting with LED 1
     */
    void handleLedStates(String states) {
        for (int i = 0; i < states.length() && i < KeypadConfigHw.MAX_BUTTONS; i++) {
            int digit = Character.digit(states.charAt(i), 10);
            if (digit >= 0 && digit < LED_STATES.size()) {
                support.update(KeypadConfigHw.ledId(i + 1), new StringType(LED_STATES.get(digit)));
            } else {
                logger.debug("Ignoring state '{}' of LED {} of keypad {}", states.charAt(i), i + 1, address);
            }
        }
    }
}
