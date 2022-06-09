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
package org.openhab.binding.boschspexor.internal;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;
import static org.openhab.binding.boschspexor.internal.api.model.SensorValue.*;

import java.math.BigDecimal;
import java.time.Duration;
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
import org.openhab.core.cache.ExpiringCache;
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
    private static final long DEFAULT_CACHE_TIMEOUT = 30;

    private final Logger logger = LoggerFactory.getLogger(BoschSpexorThingHandler.class);

    private Optional<ScheduledFuture<?>> pollEvent;
    private ExpiringCache<SpexorInfo> cache;

    public BoschSpexorThingHandler(Thing thing) {
        super(thing);
        this.pollEvent = Optional.empty();
        cache = new ExpiringCache<SpexorInfo>(Duration.ofSeconds(DEFAULT_CACHE_TIMEOUT), this::getSpexorInfo);
        if (logger.isDebugEnabled()) {
            logger.debug("Bosch spexor handler was created");
        }
    }

    private Optional<String> getSpexorID() {
        return Optional.ofNullable((String) thing.getConfiguration().get(PROPERTY_SPEXOR_ID));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (logger.isDebugEnabled()) {
            logger.debug("received ChannelUID {} with command {}", channelUID, command);
        }

        if (getSpexorID().isEmpty()) {
            logger.warn("thing is not well created and can't be used");
            return;
        }

        if (CHANNEL_BOSCH_SPEXOR.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                refreshThing();
            }
        } else if (GROUP_ID_OBSERVATIONS.equals(channelUID.getGroupId())) {
            SpexorAPIService apiService = getSpexorAPIService();
            if (command instanceof StringType) {
                if (apiService == null) {
                    logger.warn("spexor API service is not available and comand won't be performed");
                    return;
                }
                SensorMode mode = null;
                try {
                    mode = SensorMode.valueOf(command.toString().toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("no SensorMode could be found for command '{}'", command, e);
                }
                String type = channelUID.getIdWithoutGroup();
                if (SensorMode.ACTIVATED.equals(mode) || SensorMode.DEACTIVATED.equals(mode)) {
                    ObservationChangeStatus newObservationState = apiService.setObservation(getSpexorID().get(), type,
                            SensorMode.ACTIVATED.equals(mode));
                    if (logger.isDebugEnabled()) {
                        logger.debug("setting new observation state for {} to {} was {}", type, mode,
                                newObservationState.getStatusCode());
                    }
                    updateState(channelUID, new StringType(newObservationState.getSensorMode().name()));
                } else {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "setting observation state not allowed. Only 'ACTIVATED' and 'DEACTIVATED' are valid options ");
                    logger.warn(
                            "setting observation state for {} to {} not allowed. Only 'Activated' and 'Deactivated' are valid options ",
                            channelUID, mode);
                }
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        if (logger.isDebugEnabled()) {
            logger.debug("thing {} got update with {} channels", thing.getUID(), thing.getChannels().size());
        }
        super.thingUpdated(thing);
    }

    private void refreshThing() {
        if (logger.isDebugEnabled()) {
            logger.debug("updating {} with new values from backend", getThing().getUID());
        }
        SpexorInfo spexor = cache.getValue();
        SpexorAPIService apiService = getSpexorAPIService();
        if (spexor == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not determine further information.");
        } else if (apiService != null) {
            Map<String, SensorValue<?>> values = apiService.getSensorValues(spexor.getId(), spexor.getSensors());
            updateStates(spexor, values);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateStates(SpexorInfo spexor, Map<String, SensorValue<?>> values) {
        Connection connection = spexor.getStatus().getConnection();
        boolean thingReachable = connection.isOnline();
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

            // OBSERVATION
            for (ObservationStatus observationStatus : spexor.getStatus().getObservation()) {
                String observationType = observationStatus.getObservationType();
                Channel channel = getThing().getChannel(getChannelID(GROUP_ID_OBSERVATIONS, observationType));
                if (channel != null) {
                    updateState(channel.getUID(), new StringType(observationStatus.getSensorMode().name()));
                }
            }

            // SENSORS
            for (String sensor : spexor.getSensors()) {
                String sensorType = sensor;
                Channel channel = getThing().getChannel(getChannelID(GROUP_ID_SENSORS, sensorType));
                if (channel != null) {
                    SensorValue<?> sensorValue = values.get(sensor);
                    if (sensorValue != null) {
                        if (sensorValue.getValue() instanceof Integer) {
                            Integer value = ((SensorValue<Integer>) sensorValue).getValue();
                            updateState(channel.getUID(), new DecimalType(value.doubleValue()));
                        } else if (sensorValue.getValue() instanceof String) {
                            String value = ((SensorValue<String>) sensorValue).getValue();
                            updateState(channel.getUID(), new StringType(value));
                        }
                    }
                }
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "spexor is not reachable");
        }
    }

    private @Nullable SpexorInfo getSpexorInfo() {
        SpexorInfo result = null;
        SpexorAPIService apiService = getSpexorAPIService();
        if (apiService == null) {
            logger.warn("spexor API service is not available and device won't be updated");
        } else if (getSpexorID().isEmpty()) {
            logger.warn("thing is not available and device can't be updated");
        } else {
            SpexorInfo spexor = apiService.getSpexor(getSpexorID().get());
            if (spexor == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not determine further information.");
            }
            result = spexor;
        }
        return result;
    }

    public void discoverChannels() {
        SpexorInfo spexor = cache.getValue();
        if (spexor != null) {
            boolean thingStructureChanged = false;
            ThingBuilder thingBuilder = editThing();

            for (ObservationStatus observationStatus : spexor.getStatus().getObservation()) {
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

            }

            if (thingStructureChanged) {
                updateThing(thingBuilder.build());
                if (logger.isDebugEnabled()) {
                    logger.debug("structural change of thing \"{}\"", getThing().getUID());
                }
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
                channel = ChannelBuilder.create(getChannelID(GROUP_ID_SENSORS, sensorType))
                        .withType(new ChannelTypeUID("boschspexor", "sampleNumberChannel"))
                        .withDescription("Sensor Value \"" + sensorType + "\"").withLabel("Sensor Type " + sensorType)
                        .build();
                break;
            case TYPE_AIR_QUALITY_LEVEL:
            default:
                channel = ChannelBuilder.create(getChannelID(GROUP_ID_SENSORS, sensorType))
                        .withType(new ChannelTypeUID("boschspexor", "sampleStringChannel"))
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

    @SuppressWarnings("null")
    @Nullable
    public SpexorAPIService getSpexorAPIService() {
        if (getBridge().getHandler() instanceof BoschSpexorBridgeHandler) {
            return ((BoschSpexorBridgeHandler) getBridge().getHandler()).getApiService();
        } else {
            return null;
        }
    }

    @Override
    public void initialize() {
        if (logger.isDebugEnabled()) {
            logger.debug("Bosch Spexor ID is {}", getSpexorID());
        }
        int refreshRate = ((BigDecimal) getConfig().get("refreshInterval")).intValue();
        cache = new ExpiringCache<>(Duration.ofSeconds(refreshRate), this::getSpexorInfo);
        if (pollEvent.isPresent()) {
            pollEvent.get().cancel(false);
        }
        pollEvent = Optional.of(scheduler.scheduleWithFixedDelay(this::refreshThing, 0, refreshRate, TimeUnit.SECONDS));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.pollEvent.isPresent()) {
            this.pollEvent.get().cancel(true);
        }
    }
}
