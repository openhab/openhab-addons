/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
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
 * Handler for a FRITZ!Box device. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 *
 */
public class BoxHandler extends BaseBridgeHandler implements IFritzHandler {

    private final Logger logger = LoggerFactory.getLogger(BoxHandler.class);

    /**
     * Initial delay in s for polling job.
     */
    private static final int INITIAL_DELAY = 1;
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
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public BoxHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("About to initialize FRITZ!Box {}", BindingConstants.BRIDGE_FRITZBOX);
        Bridge bridge = this.getThing();
        AvmFritzConfiguration config = this.getConfigAs(AvmFritzConfiguration.class);

        logger.debug("Discovered FRITZ!Box initialized: {}", config);

        this.refreshInterval = config.getPollingInterval();
        this.connection = new FritzahaWebInterface(config, this);
        if (config.getPassword() != null) {
            this.onUpdate();
        } else {
            bridge.setStatusInfo(
                    new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no password set"));
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public FritzahaWebInterface getWebInterface() {
        return connection;
    }

    @Override
    public void addDeviceList(DeviceModel device) {
        try {
            logger.debug("set device model: {}", device);
            ThingUID thingUID = getThingUID(device);
            Thing thing = getThingByUID(thingUID);
            if (thing != null) {
                logger.debug("update thing {} with device model: {}", thingUID, device);
                DeviceHandler handler = (DeviceHandler) thing.getHandler();
                if (handler != null) {
                    handler.setState(device);
                }
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
        } else {
            return null;
        }
    }

    /**
     * Start the polling.
     */
    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}s", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                if (getWebInterface() != null) {
                    logger.debug("polling FRITZ!Box {}", getThing().getUID());
                    FritzAhaUpdateXmlCallback callback = new FritzAhaUpdateXmlCallback(getWebInterface(), this);
                    getWebInterface().asyncGet(callback);
                }
            }, INITIAL_DELAY, refreshInterval, TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob active");
        }
    }

    /**
     * Just logging - nothing to do.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("command for {}: {}", channelUID, command);
        if (command instanceof RefreshType) {
            if (getWebInterface() != null) {
                logger.debug("polling FRITZ!Box {}", getThing().getUID());
                FritzAhaUpdateXmlCallback callback = new FritzAhaUpdateXmlCallback(getWebInterface(), this);
                getWebInterface().asyncGet(callback);
            }
            return;
        }
    }
}
