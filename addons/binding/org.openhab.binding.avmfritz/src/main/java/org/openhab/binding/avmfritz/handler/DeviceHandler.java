/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.config.AvmFritzConfiguration;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.HeatingModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.ahamodel.TemperatureModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 *
 */
public class DeviceHandler extends BaseThingHandler implements IFritzHandler {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    /**
     * Ip of PL546E in standalone mode
     */
    private String soloIp;
    /**
     * the refresh interval which is used to poll values from the FRITZ!Box web
     * interface server (optional, defaults to 15 s)
     */
    protected long refreshInterval = 15;
    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    protected FritzahaWebInterface connection;
    /**
     * Job which will do the FRITZ!Box polling
     */
    private DeviceListPolling pollingRunnable;
    /**
     * Schedule for polling
     */
    private ScheduledFuture<?> pollingJob;

    public DeviceHandler(Thing thing) {
        super(thing);
        this.pollingRunnable = new DeviceListPolling(this);
    }

    /**
     * Initializes the bridge.
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
        if (this.getThing() != null) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                logger.debug("start polling job at intervall {}", refreshInterval);
                pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, refreshInterval, TimeUnit.SECONDS);
            } else {
                logger.debug("pollingJob active");
            }
        } else {
            logger.warn("thing is null");
        }
    }

    /**
     * Handle the commands for switchable outlets or heating thermostats. TODO:
     * test switch behaviour on PL546E standalone
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {}", channelUID.getIdWithoutGroup(), command);
        FritzahaWebInterface fritzBox = null;
        if (!getThing().getThingTypeUID().equals(PL546E_STANDALONE_THING_TYPE)) {
            Bridge bridge = getBridge();
            if (bridge != null && bridge.getHandler() instanceof BoxHandler) {
                fritzBox = ((BoxHandler) bridge.getHandler()).getWebInterface();
            }
        } else {
            fritzBox = getWebInterface();
        }
        String ain = getThing().getConfiguration().get(THING_AIN).toString();
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_TEMP:
            case CHANNEL_ENERGY:
            case CHANNEL_POWER:
            case CHANNEL_ECOTEMP:
            case CHANNEL_COMFORTTEMP:
            case CHANNEL_MODE:
            case CHANNEL_LOCKED:
            case CHANNEL_ACTUALTEMP:
            case CHANNEL_NEXTCHANGE:
            case CHANNEL_NEXTTEMP:
            case CHANNEL_BATTERY:
                break;
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    fritzBox.setSwitch(ain, command.equals(OnOffType.ON) ? true : false);
                }
                break;
            case CHANNEL_SETTEMP:
                if (command instanceof DecimalType) {
                    BigDecimal temperature = new BigDecimal(command.toString());
                    fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(temperature));
                } else if (command instanceof OnOffType) {
                    BigDecimal temperature = command.equals(OnOffType.ON) ? HeatingModel.TEMP_FRITZ_ON
                            : HeatingModel.TEMP_FRITZ_OFF;
                    fritzBox.setSetTemp(ain, temperature);
                }
                break;
            case CHANNEL_RADIATOR_MODE:
                if (command instanceof StringType) {
                    String mode = command.toString();
                    if (command.equals(MODE_ON)) {
                        BigDecimal settemp = (BigDecimal) getThing().getConfiguration().get(THING_SETTEMP);
                        fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(settemp));
                        Channel channelSetTemp = getThing().getChannel(CHANNEL_SETTEMP);
                        updateState(channelSetTemp.getUID(), new DecimalType(settemp));
                    } else if (command.equals(MODE_OFF)) {
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_OFF);
                        Channel channelSetTemp = getThing().getChannel(CHANNEL_SETTEMP);
                        updateState(channelSetTemp.getUID(),
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_OFF)));
                    } else if (command.equals(MODE_COMFORT)) {
                        BigDecimal comfort_temp = (BigDecimal) getThing().getConfiguration().get(THING_COMFORTTEMP);
                        fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(comfort_temp));
                        Channel channelSetTemp = getThing().getChannel(CHANNEL_SETTEMP);
                        updateState(channelSetTemp.getUID(), new DecimalType(comfort_temp));
                    } else if (command.equals(MODE_ECO)) {
                        BigDecimal eco_temp = (BigDecimal) getThing().getConfiguration().get(THING_ECOTEMP);
                        fritzBox.setSetTemp(ain, HeatingModel.fromCelsius(eco_temp));
                        Channel channelSetTemp = getThing().getChannel(CHANNEL_SETTEMP);
                        updateState(channelSetTemp.getUID(), new DecimalType(eco_temp));
                    } else if (command.equals(MODE_BOOST)) {
                        fritzBox.setSetTemp(ain, HeatingModel.TEMP_FRITZ_MAX);
                        Channel channelSetTemp = getThing().getChannel(CHANNEL_SETTEMP);
                        updateState(channelSetTemp.getUID(),
                                new DecimalType(HeatingModel.toCelsius(HeatingModel.TEMP_FRITZ_MAX)));
                    } else {
                        logger.warn("Received unknown command {} for channel {}", command.toString(),
                                CHANNEL_RADIATOR_MODE);
                    }
                }
                break;
            default:
                logger.debug("Received unknown channel {}", channelUID.getIdWithoutGroup());
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FritzahaWebInterface getWebInterface() {
        return this.connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDeviceList(DeviceModel device) {
        try {
            logger.debug("set device model: {}", device);
            ThingUID thingUID = getThingUID(device);
            Thing thing = getThing();
            if (thing != null) {
                logger.debug("update thing {} with device model: {}", thingUID, device);
                updateThingFromDevice(thing, device);
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
            if (device.isTempSensor() && device.getTemperature() != null) {
                Channel channelTemp = thing.getChannel(CHANNEL_TEMP);
                updateState(channelTemp.getUID(),
                        new DecimalType(TemperatureModel.toCelsius(device.getTemperature().getCelsius())));
            }
            if (device.isPowermeter() && device.getPowermeter() != null) {
                Channel channelEnergy = thing.getChannel(CHANNEL_ENERGY);
                updateState(channelEnergy.getUID(), new DecimalType(device.getPowermeter().getEnergy()));
                Channel channelPower = thing.getChannel(CHANNEL_POWER);
                updateState(channelPower.getUID(), new DecimalType(device.getPowermeter().getPower()));
            }
            if (device.isSwitchableOutlet() && device.getSwitch() != null) {
                Channel channelMode = thing.getChannel(CHANNEL_MODE);
                if (channelMode != null) {
                    updateState(channelMode.getUID(), new StringType(device.getSwitch().getMode()));
                } else {
                    logger.warn("Channel {} in thing {} does not exist, please recreate the thing", CHANNEL_MODE,
                            thing.getUID());
                }
                Channel channelLocked = thing.getChannel(CHANNEL_LOCKED);
                if (channelLocked != null) {
                    updateState(channelLocked.getUID(), device.getSwitch().getLock().equals(BigDecimal.ONE)
                            ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                } else {
                    logger.warn("Channel {} in thing {} does not exist, please recreate the thing", CHANNEL_LOCKED,
                            thing.getUID());
                }
                Channel channelSwitch = thing.getChannel(CHANNEL_SWITCH);
                if (device.getSwitch().getState() == null) {
                    updateState(channelSwitch.getUID(), UnDefType.UNDEF);
                } else if (device.getSwitch().getState().equals(SwitchModel.ON)) {
                    updateState(channelSwitch.getUID(), OnOffType.ON);
                } else if (device.getSwitch().getState().equals(SwitchModel.OFF)) {
                    updateState(channelSwitch.getUID(), OnOffType.OFF);
                } else {
                    logger.warn("Received unknown value {} for channel {}", device.getSwitch().getState(),
                            channelSwitch.getUID());
                }
            }
            if (device.isHeatingThermostat() && device.getHkr() != null) {
                Channel channelMode = thing.getChannel(CHANNEL_MODE);
                if (channelMode != null) {
                    updateState(channelMode.getUID(), new StringType(device.getHkr().getMode()));
                } else {
                    logger.warn("Channel {} in thing {} does not exist, please recreate the thing", CHANNEL_MODE,
                            thing.getUID());
                }
                Channel channelLocked = thing.getChannel(CHANNEL_LOCKED);
                if (channelLocked != null) {
                    updateState(channelLocked.getUID(), device.getHkr().getLock().equals(BigDecimal.ONE)
                            ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                } else {
                    logger.warn("Channel {} in thing {} does not exist, please recreate the thing", CHANNEL_LOCKED,
                            thing.getUID());
                }
                Channel channelActualTemp = thing.getChannel(CHANNEL_ACTUALTEMP);
                updateState(channelActualTemp.getUID(),
                        new DecimalType(HeatingModel.toCelsius(device.getHkr().getTist())));
                BigDecimal settemp = HeatingModel.toCelsius(device.getHkr().getTsoll());
                if (HeatingModel.inCelsiusRange(settemp)) {
                    thing.getConfiguration().put(THING_SETTEMP, settemp);
                }
                Channel channelSetTemp = thing.getChannel(CHANNEL_SETTEMP);
                updateState(channelSetTemp.getUID(), new DecimalType(settemp));
                BigDecimal ecotemp = HeatingModel.toCelsius(device.getHkr().getAbsenk());
                thing.getConfiguration().put(THING_ECOTEMP, ecotemp);
                Channel channelEcoTemp = thing.getChannel(CHANNEL_ECOTEMP);
                updateState(channelEcoTemp.getUID(), new DecimalType(ecotemp));
                BigDecimal comforttemp = HeatingModel.toCelsius(device.getHkr().getKomfort());
                thing.getConfiguration().put(THING_COMFORTTEMP, comforttemp);
                Channel channelComfortTemp = thing.getChannel(CHANNEL_COMFORTTEMP);
                updateState(channelComfortTemp.getUID(), new DecimalType(comforttemp));
                Channel channelRadiatorMode = thing.getChannel(CHANNEL_RADIATOR_MODE);
                updateState(channelRadiatorMode.getUID(), new StringType(device.getHkr().getRadiatorMode()));
                if (device.getHkr().getNextchange() != null) {
                    Channel channelNextChange = thing.getChannel(CHANNEL_NEXTCHANGE);
                    if (device.getHkr().getNextchange().getEndperiod() == 0) {
                        updateState(channelNextChange.getUID(), UnDefType.UNDEF);
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date(device.getHkr().getNextchange().getEndperiod() * 1000L));
                        updateState(channelNextChange.getUID(), new DateTimeType(calendar));
                    }
                    Channel channelNextTemp = thing.getChannel(CHANNEL_NEXTTEMP);
                    updateState(channelNextTemp.getUID(),
                            new DecimalType(HeatingModel.toCelsius(device.getHkr().getNextchange().getTchange())));
                }
                Channel channelBattery = thing.getChannel(CHANNEL_BATTERY);
                if (device.getHkr().getBatterylow() == null) {
                    updateState(channelBattery.getUID(), UnDefType.UNDEF);
                } else if (device.getHkr().getBatterylow().equals(HeatingModel.BATTERY_ON)) {
                    updateState(channelBattery.getUID(), OnOffType.ON);
                } else if (device.getHkr().getBatterylow().equals(HeatingModel.BATTERY_OFF)) {
                    updateState(channelBattery.getUID(), OnOffType.OFF);
                } else {
                    logger.warn("Received unknown value {} for channel {}", device.getHkr().getBatterylow(),
                            channelBattery.getUID());
                }
            }
            // save AIN to config for PL546E standalone
            if (thing.getConfiguration().get(THING_AIN) == null) {
                thing.getConfiguration().put(THING_AIN, device.getIdentifier());
            }
        } else {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device not present"));
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
}
