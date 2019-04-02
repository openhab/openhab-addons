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
package org.openhab.binding.max.internal.device;

/**
 * Cube Lan Gateway.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class Cube extends Device {

    private boolean portalEnabled;
    private String portalUrl;
    private String timeZoneWinter;
    private String timeZoneDaylightSaving;

    public Cube(DeviceConfiguration c) {
        super(c);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.Cube;
    }

    @Override
    public String getName() {
        return "MAX! Cube Lan interface";
    }

    public boolean isPortalEnabled() {
        return portalEnabled;
    }

    public void setPortalEnabled(boolean portalEnabled) {
        this.portalEnabled = portalEnabled;
    }

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getTimeZoneWinter() {
        return timeZoneWinter;
    }

    public void setTimeZoneWinter(String timeZoneWinter) {
        this.timeZoneWinter = timeZoneWinter;
    }

    public String getTimeZoneDaylightSaving() {
        return timeZoneDaylightSaving;
    }

    public void setTimeZoneDaylightSaving(String timeZoneDaylightSaving) {
        this.timeZoneDaylightSaving = timeZoneDaylightSaving;
    }
}
