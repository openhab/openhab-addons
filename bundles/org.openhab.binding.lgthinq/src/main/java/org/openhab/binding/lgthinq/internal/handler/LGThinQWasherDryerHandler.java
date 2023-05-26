/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lgthinq.internal.LGThinQDeviceDynStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.type.ThinqChannelGroupTypeProvider;
import org.openhab.binding.lgthinq.internal.type.ThinqChannelTypeProvider;
import org.openhab.binding.lgthinq.lgservices.*;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;
import org.openhab.binding.lgthinq.lgservices.model.CommandDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.*;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
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

    public final ChannelGroupUID channelGroupRemoteStartUID;
    public final ChannelGroupUID channelGroupDashboardUID;

    private final List<Channel> remoteStartEnabledChannels = new CopyOnWriteArrayList<>();

    private final ItemChannelLinkRegistry itemChannelLinkRegistry;
    private final Map<String, Map<String, Object>> cachedBitKeyDefinitions = new HashMap<>();
    @Nullable
    private WasherDryerSnapshot lastShot;

    private final Logger logger = LoggerFactory.getLogger(LGThinQWasherDryerHandler.class);
    @NonNullByDefault
    private final LGThinQWMApiClientService lgThinqWMApiClientService;

    public LGThinQWasherDryerHandler(Thing thing, LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider,
            ThinqChannelTypeProvider channelTypeProvider, ThinqChannelGroupTypeProvider channelGroupTypeProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, stateDescriptionProvider);
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.thinqChannelGroupProvider = channelGroupTypeProvider;
        this.thinqChannelProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgThinqWMApiClientService = lgPlatformType.equals(PLATFORM_TYPE_V1)
                ? LGThinQWMApiV1ClientServiceImpl.getInstance()
                : LGThinQWMApiV2ClientServiceImpl.getInstance();
        channelGroupRemoteStartUID = new ChannelGroupUID(getThing().getUID(), WM_CHANNEL_REMOTE_START_GRP_ID);
        channelGroupDashboardUID = new ChannelGroupUID(getThing().getUID(), WM_CHANNEL_DASHBOARD_GRP_ID);
        courseChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_COURSE_ID);
        dryLevelChannelUID = new ChannelUID(channelGroupDashboardUID, DR_CHANNEL_DRY_LEVEL_ID);
        stateChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_STATE_ID);
        processStateChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_PROCESS_STATE_ID);
        remainTimeChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_REMAIN_TIME_ID);
        delayTimeChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_DELAY_TIME_ID);
        temperatureChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_TEMP_LEVEL_ID);
        doorLockChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_DOOR_LOCK_ID);
        childLockChannelUID = new ChannelUID(channelGroupDashboardUID, DR_CHANNEL_CHILD_LOCK_ID);
        rinseChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_RINSE_ID);
        spinChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_SPIN_ID);
        standByModeChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_STAND_BY_ID);
        remoteStartFlagChannelUID = new ChannelUID(channelGroupDashboardUID, WM_CHANNEL_REMOTE_START_ID);
        remoteStartStopChannelUID = new ChannelUID(channelGroupRemoteStartUID, WM_CHANNEL_REMOTE_START_START_STOP);
    }

    @Override
    protected void initializeThing(@Nullable ThingStatus bridgeStatus) {
        super.initializeThing(bridgeStatus);
        ThingBuilder builder = editThing()
                .withoutChannels(this.getThing().getChannelsOfGroup(channelGroupRemoteStartUID.getId()));
        updateThing(builder.build());
        remoteStartEnabledChannels.clear();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thinq thing. Washer/Dryer Thing v3.4.3");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        WasherDryerCapability wmCap = getCapabilities();

        List<StateOption> options = new ArrayList<>();
        wmCap.getStateFeat().getValuesMapping()
                .forEach((k, v) -> options.add(new StateOption(k, keyIfValueNotFound(CAP_WDM_STATE, v))));
        stateDescriptionProvider.setStateOptions(stateChannelUID, options);

        List<StateOption> optionsCourses = new ArrayList<>();
        wmCap.getCourses().forEach((k, v) -> optionsCourses.add(new StateOption(k, emptyIfNull(v.getCourseName()))));
        stateDescriptionProvider.setStateOptions(courseChannelUID, optionsCourses);

        List<StateOption> optionsTemp = new ArrayList<>();
        wmCap.getTemperatureFeat().getValuesMapping()
                .forEach((k, v) -> optionsTemp.add(new StateOption(k, keyIfValueNotFound(CAP_WM_TEMPERATURE, v))));
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
                .forEach((k, v) -> optionsPre.add(new StateOption(k, keyIfValueNotFound(CAP_WDM_PROCESS_STATE, v))));
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

    private ZonedDateTime getZonedDateTime(String minutesAndSeconds) {
        if (minutesAndSeconds.length() != 5) {
            logger.error("Washer/Disher remain/delay time is not in standard MM:SS. Value received: {}. Reset to 00:00",
                    minutesAndSeconds);
            minutesAndSeconds = "00:00";
        }
        String min = minutesAndSeconds.substring(0, 2);
        String sec = minutesAndSeconds.substring(3);

        return ZonedDateTime.of(1970, 1, 1, 0, Integer.parseInt(min), Integer.parseInt(sec), 0, ZoneId.systemDefault());
    }

    @Nullable
    private String getItemLinkedValue(ChannelUID channelUID) {
        Set<Item> items = itemChannelLinkRegistry.getLinkedItems(channelUID);
        if (items.size() > 0) {
            for (Item i : items) {
                return i.getState().toString();
            }
        }
        return null;
    }

    @Override
    protected void updateDeviceChannels(WasherDryerSnapshot shot) {
        lastShot = shot;
        updateState("dashboard#" + CHANNEL_POWER_ID,
                (DevicePowerState.DV_POWER_ON.equals(shot.getPowerStatus()) ? OnOffType.ON : OnOffType.OFF));
        updateState(stateChannelUID, new StringType(shot.getState()));
        updateState(processStateChannelUID, new StringType(shot.getProcessState()));
        updateState(dryLevelChannelUID, new StringType(shot.getDryLevel()));
        updateState(childLockChannelUID, new StringType(shot.getChildLock()));
        updateState(courseChannelUID, new StringType(shot.getCourse()));
        updateState(temperatureChannelUID, new StringType(shot.getTemperatureLevel()));
        updateState(doorLockChannelUID, new StringType(shot.getDoorLock()));
        updateState(remainTimeChannelUID, new StringType(shot.getRemainingTime()));
        updateState(delayTimeChannelUID, new StringType(shot.getReserveTime()));
        updateState(standByModeChannelUID, shot.isStandBy() ? OnOffType.ON : OnOffType.OFF);
        updateState(remoteStartFlagChannelUID, shot.isRemoteStartEnabled() ? OnOffType.ON : OnOffType.OFF);
        updateState(spinChannelUID, new StringType(shot.getSpin()));
        updateState(rinseChannelUID, new StringType(shot.getRinse()));
        Channel rsStartStopChannel = getThing().getChannel(remoteStartStopChannelUID);
        final List<Channel> dynChannels = new ArrayList<>();
        // only can have remote start channel is the WM is not in sleep mode, and remote start is enabled.
        if (shot.isRemoteStartEnabled() && !shot.isStandBy()) {
            ThingHandlerCallback callback = getCallback();
            if (rsStartStopChannel == null && callback != null) {
                // === creating channel LaunchRemote
                dynChannels
                        .add(createDynChannel(WM_CHANNEL_REMOTE_START_START_STOP, remoteStartStopChannelUID, "Switch"));
                // Just enabled remote start. Then is Off
                updateState(remoteStartStopChannelUID, OnOffType.OFF);
                // === creating selectable channels for the Course (if any)
                try {
                    WasherDryerCapability cap = getCapabilities();
                    CourseDefinition courseDef = cap.getCourses().get(shot.getCourse());
                    if (WM_COURSE_NOT_SELECTED_VALUE.equals(shot.getSmartCourse()) && courseDef != null) {
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
                            ChannelUID targetChannel = null;
                            ChannelUID refChannel = null;
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
                                            String canonicalValue = Objects
                                                    .requireNonNullElse(fd.getValuesMapping().get(v), v);
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
                } catch (LGThinqApiException e) {
                    throw new RuntimeException(e);
                }
                remoteStartEnabledChannels.addAll(dynChannels);

            }
            if (isLinked(remoteStartStopChannelUID)) {
                updateState(WM_CHANNEL_REMOTE_START_START_STOP, new StringType(""));
            }
        } else if (remoteStartEnabledChannels.size() > 0) {
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
        data.put(cap.getDefaultSmartCourseFeatName(), smartCourse);
        CourseType courseType = cap.getCourses().get("NOT_SELECTED".equals(smartCourse) ? course : smartCourse)
                .getCourseType();
        data.put("courseType", courseType.getValue());
        for (Channel c : remoteStartEnabledChannels) {
            String value = Objects.requireNonNullElse(getItemLinkedValue(c.getUID()), "");
            String simpleChannelUID = getSimpleChannelUID(c.getUID().getId());
            switch (simpleChannelUID) {
                case WM_CHANNEL_REMOTE_START_RINSE:
                    data.put(cap.getRinseFeat().getName(), value);
                    break;
                case WM_CHANNEL_REMOTE_START_TEMP:
                    data.put(cap.getTemperatureFeat().getName(), value);
                    break;
                case WM_CHANNEL_REMOTE_START_SPIN:
                    data.put(cap.getSpinFeat().getName(), value);
                    break;
                default:
                    logger.warn("channel [{}] not mapped for this binding. It most likely a bug.", simpleChannelUID);
            }
        }

        return data;
    }

    @Override
    protected void processCommand(LGThinQAbstractDeviceHandler.AsyncCommandParams params) throws LGThinqApiException {
        Command command = params.command;
        String simpleChannelUID;
        simpleChannelUID = getSimpleChannelUID(params.channelUID);
        switch (simpleChannelUID) {
            case WM_CHANNEL_REMOTE_START_START_STOP: {
                if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        if (lastShot != null && !lastShot.isStandBy()) {
                            lgThinqWMApiClientService.remoteStart(getBridgeId(), getCapabilities(), getDeviceId(),
                                    getRemoteStartData());
                        } else if (lastShot != null && lastShot.isStandBy()) {
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

    /**
     * Returns the simple channel UID name, i.e., without group.
     * 
     * @param uid Full UID name
     * @return simple channel UID name, i.e., without group.
     */
    private static String getSimpleChannelUID(String uid) {
        String simpleChannelUID;
        if (uid.indexOf("#") > 0) {
            // I have to remove the channelGroup from de channelUID
            simpleChannelUID = uid.split("#")[1];
        } else {
            simpleChannelUID = uid;
        }
        return simpleChannelUID;
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
    }
}
