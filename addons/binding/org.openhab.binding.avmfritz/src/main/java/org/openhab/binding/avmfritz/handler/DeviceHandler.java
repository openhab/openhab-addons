/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.HeatingModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.config.AvmFritzConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateXmlCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ! device. Handles commands , which are sent to one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 *
 */
public class DeviceHandler extends BaseThingHandler implements IFritzHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    /**
     * IP of FRITZ!Powerline 546E in stand-alone mode
     */
    private String soloIp;
    /**
     * Refresh interval which is used to poll values from the FRITZ!Box web interface (optional, defaults to 15 s)
     */
    private long refreshInterval = 15;
    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    private FritzahaWebInterface connection;
    /**
     * Schedule for polling
     */
    private ScheduledFuture<?> pollingJob;

    /**
     * keeps track of the current state for handling of increase/decrease
     */
    private DeviceModel state;

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! device
     */
    public DeviceHandler(@NonNull Thing thing) {
        super(thing);
    }

    /**
     * Initializes the thing.
     */
    @Override
    public void initialize() {
        if (this.getThing().getThingTypeUID().equals(PL546E_STANDALONE_THING_TYPE)) {
            logger.debug("About to initialize thing {}", BindingConstants.DEVICE_PL546E_STANDALONE);
            Thing thing = this.getThing();
            AvmFritzConfiguration config = this.getConfigAs(AvmFritzConfiguration.class);
            this.soloIp = config.getIpAddress();

            logger.debug("discovered PL546E initialized: {}", config);

            this.refreshInterval = config.getPollingInterval();
            this.connection = new FritzahaWebInterface(config, this);
            if (config.getPassword() != null) {
                this.onUpdate();
            } else {
                thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "no password set"));
            }
        }
    }

    /**
     * Disposes the thing.
     */
    @Override
    public void dispose() {
        if (this.getThing().getThingTypeUID().equals(PL546E_STANDALONE_THING_TYPE)) {
            logger.debug("Handler disposed.");
            if (pollingJob != null && !pollingJob.isCancelled()) {
                pollingJob.cancel(true);
                pollingJob = null;
            }
        }
    }

    /**
     * Start the polling.
     */
    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                if (getWebInterface() != null) {
                    logger.debug("polling FRITZ!Box {}", getThing().getUID());
                    FritzAhaUpdateXmlCallback callback = new FritzAhaUpdateXmlCallback(getWebInterface(), this);
                    getWebInterface().asyncGet(callback);
                }
            }, 1, refreshInterval, TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob active");
        }
    }

    /**
     * Handle the commands for switchable outlets or heating thermostats. TODO:
     * test switch behaviour on PL546E stand-alone
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        FritzahaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            return;
        }
        String ain = getThing().getConfiguration().get(THING_AIN).toString();
        switch (channelId) {
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_DEVICE_LOCKED:
            case CHANNEL_TEMP:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_ACTUALTEMP:
            case CHANNEL_ECOTEMP:
            case CHANNEL_COMFORTTEMP:
            case CHANNEL_NEXTCHANGE:
            case CHANNEL_NEXTTEMP:
            case CHANNEL_BATTERY:
                logger.debug("Channel {} is a read-only channel and cannot handle command '{}'.", channelId, command);
                break;
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    state.getSwitch().setState(OnOffType.ON.equals(command) ? SwitchModel.ON : SwitchModel.OFF);
                    fritzBox.setSwitch(ain, OnOffType.ON.equals(command));
                } else {
                    logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_SWITCH);
                }
                break;
            case CHANNEL_SETTEMP:
                if (command instanceof DecimalType) {
                    BigDecimal temperature = new BigDecimal(command.toString());
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(temperature));
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
                    BigDecimal temperature = OnOffType.ON.equals(command) ? HeatingModel.TEMP_FRITZ_ON
                            : HeatingModel.TEMP_FRITZ_OFF;
                    state.getHkr().setTsoll(temperature);
                    fritzBox.setSetTemp(ain, temperature);
                    updateState(CHANNEL_RADIATOR_MODE, new StringType(state.getHkr().getRadiatorMode()));
                } else {
                    logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_SETTEMP);
                }
                break;
            case CHANNEL_RADIATOR_MODE:
                if (command instanceof StringType) {
                    String commandString = command.toString();
                    if (MODE_ON.equals(commandString)) {
                        state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_ON);
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_ON);
                        updateState(CHANNEL_SETTEMP,
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_ON)));
                    } else if (MODE_OFF.equals(commandString)) {
                        state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_OFF);
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_OFF);
                        updateState(CHANNEL_SETTEMP,
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_OFF)));
                    } else if (MODE_COMFORT.equals(commandString)) {
                        BigDecimal comfort_temp = state.getHkr().getKomfort();
                        state.getHkr().setTsoll(comfort_temp);
                        fritzBox.setSetTemp(ain, comfort_temp);
                        updateState(CHANNEL_SETTEMP, new DecimalType(HeatingModel.toCelsius(comfort_temp)));
                    } else if (MODE_ECO.equals(commandString)) {
                        BigDecimal eco_temp = state.getHkr().getAbsenk();
                        state.getHkr().setTsoll(eco_temp);
                        fritzBox.setSetTemp(ain, eco_temp);
                        updateState(CHANNEL_SETTEMP, new DecimalType(HeatingModel.toCelsius(eco_temp)));
                    } else if (MODE_BOOST.equals(commandString)) {
                        state.getHkr().setTsoll(HeatingModel.TEMP_FRITZ_MAX);
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_MAX);
                        updateState(CHANNEL_SETTEMP,
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_MAX)));
                    } else {
                        logger.warn("Received unknown command '{}' for channel {}", command, CHANNEL_RADIATOR_MODE);
                    }
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelId);
                break;
        }
    }

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public FritzahaWebInterface getWebInterface() {
        if (getThing().getThingTypeUID().equals(PL546E_STANDALONE_THING_TYPE)) {
            return connection;
        } else {
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler handler = bridge.getHandler();
                if (handler != null && handler instanceof BoxHandler) {
                    return ((BoxHandler) handler).getWebInterface();
                }
            }
        }
        return null;
    }

    @Override
    public void addDeviceList(DeviceModel device) {
        try {
            logger.debug("set device model: {}", device);
            ThingUID thingUID = getThingUID(device);
            if (thingUID.equals(thing.getUID())) {
                logger.debug("update thing {} with device model: {}", thingUID, device);
                DeviceHandler handler = (DeviceHandler) thing.getHandler();
                if (handler != null) {
                    handler.setState(device);
                }
                updateThingFromDevice(getThing(), device);
            }
        } catch (Exception e) {
            logger.error("{}", e.getLocalizedMessage(), e);
        }
    }

    /**
     * Updates things from device model.
     *
     * @param thing Thing to be updated.
     * @param device Device model with new data.
     */
    private void updateThingFromDevice(Thing thing, DeviceModel device) {
        if (thing == null || device == null) {
            throw new IllegalArgumentException("thing or device is null, cannot perform update");
        }
        if (device.getPresent() == 1) {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
            thing.setProperty(PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
            if (device.isTempSensor() && device.getTemperature() != null) {
                updateThingChannelState(thing, CHANNEL_TEMP, new DecimalType(device.getTemperature().getCelsius()));
            }
            if (device.isPowermeter() && device.getPowermeter() != null) {
                updateThingChannelState(thing, CHANNEL_ENERGY, new DecimalType(device.getPowermeter().getEnergy()));
                updateThingChannelState(thing, CHANNEL_POWER, new DecimalType(device.getPowermeter().getPower()));
            }
            if (device.isSwitchableOutlet() && device.getSwitch() != null) {
                updateThingChannelState(thing, CHANNEL_MODE, new StringType(device.getSwitch().getMode()));
                updateThingChannelState(thing, CHANNEL_LOCKED,
                        BigDecimal.ZERO.equals(device.getSwitch().getLock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                updateThingChannelState(thing, CHANNEL_DEVICE_LOCKED,
                        BigDecimal.ZERO.equals(device.getSwitch().getDevicelock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                if (device.getSwitch().getState() == null) {
                    updateThingChannelState(thing, CHANNEL_SWITCH, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(thing, CHANNEL_SWITCH,
                            SwitchModel.ON.equals(device.getSwitch().getState()) ? OnOffType.ON : OnOffType.OFF);
                }
            }
            if (device.isHeatingThermostat() && device.getHkr() != null) {
                updateThingChannelState(thing, CHANNEL_MODE, new StringType(device.getHkr().getMode()));
                updateThingChannelState(thing, CHANNEL_LOCKED,
                        BigDecimal.ZERO.equals(device.getHkr().getLock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                updateThingChannelState(thing, CHANNEL_DEVICE_LOCKED,
                        BigDecimal.ZERO.equals(device.getHkr().getDevicelock()) ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED);
                updateThingChannelState(thing, CHANNEL_ACTUALTEMP,
                        new DecimalType(HeatingModel.toCelsius(device.getHkr().getTist())));
                updateThingChannelState(thing, CHANNEL_SETTEMP,
                        new DecimalType(HeatingModel.toCelsius(device.getHkr().getTsoll())));
                updateThingChannelState(thing, CHANNEL_ECOTEMP,
                        new DecimalType(HeatingModel.toCelsius(device.getHkr().getAbsenk())));
                updateThingChannelState(thing, CHANNEL_COMFORTTEMP,
                        new DecimalType(HeatingModel.toCelsius(device.getHkr().getKomfort())));
                updateThingChannelState(thing, CHANNEL_RADIATOR_MODE,
                        new StringType(device.getHkr().getRadiatorMode()));
                if (device.getHkr().getNextchange() != null) {
                    if (device.getHkr().getNextchange().getEndperiod() == 0) {
                        updateThingChannelState(thing, CHANNEL_NEXTCHANGE, UnDefType.UNDEF);
                    } else {
                        final Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date(device.getHkr().getNextchange().getEndperiod() * 1000L));
                        updateThingChannelState(thing, CHANNEL_NEXTCHANGE, new DateTimeType(calendar));
                    }
                    if (HeatingModel.TEMP_FRITZ_UNDEFINED.equals(device.getHkr().getNextchange().getTchange())) {
                        updateThingChannelState(thing, CHANNEL_NEXTTEMP, UnDefType.UNDEF);
                    } else {
                        updateThingChannelState(thing, CHANNEL_NEXTTEMP,
                                new DecimalType(HeatingModel.toCelsius(device.getHkr().getNextchange().getTchange())));
                    }
                }
                if (device.getHkr().getBatterylow() == null) {
                    updateThingChannelState(thing, CHANNEL_BATTERY, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(thing, CHANNEL_BATTERY,
                            HeatingModel.BATTERY_ON.equals(device.getHkr().getBatterylow()) ? OnOffType.ON
                                    : OnOffType.OFF);
                }
            }
            // save AIN to config for PL546E stand-alone
            if (thing.getConfiguration().get(THING_AIN) == null) {
                thing.getConfiguration().put(THING_AIN, device.getIdentifier());
            }
        } else {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device not present"));
        }
    }

    /**
     * Updates thing channels.
     *
     * @param thing Thing which channels should be updated.
     * @param channelId ID of the channel to be updated.
     * @param state State to be set.
     */
    private void updateThingChannelState(Thing thing, String channelId, State state) {
        final Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state);
        } else {
            logger.warn("Channel {} in thing {} does not exist, please recreate the thing", channelId, thing.getUID());
        }
    }

    /**
     * Builds a {@link ThingUID} from a device model. The UID is build from the
     * {@link BindingConstants#BINDING_ID} and value of
     * {@link DeviceModel#getProductName()} in which all characters NOT matching
     * the regex [^a-zA-Z0-9_] are replaced by "_".
     *
     * @param device Discovered device model
     * @return ThingUID without illegal characters.
     */
    public ThingUID getThingUID(DeviceModel device) {
        ThingUID bridgeUID = this.getThing().getUID();
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID,
                device.getProductName().replaceAll("[^a-zA-Z0-9_]", "_"));

        if (BindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            String thingName = device.getIdentifier().replaceAll("[^a-zA-Z0-9_]", "_");
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, thingName);
            return thingUID;
        } else if (thingTypeUID.equals(PL546E_STANDALONE_THING_TYPE)) {
            String thingName = this.soloIp.replaceAll("[^a-zA-Z0-9_]", "_");
            ThingUID thingUID = new ThingUID(thingTypeUID, thingName);
            return thingUID;
        } else {
            return null;
        }
    }

    public DeviceModel getState() {
        return state;
    }

    public void setState(DeviceModel state) {
        this.state = state;
    }
}
