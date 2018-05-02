/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
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
import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.GroupModel;
import org.openhab.binding.avmfritz.internal.ahamodel.HeatingModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.config.AVMFritzConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateXmlCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! bridge. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public abstract class AVMFritzBaseBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzBaseBridgeHandler.class);

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
    @Nullable
    private FritzAhaWebInterface connection;
    /**
     * Schedule for polling
     */
    @Nullable
    private ScheduledFuture<?> pollingJob;
    /**
     * shared instance of HTTP client for asynchronous calls
     */
    private HttpClient httpClient;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public AVMFritzBaseBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("About to initialize FRITZ!Box {}", BRIDGE_FRITZBOX);
        Bridge bridge = getThing();
        AVMFritzConfiguration config = getConfigAs(AVMFritzConfiguration.class);

        logger.debug("Discovered FRITZ!Box initialized: {}", config);

        this.refreshInterval = config.getPollingInterval();
        this.connection = new FritzAhaWebInterface(config, this, httpClient);
        if (config.getPassword() != null) {
            onUpdate();
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

    /**
     * Start the polling.
     */
    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}s", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY, refreshInterval, TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob active");
        }
    }

    /**
     * Polls the bridge.
     */
    private void poll() {
        FritzAhaWebInterface webInterface = getWebInterface();
        if (webInterface != null) {
            logger.debug("polling FRITZ!Box {}", getThing().getUID());
            FritzAhaUpdateXmlCallback callback = new FritzAhaUpdateXmlCallback(webInterface, this);
            webInterface.asyncGet(callback);
        }
    }

    /**
     * Called from {@link FritzAhaWebInterface#authenticate()} to update
     * the bridge status because updateStatus is protected.
     *
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        super.updateStatus(status, statusDetail, description);
    }

    /**
     * Called from {@link FritzAhaUpdateXmlCallback} to provide new values for
     * things.
     *
     * @param model Device model with updated data.
     */
    public void addDeviceList(AVMFritzBaseModel device) {
        logger.debug("set device model: {}", device);
        ThingUID thingUID = getThingUID(device);
        if (thingUID != null) {
            Thing thing = getThingByUID(thingUID);
            if (thing != null) {
                logger.debug("update thing {} with device model: {}", thingUID, device);
                AVMFritzBaseThingHandler handler = (AVMFritzBaseThingHandler) thing.getHandler();
                if (handler != null) {
                    handler.setState(device);
                }
                updateThingFromDevice(thing, device);
            } else {
                logger.debug("no thing {} found for device model: {}", thingUID, device);
            }
            logger.debug("no thing UID found for device model: {}", device);
        }
    }

    /**
     * Updates thing from device model.
     *
     * @param thing Thing to be updated.
     * @param device Device model with new data.
     */
    protected void updateThingFromDevice(Thing thing, AVMFritzBaseModel device) {
        if (device.getPresent() == 1) {
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
            thing.setProperty(PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
            if (device instanceof GroupModel && ((GroupModel) device).getGroupinfo() != null) {
                thing.setProperty(PROPERTY_MASTER, ((GroupModel) device).getGroupinfo().getMasterdeviceid());
                thing.setProperty(PROPERTY_MEMBERS, ((GroupModel) device).getGroupinfo().getMembers());
            }
            if (device instanceof DeviceModel && device.isTempSensor()
                    && ((DeviceModel) device).getTemperature() != null) {
                updateThingChannelState(thing, CHANNEL_TEMP,
                        new QuantityType<>(((DeviceModel) device).getTemperature().getCelsius(), CELSIUS));
            }
            if (device.isPowermeter() && device.getPowermeter() != null) {
                updateThingChannelState(thing, CHANNEL_ENERGY,
                        new QuantityType<>(device.getPowermeter().getEnergy(), SmartHomeUnits.WATT_HOUR));
                updateThingChannelState(thing, CHANNEL_POWER,
                        new QuantityType<>(device.getPowermeter().getPower(), SmartHomeUnits.WATT));
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
                        new QuantityType<>(HeatingModel.toCelsius(device.getHkr().getTist()), CELSIUS));
                updateThingChannelState(thing, CHANNEL_SETTEMP,
                        new QuantityType<>(HeatingModel.toCelsius(device.getHkr().getTsoll()), CELSIUS));
                updateThingChannelState(thing, CHANNEL_ECOTEMP,
                        new QuantityType<>(HeatingModel.toCelsius(device.getHkr().getAbsenk()), CELSIUS));
                updateThingChannelState(thing, CHANNEL_COMFORTTEMP,
                        new QuantityType<>(HeatingModel.toCelsius(device.getHkr().getKomfort()), CELSIUS));
                updateThingChannelState(thing, CHANNEL_RADIATOR_MODE,
                        new StringType(device.getHkr().getRadiatorMode()));
                if (device.getHkr().getNextchange() != null) {
                    if (device.getHkr().getNextchange().getEndperiod() == 0) {
                        updateThingChannelState(thing, CHANNEL_NEXTCHANGE, UnDefType.UNDEF);
                    } else {
                        updateThingChannelState(thing, CHANNEL_NEXTCHANGE,
                                new DateTimeType(ZonedDateTime.ofInstant(
                                        Instant.ofEpochMilli(device.getHkr().getNextchange().getEndperiod()),
                                        ZoneId.systemDefault())));
                    }
                    if (HeatingModel.TEMP_FRITZ_UNDEFINED.equals(device.getHkr().getNextchange().getTchange())) {
                        updateThingChannelState(thing, CHANNEL_NEXTTEMP, UnDefType.UNDEF);
                    } else {
                        updateThingChannelState(thing, CHANNEL_NEXTTEMP, new QuantityType<>(
                                HeatingModel.toCelsius(device.getHkr().getNextchange().getTchange()), CELSIUS));
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
     * Called from {@link FritzAhaUpdateXmlCallback} to set missing devices to OFFLINE.
     */
    public void setMissingThingsOffline(Set<String> ains) {
        for (Thing thing : getThing().getThings()) {
            AVMFritzBaseThingHandler handler = (AVMFritzBaseThingHandler) thing.getHandler();
            if (handler != null) {
                if (!ains.contains(handler.getState().getIdentifier())) {
                    thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                            "Device not present in response"));
                }
            }
        }
    }

    /**
     * Provides the web interface object.
     *
     * @return The web interface object
     */
    @Nullable
    public FritzAhaWebInterface getWebInterface() {
        return connection;
    }

    /**
     * Builds a {@link ThingUID} from a device model. The UID is build from the {@link BindingConstants#BINDING_ID} and
     * value of {@link AVMFritzBaseModel#getProductName()} in which all characters NOT matching the RegEx [^a-zA-Z0-9_]
     * are replaced by "_".
     *
     * @param device Discovered device model
     * @return ThingUID without illegal characters.
     */
    @Nullable
    public ThingUID getThingUID(AVMFritzBaseModel device) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, getThingTypeId(device));
        ThingUID bridgeUID = getThing().getUID();
        String thingName = getThingName(device);

        if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new ThingUID(thingTypeUID, bridgeUID, thingName);
        } else if (device.isHeatingThermostat()) {
            return new ThingUID(GROUP_HEATING_THING_TYPE, bridgeUID, thingName);
        } else if (device.isSwitchableOutlet()) {
            return new ThingUID(GROUP_SWITCH_THING_TYPE, bridgeUID, thingName);
        } else {
            return null;
        }
    }

    /**
     *
     * @param device Discovered device model
     * @return ThingTypeId without illegal characters.
     */
    public String getThingTypeId(AVMFritzBaseModel device) {
        if (device instanceof GroupModel) {
            if (device.isHeatingThermostat()) {
                return GROUP_HEATING;
            } else if (device.isSwitchableOutlet()) {
                return GROUP_SWITCH;
            }
        }
        return device.getProductName().replaceAll(INVALID_PATTERN, "_");
    }

    /**
     *
     * @param device Discovered device model
     * @return Thing name without illegal characters.
     */
    public String getThingName(AVMFritzBaseModel device) {
        return device.getIdentifier().replaceAll(INVALID_PATTERN, "_");
    }

    /**
     * Just logging - nothing to do.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            scheduler.submit(() -> poll());
            return;
        }
    }
}
