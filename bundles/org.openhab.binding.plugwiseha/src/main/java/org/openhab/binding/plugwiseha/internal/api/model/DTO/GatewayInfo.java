/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.plugwiseha.internal.api.model.DTO;

import java.time.ZonedDateTime;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("gateway")
public class GatewayInfo extends PlugwiseBaseModel {

    private String name;
    private String description;
    private String hostname;
    private String timezone;
    private ZonedDateTime time;

    @XStreamAlias("gateway_environment")
    private GatewayEnvironment gatewayEnvironment;

    @XStreamAlias("vendor_name")
    private String vendorName;

    @XStreamAlias("vendor_model")
    private String vendorModel;

    @XStreamAlias("hardware_version")
    private String hardwareVersion;

    @XStreamAlias("firmware_version")
    private String firmwareVersion;

    @XStreamAlias("mac_address")
    private String macAddress;

    @XStreamAlias("lan_ip")
    private String lanIp;

    @XStreamAlias("wifi_ip")
    private String wifiIp;

    @XStreamAlias("last_reset_date")
    private ZonedDateTime lastResetDate;

    @XStreamAlias("last_boot_date")
    private ZonedDateTime lastBootDate;

    public ZonedDateTime getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHostname() {
        return hostname;
    }

    public String getTimezone() {
        return timezone;
    }

    public GatewayEnvironment getGatewayEnvironment() {
        return gatewayEnvironment;
    }

    public String getVendorName() {
        return vendorName;
    }

    public String getVendorModel() {
        return vendorModel;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getLanIp() {
        return lanIp;
    }

    public String getWifiIp() {
        return wifiIp;
    }

    public ZonedDateTime getLastResetDate() {
        return lastResetDate;
    }

    public ZonedDateTime getLastBootDate() {
        return lastBootDate;
    }
}
