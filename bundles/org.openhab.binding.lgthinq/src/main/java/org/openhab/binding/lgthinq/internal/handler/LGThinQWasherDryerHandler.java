/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.LGThinQWMApiClientService;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseDefinition;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseFunction;
import org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers.CourseType;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQWasherDryerHandler} Handle Washer/Dryer And Washer Dryer Towers things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQWasherDryerHandler
        extends LGThinQAbstractDeviceHandler<WasherDryerCapability, WasherDryerSnapshot> {

    public final ChannelGroupUID channelGroupRemoteStartUID;
    public final ChannelGroupUID channelGroupDashboardUID;
    private final LGThinQStateDescriptionProvider stateDescriptionProvider;
    private final ChannelUID courseChannelUID;
    private final ChannelUID remoteStartStopChannelUID;
    private final ChannelUID remainTimeChannelUID;
    private final ChannelUID delayTimeChannelUID;
    private final ChannelUID spinChannelUID;
    private final ChannelUID rinseChannelUID;
    private final ChannelUID stateChannelUID;
    private final ChannelUID processStateChannelUID;
    private final ChannelUID childLockChannelUID;
    private final ChannelUID dryLevelChannelUID;
    private final ChannelUID temperatureChannelUID;
    private final ChannelUID doorLockChannelUID;
    private final ChannelUID standByModeChannelUID;
    private final ChannelUID remoteStartFlagChannelUID;
    private final ChannelUID remoteStartCourseChannelUID;
    private final List<Channel> remoteStartEnabledChannels = new CopyOnWriteArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(LGThinQWasherDryerHandler.class);
    private final LGThinQWMApiClientService lgThinqWMApiClientService;

    public LGThinQWasherDryerHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry, HttpClientFactory httpClientFactory) {
        super(thing, stateDescriptionProvider, itemChannelLinkRegistry);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgThinqWMApiClientService = LGThinQApiClientServiceFactory.newWMApiClientService(lgPlatformType,
                httpClientFactory);
        channelGroupRemoteStartUID = new ChannelGroupUID(getThing().getUID(), CHANNEL_WMD_REMOTE_START_GRP_ID);
        channelGroupDashboardUID = new ChannelGroupUID(getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID);
        courseChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_COURSE_ID);
        dryLevelChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_DRY_LEVEL_ID);
        stateChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_STATE_ID);
        processStateChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_PROCESS_STATE_ID);
        remainTimeChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_REMAIN_TIME_ID);
        delayTimeChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_DELAY_TIME_ID);
        temperatureChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_TEMP_LEVEL_ID);
        doorLockChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_DOOR_LOCK_ID);
        childLockChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_CHILD_LOCK_ID);
        rinseChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_RINSE_ID);
        spinChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_SPIN_ID);
        standByModeChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_STAND_BY_ID);
        remoteStartFlagChannelUID = new ChannelUID(channelGroupDashboardUID, CHANNEL_WMD_REMOTE_START_ID);
        remoteStartStopChannelUID = new ChannelUID(channelGroupRemoteStartUID, CHANNEL_WMD_REMOTE_START_START_STOP);
        remoteStartCourseChannelUID = new ChannelUID(channelGroupRemoteStartUID, CHANNEL_WMD_REMOTE_COURSE);
    }

    @Override
    protected void initializeThing(@Nullable ThingStatus bridgeStatus) {
        super.initializeThing(bridgeStatus);
        ThingBuilder builder = editThing()
                .withoutChannels(this.getThing().getChannelsOfGroup(channelGroupRemoteStartUID.getId()));
        updateThing(builder.build());
        remoteStartEnabledChannels.clear();
    }

    private void loadOptionsCourse(WasherDryerCapability cap, ChannelUID courseChannel) {
        List<StateOption> optionsCourses = new ArrayList<>();
        cap.getCourses().forEach((k, v) -> optionsCourses.add(new StateOption(k, emptyIfNull(v.getCourseName()))));
        stateDescriptionProvider.setStateOptions(courseChannel, optionsCourses);
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        WasherDryerCapability wmCap = getCapabilities();

        List<StateOption> options = new ArrayList<>();
        wmCap.getStateFeat().getValuesMapping()
                .forEach((k, v) -> options.add(new StateOption(k, keyIfValueNotFound(CAP_WMD_STATE, v))));
        stateDescriptionProvider.setStateOptions(stateChannelUID, options);

        loadOptionsCourse(wmCap, courseChannelUID);

        List<StateOption> optionsTemp = new ArrayList<>();
        wmCap.getTemperatureFeat().getValuesMapping()
                .forEach((k, v) -> optionsTemp.add(new StateOption(k, keyIfValueNotFound(CAP_WMD_TEMPERATURE, v))));
        stateDescriptionProvider.setStateOptions(temperatureChannelUID, optionsTemp);

        List<StateOption> optionsDoor = new ArrayList<>();
        optionsDoor.add(new StateOption("0", "Unlocked"));
        optionsDoor.add(new StateOption("1", "Locked"));
        stateDescriptionProvider.setStateOptions(doorLockChannelUID, optionsDoor);

        List<StateOption> optionsSpin = new ArrayList<>();
        wmCap.getSpinFeat().getValuesMapping()
                .forEach((k, v) -> optionsSpin.add(new StateOption(k, keyIfValueNotFound(CAP_WM_SPIN, v))));
        stateDescriptionProvider.setStateOptions(spinChannelUID, optionsSpin);

        List<StateOption> optionsRinse = new ArrayList<>();
        wmCap.getRinseFeat().getValuesMapping()
                .forEach((k, v) -> optionsRinse.add(new StateOption(k, keyIfValueNotFound(CAP_WM_RINSE, v))));
        stateDescriptionProvider.setStateOptions(rinseChannelUID, optionsRinse);

        List<StateOption> optionsPre = new ArrayList<>();
        wmCap.getProcessState().getValuesMapping()
                .forEach((k, v) -> optionsPre.add(new StateOption(k, keyIfValueNotFound(CAP_WMD_PROCESS_STATE, v))));
        stateDescriptionProvider.setStateOptions(processStateChannelUID, optionsPre);

        List<StateOption> optionsChildLock = new ArrayList<>();
        optionsChildLock.add(new StateOption("CHILDLOCK_OFF", "Unlocked"));
        optionsChildLock.add(new StateOption("CHILDLOCK_ON", "Locked"));
        stateDescriptionProvider.setStateOptions(childLockChannelUID, optionsChildLock);

        List<StateOption> optionsDryLevel = new ArrayList<>();
        wmCap.getDryLevel().getValuesMapping()
                .forEach((k, v) -> optionsDryLevel.add(new StateOption(k, keyIfValueNotFound(CAP_DR_DRY_LEVEL, v))));
        stateDescriptionProvider.setStateOptions(dryLevelChannelUID, optionsDryLevel);
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
    protected void updateDeviceChannels(WasherDryerSnapshot shot) throws LGThinqApiException {
        updateState(CHANNEL_DASHBOARD_GRP_WITH_SEP + CHANNEL_AC_POWER_ID,
                OnOffType.from(DevicePowerState.DV_POWER_ON == shot.getPowerStatus()));
        updateState(stateChannelUID, new StringType(shot.getState()));
        updateState(processStateChannelUID, new StringType(shot.getProcessState()));
        updateState(dryLevelChannelUID, new StringType(shot.getDryLevel()));
        updateState(childLockChannelUID, new StringType(shot.getChildLock()));
        updateState(courseChannelUID, new StringType(shot.getCourse()));
        updateState(temperatureChannelUID, new StringType(shot.getTemperatureLevel()));
        updateState(doorLockChannelUID, new StringType(shot.getDoorLock()));
        updateState(remainTimeChannelUID, new StringType(shot.getRemainingTime()));
        updateState(delayTimeChannelUID, new StringType(shot.getReserveTime()));
        updateState(standByModeChannelUID, OnOffType.from(shot.isStandBy()));
        updateState(remoteStartFlagChannelUID, OnOffType.from(shot.isRemoteStartEnabled()));
        updateState(spinChannelUID, new StringType(shot.getSpin()));
        updateState(rinseChannelUID, new StringType(shot.getRinse()));
        Channel rsStartStopChannel = getThing().getChannel(remoteStartStopChannelUID);
        final List<Channel> dynChannels = new ArrayList<>();
        // only can have remote start channel is the WM is not in sleep mode, and remote start is enabled.
        if (shot.isRemoteStartEnabled() && !shot.isStandBy()) {
            ThingHandlerCallback callback = getCallback();
            if (rsStartStopChannel == null && callback != null) {
                // === creating channel LaunchRemote
                dynChannels.add(
                        createDynChannel(CHANNEL_WMD_REMOTE_START_START_STOP, remoteStartStopChannelUID, "Switch"));
                dynChannels.add(createDynChannel(CHANNEL_WMD_REMOTE_COURSE, remoteStartCourseChannelUID, "String"));
                // Just enabled remote start. Then is Off
                updateState(remoteStartStopChannelUID, OnOffType.OFF);
                // === creating selectable channels for the Course (if any)
                WasherDryerCapability cap = getCapabilities();
                loadOptionsCourse(cap, remoteStartCourseChannelUID);
                updateState(remoteStartCourseChannelUID, new StringType(cap.getDefaultCourseId()));

                CourseDefinition courseDef = cap.getCourses().get(cap.getDefaultCourseId());
                if (WMD_COURSE_NOT_SELECTED_VALUE.equals(shot.getSmartCourse()) && courseDef != null) {
                    // only create selectable channels if the course is not a smart course. Smart courses have
                    // already predefined
                    // the functions values
                    for (CourseFunction f : courseDef.getFunctions()) {
                        if (!f.isSelectable()) {
                            // only for selectable features
                            continue;
                        }
                        // handle well know dynamic fields
                        FeatureDefinition fd = cap.getFeatureDefinition(f.getValue());
                        ChannelUID targetChannel;
                        ChannelUID refChannel;
                        if (!FeatureDefinition.NULL_DEFINITION.equals(fd)) {
                            targetChannel = new ChannelUID(channelGroupRemoteStartUID, fd.getChannelId());
                            refChannel = new ChannelUID(channelGroupDashboardUID, fd.getRefChannelId());
                            dynChannels.add(createDynChannel(fd.getChannelId(), targetChannel,
                                    translateFeatureToItemType(fd.getDataType())));
                            if (CAP_WM_DICT_V2.containsKey(f.getValue())) {
                                // if the function has translation dictionary (I hope so), then the values in
                                // the selectable channel will be translated to something more readable
                                List<StateOption> options = new ArrayList<>();
                                for (String v : f.getSelectableValues()) {
                                    Map<String, String> values = CAP_WM_DICT_V2.get(f.getValue());
                                    if (values != null) {
                                        // Canonical Value is the KEY (@...) that represents a constant in the
                                        // definition
                                        // that can be translated to a human description
                                        String canonicalValue = Objects.requireNonNullElse(fd.getValuesMapping().get(v),
                                                v);
                                        options.add(new StateOption(v, keyIfValueNotFound(values, canonicalValue)));
                                        stateDescriptionProvider.setStateOptions(targetChannel, options);
                                    }
                                }
                            }
                            // update state with the default referenced channel
                            updateState(targetChannel, new StringType(getItemLinkedValue(refChannel)));
                        }
                    }
                }

                remoteStartEnabledChannels.addAll(dynChannels);
            }
        } else if (!remoteStartEnabledChannels.isEmpty()) {
            ThingBuilder builder = editThing().withoutChannels(remoteStartEnabledChannels);
            updateThing(builder.build());
            remoteStartEnabledChannels.clear();
        }
    }

    @Override
    protected DeviceTypes getDeviceType() {
        if (THING_TYPE_WASHING_MACHINE.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.WASHERDRYER_MACHINE;
        } else if (THING_TYPE_WASHING_TOWER.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.WASHER_TOWER;
        } else if (THING_TYPE_DRYER.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.WASHER_TOWER;
        } else {
            throw new IllegalArgumentException(
                    "DeviceTypeUuid [" + getThing().getThingTypeUID() + "] not expected for WashingTower/Machine");
        }
    }

    private Map<String, Object> getRemoteStartData() throws LGThinqApiException {
        WasherDryerSnapshot lastShot = getLastShot();
        if (lastShot.getRawData().isEmpty()) {
            return lastShot.getRawData();
        }
        String selectedCourse = getItemLinkedValue(remoteStartCourseChannelUID);
        if (selectedCourse == null) {
            logger.warn("Remote Start Channel must be linked to proceed with remote start.");
            return Collections.emptyMap();
        }
        WasherDryerCapability cap = getCapabilities();
        Map<String, Object> rawData = lastShot.getRawData();
        Map<String, Object> data = new HashMap<>();
        CommandDefinition cmd = cap.getCommandsDefinition().get(cap.getCommandRemoteStart());
        if (cmd == null) {
            logger.error("Command for Remote Start not found in the Washer descriptor. It's most likely a bug");
            return Collections.emptyMap();
        }
        Map<String, Object> cmdData = cmd.getData();
        // 1st - copy snapshot data to command
        cmdData.forEach((k, v) -> {
            data.put(k, rawData.getOrDefault(k, v));
        });
        // 2nd - replace remote start data with selected course values
        CourseDefinition selCourseDef = cap.getCourses().get(selectedCourse);
        if (selCourseDef != null) {
            selCourseDef.getFunctions().forEach(f -> {
                data.put(f.getValue(), f.getDefaultValue());
            });
        }
        String smartCourse = lastShot.getSmartCourse();
        data.put(cap.getDefaultCourseFieldName(), selectedCourse);
        data.put(cap.getDefaultSmartCourseFeatName(), smartCourse);
        CourseType courseType = Objects
                .requireNonNull(cap.getCourses().get("NOT_SELECTED".equals(smartCourse) ? selectedCourse : smartCourse),
                        "NOT_SELECTED should be hardcoded. It is most likely a bug")
                .getCourseType();
        data.put("courseType", courseType.getValue());
        // 3rd - replace custom selectable features with channel's ones.
        for (Channel c : remoteStartEnabledChannels) {
            String value = Objects.requireNonNullElse(getItemLinkedValue(c.getUID()), "");
            String simpleChannelUID = getSimpleChannelUID(c.getUID().getId());
            switch (simpleChannelUID) {
                case CHANNEL_WMD_REMOTE_START_RINSE:
                    data.put(cap.getRinseFeat().getName(), value);
                    break;
                case CHANNEL_WMD_REMOTE_START_TEMP:
                    data.put(cap.getTemperatureFeat().getName(), value);
                    break;
                case CHANNEL_WMD_REMOTE_START_SPIN:
                    data.put(cap.getSpinFeat().getName(), value);
                    break;
                default:
                    logger.warn("channel [{}] not mapped for this binding. It is most likely a bug.", simpleChannelUID);
            }
        }

        return data;
    }

    @Override
    protected void processCommand(LGThinQAbstractDeviceHandler.AsyncCommandParams params) throws LGThinqApiException {
        WasherDryerSnapshot lastShot = getLastShot();
        Command command = params.command;
        String simpleChannelUID;
        simpleChannelUID = getSimpleChannelUID(params.channelUID);
        switch (simpleChannelUID) {
            case CHANNEL_WMD_REMOTE_START_START_STOP: {
                if (command instanceof OnOffType ooCmd) {
                    if (ooCmd == OnOffType.ON) {
                        if (!lastShot.isStandBy()) {
                            lgThinqWMApiClientService.remoteStart(getBridgeId(), getCapabilities(), getDeviceId(),
                                    getRemoteStartData());
                        } else {
                            logger.warn(
                                    "WM is in StandBy mode. Command START can't be sent to Remote Start channel. Ignoring");
                        }
                    } else {
                        logger.warn("Command Remote Start OFF not implemented yet");
                    }
                } else {
                    logger.warn("Received command different of StringType in Remote Start Channel. Ignoring");
                }
                break;
            }
            case CHANNEL_WMD_STAND_BY_ID: {
                if (command instanceof OnOffType ooCmd) {
                    lgThinqWMApiClientService.wakeUp(getBridgeId(), getDeviceId(), ooCmd == OnOffType.ON);
                } else {
                    logger.warn("Received command different of OnOffType in StandBy Channel. Ignoring");
                }
                break;
            }
            default: {
                logger.warn("Command {} to the channel {} not supported. Ignored.", command, params.channelUID);
            }
        }
    }

    @Override
    public String getDeviceAlias() {
        return emptyIfNull(getThing().getProperties().get(PROP_INFO_DEVICE_ALIAS));
    }

    @Override
    public String getDeviceUriJsonConfig() {
        return emptyIfNull(getThing().getProperties().get(PROP_INFO_MODEL_URL_INFO));
    }

    @Override
    public void onDeviceRemoved() {
    }

    /**
     * Put the channels in default state if the device is disconnected or gone.
     */
    @Override
    public void onDeviceDisconnected() {
        updateState(CHANNEL_AC_POWER_ID, OnOffType.OFF);
        updateState(CHANNEL_WMD_STATE_ID, new StringType(WMD_POWER_OFF_VALUE));
        updateState(CHANNEL_WMD_COURSE_ID, new StringType("NOT_SELECTED"));
        updateState(CHANNEL_WMD_SMART_COURSE_ID, new StringType("NOT_SELECTED"));
        updateState(CHANNEL_WMD_TEMP_LEVEL_ID, new StringType("NOT_SELECTED"));
        updateState(CHANNEL_WMD_DOOR_LOCK_ID, new StringType("DOOR_LOCK_OFF"));
        updateState(CHANNEL_WMD_REMAIN_TIME_ID, new StringType("00:00"));
    }
}
