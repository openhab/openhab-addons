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
import static org.openhab.binding.avmfritz.internal.ahamodel.HeatingModel.*;

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
import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.AlertModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.GroupModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.config.AVMFritzDeviceConfiguration;
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
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler handler = bridge.getHandler();
                if (handler instanceof AVMFritzBaseBridgeHandler) {
                    ((AVMFritzBaseBridgeHandler) handler).registerStatusListener(this);
                }
            }

            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof AVMFritzBaseBridgeHandler) {
                ((AVMFritzBaseBridgeHandler) handler).unregisterStatusListener(this);
            }
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

            updateProperties(device);

            if (device instanceof DeviceModel && device.isTempSensor()
                    && ((DeviceModel) device).getTemperature() != null) {
                updateThingChannelState(CHANNEL_TEMPERATURE,
                        new QuantityType<>(((DeviceModel) device).getTemperature().getCelsius(), SIUnits.CELSIUS));
                updateThingChannelConfiguration(CHANNEL_TEMPERATURE, CONFIG_CHANNEL_TEMP_OFFSET,
                        ((DeviceModel) device).getTemperature().getOffset());
            }
            if (device.isPowermeter() && device.getPowermeter() != null) {
                updateThingChannelState(CHANNEL_ENERGY,
                        new QuantityType<>(device.getPowermeter().getEnergy(), SmartHomeUnits.WATT_HOUR));
                updateThingChannelState(CHANNEL_POWER,
                        new QuantityType<>(device.getPowermeter().getPower(), SmartHomeUnits.WATT));
                updateThingChannelState(CHANNEL_VOLTAGE,
                        new QuantityType<>(device.getPowermeter().getVoltage(), SmartHomeUnits.VOLT));
            }
            if (device.isSwitchableOutlet() && device.getSwitch() != null) {
                updateThingChannelState(CHANNEL_MODE, new StringType(device.getSwitch().getMode()));
                updateThingChannelState(CHANNEL_LOCKED,
                        BigDecimal.ZERO.equals(device.getSwitch().getLock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                        BigDecimal.ZERO.equals(device.getSwitch().getDevicelock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                if (device.getSwitch().getState() == null) {
                    updateThingChannelState(CHANNEL_OUTLET, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(CHANNEL_OUTLET,
                            SwitchModel.ON.equals(device.getSwitch().getState()) ? OnOffType.ON : OnOffType.OFF);
                }
            }
            if (device.isHeatingThermostat() && device.getHkr() != null) {
                updateThingChannelState(CHANNEL_MODE, new StringType(device.getHkr().getMode()));
                updateThingChannelState(CHANNEL_LOCKED,
                        BigDecimal.ZERO.equals(device.getHkr().getLock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                updateThingChannelState(CHANNEL_DEVICE_LOCKED,
                        BigDecimal.ZERO.equals(device.getHkr().getDevicelock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                updateThingChannelState(CHANNEL_ACTUALTEMP,
                        new QuantityType<>(toCelsius(device.getHkr().getTist()), SIUnits.CELSIUS));
                updateThingChannelState(CHANNEL_SETTEMP,
                        new QuantityType<>(toCelsius(device.getHkr().getTsoll()), SIUnits.CELSIUS));
                updateThingChannelState(CHANNEL_ECOTEMP,
                        new QuantityType<>(toCelsius(device.getHkr().getAbsenk()), SIUnits.CELSIUS));
                updateThingChannelState(CHANNEL_COMFORTTEMP,
                        new QuantityType<>(toCelsius(device.getHkr().getKomfort()), SIUnits.CELSIUS));
                updateThingChannelState(CHANNEL_RADIATOR_MODE, new StringType(device.getHkr().getRadiatorMode()));
                if (device.getHkr().getNextchange() != null) {
                    if (device.getHkr().getNextchange().getEndperiod() == 0) {
                        updateThingChannelState(CHANNEL_NEXT_CHANGE, UnDefType.UNDEF);
                    } else {
                        updateThingChannelState(CHANNEL_NEXT_CHANGE,
                                new DateTimeType(ZonedDateTime.ofInstant(
                                        Instant.ofEpochSecond(device.getHkr().getNextchange().getEndperiod()),
                                        ZoneId.systemDefault())));
                    }
                    if (TEMP_FRITZ_UNDEFINED.equals(device.getHkr().getNextchange().getTchange())) {
                        updateThingChannelState(CHANNEL_NEXTTEMP, UnDefType.UNDEF);
                    } else {
                        updateThingChannelState(CHANNEL_NEXTTEMP, new QuantityType<>(
                                toCelsius(device.getHkr().getNextchange().getTchange()), SIUnits.CELSIUS));
                    }
                }
                if (device.getHkr().getBattery() == null) {
                    updateThingChannelState(CHANNEL_BATTERY, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(CHANNEL_BATTERY, new DecimalType(device.getHkr().getBattery()));
                }
                if (device.getHkr().getBatterylow() == null) {
                    updateThingChannelState(CHANNEL_BATTERY_LOW, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(CHANNEL_BATTERY_LOW,
                            BATTERY_ON.equals(device.getHkr().getBatterylow()) ? OnOffType.ON : OnOffType.OFF);
                }
            }
            if (device instanceof DeviceModel && device.isAlarmSensor() && ((DeviceModel) device).getAlert() != null) {
                updateThingChannelState(CHANNEL_CONTACT_STATE,
                        AlertModel.ON.equals(((DeviceModel) device).getAlert().getState()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
            }
            if (device instanceof DeviceModel && device.isButton() && ((DeviceModel) device).getButton() != null) {
                if (((DeviceModel) device).getButton().getLastpressedtimestamp() == 0) {
                    updateThingChannelState(CHANNEL_LAST_CHANGE, UnDefType.UNDEF);
                } else {
                    ZoneId zoneId = ZoneId.systemDefault();
                    ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(((DeviceModel) device).getButton().getLastpressedtimestamp()),
                            zoneId);
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
    }

    /**
     * Updates thing properties.
     *
     * @param device the {@link AVMFritzBaseModel}
     */
    private void updateProperties(AVMFritzBaseModel device) {
        Map<String, String> editProperties = editProperties();
        editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
        if (device instanceof GroupModel && ((GroupModel) device).getGroupinfo() != null) {
            editProperties.put(PROPERTY_MASTER, ((GroupModel) device).getGroupinfo().getMasterdeviceid());
            editProperties.put(PROPERTY_MEMBERS, ((GroupModel) device).getGroupinfo().getMembers());
        }
        updateProperties(editProperties);
    }

    /**
     * Updates thing channels.
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
                    ? new ChannelTypeUID("system", "battery-level")
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
                    if (state != null) {
                        state.getSwitch().setState(OnOffType.ON.equals(command) ? SwitchModel.ON : SwitchModel.OFF);
                    }
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                }
                break;
            case CHANNEL_SETTEMP:
                if (command instanceof DecimalType) {
                    BigDecimal temperature = normalizeCelsius(((DecimalType) command).toBigDecimal());
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, fromCelsius(temperature));
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else if (command instanceof QuantityType) {
                    @SuppressWarnings({ "unchecked", "null" })
                    BigDecimal temperature = normalizeCelsius(
                            ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS).toBigDecimal());
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, fromCelsius(temperature));
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else if (command instanceof IncreaseDecreaseType) {
                    BigDecimal temperature = state.getHkr().getTsoll();
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        temperature.add(BigDecimal.ONE);
                    } else {
                        temperature.subtract(BigDecimal.ONE);
                    }
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else if (command instanceof OnOffType) {
                    BigDecimal temperature = OnOffType.ON.equals(command) ? TEMP_FRITZ_ON : TEMP_FRITZ_OFF;
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                }
                break;
            case CHANNEL_RADIATOR_MODE:
                if (command instanceof StringType) {
                    switch (command.toString()) {
                        case MODE_ON:
                            if (state != null) {
                                state.getHkr().setTsoll(TEMP_FRITZ_ON);
                            }
                            fritzBox.setSetTemp(ain, TEMP_FRITZ_ON);
                            updateState(CHANNEL_SETTEMP, new QuantityType<>(toCelsius(TEMP_FRITZ_ON), SIUnits.CELSIUS));
                            break;
                        case MODE_OFF:
                            if (state != null) {
                                state.getHkr().setTsoll(TEMP_FRITZ_OFF);
                            }
                            fritzBox.setSetTemp(ain, TEMP_FRITZ_OFF);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(toCelsius(TEMP_FRITZ_OFF), SIUnits.CELSIUS));
                            break;
                        case MODE_COMFORT:
                            BigDecimal comfortTemperature = state.getHkr().getKomfort();
                            state.getHkr().setTsoll(comfortTemperature);
                            fritzBox.setSetTemp(ain, comfortTemperature);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(toCelsius(comfortTemperature), SIUnits.CELSIUS));
                            break;
                        case MODE_ECO:
                            BigDecimal ecoTemperature = state.getHkr().getAbsenk();
                            state.getHkr().setTsoll(ecoTemperature);
                            fritzBox.setSetTemp(ain, ecoTemperature);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(toCelsius(ecoTemperature), SIUnits.CELSIUS));
                            break;
                        case MODE_BOOST:
                            if (state != null) {
                                state.getHkr().setTsoll(TEMP_FRITZ_MAX);
                            }
                            fritzBox.setSetTemp(ain, TEMP_FRITZ_MAX);
                            updateState(CHANNEL_SETTEMP,
                                    new QuantityType<>(toCelsius(TEMP_FRITZ_MAX), SIUnits.CELSIUS));
                            break;
                        case MODE_UNKNOWN:
                        case MODE_WINDOW_OPEN:
                            logger.debug("Command '{}' is a read-only command for channel {}.", command, channelId);
                            break;
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
