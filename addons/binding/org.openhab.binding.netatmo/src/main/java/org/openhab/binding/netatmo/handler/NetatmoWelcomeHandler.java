/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import static org.openhab.binding.netatmo.NetatmoBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.AbstractNetatmoWelcomeThingConfiguration;
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
 * {@link NetatmoWelcomeHandler} is the abstract class that handles
 * common behaviors of both Devices and Modules
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */

public abstract class NetatmoWelcomeHandler<X extends AbstractNetatmoWelcomeThingConfiguration>
        extends AbstractNetatmoWelcomeThingHandler<X> {
    private static Logger logger = LoggerFactory.getLogger(NetatmoWelcomeHandler.class);

    private static HashMap<String, NAWelcomeHomes> welcomeHomes = new HashMap<String, NAWelcomeHomes>();
    private static HashMap<String, String> videoUrl = new HashMap<String, String>();

    protected NetatmoWelcomeHandler(Thing thing, Class<X> configurationClass) {
        super(thing, configurationClass);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    protected NAWelcomeHomes getWelcomeHomes(String homeID) {
        return welcomeHomes.get(homeID);
    }

    protected void setWelcomeHomes(String homeID, NAWelcomeHomes welcomeHomes) {
        NetatmoWelcomeHandler.welcomeHomes.put(homeID, welcomeHomes);

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
        NetatmoWelcomeHandler.videoUrl.put(cameraID, videoUrl);
    }

    @Override
    protected NetatmoWelcomeBridgeHandler<?> getBridgeHandler() {
        return (NetatmoWelcomeBridgeHandler<?>) getBridge().getHandler();
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        return null;
    }

    protected void updateChannels() {
        logger.debug("Updating device channels");

        for (Channel channel : getThing().getChannels()) {
            String channelId = channel.getUID().getId();
            State state = getNAThingProperty(channelId);
            if (state != null) {
                updateState(channel.getUID(), state);
            }
        }

        updateStatus(ThingStatus.ONLINE);
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
