/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.handler;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.blueiris.BlueIrisBindingConstants;
import org.openhab.binding.blueiris.internal.data.CamConfigRequest;
import org.openhab.binding.blueiris.internal.data.CamListReply;
import org.openhab.binding.blueiris.internal.data.CamListRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlueIrisCameraHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Bennett - Initial contribution
 */
public class BlueIrisCameraHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BlueIrisCameraHandler.class);
    private CamConfigRequest request;

    public BlueIrisCameraHandler(Thing thing) {
        super(thing);
        request = new CamConfigRequest();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.error("Got channel id {} {}", channelUID, command);
        if (channelUID.getId().equals(BlueIrisBindingConstants.CHANNEL_ENABLED)) {
            if (command instanceof OnOffType) {
                CamConfigRequest request = new CamConfigRequest();
                OnOffType onOff = (OnOffType) command;
                if (onOff == OnOffType.ON) {
                    request.setEnable(true);
                } else {
                    request.setEnable(false);
                }
                request.setCamera(getThing().getProperties().get(BlueIrisBindingConstants.PROPERTY_SHORT_NAME));

                if (getBlueIrisBridgeHandler().getConnection().sendCommand(request)) {
                    // Re-request the data as a list.
                    CamListRequest camList = new CamListRequest();
                    if (getBlueIrisBridgeHandler().getConnection().sendCommand(camList)) {
                        getBlueIrisBridgeHandler().onCamList(camList.getReply());
                    }
                }
            }
        }
        if (channelUID.getId().equals(BlueIrisBindingConstants.CHANNEL_MOTION)) {
            if (command instanceof OnOffType) {
                CamConfigRequest request = new CamConfigRequest();
                OnOffType onOff = (OnOffType) command;
                if (onOff == OnOffType.ON) {
                    request.setMotion(true);
                } else {
                    request.setMotion(false);
                }
                request.setCamera(getThing().getProperties().get(BlueIrisBindingConstants.PROPERTY_SHORT_NAME));

                if (getBlueIrisBridgeHandler().getConnection().sendCommand(request)) {
                    // Re-request the data as a list.
                    CamListRequest camList = new CamListRequest();
                    if (getBlueIrisBridgeHandler().getConnection().sendCommand(camList)) {
                        getBlueIrisBridgeHandler().onCamList(camList.getReply());
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for config from blue iris");
    }

    /**
     * Called by the bridge when there is data for this specific camera.
     *
     * @param camData The data to update with
     */
    public void onCamUpdated(CamListReply.Data camData) {
        if (camData.isOnline()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Offline from the blue iris software");
        }
        Channel chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_AUDIO_SUPPORTED);
        updateState(chan.getUID(), camData.isAudio() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_CAUTION_ICON);
        updateState(chan.getUID(), camData.isYellow() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_ENABLED);
        updateState(chan.getUID(), camData.isEnabled() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_MOTION);
        updateState(chan.getUID(), camData.isMotion() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_NO_SIGNAL);
        updateState(chan.getUID(), camData.isNoSignal() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_PAUSED);
        updateState(chan.getUID(), camData.isPaused() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_WEBCAST);
        updateState(chan.getUID(), camData.isWebcast() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_PAUSED_TYPE);
        updateState(chan.getUID(), new DecimalType(camData.getPause()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_PTZ_SUPPORTED);
        updateState(chan.getUID(), camData.isPtz() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_RECORDING);
        updateState(chan.getUID(), camData.isRecording() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_TRIGGERED);
        updateState(chan.getUID(), camData.isTriggered() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_ALERTING);
        updateState(chan.getUID(), camData.isAlerting() ? OnOffType.ON : OnOffType.OFF);
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_NUMBER_OF_ALERTS);
        updateState(chan.getUID(), new DecimalType(camData.getNumAlerts()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_NUMBER_OF_NO_SIGNAL);
        updateState(chan.getUID(), new DecimalType(camData.getNumNoSignal()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_NUMBER_OF_CLIPS);
        updateState(chan.getUID(), new DecimalType(camData.getNumClips()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_NUMBER_OF_TRIGGERS);
        updateState(chan.getUID(), new DecimalType(camData.getNumTriggers()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_PROFILE);
        updateState(chan.getUID(), new StringType(camData.getProfile()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_HEIGHT);
        updateState(chan.getUID(), new DecimalType(camData.getHeight()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_WIDTH);
        updateState(chan.getUID(), new DecimalType(camData.getWidth()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_FPS);
        updateState(chan.getUID(), new DecimalType(camData.getFPS()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_NEW_ALERTS);
        updateState(chan.getUID(), new DecimalType(camData.getNewAlerts()));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_LAST_ALERT);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(camData.getAlertUTC() * 1000);
        updateState(chan.getUID(), new DateTimeType(cal));
        chan = getThing().getChannel(BlueIrisBindingConstants.CHANNEL_COLOR);
        HSBType color = HSBType.fromRGB(camData.getColor() & 0xff, (camData.getColor() >> 8) & 0xff,
                (camData.getColor() >> 16) & 0xff);
        updateState(chan.getUID(), color);
        request.setEnable(camData.isEnabled());
        request.setMotion(camData.isMotion());
        request.setPause(0);
        request.setReset(false);

        // Now ask for details about ourselves.
        CamConfigRequest configRequest = new CamConfigRequest();
        configRequest.setCamera(camData.getOptionValue());
        if (getBlueIrisBridgeHandler().getConnection().sendCommand(configRequest)) {
            // Update some stuff.
        }
    }

    BlueIrisBridgeHandler getBlueIrisBridgeHandler() {
        return (BlueIrisBridgeHandler) getBridge().getHandler();
    }
}
