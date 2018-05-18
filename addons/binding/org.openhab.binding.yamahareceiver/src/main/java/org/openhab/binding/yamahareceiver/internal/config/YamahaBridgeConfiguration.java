/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.config;

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * Main settings.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class YamahaBridgeConfiguration {

    /**
     * The host name of the Yamaha AVR.
     */
    private String host;
    /**
     * Port under which the control interface is exposed on the Yamaha AVR.
     */
    private int port = 80;
    /**
     * Interval (in seconds) at which state updates are polled.
     */
    private int refreshInterval = 60; // Default: Every 1min
    /**
     * The default album image placeholder URL used when the source does not provide its own.
     */
    private String albumUrl = "";
    /**
     * Input source mapping for each command. This is a comma separated list of settings.
     */
    private String inputMapping = "";

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public String getAlbumUrl() {
        return albumUrl;
    }

    public Optional<String> getHostWithPort() {
        if (StringUtils.isEmpty(host)) {
            return Optional.empty();
        }
        return Optional.of(host + ":" + port);
    }

    public String getInputMapping() {
        return inputMapping;
    }
}
