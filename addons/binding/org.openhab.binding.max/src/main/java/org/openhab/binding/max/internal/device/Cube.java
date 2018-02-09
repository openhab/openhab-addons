/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

/**
 * Cube Lan Gateway.
 *
 * @author Marcel Verpaalen
 * @since 2.0.0
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
