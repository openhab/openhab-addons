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
import java.util.concurrent.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.LGThinQDeviceDynStateDescriptionProvider;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQWMApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQWMApiV2ClientServiceImpl;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.washer.WasherCapability;
import org.openhab.binding.lgthinq.lgservices.model.washer.WasherSnapshot;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGThinQWasherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQWasherHandler extends LGThinQAbstractDeviceHandler<WasherCapability, WasherSnapshot> {

    private final LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider;
    private final ChannelUID stateChannelUUID;
    private final ChannelUID courseChannelUUID;
    private final ChannelUID smartCourseChannelUUID;
    private final ChannelUID temperatureChannelUUID;
    private final Logger logger = LoggerFactory.getLogger(LGThinQWasherHandler.class);
    @NonNullByDefault
    private final LGThinQWMApiClientService lgThinqWMApiClientService;

    // *** Long running isolated threadpools.
    private final ScheduledExecutorService pollingScheduler = Executors.newScheduledThreadPool(1);

    private final LinkedBlockingQueue<AsyncCommandParams> commandBlockQueue = new LinkedBlockingQueue<>(20);

    @NonNullByDefault

    public LGThinQWasherHandler(Thing thing, LGThinQDeviceDynStateDescriptionProvider stateDescriptionProvider) {
        super(thing, stateDescriptionProvider);
        this.stateDescriptionProvider = stateDescriptionProvider;
        lgThinqWMApiClientService = LGThinQWMApiV2ClientServiceImpl.getInstance();
        stateChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_STATE_ID);
        courseChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_COURSE_ID);
        smartCourseChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_SMART_COURSE_ID);
        temperatureChannelUUID = new ChannelUID(getThing().getUID(), WM_CHANNEL_TEMP_LEVEL_ID);
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
        logger.debug("Initializing Thinq thing. Washer Thing v0.1");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    public void updateChannelDynStateDescription() throws LGThinqApiException {
        WasherCapability wmCap = getCapabilities();
        if (isLinked(stateChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getState().forEach((k, v) -> options.add(new StateOption(v, keyIfValueNotFound(CAP_WP_STATE, k))));
            stateDescriptionProvider.setStateOptions(stateChannelUUID, options);
        }
        if (isLinked(courseChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getCourses().forEach((k, v) -> options.add(new StateOption(k, emptyIfNull(v))));
            stateDescriptionProvider.setStateOptions(courseChannelUUID, options);
        }
        if (isLinked(smartCourseChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getSmartCourses().forEach((k, v) -> options.add(new StateOption(k, emptyIfNull(v))));
            stateDescriptionProvider.setStateOptions(smartCourseChannelUUID, options);
        }
        if (isLinked(temperatureChannelUUID)) {
            List<StateOption> options = new ArrayList<>();
            wmCap.getTemperature()
                    .forEach((k, v) -> options.add(new StateOption(v, keyIfValueNotFound(CAP_WP_TEMPERATURE, k))));
            stateDescriptionProvider.setStateOptions(temperatureChannelUUID, options);
        }
    }

    @Override
    public LGThinQApiClientService<WasherCapability, WasherSnapshot> getLgThinQAPIClientService() {
        return lgThinqWMApiClientService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void updateDeviceChannels(WasherSnapshot shot) {
        updateState(CHANNEL_POWER_ID,
                (DevicePowerState.DV_POWER_ON.equals(shot.getPowerStatus()) ? OnOffType.ON : OnOffType.OFF));
        updateState(WM_CHANNEL_STATE_ID, new StringType(shot.getState()));
        updateState(WM_CHANNEL_COURSE_ID, new StringType(shot.getCourse()));
        updateState(WM_CHANNEL_SMART_COURSE_ID, new StringType(shot.getSmartCourse()));
        updateState(WM_CHANNEL_TEMP_LEVEL_ID, new StringType(shot.getTemperatureLevel()));
        updateState(WM_CHANNEL_DOOR_LOCK_ID, new StringType(shot.getDoorLock()));
        updateState(WM_CHANNEL_REMAIN_TIME_ID, new StringType(shot.getRemainingTime()));
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected void processCommand(LGThinQAbstractDeviceHandler.AsyncCommandParams params) throws LGThinqApiException {
        Command command = params.command;
        switch (params.channelUID) {
            case CHANNEL_POWER_ID: {
                if (command instanceof OnOffType) {
                    lgThinqWMApiClientService.turnDevicePower(getBridgeId(), getDeviceId(),
                            command == OnOffType.ON ? DevicePowerState.DV_POWER_ON : DevicePowerState.DV_POWER_OFF);
                } else {
                    logger.warn("Received command different of OnOffType in Power Channel. Ignoring");
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
    public String getDeviceId() {
        return getThing().getUID().getId();
    }

    @Override
    public String getDeviceAlias() {
        return emptyIfNull(getThing().getProperties().get(DEVICE_ALIAS));
    }

    @Override
    public String getDeviceModelName() {
        return emptyIfNull(getThing().getProperties().get(MODEL_NAME));
    }

    @Override
    public String getDeviceUriJsonConfig() {
        return emptyIfNull(getThing().getProperties().get(MODEL_URL_INFO));
    }

    @Override
    public boolean onDeviceStateChanged() {
        // TODO - HANDLE IT, Think if it's needed
        return false;
    }

    @Override
    public void onDeviceRemoved() {
        // TODO - HANDLE IT, Think if it's needed
    }

    @Override
    public void onDeviceGone() {
        // TODO - HANDLE IT, Think if it's needed
    }
}
