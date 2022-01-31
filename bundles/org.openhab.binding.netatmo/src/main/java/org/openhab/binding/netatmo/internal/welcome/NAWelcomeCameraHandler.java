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
package org.openhab.binding.netatmo.internal.welcome;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.camera.CameraHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;

/**
 * {@link NAWelcomeCameraHandler} is the class used to handle the Welcome Camera Data
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
@NonNullByDefault
public class NAWelcomeCameraHandler extends CameraHandler {

    public NAWelcomeCameraHandler(Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_WELCOME_CAMERA_STATUS:
                return getStatusState();
            case CHANNEL_WELCOME_CAMERA_SDSTATUS:
                return getSdStatusState();
            case CHANNEL_WELCOME_CAMERA_ALIMSTATUS:
                return getAlimStatusState();
            case CHANNEL_WELCOME_CAMERA_ISLOCAL:
                return getIsLocalState();
            case CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL:
                return getLivePictureURLState();
            case CHANNEL_WELCOME_CAMERA_LIVEPICTURE:
                return getLivePictureState();
            case CHANNEL_WELCOME_CAMERA_LIVESTREAM_URL:
                return getLiveStreamState();
        }
        return super.getNAThingProperty(channelId);
    }
}
