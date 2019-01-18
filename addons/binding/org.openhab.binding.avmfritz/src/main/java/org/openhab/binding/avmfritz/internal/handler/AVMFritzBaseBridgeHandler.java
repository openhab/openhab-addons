/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.thing.CommonTriggerEvents.PRESSED;
import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.avmfritz.internal.BindingConstants.*;
import static org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel.ETSUnitInfoModel.*;
import static org.openhab.binding.avmfritz.internal.ahamodel.HeatingModel.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicStateDescriptionProvider;
import org.openhab.binding.avmfritz.internal.BindingConstants;
import org.openhab.binding.avmfritz.internal.ahamodel.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.ahamodel.AlertModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.GroupModel;
import org.openhab.binding.avmfritz.internal.ahamodel.SwitchModel;
import org.openhab.binding.avmfritz.internal.ahamodel.templates.TemplateModel;
import org.openhab.binding.avmfritz.internal.config.AVMFritzConfiguration;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaApplyTemplateCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateCallback;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateTemplatesCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for a FRITZ! bridge. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
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
    private @Nullable FritzAhaWebInterface connection;
    /**
     * Schedule for polling
     */
    private @Nullable ScheduledFuture<?> pollingJob;
    /**
     * shared instance of HTTP client for asynchronous calls
     */
    private final HttpClient httpClient;

    private final AVMFritzDynamicStateDescriptionProvider stateDescriptionProvider;

    /**
     * keeps track of the {@link ChannelUID} for the 'apply_tamplate' {@link Channel}
     */
    private final ChannelUID applyTemplateChannelUID;

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public AVMFritzBaseBridgeHandler(Bridge bridge, HttpClient httpClient,
            AVMFritzDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;

        applyTemplateChannelUID = new ChannelUID(bridge.getUID(), CHANNEL_APPLY_TEMPLATE);
    }

    /**
     * Initializes the bridge.
     */
    @Override
    public void initialize() {
        logger.debug("About to initialize FRITZ!Box {}", BRIDGE_FRITZBOX);
        AVMFritzConfiguration config = getConfigAs(AVMFritzConfiguration.class);

        logger.debug("Discovered FRITZ!Box initialized: {}", config);

        this.refreshInterval = config.getPollingInterval();
        this.connection = new FritzAhaWebInterface(config, this, httpClient);
        if (config.getPassword() != null) {
            stopPolling();
            startPolling();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no password set");
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopPolling();
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at interval {}s", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY, refreshInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the polling.
     */
    private void stopPolling() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Polls the bridge.
     */
    private void poll() {
        FritzAhaWebInterface webInterface = getWebInterface();
        if (webInterface != null) {
            logger.debug("Poll FRITZ!Box for updates {}", getThing().getUID());
            FritzAhaUpdateCallback updateCallback = new FritzAhaUpdateCallback(webInterface, this);
            webInterface.asyncGet(updateCallback);
            if (isLinked(applyTemplateChannelUID)) {
                logger.debug("Poll FRITZ!Box for templates {}", getThing().getUID());
                FritzAhaUpdateTemplatesCallback templateCallback = new FritzAhaUpdateTemplatesCallback(webInterface,
                        this);
                webInterface.asyncGet(templateCallback);
            }
        }
    }

    /**
     * Called from {@link FritzAhaWebInterface#authenticate()} to update the bridge status because updateStatus is
     * protected.
     *
     * @param status Bridge status
     * @param statusDetail Bridge status detail
     * @param description Bridge status description
     */
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatus(status, statusDetail, description);
    }

    /**
     * Called from {@link FritzAhaApplyTemplateCallback} to provide new templates for things.
     *
     * @param templateList list of template models
     */
    public void addTemplateList(List<TemplateModel> templateList) {
        List<StateOption> options = new ArrayList<>();
        for (TemplateModel template : templateList) {
            logger.debug("Process template model: {}", template);
            options.add(new StateOption(template.getIdentifier(), template.getName()));
        }
        stateDescriptionProvider.setStateOptions(applyTemplateChannelUID, options);
    }

    /**
     * Called from {@link FritzAhaUpdateCallback} to provide new values for things.
     *
     * @param deviceList list of device models
     */
    public void addDeviceList(List<AVMFritzBaseModel> deviceList) {
        for (Thing thing : getThing().getThings()) {
            AVMFritzBaseThingHandler handler = (AVMFritzBaseThingHandler) thing.getHandler();
            if (handler != null) {
                Optional<AVMFritzBaseModel> optionalDevice = deviceList.stream()
                        .filter(it -> it.getIdentifier().equals(handler.getIdentifier())).findFirst();
                if (optionalDevice.isPresent()) {
                    AVMFritzBaseModel device = optionalDevice.get();
                    logger.debug("update thing '{}' with device model: {}", thing.getUID(), device);
                    handler.setState(device);
                    if (device.getPresent() == 1) {
                        handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
                        updateThingFromDevice(thing, device);
                    } else {
                        handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device not present");
                    }
                } else {
                    handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                            "Device not present in response");
                }
            } else {
                logger.debug("handler missing for thing '{}'", thing.getUID());
            }
        }
    }

    /**
     * Updates thing from device model.
     *
     * @param thing Thing to be updated.
     * @param device Device model with new data.
     */
    protected void updateThingFromDevice(Thing thing, AVMFritzBaseModel device) {
        thing.setProperty(PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
        if (device instanceof GroupModel && ((GroupModel) device).getGroupinfo() != null) {
            thing.setProperty(PROPERTY_MASTER, ((GroupModel) device).getGroupinfo().getMasterdeviceid());
            thing.setProperty(PROPERTY_MEMBERS, ((GroupModel) device).getGroupinfo().getMembers());
        }
        if (device instanceof DeviceModel && device.isTempSensor() && ((DeviceModel) device).getTemperature() != null) {
            updateThingChannelState(thing, CHANNEL_TEMPERATURE,
                    new QuantityType<>(((DeviceModel) device).getTemperature().getCelsius(), CELSIUS));
            updateThingChannelConfiguration(thing, CHANNEL_TEMPERATURE, CONFIG_CHANNEL_TEMP_OFFSET,
                    ((DeviceModel) device).getTemperature().getOffset());
        }
        if (device.isPowermeter() && device.getPowermeter() != null) {
            updateThingChannelState(thing, CHANNEL_ENERGY,
                    new QuantityType<>(device.getPowermeter().getEnergy(), SmartHomeUnits.WATT_HOUR));
            updateThingChannelState(thing, CHANNEL_POWER,
                    new QuantityType<>(device.getPowermeter().getPower(), SmartHomeUnits.WATT));
            updateThingChannelState(thing, CHANNEL_VOLTAGE,
                    new QuantityType<>(device.getPowermeter().getVoltage(), SmartHomeUnits.VOLT));
        }
        if (device.isSwitchableOutlet() && device.getSwitch() != null) {
            updateThingChannelState(thing, CHANNEL_MODE, new StringType(device.getSwitch().getMode()));
            updateThingChannelState(thing, CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(device.getSwitch().getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(thing, CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(device.getSwitch().getDevicelock()) ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            if (device.getSwitch().getState() == null) {
                updateThingChannelState(thing, CHANNEL_OUTLET, UnDefType.UNDEF);
            } else {
                updateThingChannelState(thing, CHANNEL_OUTLET,
                        SwitchModel.ON.equals(device.getSwitch().getState()) ? OnOffType.ON : OnOffType.OFF);
            }
        }
        if (device.isHeatingThermostat() && device.getHkr() != null) {
            updateThingChannelState(thing, CHANNEL_MODE, new StringType(device.getHkr().getMode()));
            updateThingChannelState(thing, CHANNEL_LOCKED,
                    BigDecimal.ZERO.equals(device.getHkr().getLock()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateThingChannelState(thing, CHANNEL_DEVICE_LOCKED,
                    BigDecimal.ZERO.equals(device.getHkr().getDevicelock()) ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            updateThingChannelState(thing, CHANNEL_ACTUALTEMP,
                    new QuantityType<>(toCelsius(device.getHkr().getTist()), CELSIUS));
            updateThingChannelState(thing, CHANNEL_SETTEMP,
                    new QuantityType<>(toCelsius(device.getHkr().getTsoll()), CELSIUS));
            updateThingChannelState(thing, CHANNEL_ECOTEMP,
                    new QuantityType<>(toCelsius(device.getHkr().getAbsenk()), CELSIUS));
            updateThingChannelState(thing, CHANNEL_COMFORTTEMP,
                    new QuantityType<>(toCelsius(device.getHkr().getKomfort()), CELSIUS));
            updateThingChannelState(thing, CHANNEL_RADIATOR_MODE, new StringType(device.getHkr().getRadiatorMode()));
            if (device.getHkr().getNextchange() != null) {
                if (device.getHkr().getNextchange().getEndperiod() == 0) {
                    updateThingChannelState(thing, CHANNEL_NEXT_CHANGE, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(thing, CHANNEL_NEXT_CHANGE,
                            new DateTimeType(ZonedDateTime.ofInstant(
                                    Instant.ofEpochSecond(device.getHkr().getNextchange().getEndperiod()),
                                    ZoneId.systemDefault())));
                }
                if (TEMP_FRITZ_UNDEFINED.equals(device.getHkr().getNextchange().getTchange())) {
                    updateThingChannelState(thing, CHANNEL_NEXTTEMP, UnDefType.UNDEF);
                } else {
                    updateThingChannelState(thing, CHANNEL_NEXTTEMP,
                            new QuantityType<>(toCelsius(device.getHkr().getNextchange().getTchange()), CELSIUS));
                }
            }
            if (device.getHkr().getBattery() == null) {
                updateThingChannelState(thing, CHANNEL_BATTERY, UnDefType.UNDEF);
            } else {
                updateThingChannelState(thing, CHANNEL_BATTERY, new DecimalType(device.getHkr().getBattery()));
            }
            if (device.getHkr().getBatterylow() == null) {
                updateThingChannelState(thing, CHANNEL_BATTERY_LOW, UnDefType.UNDEF);
            } else {
                updateThingChannelState(thing, CHANNEL_BATTERY_LOW,
                        BATTERY_ON.equals(device.getHkr().getBatterylow()) ? OnOffType.ON : OnOffType.OFF);
            }
        }
        if (device instanceof DeviceModel && device.isAlarmSensor() && ((DeviceModel) device).getAlert() != null) {
            updateThingChannelState(thing, CHANNEL_CONTACT_STATE,
                    AlertModel.ON.equals(((DeviceModel) device).getAlert().getState()) ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
        }
        if (device instanceof DeviceModel && device.isButton() && ((DeviceModel) device).getButton() != null) {
            if (((DeviceModel) device).getButton().getLastpressedtimestamp() == 0) {
                updateThingChannelState(thing, CHANNEL_LAST_CHANGE, UnDefType.UNDEF);
            } else {
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime timestamp = ZonedDateTime.ofInstant(
                        Instant.ofEpochSecond(((DeviceModel) device).getButton().getLastpressedtimestamp()), zoneId);
                Instant then = timestamp.toInstant();
                ZonedDateTime now = ZonedDateTime.now(zoneId);
                Instant someSecondsEarlier = now.minusSeconds(refreshInterval).toInstant();
                if (then.isAfter(someSecondsEarlier) && then.isBefore(now.toInstant())) {
                    triggerThingChannel(thing, CHANNEL_PRESS, PRESSED);
                }
                updateThingChannelState(thing, CHANNEL_LAST_CHANGE, new DateTimeType(timestamp));
            }
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
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state);
        } else {
            logger.debug("Channel '{}' in thing '{}' does not exist, recreating thing.", channelId, thing.getUID());
            AVMFritzBaseThingHandler handler = (AVMFritzBaseThingHandler) thing.getHandler();
            if (handler != null) {
                handler.createChannel(channelId);
            }
        }
    }

    /**
     * Triggers thing channels.
     *
     * @param thing Thing which channels should be triggered.
     * @param channelId ID of the channel to be triggered.
     * @param event Event to emit
     */
    private void triggerThingChannel(Thing thing, String channelId, String event) {
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
     * @param thing Thing which channel configuration should be updated.
     * @param channelId ID of the channel which configuration to be updated.
     * @param configId ID of the configuration to be updated.
     * @param value Value to be set.
     */
    private void updateThingChannelConfiguration(Thing thing, String channelId, String configId, Object value) {
        Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            Configuration config = channel.getConfiguration();
            Configuration editConfig = new Configuration(new HashMap<>(config.getProperties()));
            editConfig.put(configId, value);
            config.setProperties(editConfig.getProperties());
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
        } else if (device instanceof DeviceModel && device.isHANFUNUnit()) {
            List<String> interfaces = Arrays
                    .asList(((DeviceModel) device).getEtsiunitinfo().getInterfaces().split(","));
            if (interfaces.contains(HAN_FUN_INTERFACE_ALERT)) {
                return DEVICE_HAN_FUN_CONTACT;
            } else if (interfaces.contains(HAN_FUN_INTERFACE_SIMPLE_BUTTON)) {
                return DEVICE_HAN_FUN_SWITCH;
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        logger.debug("Handle command '{}' for channel {}", command, channelId);
        if (command == RefreshType.REFRESH) {
            handleRefreshCommand();
        }
        FritzAhaWebInterface fritzBox = getWebInterface();
        if (fritzBox == null) {
            logger.debug("Cannot handle command '{}' because connection is missing", command);
            return;
        }
        if (CHANNEL_APPLY_TEMPLATE.equals(channelId)) {
            if (command instanceof StringType) {
                fritzBox.applyTemplate(command.toString());
            }
            updateState(CHANNEL_APPLY_TEMPLATE, UnDefType.UNDEF);
        }
    }

    public void handleRefreshCommand() {
        scheduler.submit(this::poll);
    }
}
