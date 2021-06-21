/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link CameraChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class CameraChannelHelper extends AbstractChannelHelper {
    private static final String LIVE_PICTURE = "/live/snapshot_720.jpg";

    public CameraChannelHelper() {
        super(Set.of(GROUP_WELCOME));
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NAWelcome camera = (NAWelcome) naThing;
        switch (channelId) {
            case CHANNEL_CAMERA_IS_MONITORING:
                return camera.getStatus();
            case CHANNEL_CAMERA_SDSTATUS:
                return camera.getSdStatus();
            case CHANNEL_CAMERA_ALIMSTATUS:
                return camera.getAlimStatus();
            case CHANNEL_CAMERA_LIVEPICTURE_URL:
                return toStringType(getLivePictureURL(camera));
            case CHANNEL_CAMERA_LIVEPICTURE:
                return toRawType(getLivePictureURL(camera));
            case CHANNEL_CAMERA_LIVESTREAM_URL:
                return getLiveStreamURL(camera);
        }
        return null;
    }

    /**
     * Get the url for the live snapshot
     *
     * @param camera
     *
     * @return Url of the live snapshot
     */
    private @Nullable String getLivePictureURL(NAWelcome camera) {
        String result = camera.getVpnUrl();
        if (result != null) {
            return result + LIVE_PICTURE;
        }
        return null;
    }

    /**
     * Get the url for the live stream depending wether local or not
     *
     * @return Url of the live stream
     */
    private State getLiveStreamURL(NAWelcome camera) {
        String result = camera.getVpnUrl();
        if (result != null) {
            StringBuilder resultStringBuilder = new StringBuilder(result);
            resultStringBuilder.append("/live/index");
            if (camera.isLocal()) {
                resultStringBuilder.append("_local");
            }
            resultStringBuilder.append(".m3u8");
            return new StringType(resultStringBuilder.toString());
        }
        return UnDefType.NULL;
    }
}
