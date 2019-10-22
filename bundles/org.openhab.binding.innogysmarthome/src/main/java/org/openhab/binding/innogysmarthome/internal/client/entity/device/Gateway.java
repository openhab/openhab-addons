/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.innogysmarthome.internal.client.entity.device;

import com.google.api.client.util.Key;

/**
 * Defines the {@link Gateway} structure.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Gateway {

    /**
     * Serial number of the gateway
     */
    @Key("serialNumber")
    private String serialNumber;

    /**
     * Connected status
     */
    @Key("connected")
    private Boolean connected;

    /**
     * Version of the app
     */
    @Key("appVersion")
    private String appVersion;

    /**
     * Version of the operating system
     */
    @Key("osVersion")
    private String osVersion;

    /**
     * Version of the configuration. Changes each time the configuration was changed via the innogy client app.
     */
    @Key("configVersion")
    private String configVersion;

    /**
     * @return the serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serial number to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return if the gateway is connected
     */
    public Boolean getConnected() {
        return connected;
    }

    /**
     * @param connected the connected state to set, true if connected
     */
    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    /**
     * @return the app version
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @param appVersion the app version to set
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * @return the os version
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @param osVersion the os version to set
     */
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /**
     * @return the configuration version
     */
    public String getConfigVersion() {
        return configVersion;
    }

    /**
     * @param configVersion the config version to set
     */
    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }
}
