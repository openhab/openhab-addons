/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.sony.internal.simpleip.SimpleIpHandler;

// TODO: Auto-generated Javadoc
/**
 * Configuration class for the {@link SimpleIpHandler}.
 *
 * @author Tim Roberts - Initial contribution
 */
public class DialConfig {

    /** The Constant DeviceMacAddress. */
    public static final String DeviceMacAddress = "deviceMacAddress";

    /** The device mac address. */
    private String deviceMacAddress;

    /** The network interface the system listens on (eth0 or wlan0). */
    public static final String DialUri = "dialUri";

    /** The dial uri. */
    private String dialUri;

    /** Refresh time (in seconds) to refresh attributes from the system. */
    public static final String Refresh = "refresh";

    /** The refresh. */
    private int refresh;

    /** The retry polling. */
    private int retryPolling;

    /**
     * Returns the IP address or host name.
     *
     * @return the IP address or host name
     */
    public String getIpAddress() {
        try {
            return new URI(dialUri).getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Returns the refresh interval (in seconds).
     *
     * @return the refresh interval (in seconds)
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Sets the refresh interval (in seconds).
     *
     * @param refresh the refresh interval (in seconds)
     */
    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    /**
     * Gets the device mac address.
     *
     * @return the device mac address
     */
    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    /**
     * Sets the device mac address.
     *
     * @param deviceMacAddress the new device mac address
     */
    public void setDeviceMacAddress(String deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
    }

    /**
     * Gets the dial uri.
     *
     * @return the dial uri
     */
    public String getDialUri() {
        return dialUri;
    }

    /**
     * Sets the dial uri.
     *
     * @param dialUrl the new dial uri
     */
    public void setDialUri(String dialUrl) {
        this.dialUri = dialUrl;
    }

    /**
     * Checks if is wol.
     *
     * @return true, if is wol
     */
    public boolean isWOL() {
        return !StringUtils.isEmpty(deviceMacAddress);
    }

    /**
     * Gets the retry polling.
     *
     * @return the retry polling
     */
    public int getRetryPolling() {
        return retryPolling;
    }

    /**
     * Sets the retry polling.
     *
     * @param retry the new retry polling
     */
    public void setRetryPolling(int retry) {
        this.retryPolling = retry;
    }
}
