/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.welcome;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.netatmo.internal.camera.NACameraHandler;

/**
 * {@link NAWelcomeCameraHandler} is the class used to handle the Welcome Camera Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
public class NAWelcomeCameraHandler extends NACameraHandler {

    public NAWelcomeCameraHandler(@NonNull Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    protected State getNAThingProperty(String chanelId) {
        switch (chanelId) {
            case CHANNEL_WELCOME_CAMERA_STATUS:
                return module != null ? toOnOffType(module.getStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_CAMERA_SDSTATUS:
                return module != null ? toOnOffType(module.getSdStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_CAMERA_ALIMSTATUS:
                return module != null ? toOnOffType(module.getAlimStatus()) : UnDefType.UNDEF;
            case CHANNEL_WELCOME_CAMERA_ISLOCAL:
                return (module == null || module.getIsLocal() == null) ? UnDefType.UNDEF
                        : module.getIsLocal() ? OnOffType.ON : OnOffType.OFF;
            case CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL:
                return getLivePictureURL() == null ? UnDefType.UNDEF : toStringType(getLivePictureURL());
            case CHANNEL_WELCOME_CAMERA_LIVEPICTURE:
                return getLivePictureURL() == null ? UnDefType.UNDEF : HttpUtil.downloadImage(getLivePictureURL());
            case CHANNEL_WELCOME_CAMERA_LIVESTREAM_URL:
                return getLiveStreamURL() == null ? UnDefType.UNDEF : new StringType(getLiveStreamURL());
        }
        return super.getNAThingProperty(chanelId);
    }
}
