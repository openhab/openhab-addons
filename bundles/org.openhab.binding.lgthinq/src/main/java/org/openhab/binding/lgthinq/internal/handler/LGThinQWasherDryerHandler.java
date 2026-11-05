/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer.WasherDryerSnapshot;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQWasherDryerHandler} Handle Washer/Dryer And Washer Dryer Towers things
 *
 * @author Nemer Daud - Initial contribution
 * @author Carlo Dischler - Fix remote start and keep its settings across stand-by
 */
@NonNullByDefault
public class LGThinQWasherDryerHandler
        extends LGThinQAbstractDeviceHandler<WasherDryerCapability, WasherDryerSnapshot> {

    private final DeviceChannelGroup dashboard;
    private final DeviceChannelGroup remoteStart;
    private final LGThinQStateDescriptionProvider stateDescriptionProvider;
    private volatile @Nullable WasherDryerSnapshot remoteStartShot;
    private volatile boolean remoteStartValuesInitialized;

    private final Logger logger = LoggerFactory.getLogger(LGThinQWasherDryerHandler.class);
    private final LGThinQWMApiClientService lgThinqWMApiClientService;

    public LGThinQWasherDryerHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry, HttpClientFactory httpClientFactory) {
        super(thing, stateDescriptionProvider, itemChannelLinkRegistry);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgThinqWMApiClientService = LGThinQApiClientServiceFactory.newWMApiClientService(lgPlatformType,
                httpClientFactory);
        dashboard = new DeviceChannelGroup(getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID);
        remoteStart = new DeviceChannelGroup(getThing().getUID(), CHANNEL_WMD_REMOTE_START_GRP_ID);
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        WasherDryerCapability wmCap = getCapabilities();

        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_STATE_ID),
                wmCap.getStateFeat().getValuesMapping(), CAP_WMD_STATE);
        loadCourseOptions(wmCap.getCourses(), dashboard.channel(CHANNEL_WMD_COURSE_ID));
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_TEMP_LEVEL_ID),
                wmCap.getTemperatureFeat().getValuesMapping(), CAP_WMD_TEMPERATURE);
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_DOOR_LOCK_ID),
                Map.of("0", "Unlocked", "1", "Locked"));
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_SPIN_ID),
                wmCap.getSpinFeat().getValuesMapping(), CAP_WM_SPIN);
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_RINSE_ID),
                wmCap.getRinseFeat().getValuesMapping(), CAP_WM_RINSE);
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                dashboard.channel(CHANNEL_WMD_PROCESS_STATE_ID), wmCap.getProcessState().getValuesMapping(),
                CAP_WMD_PROCESS_STATE);
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                dashboard.channel(CHANNEL_WMD_PREVIOUS_STATE_ID), wmCap.getPreviousState().getValuesMapping(),
                CAP_WMD_STATE);
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_CHILD_LOCK_ID),
                Map.of("CHILDLOCK_OFF", "Unlocked", "CHILDLOCK_ON", "Locked"));
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_DRY_LEVEL_ID),
                wmCap.getDryLevel().getValuesMapping(), CAP_DR_DRY_LEVEL);

        // the remote start channels are always present; earlier versions removed them while remote start was off
        addRemoteStartChannel(CHANNEL_WMD_REMOTE_START_START_STOP, CoreItemFactory.SWITCH);
        addRemoteStartChannel(CHANNEL_WMD_REMOTE_COURSE, CoreItemFactory.STRING);
        loadCourseOptions(wmCap.getCourses(), remoteStart.channel(CHANNEL_WMD_REMOTE_COURSE));
        if (!FeatureDefinition.NULL_DEFINITION.equals(wmCap.getTemperatureFeat())) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                    addRemoteStartChannel(CHANNEL_WMD_REMOTE_START_TEMP, CoreItemFactory.STRING),
                    wmCap.getTemperatureFeat().getValuesMapping(), CAP_WMD_TEMPERATURE);
        }
        if (!FeatureDefinition.NULL_DEFINITION.equals(wmCap.getSpinFeat())) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                    addRemoteStartChannel(CHANNEL_WMD_REMOTE_START_SPIN, CoreItemFactory.STRING),
                    wmCap.getSpinFeat().getValuesMapping(), CAP_WM_SPIN);
        }
        if (!FeatureDefinition.NULL_DEFINITION.equals(wmCap.getRinseFeat())) {
            StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                    addRemoteStartChannel(CHANNEL_WMD_REMOTE_START_RINSE, CoreItemFactory.STRING),
                    wmCap.getRinseFeat().getValuesMapping(), CAP_WM_RINSE);
        }
    }

    private ChannelUID addRemoteStartChannel(String channelId, String itemType) {
        ChannelUID channelUID = remoteStart.channel(channelId);
        if (getThing().getChannel(channelUID) == null && getCallback() != null) {
            createDynChannel(channelId, channelUID, itemType);
        }
        return channelUID;
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
        updateState(dashboard.channel(CHANNEL_WMD_STATE_ID), stringState(shot.getState()));
        updateState(dashboard.channel(CHANNEL_WMD_PREVIOUS_STATE_ID), stringState(shot.getPreviousState()));
        updateState(dashboard.channel(CHANNEL_WMD_PROCESS_STATE_ID), stringState(shot.getProcessState()));
        updateState(dashboard.channel(CHANNEL_WMD_DRY_LEVEL_ID), new StringType(shot.getDryLevel()));
        updateState(dashboard.channel(CHANNEL_WMD_CHILD_LOCK_ID), new StringType(shot.getChildLock()));
        updateState(dashboard.channel(CHANNEL_WMD_COURSE_ID), new StringType(shot.getCourse()));
        updateState(dashboard.channel(CHANNEL_WMD_TEMP_LEVEL_ID), new StringType(shot.getTemperatureLevel()));
        updateState(dashboard.channel(CHANNEL_WMD_DOOR_LOCK_ID), new StringType(shot.getDoorLock()));
        updateState(dashboard.channel(CHANNEL_WMD_REMAIN_TIME_ID), new StringType(shot.getRemainingTime()));
        updateState(dashboard.channel(CHANNEL_WMD_DELAY_TIME_ID), new StringType(shot.getReserveTime()));
        updateState(dashboard.channel(CHANNEL_WMD_STAND_BY_ID), OnOffType.from(shot.isStandBy()));
        updateState(dashboard.channel(CHANNEL_WMD_REMOTE_START_ID), OnOffType.from(shot.isRemoteStartEnabled()));
        updateState(dashboard.channel(CHANNEL_WMD_SPIN_ID), new StringType(shot.getSpin()));
        updateState(dashboard.channel(CHANNEL_WMD_RINSE_ID), new StringType(shot.getRinse()));
        // in stand-by the machine reports no course, so keep the settings it had while remote start was enabled
        WasherDryerCapability cap = getCapabilities();
        if (!shot.isRemoteStartEnabled()) {
            remoteStartShot = null;
            remoteStartValuesInitialized = false;
        } else if (hasSelectedCourse(cap, shot)) {
            remoteStartShot = shot;
            // fill the remote start items once with the washer's settings, afterwards they keep the user's values
            if (!remoteStartValuesInitialized) {
                remoteStartValuesInitialized = true;
                updateState(remoteStart.channel(CHANNEL_WMD_REMOTE_START_START_STOP), OnOffType.OFF);
                updateState(remoteStart.channel(CHANNEL_WMD_REMOTE_COURSE), new StringType(shot.getCourse()));
                for (String channelId : List.of(CHANNEL_WMD_REMOTE_START_TEMP, CHANNEL_WMD_REMOTE_START_SPIN,
                        CHANNEL_WMD_REMOTE_START_RINSE)) {
                    updateState(remoteStart.channel(channelId), new StringType(getFeatureValue(channelId, shot)));
                }
            }
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

    private static boolean hasSelectedCourse(WasherDryerCapability cap, WasherDryerSnapshot shot) {
        String course = shot.getCourse();
        return !cap.getNotSelectedCourseKey().equals(course) && cap.getCourses().containsKey(course);
    }

    private static String getCurrentCourse(WasherDryerCapability cap, WasherDryerSnapshot shot) {
        return hasSelectedCourse(cap, shot) ? shot.getCourse() : cap.getDefaultCourseId();
    }

    static WasherDryerSnapshot selectRemoteStartShot(WasherDryerCapability cap, WasherDryerSnapshot shot,
            @Nullable WasherDryerSnapshot remembered) {
        return remembered != null && !hasSelectedCourse(cap, shot) ? remembered : shot;
    }

    private static String getFeatureValue(String remoteStartChannelId, WasherDryerSnapshot shot) {
        return switch (remoteStartChannelId) {
            case CHANNEL_WMD_REMOTE_START_TEMP -> shot.getTemperatureLevel();
            case CHANNEL_WMD_REMOTE_START_SPIN -> shot.getSpin();
            case CHANNEL_WMD_REMOTE_START_RINSE -> shot.getRinse();
            default -> "";
        };
    }

    private Map<String, Object> getRemoteStartData(WasherDryerSnapshot lastShot) throws LGThinqApiException {
        if (lastShot.getRawData().isEmpty()) {
            return lastShot.getRawData();
        }
        WasherDryerCapability cap = getCapabilities();
        CommandDefinition cmd = cap.getCommandsDefinition().get(cap.getCommandRemoteStart());
        if (cmd == null) {
            logger.error("Command for Remote Start not found in the Washer descriptor. It's most likely a bug");
            return Collections.emptyMap();
        }
        WasherDryerSnapshot rsShot = selectRemoteStartShot(cap, lastShot, remoteStartShot);
        String selectedCourse = getItemLinkedValue(remoteStart.channel(CHANNEL_WMD_REMOTE_COURSE));
        if (selectedCourse == null || cap.getNotSelectedCourseKey().equals(selectedCourse)
                || !cap.getCourses().containsKey(selectedCourse)) {
            selectedCourse = getCurrentCourse(cap, rsShot);
        }
        Map<String, String> selectedFeatures = new HashMap<>();
        for (Channel c : getThing().getChannelsOfGroup(CHANNEL_WMD_REMOTE_START_GRP_ID)) {
            String simpleChannelUID = getSimpleChannelUID(c.getUID().getId());
            String featureName = switch (simpleChannelUID) {
                case CHANNEL_WMD_REMOTE_START_START_STOP, CHANNEL_WMD_REMOTE_COURSE -> "";
                case CHANNEL_WMD_REMOTE_START_RINSE -> cap.getRinseFeat().getName();
                case CHANNEL_WMD_REMOTE_START_TEMP -> cap.getTemperatureFeat().getName();
                case CHANNEL_WMD_REMOTE_START_SPIN -> cap.getSpinFeat().getName();
                default -> {
                    logger.warn("channel [{}] not mapped for this binding. It is most likely a bug.", simpleChannelUID);
                    yield "";
                }
            };
            String value = getItemLinkedValue(c.getUID());
            // the name is also empty for static remote start channels whose feature the model doesn't have
            if (!featureName.isEmpty() && value != null && !value.isBlank()) {
                selectedFeatures.put(featureName, value);
            }
        }
        return buildRemoteStartData(cap, cmd.getData(), rsShot, selectedCourse, selectedFeatures,
                WMD_STATE_INITIAL.equals(lastShot.getState()));
    }

    static Map<String, Object> buildRemoteStartData(WasherDryerCapability cap, Map<String, Object> cmdTemplate,
            WasherDryerSnapshot shot, String selectedCourse, Map<String, String> selectedFeatures,
            boolean initialStart) {
        Map<String, Object> rawData = shot.getRawData();
        Map<String, Object> data = new HashMap<>();
        // 1st - copy snapshot data to command
        cmdTemplate.forEach((k, v) -> {
            Object value = rawData.get(k);
            data.put(k, value != null ? value : v);
        });
        // 2nd - use the defaults of the selected course for another course and for values the snapshot lacks
        CourseDefinition selCourseDef = cap.getCourses().get(selectedCourse);
        if (selCourseDef != null) {
            boolean otherCourse = !selectedCourse.equals(shot.getCourse());
            selCourseDef.getFunctions().stream().filter(f -> !f.getDefaultValue().isBlank())
                    .filter(f -> otherCourse || rawData.get(f.getValue()) == null)
                    .forEach(f -> data.put(f.getValue(), f.getDefaultValue()));
        }
        String smartCourse = shot.getSmartCourse();
        CourseDefinition courseDef = cap.getCourses().get(smartCourse);
        if (cap.getNotSelectedCourseKey().equals(smartCourse) || courseDef == null) {
            smartCourse = cap.getNotSelectedCourseKey();
            courseDef = Objects.requireNonNull(selCourseDef, "The selected course must be a course of the device");
        }
        data.put(cap.getDefaultCourseFieldName(), selectedCourse);
        data.put(cap.getDefaultSmartCourseFeatName(), smartCourse);
        data.put("courseType", courseDef.getCourseType().getValue());
        // 3rd - replace custom selectable features with channel's ones.
        data.putAll(selectedFeatures);
        // a washer waiting in the initial state only runs the course and options from the command with this bit,
        // otherwise it starts whatever is set on its panel, which is nothing after stand-by
        if (data.containsKey("initialBit")) {
            data.put("initialBit", initialStart ? "INITIAL_BIT_ON" : "INITIAL_BIT_OFF");
        }
        return data;
    }

    @Override
    protected void processCommand(AsyncCommandParams params) throws LGThinqApiException {
        WasherDryerSnapshot lastShot = getLastShot();
        Command command = params.command();
        String simpleChannelUID;
        simpleChannelUID = getSimpleChannelUID(params.channelUID());
        switch (simpleChannelUID) {
            case CHANNEL_WMD_REMOTE_START_START_STOP: {
                if (command instanceof OnOffType ooCmd) {
                    if (ooCmd == OnOffType.ON) {
                        if (!lastShot.isRemoteStartEnabled()) {
                            logger.warn("Remote start is not enabled on the washer. Ignoring command");
                        } else if (!lastShot.isStandBy()) {
                            Map<String, Object> data = getRemoteStartData(lastShot);
                            if (data.isEmpty()) {
                                logger.warn("No data available for remote start. Ignoring command");
                            } else {
                                lgThinqWMApiClientService.remoteStart(getBridgeId(), getCapabilities(), getDeviceId(),
                                        data);
                            }
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
            case CHANNEL_WMD_REMOTE_COURSE:
            case CHANNEL_WMD_REMOTE_START_TEMP:
            case CHANNEL_WMD_REMOTE_START_SPIN:
            case CHANNEL_WMD_REMOTE_START_RINSE:
                // the values are read from the linked items when the remote start is sent
                break;
            default: {
                logger.warn("Command {} to the channel {} not supported. Ignored.", command, params.channelUID());
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
        updateState(CHANNEL_DASHBOARD_GRP_WITH_SEP + CHANNEL_AC_POWER_ID, OnOffType.OFF);
        updateState(dashboard.channel(CHANNEL_WMD_STATE_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_PREVIOUS_STATE_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_PROCESS_STATE_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_COURSE_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_TEMP_LEVEL_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_DOOR_LOCK_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_REMAIN_TIME_ID), UnDefType.UNDEF);
    }

    private State stringState(String value) {
        return value.isBlank() ? UnDefType.UNDEF : new StringType(value);
    }
}
