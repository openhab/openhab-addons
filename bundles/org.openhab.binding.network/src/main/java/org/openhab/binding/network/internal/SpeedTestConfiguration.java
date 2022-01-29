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
package org.openhab.binding.network.internal;

/**
 * The {@link SpeedTestConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class SpeedTestConfiguration {
    public Integer refreshInterval = 20;
    public Integer initialDelay = 5;
    public Integer uploadSize = 1000000;
    public Integer maxTimeout = 3;
    private String url;
    private String fileName;

    public String getUploadURL() {
        return url + (url.endsWith("/") ? "" : "/");
    }

    public String getDownloadURL() {
        return getUploadURL() + fileName;
    }
}
