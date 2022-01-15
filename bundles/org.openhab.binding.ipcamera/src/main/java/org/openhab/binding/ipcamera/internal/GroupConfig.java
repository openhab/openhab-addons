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
package org.openhab.binding.ipcamera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GroupConfig} handles the configuration of camera groups.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class GroupConfig {
    private int pollTime;
    private boolean motionChangesOrder = true;
    private String ipWhitelist = "";
    private String ffmpegLocation = "";
    private String ffmpegOutput = "";
    private String firstCamera = "";
    private String secondCamera = "";
    private String thirdCamera = "";
    private String forthCamera = "";

    public String getFirstCamera() {
        return firstCamera;
    }

    public String getSecondCamera() {
        return secondCamera;
    }

    public String getThirdCamera() {
        return thirdCamera;
    }

    public String getForthCamera() {
        return forthCamera;
    }

    public boolean getMotionChangesOrder() {
        return motionChangesOrder;
    }

    public String getIpWhitelist() {
        return ipWhitelist;
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public String getFfmpegOutput() {
        return ffmpegOutput;
    }

    public int getPollTime() {
        return pollTime;
    }
}
