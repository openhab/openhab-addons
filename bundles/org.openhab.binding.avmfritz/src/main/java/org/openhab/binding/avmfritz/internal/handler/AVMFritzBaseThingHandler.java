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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.BindingConstants.*;
import static org.openhab.binding.avmfritz.internal.dto.HeatingModel.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.internal.config.AVMFritzDeviceConfiguration;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.AlertModel;
import org.openhab.binding.avmfritz.internal.dto.ButtonModel;
import org.openhab.binding.avmfritz.internal.dto.DeviceModel;
import org.openhab.binding.avmfritz.internal.dto.HeatingModel;
import org.openhab.binding.avmfritz.internal.dto.HeatingModel.NextChangeModel;
import org.openhab.binding.avmfritz.internal.dto.PowerMeterModel;
import org.openhab.binding.avmfritz.internal.dto.SwitchModel;
import org.openhab.binding.avmfritz.internal.dto.TemperatureModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaStatusListener;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! thing. Handles commands, which are sent to one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public abstract class AVMFritzBaseThingHandler extends BaseThingHandler implements FritzAhaStatusListener {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzBaseThingHandler.class);

    /**
     * keeps track of the current state for handling of increase/decrease
     */
    private @Nullable AVMFritzBaseModel state;
    private @NonNullByDefault({}) AVMFritzDeviceConfiguration config;

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! device
     */
    public AVMFritzBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(AVMFritzDeviceConfiguration.class);

        String newIdentifier = config.ain;
        if (newIdentifier == null || newIdentifier.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'ain' parameter must be configured.");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void onDeviceAdded(AVMFritzBaseModel device) {
        // nothing to do
    }

    @Override
    public void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device) {
        if (thing.getUID().equals(thingUID)) {
            logger.debug("Update thing '{}' with device model: {}", thingUID, device);
            if (device.getPresent() == 1) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device not present");
            }
            state = device;

            updateProperties(device, editProperties());

            if (device.isPowermeter()) {
                updatePowermeter(device.getPowermeter());
            }
            if (device.isSwitchableOutlet()) {
                updateSwitchableOutlet(device.getSwitch());
            }
            if (device.isHeatingThermostat()) {
                updateHeatingThermostat(device.getHkr());
            }
            if (device instanceof DeviceModel) {
                DeviceModel deviceModel = (DeviceModel) device;
                if (deviceModel.isTempSensor()) {
                    updateTemperatureSensor(deviceModel.getTemperature());
                }
                if (deviceModel.isAlarmSensor()) {
                    updateAlarmSensor(deviceModel.getAlert());
                }
                if (deviceModel.isButton()) {
                    updateButton(deviceModel.getButton());
                }
            }
        }
    }

    private void updateButton(@Nullable ButtonModel buttonModel) {
        if (buttonModel != null) {
            int lastPressedTimestamp = buttonModel.getLastpressedtimestamp();
            if (lastPressedTimestamp == 0) {
                updateThingChannelState(CHANNEL_LAST_CHANGE, UnDefType.UNDEF);
            } else {
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime timestamp = ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastPressedTimestamp), zoneId);
                Instant then = timestamp.toInstant();
                ZonedDateTime now = ZonedDateTime.now(zoneId);
                Instant someSecondsEarlier = now.minusSeconds(15).toInstant();
                if (then.isAfter(someSecondsEarlier) && then.isBefore(now.toInstant())) {
                    triggerThingChannel(CHANNEL_PRESS, CommonTriggerEvents.PRESSED);
                }
                updateThingChannelState(CHANNEL_LAST_CHANGE, new DateTimeType(timestamp));
            }
        }
    }

    private void updateAlarmSensor(@Nullable AlertModel alertModel) {
        if (alertModel != null) {
            updateThingChannelState(CHANNEL_CONTACT_STATE,
                    AlertModel.ON.equals(alertModel.getState()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }
    }

    private void updateTemperatureSensor(@Nullable TemperatureModel temperatureModel) {
        if (temperatureModel != null) {
            updateThingChannelState(CHANNEL_TEMPERATURE,
                    new QuantityType<>(temperatureModel.getCelsius(), SIUnits.CELSIUS));
            updateThingChannelConfiguration(CHANNEL_TEMPERATURE, CONFIG_CHANNEL_TEMP_OFFSET,
                    temperatureModel.getOffset());
        }
    }

    private void updateHeatingThermostat(@Nullable HeatingModel heatingModel) {
        if (heatingModel != null) {
            updateThingChannelState(CHANNEL_MODE, new StringType(heatingModel.getMode()));
            updateThingChannelState(CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(heatingModel.getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(heatingModel.getDevicelock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_ACTUALTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getTist()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_SETTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getTsoll()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_ECOTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getAbsenk()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_COMFORTTEMP,
                    new QuantityType<>(toCelsius(heatingModel.getKomfort()), SIUnits.CELSIUS));
            updateThingChannelState(CHANNEL_RADIATOR_MODE, new StringType(heatingModel.getRadiatorMode()));
            NextChangeModel nextChange = heatingModel.getNextchange();
            if (nextChange != null) {
                int endPeriod = nextChange.getEndperiod();
                updateThingChannelState(CHANNEL_NEXT_CHANGE, endPeriod == 0 ? UnDefType.UNDEF
                        : new DateTimeType(
                                ZonedDateTime.ofInstant(Instant.ofEpochSecond(endPeriod), ZoneId.systemDefault())));
                BigDecimal nextTemperature = nextChange.getTchange();
                updateThingChannelState(CHANNEL_NEXTTEMP, TEMP_FRITZ_UNDEFINED.equals(nextTemperature) ? UnDefType.UNDEF
                        : new QuantityType<>(toCelsius(nextTemperature), SIUnits.CELSIUS));
            }
            BigDecimal batteryLevel = heatingModel.getBattery();
            updateThingChannelState(CHANNEL_BATTERY,
                    batteryLevel == null ? UnDefType.UNDEF : new DecimalType(batteryLevel));
            BigDecimal lowBattery = heatingModel.getBatterylow();
            if (lowBattery == null) {
                updateThingChannelState(CHANNEL_BATTERY_LOW, UnDefType.UNDEF);
            } else {
                updateThingChannelState(CHANNEL_BATTERY_LOW,
                        BATTERY_ON.equals(lowBattery) ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    private void updateSwitchableOutlet(@Nullable SwitchModel switchModel) {
        if (switchModel != null) {
            updateThingChannelState(CHANNEL_MODE, new StringType(switchModel.getMode()));
            updateThingChannelState(CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(switchModel.getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(switchModel.getDevicelock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            BigDecimal state = switchModel.getState();
            if (state == null) {
                updateThingChannelState(CHANNEL_OUTLET, UnDefType.UNDEF);
            } else {
                updateThingChannelState(CHANNEL_OUTLET, SwitchModel.ON.equals(state) ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    private void updatePowermeter(@Nullable PowerMeterModel powerMeterModel) {
        if (powerMeterModel != null) {
            updateThingChannelState(CHANNEL_ENERGY,
                    new QuantityType<>(powerMeterModel.getEnergy(), SmartHomeUnits.WATT_HOUR));
            updateThingChannelState(CHANNEL_POWER, new QuantityType<>(powerMeterModel.getPower(), SmartHomeUnits.WATT));
            updateThingChannelState(CHANNEL_VOLTAGE,
                    new QuantityType<>(powerMeterModel.getVoltage(), SmartHomeUnits.VOLT));
        }
    }

    /**
     * Updates thing properties.
     *
     * @param device the {@link AVMFritzBaseModel}
     * @param editProperties map of existing properties
     */
    protected void updateProperties(AVMFritzBaseModel device, Map<String, String> editProperties) {
        editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
        updateProperties(editProperties);
    }

    /**
     * Updates thing channels and creates dynamic channels if missing.
     *
     * @param channelId ID of the channel to be updated.
     * @param state State to be set.
     */
    private void updateThingChannelState(String channelId, State state) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state);
        } else {
            logger.debug("Channel '{}' in thing '{}' does not exist, recreating thing.", channelId, thing.getUID());
            createChannel(channelId);
        }
    }

    /**
     * Creates new channels for the thing.
     *
     * @param channelId ID of the channel to be created.
     */
    private void createChannel(String channelId) {
        ThingHandlerCallback callback = getCallback();
        if (callback != null) {
            ChannelUID channelUID = new ChannelUID(thing.getUID(), channelId);
            ChannelTypeUID channelTypeUID = CHANNEL_BATTERY.equals(channelId)
                    ? DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_BATTERY_LEVEL.getUID()
                    : new ChannelTypeUID(BINDING_ID, channelId);
            Channel channel = callback.createChannelBuilder(channelUID, channelTypeUID).build();
            updateThing(editThing().withoutChannel(channelUID).withChannel(channel).build());
        }
    }

    /**
     * Triggers thing channels.
     *
     * @param channelId ID of the channel to be triggered.
     * @param event Event to emit
     */
    private void triggerThingChannel(String channelId, String event) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            triggerChannel(channel.getUID(), event);
        } else {
            logger.debug("Channel '{}' in thing '{}' does not exist.", channelId, thing.getUID());
        }
    }

    /**
     * Updates thing channel configurations.
     *
     * @param channelId ID of the channel which configuration to be updated.
     * @param configId ID of the configuration to be updated.
     * @param value Value to be set.
     */
    private void updateThingChannelConfiguration(String channelId, String configId, Object value) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Configuration editConfig = channel.getConfiguration();
            editConfig.put(configId, value);
        }
    }

    @Override
    public void onDeviceGone(ThingUID thingUID) {
        if (thing.getUID().equals(thingUID)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Device not present in response");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            handleRefreshCommand();
            return;
        }
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
            return;
        }
        String ain = getIdentifier();
        if (ain == null) {
            logger.debug("Cannot handle command '{}' because AIN is missing", command);
            return;
        }
        switch (channelId) {
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_DEVICE_LOCKED:
            case CHANNEL_TEMPERATURE:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_VOLTAGE:
            case CHANNEL_ACTUALTEMP:
            case CHANNEL_ECOTEMP:
            case CHANNEL_COMFORTTEMP:
            case CHANNEL_NEXT_CHANGE:
            case CHANNEL_NEXTTEMP:
            case CHANNEL_BATTERY:
            case CHANNEL_BATTERY_LOW:
            case CHANNEL_CONTACT_STATE:
            case CHANNEL_LAST_CHANGE:
                logger.debug("Channel {} is a read-only channel and cannot handle command '{}'", channelId, command);
                break;
            case CHANNEL_OUTLET:
                if (command instanceof OnOffType) {
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                    if (state != null) {
                        state.getSwitch().setState(OnOffType.ON.equals(command) ? SwitchModel.ON : SwitchModel.OFF);
                    }
                }
                break;
            case CHANNEL_SETTEMP:
                BigDecimal temperature = null;
                if (command instanceof DecimalType) {
                    temperature = normalizeCelsius(((DecimalType) command).toBigDecimal());
                } else if (command instanceof QuantityType) {
                    temperature = normalizeCelsius(
                            ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS).toBigDecimal());
                } else if (command instanceof IncreaseDecreaseType) {
                    temperature = state.getHkr().getTsoll();
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        temperature.add(BigDecimal.ONE);
                    } else {
                        temperature.subtract(BigDecimal.ONE);
                    }
                } else if (command instanceof OnOffType) {
                    temperature = OnOffType.ON.equals(command) ? TEMP_FRITZ_ON : TEMP_FRITZ_OFF;
                }
                if (temperature != null) {
                    fritzBox.setSetTemp(ain, fromCelsius(temperature));
                    HeatingModel heatingModel = state.getHkr();
                    heatingModel.setTsoll(temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(heatingModel.getRadiatorMode()));
                }
                break;
            case CHANNEL_RADIATOR_MODE:
                BigDecimal targetTemperature = null;
                if (command instanceof StringType) {
                    switch (command.toString()) {
                        case MODE_ON:
                            targetTemperature = TEMP_FRITZ_ON;
                            break;
                        case MODE_OFF:
                            targetTemperature = TEMP_FRITZ_OFF;
                            break;
                        case MODE_COMFORT:
                            targetTemperature = state.getHkr().getKomfort();
                            break;
                        case MODE_ECO:
                            targetTemperature = state.getHkr().getAbsenk();
                            break;
                        case MODE_BOOST:
                            targetTemperature = TEMP_FRITZ_MAX;
                            break;
                        case MODE_UNKNOWN:
                        case MODE_WINDOW_OPEN:
                            logger.debug("Command '{}' is a read-only command for channel {}.", command, channelId);
                            break;
                    }
                    if (targetTemperature != null) {
                        fritzBox.setSetTemp(ain, targetTemperature);
                        state.getHkr().setTsoll(targetTemperature);
                        updateState(CHANNEL_SETTEMP, new QuantityType<>(toCelsius(targetTemperature), SIUnits.CELSIUS));
                    }
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }

    }

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    private @Nullable FritzAhaWebInterface getWebInterface() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof AVMFritzBaseBridgeHandler) {
                return ((AVMFritzBaseBridgeHandler) handler).getWebInterface();
            }
        }
        return null;
    }

    /**
     * Handles a refresh command.
     */
    private void handleRefreshCommand() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof AVMFritzBaseBridgeHandler) {
                ((AVMFritzBaseBridgeHandler) handler).handleRefreshCommand();
            }
        }
    }

    /**
     * Returns the AIN.
     *
     * @return the AIN
     */
    public @Nullable String getIdentifier() {
        return config.ain;
    }
}
