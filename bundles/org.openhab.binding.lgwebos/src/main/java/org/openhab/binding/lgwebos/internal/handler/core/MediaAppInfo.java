/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.lgwebos.internal.handler.core;

import java.util.List;

/**
 * {@link MediaAppInfo} represents the payload that describes the foreground app's media state (playState) on WebOSTV.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
public class MediaAppInfo {
    // Example payload:
    // {"subscribed":true,"returnValue":true,"foregroundAppInfo":[{"appId":"netflix","playState":"loaded","type":"media","mediaId":"_fdTzfnKXXXXX","windowId":"_Window_Id_1"}]}
    // playState values: starting, loaded, playing, paused
    private List<AppInfo> foregroundAppInfo;

    public MediaAppInfo() {
        // no-argument constructor for gson
    }

    public MediaAppInfo(List<AppInfo> foregroundAppInfo) {
        this.foregroundAppInfo = foregroundAppInfo;
    }

    public List<AppInfo> getForegroundAppInfo() {
        return foregroundAppInfo;
    }

    @Override
    public String toString() {
        return "MediaAppInfo [foregroundAppInfo=" + foregroundAppInfo + "]";
    }
}
