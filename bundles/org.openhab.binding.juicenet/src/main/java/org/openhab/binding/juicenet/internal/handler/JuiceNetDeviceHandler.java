package org.openhab.binding.juicenet.internal.handler;

import static org.openhab.binding.juicenet.internal.JuiceNetBindingConstants.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.juicenet.internal.api.JuiceNetApi;
import org.openhab.binding.juicenet.internal.api.JuiceNetApiException;
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

    protected String name = "";
    protected String id = "";
    protected String token = "";

    protected long targetTimeTou = 0;
    protected long lastInfoTimestamp = 0;

    JuiceNetApi.JuiceNetApiDeviceStatus deviceStatus = new JuiceNetApi.JuiceNetApiDeviceStatus();
    JuiceNetApi.JuiceNetApiInfo deviceInfo = new JuiceNetApi.JuiceNetApiInfo();
    JuiceNetApi.JuiceNetApiTouSchedule deviceTouSchedule = new JuiceNetApi.JuiceNetApiTouSchedule();

    // This holds the last time the info was updated and is compared everytime we get device status to determine
    // if the deviceInfo should be updated. Set the time in the past to ensure info is updated on initial call.
    protected ZonedDateTime infoTimestamp = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    public JuiceNetDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.trace("JuiceNetDeviceHandler:initialize");
        Map<String, String> properties = editProperties();

        name = Objects.requireNonNull(properties.get(DEVICE_PROP_NAME));
        id = Objects.requireNonNull(properties.get(DEVICE_PROP_UNIT_ID));
        token = Objects.requireNonNull(properties.get(DEVICE_PROP_TOKEN));

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            try {
                queryDeviceStatusAndInfo();
            } catch (JuiceNetApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
                return;
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
                return;
            } catch (InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.toString());
                return;
            }

            updateStatus(ThingStatus.ONLINE);
        });
    }

    protected JuiceNetApi getApi() {
        Bridge bridge = Objects.requireNonNull(getBridge());
        BridgeHandler handler = Objects.requireNonNull(bridge.getHandler());

        return ((JuiceNetBridgeHandler) handler).getApi();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case DEVICE_STATE:
                case DEVICE_OVERRIDE:
                case DEVICE_CHARGING_TIME_LEFT:
                case DEVICE_PLUG_UNPLUG_TIME:
                case DEVICE_TARGET_TIME:
                case DEVICE_UNIT_TIME:
                case DEVICE_TEMPERATURE:
                case DEVICE_AMPS_LIMIT:
                case DEVICE_AMPS_CURRENT:
                case DEVICE_VOLTAGE:
                case DEVICE_ENERGY:
                case DEVICE_SAVINGS:
                case DEVICE_POWER:
                case DEVICE_CHARGING_TIME:
                case DEVICE_PLUGINENERGY:
                case DEVICE_ENERGYTOADD:
                case DEVICE_LIFETIME_ENERGY:
                case DEVICE_LIFETIME_SAVINGS:
                case DEVICE_CAR_DESCRIPTION:
                case DEVICE_CAR_BATTERY_SIZE:
                case DEVICE_CAR_BATTERY_RANGE:
                case DEVICE_CAR_CHARGING_RATE:
                    refreshStatusChannels();
                    break;
                case DEVICE_GASCOST:
                case DEVICE_MPG:
                case DEVICE_ECOST:
                case DEVICE_WHPERMILE:

                    refreshInfoChannels();
                    break;
            }

            return;
        }

        try {

            switch (channelUID.getId()) {
                case DEVICE_OVERRIDE:
                    if (channelUID.getId().equals(DEVICE_OVERRIDE)) {
                        if (!(command instanceof OnOffType)) {
                            return;
                        }

                        if (command.equals(OnOffType.ON)) {
                            getApi().setOverride(token, deviceStatus.charging.wh_energy_at_plugin,
                                    deviceStatus.unit_time, 0);

                        } else {
                            getApi().setOverride(token, 0, null, 0);
                        }

                    }
                    break;
                case DEVICE_AMPS_LIMIT:
                    int limit = ((QuantityType<?>) command).intValue();

                    getApi().setCurrentLimit(token, limit);
                    break;
                case DEVICE_CHARGING_STATE:
                    String state = ((StringType) command).toString();
                    Long override_time = deviceStatus.unit_time;
                    int energy_at_plugin = 0;
                    int energy_to_add = 7600;

                    switch (state) {
                        case "stop":
                            if (targetTimeTou == 0) {
                                targetTimeTou = deviceStatus.target_time;
                            }
                            override_time = deviceStatus.unit_time + 31556926;
                            break;
                        case "start":
                            if (targetTimeTou == 0) {
                                targetTimeTou = deviceStatus.target_time;
                            }
                            override_time = deviceStatus.unit_time;
                            break;
                        case "smart":
                            override_time = targetTimeTou;
                            targetTimeTou = 0;
                            break;
                    }

                    getApi().setOverride(token, energy_at_plugin, override_time, energy_to_add);
                    break;
            }
        } catch (IOException e) {
            logger.debug("Unable to open connection to api host: {}", e.getMessage());
        } catch (InterruptedException e) {
            logger.debug("Unable to open connection to api host: {}", e.getMessage());
        } catch (JuiceNetApiException e) {
            logger.debug("Malformed JuiceNet API error: {}", e.getMessage());
        }
    }

    public void queryDeviceStatusAndInfo() throws JuiceNetApiException, IOException, InterruptedException {
        // deviceStatus = Objects.requireNonNull(getBridgeHandler().getApi().queryDeviceStatus(token));

        deviceStatus = getApi().queryDeviceStatus(token);

        if (deviceStatus.info_timestamp > lastInfoTimestamp) {
            lastInfoTimestamp = deviceStatus.info_timestamp;
            deviceInfo = getApi().queryInfo(token);
            deviceTouSchedule = getApi().queryTOUSchedule(token);
            refreshInfoChannels();
        }

        refreshStatusChannels();
    }

    protected ZonedDateTime toZonedDateTime(long localEpochSeconds) {
        Instant instant = Instant.ofEpochSecond(localEpochSeconds);
        ZonedDateTime zdt = instant.atZone(ZoneId.of("UTC"));

        return zdt.withZoneSameLocal(ZoneId.systemDefault());
    }

    protected void refreshStatusChannels() {
        updateState(DEVICE_STATE, new StringType(deviceStatus.state));

        if (deviceStatus.target_time <= deviceStatus.unit_time) {
            updateState(DEVICE_CHARGING_STATE, new StringType("start"));
        } else if ((deviceStatus.target_time - deviceStatus.unit_time) < TimeUnit.DAYS.toSeconds(2)) {
            updateState(DEVICE_CHARGING_STATE, new StringType("smart"));
        } else {
            updateState(DEVICE_CHARGING_STATE, new StringType("stop"));
        }

        updateState(DEVICE_OVERRIDE, OnOffType.from(deviceStatus.show_override));
        updateState(DEVICE_CHARGING_TIME_LEFT, new QuantityType<Time>(deviceStatus.charging_time_left, Units.SECOND));
        updateState(DEVICE_PLUG_UNPLUG_TIME, new DateTimeType(toZonedDateTime(deviceStatus.plug_unplug_time)));
        updateState(DEVICE_TARGET_TIME, new DateTimeType(toZonedDateTime(deviceStatus.target_time)));
        updateState(DEVICE_UNIT_TIME, new DateTimeType(toZonedDateTime(deviceStatus.unit_time)));
        updateState(DEVICE_TEMPERATURE, new QuantityType<Temperature>(deviceStatus.temperature, SIUnits.CELSIUS));
        updateState(DEVICE_AMPS_LIMIT,
                new QuantityType<ElectricCurrent>(deviceStatus.charging.amps_limit, Units.AMPERE));
        updateState(DEVICE_AMPS_CURRENT,
                new QuantityType<ElectricCurrent>(deviceStatus.charging.amps_current, Units.AMPERE));
        updateState(DEVICE_VOLTAGE, new QuantityType<ElectricPotential>(deviceStatus.charging.voltage, Units.VOLT));
        updateState(DEVICE_ENERGY, new QuantityType<Energy>(deviceStatus.charging.wh_energy, Units.WATT_HOUR));
        updateState(DEVICE_SAVINGS, new DecimalType(deviceStatus.charging.savings / 100.0));
        updateState(DEVICE_POWER, new QuantityType<Power>(deviceStatus.charging.watt_power, Units.WATT));
        updateState(DEVICE_CHARGING_TIME, new QuantityType<Time>(deviceStatus.charging.seconds_charging, Units.SECOND));
        updateState(DEVICE_PLUGINENERGY,
                new QuantityType<Energy>(deviceStatus.charging.wh_energy_at_plugin, Units.WATT_HOUR));
        updateState(DEVICE_ENERGYTOADD,
                new QuantityType<Energy>(deviceStatus.charging.wh_energy_to_add, Units.WATT_HOUR));
        updateState(DEVICE_LIFETIME_ENERGY, new QuantityType<Energy>(deviceStatus.lifetime.wh_energy, Units.WATT_HOUR));
        updateState(DEVICE_LIFETIME_SAVINGS, new DecimalType(deviceStatus.lifetime.savings / 100.0));

        int carId = deviceStatus.car_id;
        for (JuiceNetApi.JuiceNetApiCar car : deviceInfo.cars) {
            if (car.car_id == carId) {
                updateState(DEVICE_CAR_DESCRIPTION, new StringType(car.description));
                updateState(DEVICE_CAR_BATTERY_SIZE, new QuantityType<Energy>(car.battery_size_wh, Units.WATT_HOUR));
                updateState(DEVICE_CAR_BATTERY_RANGE,
                        new QuantityType<Length>(car.battery_range_m, ImperialUnits.MILE));
                updateState(DEVICE_CAR_CHARGING_RATE, new QuantityType<Power>(car.charging_rate_w, Units.WATT));
                break;
            }
        }
    }

    protected void refreshInfoChannels() {
        updateState(DEVICE_GASCOST, new DecimalType(deviceInfo.gascost / 100.0));
        updateState(DEVICE_MPG, new DecimalType(deviceInfo.mpg));
        updateState(DEVICE_ECOST, new DecimalType(deviceInfo.ecost / 100.0));
        updateState(DEVICE_WHPERMILE, new DecimalType(deviceInfo.whpermile));
    }
}
