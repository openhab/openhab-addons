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
package org.openhab.binding.electroluxair.internal.handler;

import static org.openhab.binding.electroluxair.internal.ElectroluxAirBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
            update();
        } else {
            ElectroluxPureA9DTO dto = getElectroluxPureA9DTO();
            ElectroluxDeltaAPI api = getElectroluxDeltaAPO();
            if (api != null && dto != null) {
                if (CHANNEL_WORK_MODE.equals(channelUID.getId())) {
                    if (command.toString().equals(COMMAND_WORKMODE_POWEROFF)) {
                        api.workModePowerOff(dto.getPncId());
                    } else if (command.toString().equals(COMMAND_WORKMODE_AUTO)) {
                        api.workModeAuto(dto.getPncId());
                    } else if (command.toString().equals(COMMAND_WORKMODE_MANUAL)) {
                        api.workModeManual(dto.getPncId());
                    }
                } else if (CHANNEL_FAN_SPEED.equals(channelUID.getId())) {
                    api.setFanSpeedLevel(dto.getPncId(), Integer.parseInt(command.toString()));
                } else if (CHANNEL_IONIZER.equals(channelUID.getId())) {
                    if (command == OnOffType.OFF) {
                        api.setIonizer(dto.getPncId(), "false");
                    } else if (command == OnOffType.ON) {
                        api.setIonizer(dto.getPncId(), "true");
                    } else {
                        logger.debug("Unknown command! {}", command);
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

    private @Nullable ElectroluxDeltaAPI getElectroluxDeltaAPO() {
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
                        updateState(channelUID, state);
                    });
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private State getValue(String channelId, ElectroluxPureA9DTO dto) {
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                return new QuantityType<Temperature>(dto.getTwin().getProperties().getReported().getTemp(),
                        SIUnits.CELSIUS);
            case CHANNEL_HUMIDITY:
                return new QuantityType<Dimensionless>(dto.getTwin().getProperties().getReported().getHumidity(),
                        Units.PERCENT);
            case CHANNEL_TVOC:
                return new QuantityType<Density>(dto.getTwin().getProperties().getReported().gettVOC(),
                        Units.MICROGRAM_PER_CUBICMETRE);
            case CHANNEL_PM1:
                return new QuantityType<Dimensionless>(dto.getTwin().getProperties().getReported().getpM1(),
                        Units.PARTS_PER_BILLION);
            case CHANNEL_PM25:
                return new QuantityType<Dimensionless>(dto.getTwin().getProperties().getReported().getpM25(),
                        Units.PARTS_PER_BILLION);
            case CHANNEL_PM10:
                return new QuantityType<Dimensionless>(dto.getTwin().getProperties().getReported().getpM10(),
                        Units.PARTS_PER_BILLION);
            case CHANNEL_CO2:
                return new QuantityType<Dimensionless>(dto.getTwin().getProperties().getReported().getcO2(),
                        Units.PARTS_PER_MILLION);
            case CHANNEL_FAN_SPEED:
                return new StringType(Integer.toString(dto.getTwin().getProperties().getReported().getFanspeed()));
            case CHANNEL_FILTER_LIFE:
                return new QuantityType<Dimensionless>(dto.getTwin().getProperties().getReported().getFilterLife(),
                        Units.PERCENT);
            case CHANNEL_IONIZER:
                return OnOffType.from(dto.getTwin().getProperties().getReported().ionizer);
            case CHANNEL_WORK_MODE:
                return new StringType(dto.getTwin().getProperties().getReported().workmode);
            case CHANNEL_DOOR_OPEN:
                return dto.getTwin().getProperties().getReported().doorOpen ? OpenClosedType.OPEN
                        : OpenClosedType.CLOSED;
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
                    properties.put(Thing.PROPERTY_VENDOR, dto.getApplicancesInfo().brand);
                    properties.put(PROPERTY_COLOUR, dto.getApplicancesInfo().colour);
                    properties.put(PROPERTY_DEVICE, dto.getApplicancesInfo().device);
                    properties.put(Thing.PROPERTY_MODEL_ID, dto.getApplicancesInfo().model);
                    properties.put(Thing.PROPERTY_SERIAL_NUMBER, dto.getApplicancesInfo().serialNumber);
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                            dto.getTwin().getProperties().getReported().frmVerNIU);
                }
            }
        }
        return properties;
    }
}
