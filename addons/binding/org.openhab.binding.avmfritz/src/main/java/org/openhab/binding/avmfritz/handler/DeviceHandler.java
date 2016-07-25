/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.openhab.binding.avmfritz.BindingConstants.*;

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
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.avmfritz.BindingConstants;
import org.openhab.binding.avmfritz.config.AvmFritzConfiguration;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaSetSwitchCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Robert Bausdorf - Initial contribution
 *
 */
public class DeviceHandler extends BaseThingHandler implements IFritzHandler {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Ip of PL546E in standalone mode
     */
    private String soloIp;
    /**
     * the refresh interval which is used to poll values from the fritzaha.
     * server (optional, defaults to 15 s)
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
            logger.debug("About to initialize thing " + BindingConstants.DEVICE_PL546E_STANDALONE);
            Thing thing = this.getThing();
            AvmFritzConfiguration config = this.getConfigAs(AvmFritzConfiguration.class);
            this.soloIp = config.getIpAddress();

            logger.debug("discovered PL546E initialized: " + config.toString());

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
                logger.debug("start polling job at intervall " + refreshInterval);
                pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, refreshInterval, TimeUnit.SECONDS);
            } else {
                logger.debug("pollingJob active");
            }
        } else {
            logger.warn("thing is null");
        }
    }

    /**
     * Handle the commands for switchable outlets.
     * TODO: test switch behaviour on PL546E standalone
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("command for " + channelUID.getAsString() + ": " + command.toString());
        if (channelUID.getId().equals(CHANNEL_SWITCH)) {
            logger.debug("update " + channelUID.getAsString() + " with " + command.toString());
            FritzahaWebInterface fritzBox = null;
            if (!thing.getThingTypeUID().equals(PL546E_STANDALONE_THING_TYPE)) {
                Bridge bridge = this.getBridge();
                if (bridge != null && bridge.getHandler() instanceof BoxHandler) {
                    fritzBox = ((BoxHandler) bridge.getHandler()).getWebInterface();
                }
            } else {
                fritzBox = this.getWebInterface();
            }
            if (fritzBox != null && this.getThing().getConfiguration().get(THING_AIN) != null) {
                if (command instanceof OnOffType) {
                    FritzAhaSetSwitchCallback callback = new FritzAhaSetSwitchCallback(fritzBox,
                            this.getThing().getConfiguration().get(THING_AIN).toString(),
                            command.equals(OnOffType.ON) ? true : false);
                    fritzBox.asyncGet(callback);
                }
            }
        } else {
            logger.error("unknown channel uid " + channelUID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public FritzahaWebInterface getWebInterface() {
        return this.connection;
    }

    @Override
    public void addDeviceList(DeviceModel model) {
        try {
            logger.debug("set device model: " + model.toString());
            Thing thing = this.getThing();
            if (thing != null) {
                logger.debug("update thing " + thing.getUID() + " with device model: " + model.toString());
                logger.debug("about to update " + thing.getUID() + " from " + model.toString());
                if (model.isTempSensor()) {
                    Channel channel = thing.getChannel(CHANNEL_TEMP);
                    this.updateState(channel.getUID(), new DecimalType(model.getTemperature().getCelsius()));
                }
                if (model.isPowermeter()) {
                    Channel channelEnergy = thing.getChannel(CHANNEL_ENERGY);
                    this.updateState(channelEnergy.getUID(), new DecimalType(model.getPowermeter().getEnergy()));
                    Channel channelPower = thing.getChannel(CHANNEL_POWER);
                    this.updateState(channelPower.getUID(), new DecimalType(model.getPowermeter().getPower()));
                }
                if (model.isSwitchableOutlet()) {
                    Channel channel = thing.getChannel(CHANNEL_SWITCH);
                    if (model.getSwitch().getState().equals(SwitchModel.ON)) {
                        this.updateState(channel.getUID(), OnOffType.ON);
                    } else if (model.getSwitch().getState().equals(SwitchModel.OFF)) {
                        this.updateState(channel.getUID(), OnOffType.OFF);
                    } else {
                        logger.warn(
                                "unknown state " + model.getSwitch().getState() + " for channel " + channel.getUID());
                    }
                }
                // save AIN to config for PL546E standalone
                if (thing.getConfiguration().get(THING_AIN) == null) {
                    thing.getConfiguration().put(THING_AIN, model.getIdentifier());
                }
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
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
        } else if (thingTypeUID.equals(PL546E_STANDALONE_THING_TYPE)) {
            String thingName = this.soloIp.replaceAll("[^a-zA-Z0-9_]", "_");
            ThingUID thingUID = new ThingUID(thingTypeUID, thingName);
            return thingUID;
        } else {
            return null;
        }
    }
}
