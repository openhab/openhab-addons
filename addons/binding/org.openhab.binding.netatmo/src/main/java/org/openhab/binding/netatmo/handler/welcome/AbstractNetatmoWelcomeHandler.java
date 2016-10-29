/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler.welcome;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.handler.NetatmoBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import io.swagger.client.model.NAWelcomeCameras;
import io.swagger.client.model.NAWelcomeHomes;

/**
 * {@link AbstractNetatmoWelcomeHandler} is the abstract class that handles
 * common behaviors of both Devices and Modules
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
abstract class AbstractNetatmoWelcomeHandler extends BaseThingHandler {
    private static Logger logger = LoggerFactory.getLogger(AbstractNetatmoWelcomeHandler.class);

    protected NetatmoBridgeHandler bridgeHandler;
    private static HashMap<String, NAWelcomeHomes> welcomeHomes = new HashMap<String, NAWelcomeHomes>();
    private static HashMap<String, String> videoUrl = new HashMap<String, String>();

    protected NAWelcomeHomes getWelcomeHomes(String homeID) {
        return welcomeHomes.get(homeID);
    }

    protected void setWelcomeHomes(String homeID, NAWelcomeHomes welcomeHomes) {
        AbstractNetatmoWelcomeHandler.welcomeHomes.put(homeID, welcomeHomes);

        // Build the url for video streaming and the live camera picture
        for (NAWelcomeCameras camera : welcomeHomes.getCameras()) {
            String url = getNetatmoWelcomeUrl(camera.getVpnUrl());
            setVideoUrl(camera.getId(), url);
        }
    }

    protected String getVideoUrl(String cameraID) {
        return videoUrl.get(cameraID);
    }

    protected void setVideoUrl(String cameraID, String videoUrl) {
        AbstractNetatmoWelcomeHandler.videoUrl.put(cameraID, videoUrl);
    }

    AbstractNetatmoWelcomeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        super.bridgeHandlerInitialized(thingHandler, bridge);
        bridgeHandler = (NetatmoBridgeHandler) thingHandler;
    }

    protected State getNAThingProperty(String chanelId) {
        return null;
    }

    protected void updateChannels() {
        logger.debug("Updating device channels");

        for (Channel channel : getThing().getChannels()) {
            String chanelId = channel.getUID().getId();
            State state = getNAThingProperty(chanelId);
            if (state != null) {
                updateState(channel.getUID(), state);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    protected Calendar timestampToCalendar(Integer netatmoTS) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(netatmoTS * 1000L);
        return calendar;
    }

    protected DecimalType toDecimalType(float value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new DecimalType(decimal);
    }

    protected DecimalType toDecimalType(double value) {
        BigDecimal decimal = new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        return new DecimalType(decimal);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);

            State state = getNAThingProperty(channelUID.getId());
            if (state != null) {
                for (Channel channel : getThing().getChannels()) {
                    if (channelUID.equals(channel.getUID())) {
                        updateState(channelUID, state);
                        break;
                    }
                }
            }
        } else {
            logger.warn("This Thing is read-only and can only handle REFRESH command");
        }
    }

    /**
     * Returns the Url of the picture
     *
     * @return Url of the picture or UnDefType.UNDEF
     */
    protected State getPictureUrl(String id, String key) {
        State ret = UnDefType.UNDEF;

        if (id != null && key != null) {
            StringBuffer sb = new StringBuffer();

            sb.append(WELCOME_PICTURE_URL);
            sb.append("?").append(WELCOME_PICTURE_IMAGEID).append("=").append(id);
            sb.append("&").append(WELCOME_PICTURE_KEY).append("=").append(key);

            ret = new StringType(sb.toString());
        }

        return ret;
    }

    /**
     *
     * @param url The VPNUrl for which the local url should be found
     * @return The local Urtl or the vpn url if the request is not local
     */
    private String getNetatmoWelcomeUrl(String vpnurl) {
        String ret = vpnurl;

        try {
            // Read the local Url
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(vpnurl + WELCOME_PING).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String json = response.body().string();
                JsonElement resp = new JsonParser().parse(json);
                String localUrl = resp.getAsJsonObject().get("local_url").getAsString();

                // Validate the local Url
                request = new Request.Builder().url(localUrl + WELCOME_PING).build();
                response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String json2 = response.body().string();
                    JsonElement resp2 = new JsonParser().parse(json2);
                    String localUrl2 = resp2.getAsJsonObject().get("local_url").getAsString();

                    if (localUrl.equals(localUrl2)) {
                        ret = localUrl;
                    }
                }
            }

        } catch (IOException e) {
        }
        return ret;
    }

}
