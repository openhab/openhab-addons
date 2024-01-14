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
package org.openhab.binding.linuxinput.internal;

import static org.openhab.binding.linuxinput.internal.LinuxInputBindingConstants.*;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linuxinput.internal.evdev4j.EvdevDevice;
import org.openhab.binding.linuxinput.internal.evdev4j.jnr.EvdevLibrary;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for Linux Input devices.
 *
 * @author Thomas Weißschuh - Initial contribution
 */
@NonNullByDefault
public final class LinuxInputHandler extends DeviceReadingHandler {

    private final Logger logger = LoggerFactory.getLogger(LinuxInputHandler.class);

    private final Map<Integer, Channel> channels;
    private final Channel keyChannel;
    private @Nullable EvdevDevice device;
    private final @Nullable String defaultLabel;

    private @NonNullByDefault({}) LinuxInputConfiguration config;

    public LinuxInputHandler(Thing thing, @Nullable String defaultLabel) {
        super(thing);
        this.defaultLabel = defaultLabel;

        keyChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), "key"), CoreItemFactory.STRING)
                .withType(CHANNEL_TYPE_KEY).build();
        channels = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /* no commands to handle */
    }

    @Override
    boolean immediateSetup() {
        config = getConfigAs(LinuxInputConfiguration.class);
        channels.clear();
        String statusDesc = null;
        if (!config.enable) {
            statusDesc = "Administratively disabled";
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, statusDesc);
        return true;
    }

    @Override
    boolean delayedSetup() throws IOException {
        ThingBuilder customizer = editThing();
        List<Channel> newChannels = new ArrayList<>();
        newChannels.add(keyChannel);
        EvdevDevice newDevice = new EvdevDevice(config.path);
        for (EvdevDevice.Key o : newDevice.enumerateKeys()) {
            String name = o.getName();
            if (name == null) {
                name = Integer.toString(o.getCode());
            }
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(thing.getUID(), CHANNEL_GROUP_KEYPRESSES_ID, name), CoreItemFactory.CONTACT)
                    .withLabel(name).withType(CHANNEL_TYPE_KEY_PRESS).withDescription("Event Code " + o.getCode())
                    .build();
            channels.put(o.getCode(), channel);
            newChannels.add(channel);
        }
        if (Objects.equals(defaultLabel, thing.getLabel())) {
            customizer.withLabel(newDevice.getName());
        }
        customizer.withChannels(newChannels);
        Map<String, String> props = getProperties(Objects.requireNonNull(newDevice));
        customizer.withProperties(props);
        updateThing(customizer.build());
        for (Channel channel : newChannels) {
            updateState(channel.getUID(), OpenClosedType.OPEN);
        }
        if (config.enable) {
            updateStatus(ThingStatus.ONLINE);
        }
        device = newDevice;
        return config.enable;
    }

    @Override
    protected void closeDevice() throws IOException {
        @Nullable
        EvdevDevice currentDevice = device;
        device = null;

        if (currentDevice != null) {
            currentDevice.close();
        }
        logger.debug("Device {} closed", this);
    }

    @Override
    String getInstanceName() {
        LinuxInputConfiguration c = config;
        if (c == null || c.path == null) {
            return "unknown";
        }
        return c.path;
    }

    @Override
    void handleEventsInThread() throws IOException {
        try (Selector selector = EvdevDevice.openSelector()) {
            @Nullable
            EvdevDevice currentDevice = device;
            if (currentDevice == null) {
                throw new IOException("trying to handle events without a device");
            }
            SelectionKey evdevReady = currentDevice.register(selector);

            logger.debug("Grabbing device {}", currentDevice);
            currentDevice.grab(); // ungrab will happen implicitly at device.close()

            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    logger.debug("Thread interrupted, exiting");
                    break;
                }
                logger.trace("Waiting for event");
                selector.select(20_000);
                if (selector.selectedKeys().remove(evdevReady)) {
                    while (true) {
                        Optional<EvdevDevice.InputEvent> ev = currentDevice.nextEvent();
                        if (ev.isEmpty()) {
                            break;
                        }
                        handleEvent(ev.get());
                    }
                }
            }
        }
    }

    private void handleEvent(EvdevDevice.InputEvent event) {
        if (event.type() != EvdevLibrary.Type.KEY) {
            return;
        }
        @Nullable
        Channel channel = channels.get(event.getCode());
        if (channel == null) {
            String msg = "Could not find channel for code {}";
            if (isInitialized()) {
                logger.warn(msg, event.getCode());
            } else {
                logger.debug(msg, event.getCode());
            }
            return;
        }
        logger.debug("Got event: {}", event);
        // Documented in README.md
        int eventValue = event.getValue();
        switch (eventValue) {
            case EvdevLibrary.KeyEventValue.DOWN:
                String keyCode = channel.getUID().getIdWithoutGroup();
                updateState(keyChannel.getUID(), new StringType(keyCode));
                updateState(channel.getUID(), OpenClosedType.CLOSED);
                triggerChannel(keyChannel.getUID(), keyCode);
                triggerChannel(channel.getUID(), CommonTriggerEvents.PRESSED);
                updateState(keyChannel.getUID(), new StringType());
                break;
            case EvdevLibrary.KeyEventValue.UP:
                updateState(channel.getUID(), OpenClosedType.OPEN);
                triggerChannel(channel.getUID(), CommonTriggerEvents.RELEASED);
                break;
            case EvdevLibrary.KeyEventValue.REPEAT:
                /* Ignored */
                break;
            default:
                logger.debug("Unexpected event value for channel {}: {}", channel, eventValue);
                break;
        }
    }

    private static Map<String, String> getProperties(EvdevDevice device) {
        Map<String, String> properties = new HashMap<>();
        properties.put("physicalLocation", device.getPhys());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getUniq());
        properties.put(Thing.PROPERTY_MODEL_ID, hex(device.getProdutId()));
        properties.put(Thing.PROPERTY_VENDOR, hex(device.getVendorId()));
        properties.put("busType", device.getBusType().map(Object::toString).orElseGet(() -> hex(device.getBusId())));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, hex(device.getVersionId()));
        properties.put("driverVersion", hex(device.getDriverVersion()));
        return properties;
    }

    private static String hex(int i) {
        return String.format("%04x", i);
    }
}
