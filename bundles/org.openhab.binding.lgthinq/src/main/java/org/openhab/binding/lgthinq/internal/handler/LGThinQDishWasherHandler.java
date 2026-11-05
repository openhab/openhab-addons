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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.LGThinQDishWasherApiClientService;
import org.openhab.binding.lgthinq.lgservices.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherCapability;
import org.openhab.binding.lgthinq.lgservices.model.devices.dishwasher.DishWasherSnapshot;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQDishWasherHandler} Handle the Dish Washer Things
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQDishWasherHandler extends LGThinQAbstractDeviceHandler<DishWasherCapability, DishWasherSnapshot> {

    private final DeviceChannelGroup dashboard;
    private final LGThinQStateDescriptionProvider stateDescriptionProvider;
    private final Logger logger = LoggerFactory.getLogger(LGThinQDishWasherHandler.class);
    private final LGThinQDishWasherApiClientService lgThinqDishWasherApiClientService;

    public LGThinQDishWasherHandler(Thing thing, LGThinQStateDescriptionProvider stateDescriptionProvider,
            ItemChannelLinkRegistry itemChannelLinkRegistry, HttpClientFactory httpClientFactory) {
        super(thing, stateDescriptionProvider, itemChannelLinkRegistry);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgThinqDishWasherApiClientService = LGThinQApiClientServiceFactory.newDishWasherApiClientService(lgPlatformType,
                httpClientFactory);
        dashboard = new DeviceChannelGroup(getThing().getUID(), CHANNEL_DASHBOARD_GRP_ID);
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        DishWasherCapability dwCap = getCapabilities();
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_STATE_ID),
                dwCap.getStateFeat().getValuesMapping(), CAP_DW_STATE);
        loadCourseOptions(dwCap.getCourses(), dashboard.channel(CHANNEL_WMD_COURSE_ID));
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider, dashboard.channel(CHANNEL_WMD_DOOR_LOCK_ID),
                dwCap.getDoorStateFeat().getValuesMapping(), CAP_DW_DOOR_STATE);
        StateDescriptionBuilder.setStateOptions(stateDescriptionProvider,
                dashboard.channel(CHANNEL_WMD_PROCESS_STATE_ID), dwCap.getProcessState().getValuesMapping(),
                CAP_DW_PROCESS_STATE);
    }

    @Override
    public LGThinQApiClientService<DishWasherCapability, DishWasherSnapshot> getLgThinQAPIClientService() {
        return lgThinqDishWasherApiClientService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void updateDeviceChannels(DishWasherSnapshot shot) {
        updateState(CHANNEL_DASHBOARD_GRP_WITH_SEP + CHANNEL_AC_POWER_ID,
                OnOffType.from(DevicePowerState.DV_POWER_ON.equals(shot.getPowerStatus())));
        updateState(dashboard.channel(CHANNEL_WMD_STATE_ID), new StringType(shot.getState()));
        updateState(dashboard.channel(CHANNEL_WMD_PROCESS_STATE_ID), new StringType(shot.getProcessState()));
        updateState(dashboard.channel(CHANNEL_WMD_COURSE_ID), new StringType(shot.getCourse()));
        updateState(dashboard.channel(CHANNEL_WMD_DOOR_LOCK_ID), new StringType(shot.getDoorLock()));
        updateState(dashboard.channel(CHANNEL_WMD_REMAIN_TIME_ID), new StringType(shot.getRemainingTime()));
    }

    @Override
    protected DeviceTypes getDeviceType() {
        if (THING_TYPE_DISHWASHER.equals(getThing().getThingTypeUID())) {
            return DeviceTypes.DISH_WASHER;
        } else {
            throw new IllegalArgumentException(
                    "DeviceTypeUuid [" + getThing().getThingTypeUID() + "] not expected for DishWashing/Machine");
        }
    }

    @Override
    protected void processCommand(AsyncCommandParams params) {
        logger.warn("Command {} to the channel {} not supported. Ignored.", params.command(), params.channelUID());
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
        updateState(dashboard.channel(CHANNEL_WMD_PROCESS_STATE_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_COURSE_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_DOOR_LOCK_ID), UnDefType.UNDEF);
        updateState(dashboard.channel(CHANNEL_WMD_REMAIN_TIME_ID), UnDefType.UNDEF);
    }
}
