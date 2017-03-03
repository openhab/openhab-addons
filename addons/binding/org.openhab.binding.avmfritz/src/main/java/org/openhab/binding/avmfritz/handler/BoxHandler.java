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

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.config.AvmFritzConfiguration;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a FRITZ!Box device. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf
 *
 */
public class BoxHandler extends BaseBridgeHandler implements IFritzHandler {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * the refresh interval which is used to poll values from the fritzaha.
     * server (optional, defaults to 15 s)
     */
    private long refreshInterval = 15;
    /**
     * Interface object for querying the FRITZ!Box web interface
     */
    private FritzahaWebInterface connection;
    /**
     * Holder for last data received from the box.
     */
    private Map<String, DeviceModel> deviceList;
    /**
     * Job which will do the FRITZ!Box polling
     */
    private DeviceListPolling pollingRunnable;
    /**
     * Schedule for polling
     */
    private ScheduledFuture<?> pollingJob;

    /**
     * Constructor
     *
     * @param bridge
     *            Bridge object representing a FRITZ!Box
     */
    public BoxHandler(Bridge bridge) {
        super(bridge);
        this.deviceList = new TreeMap<String, DeviceModel>();
        this.pollingRunnable = new DeviceListPolling(this);
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("About to initialize bridge " + BindingConstants.BRIDGE_FRITZBOX);
        Bridge bridge = this.getThing();
        AvmFritzConfiguration config = this.getConfigAs(AvmFritzConfiguration.class);

        logger.debug("discovered fritzaha bridge initialized: " + config.toString());

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
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDeviceList(DeviceModel model) {
        try {
            logger.debug("set device model: " + model.toString());
            this.deviceList.put(model.getIdentifier(), model);
            ThingUID thingUID = this.getThingUID(model);
            Thing thing = this.getThingByUID(thingUID);
            if (thing != null) {
                logger.debug("update thing " + thingUID + " with device model: " + model.toString());
                this.updateThingFromDevice(thing, model);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FritzahaWebInterface getWebInterface() {
        return this.connection;
    }

    /**
     * Updates things from device model.
     *
     * @param thing
     *            Thing to be updated.
     * @param device
     *            Device model with new data.
     */
    private void updateThingFromDevice(Thing thing, DeviceModel device) {
        if (thing == null || device == null) {
            throw new IllegalArgumentException("thing or device null, cannot perform update");
        }
        if (device.getPresent() == 1) {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
            logger.debug("about to update " + thing.getUID() + " from " + device.toString());
            if (device.isTempSensor()) {
                Channel channel = thing.getChannel(CHANNEL_TEMP);
                this.updateState(channel.getUID(), new DecimalType(device.getTemperature().getCelsius()));
            }
            if (device.isPowermeter()) {
                Channel channelEnergy = thing.getChannel(CHANNEL_ENERGY);
                this.updateState(channelEnergy.getUID(), new DecimalType(device.getPowermeter().getEnergy()));
                Channel channelPower = thing.getChannel(CHANNEL_POWER);
                this.updateState(channelPower.getUID(), new DecimalType(device.getPowermeter().getPower()));
            }
            if (device.isSwitchableOutlet()) {
                Channel channel = thing.getChannel(CHANNEL_SWITCH);
                if (device.getSwitch().getState().equals(SwitchModel.ON)) {
                    this.updateState(channel.getUID(), OnOffType.ON);
                } else if (device.getSwitch().getState().equals(SwitchModel.OFF)) {
                    this.updateState(channel.getUID(), OnOffType.OFF);
                } else {
                    logger.warn("unknown state " + device.getSwitch().getState() + " for channel " + channel.getUID());
                }
            }
        } else {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, null));
        }
    }

    /**
     * Builds a {@link ThingUID} from a device model. The UID is build from
     * the {@link BindingConstants#BINDING_ID} and value of
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
        if (this.getThing() != null) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                logger.debug("start polling job at intervall " + refreshInterval);
                pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, refreshInterval, TimeUnit.SECONDS);
            } else {
                logger.debug("pollingJob active");
            }
        } else {
            logger.warn("bridge is null");
        }
    }

    /**
     * Just logging - nothing to do.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("update " + channelUID.getAsString() + " with " + command.toString());
    }

    /**
     * Called from {@link FritzahaWebInterface#authenticate()} to update
     * the bridge status because updateStatus is protected.
     *
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }
}
