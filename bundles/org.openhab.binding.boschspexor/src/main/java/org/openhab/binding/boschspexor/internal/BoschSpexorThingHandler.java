/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.CHANNEL_BOSCH_SPEXOR;
import static org.openhab.binding.boschspexor.internal.api.model.SensorValue.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschspexor.internal.api.model.Connection;
import org.openhab.binding.boschspexor.internal.api.model.Energy;
import org.openhab.binding.boschspexor.internal.api.model.Firmware;
import org.openhab.binding.boschspexor.internal.api.model.ObservationChangeStatus;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus.SensorMode;
import org.openhab.binding.boschspexor.internal.api.model.Profile;
import org.openhab.binding.boschspexor.internal.api.model.SensorValue;
import org.openhab.binding.boschspexor.internal.api.model.SpexorInfo;
import org.openhab.binding.boschspexor.internal.api.service.SpexorAPIService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BoschSpexorThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class BoschSpexorThingHandler extends BaseThingHandler {

    private static final String GROUP_ID_STATUS = "status";
    private static final String GROUP_ID_OBSERVATIONS = "observations";
    private static final String GROUP_ID_PROFILE = "profile";
    private static final String GROUP_ID_SENSORS = "sensors";

    private static final String CHANNEL_ID_SOC = "soc";
    private static final String CHANNEL_ID_LAST_CONNECTED = "lastConnected";
    private static final String CHANNEL_ID_CONNECTION_TYPE = "connectionType";
    private static final String CHANNEL_ID_POWERED = "powered";
    private static final String CHANNEL_ID_ENERGY_MODE = "energyMode";
    private static final String CHANNEL_ID_AVAILABLE_VERSION = "availableVersion";
    private static final String CHANNEL_ID_INSTALLED_VERSION = "installedVersion";
    private static final String CHANNEL_ID_FIRMWARE_STATE = "firmwareState";
    private static final String CHANNEL_ID_PROFILE_NAME = "profileName";
    private static final String CHANNEL_ID_PROFILE_TYPE = "profileType";

    private final Logger logger = LoggerFactory.getLogger(BoschSpexorThingHandler.class);

    private @NonNullByDefault({}) SpexorAPIService apiService;

    private Optional<ScheduledFuture<?>> pollEvent;

    public BoschSpexorThingHandler(Thing thing, @Nullable SpexorAPIService apiService) {
        super(thing);
        this.apiService = apiService;
        this.pollEvent = Optional.empty();
        logger.debug("Bosch spexor handler was created");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("received ChannelUID {} with command {}", channelUID, command);
        if (CHANNEL_BOSCH_SPEXOR.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                updateStatus();
            }
        } else if (channelUID.getId().contains(GROUP_ID_OBSERVATIONS + "#")) {
            logger.debug("received {} command {}", channelUID, command);
            if (command instanceof StringType) {
                try {
                    SensorMode mode = SensorMode.valueOf(command.toString());
                    String type = channelUID.getId().substring(channelUID.getId().lastIndexOf('#') + 1);
                    if (SensorMode.Activated.equals(mode) || SensorMode.Deactivated.equals(mode)) {
                        ObservationChangeStatus newObservationState = apiService
                                .setObservation(getThing().getUID().getId(), type, SensorMode.Activated.equals(mode));
                        logger.info("setting new observation state for {} to {} was {}", type, mode,
                                newObservationState.getStatusCode());
                        updateState(channelUID, new StringType(newObservationState.getSensorMode().name()));
                    } else {
                        logger.info(
                                "setting observation state for {} to {} not allowed. Only 'Activated' and 'Deactivated' are valid options ",
                                channelUID, mode);
                    }
                } catch (Exception e) {
                    logger.error("no supported command for observation");
                }
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("thing {} got update with {} channels", thing.getUID(), thing.getChannels().size());
        super.thingUpdated(thing);
    }

    @SuppressWarnings("unchecked")
    private void updateStatus() {
        logger.info("updating {} with new values from backend", getThing().getUID());
        SpexorInfo spexor = apiService.getSpexor(getThing().getUID().getId());
        if (spexor == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.UNKNOWN.NONE);
        } else {
            Connection connection = spexor.getStatus().getConnection();
            boolean thingReachable = true;// connection.isOnline();
            if (thingReachable) {
                // CONNECTION STATUS
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.ONLINE.NONE);
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_CONNECTION_TYPE),
                        new StringType(connection.getConnectionType().name()));
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_LAST_CONNECTED),
                        new DateTimeType(connection.getLastConnected()));
                // ENERGY STATUS
                Energy energy = spexor.getStatus().getEnergy();
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_ENERGY_MODE),
                        new StringType(energy.getEnergyMode().name()));
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_POWERED), OnOffType.from(energy.isPowered()));
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_SOC),
                        new PercentType(energy.getStateOfCharge().getValue()));

                // FIRMWARE INFO
                Firmware firmware = spexor.getStatus().getFirmware();
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_AVAILABLE_VERSION),
                        new StringType(firmware.getAvailableVersion()));
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_INSTALLED_VERSION),
                        new StringType(firmware.getCurrentVersion()));
                updateState(getChannelID(GROUP_ID_STATUS, CHANNEL_ID_FIRMWARE_STATE),
                        new StringType(firmware.getState().name()));

                // PROFILE
                Profile profile = spexor.getProfile();
                updateState(getChannelID(GROUP_ID_PROFILE, CHANNEL_ID_PROFILE_NAME), new StringType(profile.getName()));
                updateState(getChannelID(GROUP_ID_PROFILE, CHANNEL_ID_PROFILE_TYPE),
                        new StringType(profile.getProfileType().name()));

                boolean thingStructureChanged = false;
                ThingBuilder thingBuilder = editThing();

                // OBSERVATION
                for (ObservationStatus observationStatus : spexor.getStatus().getObservation()) {
                    @SuppressWarnings("null")
                    String observationType = observationStatus.getObservationType();
                    Channel channel = getThing().getChannel(getChannelID(GROUP_ID_OBSERVATIONS, observationType));
                    if (channel == null) {
                        channel = createObservationChannel(observationType);
                        thingBuilder.withoutChannel(channel.getUID()).withChannel(channel);
                        thingStructureChanged = true;
                    }
                    updateState(channel.getUID(), new StringType(observationStatus.getSensorMode().name()));
                }

                // SENSORS
                Map<String, SensorValue<?>> values = apiService.getSensorValues(spexor.getId(), spexor.getSensors());
                for (String sensor : spexor.getSensors()) {
                    String sensorType = sensor;
                    Channel channel = getThing().getChannel(getChannelID(GROUP_ID_SENSORS, sensorType));
                    if (channel == null) {
                        channel = createSensorChannel(sensor, sensorType);
                        if (channel != null) {
                            thingBuilder.withoutChannel(channel.getUID()).withChannel(channel);
                            thingStructureChanged = true;
                        }
                    }
                    if (channel != null) {
                        SensorValue<?> sensorValue = values.get(sensor);
                        if (sensorValue != null) {
                            if (sensorValue.getValue() instanceof Integer) {
                                updateState(channel.getUID(),
                                        new DecimalType(((SensorValue<Integer>) sensorValue).getValue()));
                            } else if (sensorValue.getValue() instanceof String) {
                                updateState(channel.getUID(),
                                        new StringType(((SensorValue<String>) sensorValue).getValue()));
                            }
                        }
                    }
                }

                if (thingStructureChanged) {
                    updateThing(thingBuilder.build());
                    logger.debug("structural change of thing \"{}\"", getThing().getUID());
                }

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE);
            }

        }
    }

    private Channel createObservationChannel(String observationType) {
        Channel channel = ChannelBuilder.create(getChannelID(GROUP_ID_OBSERVATIONS, observationType), "String")
                .withType(new ChannelTypeUID("boschspexor", "sampleObservationState"))
                .withLabel("Observation Type " + observationType).build();
        return channel;
    }

    private @Nullable Channel createSensorChannel(String sensor, String sensorType) {
        Channel channel = null;
        switch (sensor) {
            case TYPE_AIR_QUALITY:
            case TYPE_TEMPERATURE:
            case TYPE_HUMIDITY:
            case TYPE_MICROPHONE:
            case TYPE_FIRE:
            case TYPE_PRESSURE:
            case TYPE_ACCELERATION:
            case TYPE_LIGHT:
            case TYPE_GAS:
            case TYPE_PASSIVE_INFRARED:
                channel = ChannelBuilder.create(getChannelID(GROUP_ID_SENSORS, sensorType), "Number")
                        .withType(new ChannelTypeUID("boschspexor", "sampleNumberChannel")).withKind(ChannelKind.STATE)
                        .withDescription("Sensor Value \"" + sensorType + "\"").withLabel("Sensor Type " + sensorType)
                        .build();
                break;
            case TYPE_AIR_QUALITY_LEVEL:
            default:
                channel = ChannelBuilder.create(getChannelID(GROUP_ID_SENSORS, sensorType), "String")
                        .withType(new ChannelTypeUID("boschspexor", "sampleStringChannel")).withKind(ChannelKind.STATE)
                        .withDescription("Sensor Value \"" + sensorType + "\"").withLabel("Sensor Type " + sensorType)
                        .build();
                break;
        }
        return channel;
    }

    private ChannelUID getChannelID(String groupID, String channelID) {
        ChannelGroupUID channelGroupUIDStatus = new ChannelGroupUID(getThing().getUID(), groupID);
        return new ChannelUID(channelGroupUIDStatus, channelID);
    }

    @Override
    public void initialize() {
        logger.debug("Bosch Spexor ID is {}", getThing().getUID().getId());
        int refreshRate = ((BigDecimal) getConfig().get("refreshInterval")).intValue();
        if (pollEvent.isPresent()) {
            pollEvent.get().cancel(false);
        }
        pollEvent = Optional.of(scheduler.scheduleWithFixedDelay(this::updateStatus, 0, refreshRate, TimeUnit.SECONDS));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.pollEvent.isPresent()) {
            this.pollEvent.get().cancel(true);
        }
    }
}
