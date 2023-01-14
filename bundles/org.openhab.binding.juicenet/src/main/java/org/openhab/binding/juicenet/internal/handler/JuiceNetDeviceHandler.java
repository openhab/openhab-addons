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
package org.openhab.binding.juicenet.internal.handler;

import static org.openhab.binding.juicenet.internal.JuiceNetBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.juicenet.internal.api.JuiceNetApi;
import org.openhab.binding.juicenet.internal.api.JuiceNetApiException;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiCar;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiDeviceStatus;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiInfo;
import org.openhab.binding.juicenet.internal.api.dto.JuiceNetApiTouSchedule;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JuiceNetDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetDeviceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(JuiceNetDeviceHandler.class);

    private final TimeZoneProvider timeZoneProvider;

    // properties
    private String name = "";

    private String token = "";
    private long targetTimeTou = 0;
    private long lastInfoTimestamp = 0;

    JuiceNetApiDeviceStatus deviceStatus = new JuiceNetApiDeviceStatus();
    JuiceNetApiInfo deviceInfo = new JuiceNetApiInfo();
    JuiceNetApiTouSchedule deviceTouSchedule = new JuiceNetApiTouSchedule();
    JuiceNetApiCar deviceCar = new JuiceNetApiCar();

    public JuiceNetDeviceHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);

        this.timeZoneProvider = timeZoneProvider;
    }

    public void setNameAndToken(String name, String token) {
        logger.trace("setNameAndToken");
        this.token = token;

        if (!name.equals(this.name)) {
            updateProperty(PROPERTY_NAME, name);
            this.name = name;
        }

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            goOnline();
        }
    }

    @Override
    public void initialize() {
        logger.trace("Device initialized: {}", Objects.requireNonNull(getThing().getUID()));
        Configuration configuration = getThing().getConfiguration();

        String stringId = configuration.get(PARAMETER_UNIT_ID).toString();
        if (stringId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.id-missing");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        // This device will go ONLINE on the first successful API call in queryDeviceStatusAndInfo
    }

    private void handleApiException(Exception e) {
        if (e instanceof JuiceNetApiException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
        } else if (e instanceof InterruptedException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.toString());
            Thread.currentThread().interrupt();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.NONE, e.toString());
        }
    }

    private void goOnline() {
        logger.trace("goOnline");
        if (this.getThing().getStatus() == ThingStatus.ONLINE) {
            return;
        }

        if (token.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.non-existent-device");
            return;
        }

        try {
            tryQueryDeviceStatusAndInfo();
        } catch (JuiceNetApiException | InterruptedException e) {
            handleApiException(e);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Nullable
    private JuiceNetApi getApi() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.bridge-missing");
            return null;
        }
        BridgeHandler handler = Objects.requireNonNull(bridge.getHandler());

        return ((JuiceNetBridgeHandler) handler).getApi();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        JuiceNetApi api = getApi();
        if (api == null) {
            return;
        }

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_NAME:
                case CHANNEL_STATE:
                case CHANNEL_MESSAGE:
                case CHANNEL_OVERRIDE:
                case CHANNEL_CHARGING_TIME_LEFT:
                case CHANNEL_PLUG_UNPLUG_TIME:
                case CHANNEL_TARGET_TIME:
                case CHANNEL_UNIT_TIME:
                case CHANNEL_TEMPERATURE:
                case CHANNEL_CURRENT_LIMIT:
                case CHANNEL_CURRENT:
                case CHANNEL_VOLTAGE:
                case CHANNEL_ENERGY:
                case CHANNEL_SAVINGS:
                case CHANNEL_POWER:
                case CHANNEL_CHARGING_TIME:
                case CHANNEL_ENERGY_AT_PLUGIN:
                case CHANNEL_ENERGY_TO_ADD:
                case CHANNEL_LIFETIME_ENERGY:
                case CHANNEL_LIFETIME_SAVINGS:
                case CHANNEL_CAR_DESCRIPTION:
                case CHANNEL_CAR_BATTERY_SIZE:
                case CHANNEL_CAR_BATTERY_RANGE:
                case CHANNEL_CAR_CHARGING_RATE:
                    refreshStatusChannels();
                    break;
                case CHANNEL_GAS_COST:
                case CHANNEL_FUEL_CONSUMPTION:
                case CHANNEL_ECOST:
                case CHANNEL_ENERGY_PER_MILE:
                    refreshInfoChannels();
                    break;
            }

            return;
        }

        try {
            switch (channelUID.getId()) {
                case CHANNEL_CURRENT_LIMIT:
                    int limit = ((QuantityType<?>) command).intValue();
                    api.setCurrentLimit(Objects.requireNonNull(token), limit);
                    break;
                case CHANNEL_TARGET_TIME: {
                    int energyAtPlugin = 0;
                    int energyToAdd = deviceCar.batterySizeWH;

                    if (!(command instanceof DateTimeType)) {
                        logger.info("Target Time is not an instance of DateTimeType");
                        return;
                    }

                    ZonedDateTime datetime = ((DateTimeType) command).getZonedDateTime();
                    Long targetTime = datetime.toEpochSecond() + datetime.get(ChronoField.OFFSET_SECONDS);
                    logger.debug("DateTime: {} - {}", datetime.toString(), targetTime);

                    api.setOverride(Objects.requireNonNull(token), energyAtPlugin, targetTime, energyToAdd);

                    break;
                }
                case CHANNEL_CHARGING_STATE: {
                    String state = ((StringType) command).toString();
                    Long overrideTime = deviceStatus.unitTime;
                    int energyAtPlugin = 0;
                    int energyToAdd = deviceCar.batterySizeWH;

                    switch (state) {
                        case "stop":
                            if (targetTimeTou == 0) {
                                targetTimeTou = deviceStatus.targetTime;
                            }
                            overrideTime = deviceStatus.unitTime + 31556926;
                            break;
                        case "start":
                            if (targetTimeTou == 0) {
                                targetTimeTou = deviceStatus.targetTime;
                            }
                            overrideTime = deviceStatus.unitTime;
                            break;
                        case "smart":
                            overrideTime = deviceStatus.defaultTargetTime;
                            break;
                    }

                    api.setOverride(Objects.requireNonNull(token), energyAtPlugin, overrideTime, energyToAdd);

                    break;
                }
            }
        } catch (JuiceNetApiException | InterruptedException e) {
            handleApiException(e);
            return;
        }
    }

    private void tryQueryDeviceStatusAndInfo() throws JuiceNetApiException, InterruptedException {
        String apiToken = Objects.requireNonNull(this.token);
        JuiceNetApi api = getApi();
        if (api == null) {
            return;
        }

        deviceStatus = api.queryDeviceStatus(apiToken);

        if (deviceStatus.infoTimestamp > lastInfoTimestamp) {
            lastInfoTimestamp = deviceStatus.infoTimestamp;

            deviceInfo = api.queryInfo(apiToken);
            deviceTouSchedule = api.queryTOUSchedule(apiToken);
            refreshInfoChannels();
        }

        int carId = deviceStatus.carId;
        for (JuiceNetApiCar car : deviceInfo.cars) {
            if (car.carId == carId) {
                this.deviceCar = car;
                break;
            }
        }

        refreshStatusChannels();
    }

    public void queryDeviceStatusAndInfo() {
        logger.trace("queryStatusAndInfo");
        ThingStatus status = getThing().getStatus();

        if (status != ThingStatus.ONLINE) {
            goOnline();
            return;
        }

        try {
            tryQueryDeviceStatusAndInfo();
        } catch (JuiceNetApiException | InterruptedException e) {
            handleApiException(e);
            return;
        }
    }

    private ZonedDateTime toZonedDateTime(long localEpochSeconds) {
        return Instant.ofEpochSecond(localEpochSeconds).atZone(timeZoneProvider.getTimeZone());
    }

    private void refreshStatusChannels() {
        updateState(CHANNEL_STATE, new StringType(deviceStatus.state));

        if (deviceStatus.targetTime <= deviceStatus.unitTime) {
            updateState(CHANNEL_CHARGING_STATE, new StringType("start"));
        } else if ((deviceStatus.targetTime - deviceStatus.unitTime) < TimeUnit.DAYS.toSeconds(2)) {
            updateState(CHANNEL_CHARGING_STATE, new StringType("smart"));
        } else {
            updateState(CHANNEL_CHARGING_STATE, new StringType("stop"));
        }

        updateState(CHANNEL_MESSAGE, new StringType(deviceStatus.message));
        updateState(CHANNEL_OVERRIDE, OnOffType.from(deviceStatus.showOverride));
        updateState(CHANNEL_CHARGING_TIME_LEFT, new QuantityType<>(deviceStatus.chargingTimeLeft, Units.SECOND));
        updateState(CHANNEL_PLUG_UNPLUG_TIME, new DateTimeType(toZonedDateTime(deviceStatus.plugUnplugTime)));
        updateState(CHANNEL_TARGET_TIME, new DateTimeType(toZonedDateTime(deviceStatus.targetTime)));
        updateState(CHANNEL_UNIT_TIME, new DateTimeType(toZonedDateTime(deviceStatus.utcTime)));
        updateState(CHANNEL_TEMPERATURE, new QuantityType<>(deviceStatus.temperature, SIUnits.CELSIUS));
        updateState(CHANNEL_CURRENT_LIMIT, new QuantityType<>(deviceStatus.charging.ampsLimit, Units.AMPERE));
        updateState(CHANNEL_CURRENT, new QuantityType<>(deviceStatus.charging.ampsCurrent, Units.AMPERE));
        updateState(CHANNEL_VOLTAGE, new QuantityType<>(deviceStatus.charging.voltage, Units.VOLT));
        updateState(CHANNEL_ENERGY, new QuantityType<>(deviceStatus.charging.whEnergy, Units.WATT_HOUR));
        updateState(CHANNEL_SAVINGS, new DecimalType(deviceStatus.charging.savings / 100.0));
        updateState(CHANNEL_POWER, new QuantityType<>(deviceStatus.charging.wattPower, Units.WATT));
        updateState(CHANNEL_CHARGING_TIME, new QuantityType<>(deviceStatus.charging.secondsCharging, Units.SECOND));
        updateState(CHANNEL_ENERGY_AT_PLUGIN,
                new QuantityType<>(deviceStatus.charging.whEnergyAtPlugin, Units.WATT_HOUR));
        updateState(CHANNEL_ENERGY_TO_ADD, new QuantityType<>(deviceStatus.charging.whEnergyToAdd, Units.WATT_HOUR));
        updateState(CHANNEL_LIFETIME_ENERGY, new QuantityType<>(deviceStatus.lifetime.whEnergy, Units.WATT_HOUR));
        updateState(CHANNEL_LIFETIME_SAVINGS, new DecimalType(deviceStatus.lifetime.savings / 100.0));

        // update Car items
        updateState(CHANNEL_CAR_DESCRIPTION, new StringType(deviceCar.description));
        updateState(CHANNEL_CAR_BATTERY_SIZE, new QuantityType<>(deviceCar.batterySizeWH, Units.WATT_HOUR));
        updateState(CHANNEL_CAR_BATTERY_RANGE, new QuantityType<>(deviceCar.batteryRangeM, ImperialUnits.MILE));
        updateState(CHANNEL_CAR_CHARGING_RATE, new QuantityType<>(deviceCar.chargingRateW, Units.WATT));
    }

    private void refreshInfoChannels() {
        updateState(CHANNEL_NAME, new StringType(name));
        updateState(CHANNEL_GAS_COST, new DecimalType(deviceInfo.gasCost / 100.0));
        // currently there is no unit defined for fuel consumption
        updateState(CHANNEL_FUEL_CONSUMPTION, new DecimalType(deviceInfo.mpg));
        updateState(CHANNEL_ECOST, new DecimalType(deviceInfo.ecost / 100.0));
        updateState(CHANNEL_ENERGY_PER_MILE, new DecimalType(deviceInfo.whPerMile));
    }
}
