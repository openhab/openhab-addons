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
package org.openhab.binding.netatmo.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAWelcome extends NAModule {
    private @Nullable OnOffType status;
    private @Nullable String vpnUrl;
    private boolean isLocal;
    private @Nullable OnOffType sdStatus;
    private @Nullable OnOffType alimStatus;
    private PresenceLightMode lightModeStatus = PresenceLightMode.UNKNOWN;

    /**
     * If camera is monitoring (on/off)
     *
     * @return status
     **/
    public State getStatus() {
        OnOffType localStatus = status;
        return localStatus != null ? localStatus : UnDefType.NULL;
    }

    /**
     * Only for scope access_camera. Address of the camera
     *
     * @return vpnUrl
     **/
    public @Nullable String getVpnUrl() {
        return vpnUrl;
    }

    /**
     * Only for scope access_camera. If Camera and application requesting the information are on the same IP
     * (true/false)
     *
     * @return isLocal
     **/
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * If SD card status is ok (on/off)
     *
     * @return sdStatus
     **/
    public State getSdStatus() {
        OnOffType sd = sdStatus;
        return sd != null ? sd : UnDefType.NULL;
    }

    /**
     * If power supply is ok (on/off)
     *
     * @return alimStatus
     **/
    public State getAlimStatus() {
        OnOffType alim = alimStatus;
        return alim != null ? alim : UnDefType.NULL;
    }

    /**
     * State of (flood-)light
     *
     * @return lightModeStatus
     **/
    public PresenceLightMode getLightModeStatus() {
        return lightModeStatus;
    }
}
