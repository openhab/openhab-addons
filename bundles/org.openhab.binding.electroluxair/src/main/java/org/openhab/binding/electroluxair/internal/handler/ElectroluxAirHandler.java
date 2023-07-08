/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.electroluxair.internal.handler;

import static org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants;
import org.openhab.binding.electroluxair.internal.ElectroluxAirConfiguration;
import org.openhab.binding.electroluxair.internal.api.ElectroluxDeltaAPI;
import org.openhab.binding.electroluxair.internal.dto.ElectroluxPureA9DTO;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElectroluxAirHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class ElectroluxAirHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxAirHandler.class);

    private ElectroluxAirConfiguration config = new ElectroluxAirConfiguration();

    public ElectroluxAirHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {}", command);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler bridgeHandler = bridge.getHandler();
                if (bridgeHandler != null) {
                    bridgeHandler.handleCommand(channelUID, command);
                }
            }
        } else {
            ElectroluxPureA9DTO dto = getElectroluxPureA9DTO();
            ElectroluxDeltaAPI api = getElectroluxDeltaAPI();
            if (api != null && dto != null) {
                if (CHANNEL_WORK_MODE.equals(channelUID.getId())) {
                    if (command.toString().equals(COMMAND_WORKMODE_POWEROFF)) {
                        api.workModePowerOff(dto.getApplianceId());
                    } else if (command.toString().equals(COMMAND_WORKMODE_AUTO)) {
                        api.workModeAuto(dto.getApplianceId());
                    } else if (command.toString().equals(COMMAND_WORKMODE_MANUAL)) {
                        api.workModeManual(dto.getApplianceId());
                    }
                } else if (CHANNEL_FAN_SPEED.equals(channelUID.getId())) {
                    api.setFanSpeedLevel(dto.getApplianceId(), Integer.parseInt(command.toString()));
                } else if (CHANNEL_IONIZER.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setIonizer(dto.getApplianceId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setIonizer(dto.getApplianceId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                } else if (CHANNEL_UI_LIGHT.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setUILight(dto.getApplianceId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setUILight(dto.getApplianceId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                } else if (CHANNEL_SAFETY_LOCK.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setSafetyLock(dto.getApplianceId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setSafetyLock(dto.getApplianceId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
                    }
                }

                Bridge bridge = getBridge();
                if (bridge != null) {
                    BridgeHandler bridgeHandler = bridge.getHandler();
                    if (bridgeHandler != null) {
                        bridgeHandler.handleCommand(
                                new ChannelUID(this.thing.getUID(), ElectroluxAirBindingConstants.CHANNEL_STATUS),
                                RefreshType.REFRESH);
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ElectroluxAirConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            update();
            Map<String, String> properties = refreshProperties();
            updateProperties(properties);
        });
    }

    public void update() {
        ElectroluxPureA9DTO dto = getElectroluxPureA9DTO();
        if (dto != null) {
            update(dto);
        } else {
            logger.warn("ElectroluxPureA9DTO is null!");
        }
    }

    private @Nullable ElectroluxDeltaAPI getElectroluxDeltaAPI() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ElectroluxAirBridgeHandler handler = (ElectroluxAirBridgeHandler) bridge.getHandler();
            if (handler != null) {
                return handler.getElectroluxDeltaAPI();
            }
        }
        return null;
    }

    private @Nullable ElectroluxPureA9DTO getElectroluxPureA9DTO() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ElectroluxAirBridgeHandler bridgeHandler = (ElectroluxAirBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                return bridgeHandler.getElectroluxAirThings().get(config.getDeviceId());
            }
        }
        return null;
    }

    private void update(@Nullable ElectroluxPureA9DTO dto) {
        if (dto != null) {
            // Update all channels from the updated data
            getThing().getChannels().stream().map(Channel::getUID).filter(channelUID -> isLinked(channelUID))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), dto);
                        logger.trace("Channel: {}, State: {}", channelUID, state);
                        updateState(channelUID, state);
                    });
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private State getValue(String channelId, ElectroluxPureA9DTO dto) {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new QuantityType<Temperature>(dto.getProperties().getReported().getTemp(), SIUnits.CELSIUS);
            case CHANNEL_HUMIDITY:
                return new QuantityType<Dimensionless>(dto.getProperties().getReported().getHumidity(), Units.PERCENT);
            case CHANNEL_TVOC:
                return new QuantityType<Density>(dto.getProperties().getReported().getTVOC(),
                        Units.MICROGRAM_PER_CUBICMETRE);
            case CHANNEL_PM1:
                return new QuantityType<Dimensionless>(dto.getProperties().getReported().getPM1(),
                        Units.PARTS_PER_BILLION);
            case CHANNEL_PM25:
                return new QuantityType<Dimensionless>(dto.getProperties().getReported().getPM25(),
                        Units.PARTS_PER_BILLION);
            case CHANNEL_PM10:
                return new QuantityType<Dimensionless>(dto.getProperties().getReported().getPM10(),
                        Units.PARTS_PER_BILLION);
            case CHANNEL_CO2:
                return new QuantityType<Dimensionless>(dto.getProperties().getReported().getCO2(),
                        Units.PARTS_PER_MILLION);
            case CHANNEL_FAN_SPEED:
                return new StringType(Integer.toString(dto.getProperties().getReported().getFanspeed()));
            case CHANNEL_FILTER_LIFE:
                return new QuantityType<Dimensionless>(dto.getProperties().getReported().getFilterLife(),
                        Units.PERCENT);
            case CHANNEL_IONIZER:
                return OnOffType.from(dto.getProperties().getReported().isIonizer());
            case CHANNEL_UI_LIGHT:
                return OnOffType.from(dto.getProperties().getReported().isUILight());
            case CHANNEL_SAFETY_LOCK:
                return OnOffType.from(dto.getProperties().getReported().isSafetyLock());
            case CHANNEL_WORK_MODE:
                return new StringType(dto.getProperties().getReported().getWorkmode());
            case CHANNEL_DOOR_OPEN:
                return dto.getProperties().getReported().isDoorOpen() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }
        return UnDefType.UNDEF;
    }

    private Map<String, String> refreshProperties() {
        Map<String, String> properties = new HashMap<>();
        Bridge bridge = getBridge();
        if (bridge != null) {
            ElectroluxAirBridgeHandler bridgeHandler = (ElectroluxAirBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                ElectroluxPureA9DTO dto = bridgeHandler.getElectroluxAirThings().get(config.getDeviceId());
                if (dto != null) {
                    properties.put(Thing.PROPERTY_VENDOR, dto.getApplianceInfo().getBrand());
                    properties.put(PROPERTY_COLOUR, dto.getApplianceInfo().getColour());
                    properties.put(PROPERTY_DEVICE, dto.getApplianceInfo().getDeviceType());
                    properties.put(Thing.PROPERTY_MODEL_ID, dto.getApplianceInfo().getModel());
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, dto.getApplianceInfo().getSerialNumber());
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, dto.getProperties().getReported().getFrmVerNIU());
                }
            }
        }
        return properties;
    }
}
