/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model.cmd;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builds the save request for remote-start settings in the Robonect web interface.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class RemoteWebCommand implements Command {
    private final String url;

    /**
     * Creates a web-interface request to update a remote-start point.
     *
     * @param baseUrl mower web-interface base URL
     * @param remoteStart point number from 1 to 5
     * @param name point name
     * @param proportion selection proportion
     * @param distance distance to the point
     * @param path path setting
     * @param visible whether the point is visible
     * @param corridorCut whether corridor cutting is enabled
     * @throws IllegalArgumentException if the point number is outside 1 to 5
     */
    public RemoteWebCommand(String baseUrl, int remoteStart, String name, int proportion, int distance, int path,
            boolean visible, boolean corridorCut) {
        if (remoteStart < 1 || remoteStart > 5) {
            throw new IllegalArgumentException("Remote start must be between 1 and 5");
        }
        int index = remoteStart - 1;
        StringBuilder requestUrl = new StringBuilder(baseUrl).append("/remote?");
        if (visible) {
            requestUrl.append("visible").append(index).append("=on&");
        }
        requestUrl.append("name").append(index).append("=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
        requestUrl.append("&prop").append(index).append("=").append(proportion);
        requestUrl.append("&dist").append(index).append("=").append(distance);
        requestUrl.append("&path").append(index).append("=").append(path);
        if (corridorCut) {
            requestUrl.append("&cc").append(index).append("=on");
        }
        this.url = requestUrl.append("&save=").toString();
    }

    /**
     * Returns the web-interface URL prepared when this command was created.
     *
     * @param baseURL ignored because the URL is supplied to the constructor
     * @return the prepared request URL
     */
    @Override
    public String toCommandURL(String baseURL) {
        return url;
    }
}
