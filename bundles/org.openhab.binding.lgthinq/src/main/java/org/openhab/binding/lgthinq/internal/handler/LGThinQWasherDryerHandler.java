/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.internal.handler;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQDeviceDynStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.*;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDataType;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.CourseType;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQWasherDryerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQWasherDryerHandler
        extends LGThinQAbstractDeviceHandler<WasherDryerCapability, WasherDryerSnapshot> {

    private final LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider;
    private final ChannelUID courseChannelUUID;
    private final ChannelUID remoteStartChannelUUID;
    private final ChannelUID launchRemoteStartUUID;
    private final ChannelUID spinChannelUUID;
    private final ChannelUID rinseChannelUUID;
    private final ChannelUID standbyChannelUUID;
    private final ChannelUID stateChannelUUID;
    private final ChannelUID temperatureChannelUUID;
    private final ChannelUID doorLockChannelUUID;

    private final Map<String, ChannelUID> featureChannels = new HashMap<>();
    @Nullable
    private WasherDryerSnapshot lastShot;

    private final Logger logger = LoggerFactory.getLogger(LGThinQWasherDryerHandler.class);
    @NonNullByDefault
    private final LGThinQWMApiClientService lgThinqWMApiClientService;

    // *** Long running isolated threadpools.
    private final ScheduledExecutorService pollingScheduler = Executors.newScheduledThreadPool(1);

    private final LinkedBlockingQueue<AsyncCommandParams> commandBlockQueue = new LinkedBlockingQueue<>(20);

    public LGThinQWasherDryerHandler(Thing thing, LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        super(thing, stateDescriptionProvider);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgThinqWMApiClientService = lgPlatformType.equals(PLATFORM_TYPE_V1)
                ? LGThinQWMApiV1ClientServiceImpl.getInstance()
                : LGThinQWMApiV2ClientServiceImpl.getInstance();
        courseChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_COURSE_ID);
        remoteStartChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_REMOTE_START_ID);
        standbyChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_STAND_BY_ID);
        stateChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_STATE_ID);
        temperatureChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_TEMP_LEVEL_ID);
        doorLockChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_DOOR_LOCK_ID);
        rinseChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_RINSE_ID);
        spinChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_SPIN_ID);
        launchRemoteStartUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_LAUNCH_REMOTE_START_ID);
    }

    static class AsyncCommandParams {
        final String channelUID;
        final Command command;

        public AsyncCommandParams(String channelUUID, Command command) {
            this.channelUID = channelUUID;
            this.command = command;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thinq thing. Washer/Dryer Thing v0.1");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    private String getItemTypeFromFeatureType(FeatureDataType dataType) {
        switch (dataType) {
            case RANGE:
            case BIT:
                return "Number";
            case BOOLEAN:
                return "Switch";
            case ENUM:
            default:
                return "String";
        }
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        WasherDryerCapability wmCap = getCapabilities();

        // if (featureChannels.size() == 0) {
        // // create dyn channels for the device features
        // wmCap.getFeatures().forEach((channelName, featureDef) -> {
        // createDynChannel(channelName, new ChannelUID(getThing().getUID(), channelName),
        // getItemTypeFromFeatureType(featureDef.getDataType()));
        // });
        // }
        if (isLinked(stateChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getState().getValuesMapping()
                    .forEach((k, v) -> options.add(new StateOption(k, keyIfValueNotFound(CAP_WP_STATE, v))));
            stateDescriptionProvider.setStateOptions(stateChannelUUID, options);
        }
        if (isLinked(courseChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getCourses().forEach((k, v) -> options.add(new StateOption(k, emptyIfNull(v.getCourseName()))));
            stateDescriptionProvider.setStateOptions(courseChannelUUID, options);
        }

        if (isLinked(temperatureChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getTemperature().getValuesMapping()
                    .forEach((k, v) -> options.add(new StateOption(k, keyIfValueNotFound(CAP_WP_TEMPERATURE, v))));
            stateDescriptionProvider.setStateOptions(temperatureChannelUUID, options);
        }
        if (isLinked(doorLockChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            options.add(new StateOption("0", "Unlocked"));
            options.add(new StateOption("1", "Locked"));
            stateDescriptionProvider.setStateOptions(doorLockChannelUUID, options);
        }
        if (isLinked(spinChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getSpin().getValuesMapping()
                    .forEach((k, v) -> options.add(new StateOption(k, keyIfValueNotFound(CAP_WP_SPIN, v))));
            stateDescriptionProvider.setStateOptions(spinChannelUUID, options);
        }
        if (isLinked(rinseChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getRinse().getValuesMapping()
                    .forEach((k, v) -> options.add(new StateOption(k, keyIfValueNotFound(CAP_WP_RINSE, v))));
            stateDescriptionProvider.setStateOptions(rinseChannelUUID, options);
        }

        // ======================================================

        if (getThing().getChannel(standbyChannelUUID) == null) {
            createDynChannel(WM_CHANNEL_STAND_BY_ID, standbyChannelUUID, "Switch");
        }
        if (getThing().getChannel(remoteStartChannelUUID) == null) {
            createDynChannel(WM_CHANNEL_REMOTE_START_ID, remoteStartChannelUUID, "Switch");
        }
    }

    @Override
    public LGThinQApiClientService<WasherDryerCapability, WasherDryerSnapshot> getLgThinQAPIClientService() {
        return lgThinqWMApiClientService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void updateDeviceChannels(WasherDryerSnapshot shot) {
        lastShot = shot;
        updateState(CHANNEL_POWER_ID,
                (DevicePowerState.DV_POWER_ON.equals(shot.getPowerStatus()) ? OnOffType.ON : OnOffType.OFF));
        updateState(WM_CHANNEL_STATE_ID, new StringType(shot.getState()));
        updateState(WM_CHANNEL_COURSE_ID, new StringType(shot.getCourse()));
        updateState(WM_CHANNEL_SMART_COURSE_ID, new StringType(shot.getSmartCourse()));
        updateState(WM_CHANNEL_TEMP_LEVEL_ID, new StringType(shot.getTemperatureLevel()));
        updateState(WM_CHANNEL_DOOR_LOCK_ID, new StringType(shot.getDoorLock()));
        updateState(WM_CHANNEL_REMAIN_TIME_ID, new DateTimeType(shot.getRemainingTime()));
        updateState(WM_CHANNEL_DELAY_TIME_ID, new DateTimeType(shot.getReserveTime()));
        updateState(WM_CHANNEL_DOWNLOADED_COURSE_ID, new StringType(shot.getDownloadedCourse()));
        updateState(WM_CHANNEL_STAND_BY_ID, shot.isStandBy() ? OnOffType.ON : OnOffType.OFF);
        updateState(WM_CHANNEL_REMOTE_START_ID, shot.isRemoteStartEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(WM_CHANNEL_SPIN_ID, new StringType(shot.getSpin()));
        updateState(WM_CHANNEL_RINSE_ID, new StringType(shot.getRinse()));
        Channel launchRemoteStart = getThing().getChannel(launchRemoteStartUUID);
        // only can have remote start channel is the WM is not in sleep mode, and remote start is enabled.
        if (shot.isRemoteStartEnabled() && !shot.isStandBy()) {
            ThingHandlerCallback callback = getCallback();
            if (launchRemoteStart == null && callback != null && this.thinqChannelProvider != null) {
                ChannelTypeUID channelTypeUid = createDynTypeChannel(this.thinqChannelProvider,
                        WM_CHANNEL_LAUNCH_REMOTE_START_ID, "Launch Remote", "String", false);
                ChannelBuilder builder = getCallback().createChannelBuilder(launchRemoteStartUUID, channelTypeUid);
                Channel newChannel = builder.build();
                ThingBuilder thingBuilder = editThing();
                updateThing(thingBuilder.withChannel(newChannel).build());
            }
            if (isLinked(launchRemoteStartUUID)) {
                updateState(WM_CHANNEL_LAUNCH_REMOTE_START_ID, new StringType(""));
            }
        } else {
            if (launchRemoteStart != null) {
                ThingBuilder builder = editThing().withoutChannels(launchRemoteStart);
                updateThing(builder.build());
            }
        }
    }

    @Override
    protected DeviceTypes getDeviceType() {
        if (THING_TYPE_WASHING_MACHINE.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.WASHERDRYER_MACHINE;
        } else if (THING_TYPE_WASHING_TOWER.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.WASHING_TOWER;
        } else {
            throw new IllegalArgumentException(
                    "DeviceTypeUuid [" + getThing().getThingTypeUID() + "] not expected for WashingTower/Machine");
        }
    }

    private Map<String, Object> getRemoteStartData() throws LGThinqApiException {
        if (lastShot == null) {
            return Collections.EMPTY_MAP;
        }
        WasherDryerCapability cap = getCapabilities();
        Map<String, Object> rawData = lastShot.getRawData();
        Map<String, Object> data = new HashMap<>();
        CommandDefinition cmd = cap.getCommandsDefinition().get(cap.getCommandRemoteStart());
        if (cmd == null) {
            logger.error("Command for Remote Start not found in the Washer descriptor. It's most likely a bug");
            return Collections.EMPTY_MAP;
        }
        Map<String, Object> cmdData = cmd.getData();
        cmdData.forEach((k, v) -> {
            data.put(k, rawData.getOrDefault(k, v));
        });
        String course = lastShot.getCourse();
        String smartCourse = lastShot.getSmartCourse();
        data.put(cap.getDefaultCourseFieldName(), course);
        data.put(cap.getDefaultSmartCourseFieldName(), smartCourse);
        CourseType courseType = cap.getCourses().get("NOT_SELECTED".equals(smartCourse) ? course : smartCourse)
                .getCourseType();
        data.put("courseType", courseType.getValue());
        return data;
    }

    @Override
    protected void processCommand(LGThinQAbstractDeviceHandler.AsyncCommandParams params) throws LGThinqApiException {
        Command command = params.command;
        switch (params.channelUID) {
            case WM_CHANNEL_LAUNCH_REMOTE_START_ID: {
                if (command instanceof StringType) {
                    if ("START".equalsIgnoreCase(command.toString())) {
                        if (lastShot != null && !lastShot.isStandBy()) {
                            lgThinqWMApiClientService.remoteStart(getBridgeId(), getCapabilities(), getDeviceId(),
                                    getRemoteStartData());
                        } else if (lastShot != null && lastShot.isStandBy()) {
                            logger.warn(
                                    "WM is in StandBy mode. Command START can't be sent to Remote Start channel. Ignoring");
                        }
                    } else {
                        logger.warn(
                                "Command [{}] sent to Remote Start channel is invalid. Only command START is valid.",
                                command);
                    }
                } else {
                    logger.warn("Received command different of StringType in Remote Start Channel. Ignoring");
                }
                break;
            }
            case WM_CHANNEL_STAND_BY_ID: {
                if (command instanceof OnOffType) {
                    if (lastShot == null || !lastShot.isStandBy()) {
                        logger.warn(
                                "Command {} was sent to StandBy channel, but the state of the WM is unknown or already waked up. Ignoring",
                                command);
                        break;
                    }
                    lgThinqWMApiClientService.wakeUp(getBridgeId(), getDeviceId(), OnOffType.ON.equals(command));
                } else {
                    logger.warn("Received command different of OnOffType in StandBy Channel. Ignoring");
                }
                break;
            }
            default: {
                logger.error("Command {} to the channel {} not supported. Ignored.", command, params.channelUID);
            }
        }
    }

    @Override
    public void onDeviceAdded(LGDevice device) {
        // TODO - handle it. Think if it's needed
    }

    @Override
    public String getDeviceAlias() {
        return emptyIfNull(getThing().getProperties().get(DEVICE_ALIAS));
    }

    @Override
    public String getDeviceUriJsonConfig() {
        return emptyIfNull(getThing().getProperties().get(MODEL_URL_INFO));
    }

    @Override
    public void onDeviceRemoved() {
        // TODO - HANDLE IT, Think if it's needed
    }

    /**
     * Put the channels in default state if the device is disconnected or gone.
     */
    @Override
    public void onDeviceDisconnected() {
        updateState(CHANNEL_POWER_ID, OnOffType.OFF);
        updateState(WM_CHANNEL_STATE_ID, new StringType(WM_POWER_OFF_VALUE));
        updateState(WM_CHANNEL_COURSE_ID, new StringType("NOT_SELECTED"));
        updateState(WM_CHANNEL_SMART_COURSE_ID, new StringType("NOT_SELECTED"));
        updateState(WM_CHANNEL_TEMP_LEVEL_ID, new StringType("NOT_SELECTED"));
        updateState(WM_CHANNEL_DOOR_LOCK_ID, new StringType("DOOR_LOCK_OFF"));
        updateState(WM_CHANNEL_REMAIN_TIME_ID, new StringType("00:00"));
        updateState(WM_CHANNEL_DOWNLOADED_COURSE_ID, new StringType("NOT_SELECTED"));
    }
}
