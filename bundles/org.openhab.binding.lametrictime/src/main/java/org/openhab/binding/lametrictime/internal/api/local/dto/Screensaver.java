/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for screensaver.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Screensaver {
    private Boolean enabled;
    private Modes modes;
    private String widget;

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Screensaver withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Modes getModes() {
        return modes;
    }

    public void setModes(Modes modes) {
        this.modes = modes;
    }

    public Screensaver withModes(Modes modes) {
        this.modes = modes;
        return this;
    }

    public String getWidget() {
        return widget;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    public Screensaver withWidget(String widget) {
        this.widget = widget;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Screensaver [enabled=");
        builder.append(enabled);
        builder.append(", modes=");
        builder.append(modes);
        builder.append(", widget=");
        builder.append(widget);
        builder.append("]");
        return builder.toString();
    }
}
