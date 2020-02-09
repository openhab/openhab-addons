/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dreamscreen.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.smarthome.core.library.types.OnOffType.*;
import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DreamScreenHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public class DreamScreenHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(DreamScreenHandler.class);
    private final DreamScreenDatagramServer server;
    private final @Nullable DreamScreenDynamicStateDescriptionProvider descriptionProvider;

    private @Nullable DreamScreenConfiguration config;
    private int group;
    private boolean powerOn;
    private DreamScreenEnumMode powerOnMode = DreamScreenEnumMode.VIDEO; // TODO: Can this be persisted somewhere?
    private @Nullable InetAddress address;

    public DreamScreenHandler(Thing thing, DreamScreenDatagramServer server,
            @Nullable DreamScreenDynamicStateDescriptionProvider descriptionProvider) {
        super(thing);
        this.server = server;
        this.descriptionProvider = descriptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(DreamScreenConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
        try {
            server.register(this, scheduler);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot initialize " + getName());
        }
        });
    }

    @Override
    public void dispose() {
        final ThingUID thingUID = this.getThing().getUID();
        server.unregister(this);
        super.dispose();
        final DreamScreenDynamicStateDescriptionProvider descProvider = this.descriptionProvider;
        if (descProvider != null) {
            descProvider.removeThingDescriptions(thingUID);
        }
    }

    @Nullable
    String getName() {
        DreamScreenConfiguration config = this.config;
        return config == null ? null : config.name;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    changePower((OnOffType) command);
                }
                break;
            case CHANNEL_MODE:
                if (command instanceof DecimalType) {
                    changeMode((DecimalType) command);
                }
                break;
            case CHANNEL_SCENE:
                if (command instanceof DecimalType) {
                    changeScene((DecimalType) command);
                }
            case CHANNEL_INPUT:
                if (command instanceof DecimalType) {
                    changeInput((DecimalType) command);
                }
                break;
        }
    }

    private void changePower(OnOffType command) {
        try {
            send(0x03, 0x01, new byte[] { command == ON ? powerOnMode.deviceMode : 0 });
            this.powerOn = command == ON;
            updateState(CHANNEL_POWER, command);
        } catch (IOException e) {
            logger.error("Error changing power state", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot send power command to " + getName());
        }
    }

    private void changeMode(DecimalType state) {
        try {
            final DreamScreenEnumMode mode = DreamScreenEnumMode.fromState(state);
            if (this.powerOn) {
                send(0x03, 0x01, new byte[] { mode.deviceMode });
            }
            this.powerOnMode = mode;
            updateState(CHANNEL_MODE, state);
        } catch (IOException e) {
            logger.error("Error changing mode", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot send mode command to " + getName());
        }
    }

    private void changeScene(DecimalType state) {
        try {
            final DreamScreenEnumScene scene = DreamScreenEnumScene.fromState(state);
            send(0x03, 0x08, new byte[] { scene.deviceAmbientSceneType });
            if (scene.deviceAmbientSceneType == 1) {
                send(0x03, 0x0D, new byte[] { scene.deviceAmbientScene });
            }
            updateState(CHANNEL_SCENE, state);
        } catch (IOException e) {
            logger.error("Error changing scene", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot send scene command to " + getName());
        }
    }

    private void changeInput(DecimalType state) {
        try {
            send(0x03, 0x20, new byte[] { state.byteValue() });
            updateState(CHANNEL_INPUT, state);
        } catch (IOException e) {
            logger.error("Error changing input", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot send input change command to " + getName());
        }
    }

    private void send(int commandUpper, int commandLower, byte[] payload) throws IOException {
        if (this.address != null) {
            server.send(this.group, commandUpper, commandLower, payload, this.address);
        } else {
            logger.warn("DreamScreen {} is not on-line", getName());
        }
    }

    void refreshState(final byte[] data, int off, int len, InetAddress address) {
        this.address = address;
        this.group = data[off + 38];

        logger.info("{}", data.toString());
        refreshMode(data[off + 39]);
        refreshAmbientScene((byte) 0, data[off + 68]);
        refreshInputNames(new String(data, off + 81, 16, UTF_8), //
                new String(data, off + 97, 16, UTF_8), //
                new String(data, off + 113, 16, UTF_8));
        updateState(CHANNEL_INPUT, new DecimalType(data[off + 79]));
        updateStatus(ThingStatus.ONLINE);
    }

    private void refreshMode(final byte deviceMode) {
        final DreamScreenEnumMode mode = DreamScreenEnumMode.fromDevice(deviceMode);
        if (mode == null) {
            this.powerOn = false;
            updateState(CHANNEL_POWER, OFF);
        } else {
            this.powerOn = true;
            this.powerOnMode = mode;
            switch (DreamScreenEnumMode.fromDevice(deviceMode)) {
                case VIDEO:
                    updateState(CHANNEL_POWER, ON);
                    updateState(CHANNEL_MODE, DreamScreenEnumMode.VIDEO.state());
                    break;
                case MUSIC:
                    updateState(CHANNEL_POWER, ON);
                    updateState(CHANNEL_MODE, DreamScreenEnumMode.MUSIC.state());
                    break;
                case AMBIENT:
                    updateState(CHANNEL_POWER, ON);
                    updateState(CHANNEL_MODE, DreamScreenEnumMode.AMBIENT.state());
                    break;
            }
        }
    }

    private void refreshAmbientScene(final byte deviceAmbientModeType, final byte deviceAmbientScene) {
        final DreamScreenEnumScene scene = DreamScreenEnumScene.fromDevice(deviceAmbientModeType, deviceAmbientScene);
        updateState(CHANNEL_SCENE, scene.state());
    }

    private void refreshInputNames(String channel1, String channel2, String channel3) {
        final DreamScreenDynamicStateDescriptionProvider descProvider = this.descriptionProvider;
        if (descProvider != null) {
            final ChannelUID inputChannelUID = new ChannelUID(this.getThing().getUID(), CHANNEL_INPUT);
            final List<StateOption> options = Arrays.asList( //
                    new StateOption("0", channel1.trim()), new StateOption("1", channel2.trim()),
                    new StateOption("2", channel3.trim()));
            final StateDescription description = new StateDescription(BigDecimal.ZERO, BigDecimal.valueOf(2),
                    BigDecimal.ONE, null, false, options);
            descProvider.setChannelDescription(inputChannelUID, description);
        }

    }
}
