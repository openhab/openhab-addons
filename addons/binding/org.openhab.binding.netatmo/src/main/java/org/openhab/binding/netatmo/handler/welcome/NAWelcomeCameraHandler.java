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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.config.NetatmoWelcomeConfiguration;

import io.swagger.client.model.NAWelcomeCameras;

/**
 * {@link NAWelcomeCameraHandler} is the class used to handle the Welcome Camera Data
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NAWelcomeCameraHandler extends AbstractNetatmoWelcomeHandler {

    private NetatmoWelcomeConfiguration configuration;
    protected NAWelcomeCameras camera;

    public NAWelcomeCameraHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.configuration = this.getConfigAs(NetatmoWelcomeConfiguration.class);
    }

    public String getParentId() {
        return configuration.getWelcomeHomeId();
    }

    public String getId() {
        return configuration.getWelcomeCameraId();
    }

    @Override
    protected void updateChannels() {
        try {
            for (Thing thing : getBridgeHandler().getThing().getThings()) {
                ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof NAWelcomeHomeHandler) {
                    NAWelcomeHomeHandler welcomeHomeHandler = (NAWelcomeHomeHandler) thingHandler;
                    String parentId = welcomeHomeHandler.getId();
                    if (parentId != null && parentId.equals(getParentId())) {

                        for (NAWelcomeCameras myCamera : getWelcomeHomes(getParentId()).getCameras()) {
                            if (myCamera.getId().equalsIgnoreCase(getId())) {
                                this.camera = myCamera;
                                super.updateChannels();
                                break;
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    @Override
    protected State getNAThingProperty(String chanelId) {
        try {
            switch (chanelId) {
                case CHANNEL_WELCOME_CAMERA_ID:
                    return camera.getId() != null ? new StringType(camera.getId()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_TYPE:
                    return camera.getType() != null ? new StringType(camera.getType()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_STATUS:
                    return camera.getStatus() != null
                            ? ("on".equalsIgnoreCase(camera.getStatus()) ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_SDSTATUS:
                    return camera.getSdStatus() != null
                            ? ("on".equalsIgnoreCase(camera.getSdStatus()) ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_ALIMSTATUS:
                    return camera.getAlimStatus() != null
                            ? ("on".equalsIgnoreCase(camera.getAlimStatus()) ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_NAME:
                    return camera.getName() != null ? new StringType(camera.getName()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_VPNURL:
                    return camera.getVpnUrl() != null ? new StringType(camera.getVpnUrl()) : UnDefType.UNDEF;
                case CHANNEL_WELCOME_CAMERA_ISLOCAL:
                    return camera.getIsLocal() != null ? (camera.getIsLocal() ? OnOffType.ON : OnOffType.OFF)
                            : UnDefType.UNDEF;

                case CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL:
                    return getLivePictureUrl();
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOPOOR_URL:
                    return getLiveVideoUrl(POOR);
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOLOW_URL:
                    return getLiveVideoUrl(LOW);
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOMEDIUM_URL:
                    return getLiveVideoUrl(MEDIUM);
                case CHANNEL_WELCOME_CAMERA_LIVEVIDEOHIGH_URL:
                    return getLiveVideoUrl(HIGH);

                default:
                    return super.getNAThingProperty(chanelId);
            }
        } catch (Exception e) {
            return UnDefType.UNDEF;
        }
    }

    /**
     * Get the url for the live picture
     *
     * @return Url of the live Picture or UnDefType.UNDEF
     */
    private State getLivePictureUrl() {
        State ret = UnDefType.UNDEF;
        if (camera != null) {
            String sUrl = getVideoUrl(camera.getId());
            if (sUrl != null) {
                ret = new StringType(sUrl + WELCOME_LIVE_PICTURE);
            }
        }

        return ret;
    }

    /**
     * Get the url of the live video strem
     *
     * @param i
     *
     * @return Url of the video stream or UnDefType.UNDEF
     */
    private State getLiveVideoUrl(int iQuality) {
        State ret = UnDefType.UNDEF;

        if (camera != null) {
            String sUrl = getVideoUrl(camera.getId());
            if (sUrl != null) {
                switch (iQuality) {
                    case POOR:
                        sUrl += WELCOME_LIVE_VIDEO_POOR;
                        break;
                    case LOW:
                        sUrl += WELCOME_LIVE_VIDEO_LOW;
                        break;
                    case MEDIUM:
                        sUrl += WELCOME_LIVE_VIDEO_MEDIUM;
                        break;
                    case HIGH:
                        sUrl += WELCOME_LIVE_VIDEO_HIGH;
                        break;
                    default:
                        sUrl = null;
                        break;
                }

                if (sUrl != null) {
                    ret = new StringType(sUrl);
                }
            }
        }

        return ret;
    }

}