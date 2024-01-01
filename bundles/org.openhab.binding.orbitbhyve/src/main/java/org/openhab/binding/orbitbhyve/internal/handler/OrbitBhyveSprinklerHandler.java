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
package org.openhab.binding.orbitbhyve.internal.handler;

import static org.openhab.binding.orbitbhyve.internal.OrbitBhyveBindingConstants.*;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveDevice;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveDeviceStatus;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveProgram;
import org.openhab.binding.orbitbhyve.internal.model.OrbitBhyveZone;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OrbitBhyveSprinklerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveSprinklerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OrbitBhyveSprinklerHandler.class);

    public OrbitBhyveSprinklerHandler(Thing thing) {
        super(thing);
    }

    private int wateringTime = 5;
    private HashMap<String, OrbitBhyveProgram> programs = new HashMap<>();
    private String deviceId = "";

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        OrbitBhyveBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            if (CHANNEL_CONTROL.equals(channelUID.getId()) && command instanceof OnOffType) {
                String mode = OnOffType.ON.equals(command) ? "auto" : "off";
                handler.changeRunMode(deviceId, mode);
                return;
            }
            if (CHANNEL_SMART_WATERING.equals(channelUID.getId()) && command instanceof OnOffType) {
                boolean enable = OnOffType.ON.equals(command);
                handler.setSmartWatering(deviceId, enable);
                return;
            }
            if (!channelUID.getId().startsWith("enable_program") && OnOffType.OFF.equals(command)) {
                handler.stopWatering(deviceId);
                return;
            }
            if (CHANNEL_WATERING_TIME.equals(channelUID.getId()) && command instanceof QuantityType quantityCommand) {
                final QuantityType<?> value = quantityCommand.toUnit(Units.MINUTE);
                if (value != null) {
                    wateringTime = value.intValue();
                }
                return;
            }
            if (channelUID.getId().startsWith("zone")) {
                if (OnOffType.ON.equals(command)) {
                    handler.runZone(deviceId, channelUID.getId().replace("zone_", ""), wateringTime);
                }
                return;
            }
            if (channelUID.getId().startsWith("program")) {
                if (OnOffType.ON.equals(command)) {
                    handler.runProgram(deviceId, channelUID.getId().replace("program_", ""));
                }
                return;
            }
            if (channelUID.getId().startsWith("enable_program") && command instanceof OnOffType) {
                String id = channelUID.getId().replace("enable_program_", "");
                OrbitBhyveProgram prog = programs.get(id);
                if (prog != null) {
                    handler.enableProgram(prog, OnOffType.ON.equals(command));
                } else {
                    logger.debug("Cannot get program id: {}", id);
                }
                return;
            }
            if (CHANNEL_RAIN_DELAY.equals(channelUID.getId()) && command instanceof DecimalType) {
                final QuantityType<?> value = ((QuantityType<?>) command).toUnit(Units.HOUR);
                if (value != null) {
                    handler.setRainDelay(deviceId, value.intValue());
                }

            }
        }
    }

    private String getSprinklerId() {
        return getThing().getConfiguration().get("id") != null ? getThing().getConfiguration().get("id").toString()
                : "";
    }

    private @Nullable OrbitBhyveBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (OrbitBhyveBridgeHandler) bridge.getHandler();
        }
        return null;
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            logger.debug("Initializing, bridge is {}", bridge.getStatus());
            if (ThingStatus.ONLINE == bridge.getStatus()) {
                doInit();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    private synchronized void doInit() {
        OrbitBhyveBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            deviceId = getSprinklerId();
            if ("".equals(deviceId)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Sprinkler id is missing!");
            } else {
                OrbitBhyveDevice device = handler.getDevice(deviceId);
                if (device != null) {
                    setDeviceOnline(device.isConnected());
                    createChannels(device.getZones());
                    updateDeviceStatus(device.getStatus());
                }
                List<OrbitBhyveProgram> programs = handler.getPrograms();
                for (OrbitBhyveProgram program : programs) {
                    if (deviceId.equals(program.getDeviceId())) {
                        cacheProgram(program);
                        createProgram(program);
                    }
                }

                updateState(CHANNEL_WATERING_TIME, new QuantityType<>(wateringTime, Units.MINUTE));
                logger.debug("Finished initializing of sprinkler!");
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            doInit();
        }
    }

    private synchronized void cacheProgram(OrbitBhyveProgram program) {
        if (!programs.containsKey(program.getProgram())) {
            programs.put(program.getProgram(), program);
        }
    }

    public void updateDeviceStatus(OrbitBhyveDeviceStatus status) {
        if (!status.getMode().isEmpty()) {
            updateState(CHANNEL_MODE, new StringType(status.getMode()));
            updateState(CHANNEL_CONTROL, OnOffType.from(!"off".equals(status.getMode())));
        }
        if (!status.getNextStartTime().isEmpty()) {
            DateTimeType dt = new DateTimeType(status.getNextStartTime());
            updateState(CHANNEL_NEXT_START, dt);
            logger.debug("Next start time: {}", status.getNextStartTime());
        }
        updateState(CHANNEL_RAIN_DELAY, new DecimalType(status.getDelay()));
    }

    private void createProgram(OrbitBhyveProgram program) {
        String channelName = "program_" + program.getProgram();
        if (thing.getChannel(channelName) == null) {
            logger.debug("Creating channel for program: {} with name: {}", program.getProgram(), program.getName());
            createProgramChannel(channelName, "Switch", "Program " + program.getName());
        }
        String enableChannelName = "enable_" + channelName;
        if (thing.getChannel(enableChannelName) == null) {
            logger.debug("Creating enable channel for program: {} with name: {}", program.getProgram(),
                    program.getName());
            createProgramChannel(enableChannelName, "Switch", "Enable program " + program.getName());
        }
        Channel ch = thing.getChannel(enableChannelName);
        if (ch != null) {
            updateState(ch.getUID(), OnOffType.from(program.isEnabled()));
        }
    }

    private void createProgramChannel(String name, String type, String label) {
        ChannelTypeUID program = new ChannelTypeUID(BINDING_ID, "program");
        createChannel(name, type, label, program);
    }

    private void createChannels(List<OrbitBhyveZone> zones) {
        for (OrbitBhyveZone zone : zones) {
            String channelName = "zone_" + zone.getStation();
            if (thing.getChannel(channelName) == null) {
                logger.debug("Creating channel for zone: {} with name: {}", zone.getStation(), zone.getName());
                createZoneChannel(channelName, "Switch", "Zone " + zone.getName());
            }
        }
    }

    private void createZoneChannel(String name, String type, String label) {
        ChannelTypeUID zone = new ChannelTypeUID(BINDING_ID, "zone");
        createChannel(name, type, label, zone);
    }

    private void createChannel(String name, String type, String label, ChannelTypeUID typeUID) {
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), type).withLabel(label)
                .withType(typeUID).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    public void setDeviceOnline(boolean connected) {
        if (!connected) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Not connected to Orbit BHyve Cloud");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void updateProgram(OrbitBhyveProgram program) {
        String enableChannelName = "enable_program_" + program.getProgram();
        Channel ch = thing.getChannel(enableChannelName);
        if (ch != null) {
            updateState(ch.getUID(), OnOffType.from(program.isEnabled()));
        }
    }

    public void updateSmartWatering(String senseMode) {
        updateState(CHANNEL_SMART_WATERING, OnOffType.from("auto".equals(senseMode)));
    }
}
